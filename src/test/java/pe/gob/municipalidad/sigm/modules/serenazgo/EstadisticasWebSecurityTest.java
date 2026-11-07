package pe.gob.municipalidad.sigm.modules.serenazgo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class EstadisticasWebSecurityTest {

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
                .numeroDocumento(String.format("1%07d", (int)(Math.abs(ts) % 9000000 + 1000000)))
                .tipoDocumento("DNI")
                .nombres("Admin")
                .apellidoPaterno("Estadistica")
                .apellidoMaterno("Quinua")
                .estado("ACTIVO")
                .build());

        Usuario admin = usuarioRepository.save(Usuario.builder()
                .username("admin_est_" + ts)
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
                        new SimpleGrantedAuthority("SERENAZGO_ESTADISTICAS_VER"),
                        new SimpleGrantedAuthority("SERENAZGO_REPORTES_GENERAR"),
                        new SimpleGrantedAuthority("SERENAZGO_REPORTES_DESCARGAR"),
                        new SimpleGrantedAuthority("SERENAZGO_REPORTES_VER_HISTORIAL")
                ))
                .build();
        adminToken = jwtTokenProvider.generateAccessToken(cudAdmin);

        // Supervisor
        Persona perSup = personaRepository.save(Persona.builder()
                .numeroDocumento(String.format("2%07d", (int)(Math.abs(ts) % 9000000 + 1000000)))
                .tipoDocumento("DNI")
                .nombres("Supervisor")
                .apellidoPaterno("Analitica")
                .apellidoMaterno("Quinua")
                .estado("ACTIVO")
                .build());

        Usuario sup = usuarioRepository.save(Usuario.builder()
                .username("sup_est_" + ts)
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
                        new SimpleGrantedAuthority("SERENAZGO_ESTADISTICAS_VER"),
                        new SimpleGrantedAuthority("SERENAZGO_REPORTES_GENERAR"),
                        new SimpleGrantedAuthority("SERENAZGO_REPORTES_DESCARGAR"),
                        new SimpleGrantedAuthority("SERENAZGO_REPORTES_VER_HISTORIAL")
                ))
                .build();

        // Ciudadano
        Persona perCiu = personaRepository.save(Persona.builder()
                .numeroDocumento(String.format("3%07d", (int)(Math.abs(ts) % 9000000 + 1000000)))
                .tipoDocumento("DNI")
                .nombres("Vecino")
                .apellidoPaterno("Quinua")
                .apellidoMaterno("SinPermiso")
                .estado("ACTIVO")
                .build());

        Usuario ciu = usuarioRepository.save(Usuario.builder()
                .username("ciu_est_" + ts)
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
    @DisplayName("1. Redireccionar acceso anónimo a /dashboard/serenazgo/estadisticas")
    void testAccesoAnonimoEstadisticasRedirecciona() throws Exception {
        mockMvc.perform(get("/dashboard/serenazgo/estadisticas"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("2. Redireccionar acceso anónimo a /dashboard/serenazgo/reportes")
    void testAccesoAnonimoReportesRedirecciona() throws Exception {
        mockMvc.perform(get("/dashboard/serenazgo/reportes"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("3. Denegar acceso a ciudadano a /dashboard/serenazgo/estadisticas")
    void testAccesoCiudadanoEstadisticasDenegado() throws Exception {
        mockMvc.perform(get("/dashboard/serenazgo/estadisticas")
                        .with(user(cudCiudadano)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("4. Denegar acceso REST API con token ciudadano")
    void testAccesoCiudadanoApiRestDenegado() throws Exception {
        mockMvc.perform(get("/api/v1/serenazgo/estadisticas/dashboard")
                        .header("Authorization", "Bearer " + ciudadanoToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("5. Permitir acceso a supervisor a /dashboard/serenazgo/estadisticas")
    void testAccesoSupervisorEstadisticasPermitido() throws Exception {
        mockMvc.perform(get("/dashboard/serenazgo/estadisticas")
                        .with(user(cudSupervisor)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("6. Permitir acceso a supervisor a /dashboard/serenazgo/reportes")
    void testAccesoSupervisorReportesPermitido() throws Exception {
        mockMvc.perform(get("/dashboard/serenazgo/reportes")
                        .with(user(cudSupervisor)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("7. Permitir acceso a REST API con token de administrador")
    void testAccesoAdminApiRestPermitido() throws Exception {
        mockMvc.perform(get("/api/v1/serenazgo/estadisticas/dashboard")
                        .header("Authorization", "Bearer " + adminToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
