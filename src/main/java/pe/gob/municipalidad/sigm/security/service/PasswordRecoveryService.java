package pe.gob.municipalidad.sigm.security.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.security.dto.ResetPasswordValidationResponse;
import pe.gob.municipalidad.sigm.security.model.PasswordResetToken;
import pe.gob.municipalidad.sigm.security.repository.PasswordResetTokenRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.Optional;

/**
 * Servicio de gestión del flujo de recuperación segura de contraseñas.
 * Implementa almacenamiento exclusivo de huellas SHA-256, expiración corta, un solo uso
 * y prevención de enumeración de usuarios.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordRecoveryService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final SecurityAuditService securityAuditService;
    private final EmailNotificationService emailNotificationService;

    private static final int TOKEN_EXPIRATION_MINUTES = 30;
    private static final String GENERIC_RESPONSE_MESSAGE =
            "Si los datos ingresados corresponden a una cuenta activa registrada, se ha enviado un enlace seguro de recuperación.";

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Solicita la recuperación de acceso.
     * Siempre devuelve una respuesta genérica sin revelar si el usuario o correo existen en el sistema.
     */
    @Transactional
    public String solicitarRecuperacion(String identificador, HttpServletRequest request) {
        if (identificador == null || identificador.trim().isEmpty()) {
            return GENERIC_RESPONSE_MESSAGE;
        }

        String idLimpio = identificador.trim();
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsernameOrPersonaCorreo(idLimpio);

        if (usuarioOpt.isEmpty()) {
            log.info("[Recuperación] Solicitud para identificador no existente: '{}'", idLimpio);
            securityAuditService.recordEvent(idLimpio, "PASSWORD_RESET_ATTEMPT_UNKNOWN", "FALLO",
                    "Solicitud de recuperación para cuenta inexistente", request);
            return GENERIC_RESPONSE_MESSAGE;
        }

        Usuario usuario = usuarioOpt.get();

        if (Boolean.FALSE.equals(usuario.getEnabled())) {
            log.warn("[Recuperación] Solicitud para usuario deshabilitado: '{}'", usuario.getUsername());
            securityAuditService.recordEvent(usuario.getUsername(), "PASSWORD_RESET_DISABLED_USER", "FALLO",
                    "Solicitud de recuperación para cuenta inactiva", request);
            return GENERIC_RESPONSE_MESSAGE;
        }

        // 1. Invalidar cualquier token de recuperación pendiente previo
        passwordResetTokenRepository.invalidatePendingTokensByUsuarioId(usuario.getId(), OffsetDateTime.now());

        // 2. Generar token aleatorio criptográficamente seguro (32 bytes = 64 hex chars)
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        String rawToken = HexFormat.of().formatHex(randomBytes);
        String tokenHash = hashToken(rawToken);

        // 3. Persistir entidad con expiración corta (30 minutos)
        PasswordResetToken tokenEntity = PasswordResetToken.builder()
                .usuario(usuario)
                .tokenHash(tokenHash)
                .expiresAt(OffsetDateTime.now().plusMinutes(TOKEN_EXPIRATION_MINUTES))
                .used(false)
                .ipSolicitud(extraerIp(request))
                .userAgent(extraerUserAgent(request))
                .createdAt(OffsetDateTime.now())
                .build();

        passwordResetTokenRepository.save(tokenEntity);

        // 4. Enviar notificación
        String correo = usuario.getPersona() != null ? usuario.getPersona().getCorreo() : null;
        String nombre = usuario.getPersona() != null ? usuario.getPersona().getNombres() : usuario.getUsername();
        if (correo != null && !correo.isBlank()) {
            emailNotificationService.enviarEnlaceRecuperacion(correo, nombre, rawToken);
        }

        // 5. Auditoría
        securityAuditService.recordEvent(usuario.getUsername(), "PASSWORD_RESET_REQUESTED", "EXITO",
                "Emisión de enlace seguro de recuperación de contraseña", request);

        return GENERIC_RESPONSE_MESSAGE;
    }

    /**
     * Valida la vigencia y estado de un token de recuperación.
     */
    @Transactional(readOnly = true)
    public ResetPasswordValidationResponse validarToken(String rawToken) {
        if (rawToken == null || rawToken.trim().isEmpty()) {
            return ResetPasswordValidationResponse.builder()
                    .valid(false)
                    .message("El token de recuperación no fue proporcionado.")
                    .build();
        }

        String tokenHash = hashToken(rawToken.trim());
        Optional<PasswordResetToken> tokenOpt = passwordResetTokenRepository.findByTokenHashWithUsuario(tokenHash);

        if (tokenOpt.isEmpty()) {
            return ResetPasswordValidationResponse.builder()
                    .valid(false)
                    .message("El enlace de recuperación es inválido o no existe.")
                    .build();
        }

        PasswordResetToken token = tokenOpt.get();

        if (Boolean.TRUE.equals(token.getUsed())) {
            return ResetPasswordValidationResponse.builder()
                    .valid(false)
                    .message("Este enlace de recuperación ya fue utilizado previamente. Solicite uno nuevo.")
                    .build();
        }

        if (token.isExpired()) {
            return ResetPasswordValidationResponse.builder()
                    .valid(false)
                    .message("El enlace de recuperación ha caducado por límite de tiempo. Solicite uno nuevo.")
                    .build();
        }

        return ResetPasswordValidationResponse.builder()
                .valid(true)
                .username(token.getUsuario().getUsername())
                .message("Token válido para restablecimiento.")
                .build();
    }

    /**
     * Restablece la contraseña utilizando un token válido.
     * Codifica con BCrypt, revoca todas las sesiones previas del usuario y registra auditoría.
     */
    @Transactional
    public void restablecerContrasena(String rawToken, String nuevaContrasena, String confirmarContrasena, HttpServletRequest request) {
        if (rawToken == null || rawToken.trim().isEmpty()) {
            throw new BusinessException("ERR_TOKEN_REQUERIDO", "El token de recuperación es obligatorio.");
        }

        if (nuevaContrasena == null || nuevaContrasena.trim().isEmpty()) {
            throw new BusinessException("ERR_PASSWORD_VACIO", "La nueva contraseña no puede estar vacía.");
        }

        if (!nuevaContrasena.equals(confirmarContrasena)) {
            throw new BusinessException("ERR_PASSWORDS_NO_COINCIDEN", "Las contraseñas ingresadas no coinciden.");
        }

        validarPoliticaContrasena(nuevaContrasena);

        String tokenHash = hashToken(rawToken.trim());
        PasswordResetToken token = passwordResetTokenRepository.findByTokenHashWithUsuario(tokenHash)
                .orElseThrow(() -> new BusinessException("ERR_TOKEN_INVALIDO", "El token de recuperación no es válido."));

        if (Boolean.TRUE.equals(token.getUsed())) {
            securityAuditService.recordEvent(token.getUsuario().getUsername(), "PASSWORD_RESET_REUSED_ATTEMPT", "ALERTA",
                    "Intento de reutilizar token de recuperación ya consumido", request);
            throw new BusinessException("ERR_TOKEN_UTILIZADO", "El enlace de recuperación ya fue utilizado previamente.");
        }

        if (token.isExpired()) {
            securityAuditService.recordEvent(token.getUsuario().getUsername(), "PASSWORD_RESET_EXPIRED_ATTEMPT", "FALLO",
                    "Intento de usar token de recuperación expirado", request);
            throw new BusinessException("ERR_TOKEN_EXPIRADO", "El enlace de recuperación ha expirado. Por favor, solicite uno nuevo.");
        }

        Usuario usuario = token.getUsuario();

        // Actualizar contraseña con BCrypt
        usuario.setPasswordHash(passwordEncoder.encode(nuevaContrasena));
        usuario.setFailedAttempts(0);
        usuario.setAccountLocked(false); // Desbloqueo tras restablecimiento exitoso
        usuarioRepository.save(usuario);

        // Marcar token como utilizado
        token.setUsed(true);
        token.setUsedAt(OffsetDateTime.now());
        passwordResetTokenRepository.save(token);

        // Invalidar todas las sesiones activas y refresh tokens existentes del usuario
        refreshTokenService.revokeAllUserTokens(usuario.getId());

        // Registrar auditoría
        securityAuditService.recordEvent(usuario.getUsername(), "PASSWORD_RESET_SUCCESS", "EXITO",
                "Restablecimiento exitoso de contraseña e invalidación de sesiones previas", request);

        log.info("[Seguridad] Contraseña restablecida exitosamente para usuario '{}'", usuario.getUsername());
    }

    public String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algoritmo criptográfico SHA-256 no disponible en la JVM", e);
        }
    }

    private void validarPoliticaContrasena(String password) {
        if (password.length() < 8) {
            throw new BusinessException("ERR_PASSWORD_DEBIL", "La contraseña debe tener como mínimo 8 caracteres.");
        }
        if (password.length() > 100) {
            throw new BusinessException("ERR_PASSWORD_LONGITUD", "La contraseña no puede exceder 100 caracteres.");
        }
        boolean tieneLetra = password.matches(".*[a-zA-Z].*");
        boolean tieneNumeroODigito = password.matches(".*[0-9!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");
        if (!tieneLetra || !tieneNumeroODigito) {
            throw new BusinessException("ERR_PASSWORD_COMPLEJIDAD",
                    "La contraseña debe combinar letras con números o caracteres especiales.");
        }
    }

    private String extraerIp(HttpServletRequest request) {
        if (request == null) return "0.0.0.0";
        String xf = request.getHeader("X-Forwarded-For");
        return (xf != null && !xf.isBlank()) ? xf.split(",")[0].trim() : request.getRemoteAddr();
    }

    private String extraerUserAgent(HttpServletRequest request) {
        if (request == null) return "Unknown";
        String ua = request.getHeader("User-Agent");
        return (ua != null && ua.length() > 250) ? ua.substring(0, 250) : (ua != null ? ua : "Unknown");
    }
}
