package pe.gob.municipalidad.sigm.security.authentication;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.gob.municipalidad.sigm.common.response.ApiResponse;
import pe.gob.municipalidad.sigm.security.dto.AuthResponse;
import pe.gob.municipalidad.sigm.security.dto.LoginRequest;
import pe.gob.municipalidad.sigm.security.dto.RefreshTokenRequest;
import pe.gob.municipalidad.sigm.security.dto.UserProfileResponse;
import pe.gob.municipalidad.sigm.security.service.AuthService;

/**
 * Controlador REST para endpoints de autenticación, renovación de credenciales y perfil de sesión.
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final pe.gob.municipalidad.sigm.security.service.PasswordRecoveryService passwordRecoveryService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest request,
            HttpServletResponse response) {
        AuthResponse authResponse = authService.login(loginRequest, request, response);
        return ApiResponse.ok("Autenticación realizada exitosamente.", authResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @RequestBody(required = false) RefreshTokenRequest refreshRequest,
            HttpServletRequest request,
            HttpServletResponse response) {
        AuthResponse authResponse = authService.refresh(refreshRequest, request, response);
        return ApiResponse.ok("Sesión renovada correctamente.", authResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestBody(required = false) RefreshTokenRequest logoutRequest,
            HttpServletRequest request,
            HttpServletResponse response) {
        authService.logout(logoutRequest, request, response);
        return ApiResponse.ok("Cierre de sesión realizado con éxito.");
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> me() {
        UserProfileResponse profile = authService.getCurrentUserProfile();
        return ApiResponse.ok("Perfil de usuario obtenido con éxito.", profile);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(
            @Valid @RequestBody pe.gob.municipalidad.sigm.security.dto.ForgotPasswordRequest forgotRequest,
            HttpServletRequest request) {
        String mensaje = passwordRecoveryService.solicitarRecuperacion(forgotRequest.getIdentificador(), request);
        return ApiResponse.ok(mensaje, mensaje);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody pe.gob.municipalidad.sigm.security.dto.ResetPasswordRequest resetRequest,
            HttpServletRequest request) {
        passwordRecoveryService.restablecerContrasena(
                resetRequest.getToken(),
                resetRequest.getNuevaContrasena(),
                resetRequest.getConfirmarContrasena(),
                request
        );
        return ApiResponse.ok("Contraseña restablecida exitosamente.");
    }

    @GetMapping("/verify-reset-token")
    public ResponseEntity<ApiResponse<pe.gob.municipalidad.sigm.security.dto.ResetPasswordValidationResponse>> verifyResetToken(
            @org.springframework.web.bind.annotation.RequestParam("token") String token) {
        pe.gob.municipalidad.sigm.security.dto.ResetPasswordValidationResponse val = passwordRecoveryService.validarToken(token);
        return ApiResponse.ok("Validación de token completada.", val);
    }
}
