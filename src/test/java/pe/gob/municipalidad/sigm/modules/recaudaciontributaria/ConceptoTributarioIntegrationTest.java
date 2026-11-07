package pe.gob.municipalidad.sigm.modules.recaudaciontributaria;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.constants.SecurityConstants;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.ConceptoExportService;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.ConceptoTributarioService;
import pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails;
import pe.gob.municipalidad.sigm.security.jwt.JwtTokenProvider;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ConceptoTributarioIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ConceptoTributarioService conceptoService;

    @Autowired
    private ConceptoExportService exportService;

    private Cookie adminAuthCookie;
    private UUID testPeriodoId;

    @BeforeEach
    void setUp() {
        CustomUserDetails adminDetails = CustomUserDetails.builder()
                .id(103L)
                .username("admin.conceptos")
                .password("dummy")
                .enabled(true)
                .accountNonLocked(true)
                .roles(Set.of("ROLE_ADMIN"))
                .permisos(Set.of(
                        "TRIBUTARIA_CONCEPTOS_VER",
                        "TRIBUTARIA_CONCEPTOS_CREAR",
                        "TRIBUTARIA_CONCEPTOS_EDITAR",
                        "TRIBUTARIA_CONCEPTOS_CONFIGURAR",
                        "TRIBUTARIA_CONCEPTOS_REVISAR",
                        "TRIBUTARIA_CONCEPTOS_APROBAR",
                        "TRIBUTARIA_CONCEPTOS_ACTIVAR",
                        "TRIBUTARIA_CONCEPTOS_DESACTIVAR",
                        "TRIBUTARIA_CONCEPTOS_EXPORTAR",
                        "TRIBUTARIA_CONCEPTOS_VER_HISTORIAL",
                        "TRIBUTARIA_CONCEPTOS_GESTIONAR_NORMAS"
                ))
                .authorities(List.of(
                        new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("TRIBUTARIA_CONCEPTOS_VER"),
                        new SimpleGrantedAuthority("TRIBUTARIA_CONCEPTOS_CREAR"),
                        new SimpleGrantedAuthority("TRIBUTARIA_CONCEPTOS_EDITAR"),
                        new SimpleGrantedAuthority("TRIBUTARIA_CONCEPTOS_CONFIGURAR"),
                        new SimpleGrantedAuthority("TRIBUTARIA_CONCEPTOS_REVISAR"),
                        new SimpleGrantedAuthority("TRIBUTARIA_CONCEPTOS_APROBAR"),
                        new SimpleGrantedAuthority("TRIBUTARIA_CONCEPTOS_ACTIVAR"),
                        new SimpleGrantedAuthority("TRIBUTARIA_CONCEPTOS_DESACTIVAR"),
                        new SimpleGrantedAuthority("TRIBUTARIA_CONCEPTOS_EXPORTAR"),
                        new SimpleGrantedAuthority("TRIBUTARIA_CONCEPTOS_VER_HISTORIAL"),
                        new SimpleGrantedAuthority("TRIBUTARIA_CONCEPTOS_GESTIONAR_NORMAS")
                ))
                .build();

        String token = jwtTokenProvider.generateAccessToken(adminDetails);
        adminAuthCookie = new Cookie(SecurityConstants.TOKEN_COOKIE_NAME, token);
        adminAuthCookie.setPath("/");
        adminAuthCookie.setHttpOnly(true);

        List<PeriodoSummaryDto> periodos = conceptoService.listarPeriodos();
        assertFalse(periodos.isEmpty(), "Debe existir al menos un período fiscal (2026)");
        testPeriodoId = periodos.get(0).getId();
    }

    @Test
    @Order(1)
    @DisplayName("1. Debe registrar un nuevo concepto tributario y consultar su ficha")
    void testRegistrarYConsultarConcepto() {
        ConceptoCreateRequest request = ConceptoCreateRequest.builder()
                .codigo("TASA_TRAMITE_QUINUA")
                .nombre("Tasa por Trámite Administrativo")
                .descripcion("Grava expedición de constancias y certificaciones.")
                .tipoConcepto(TipoConceptoTributario.TASA_ADMINISTRATIVA)
                .naturalezaTributaria(NaturalezaTributaria.TASA_DERECHO)
                .periodicidad(PeriodicidadConcepto.UNICO)
                .aplicaAPredio(false)
                .cuentaPresupuestal("1.3.2.1.1.1")
                .fechaInicioVigencia(LocalDate.of(2026, 1, 1))
                .build();

        ConceptoDetailResponse response = conceptoService.registrarConcepto(request, "admin.conceptos");

        assertNotNull(response.getId());
        assertEquals("TASA_TRAMITE_QUINUA", response.getCodigo());
        assertEquals(EstadoConceptoTributario.ACTIVO, response.getEstado());

        ConceptoDetailResponse buscado = conceptoService.obtenerConceptoPorId(response.getId());
        assertEquals("Tasa por Trámite Administrativo", buscado.getNombre());
    }

    @Test
    @Order(2)
    @DisplayName("2. Debe gestionar ciclo de vida completo de configuración: Borrador -> Revisión -> Aprobado -> Activo")
    void testCicloDeVidaConfiguracion() {
        // 1. Obtener concepto de prueba o crear uno
        ConceptoDetailResponse concepto = conceptoService.listarConceptos(new ConceptoFilterDto(), org.springframework.data.domain.Pageable.unpaged())
                .getContent().stream()
                .filter(c -> "IMP_PREDIAL".equals(c.getCodigo()))
                .findFirst()
                .map(c -> conceptoService.obtenerConceptoPorId(c.getId()))
                .orElseGet(() -> {
                    ConceptoCreateRequest r = ConceptoCreateRequest.builder()
                            .codigo("IMP_TEST_CICLO")
                            .nombre("Impuesto Ciclo")
                            .tipoConcepto(TipoConceptoTributario.IMPUESTO_PREDIAL)
                            .naturalezaTributaria(NaturalezaTributaria.IMPUESTO)
                            .periodicidad(PeriodicidadConcepto.ANUAL)
                            .aplicaAPredio(true)
                            .fechaInicioVigencia(LocalDate.of(2026, 1, 1))
                            .build();
                    return conceptoService.registrarConcepto(r, "admin.conceptos");
                });

        // 2. Crear configuración en Borrador
        ConfiguracionCreateRequest cfgReq = ConfiguracionCreateRequest.builder()
                .conceptoId(concepto.getId())
                .periodoId(testPeriodoId)
                .fechaInicioVigencia(LocalDate.of(2026, 1, 1))
                .descripcionMotivo("Configuración anual inicial para ejercicio 2026")
                .build();

        ConfiguracionDetailResponse cfg = conceptoService.crearConfiguracion(cfgReq, "admin.conceptos");
        assertEquals(EstadoConfiguracionConcepto.BORRADOR, cfg.getEstado());
        assertTrue(cfg.isEditable());

        // 3. Agregar parámetros al borrador
        ParametroCreateRequest p1 = ParametroCreateRequest.builder()
                .codigo("ALICUOTA_1")
                .nombre("Alícuota Tramo Inicial")
                .tipoDato(TipoDatoParametro.DECIMAL)
                .unidad(UnidadMedidaParametro.PORCENTAJE)
                .valorDecimal(new BigDecimal("0.20"))
                .origenNormativo("Art. 13 LTM")
                .esObligatorio(true)
                .build();

        ConfiguracionDetailResponse cfgConParam = conceptoService.agregarParametro(cfg.getId(), p1, "admin.conceptos");
        assertEquals(1, cfgConParam.getParametros().size());

        // 4. Enviar a Revisión
        ConfiguracionDetailResponse cfgEnRevision = conceptoService.enviarARevision(cfg.getId(), "Revisión técnica conforme", "admin.conceptos");
        assertEquals(EstadoConfiguracionConcepto.EN_REVISION, cfgEnRevision.getEstado());

        // 5. Aprobar
        ConfiguracionDetailResponse cfgAprobada = conceptoService.aprobarConfiguracion(
                cfg.getId(),
                "Ordenanza Municipal N° 005-2026-MDQ",
                "Aprobado por el Concejo Municipal de Quinua",
                "admin.conceptos"
        );
        assertEquals(EstadoConfiguracionConcepto.APROBADO, cfgAprobada.getEstado());

        // 6. Activar
        ConfiguracionDetailResponse cfgActiva = conceptoService.activarConfiguracion(
                cfg.getId(),
                "Activación oficial para cálculo y emisión",
                "admin.conceptos"
        );
        assertEquals(EstadoConfiguracionConcepto.ACTIVO, cfgActiva.getEstado());
        assertFalse(cfgActiva.isEditable());

        // 7. Clonar a nueva versión borrador
        ConfiguracionDetailResponse vNueva = conceptoService.crearNuevaVersion(
                cfg.getId(),
                "Proyecto de ajuste tarifario",
                "admin.conceptos"
        );
        assertEquals(EstadoConfiguracionConcepto.BORRADOR, vNueva.getEstado());
        assertEquals(cfg.getNumeroVersion() + 1, vNueva.getNumeroVersion());
        assertEquals(1, vNueva.getParametros().size(), "Debe haber heredado los parámetros de la versión previa");

        // 8. Comparar ambas versiones
        ComparacionVersionesResponse comp = conceptoService.compararVersiones(cfg.getId(), vNueva.getId());
        assertNotNull(comp);
        assertEquals(cfg.getNumeroVersion(), comp.getNumeroVersionBase());
        assertEquals(vNueva.getNumeroVersion(), comp.getNumeroVersionComparada());
        assertFalse(comp.getParametros().isEmpty());
    }

    @Test
    @Order(3)
    @DisplayName("3. Debe programar cuotas en el calendario de vencimientos")
    void testCalendarioVencimientos() {
        ConceptoSummaryDto summary = conceptoService.listarConceptos(new ConceptoFilterDto(), org.springframework.data.domain.Pageable.unpaged())
                .getContent().get(0);
        ConceptoDetailResponse concepto = conceptoService.obtenerConceptoPorId(summary.getId());

        CalendarioCuotaRequest c1 = CalendarioCuotaRequest.builder()
                .numeroCuota(1)
                .descripcionCuota("Primera Cuota Trimestral (Al Contado)")
                .fechaVencimiento(LocalDate.of(2026, 2, 28))
                .permiteDescuento(true)
                .porcentajeDescuento(new BigDecimal("10.00"))
                .build();

        CalendarioCuotaResponse resp = conceptoService.agregarCuotaCalendario(
                concepto.getId(),
                testPeriodoId,
                c1,
                "admin.conceptos"
        );

        assertNotNull(resp.getId());
        assertEquals(1, resp.getNumeroCuota());
        assertEquals(new BigDecimal("10.00"), resp.getPorcentajeDescuento());

        List<CalendarioCuotaResponse> lista = conceptoService.listarCalendarioPorConceptoYPeriodo(concepto.getId(), testPeriodoId);
        assertFalse(lista.isEmpty());
    }

    @Test
    @Order(4)
    @DisplayName("4. Debe generar exportaciones en Excel y PDF")
    void testExportaciones() {
        byte[] excel = exportService.exportarExcel(new ConceptoFilterDto());
        assertNotNull(excel);
        assertTrue(excel.length > 0, "El archivo Excel generado no debe estar vacío");

        byte[] pdf = exportService.exportarPdf(new ConceptoFilterDto());
        assertNotNull(pdf);
        assertTrue(pdf.length > 0, "El archivo PDF generado no debe estar vacío");
    }

    @Test
    @Order(5)
    @DisplayName("5. Debe responder correctamente a través de endpoints MVC y REST")
    void testEndpointsWebYRest() throws Exception {
        // MVC Catálogo
        mockMvc.perform(get("/dashboard/tributaria/conceptos")
                        .cookie(adminAuthCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/tributaria/conceptos/list"))
                .andExpect(model().attributeExists("conceptos", "stats", "filtro"));

        // MVC Formulario Nuevo
        mockMvc.perform(get("/dashboard/tributaria/conceptos/nuevo")
                        .cookie(adminAuthCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/tributaria/conceptos/form"))
                .andExpect(model().attributeExists("form", "tiposConcepto", "naturalezas"));

        // REST GET List
        mockMvc.perform(get("/api/v1/tributaria/conceptos")
                        .cookie(adminAuthCookie)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        // REST GET Estadísticas
        mockMvc.perform(get("/api/v1/tributaria/conceptos/estadisticas")
                        .cookie(adminAuthCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalConceptos").isNumber());
    }
}
