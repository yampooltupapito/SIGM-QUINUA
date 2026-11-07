package pe.gob.municipalidad.sigm.modules.serenazgo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.model.Persona;
import pe.gob.municipalidad.sigm.common.repository.PersonaRepository;
import pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails;
import pe.gob.municipalidad.sigm.common.model.Rol;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.RolRepository;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.security.jwt.JwtTokenProvider;

import java.util.List;
import java.util.Set;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class MapaWebSecurityTest {

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
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    private String adminToken;
    private String ciudadanoToken;

    private CustomUserDetails cudAdmin;
    private CustomUserDetails cudSupervisor;
    private CustomUserDetails cudCiudadano;

    @BeforeEach
    void setUp() {
        long ts = System.currentTimeMillis();

        Rol rolAdmin = rolRepository.findByCodigo("ROLE_ADMIN")
                .orElseGet(() -> rolRepository.save(Rol.builder().codigo("ROLE_ADMIN").nombre("Administrador").estado("ACTIVO").build()));
        Rol rolSupervisor = rolRepository.findByCodigo("ROLE_SUPERVISOR_SERENAZGO")
                .orElseGet(() -> rolRepository.save(Rol.builder().codigo("ROLE_SUPERVISOR_SERENAZGO").nombre("Supervisor Serenazgo").estado("ACTIVO").build()));
        Rol rolCiudadano = rolRepository.findByCodigo("ROLE_CIUDADANO")
                .orElseGet(() -> rolRepository.save(Rol.builder().codigo("ROLE_CIUDADANO").nombre("Ciudadano").estado("ACTIVO").build()));

        // 1. Admin
        Persona pAdmin = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI").numeroDocumento("48" + String.format("%06d", (int)(ts % 1000000)))
                .nombres("Admin").apellidoPaterno("GIS").estado("ACTIVO").build());
        Usuario uAdmin = usuarioRepository.save(Usuario.builder()
                .username("admin_map_" + ts).persona(pAdmin).passwordHash(passwordEncoder.encode("Pass123!"))
                .enabled(true).accountLocked(false).roles(Set.of(rolAdmin)).build());

        cudAdmin = CustomUserDetails.builder()
                .id(uAdmin.getId()).username(uAdmin.getUsername()).password("dummy").enabled(true).accountNonLocked(true)
                .authorities(List.of(
                        new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("SERENAZGO_MAPA_VER"),
                        new SimpleGrantedAuthority("SERENAZGO_MAPA_VER_INCIDENCIAS"),
                        new SimpleGrantedAuthority("SERENAZGO_MAPA_VER_ALERTAS"),
                        new SimpleGrantedAuthority("SERENAZGO_MAPA_EXPORTAR")
                ))
                .build();
        adminToken = jwtTokenProvider.generateAccessToken(cudAdmin);

        // 2. Supervisor de Serenazgo
        Persona pSup = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI").numeroDocumento("49" + String.format("%06d", (int)(ts % 1000000)))
                .nombres("Supervisor").apellidoPaterno("Seguridad").estado("ACTIVO").build());
        Usuario uSup = usuarioRepository.save(Usuario.builder()
                .username("sup_map_" + ts).persona(pSup).passwordHash(passwordEncoder.encode("Pass123!"))
                .enabled(true).accountLocked(false).roles(Set.of(rolSupervisor)).build());

        cudSupervisor = CustomUserDetails.builder()
                .id(uSup.getId()).username(uSup.getUsername()).password("dummy").enabled(true).accountNonLocked(true)
                .authorities(List.of(
                        new SimpleGrantedAuthority("ROLE_SUPERVISOR_SERENAZGO"),
                        new SimpleGrantedAuthority("SERENAZGO_MAPA_VER"),
                        new SimpleGrantedAuthority("SERENAZGO_MAPA_VER_INCIDENCIAS"),
                        new SimpleGrantedAuthority("SERENAZGO_MAPA_EXPORTAR")
                ))
                .build();

        // 3. Ciudadano sin permisos de mapa administrativo
        Persona pCiu = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI").numeroDocumento("50" + String.format("%06d", (int)(ts % 1000000)))
                .nombres("Vecino").apellidoPaterno("Comun").estado("ACTIVO").build());
        Usuario uCiu = usuarioRepository.save(Usuario.builder()
                .username("ciu_map_" + ts).persona(pCiu).passwordHash(passwordEncoder.encode("Pass123!"))
                .enabled(true).accountLocked(false).roles(Set.of(rolCiudadano)).build());

        cudCiudadano = CustomUserDetails.builder()
                .id(uCiu.getId()).username(uCiu.getUsername()).password("dummy").enabled(true).accountNonLocked(true)
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_CIUDADANO")))
                .build();
        ciudadanoToken = jwtTokenProvider.generateAccessToken(cudCiudadano);
    }

    @Test
    @DisplayName("SEC-01: Usuario anónimo no puede acceder al mapa web -> Redirección a login")
    void testAccesoAnonimoRedirigeLogin() throws Exception {
        mockMvc.perform(get("/dashboard/serenazgo/mapa"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("SEC-02: Usuario sin autoridad SERENAZGO_MAPA_VER recibe 403 Forbidden")
    void testAccesoDenegadoParaCiudadano() throws Exception {
        mockMvc.perform(get("/dashboard/serenazgo/mapa")
                        .with(user(cudCiudadano)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("SEC-03: Administrador con ROLE_ADMIN accede exitosamente a la vista del mapa")
    void testAccesoAdminExitoso() throws Exception {
        mockMvc.perform(get("/dashboard/serenazgo/mapa")
                        .with(user(cudAdmin)))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/serenazgo/mapa/index"))
                .andExpect(model().attributeExists("stats"))
                .andExpect(model().attributeExists("sectores"));
    }

    @Test
    @DisplayName("SEC-04: Supervisor de Serenazgo accede exitosamente a la vista del mapa")
    void testAccesoSupervisorExitoso() throws Exception {
        mockMvc.perform(get("/dashboard/serenazgo/mapa")
                        .with(user(cudSupervisor)))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/serenazgo/mapa/index"));
    }

    @Test
    @DisplayName("SEC-05: API REST /sectores responde 200 JSON con token JWT de administrador")
    void testApiSectoresConJwt() throws Exception {
        mockMvc.perform(get("/api/v1/serenazgo/mapa/sectores")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("FeatureCollection"));
    }

    @Test
    @DisplayName("SEC-06: API REST /alertas con JWT de usuario no autorizado es bloqueada con 403")
    void testApiAlertasBloqueadaSinPermiso() throws Exception {
        mockMvc.perform(get("/api/v1/serenazgo/mapa/alertas")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ciudadanoToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("SEC-07: Exportación de GeoJSON protegida con permiso SERENAZGO_MAPA_EXPORTAR")
    void testExportarGeoJsonPermitidoAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/serenazgo/mapa/exportar/geojson?capa=SECTORES")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(header().exists(HttpHeaders.CONTENT_DISPOSITION));
    }

    @Test
    @DisplayName("SEC-08: Exportación de Excel en Web Controller protegida con autorización")
    void testExportarExcelWebController() throws Exception {
        mockMvc.perform(get("/dashboard/serenazgo/mapa/exportar/excel")
                        .with(user(cudAdmin)))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.CONTENT_DISPOSITION));
    }
}
