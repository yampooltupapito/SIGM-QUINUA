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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class AlertasWebSecurityTest {

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
    private String serenoToken;
    private String ciudadanoToken;

    @BeforeEach
    void setUp() {
        long ts = System.currentTimeMillis();

        Rol rolAdmin = rolRepository.findByCodigo("ROLE_ADMIN").orElseThrow();
        Rol rolSereno = rolRepository.findByCodigo("ROLE_SERENO").orElseThrow();
        Rol rolCiudadano = rolRepository.findByCodigo("ROLE_CIUDADANO").orElseThrow();

        // 1. Admin
        Persona pAdmin = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI").numeroDocumento("87" + String.format("%06d", (int)(ts % 1000000)))
                .nombres("Admin").apellidoPaterno("Alertas").apellidoMaterno("Quinua").estado("ACTIVO").build());
        Usuario uAdmin = usuarioRepository.save(Usuario.builder()
                .persona(pAdmin).username("admin_alt_" + ts).passwordHash(passwordEncoder.encode("Pass123!"))
                .roles(Set.of(rolAdmin)).enabled(true).accountLocked(false).build());

        // 2. Sereno
        Persona pSereno = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI").numeroDocumento("88" + String.format("%06d", (int)(ts % 1000000)))
                .nombres("Sereno").apellidoPaterno("Alertas").apellidoMaterno("Quinua").estado("ACTIVO").build());
        Usuario uSereno = usuarioRepository.save(Usuario.builder()
                .persona(pSereno).username("sereno_alt_" + ts).passwordHash(passwordEncoder.encode("Pass123!"))
                .roles(Set.of(rolSereno)).enabled(true).accountLocked(false).build());

        // 3. Ciudadano
        Persona pCiud = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI").numeroDocumento("89" + String.format("%06d", (int)(ts % 1000000)))
                .nombres("Ciudadano").apellidoPaterno("Alertas").apellidoMaterno("Quinua").estado("ACTIVO").build());
        Usuario uCiud = usuarioRepository.save(Usuario.builder()
                .persona(pCiud).username("ciud_alt_" + ts).passwordHash(passwordEncoder.encode("Pass123!"))
                .roles(Set.of(rolCiudadano)).enabled(true).accountLocked(false).build());

        // CustomUserDetails y Tokens JWT
        CustomUserDetails cudAdmin = CustomUserDetails.builder()
                .id(uAdmin.getId()).username(uAdmin.getUsername()).password("dummy").enabled(true).accountNonLocked(true)
                .roles(Set.of("ROLE_ADMIN"))
                .permisos(Set.of("ROLE_ADMIN", "SERENAZGO_ALERTAS_VER", "SERENAZGO_ALERTAS_CLASIFICAR"))
                .authorities(List.of(
                        new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("SERENAZGO_ALERTAS_VER"),
                        new SimpleGrantedAuthority("SERENAZGO_ALERTAS_CLASIFICAR")))
                .build();
        adminToken = jwtTokenProvider.generateAccessToken(cudAdmin);

        CustomUserDetails cudSereno = CustomUserDetails.builder()
                .id(uSereno.getId()).username(uSereno.getUsername()).password("dummy").enabled(true).accountNonLocked(true)
                .roles(Set.of("ROLE_SERENO"))
                .permisos(Set.of("ROLE_SERENO", "SERENAZGO_ALERTAS_VER"))
                .authorities(List.of(
                        new SimpleGrantedAuthority("ROLE_SERENO"),
                        new SimpleGrantedAuthority("SERENAZGO_ALERTAS_VER")))
                .build();
        serenoToken = jwtTokenProvider.generateAccessToken(cudSereno);

        CustomUserDetails cudCiud = CustomUserDetails.builder()
                .id(uCiud.getId()).username(uCiud.getUsername()).password("dummy").enabled(true).accountNonLocked(true)
                .roles(Set.of("ROLE_CIUDADANO"))
                .permisos(Set.of())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_CIUDADANO")))
                .build();
        ciudadanoToken = jwtTokenProvider.generateAccessToken(cudCiud);
    }

    @Test
    @DisplayName("Portal ciudadano: formulario de reporte accesible sin autenticación (público)")
    void testFormularioReportePublico() throws Exception {
        mockMvc.perform(get("/portal/seguridad/reportar"))
                .andExpect(status().isOk())
                .andExpect(view().name("portal/seguridad/reportar"));
    }

    @Test
    @DisplayName("Portal ciudadano: seguimiento por código y PIN accesible sin autenticación")
    void testSeguimientoPublico() throws Exception {
        mockMvc.perform(get("/portal/seguridad/seguimiento"))
                .andExpect(status().isOk())
                .andExpect(view().name("portal/seguridad/seguimiento"));
    }

    @Test
    @DisplayName("Portal ciudadano: mis reportes requiere autenticación (redirige a login si anónimo)")
    void testMisReportesRequiereAuth() throws Exception {
        mockMvc.perform(get("/portal/seguridad/mis-reportes"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("Portal ciudadano: mis reportes con usuario autenticado retorna 200 OK")
    void testMisReportesConAuth() throws Exception {
        mockMvc.perform(get("/portal/seguridad/mis-reportes")
                        .with(user("ciudadano.alertas").roles("CIUDADANO")))
                .andExpect(status().isOk())
                .andExpect(view().name("portal/seguridad/mis-reportes"));
    }

    @Test
    @DisplayName("Dashboard de alertas: anónimo es redirigido o denegado")
    void testDashboardAnonimoDenegado() throws Exception {
        mockMvc.perform(get("/dashboard/serenazgo/alertas"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("Dashboard de alertas: usuario con rol ADMIN o SERENO accede exitosamente (200 OK)")
    void testDashboardAdminAcceso() throws Exception {
        mockMvc.perform(get("/dashboard/serenazgo/alertas")
                        .with(user("admin.alertas").authorities(new SimpleGrantedAuthority("SERENAZGO_ALERTAS_VER"), new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/serenazgo/alertas/index"));
    }

    @Test
    @DisplayName("Dashboard de alertas: ciudadano sin permisos es denegado con 403 Forbidden")
    void testDashboardCiudadanoDenegado() throws Exception {
        mockMvc.perform(get("/dashboard/serenazgo/alertas")
                        .with(user("ciudadano.alertas").roles("CIUDADANO")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("API REST de alertas: GET /api/serenazgo/alertas con token ADMIN retorna 200")
    void testApiListarAlertasConTokenAdmin() throws Exception {
        mockMvc.perform(get("/api/serenazgo/alertas")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("API REST de alertas: GET /api/serenazgo/alertas con token CIUDADANO retorna 403")
    void testApiListarAlertasConTokenCiudadanoRetorna403() throws Exception {
        mockMvc.perform(get("/api/serenazgo/alertas")
                        .header("Authorization", "Bearer " + ciudadanoToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
