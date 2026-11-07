package pe.gob.municipalidad.sigm.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.security.model.RefreshToken;
import pe.gob.municipalidad.sigm.security.repository.RefreshTokenRepository;
import pe.gob.municipalidad.sigm.security.service.RefreshTokenService;
import pe.gob.municipalidad.sigm.security.service.SecurityAuditService;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias de Refresh Token y Revocación - SIGM-QUINUA")
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private SecurityAuditService securityAuditService;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private Usuario sampleUser;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenExpirationMs", 604800000L); // 7 días
        sampleUser = Usuario.builder()
                .id(10L)
                .username("carlos.mendoza")
                .enabled(true)
                .accountLocked(false)
                .failedAttempts(0)
                .build();
    }

    @Test
    @DisplayName("10. Refresh token válido: Generación y rotación atómica de token")
    void shouldCreateAndRotateRefreshTokenSuccessfully() {
        // 1. Creación
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RefreshTokenService.RefreshTokenResult created = refreshTokenService.createRefreshToken(sampleUser, null);
        assertThat(created).isNotNull();
        assertThat(created.rawToken()).isNotBlank();
        assertThat(created.tokenId()).isNotBlank();
        assertThat(created.entity().getTokenHash()).isNotBlank();

        // 2. Rotación con token válido
        String currentHash = created.entity().getTokenHash();
        when(refreshTokenRepository.findByTokenHash(currentHash)).thenReturn(Optional.of(created.entity()));

        RefreshTokenService.RefreshTokenResult rotated = refreshTokenService.rotateRefreshToken(created.rawToken(), null);
        assertThat(rotated).isNotNull();
        assertThat(rotated.rawToken()).isNotEqualTo(created.rawToken());
        assertThat(rotated.entity().getFamilyId()).isEqualTo(created.entity().getFamilyId());
        assertThat(created.entity().getRevoked()).isTrue();
    }

    @Test
    @DisplayName("11. Refresh token revocado: Rechazo de token expirado o inválido")
    void shouldRejectInvalidOrExpiredRefreshToken() {
        // Token inexistente
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshTokenService.rotateRefreshToken("token-falso-inexistente", null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("inválido o inexistente");

        // Token expirado
        String rawToken = "token-expirado";
        String hash = refreshTokenService.hashToken(rawToken);
        RefreshToken expiredToken = RefreshToken.builder()
                .usuario(sampleUser)
                .tokenHash(hash)
                .familyId("fam-123")
                .revoked(false)
                .expiresAt(OffsetDateTime.now().minusDays(1))
                .build();

        when(refreshTokenRepository.findByTokenHash(hash)).thenReturn(Optional.of(expiredToken));

        assertThatThrownBy(() -> refreshTokenService.rotateRefreshToken(rawToken, null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ha expirado");
    }

    @Test
    @DisplayName("12. Reutilización de refresh token: Detección maliciosa e invalidación total de familia")
    void shouldDetectReusedRefreshTokenAndInvalidateFamily() {
        String rawToken = "token-reutilizado-sospechoso";
        String hash = refreshTokenService.hashToken(rawToken);
        String familyId = "fam-comprometida-999";

        RefreshToken revokedToken = RefreshToken.builder()
                .usuario(sampleUser)
                .tokenHash(hash)
                .familyId(familyId)
                .revoked(true) // Ya fue utilizado / revocado antes
                .revokedAt(OffsetDateTime.now().minusHours(2))
                .expiresAt(OffsetDateTime.now().plusDays(5))
                .build();

        when(refreshTokenRepository.findByTokenHash(hash)).thenReturn(Optional.of(revokedToken));

        assertThatThrownBy(() -> refreshTokenService.rotateRefreshToken(rawToken, null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Token de sesión previamente utilizado");

        // Verificar que se invalidó toda la familia de tokens en la base de datos
        verify(refreshTokenRepository).revokeFamily(eq(familyId), any(OffsetDateTime.class));
        verify(securityAuditService).recordEvent(eq("carlos.mendoza"), eq("REFRESH_REUSED_REJECTED"), eq("ALERTA"), anyString(), any());
    }

    @Test
    @DisplayName("13. Logout: Revocación explícita del token de actualización")
    void shouldRevokeRefreshTokenOnLogout() {
        String rawToken = "token-para-logout";
        String hash = refreshTokenService.hashToken(rawToken);

        RefreshToken token = RefreshToken.builder()
                .usuario(sampleUser)
                .tokenHash(hash)
                .familyId("fam-logout")
                .revoked(false)
                .expiresAt(OffsetDateTime.now().plusDays(3))
                .build();

        when(refreshTokenRepository.findByTokenHash(hash)).thenReturn(Optional.of(token));

        refreshTokenService.revokeToken(rawToken, null);

        assertThat(token.getRevoked()).isTrue();
        assertThat(token.getRevokedAt()).isNotNull();
        verify(refreshTokenRepository).save(token);
        verify(securityAuditService).recordEvent(eq("carlos.mendoza"), eq("LOGOUT"), eq("EXITO"), anyString(), any());
    }
}
