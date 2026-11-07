package pe.gob.municipalidad.sigm.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Proveedor y validador de tokens JWT utilizando JJWT 0.12.x para SIGM-QUINUA.
 * Garantiza firma HMAC-SHA256 con claves criptográficas de longitud suficiente y validación estricta de claims.
 */
@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long accessTokenExpirationMs;
    private final String issuer;

    public JwtTokenProvider(
            @Value("${sigm.security.jwt.secret:sigm-quinua-municipalidad-distrital-quinua-huamanga-ayacucho-secret-key-2026-secure-token-sha256}") String secret,
            @Value("${sigm.security.jwt.access-token-expiration-ms:900000}") long accessTokenExpirationMs,
            @Value("${sigm.security.jwt.issuer:SIGM-QUINUA}") String issuer) {

        byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < 32) {
            throw new IllegalArgumentException("La clave secreta JWT debe tener al menos 256 bits (32 caracteres).");
        }
        this.key = Keys.hmacShaKeyFor(secretBytes);
        this.accessTokenExpirationMs = accessTokenExpirationMs;
        this.issuer = issuer;
    }

    public String generateAccessToken(CustomUserDetails userDetails) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpirationMs);

        List<String> allAuthorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        // Evita superar el límite de 4 KB de cookies del navegador (RFC 6265) y de 8 KB de cabeceras HTTP en Tomcat
        // cuando el rol (ej. ROLE_ADMIN con 438 permisos) posee decenas o cientos de permisos.
        boolean authCompact = allAuthorities.size() > 25;
        List<String> tokenAuthorities = authCompact
                ? allAuthorities.stream()
                        .filter(a -> a.startsWith("ROLE_")
                                || "ADMINISTRACION_MANAGE".equals(a)
                                || "PORTAL_MANAGE".equals(a))
                        .collect(Collectors.toList())
                : allAuthorities;

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .id(UUID.randomUUID().toString())
                .issuer(issuer)
                .issuedAt(now)
                .expiration(expiryDate)
                .claim("userId", userDetails.getId())
                .claim("personaId", userDetails.getPersonaId())
                .claim("nombre", userDetails.getNombreCompleto())
                .claim("roles", userDetails.getRoles())
                .claim("authorities", tokenAuthorities)
                .claim("authCompact", authCompact)
                .signWith(key)
                .compact();
    }

    public boolean isAuthoritiesCompacted(String token) {
        try {
            Claims claims = getClaims(token);
            return Boolean.TRUE.equals(claims.get("authCompact", Boolean.class));
        } catch (Exception ex) {
            return false;
        }
    }

    public boolean validateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }
        try {
            Claims claims = getClaims(token);
            return issuer.equals(claims.getIssuer()) && claims.getExpiration().after(new Date());
        } catch (SignatureException ex) {
            log.warn("Firma criptográfica de token JWT inválida: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            log.warn("Token JWT con formato o estructura mal formada: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            log.debug("Token JWT expirado: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.warn("Algoritmo de token JWT no soportado: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.warn("Token JWT vacío o nulo: {}", ex.getMessage());
        } catch (JwtException ex) {
            log.warn("Error en validación de token JWT: {}", ex.getMessage());
        }
        return false;
    }

    public String getUsernameFromToken(String token) {
        return getClaims(token).getSubject();
    }

    public Long getUserIdFromToken(String token) {
        Object userId = getClaims(token).get("userId");
        if (userId instanceof Number number) {
            return number.longValue();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public Collection<? extends GrantedAuthority> getAuthoritiesFromToken(String token) {
        Claims claims = getClaims(token);
        List<String> authorities = claims.get("authorities", List.class);
        if (authorities == null) {
            return List.of();
        }
        return authorities.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .requireIssuer(issuer)
                .build()
                .parseSignedClaims(token.trim())
                .getPayload();
    }

    public long getAccessTokenExpirationMs() {
        return accessTokenExpirationMs;
    }
}
