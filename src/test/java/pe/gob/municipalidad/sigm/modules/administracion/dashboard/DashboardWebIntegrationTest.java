package pe.gob.municipalidad.sigm.modules.administracion.dashboard;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
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
import pe.gob.municipalidad.sigm.modules.notificaciones.model.Notificacion;
import pe.gob.municipalidad.sigm.modules.notificaciones.repository.NotificacionRepository;
import pe.gob.municipalidad.sigm.security.jwt.JwtTokenProvider;

import jakarta.servlet.http.Cookie;
import java.util.Set;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
class DashboardWebIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private NotificacionRepository notificacionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private Usuario adminUser;
    private Cookie authCookie;
    private Notificacion testNotif;

    @BeforeEach
    void setUp() {
        Rol rolAdmin = rolRepository.findByCodigo("ROLE_ADMIN").orElseGet(() ->
                rolRepository.save(Rol.builder()
                        .codigo("ROLE_ADMIN")
                        .nombre("Administrador General")
                        .estado("ACTIVO")
                        .build()));

        Persona personaAdmin = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento("10203040")
                .nombres("Carlos")
                .apellidoPaterno("Mendoza")
                .correo("admin.dashboard@quinua.gob.pe")
                .build());

        adminUser = usuarioRepository.save(Usuario.builder()
                .username("admin.dash")
                .persona(personaAdmin)
                .passwordHash(passwordEncoder.encode("AdminPass2026!"))
                .enabled(true)
                .accountLocked(false)
                .roles(Set.of(rolAdmin))
                .build());

        pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails details =
                pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails.builder()
                        .id(adminUser.getId())
                        .username(adminUser.getUsername())
                        .password("dummy")
                        .enabled(true)
                        .accountNonLocked(true)
                        .roles(Set.of("ROLE_ADMIN"))
                        .permisos(Set.of("ADMINISTRACION_MANAGE", "DASHBOARD_VIEW"))
                        .authorities(java.util.List.of(
                                new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN"),
                                new org.springframework.security.core.authority.SimpleGrantedAuthority("ADMINISTRACION_MANAGE")
                        ))
                        .build();

        String jwt = jwtTokenProvider.generateAccessToken(details);
        authCookie = new Cookie(SecurityConstants.TOKEN_COOKIE_NAME, jwt);
        authCookie.setPath("/");

        testNotif = notificacionRepository.save(Notificacion.builder()
                .usuario(adminUser)
                .titulo("Notificación de Prueba")
                .mensaje("Mensaje descriptivo para pruebas de integración")
                .tipo("INFO")
                .leida(false)
                .build());
    }

    @AfterEach
    void tearDown() {
        if (testNotif != null && testNotif.getId() != null) {
            notificacionRepository.deleteById(testNotif.getId());
        }
        if (adminUser != null && adminUser.getId() != null) {
            usuarioRepository.deleteById(adminUser.getId());
        }
    }

    @Test
    @DisplayName("1. Denegación de acceso anónimo a /admin/dashboard: Redirección a /login")
    void testAccesoAnonimoRechazado() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("2. Acceso autenticado de administrador a /admin/dashboard: HTTP 200 con HTML completo")
    void testAccesoAutenticadoDashboard() throws Exception {
        mockMvc.perform(get("/admin/dashboard").cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("SIGM-QUINUA")))
                .andExpect(content().string(containsString("Indicadores Administrativos Reales")))
                .andExpect(content().string(containsString("Usuarios Activos")))
                .andExpect(content().string(containsString("Oficinas y Áreas")))
                .andExpect(content().string(containsString("chartUsuariosEstado")))
                .andExpect(content().string(containsString("chartUsuariosOficina")))
                .andExpect(content().string(containsString("chartUsuariosRol")))
                .andExpect(content().string(containsString("Trazabilidad de Operaciones Recientes")));
    }

    @Test
    @DisplayName("3. API REST /api/dashboard/stats: Recuperación de métricas reales")
    void testApiDashboardStats() throws Exception {
        mockMvc.perform(get("/api/dashboard/stats").cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalUsuariosActivos", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.data.totalOficinasActivas", greaterThanOrEqualTo(0)))
                .andExpect(jsonPath("$.data.recaudacionTributaria", containsString("Operativo")));
    }

    @Test
    @DisplayName("4. API REST /api/dashboard/charts: Recuperación de datasets para Chart.js")
    void testApiDashboardCharts() throws Exception {
        mockMvc.perform(get("/api/dashboard/charts").cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.usuariosPorEstado.labels").isArray())
                .andExpect(jsonPath("$.data.usuariosPorOficina.labels").isArray())
                .andExpect(jsonPath("$.data.usuariosPorRol.labels").isArray());
    }

    @Test
    @DisplayName("5. API REST /api/dashboard/actividad: Paginación y filtros")
    void testApiDashboardActividad() throws Exception {
        mockMvc.perform(get("/api/dashboard/actividad")
                        .param("periodo", "todos")
                        .param("resultado", "todos")
                        .param("page", "0")
                        .param("size", "5")
                        .cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @DisplayName("6. API REST /api/notificaciones: Lista de notificaciones del usuario")
    void testApiNotificaciones() throws Exception {
        mockMvc.perform(get("/api/notificaciones").cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.notificaciones").isArray())
                .andExpect(jsonPath("$.data.noLeidas", greaterThanOrEqualTo(1)));
    }

    @Test
    @DisplayName("7. API REST /api/notificaciones/{id}/leer: Marcar notificación como leída")
    void testApiMarcarNotificacionLeida() throws Exception {
        mockMvc.perform(put("/api/notificaciones/" + testNotif.getId() + "/leer")
                        .with(csrf())
                        .cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("8. API REST /api/notificaciones/marcar-todas: Marcar todas como leídas en bloque")
    void testApiMarcarTodasNotificacionesLeidas() throws Exception {
        mockMvc.perform(put("/api/notificaciones/marcar-todas")
                        .with(csrf())
                        .cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
