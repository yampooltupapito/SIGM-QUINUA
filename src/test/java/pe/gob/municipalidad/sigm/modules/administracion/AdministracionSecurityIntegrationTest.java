package pe.gob.municipalidad.sigm.modules.administracion;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.constants.SecurityConstants;
import pe.gob.municipalidad.sigm.common.model.Persona;
import pe.gob.municipalidad.sigm.common.model.Rol;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.PersonaRepository;
import pe.gob.municipalidad.sigm.common.repository.RolRepository;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.administracion.dto.CargoDtos.CargoCreateRequest;
import pe.gob.municipalidad.sigm.modules.administracion.dto.UsuarioDtos.UsuarioResponse;
import pe.gob.municipalidad.sigm.modules.administracion.service.SuperAdminProvisioningService;
import pe.gob.municipalidad.sigm.security.jwt.JwtTokenProvider;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
@DisplayName("Pruebas de Integración de Seguridad y Autorización Administrativa - SIGM-QUINUA")
class AdministracionSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private SuperAdminProvisioningService superAdminProvisioningService;

    private String adminToken;
    private String ciudadanoToken;

    @BeforeEach
    void setUpTokens() {
        // Asegurar que existan los roles base
        Rol rolAdmin = rolRepository.findByCodigo("ROLE_ADMIN").orElseGet(() ->
                rolRepository.save(Rol.builder().codigo("ROLE_ADMIN").nombre("Administrador").estado("ACTIVO").build())
        );

        Rol rolCiudadano = rolRepository.findByCodigo("ROLE_CIUDADANO").orElseGet(() ->
                rolRepository.save(Rol.builder().codigo("ROLE_CIUDADANO").nombre("Ciudadano").estado("ACTIVO").build())
        );

        // Generar JWT para Administrador con permiso ADMINISTRACION_MANAGE
        pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails adminDetails =
                pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails.builder()
                        .id(1L)
                        .username("admin.test")
                        .password("password")
                        .enabled(true)
                        .accountNonLocked(true)
                        .authorities(java.util.List.of(
                                new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN"),
                                new org.springframework.security.core.authority.SimpleGrantedAuthority("ADMINISTRACION_MANAGE")
                        ))
                        .roles(Set.of("ROLE_ADMIN"))
                        .permisos(Set.of("ADMINISTRACION_MANAGE"))
                        .build();
        adminToken = jwtTokenProvider.generateAccessToken(adminDetails);

        // Generar JWT para Ciudadano común sin permisos administrativos
        pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails ciudadanoDetails =
                pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails.builder()
                        .id(2L)
                        .username("ciudadano.test")
                        .password("password")
                        .enabled(true)
                        .accountNonLocked(true)
                        .authorities(java.util.List.of(
                                new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_CIUDADANO")
                        ))
                        .roles(Set.of("ROLE_CIUDADANO"))
                        .permisos(Set.of())
                        .build();
        ciudadanoToken = jwtTokenProvider.generateAccessToken(ciudadanoDetails);
    }

    // 18. Proteger endpoints administrativos contra usuarios anónimos
    @Test
    @DisplayName("18. Endpoint administrativo REST rechaza acceso anónimo con 401 y Web con redirección")
    void testEndpointsAdministrativosRechazanAnonimo() throws Exception {
        // Endpoint REST sin token -> 401 Unauthorized
        mockMvc.perform(get("/api/admin/usuarios"))
                .andExpect(status().isUnauthorized());

        // Vista Web sin token -> 302 Redirección al login
        mockMvc.perform(get("/admin/usuarios"))
                .andExpect(status().is3xxRedirection());
    }

    // 19. Proteger endpoints contra usuarios sin rol administrativo
    @Test
    @DisplayName("19. Endpoint administrativo rechaza a usuario autenticado con rol CIUDADANO (403 Forbidden)")
    void testEndpointsRechazanUsuarioNoAutorizado() throws Exception {
        mockMvc.perform(get("/api/admin/usuarios")
                        .header(SecurityConstants.AUTH_HEADER_NAME, SecurityConstants.BEARER_PREFIX + ciudadanoToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/admin/roles")
                        .header(SecurityConstants.AUTH_HEADER_NAME, SecurityConstants.BEARER_PREFIX + ciudadanoToken))
                .andExpect(status().isForbidden());
    }

    // 20. Permitir acceso con ROLE_ADMIN a endpoints REST
    @Test
    @DisplayName("20. Endpoint REST permite acceso a usuario con ROLE_ADMIN")
    void testEndpointsPermitenAccesoConRoleAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/usuarios")
                        .header(SecurityConstants.AUTH_HEADER_NAME, SecurityConstants.BEARER_PREFIX + adminToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin/roles")
                        .header(SecurityConstants.AUTH_HEADER_NAME, SecurityConstants.BEARER_PREFIX + adminToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin/permisos")
                        .header(SecurityConstants.AUTH_HEADER_NAME, SecurityConstants.BEARER_PREFIX + adminToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin/oficinas")
                        .header(SecurityConstants.AUTH_HEADER_NAME, SecurityConstants.BEARER_PREFIX + adminToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin/cargos")
                        .header(SecurityConstants.AUTH_HEADER_NAME, SecurityConstants.BEARER_PREFIX + adminToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin/actores")
                        .header(SecurityConstants.AUTH_HEADER_NAME, SecurityConstants.BEARER_PREFIX + adminToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin/auditoria")
                        .header(SecurityConstants.AUTH_HEADER_NAME, SecurityConstants.BEARER_PREFIX + adminToken))
                .andExpect(status().isOk());
    }

    // 21. Validar protección CSRF en peticiones POST administrativas
    @Test
    @DisplayName("21. Validar protección CSRF: POST sin CSRF es rechazado con 403 Forbidden y con CSRF procesa")
    void testValidacionCsrfEnOperacionesModificatorias() throws Exception {
        CargoCreateRequest req = CargoCreateRequest.builder()
                .codigo("CARGO_TEST_CSRF")
                .nombre("Cargo de Prueba CSRF")
                .nivelJerarquico(1)
                .build();

        // Sin CSRF token -> Debe rechazar con 403 Forbidden
        mockMvc.perform(post("/api/admin/cargos")
                        .header(SecurityConstants.AUTH_HEADER_NAME, SecurityConstants.BEARER_PREFIX + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());

        // Con CSRF token válido y JWT -> Debe crear con 201 Created
        mockMvc.perform(post("/api/admin/cargos")
                        .with(csrf())
                        .header(SecurityConstants.AUTH_HEADER_NAME, SecurityConstants.BEARER_PREFIX + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }

    // 22. Renderizado de vistas Thymeleaf de administración con Cookie JWT
    @Test
    @DisplayName("22. Renderizado exitoso de vistas web Thymeleaf de administración")
    void testVistasWebThymeleafRenderizanCorrectamente() throws Exception {
        Cookie authCookie = new Cookie(SecurityConstants.TOKEN_COOKIE_NAME, adminToken);

        mockMvc.perform(get("/admin/usuarios").cookie(authCookie))
                .andExpect(status().isOk());

        mockMvc.perform(get("/admin/roles").cookie(authCookie))
                .andExpect(status().isOk());

        mockMvc.perform(get("/admin/permisos").cookie(authCookie))
                .andExpect(status().isOk());

        mockMvc.perform(get("/admin/oficinas").cookie(authCookie))
                .andExpect(status().isOk());

        mockMvc.perform(get("/admin/cargos").cookie(authCookie))
                .andExpect(status().isOk());

        mockMvc.perform(get("/admin/actores").cookie(authCookie))
                .andExpect(status().isOk());

        mockMvc.perform(get("/admin/auditoria").cookie(authCookie))
                .andExpect(status().isOk());
    }

    // 23. Aprovisionamiento seguro del primer Superadministrador
    @Test
    @DisplayName("23. Aprovisionamiento seguro del primer Superadministrador fuera de endpoints públicos")
    void testAprovisionamientoSuperAdmin() {
        // Asegurar que exista ROLE_SUPERADMIN en la BD
        rolRepository.findByCodigo("ROLE_SUPERADMIN").orElseGet(() ->
                rolRepository.save(Rol.builder().codigo("ROLE_SUPERADMIN").nombre("Superadministrador").estado("ACTIVO").build())
        );

        // Remover superadmin previo si existiera en la prueba
        usuarioRepository.findAll().stream()
                .filter(u -> u.getRoles() != null && u.getRoles().stream().anyMatch(r -> "ROLE_SUPERADMIN".equals(r.getCodigo())))
                .forEach(usuarioRepository::delete);

        UsuarioResponse superAdmin = superAdminProvisioningService.provisionarPrimerSuperAdmin(
                "sigm-quinua-init-sec-2026-ayacucho",
                "super_alcalde",
                "ClaveSegura2026#",
                "DNI",
                "77778888",
                "Guillermo",
                "Aguilar",
                "Huaman",
                "alcaldia@quinua.gob.pe"
        );

        assertThat(superAdmin).isNotNull();
        assertThat(superAdmin.getUsername()).isEqualTo("super_alcalde");
        assertThat(superAdmin.getRoles()).contains("ROLE_SUPERADMIN");
    }
}
