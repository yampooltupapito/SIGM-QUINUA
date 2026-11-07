package pe.gob.municipalidad.sigm.security.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.constants.SecurityConstants;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.exception.UnauthorizedException;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails;
import pe.gob.municipalidad.sigm.security.authentication.UserDetailsServiceImpl;
import pe.gob.municipalidad.sigm.security.dto.AuthResponse;
import pe.gob.municipalidad.sigm.security.dto.LoginRequest;
import pe.gob.municipalidad.sigm.security.dto.RefreshTokenRequest;
import pe.gob.municipalidad.sigm.security.dto.UserProfileResponse;
import pe.gob.municipalidad.sigm.security.jwt.JwtTokenProvider;

/**
 * Servicio central de autenticación, renovación de credenciales y gestión de sesiones.
 */
@Slf4j
@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final PasswordService passwordService;
    private final SecurityAuditService securityAuditService;
    private final UserDetailsServiceImpl userDetailsService;
    private final UsuarioRepository usuarioRepository;

    public AuthService(
            AuthenticationManager authenticationManager,
            JwtTokenProvider jwtTokenProvider,
            RefreshTokenService refreshTokenService,
            PasswordService passwordService,
            SecurityAuditService securityAuditService,
            UserDetailsServiceImpl userDetailsService,
            UsuarioRepository usuarioRepository
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenService = refreshTokenService;
        this.passwordService = passwordService;
        this.securityAuditService = securityAuditService;
        this.userDetailsService = userDetailsService;
        this.usuarioRepository = usuarioRepository;
    }

    @Value("${COOKIE_SECURE:false}")
    private boolean cookieSecure;

    @Transactional
    public AuthResponse login(LoginRequest loginRequest, HttpServletRequest request, HttpServletResponse response) {
        String username = loginRequest.getUsername().trim();

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, loginRequest.getPassword())
            );

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Restablecer contador de intentos fallidos tras éxito
            passwordService.resetFailedAttempts(username);

            // Generar Access Token y Refresh Token rotativo
            String accessToken = jwtTokenProvider.generateAccessToken(userDetails);
            Usuario usuario = usuarioRepository.findById(userDetails.getId()).orElseThrow();
            RefreshTokenService.RefreshTokenResult refreshResult = refreshTokenService.createRefreshToken(usuario, request);

            // Establecer Cookies HttpOnly Seguras
            attachAuthCookies(response, accessToken, refreshResult.rawToken());

            // Registrar Auditoría
            securityAuditService.recordEvent(username, "LOGIN_SUCCESS", "EXITO", "Autenticación exitosa", request);

            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshResult.rawToken())
                    .tokenType("Bearer")
                    .expiresIn(jwtTokenProvider.getAccessTokenExpirationMs() / 1000)
                    .username(userDetails.getUsername())
                    .nombreCompleto(userDetails.getNombreCompleto())
                    .roles(userDetails.getRoles())
                    .permisos(userDetails.getPermisos())
                    .build();

        } catch (BadCredentialsException ex) {
            passwordService.recordFailedAttempt(username);
            securityAuditService.recordEvent(username, "LOGIN_FAILED", "FALLO", "Contraseña incorrecta", request);
            throw new BadCredentialsException("Credenciales de acceso incorrectas.");
        } catch (LockedException ex) {
            securityAuditService.recordEvent(username, "LOGIN_FAILED", "ALERTA", "Intento de acceso a cuenta bloqueada", request);
            throw new LockedException("La cuenta municipal ha sido bloqueada temporalmente por seguridad. Contacte al administrador.");
        } catch (DisabledException ex) {
            securityAuditService.recordEvent(username, "LOGIN_FAILED", "FALLO", "Intento de acceso a cuenta deshabilitada", request);
            throw new DisabledException("La cuenta de usuario se encuentra deshabilitada o inactiva.");
        }
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest refreshRequest, HttpServletRequest request, HttpServletResponse response) {
        String rawToken = resolveRefreshToken(refreshRequest, request);
        if (rawToken == null || rawToken.trim().isEmpty()) {
            throw new BusinessException("ERR_AUTH_REFRESH", "Token de actualización no proporcionado.");
        }

        // Rotar Refresh Token
        RefreshTokenService.RefreshTokenResult rotateResult = refreshTokenService.rotateRefreshToken(rawToken, request);
        Usuario usuario = rotateResult.entity().getUsuario();

        // Cargar detalles de usuario y generar nuevo Access Token
        CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(usuario.getUsername());
        String newAccessToken = jwtTokenProvider.generateAccessToken(userDetails);

        // Actualizar Cookies HttpOnly
        attachAuthCookies(response, newAccessToken, rotateResult.rawToken());

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(rotateResult.rawToken())
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenExpirationMs() / 1000)
                .username(userDetails.getUsername())
                .nombreCompleto(userDetails.getNombreCompleto())
                .roles(userDetails.getRoles())
                .permisos(userDetails.getPermisos())
                .build();
    }

    @Transactional
    public void logout(RefreshTokenRequest logoutRequest, HttpServletRequest request, HttpServletResponse response) {
        String rawToken = resolveRefreshToken(logoutRequest, request);
        if (rawToken != null) {
            refreshTokenService.revokeToken(rawToken, request);
        }

        if (request != null) {
            var session = request.getSession(false);
            if (session != null) {
                try {
                    session.invalidate();
                } catch (IllegalStateException ignored) {
                }
            }
        }

        // Limpiar Cookies Seguras
        clearAuthCookies(response);
        SecurityContextHolder.clearContext();
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new UnauthorizedException("No existe una sesión autenticada activa.");
        }

        String username = authentication.getName();
        CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(username);

        return UserProfileResponse.builder()
                .id(userDetails.getId())
                .username(userDetails.getUsername())
                .nombreCompleto(userDetails.getNombreCompleto())
                .numeroDocumento(userDetails.getNumeroDocumento())
                .correo(userDetails.getCorreo())
                .roles(userDetails.getRoles())
                .permisos(userDetails.getPermisos())
                .build();
    }

    private String resolveRefreshToken(RefreshTokenRequest requestDto, HttpServletRequest httpRequest) {
        if (requestDto != null && requestDto.getRefreshToken() != null && !requestDto.getRefreshToken().trim().isEmpty()) {
            return requestDto.getRefreshToken().trim();
        }

        Cookie[] cookies = httpRequest.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (SecurityConstants.REFRESH_TOKEN_COOKIE_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private void attachAuthCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        // Cookie Access Token (15 minutos)
        Cookie authCookie = new Cookie(SecurityConstants.TOKEN_COOKIE_NAME, accessToken);
        authCookie.setHttpOnly(true);
        authCookie.setSecure(cookieSecure);
        authCookie.setPath("/");
        authCookie.setMaxAge((int) (jwtTokenProvider.getAccessTokenExpirationMs() / 1000));
        authCookie.setAttribute("SameSite", "Lax");
        response.addCookie(authCookie);

        // Cookie Refresh Token (7 días)
        Cookie refreshCookie = new Cookie(SecurityConstants.REFRESH_TOKEN_COOKIE_NAME, refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(cookieSecure);
        refreshCookie.setPath("/api/auth");
        refreshCookie.setMaxAge(7 * 24 * 60 * 60);
        refreshCookie.setAttribute("SameSite", "Lax");
        response.addCookie(refreshCookie);
    }

    private void clearAuthCookies(HttpServletResponse response) {
        Cookie authCookie = new Cookie(SecurityConstants.TOKEN_COOKIE_NAME, "");
        authCookie.setHttpOnly(true);
        authCookie.setSecure(cookieSecure);
        authCookie.setPath("/");
        authCookie.setMaxAge(0);
        authCookie.setAttribute("SameSite", "Lax");
        response.addCookie(authCookie);

        Cookie refreshCookie = new Cookie(SecurityConstants.REFRESH_TOKEN_COOKIE_NAME, "");
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(cookieSecure);
        refreshCookie.setPath("/api/auth");
        refreshCookie.setMaxAge(0);
        refreshCookie.setAttribute("SameSite", "Lax");
        response.addCookie(refreshCookie);

        Cookie jsessionCookie = new Cookie("JSESSIONID", "");
        jsessionCookie.setHttpOnly(true);
        jsessionCookie.setSecure(cookieSecure);
        jsessionCookie.setPath("/");
        jsessionCookie.setMaxAge(0);
        response.addCookie(jsessionCookie);
    }
}
