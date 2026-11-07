package pe.gob.municipalidad.sigm.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails;
import pe.gob.municipalidad.sigm.security.jwt.JwtTokenProvider;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Pruebas Unitarias del Proveedor JWT - SIGM-QUINUA")
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private final String validSecret = "ClaveSecretaSuperSeguraParaSIGMQuinuaHuamangaAyacucho2026MunicipalidadDistritalDeQuinua!";
    private final String invalidSecret = "OtraClaveSecretaCompletamenteDiferenteParaPruebaDeFirmaInvalida2026XYZ123456789!";
    private final String issuer = "SIGM-QUINUA";
    private final long expirationMs = 900000; // 15 minutos

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(validSecret, expirationMs, issuer);
    }

    private CustomUserDetails createSampleUserDetails() {
        return CustomUserDetails.builder()
                .id(1L)
                .username("juan.perez")
                .password("$2a$12$dummyHash")
                .enabled(true)
                .accountNonLocked(true)
                .nombreCompleto("Juan Pérez Quispe")
                .correo("juan.perez@quinua.gob.pe")
                .numeroDocumento("45678901")
                .roles(Set.of("ROLE_OPERADOR"))
                .permisos(Set.of("MESAPARTES_VIEW", "MESAPARTES_CREATE"))
                .authorities(List.of(
                        new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_OPERADOR"),
                        new org.springframework.security.core.authority.SimpleGrantedAuthority("MESAPARTES_VIEW")
                ))
                .build();
    }

    @Test
    @DisplayName("6. Generación de JWT: Token válido con claims institucionales correctos")
    void shouldGenerateValidJwtToken() {
        CustomUserDetails userDetails = createSampleUserDetails();
        String token = jwtTokenProvider.generateAccessToken(userDetails);

        assertThat(token).isNotBlank();
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        assertThat(jwtTokenProvider.getUsernameFromToken(token)).isEqualTo("juan.perez");
        assertThat(jwtTokenProvider.getUserIdFromToken(token)).isEqualTo(1L);
        assertThat(jwtTokenProvider.getClaims(token).get("nombre", String.class)).isEqualTo("Juan Pérez Quispe");
        assertThat(jwtTokenProvider.getAuthoritiesFromToken(token))
                .extracting("authority")
                .contains("ROLE_OPERADOR", "MESAPARTES_VIEW");
    }

    @Test
    @DisplayName("7. Firma JWT inválida: Rechazo de token firmado con clave distinta")
    void shouldRejectTokenWithInvalidSignature() {
        Key foreignKey = Keys.hmacShaKeyFor(invalidSecret.getBytes(StandardCharsets.UTF_8));
        String forgedToken = Jwts.builder()
                .subject("juan.perez")
                .issuer(issuer)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60000))
                .signWith(foreignKey)
                .compact();

        assertThat(jwtTokenProvider.validateToken(forgedToken)).isFalse();
    }

    @Test
    @DisplayName("8. Token expirado: Rechazo de token cuya vigencia ha caducado")
    void shouldRejectExpiredToken() {
        Key key = Keys.hmacShaKeyFor(validSecret.getBytes(StandardCharsets.UTF_8));
        String expiredToken = Jwts.builder()
                .subject("juan.perez")
                .issuer(issuer)
                .issuedAt(new Date(System.currentTimeMillis() - 200000))
                .expiration(new Date(System.currentTimeMillis() - 100000))
                .signWith(key)
                .compact();

        assertThat(jwtTokenProvider.validateToken(expiredToken)).isFalse();
    }

    @Test
    @DisplayName("9. Token manipulado: Rechazo de token con contenido o firma alterada")
    void shouldRejectTamperedToken() {
        CustomUserDetails userDetails = createSampleUserDetails();
        String validToken = jwtTokenProvider.generateAccessToken(userDetails);

        // Manipular el último carácter de la firma
        String tamperedToken = validToken.substring(0, validToken.length() - 4) + "X9Z=";

        assertThat(jwtTokenProvider.validateToken(tamperedToken)).isFalse();
    }
}
