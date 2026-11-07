package pe.gob.municipalidad.sigm.modules.serenazgo;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
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
import pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails;
import pe.gob.municipalidad.sigm.security.jwt.JwtTokenProvider;

import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class SerenazgoCoreIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;
    private String serenoToken;
    private String ciudadanoToken;

    @BeforeEach
    void setUp() {
        long ts = System.currentTimeMillis();

        Rol rolAdmin = rolRepository.findByCodigo("ROLE_ADMIN").orElseGet(() ->
                rolRepository.save(Rol.builder().codigo("ROLE_ADMIN").nombre("Administrador").estado("ACTIVO").build()));

        Rol rolSereno = rolRepository.findByCodigo("ROLE_SERENO").orElseGet(() ->
                rolRepository.save(Rol.builder().codigo("ROLE_SERENO").nombre("Sereno").estado("ACTIVO").build()));

        Rol rolCiudadano = rolRepository.findByCodigo("ROLE_CIUDADANO").orElseGet(() ->
                rolRepository.save(Rol.builder().codigo("ROLE_CIUDADANO").nombre("Ciudadano").estado("ACTIVO").build()));

        // 1. Admin
        Persona pAdmin = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI").numeroDocumento("74" + String.format("%06d", (int)(ts % 1000000)))
                .nombres("Admin").apellidoPaterno("Serenazgo").estado("ACTIVO").build());
        Usuario uAdmin = usuarioRepository.save(Usuario.builder()
                .username("admin_sz_" + ts).persona(pAdmin).passwordHash(passwordEncoder.encode("Pass123!"))
                .enabled(true).accountLocked(false).roles(Set.of(rolAdmin)).build());

        CustomUserDetails adminDetails = CustomUserDetails.builder()
                .id(uAdmin.getId()).username(uAdmin.getUsername()).password("dummy").enabled(true).accountNonLocked(true)
                .roles(Set.of("ROLE_ADMIN"))
                .permisos(Set.of(
                        "ROLE_ADMIN",
                        "SERENAZGO_MODULO_ACCEDER",
                        "SERENAZGO_CATALOGOS_VER",
                        "SERENAZGO_PERSONAL_VER",
                        "SERENAZGO_UNIDADES_VER",
                        "SERENAZGO_SECTORES_VER",
                        "SERENAZGO_TURNOS_VER",
                        "SERENAZGO_INCIDENCIAS_VER",
                        "SERENAZGO_ALERTAS_VER",
                        "SERENAZGO_INTERVENCIONES_VER",
                        "SERENAZGO_PATRULLAJES_VER",
                        "SERENAZGO_EVIDENCIAS_VER",
                        "SERENAZGO_REPORTES_VER"
                ))
                .authorities(List.of(
                        new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("SERENAZGO_MODULO_ACCEDER"),
                        new SimpleGrantedAuthority("SERENAZGO_CATALOGOS_VER"),
                        new SimpleGrantedAuthority("SERENAZGO_SECTORES_VER"),
                        new SimpleGrantedAuthority("SERENAZGO_REPORTES_VER")
                ))
                .build();
        adminToken = jwtTokenProvider.generateAccessToken(adminDetails);

        // 2. Sereno
        Persona pSereno = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI").numeroDocumento("75" + String.format("%06d", (int)(ts % 1000000)))
                .nombres("Sereno").apellidoPaterno("Guardia").estado("ACTIVO").build());
        Usuario uSereno = usuarioRepository.save(Usuario.builder()
                .username("sereno_sz_" + ts).persona(pSereno).passwordHash(passwordEncoder.encode("Pass123!"))
                .enabled(true).accountLocked(false).roles(Set.of(rolSereno)).build());

        CustomUserDetails serenoDetails = CustomUserDetails.builder()
                .id(uSereno.getId()).username(uSereno.getUsername()).password("dummy").enabled(true).accountNonLocked(true)
                .roles(Set.of("ROLE_SERENO"))
                .permisos(Set.of(
                        "SERENAZGO_MODULO_ACCEDER",
                        "SERENAZGO_CATALOGOS_VER",
                        "SERENAZGO_SECTORES_VER"
                ))
                .authorities(List.of(
                        new SimpleGrantedAuthority("ROLE_SERENO"),
                        new SimpleGrantedAuthority("SERENAZGO_MODULO_ACCEDER"),
                        new SimpleGrantedAuthority("SERENAZGO_CATALOGOS_VER"),
                        new SimpleGrantedAuthority("SERENAZGO_SECTORES_VER")
                ))
                .build();
        serenoToken = jwtTokenProvider.generateAccessToken(serenoDetails);

        // 3. Ciudadano
        Persona pCiudadano = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI").numeroDocumento("76" + String.format("%06d", (int)(ts % 1000000)))
                .nombres("Ciudadano").apellidoPaterno("Vecino").estado("ACTIVO").build());
        Usuario uCiudadano = usuarioRepository.save(Usuario.builder()
                .username("ciudadano_sz_" + ts).persona(pCiudadano).passwordHash(passwordEncoder.encode("Pass123!"))
                .enabled(true).accountLocked(false).roles(Set.of(rolCiudadano)).build());

        CustomUserDetails ciudadanoDetails = CustomUserDetails.builder()
                .id(uCiudadano.getId()).username(uCiudadano.getUsername()).password("dummy").enabled(true).accountNonLocked(true)
                .roles(Set.of("ROLE_CIUDADANO"))
                .permisos(Set.of())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_CIUDADANO")))
                .build();
        ciudadanoToken = jwtTokenProvider.generateAccessToken(ciudadanoDetails);
    }

    @Test
    @DisplayName("01: Rechazo de Acceso Anónimo a la API de Serenazgo (HTTP 401)")
    void test01_acceso_anonimo_rechazado() throws Exception {
        mockMvc.perform(get("/api/serenazgo/catalogos")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("02: Consulta Exitosa de Catálogos de Seguridad con Privilegios (HTTP 200)")
    void test02_acceso_admin_catalogos() throws Exception {
        mockMvc.perform(get("/api/serenazgo/catalogos")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tiposIncidencia").isArray())
                .andExpect(jsonPath("$.tiposIncidencia", hasSize(greaterThanOrEqualTo(8))))
                .andExpect(jsonPath("$.cargosOperativos").isArray())
                .andExpect(jsonPath("$.cargosOperativos", hasSize(greaterThanOrEqualTo(5))))
                .andExpect(jsonPath("$.turnos").isArray())
                .andExpect(jsonPath("$.turnos", hasSize(greaterThanOrEqualTo(3))));
    }

    @Test
    @DisplayName("03: Consulta de Sectores de Patrullaje como Sereno Autorizado (HTTP 200)")
    void test03_listar_sectores_api() throws Exception {
        mockMvc.perform(get("/api/serenazgo/sectores")
                        .header("Authorization", "Bearer " + serenoToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("04: Consulta de Resumen Operativo para Dashboard (HTTP 200)")
    void test04_resumen_dashboard_api() throws Exception {
        mockMvc.perform(get("/api/serenazgo/resumen")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPersonalActivo").isNumber())
                .andExpect(jsonPath("$.totalUnidadesOperativas").isNumber())
                .andExpect(jsonPath("$.incidenciasAbiertas").isNumber());
    }

    @Test
    @DisplayName("05: Renderizado Exitoso de Vista Thymeleaf Dashboard (HTTP 200)")
    void test05_vista_dashboard_thymeleaf() throws Exception {
        Cookie authCookie = new Cookie(SecurityConstants.ACCESS_TOKEN_COOKIE_NAME, adminToken);

        mockMvc.perform(get("/dashboard/serenazgo").cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/serenazgo/dashboard"))
                .andExpect(model().attributeExists("resumen"))
                .andExpect(model().attributeExists("tiposIncidencia"))
                .andExpect(model().attributeExists("cargos"))
                .andExpect(model().attributeExists("turnos"));
    }

    @Test
    @DisplayName("06: Denegación de Acceso a Usuario Ciudadano sin Autoridad de Serenazgo (HTTP 403)")
    void test06_usuario_sin_permiso_rechazado() throws Exception {
        mockMvc.perform(get("/api/serenazgo/catalogos")
                        .header("Authorization", "Bearer " + ciudadanoToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
