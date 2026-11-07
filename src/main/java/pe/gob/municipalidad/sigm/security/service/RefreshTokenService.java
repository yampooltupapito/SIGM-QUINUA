package pe.gob.municipalidad.sigm.security.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.security.model.RefreshToken;
import pe.gob.municipalidad.sigm.security.repository.RefreshTokenRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.UUID;

/**
 * Servicio de gestión, rotación y revocación segura de Refresh Tokens.
 * Implementa detección de reutilización maliciosa con invalidación automática de la familia de tokens.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UsuarioRepository usuarioRepository;
    private final SecurityAuditService securityAuditService;

    @Value("${sigm.security.jwt.refresh-token-expiration-ms:604800000}")
    private long refreshTokenExpirationMs;

    public record RefreshTokenResult(String rawToken, String tokenId, RefreshToken entity) {}

    @Transactional
    public RefreshTokenResult createRefreshToken(Usuario usuario, HttpServletRequest request) {
        String rawToken = UUID.randomUUID().toString() + "-" + UUID.randomUUID().toString();
        String tokenId = UUID.randomUUID().toString();
        String familyId = UUID.randomUUID().toString();
        String tokenHash = hashToken(rawToken);

        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime expiresAt = now.plusSeconds(refreshTokenExpirationMs / 1000);

        RefreshToken refreshToken = RefreshToken.builder()
                .usuario(usuario)
                .tokenId(tokenId)
                .tokenHash(tokenHash)
                .familyId(familyId)
                .revoked(false)
                .expiresAt(expiresAt)
                .createdAt(now)
                .createdByIp(extractClientIp(request))
                .userAgent(extractUserAgent(request))
                .build();

        RefreshToken saved = refreshTokenRepository.save(refreshToken);
        return new RefreshTokenResult(rawToken, tokenId, saved);
    }

    @Transactional
    public RefreshTokenResult rotateRefreshToken(String rawRefreshToken, HttpServletRequest request) {
        if (rawRefreshToken == null || rawRefreshToken.trim().isEmpty()) {
            throw new BusinessException("ERR_AUTH_REFRESH", "Token de actualización no proporcionado.");
        }

        String tokenHash = hashToken(rawRefreshToken.trim());
        RefreshToken currentToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> {
                    securityAuditService.recordEvent(null, "REFRESH_FAILED", "FALLO", "Intento de rotación con token inexistente", request);
                    return new BusinessException("ERR_AUTH_REFRESH", "Token de actualización inválido o inexistente.");
                });

        // Detección de Reutilización Maliciosa: si el token ya estaba revocado, toda la familia se invalida inmediatamente
        if (Boolean.TRUE.equals(currentToken.getRevoked())) {
            OffsetDateTime now = OffsetDateTime.now();
            refreshTokenRepository.revokeFamily(currentToken.getFamilyId(), now);
            String username = currentToken.getUsuario().getUsername();
            log.error("¡ALERTA DE SEGURIDAD! Detección de reutilización de refresh token revocado para el usuario '{}'. Toda la familia de tokens '{}' ha sido revocada.",
                    username, currentToken.getFamilyId());
            securityAuditService.recordEvent(username, "REFRESH_REUSED_REJECTED", "ALERTA",
                    "Detección de reutilización de token revocado. Familia invalidada.", request);
            throw new BusinessException("ERR_TOKEN_REUSED", "Token de sesión previamente utilizado. Por seguridad, su sesión ha sido invalidada. Inicie sesión nuevamente.");
        }

        if (currentToken.isExpired()) {
            currentToken.setRevoked(true);
            currentToken.setRevokedAt(OffsetDateTime.now());
            refreshTokenRepository.save(currentToken);
            throw new BusinessException("ERR_AUTH_REFRESH", "El token de actualización ha expirado. Inicie sesión nuevamente.");
        }

        // Revocar el token actual usado
        OffsetDateTime now = OffsetDateTime.now();
        currentToken.setRevoked(true);
        currentToken.setRevokedAt(now);
        refreshTokenRepository.save(currentToken);

        // Emitir nuevo token dentro de la misma familia
        String newRawToken = UUID.randomUUID().toString() + "-" + UUID.randomUUID().toString();
        String newTokenId = UUID.randomUUID().toString();
        String newTokenHash = hashToken(newRawToken);
        OffsetDateTime newExpiresAt = now.plusSeconds(refreshTokenExpirationMs / 1000);

        RefreshToken nextToken = RefreshToken.builder()
                .usuario(currentToken.getUsuario())
                .tokenId(newTokenId)
                .tokenHash(newTokenHash)
                .familyId(currentToken.getFamilyId())
                .revoked(false)
                .expiresAt(newExpiresAt)
                .createdAt(now)
                .createdByIp(extractClientIp(request))
                .userAgent(extractUserAgent(request))
                .build();

        RefreshToken saved = refreshTokenRepository.save(nextToken);
        securityAuditService.recordEvent(currentToken.getUsuario().getUsername(), "REFRESH_SUCCESS", "EXITO", "Renovación exitosa de sesión", request);

        return new RefreshTokenResult(newRawToken, newTokenId, saved);
    }

    @Transactional
    public void revokeToken(String rawRefreshToken, HttpServletRequest request) {
        if (rawRefreshToken != null && !rawRefreshToken.trim().isEmpty()) {
            String tokenHash = hashToken(rawRefreshToken.trim());
            refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(token -> {
                token.setRevoked(true);
                token.setRevokedAt(OffsetDateTime.now());
                refreshTokenRepository.save(token);
                securityAuditService.recordEvent(token.getUsuario().getUsername(), "LOGOUT", "EXITO", "Cierre de sesión y revocación de token", request);
            });
        }
    }

    @Transactional
    public void revokeAllUserTokens(Long usuarioId) {
        refreshTokenRepository.revokeAllByUsuarioId(usuarioId, OffsetDateTime.now());
    }

    public String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algoritmo de hash SHA-256 no disponible en la JVM.", e);
        }
    }

    private String extractClientIp(HttpServletRequest request) {
        if (request == null) return "0.0.0.0";
        String xf = request.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isEmpty()) {
            return xf.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String extractUserAgent(HttpServletRequest request) {
        if (request == null) return "Unknown";
        String ua = request.getHeader("User-Agent");
        return (ua != null && ua.length() > 250) ? ua.substring(0, 250) : (ua != null ? ua : "Unknown");
    }
}
