package pe.gob.municipalidad.sigm.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.model.Persona;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.security.dto.ResetPasswordValidationResponse;
import pe.gob.municipalidad.sigm.security.model.PasswordResetToken;
import pe.gob.municipalidad.sigm.security.repository.PasswordResetTokenRepository;
import pe.gob.municipalidad.sigm.security.service.EmailNotificationService;
import pe.gob.municipalidad.sigm.security.service.PasswordRecoveryService;
import pe.gob.municipalidad.sigm.security.service.RefreshTokenService;
import pe.gob.municipalidad.sigm.security.service.SecurityAuditService;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias de Recuperación Segura de Contraseña - SIGM-QUINUA")
class PasswordRecoveryServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private SecurityAuditService securityAuditService;

    @Mock
    private EmailNotificationService emailNotificationService;

    @InjectMocks
    private PasswordRecoveryService passwordRecoveryService;

    private Usuario testUser;
    private Persona testPersona;
    private MockHttpServletRequest mockRequest;

    @BeforeEach
    void setUp() {
        mockRequest = new MockHttpServletRequest();
        mockRequest.setRemoteAddr("192.168.1.50");
        mockRequest.addHeader("User-Agent", "Mozilla/5.0 TestBrowser");

        testPersona = Persona.builder()
                .id(1L)
                .nombres("Carlos")
                .apellidoPaterno("Quispe")
                .correo("carlos.quispe@quinua.gob.pe")
                .build();

        testUser = Usuario.builder()
                .id(10L)
                .username("carlos.quispe")
                .persona(testPersona)
                .passwordHash("$2a$12$OldPasswordHash")
                .enabled(true)
                .accountLocked(false)
                .failedAttempts(0)
                .build();
    }

    @Test
    @DisplayName("13. Recuperación de contraseña: Si la cuenta existe, genera token e invalida previos")
    void shouldInitiatePasswordRecoverySuccessfully() {
        when(usuarioRepository.findByUsernameOrPersonaCorreo("carlos.quispe")).thenReturn(Optional.of(testUser));
        when(passwordResetTokenRepository.save(any(PasswordResetToken.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        String mensaje = passwordRecoveryService.solicitarRecuperacion("carlos.quispe", mockRequest);

        assertThat(mensaje).contains("Si los datos ingresados corresponden a una cuenta activa");
        verify(passwordResetTokenRepository).invalidatePendingTokensByUsuarioId(eq(10L), any(OffsetDateTime.class));
        verify(passwordResetTokenRepository).save(any(PasswordResetToken.class));
        verify(emailNotificationService).enviarEnlaceRecuperacion(eq("carlos.quispe@quinua.gob.pe"), eq("Carlos"), anyString());
        verify(securityAuditService).recordEvent(eq("carlos.quispe"), eq("PASSWORD_RESET_REQUESTED"), eq("EXITO"), anyString(), any());
    }

    @Test
    @DisplayName("Protección contra enumeración: Cuenta inexistente devuelve mensaje idéntico sin revelar ausencia")
    void shouldReturnGenericMessageForNonExistentAccount() {
        when(usuarioRepository.findByUsernameOrPersonaCorreo("desconocido")).thenReturn(Optional.empty());

        String mensaje = passwordRecoveryService.solicitarRecuperacion("desconocido", mockRequest);

        assertThat(mensaje).contains("Si los datos ingresados corresponden a una cuenta activa");
        verify(passwordResetTokenRepository, never()).save(any(PasswordResetToken.class));
        verify(emailNotificationService, never()).enviarEnlaceRecuperacion(any(), any(), any());
        verify(securityAuditService).recordEvent(eq("desconocido"), eq("PASSWORD_RESET_ATTEMPT_UNKNOWN"), eq("FALLO"), anyString(), any());
    }

    @Test
    @DisplayName("Cuenta inactiva: Solicitud rechazada sin emitir token")
    void shouldNotGenerateTokenForDisabledAccount() {
        testUser.setEnabled(false);
        when(usuarioRepository.findByUsernameOrPersonaCorreo("carlos.quispe")).thenReturn(Optional.of(testUser));

        String mensaje = passwordRecoveryService.solicitarRecuperacion("carlos.quispe", mockRequest);

        assertThat(mensaje).contains("Si los datos ingresados corresponden a una cuenta activa");
        verify(passwordResetTokenRepository, never()).save(any(PasswordResetToken.class));
        verify(emailNotificationService, never()).enviarEnlaceRecuperacion(any(), any(), any());
    }

    @Test
    @DisplayName("Validar token: Token válido retorna valid = true")
    void shouldValidateValidTokenSuccessfully() {
        String rawToken = "my-secure-token-123456789";
        String tokenHash = passwordRecoveryService.hashToken(rawToken);

        PasswordResetToken tokenEntity = PasswordResetToken.builder()
                .usuario(testUser)
                .tokenHash(tokenHash)
                .used(false)
                .expiresAt(OffsetDateTime.now().plusMinutes(20))
                .build();

        when(passwordResetTokenRepository.findByTokenHashWithUsuario(tokenHash)).thenReturn(Optional.of(tokenEntity));

        ResetPasswordValidationResponse response = passwordRecoveryService.validarToken(rawToken);

        assertThat(response.isValid()).isTrue();
        assertThat(response.getUsername()).isEqualTo("carlos.quispe");
    }

    @Test
    @DisplayName("14. Token expirado: Rechazo con valid = false")
    void shouldRejectExpiredToken() {
        String rawToken = "my-expired-token";
        String tokenHash = passwordRecoveryService.hashToken(rawToken);

        PasswordResetToken tokenEntity = PasswordResetToken.builder()
                .usuario(testUser)
                .tokenHash(tokenHash)
                .used(false)
                .expiresAt(OffsetDateTime.now().minusMinutes(5))
                .build();

        when(passwordResetTokenRepository.findByTokenHashWithUsuario(tokenHash)).thenReturn(Optional.of(tokenEntity));

        ResetPasswordValidationResponse response = passwordRecoveryService.validarToken(rawToken);

        assertThat(response.isValid()).isFalse();
        assertThat(response.getMessage()).contains("ha caducado");
    }

    @Test
    @DisplayName("15. Token reutilizado: Rechazo cuando ya fue consumido")
    void shouldRejectAlreadyUsedToken() {
        String rawToken = "my-used-token";
        String tokenHash = passwordRecoveryService.hashToken(rawToken);

        PasswordResetToken tokenEntity = PasswordResetToken.builder()
                .usuario(testUser)
                .tokenHash(tokenHash)
                .used(true)
                .usedAt(OffsetDateTime.now().minusMinutes(10))
                .expiresAt(OffsetDateTime.now().plusMinutes(20))
                .build();

        when(passwordResetTokenRepository.findByTokenHashWithUsuario(tokenHash)).thenReturn(Optional.of(tokenEntity));

        ResetPasswordValidationResponse response = passwordRecoveryService.validarToken(rawToken);

        assertThat(response.isValid()).isFalse();
        assertThat(response.getMessage()).contains("ya fue utilizado");
    }

    @Test
    @DisplayName("16 y 17. Cambio de contraseña exitoso: Actualiza con BCrypt, revoca sesiones y desbloquea cuenta")
    void shouldResetPasswordSuccessfullyAndRevokeAllSessions() {
        String rawToken = "valid-token-for-reset";
        String tokenHash = passwordRecoveryService.hashToken(rawToken);

        testUser.setAccountLocked(true);
        testUser.setFailedAttempts(5);

        PasswordResetToken tokenEntity = PasswordResetToken.builder()
                .usuario(testUser)
                .tokenHash(tokenHash)
                .used(false)
                .expiresAt(OffsetDateTime.now().plusMinutes(25))
                .build();

        when(passwordResetTokenRepository.findByTokenHashWithUsuario(tokenHash)).thenReturn(Optional.of(tokenEntity));
        when(passwordEncoder.encode("NuevoPassword2026#")).thenReturn("$2a$12$NewEncodedPasswordHash");

        passwordRecoveryService.restablecerContrasena(rawToken, "NuevoPassword2026#", "NuevoPassword2026#", mockRequest);

        // Verificaciones
        assertThat(testUser.getPasswordHash()).isEqualTo("$2a$12$NewEncodedPasswordHash");
        assertThat(testUser.getAccountLocked()).isFalse();
        assertThat(testUser.getFailedAttempts()).isEqualTo(0);
        assertThat(tokenEntity.getUsed()).isTrue();
        assertThat(tokenEntity.getUsedAt()).isNotNull();

        verify(usuarioRepository).save(testUser);
        verify(passwordResetTokenRepository).save(tokenEntity);
        // 17. Revocación de sesiones activas existentes del usuario
        verify(refreshTokenService).revokeAllUserTokens(10L);
        verify(securityAuditService).recordEvent(eq("carlos.quispe"), eq("PASSWORD_RESET_SUCCESS"), eq("EXITO"), anyString(), any());
    }

    @Test
    @DisplayName("Validación de coincidencia de contraseña: Error si no coinciden")
    void shouldFailWhenPasswordsDoNotMatch() {
        assertThatThrownBy(() -> passwordRecoveryService.restablecerContrasena(
                "token", "Password123#", "PasswordDistinto999#", mockRequest
        ))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("no coinciden");
    }

    @Test
    @DisplayName("Validación de política de contraseñas: Error si es menor a 8 caracteres o sin complejidad")
    void shouldFailWhenPasswordDoesNotMeetPolicy() {
        assertThatThrownBy(() -> passwordRecoveryService.restablecerContrasena(
                "token", "corta", "corta", mockRequest
        ))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("mínimo 8 caracteres");
    }
}
