package pe.gob.municipalidad.sigm.modules.mercados;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import pe.gob.municipalidad.sigm.modules.mercados.dto.ExportacionReporteRequest;
import pe.gob.municipalidad.sigm.modules.mercados.dto.ReporteMercadoFilter;
import pe.gob.municipalidad.sigm.modules.mercados.entity.MercadoMunicipal;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoMercado;
import pe.gob.municipalidad.sigm.modules.mercados.enums.FormatoExportacion;
import pe.gob.municipalidad.sigm.modules.mercados.enums.PeriodoConsulta;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoReporteMercado;
import pe.gob.municipalidad.sigm.modules.mercados.repository.MercadoMunicipalRepository;
import pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails;
import pe.gob.municipalidad.sigm.security.jwt.JwtTokenProvider;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MercadoReportesWebSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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

    @Autowired
    private MercadoMunicipalRepository mercadoRepository;

    private String adminToken;
    private String operadorToken;
    private String ciudadanoToken;
    private MercadoMunicipal mercado;

    @BeforeEach
    void setUp() {
        long ts = System.currentTimeMillis();

        Rol rolAdmin = rolRepository.findByCodigo("ROLE_ADMIN").orElseGet(() ->
                rolRepository.save(Rol.builder().codigo("ROLE_ADMIN").nombre("Administrador").estado("ACTIVO").build()));

        Rol rolOperador = rolRepository.findByCodigo("ROLE_OPERADOR").orElseGet(() ->
                rolRepository.save(Rol.builder().codigo("ROLE_OPERADOR").nombre("Operador").estado("ACTIVO").build()));

        Rol rolCiudadano = rolRepository.findByCodigo("ROLE_CIUDADANO").orElseGet(() ->
                rolRepository.save(Rol.builder().codigo("ROLE_CIUDADANO").nombre("Ciudadano").estado("ACTIVO").build()));

        // Usuario Admin
        Persona pAdmin = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI").numeroDocumento("70" + String.format("%06d", (int)(ts % 1000000)))
                .nombres("Admin").apellidoPaterno("Reportes").correo("admin.rep" + ts + "@quinua.gob.pe").estado("ACTIVO").build());
        Usuario uAdmin = usuarioRepository.save(Usuario.builder()
                .username("admin_rep_" + ts).persona(pAdmin).passwordHash(passwordEncoder.encode("Pass123!"))
                .enabled(true).accountLocked(false).roles(Set.of(rolAdmin)).build());

        CustomUserDetails adminDetails = CustomUserDetails.builder()
                .id(uAdmin.getId()).username(uAdmin.getUsername()).password("dummy").enabled(true).accountNonLocked(true)
                .roles(Set.of("ROLE_ADMIN"))
                .permisos(Set.of(
                        "ROLE_ADMIN",
                        "MERCADOS_REPORTES_VER",
                        "MERCADOS_REPORTES_EJECUTIVO",
                        "MERCADOS_REPORTES_OCUPACION",
                        "MERCADOS_REPORTES_COMERCIANTES",
                        "MERCADOS_REPORTES_CONTRATOS",
                        "MERCADOS_REPORTES_FINANZAS",
                        "MERCADOS_REPORTES_INSPECCIONES",
                        "MERCADOS_REPORTES_INCIDENCIAS",
                        "MERCADOS_REPORTES_EXPORTAR_EXCEL",
                        "MERCADOS_REPORTES_EXPORTAR_PDF",
                        "MERCADOS_REPORTES_HISTORIAL_VER"
                ))
                .authorities(List.of(
                        new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("MERCADOS_REPORTES_VER"),
                        new SimpleGrantedAuthority("MERCADOS_REPORTES_EJECUTIVO"),
                        new SimpleGrantedAuthority("MERCADOS_REPORTES_OCUPACION"),
                        new SimpleGrantedAuthority("MERCADOS_REPORTES_COMERCIANTES"),
                        new SimpleGrantedAuthority("MERCADOS_REPORTES_CONTRATOS"),
                        new SimpleGrantedAuthority("MERCADOS_REPORTES_FINANZAS"),
                        new SimpleGrantedAuthority("MERCADOS_REPORTES_INSPECCIONES"),
                        new SimpleGrantedAuthority("MERCADOS_REPORTES_INCIDENCIAS"),
                        new SimpleGrantedAuthority("MERCADOS_REPORTES_EXPORTAR_EXCEL"),
                        new SimpleGrantedAuthority("MERCADOS_REPORTES_EXPORTAR_PDF"),
                        new SimpleGrantedAuthority("MERCADOS_REPORTES_HISTORIAL_VER")
                ))
                .build();
        adminToken = jwtTokenProvider.generateAccessToken(adminDetails);

        // Usuario Operador (solo ve ocupación e incidencias, NO finanzas)
        Persona pOperador = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI").numeroDocumento("71" + String.format("%06d", (int)(ts % 1000000)))
                .nombres("Operador").apellidoPaterno("Mercado").correo("op.rep" + ts + "@quinua.gob.pe").estado("ACTIVO").build());
        Usuario uOperador = usuarioRepository.save(Usuario.builder()
                .username("operador_rep_" + ts).persona(pOperador).passwordHash(passwordEncoder.encode("Pass123!"))
                .enabled(true).accountLocked(false).roles(Set.of(rolOperador)).build());

        CustomUserDetails operadorDetails = CustomUserDetails.builder()
                .id(uOperador.getId()).username(uOperador.getUsername()).password("dummy").enabled(true).accountNonLocked(true)
                .roles(Set.of("ROLE_OPERADOR"))
                .permisos(Set.of("MERCADOS_REPORTES_VER", "MERCADOS_REPORTES_OCUPACION", "MERCADOS_REPORTES_INCIDENCIAS"))
                .authorities(List.of(
                        new SimpleGrantedAuthority("ROLE_OPERADOR"),
                        new SimpleGrantedAuthority("MERCADOS_REPORTES_VER"),
                        new SimpleGrantedAuthority("MERCADOS_REPORTES_OCUPACION"),
                        new SimpleGrantedAuthority("MERCADOS_REPORTES_INCIDENCIAS")
                ))
                .build();
        operadorToken = jwtTokenProvider.generateAccessToken(operadorDetails);

        // Ciudadano sin permisos
        Persona pCiudadano = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI").numeroDocumento("72" + String.format("%06d", (int)(ts % 1000000)))
                .nombres("Ciudadano").apellidoPaterno("SinAcceso").correo("ciud.rep" + ts + "@quinua.gob.pe").estado("ACTIVO").build());
        Usuario uCiudadano = usuarioRepository.save(Usuario.builder()
                .username("ciudadano_rep_" + ts).persona(pCiudadano).passwordHash(passwordEncoder.encode("Pass123!"))
                .enabled(true).accountLocked(false).roles(Set.of(rolCiudadano)).build());

        CustomUserDetails ciudadanoDetails = CustomUserDetails.builder()
                .id(uCiudadano.getId()).username(uCiudadano.getUsername()).password("dummy").enabled(true).accountNonLocked(true)
                .roles(Set.of("ROLE_CIUDADANO"))
                .permisos(Set.of())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_CIUDADANO")))
                .build();
        ciudadanoToken = jwtTokenProvider.generateAccessToken(ciudadanoDetails);

        mercado = mercadoRepository.save(MercadoMunicipal.builder()
                .codigo("M-SEC-" + (ts % 10000))
                .nombre("Mercado Test Reportes")
                .direccion("Av. Centenario 123")
                .distrito("Quinua")
                .provincia("Huamanga")
                .departamento("Ayacucho")
                .estado(EstadoMercado.ACTIVO)
                .build());
    }

    @Test
    @DisplayName("01. Usuario no autenticado es rechazado en endpoints de reportes")
    void test01_unauthenticated_rejected() throws Exception {
        mockMvc.perform(get("/api/mercados/reportes/dashboard"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("02. Usuario admin accede exitosamente al dashboard de reportes API")
    void test02_admin_dashboard_api_ok() throws Exception {
        mockMvc.perform(get("/api/mercados/reportes/dashboard")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("mercadoId", mercado.getId().toString())
                        .param("periodo", PeriodoConsulta.MES_ACTUAL.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalMercados").exists())
                .andExpect(jsonPath("$.porcentajeOcupacionTexto").exists());
    }

    @Test
    @DisplayName("03. Usuario operador accede a ocupación pero es bloqueado en finanzas (403)")
    void test03_operador_rbac_restriction() throws Exception {
        // Operador tiene MERCADOS_REPORTES_OCUPACION
        mockMvc.perform(get("/api/mercados/reportes/ocupacion")
                        .header("Authorization", "Bearer " + operadorToken))
                .andExpect(status().isOk());

        // Operador NO tiene MERCADOS_REPORTES_FINANZAS -> 403 Forbidden
        mockMvc.perform(get("/api/mercados/reportes/recaudacion")
                        .header("Authorization", "Bearer " + operadorToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("04. Ciudadano sin permisos recibe 403 en reportes")
    void test04_ciudadano_forbidden() throws Exception {
        mockMvc.perform(get("/api/mercados/reportes/dashboard")
                        .header("Authorization", "Bearer " + ciudadanoToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("05. Exportación a Excel genera archivo binario con attachment")
    void test05_exportar_excel_ok() throws Exception {
        ExportacionReporteRequest req = ExportacionReporteRequest.builder()
                .tipoReporte(TipoReporteMercado.PUESTOS)
                .formato(FormatoExportacion.EXCEL)
                .filtro(ReporteMercadoFilter.builder().mercadoId(mercado.getId()).build())
                .build();

        mockMvc.perform(post("/api/mercados/reportes/exportar/excel")
                        .with(csrf())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("REPORTE_PUESTOS.xlsx")))
                .andExpect(content().contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    }

    @Test
    @DisplayName("06. Exportación a PDF genera documento institucional application/pdf")
    void test06_exportar_pdf_ok() throws Exception {
        ExportacionReporteRequest req = ExportacionReporteRequest.builder()
                .tipoReporte(TipoReporteMercado.EJECUTIVO)
                .formato(FormatoExportacion.PDF)
                .filtro(ReporteMercadoFilter.builder().mercadoId(mercado.getId()).build())
                .build();

        mockMvc.perform(post("/api/mercados/reportes/exportar/pdf")
                        .with(csrf())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("REPORTE_EJECUTIVO.pdf")))
                .andExpect(content().contentType(MediaType.APPLICATION_PDF));
    }

    @Test
    @DisplayName("07. Vistas web Thymeleaf de reportes renderizan 200 para admin")
    void test07_vistas_web_render_ok() throws Exception {
        Cookie authCookie = new Cookie(SecurityConstants.ACCESS_TOKEN_COOKIE_NAME, adminToken);

        mockMvc.perform(get("/dashboard/mercados/reportes").cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/mercados/reportes/index"));

        mockMvc.perform(get("/dashboard/mercados/reportes/ejecutivo").cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/mercados/reportes/ejecutivo"));

        mockMvc.perform(get("/dashboard/mercados/reportes/ocupacion").cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/mercados/reportes/ocupacion"));

        mockMvc.perform(get("/dashboard/mercados/reportes/recaudacion").cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/mercados/reportes/recaudacion"));
    }
}
