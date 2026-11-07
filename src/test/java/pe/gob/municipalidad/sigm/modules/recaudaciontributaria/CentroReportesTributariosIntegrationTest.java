package pe.gob.municipalidad.sigm.modules.recaudaciontributaria;

import jakarta.servlet.http.Cookie;
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
import pe.gob.municipalidad.sigm.common.constants.SecurityConstants;
import pe.gob.municipalidad.sigm.common.model.Persona;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.PersonaRepository;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails;
import pe.gob.municipalidad.sigm.security.jwt.JwtTokenProvider;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class CentroReportesTributariosIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Cookie adminCookie;

    @BeforeEach
    void setUp() {
        long suffix = System.currentTimeMillis() % 100000000;

        Persona personaAdmin = Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento(String.format("%08d", (suffix + 8000) % 100000000))
                .nombres("Especialista")
                .apellidoPaterno("Reportes")
                .apellidoMaterno("Quinua")
                .build();
        personaAdmin = personaRepository.save(personaAdmin);

        Usuario adminUser = Usuario.builder()
                .username("analista_quinua_" + suffix)
                .passwordHash("$2a$10$abcdefghijklmnopqrstuvwxyz123456")
                .persona(personaAdmin)
                .enabled(true)
                .build();
        adminUser = usuarioRepository.save(adminUser);

        CustomUserDetails adminDetails = CustomUserDetails.builder()
                .id(adminUser.getId())
                .username(adminUser.getUsername())
                .password("dummy")
                .enabled(true)
                .accountNonLocked(true)
                .authorities(List.of(
                        new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("ROLE_TRIBUTACION"),
                        new SimpleGrantedAuthority("TRIBUTARIA_REPORTES_ACCEDER"),
                        new SimpleGrantedAuthority("TRIBUTARIA_REPORTES_EXPORTAR"),
                        new SimpleGrantedAuthority("TRIBUTARIA_METAS_GESTIONAR")
                ))
                .build();

        String token = jwtTokenProvider.generateAccessToken(adminDetails);
        adminCookie = new Cookie(SecurityConstants.TOKEN_COOKIE_NAME, token);
        adminCookie.setPath("/");
    }

    @Test
    @DisplayName("Debe cargar exitosamente el dashboard principal de reportes tributarios")
    void testDashboardPrincipal_Exitoso() throws Exception {
        mockMvc.perform(get("/dashboard/tributaria/reportes")
                        .cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/tributaria/reportes/dashboard"))
                .andExpect(model().attributeExists("kpis"))
                .andExpect(model().attributeExists("recaudacionMensual"))
                .andExpect(model().attributeExists("recaudacionConceptos"))
                .andExpect(model().attributeExists("catalogoReportes"));
    }

    @Test
    @DisplayName("Debe cargar la vista de recaudación tributaria")
    void testVistaRecaudacion_Exitoso() throws Exception {
        mockMvc.perform(get("/dashboard/tributaria/reportes/recaudacion")
                        .cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/tributaria/reportes/recaudacion"))
                .andExpect(model().attributeExists("kpis"))
                .andExpect(model().attributeExists("resumenCajas"));
    }

    @Test
    @DisplayName("Debe cargar la vista de cartera pendiente y morosidad")
    void testVistaCartera_Exitoso() throws Exception {
        mockMvc.perform(get("/dashboard/tributaria/reportes/cartera")
                        .cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/tributaria/reportes/cartera"))
                .andExpect(model().attributeExists("carteraTramos"));
    }

    @Test
    @DisplayName("Debe cargar la vista de contribuyentes")
    void testVistaContribuyentes_Exitoso() throws Exception {
        mockMvc.perform(get("/dashboard/tributaria/reportes/contribuyentes")
                        .cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/tributaria/reportes/contribuyentes"))
                .andExpect(model().attributeExists("estadisticas"));
    }

    @Test
    @DisplayName("Debe cargar la vista de predios")
    void testVistaPredios_Exitoso() throws Exception {
        mockMvc.perform(get("/dashboard/tributaria/reportes/predios")
                        .cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/tributaria/reportes/predios"))
                .andExpect(model().attributeExists("estadisticas"));
    }

    @Test
    @DisplayName("Debe cargar la vista de fraccionamientos")
    void testVistaFraccionamientos_Exitoso() throws Exception {
        mockMvc.perform(get("/dashboard/tributaria/reportes/fraccionamientos")
                        .cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/tributaria/reportes/fraccionamientos"))
                .andExpect(model().attributeExists("resumen"));
    }

    @Test
    @DisplayName("Debe cargar la vista de notificaciones tributarias")
    void testVistaNotificaciones_Exitoso() throws Exception {
        mockMvc.perform(get("/dashboard/tributaria/reportes/notificaciones")
                        .cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/tributaria/reportes/notificaciones"))
                .andExpect(model().attributeExists("estadisticas"));
    }

    @Test
    @DisplayName("Debe cargar la vista de metas de recaudación")
    void testVistaMetas_Exitoso() throws Exception {
        mockMvc.perform(get("/dashboard/tributaria/reportes/metas")
                        .cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/tributaria/reportes/metas"))
                .andExpect(model().attributeExists("metas"));
    }

    @Test
    @DisplayName("Debe cargar el historial de exportaciones")
    void testVistaHistorialExportaciones_Exitoso() throws Exception {
        mockMvc.perform(get("/dashboard/tributaria/reportes/exportaciones")
                        .cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/tributaria/reportes/exportaciones"))
                .andExpect(model().attributeExists("exportaciones"));
    }

    @Test
    @DisplayName("Debe retornar los KPIs tributarios a través de la API REST")
    void testApiKpis_Exitoso() throws Exception {
        mockMvc.perform(get("/api/dashboard/tributaria/reportes/kpis")
                        .cookie(adminCookie)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.recaudacionConfirmadaTotal").exists())
                .andExpect(jsonPath("$.saldoPendienteTotal").exists());
    }

    @Test
    @DisplayName("Debe generar y descargar el reporte consolidado en formato PDF")
    void testExportarPdf_Exitoso() throws Exception {
        mockMvc.perform(get("/dashboard/tributaria/reportes/exportar/pdf")
                        .cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition", containsString("Reporte_Tributario_Quinua.pdf")));
    }

    @Test
    @DisplayName("Debe denegar acceso anónimo redirigiendo a login")
    void testAccesoAnonimo_Denegado() throws Exception {
        mockMvc.perform(get("/dashboard/tributaria/reportes"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", containsString("/login")));
    }
}
