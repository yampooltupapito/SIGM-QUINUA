package pe.gob.municipalidad.sigm.security;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import pe.gob.municipalidad.sigm.common.constants.SecurityConstants;
import pe.gob.municipalidad.sigm.common.model.*;
import pe.gob.municipalidad.sigm.common.repository.*;
import pe.gob.municipalidad.sigm.security.dto.LoginRequest;
import pe.gob.municipalidad.sigm.security.dto.RefreshTokenRequest;
import pe.gob.municipalidad.sigm.security.jwt.JwtTokenProvider;
import pe.gob.municipalidad.sigm.security.repository.AuditoriaSeguridadRepository;
import pe.gob.municipalidad.sigm.security.repository.RefreshTokenRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
@DisplayName("Pruebas de Integración de Autenticación, JWT, RBAC y Auditoría - SIGM-QUINUA")
class AuthSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private PermisoRepository permisoRepository;

    @Autowired
    private ModuloSistemaRepository moduloSistemaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private AuditoriaSeguridadRepository auditoriaSeguridadRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private static final String TEST_PASSWORD = "PasswordSeguro123!";
    private Usuario testAdminUser;
    private Usuario testOperadorUser;
    private Usuario testDisabledUser;
    private Usuario testLockedUser;

    @BeforeEach
    void setUp() {
        cleanTestData();

        // 1. Crear roles y permisos
        Rol rolAdmin = rolRepository.findByCodigo("ROLE_ADMIN").orElseGet(() ->
                rolRepository.save(Rol.builder().codigo("ROLE_ADMIN").nombre("Administrador").estado("ACTIVO").build())
        );

        Rol rolOperador = rolRepository.findByCodigo("ROLE_OPERADOR").orElseGet(() ->
                rolRepository.save(Rol.builder().codigo("ROLE_OPERADOR").nombre("Operador").estado("ACTIVO").build())
        );

        ModuloSistema moduloTramite = moduloSistemaRepository.findByCodigo("TRAMITE_DOCUMENTARIO")
                .orElseGet(() -> moduloSistemaRepository.save(ModuloSistema.builder()
                        .codigo("TRAMITE_DOCUMENTARIO").nombre("Trámite Documentario").orden(1).build()));

        Permiso permisoView = permisoRepository.findByCodigo("MESAPARTES_VIEW").orElseGet(() ->
                permisoRepository.save(Permiso.builder()
                        .modulo(moduloTramite)
                        .codigo("MESAPARTES_VIEW")
                        .nombre("Ver Mesa de Partes")
                        .accion("VIEW")
                        .estado("ACTIVO")
                        .build())
        );

        Permiso permisoAdmin = permisoRepository.findByCodigo("ADMINISTRACION_MANAGE").orElseGet(() ->
                permisoRepository.save(Permiso.builder()
                        .modulo(moduloTramite)
                        .codigo("ADMINISTRACION_MANAGE")
                        .nombre("Gestión de Administración")
                        .accion("MANAGE")
                        .estado("ACTIVO")
                        .build())
        );

        if (rolAdmin.getPermisos() == null) {
            rolAdmin.setPermisos(new HashSet<>());
        }
        rolAdmin.getPermisos().add(permisoAdmin);
        rolRepository.save(rolAdmin);

        if (rolOperador.getPermisos() == null) {
            rolOperador.setPermisos(new HashSet<>());
        }
        rolOperador.getPermisos().add(permisoView);
        rolRepository.save(rolOperador);

        // 2. Crear personas para los usuarios
        Persona personaAdmin = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento("70000001")
                .nombres("Admin")
                .apellidoPaterno("Quinua")
                .correo("admin.test@quinua.gob.pe")
                .build());

        Persona personaOperador = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento("70000002")
                .nombres("Carlos")
                .apellidoPaterno("Operador")
                .correo("operador.test@quinua.gob.pe")
                .build());

        Persona personaDisabled = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento("70000003")
                .nombres("Inactivo")
                .apellidoPaterno("Usuario")
                .correo("inactivo.test@quinua.gob.pe")
                .build());

        Persona personaLocked = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento("70000004")
                .nombres("Bloqueado")
                .apellidoPaterno("Usuario")
                .correo("bloqueado.test@quinua.gob.pe")
                .build());

        // 3. Crear usuarios
        testAdminUser = usuarioRepository.save(Usuario.builder()
                .persona(personaAdmin)
                .username("test.admin")
                .passwordHash(passwordEncoder.encode(TEST_PASSWORD))
                .enabled(true)
                .accountLocked(false)
                .failedAttempts(0)
                .roles(new HashSet<>(Set.of(rolAdmin)))
                .build());

        testOperadorUser = usuarioRepository.save(Usuario.builder()
                .persona(personaOperador)
                .username("test.operador")
                .passwordHash(passwordEncoder.encode(TEST_PASSWORD))
                .enabled(true)
                .accountLocked(false)
                .failedAttempts(0)
                .roles(new HashSet<>(Set.of(rolOperador)))
                .build());

        testDisabledUser = usuarioRepository.save(Usuario.builder()
                .persona(personaDisabled)
                .username("test.disabled")
                .passwordHash(passwordEncoder.encode(TEST_PASSWORD))
                .enabled(false)
                .accountLocked(false)
                .failedAttempts(0)
                .roles(new HashSet<>(Set.of(rolOperador)))
                .build());

        testLockedUser = usuarioRepository.save(Usuario.builder()
                .persona(personaLocked)
                .username("test.locked")
                .passwordHash(passwordEncoder.encode(TEST_PASSWORD))
                .enabled(true)
                .accountLocked(true)
                .failedAttempts(5)
                .roles(new HashSet<>(Set.of(rolOperador)))
                .build());
    }

    @AfterEach
    void tearDown() {
        cleanTestData();
    }

    private void cleanTestData() {
        List<String> testUsernames = List.of("test.admin", "test.operador", "test.disabled", "test.locked");
        for (String username : testUsernames) {
            usuarioRepository.findByUsername(username).ifPresent(u -> {
                refreshTokenRepository.deleteAll(refreshTokenRepository.findByFamilyId(u.getId().toString()));
                // eliminar refresh tokens asociados
                var tokens = refreshTokenRepository.findAll();
                for (var t : tokens) {
                    if (t.getUsuario().getId().equals(u.getId())) {
                        refreshTokenRepository.delete(t);
                    }
                }
                usuarioRepository.delete(u);
            });
        }
        List<String> docNums = List.of("70000001", "70000002", "70000003", "70000004");
        for (String doc : docNums) {
            personaRepository.findByTipoDocumentoAndNumeroDocumento("DNI", doc).ifPresent(personaRepository::delete);
        }
    }

    @Test
    @DisplayName("1. Contraseña correcta: Autenticación exitosa, emisión de JWT y cookies seguras")
    void shouldLoginSuccessfullyWithCorrectPassword() throws Exception {
        LoginRequest loginRequest = new LoginRequest("test.admin", TEST_PASSWORD);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isString())
                .andExpect(jsonPath("$.data.refreshToken").isString())
                .andExpect(jsonPath("$.data.username").value("test.admin"))
                .andExpect(jsonPath("$.data.roles", hasItem("ROLE_ADMIN")))
                .andExpect(jsonPath("$.data.permisos", hasItem("ADMINISTRACION_MANAGE")))
                // 20. Ausencia de contraseñas y secretos en respuestas
                .andExpect(jsonPath("$.data.password").doesNotExist())
                .andExpect(jsonPath("$.data.passwordHash").doesNotExist())
                // 19. Cookies seguras
                .andExpect(cookie().exists(SecurityConstants.TOKEN_COOKIE_NAME))
                .andExpect(cookie().httpOnly(SecurityConstants.TOKEN_COOKIE_NAME, true))
                .andExpect(cookie().exists(SecurityConstants.REFRESH_TOKEN_COOKIE_NAME))
                .andReturn();

        Cookie authCookie = result.getResponse().getCookie(SecurityConstants.TOKEN_COOKIE_NAME);
        assertThat(authCookie).isNotNull();
        assertThat(authCookie.getAttribute("SameSite")).isEqualTo("Lax");
        assertThat(authCookie.isHttpOnly()).isTrue();
        assertThat(authCookie.getPath()).isEqualTo("/");
    }

    @Test
    @DisplayName("2. Contraseña incorrecta: Rechazo con código 401 y mensaje genérico")
    void shouldFailLoginWithIncorrectPassword() throws Exception {
        LoginRequest loginRequest = new LoginRequest("test.admin", "PasswordIncorrecto999!");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Credenciales de acceso incorrectas."));
    }

    @Test
    @DisplayName("3. Usuario inexistente: Rechazo con código 401 idéntico evitando enumeración de cuentas")
    void shouldFailLoginWithNonExistentUserWithoutRevealingAbsence() throws Exception {
        LoginRequest loginRequest = new LoginRequest("usuario.no.existe.en.quinua", TEST_PASSWORD);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Credenciales de acceso incorrectas."));
    }

    @Test
    @DisplayName("4. Usuario deshabilitado: Rechazo con código 401 de cuenta inactiva")
    void shouldFailLoginWhenUserIsDisabled() throws Exception {
        LoginRequest loginRequest = new LoginRequest("test.disabled", TEST_PASSWORD);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("deshabilitada o inactiva")));
    }

    @Test
    @DisplayName("5. Usuario bloqueado: Rechazo con código 401 de cuenta bloqueada preventivamente")
    void shouldFailLoginWhenAccountIsLocked() throws Exception {
        LoginRequest loginRequest = new LoginRequest("test.locked", TEST_PASSWORD);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("bloqueada temporalmente")));
    }

    @Test
    @DisplayName("10 y 12. Rotación de refresh token y detección de reutilización maliciosa")
    void shouldRotateRefreshTokenAndDetectReusedToken() throws Exception {
        // Iniciar sesión para obtener tokens
        LoginRequest loginRequest = new LoginRequest("test.operador", TEST_PASSWORD);
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = loginResult.getResponse().getContentAsString();
        String initialRefreshToken = objectMapper.readTree(responseJson).path("data").path("refreshToken").asText();

        // 10. Rotar token correctamente
        RefreshTokenRequest refreshReq = new RefreshTokenRequest(initialRefreshToken);
        MvcResult refreshResult = mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isString())
                .andExpect(jsonPath("$.data.refreshToken").isString())
                .andReturn();

        String newRefreshToken = objectMapper.readTree(refreshResult.getResponse().getContentAsString())
                .path("data").path("refreshToken").asText();
        assertThat(newRefreshToken).isNotEqualTo(initialRefreshToken);

        // 12. Reutilización maliciosa: Intentar usar de nuevo el token inicial que ya fue revocado por la rotación
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshTokenRequest(initialRefreshToken))))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value(containsString("Token de sesión previamente utilizado")));
    }

    @Test
    @DisplayName("13. Logout: Revocación de refresh token y limpieza de cookies")
    void shouldLogoutSuccessfullyAndClearCookies() throws Exception {
        LoginRequest loginRequest = new LoginRequest("test.operador", TEST_PASSWORD);
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String refreshToken = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .path("data").path("refreshToken").asText();

        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshTokenRequest(refreshToken))))
                .andExpect(status().isOk())
                .andExpect(cookie().maxAge(SecurityConstants.TOKEN_COOKIE_NAME, 0))
                .andExpect(cookie().maxAge(SecurityConstants.REFRESH_TOKEN_COOKIE_NAME, 0));
    }

    @Test
    @DisplayName("14. Endpoint protegido sin autenticación: Rechazo con código 401")
    void shouldRejectProtectedEndpointWhenAnonymous() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/admin/recursos"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("15. Endpoint protegido sin permiso: Rechazo con código 403 Forbidden")
    void shouldDenyAccessWhenAuthorityIsInsufficient() throws Exception {
        // Iniciar sesión con test.operador (sin rol de admin)
        LoginRequest loginRequest = new LoginRequest("test.operador", TEST_PASSWORD);
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String accessToken = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .path("data").path("accessToken").asText();

        // Intentar acceder a ruta administrativa
        mockMvc.perform(get("/api/v1/admin/recursos")
                        .header(SecurityConstants.AUTH_HEADER_NAME, SecurityConstants.BEARER_PREFIX + accessToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(containsString("No cuenta con los privilegios")));
    }

    @Test
    @DisplayName("16. Endpoint autorizado con rol/permiso: Acceso concedido con código 200")
    void shouldAllowAccessWhenAuthorityMatches() throws Exception {
        // 1. Acceso a admin con test.admin
        LoginRequest adminLogin = new LoginRequest("test.admin", TEST_PASSWORD);
        MvcResult adminLoginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminLogin)))
                .andExpect(status().isOk())
                .andReturn();

        String adminToken = objectMapper.readTree(adminLoginResult.getResponse().getContentAsString())
                .path("data").path("accessToken").asText();

        mockMvc.perform(get("/api/v1/admin/recursos")
                        .header(SecurityConstants.AUTH_HEADER_NAME, SecurityConstants.BEARER_PREFIX + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ADMIN_OK"));

        // 2. Acceso a mesa de partes con test.operador (que tiene MESAPARTES_VIEW)
        LoginRequest operadorLogin = new LoginRequest("test.operador", TEST_PASSWORD);
        MvcResult opLoginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(operadorLogin)))
                .andExpect(status().isOk())
                .andReturn();

        String operadorToken = objectMapper.readTree(opLoginResult.getResponse().getContentAsString())
                .path("data").path("accessToken").asText();

        mockMvc.perform(get("/api/v1/mesa-partes/expedientes")
                        .header(SecurityConstants.AUTH_HEADER_NAME, SecurityConstants.BEARER_PREFIX + operadorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("EXPEDIENTES_OK"));
    }

    @Test
    @DisplayName("Autenticación transparente mediante Cookie HttpOnly para soporte frontend")
    void shouldAuthenticateViaHttpOnlyCookie() throws Exception {
        LoginRequest loginRequest = new LoginRequest("test.operador", TEST_PASSWORD);
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String accessToken = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .path("data").path("accessToken").asText();

        // Enviar petición usando la cookie en lugar del header Authorization
        mockMvc.perform(get("/api/auth/me")
                        .cookie(new Cookie(SecurityConstants.TOKEN_COOKIE_NAME, accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("test.operador"))
                .andExpect(jsonPath("$.data.nombreCompleto").value("Carlos Operador"))
                .andExpect(jsonPath("$.data.password").doesNotExist())
                .andExpect(jsonPath("$.data.passwordHash").doesNotExist());
    }
}
