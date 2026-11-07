package pe.gob.municipalidad.sigm.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.security.service.PasswordService;
import pe.gob.municipalidad.sigm.security.service.SecurityAuditService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias de Manejo de Contraseñas y Bloqueo - SIGM-QUINUA")
class PasswordServiceTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private SecurityAuditService securityAuditService;

    @InjectMocks
    private PasswordService passwordService;

    private Usuario testUser;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(passwordService, "maxFailedAttempts", 5);
        testUser = Usuario.builder()
                .id(1L)
                .username("operador.mesa")
                .passwordHash("$2a$12$someSecureHashValue123456789012345678901234567890")
                .enabled(true)
                .failedAttempts(0)
                .accountLocked(false)
                .build();
    }

    @Test
    @DisplayName("1. Contraseña correcta: PasswordEncoder valida correctamente")
    void shouldMatchValidPassword() {
        when(passwordEncoder.matches("PasswordSeguro123!", testUser.getPasswordHash())).thenReturn(true);

        boolean matches = passwordService.matches("PasswordSeguro123!", testUser.getPasswordHash());

        assertThat(matches).isTrue();
        verify(passwordEncoder).matches("PasswordSeguro123!", testUser.getPasswordHash());
    }

    @Test
    @DisplayName("2. Contraseña incorrecta: PasswordEncoder rechaza credencial errónea")
    void shouldRejectInvalidPassword() {
        when(passwordEncoder.matches("PasswordErroneo", testUser.getPasswordHash())).thenReturn(false);

        boolean matches = passwordService.matches("PasswordErroneo", testUser.getPasswordHash());

        assertThat(matches).isFalse();
    }

    @Test
    @DisplayName("5. Usuario bloqueado: Bloqueo automático de cuenta tras 5 intentos fallidos consecutivos")
    void shouldLockAccountAfterMaxFailedAttempts() {
        testUser.setFailedAttempts(4);
        when(usuarioRepository.findByUsername("operador.mesa")).thenReturn(Optional.of(testUser));

        passwordService.recordFailedAttempt("operador.mesa");

        assertThat(testUser.getFailedAttempts()).isEqualTo(5);
        assertThat(testUser.getAccountLocked()).isTrue();
        verify(usuarioRepository).save(testUser);
        verify(securityAuditService).recordEvent(eq("operador.mesa"), eq("ACCOUNT_LOCKED"), eq("ALERTA"), anyString(), any());
    }

    @Test
    @DisplayName("Reset de intentos: Restablecimiento de contador a 0 tras login exitoso")
    void shouldResetFailedAttemptsOnSuccess() {
        testUser.setFailedAttempts(3);
        when(usuarioRepository.findByUsername("operador.mesa")).thenReturn(Optional.of(testUser));

        passwordService.resetFailedAttempts("operador.mesa");

        assertThat(testUser.getFailedAttempts()).isEqualTo(0);
        verify(usuarioRepository).save(testUser);
    }
}
