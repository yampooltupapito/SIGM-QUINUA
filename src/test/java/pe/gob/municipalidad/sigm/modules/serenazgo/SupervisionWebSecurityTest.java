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
import pe.gob.municipalidad.sigm.common.model.Rol;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.PersonaRepository;
import pe.gob.municipalidad.sigm.common.repository.RolRepository;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails;
import pe.gob.municipalidad.sigm.security.jwt.JwtTokenProvider;

import java.util.List;
import java.util.Set;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class SupervisionWebSecurityTest {

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

        // Admin
        Persona perAdmin = personaRepository.save(Persona.builder()
                .numeroDocumento(String.format("7%07d", (int)(Math.abs(ts) % 9000000 + 1000000)))
                .tipoDocumento("DNI")
                .nombres("Admin")
                .apellidoPaterno("Supervision")
                .apellidoMaterno("Quinua")
                .estado("ACTIVO")
                .build());

        Usuario admin = usuarioRepository.save(Usuario.builder()
                .username("admin_sup_" + ts)
                .persona(perAdmin)
                .passwordHash(passwordEncoder.encode("AdminPass123!"))
                .roles(Set.of(rolAdmin, rolSupervisor))
                .enabled(true)
                .accountLocked(false)
                .build());

        cudAdmin = CustomUserDetails.builder()
                .id(admin.getId())
                .username(admin.getUsername())
                .password("dummy")
                .enabled(true)
                .accountNonLocked(true)
                .authorities(List.of(
                        new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("ROLE_SUPERVISOR_SERENAZGO"),
                        new SimpleGrantedAuthority("SERENAZGO_SUPERVISION_VER"),
                        new SimpleGrantedAuthority("SERENAZGO_SUPERVISION_VER_INDICADORES"),
                        new SimpleGrantedAuthority("SERENAZGO_SUPERVISION_VER_PENDIENTES"),
                        new SimpleGrantedAuthority("SERENAZGO_SUPERVISION_CREAR_SEGUIMIENTO")
                ))
                .build();
        adminToken = jwtTokenProvider.generateAccessToken(cudAdmin);

        // Supervisor
        Persona perSup = personaRepository.save(Persona.builder()
                .numeroDocumento(String.format("8%07d", (int)(Math.abs(ts) % 9000000 + 1000000)))
                .tipoDocumento("DNI")
                .nombres("Supervisor")
                .apellidoPaterno("Centro")
                .apellidoMaterno("Operativo")
                .estado("ACTIVO")
                .build());

        Usuario sup = usuarioRepository.save(Usuario.builder()
                .username("sup_web_" + ts)
                .persona(perSup)
                .passwordHash(passwordEncoder.encode("SupPass123!"))
                .roles(Set.of(rolSupervisor))
                .enabled(true)
                .accountLocked(false)
                .build());

        cudSupervisor = CustomUserDetails.builder()
                .id(sup.getId())
                .username(sup.getUsername())
                .password("dummy")
                .enabled(true)
                .accountNonLocked(true)
                .authorities(List.of(
                        new SimpleGrantedAuthority("ROLE_SUPERVISOR_SERENAZGO"),
                        new SimpleGrantedAuthority("SERENAZGO_SUPERVISION_VER"),
                        new SimpleGrantedAuthority("SERENAZGO_SUPERVISION_VER_INDICADORES"),
                        new SimpleGrantedAuthority("SERENAZGO_SUPERVISION_VER_PENDIENTES"),
                        new SimpleGrantedAuthority("SERENAZGO_SUPERVISION_CREAR_SEGUIMIENTO")
                ))
                .build();

        // Ciudadano sin permisos
        Persona perCiu = personaRepository.save(Persona.builder()
                .numeroDocumento(String.format("9%07d", (int)(Math.abs(ts) % 9000000 + 1000000)))
                .tipoDocumento("DNI")
                .nombres("Vecino")
                .apellidoPaterno("Quinua")
                .apellidoMaterno("SinPermiso")
                .estado("ACTIVO")
                .build());

        Usuario ciu = usuarioRepository.save(Usuario.builder()
                .username("ciu_sup_" + ts)
                .persona(perCiu)
                .passwordHash(passwordEncoder.encode("CiuPass123!"))
                .roles(Set.of(rolCiudadano))
                .enabled(true)
                .accountLocked(false)
                .build());

        cudCiudadano = CustomUserDetails.builder()
                .id(ciu.getId())
                .username(ciu.getUsername())
                .password("dummy")
                .enabled(true)
                .accountNonLocked(true)
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_CIUDADANO")))
                .build();
        ciudadanoToken = jwtTokenProvider.generateAccessToken(cudCiudadano);
    }

    @Test
    @DisplayName("1. Redireccionar o rechazar acceso anónimo a /dashboard/serenazgo/supervision")
    void testAccesoAnonimoDashboardRechazado() throws Exception {
        mockMvc.perform(get("/dashboard/serenazgo/supervision"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("2. Denegar acceso a ciudadano a /dashboard/serenazgo/supervision")
    void testAccesoCiudadanoDashboardDenegado() throws Exception {
        mockMvc.perform(get("/dashboard/serenazgo/supervision")
                        .with(user(cudCiudadano)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("3. Permitir acceso a supervisor a /dashboard/serenazgo/supervision")
    void testAccesoSupervisorDashboardPermitido() throws Exception {
        mockMvc.perform(get("/dashboard/serenazgo/supervision")
                        .with(user(cudSupervisor)))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/serenazgo/supervision/index"))
                .andExpect(model().attributeExists("indicadores", "pendientes", "graficos"));
    }

    @Test
    @DisplayName("4. Permitir acceso a supervisor a /dashboard/serenazgo/supervision/seguimientos")
    void testAccesoSupervisorSeguimientosPermitido() throws Exception {
        mockMvc.perform(get("/dashboard/serenazgo/supervision/seguimientos")
                        .with(user(cudSupervisor)))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/serenazgo/supervision/seguimientos"))
                .andExpect(model().attributeExists("pagina"));
    }

    @Test
    @DisplayName("5. Rechazar llamada REST de ciudadano a /api/v1/serenazgo/supervision/indicadores (403 Forbidden)")
    void testRestIndicadoresCiudadanoDenegado() throws Exception {
        mockMvc.perform(get("/api/v1/serenazgo/supervision/indicadores")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ciudadanoToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("6. Permitir llamada REST de admin a /api/v1/serenazgo/supervision/indicadores (200 OK)")
    void testRestIndicadoresAdminPermitido() throws Exception {
        mockMvc.perform(get("/api/v1/serenazgo/supervision/indicadores")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.zonaHoraria").value("America/Lima"))
                .andExpect(jsonPath("$.fechaHoraActualizacion").isNotEmpty());
    }

    @Test
    @DisplayName("7. Rechazar POST en /dashboard/serenazgo/supervision/seguimientos sin token CSRF")
    void testPostSeguimientoSinCsrfRechazado() throws Exception {
        mockMvc.perform(post("/dashboard/serenazgo/supervision/seguimientos")
                        .with(user(cudSupervisor))
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("tipoRegistroOrigen", "INCIDENCIA")
                        .param("descripcion", "Intento de registro sin token CSRF"))
                .andExpect(status().isForbidden());
    }
}
