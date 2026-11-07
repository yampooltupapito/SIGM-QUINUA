package pe.gob.municipalidad.sigm.security;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.constants.SecurityConstants;
import pe.gob.municipalidad.sigm.common.model.Persona;
import pe.gob.municipalidad.sigm.common.model.Rol;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.PersonaRepository;
import pe.gob.municipalidad.sigm.common.repository.RolRepository;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.security.jwt.JwtTokenProvider;
import pe.gob.municipalidad.sigm.security.model.PasswordResetToken;
import pe.gob.municipalidad.sigm.security.repository.PasswordResetTokenRepository;
import pe.gob.municipalidad.sigm.security.service.PasswordRecoveryService;

import java.time.OffsetDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
@DisplayName("Pruebas de Integración de Login Institucional, Recuperación y Web Auth - SIGM-QUINUA")
class AuthWebIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PasswordRecoveryService passwordRecoveryService;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private static final String TEST_PASSWORD = "PasswordSeguro123!";
    private Usuario adminUser;
    private Usuario operadorUser;
    private Usuario disabledUser;
    private Usuario lockedUser;

    @BeforeEach
    void setUp() {
        Rol rolAdmin = rolRepository.findByCodigo("ROLE_ADMIN").orElseGet(() ->
                rolRepository.save(Rol.builder().codigo("ROLE_ADMIN").nombre("Administrador").estado("ACTIVO").build())
        );

        Rol rolOperador = rolRepository.findByCodigo("ROLE_OPERADOR").orElseGet(() ->
                rolRepository.save(Rol.builder().codigo("ROLE_OPERADOR").nombre("Operador").estado("ACTIVO").build())
        );

        // 1. Admin
        Persona perAdmin = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI").numeroDocumento("11112222")
                .nombres("Alcalde").apellidoPaterno("Quinua")
                .correo("admin.web@quinua.gob.pe").build());

        adminUser = usuarioRepository.save(Usuario.builder()
                .username("web.admin")
                .persona(perAdmin)
                .passwordHash(passwordEncoder.encode(TEST_PASSWORD))
                .enabled(true)
                .accountLocked(false)
                .roles(Set.of(rolAdmin))
                .build());

        // 2. Operador
        Persona perOp = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI").numeroDocumento("33334444")
                .nombres("Operador").apellidoPaterno("Mesa")
                .correo("operador.web@quinua.gob.pe").build());

        operadorUser = usuarioRepository.save(Usuario.builder()
                .username("web.operador")
                .persona(perOp)
                .passwordHash(passwordEncoder.encode(TEST_PASSWORD))
                .enabled(true)
                .accountLocked(false)
                .roles(Set.of(rolOperador))
                .build());

        // 3. Deshabilitado
        Persona perDis = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI").numeroDocumento("55556666")
                .nombres("Inactivo").apellidoPaterno("Cesar")
                .correo("inactivo@quinua.gob.pe").build());

        disabledUser = usuarioRepository.save(Usuario.builder()
                .username("web.inactivo")
                .persona(perDis)
                .passwordHash(passwordEncoder.encode(TEST_PASSWORD))
                .enabled(false)
                .accountLocked(false)
                .build());

        // 4. Bloqueado
        Persona perLock = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI").numeroDocumento("77778888")
                .nombres("Bloqueado").apellidoPaterno("Alerta")
                .correo("bloqueado@quinua.gob.pe").build());

        lockedUser = usuarioRepository.save(Usuario.builder()
                .username("web.bloqueado")
                .persona(perLock)
                .passwordHash(passwordEncoder.encode(TEST_PASSWORD))
                .enabled(true)
                .accountLocked(true)
                .build());
    }

    @AfterEach
    void cleanUp() {
        passwordResetTokenRepository.deleteAll();
    }

    // 1. Renderizado del login
    @Test
    @DisplayName("1. Renderizado del login: HTTP 200 con campos, CSRF y atributos accesibles")
    void testRenderizadoLogin() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Municipalidad Distrital de Quinua")))
                .andExpect(content().string(containsString("SIGM-QUINUA")))
                .andExpect(content().string(containsString("username")))
                .andExpect(content().string(containsString("password")))
                .andExpect(content().string(containsString("autocomplete=\"username\"")))
                .andExpect(content().string(containsString("autocomplete=\"current-password\"")));
    }

    // 2. Campos obligatorios
    @Test
    @DisplayName("2. Campos obligatorios: POST /login sin contraseña o usuario es rechazado")
    void testLoginCamposObligatorios() throws Exception {
        mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("username", "")
                        .param("password", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=true"));
    }

    // 3 y 8. Credenciales correctas y redirección autorizada para Administrador
    @Test
    @DisplayName("3 y 8. Credenciales correctas: Inicia sesión, emite cookies seguras y redirige a /admin/dashboard")
    void testLoginCorrectoAdminRedireccionaAPanel() throws Exception {
        MvcResult result = mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("username", "web.admin")
                        .param("password", TEST_PASSWORD))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/dashboard"))
                .andExpect(cookie().exists(SecurityConstants.TOKEN_COOKIE_NAME))
                .andExpect(cookie().httpOnly(SecurityConstants.TOKEN_COOKIE_NAME, true))
                .andExpect(cookie().exists(SecurityConstants.REFRESH_TOKEN_COOKIE_NAME))
                .andReturn();

        Cookie authCookie = result.getResponse().getCookie(SecurityConstants.TOKEN_COOKIE_NAME);
        assertThat(authCookie).isNotNull();
        assertThat(authCookie.getAttribute("SameSite")).isEqualTo("Lax");
        assertThat(authCookie.getPath()).isEqualTo("/");
    }

    // 8. Redirección para usuario operador municipal hacia /bienvenida
    @Test
    @DisplayName("8. Redirección autorizada: Servidor con rol de operador redirige a /bienvenida")
    void testLoginCorrectoOperadorRedireccionaABienvenida() throws Exception {
        mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("username", "web.operador")
                        .param("password", TEST_PASSWORD))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/bienvenida"));
    }

    // 4. Credenciales incorrectas
    @Test
    @DisplayName("4. Credenciales incorrectas: Rechaza y redirige a /login?error=true")
    void testLoginCredencialesIncorrectas() throws Exception {
        mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("username", "web.admin")
                        .param("password", "PasswordIncorrecto999#"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=true"));
    }

    // 5. Usuario inexistente
    @Test
    @DisplayName("5. Usuario inexistente: Mismo comportamiento sin revelar inexistencia")
    void testLoginUsuarioInexistente() throws Exception {
        mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("username", "usuario.no.existe")
                        .param("password", TEST_PASSWORD))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=true"));
    }

    // 6. Usuario deshabilitado
    @Test
    @DisplayName("6. Usuario deshabilitado: Rechazo y redirección a login")
    void testLoginUsuarioDeshabilitado() throws Exception {
        mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("username", "web.inactivo")
                        .param("password", TEST_PASSWORD))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=true"));
    }

    // 7. Usuario bloqueado
    @Test
    @DisplayName("7. Usuario bloqueado: Rechazo con alerta y redirección")
    void testLoginUsuarioBloqueado() throws Exception {
        mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("username", "web.bloqueado")
                        .param("password", TEST_PASSWORD))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=true"));
    }

    // 9 y 18. Protección contra Open Redirect
    @Test
    @DisplayName("9 y 18. Protección contra Open Redirect: URLs externas o relativas sospechosas son neutralizadas")
    void testProteccionOpenRedirect() throws Exception {
        // Intento con URL externa maliciosa
        mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("username", "web.admin")
                        .param("password", TEST_PASSWORD)
                        .param("redirect", "https://sitio-malicioso.com/phishing"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/dashboard")); // Ignoró la externa y redirigió a la segura

        // Intento con protocol-relative URL (//malicious.com)
        mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("username", "web.admin")
                        .param("password", TEST_PASSWORD)
                        .param("redirect", "//malicious.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/dashboard"));
    }

    // 10. Protección CSRF
    @Test
    @DisplayName("10. Protección CSRF: POST /login sin token CSRF es rechazado con 403 Forbidden")
    void testProteccionCsrfEnLogin() throws Exception {
        mockMvc.perform(post("/login")
                        .param("username", "web.admin")
                        .param("password", TEST_PASSWORD))
                .andExpect(status().isForbidden());
    }

    // 12. Logout
    @Test
    @DisplayName("12. Logout: Invalida sesión, vacía cookies y redirige a /login?logout=true")
    void testLogoutLimpiaCookiesYRedirecciona() throws Exception {
        mockMvc.perform(get("/logout"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?logout=true"))
                .andExpect(cookie().maxAge(SecurityConstants.TOKEN_COOKIE_NAME, 0))
                .andExpect(cookie().maxAge(SecurityConstants.REFRESH_TOKEN_COOKIE_NAME, 0));
    }

    // 13. Pantalla de recuperación de contraseña
    @Test
    @DisplayName("13. Pantalla de recuperación: GET /forgot-password y POST /forgot-password")
    void testFlujoRecuperacionContrasena() throws Exception {
        mockMvc.perform(get("/forgot-password"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Recuperación de Contraseña")));

        mockMvc.perform(post("/forgot-password")
                        .with(csrf())
                        .param("identificador", "web.admin"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/forgot-password?enviado=true"));
    }

    // 14 y 15. Vista de restablecimiento de contraseña
    @Test
    @DisplayName("14 y 15. Vista reset-password: Token inválido muestra error y token válido muestra formulario")
    void testVistaResetPassword() throws Exception {
        // Token inválido o inexistente
        mockMvc.perform(get("/reset-password").param("token", "token-inexistente-123"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Enlace No Válido")));

        // Generar un token válido en base de datos
        String rawToken = "token-valido-prueba-2026";
        String tokenHash = passwordRecoveryService.hashToken(rawToken);
        PasswordResetToken tokenEntity = PasswordResetToken.builder()
                .usuario(adminUser)
                .tokenHash(tokenHash)
                .used(false)
                .expiresAt(OffsetDateTime.now().plusMinutes(30))
                .build();
        passwordResetTokenRepository.save(tokenEntity);

        // Token válido -> Formulario
        mockMvc.perform(get("/reset-password").param("token", rawToken))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Nueva Contraseña")))
                .andExpect(content().string(containsString("web.admin")));
    }

    // 16. Restablecer contraseña exitoso vía POST
    @Test
    @DisplayName("16. Restablecer contraseña exitoso: POST /reset-password redirige a /login?reset=success")
    void testProcesarResetPasswordExitoso() throws Exception {
        String rawToken = "token-valido-para-submit";
        String tokenHash = passwordRecoveryService.hashToken(rawToken);
        PasswordResetToken tokenEntity = PasswordResetToken.builder()
                .usuario(operadorUser)
                .tokenHash(tokenHash)
                .used(false)
                .expiresAt(OffsetDateTime.now().plusMinutes(30))
                .build();
        passwordResetTokenRepository.save(tokenEntity);

        mockMvc.perform(post("/reset-password")
                        .with(csrf())
                        .param("token", rawToken)
                        .param("nuevaContrasena", "NuevaClaveSegura2026#")
                        .param("confirmarContrasena", "NuevaClaveSegura2026#"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?reset=success"));

        // Comprobar que puede iniciar sesión con la nueva contraseña
        mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("username", "web.operador")
                        .param("password", "NuevaClaveSegura2026#"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/bienvenida"));
    }

    // 19 y 20. Vista de Bienvenida autenticada y rechazo si es anónima
    @Test
    @DisplayName("19 y 20. Vista de Bienvenida: Usuario anónimo es redirigido a /login y usuario autenticado ingresa")
    void testVistaBienvenida() throws Exception {
        // Anónimo -> redirige a /login
        mockMvc.perform(get("/bienvenida"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        // Con Cookie JWT válida
        pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails details =
                pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails.builder()
                        .id(operadorUser.getId())
                        .username(operadorUser.getUsername())
                        .password("dummy")
                        .enabled(true)
                        .accountNonLocked(true)
                        .roles(Set.of("ROLE_OPERADOR"))
                        .permisos(Set.of())
                        .authorities(java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_OPERADOR")))
                        .build();

        String jwt = jwtTokenProvider.generateAccessToken(details);
        Cookie cookie = new Cookie(SecurityConstants.TOKEN_COOKIE_NAME, jwt);

        mockMvc.perform(get("/bienvenida").cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("web.operador")))
                .andExpect(content().string(containsString("ROLE_OPERADOR")));
    }
}
