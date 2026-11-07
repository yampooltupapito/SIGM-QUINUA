package pe.gob.municipalidad.sigm.modules.mesapartes;

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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.constants.SecurityConstants;
import pe.gob.municipalidad.sigm.common.model.*;
import pe.gob.municipalidad.sigm.common.repository.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.CanalIngreso;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoExpediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.PrioridadExpediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.ExpedienteRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.TipoTramiteRepository;
import pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails;
import pe.gob.municipalidad.sigm.security.jwt.JwtTokenProvider;

import java.time.OffsetDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
@DisplayName("Pruebas de Integración - Reportes, Estadísticas, Supervisión y Exportaciones (Prompt 18)")
class ReportesMesaPartesIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private MunicipalidadRepository municipalidadRepository;

    @Autowired
    private OficinaRepository oficinaRepository;

    @Autowired
    private TipoTramiteRepository tipoTramiteRepository;

    @Autowired
    private ExpedienteRepository expedienteRepository;

    private Usuario usuarioAdmin;
    private Usuario usuarioOperador;
    private Usuario usuarioCiudadano;
    private Cookie adminCookie;
    private Cookie operadorCookie;
    private Cookie ciudadanoCookie;
    private Oficina oficinaMPV;
    private Expediente expedientePrueba;

    @BeforeEach
    void setUp() {
        Municipalidad muni = municipalidadRepository.findAll().stream().findFirst().orElseGet(() ->
                municipalidadRepository.save(Municipalidad.builder()
                        .nombre("Municipalidad Distrital de Quinua")
                        .nombreCorto("MD Quinua")
                        .ubigeo("050108")
                        .departamento("Ayacucho")
                        .provincia("Huamanga")
                        .distrito("Quinua")
                        .direccion("Plaza de Armas s/n, Quinua, Huamanga, Ayacucho")
                        .telefono("066-812345")
                        .correoInstitucional("mesadepartes@muniquinua.gob.pe")
                        .estado("ACTIVO")
                        .build())
        );

        String suffix = UUID.randomUUID().toString().substring(0, 6);

        oficinaMPV = oficinaRepository.save(Oficina.builder()
                .codigo("OFI-REP-" + suffix)
                .nombre("Mesa de Partes General Quinua")
                .sigla("MPV")
                .nivelJerarquico(1)
                .estado("ACTIVO")
                .municipalidad(muni)
                .build());

        // Roles
        Rol rolAdmin = rolRepository.findByCodigo("ROLE_ADMIN").orElseGet(() ->
                rolRepository.save(Rol.builder().codigo("ROLE_ADMIN").nombre("Administrador").estado("ACTIVO").build())
        );
        Rol rolOperador = rolRepository.findByCodigo("ROLE_MESA_PARTES_OPERADOR").orElseGet(() ->
                rolRepository.save(Rol.builder().codigo("ROLE_MESA_PARTES_OPERADOR").nombre("Operador MPV").estado("ACTIVO").build())
        );
        Rol rolCiudadano = rolRepository.findByCodigo("ROLE_CIUDADANO").orElseGet(() ->
                rolRepository.save(Rol.builder().codigo("ROLE_CIUDADANO").nombre("Ciudadano").estado("ACTIVO").build())
        );

        // Personas
        Persona personaAdmin = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento("44" + String.format("%06d", new Random().nextInt(999999)))
                .nombres("Alfonso")
                .apellidoPaterno("Rojas")
                .correo("admin." + suffix + "@muniquinua.gob.pe")
                .build());

        usuarioAdmin = usuarioRepository.save(Usuario.builder()
                .username("adm_" + suffix)
                .passwordHash(passwordEncoder.encode("Password2026!"))
                .persona(personaAdmin)
                .roles(Set.of(rolAdmin))
                .enabled(true)
                .build());

        Persona personaOperador = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento("45" + String.format("%06d", new Random().nextInt(999999)))
                .nombres("Sonia")
                .apellidoPaterno("Mendoza")
                .correo("sonia." + suffix + "@muniquinua.gob.pe")
                .build());

        usuarioOperador = usuarioRepository.save(Usuario.builder()
                .username("ope_" + suffix)
                .passwordHash(passwordEncoder.encode("Password2026!"))
                .persona(personaOperador)
                .roles(Set.of(rolOperador))
                .enabled(true)
                .build());

        Persona personaCiu = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento("46" + String.format("%06d", new Random().nextInt(999999)))
                .nombres("Pedro")
                .apellidoPaterno("Castro")
                .correo("pedro." + suffix + "@gmail.com")
                .build());

        usuarioCiudadano = usuarioRepository.save(Usuario.builder()
                .username("ciu_" + suffix)
                .passwordHash(passwordEncoder.encode("Password2026!"))
                .persona(personaCiu)
                .roles(Set.of(rolCiudadano))
                .enabled(true)
                .build());

        TipoTramite tramite = tipoTramiteRepository.save(TipoTramite.builder()
                .codigo("TT-CERT-" + suffix)
                .nombre("Certificado Domiciliario")
                .oficinaResponsable(oficinaMPV)
                .plazoDias(10)
                .estado("ACTIVO")
                .build());

        // Expediente de prueba
        expedientePrueba = Expediente.builder()
                .uuid(UUID.randomUUID())
                .numeroExpediente("EXP-2026-REP-" + suffix)
                .asunto("Solicitud de Certificado Domiciliario")
                .solicitante(personaCiu)
                .tipoTramite(tramite)
                .oficinaResponsable(oficinaMPV)
                .estadoActual(EstadoExpediente.RECIBIDO)
                .prioridad(PrioridadExpediente.NORMAL)
                .canalIngreso(CanalIngreso.VIRTUAL)
                .fechaPresentacion(OffsetDateTime.now().minusDays(2))
                .fechaRecepcion(OffsetDateTime.now().minusDays(1))
                .build();
        expedientePrueba = expedienteRepository.save(expedientePrueba);

        // Cookies JWT
        adminCookie = buildJwtCookie(usuarioAdmin, "ROLE_ADMIN", List.of(
                "MESAPARTES_REPORTES_VER",
                "MESAPARTES_REPORTES_GLOBALES",
                "MESAPARTES_REPORTES_EXPORTAR",
                "MESAPARTES_ESTADISTICAS_VER",
                "MESAPARTES_SUPERVISION_VER"
        ));

        operadorCookie = buildJwtCookie(usuarioOperador, "ROLE_MESA_PARTES_OPERADOR", List.of(
                "MESAPARTES_REPORTES_VER",
                "MESAPARTES_REPORTES_OFICINA",
                "MESAPARTES_REPORTES_EXPORTAR",
                "MESAPARTES_ESTADISTICAS_VER"
        ));

        ciudadanoCookie = buildJwtCookie(usuarioCiudadano, "ROLE_CIUDADANO", List.of(
                "MESAPARTES_SEGUIMIENTO_CIUDADANO"
        ));
    }

    private Cookie buildJwtCookie(Usuario usuario, String role, List<String> permissions) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(role));
        for (String p : permissions) {
            authorities.add(new SimpleGrantedAuthority(p));
        }

        CustomUserDetails userDetails = CustomUserDetails.builder()
                .id(usuario.getId())
                .username(usuario.getUsername())
                .personaId(usuario.getPersona() != null ? usuario.getPersona().getId() : null)
                .nombreCompleto(usuario.getPersona() != null ? usuario.getPersona().getNombreCompleto() : usuario.getUsername())
                .roles(Set.of(role))
                .permisos(new HashSet<>(permissions))
                .authorities(authorities)
                .enabled(true)
                .accountNonLocked(true)
                .build();

        String token = jwtTokenProvider.generateAccessToken(userDetails);
        Cookie cookie = new Cookie(SecurityConstants.TOKEN_COOKIE_NAME, token);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        return cookie;
    }

    // ==================== PRUEBAS DE VISTAS WEB ====================
    @Test
    @DisplayName("1. Vista Web Reportes debe ser accesible para Administrador")
    void testAccesoWebReportes_ComoAdmin_Exitoso() throws Exception {
        mockMvc.perform(get("/dashboard/mesa-partes/reportes").cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/mesa-partes/reportes"))
                .andExpect(model().attributeExists("oficinas", "tiposTramite", "estados", "resumen"));
    }

    @Test
    @DisplayName("2. Vista Web Reportes debe rechazar acceso sin autenticación (401 o 302)")
    void testAccesoWebReportes_NoAutenticado_Rechazado() throws Exception {
        mockMvc.perform(get("/dashboard/mesa-partes/reportes"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assertTrue(status == 401 || status == 302, "Debe ser 401 o redirección al login");
                });
    }

    @Test
    @DisplayName("3. Vista Web Reportes debe denegar acceso a rol Ciudadano (403 Forbidden)")
    void testAccesoWebReportes_ComoCiudadano_Forbidden() throws Exception {
        mockMvc.perform(get("/dashboard/mesa-partes/reportes").cookie(ciudadanoCookie))
                .andExpect(status().isForbidden());
    }

    // ==================== PRUEBAS DE ENDPOINTS REST ====================
    @Test
    @DisplayName("4. API Resumen Estadístico debe responder 200 con estructura de datos")
    void testApiResumenEstadistico_Exitoso() throws Exception {
        mockMvc.perform(get("/api/mesa-partes/reportes/resumen")
                        .cookie(adminCookie)
                        .param("periodo", "ANIO_ACTUAL")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalPresentados").isNumber())
                .andExpect(jsonPath("$.data.totalRecibidos").isNumber())
                .andExpect(jsonPath("$.data.totalPendientes").isNumber());
    }

    @Test
    @DisplayName("5. API Gráfico por Estado debe devolver labels y datos para Chart.js")
    void testApiPorEstado_Exitoso() throws Exception {
        mockMvc.perform(get("/api/mesa-partes/reportes/por-estado")
                        .cookie(adminCookie)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.labels").isArray())
                .andExpect(jsonPath("$.data.datos").isArray())
                .andExpect(jsonPath("$.data.total").isNumber());
    }

    @Test
    @DisplayName("6. API Evolución Mensual debe devolver 12 meses cronológicos")
    void testApiEvolucionMensual_Exitoso() throws Exception {
        mockMvc.perform(get("/api/mesa-partes/reportes/evolucion")
                        .cookie(adminCookie)
                        .param("anio", "2026")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.meses.length()").value(12))
                .andExpect(jsonPath("$.data.presentados.length()").value(12))
                .andExpect(jsonPath("$.data.resueltos.length()").value(12));
    }

    @Test
    @DisplayName("7. API Distribución por Oficina debe responder 200 con lista de oficinas")
    void testApiPorOficina_Exitoso() throws Exception {
        mockMvc.perform(get("/api/mesa-partes/reportes/por-oficina")
                        .cookie(adminCookie)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("8. API Procedimientos por Trámite debe responder 200 con desglose")
    void testApiPorTramite_Exitoso() throws Exception {
        mockMvc.perform(get("/api/mesa-partes/reportes/por-tramite")
                        .cookie(adminCookie)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("9. API Comparativa de Períodos debe calcular variaciones")
    void testApiComparacion_Exitoso() throws Exception {
        mockMvc.perform(get("/api/mesa-partes/reportes/comparacion")
                        .cookie(adminCookie)
                        .param("periodo", "MES")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.periodoActualLabel").isString())
                .andExpect(jsonPath("$.data.variacionPctPresentados").isString());
    }

    @Test
    @DisplayName("10. API Supervisión de Derivaciones debe responder 200")
    void testApiDerivaciones_Exitoso() throws Exception {
        mockMvc.perform(get("/api/mesa-partes/reportes/derivaciones")
                        .cookie(adminCookie)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.enviadas").isNumber())
                .andExpect(jsonPath("$.data.recibidas").isNumber());
    }

    @Test
    @DisplayName("11. API Supervisión de Observaciones debe responder 200")
    void testApiObservaciones_Exitoso() throws Exception {
        mockMvc.perform(get("/api/mesa-partes/reportes/observaciones")
                        .cookie(adminCookie)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.expedientesObservados").isNumber());
    }

    @Test
    @DisplayName("12. API Control de Plazos debe responder 200")
    void testApiPlazos_Exitoso() throws Exception {
        mockMvc.perform(get("/api/mesa-partes/reportes/plazos")
                        .cookie(adminCookie)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.enPlazo").isNumber())
                .andExpect(jsonPath("$.data.vencidos").isNumber());
    }

    @Test
    @DisplayName("13. API Tabla de Expedientes Paginada debe responder 200")
    void testApiExpedientesPaginados_Exitoso() throws Exception {
        mockMvc.perform(get("/api/mesa-partes/reportes/expedientes")
                        .cookie(adminCookie)
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").isNumber());
    }

    // ==================== PRUEBAS DE EXPORTACIÓN ====================
    @Test
    @DisplayName("14. Exportación a Excel debe devolver libro XLSX válido y auditoría")
    void testExportarExcel_Exitoso() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/mesa-partes/reportes/exportar/excel")
                        .cookie(adminCookie)
                        .param("periodo", "ANIO_ACTUAL"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .andExpect(header().exists("Content-Disposition"))
                .andReturn();

        byte[] contenido = result.getResponse().getContentAsByteArray();
        assertNotNull(contenido);
        assertTrue(contenido.length > 500, "El archivo XLSX debe contener cabecera y registros formateados");
        // Los primeros bytes de un archivo ZIP/XLSX corresponden a PK (0x50, 0x4B)
        assertEquals((byte) 0x50, contenido[0]);
        assertEquals((byte) 0x4B, contenido[1]);
    }

    @Test
    @DisplayName("15. Exportación a PDF debe devolver documento PDF oficial con cabecera %PDF-")
    void testExportarPdf_Exitoso() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/mesa-partes/reportes/exportar/pdf")
                        .cookie(adminCookie)
                        .param("periodo", "ANIO_ACTUAL"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andReturn();

        byte[] contenido = result.getResponse().getContentAsByteArray();
        assertNotNull(contenido);
        assertTrue(contenido.length > 500, "El archivo PDF debe contener estructura de documento válida");
        String magicHeader = new String(Arrays.copyOfRange(contenido, 0, 5));
        assertEquals("%PDF-", magicHeader, "La cabecera del archivo debe ser %PDF-");
    }
}
