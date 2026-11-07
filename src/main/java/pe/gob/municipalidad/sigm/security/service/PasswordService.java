package pe.gob.municipalidad.sigm.security.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;

/**
 * Servicio seguro para cifrado, verificación de contraseñas y control de intentos fallidos.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordService {

    private final PasswordEncoder passwordEncoder;
    private final UsuarioRepository usuarioRepository;
    private final SecurityAuditService securityAuditService;

    @Value("${sigm.security.max-failed-login-attempts:5}")
    private int maxFailedAttempts;

    public String encode(String rawPassword) {
        if (rawPassword == null || rawPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("La contraseña no puede estar vacía.");
        }
        return passwordEncoder.encode(rawPassword);
    }

    public boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    @Transactional
    public void recordFailedAttempt(String username) {
        usuarioRepository.findByUsername(username).ifPresent(usuario -> {
            int newAttempts = (usuario.getFailedAttempts() != null ? usuario.getFailedAttempts() : 0) + 1;
            usuario.setFailedAttempts(newAttempts);

            if (newAttempts >= maxFailedAttempts) {
                usuario.setAccountLocked(true);
                log.warn("Cuenta de usuario '{}' bloqueada tras alcanzar {} intentos fallidos de autenticación.", username, newAttempts);
                securityAuditService.recordEvent(username, "ACCOUNT_LOCKED", "ALERTA", "Cuenta bloqueada temporalmente por alcanzar el límite de intentos fallidos", null);
            }
            usuarioRepository.save(usuario);
        });
    }

    @Transactional
    public void resetFailedAttempts(String username) {
        usuarioRepository.findByUsername(username).ifPresent(usuario -> {
            if (usuario.getFailedAttempts() != null && usuario.getFailedAttempts() > 0) {
                usuario.setFailedAttempts(0);
                usuarioRepository.save(usuario);
            }
        });
    }
}
