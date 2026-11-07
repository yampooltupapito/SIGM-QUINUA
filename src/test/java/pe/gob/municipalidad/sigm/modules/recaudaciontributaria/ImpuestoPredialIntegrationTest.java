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
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request.CalculoPredialCreateRequest;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request.CalculoPredialFilterDto;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request.SimulacionPredialRequest;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response.CalculoPredialDetailResponse;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.CalculoPredialService;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.ImpuestoPredialExportService;
import pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails;
import pe.gob.municipalidad.sigm.security.jwt.JwtTokenProvider;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
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
class ImpuestoPredialIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private CalculoPredialService calculoService;

    @Autowired
    private ImpuestoPredialExportService exportService;

    @Autowired
    private ContribuyenteTributarioRepository contribuyenteRepository;

    @Autowired
    private PredioTributarioRepository predioRepository;

    @Autowired
    private RelacionContribuyentePredioRepository relacionRepository;

    @Autowired
    private PeriodoTributarioRepository periodoTributarioRepository;

    @Autowired
    private CalculoImpuestoPredialRepository calculoRepository;

    private Cookie adminAuthCookie;
    private ContribuyenteTributario testContribuyente;
    private PredioTributario testPredio;
    private PeriodoTributario testPeriodo;

    @BeforeEach
    void setUp() {
        CustomUserDetails adminDetails = CustomUserDetails.builder()
                .id(104L)
                .username("admin_predial_test")
                .password("dummy")
                .enabled(true)
                .accountNonLocked(true)
                .roles(Set.of("ROLE_ADMIN"))
                .permisos(Set.of(
                        "TRIBUTARIA_PREDIAL_VER",
                        "TRIBUTARIA_PREDIAL_SIMULAR",
                        "TRIBUTARIA_PREDIAL_CALCULAR",
                        "TRIBUTARIA_PREDIAL_REVISAR",
                        "TRIBUTARIA_PREDIAL_APROBAR",
                        "TRIBUTARIA_PREDIAL_ANULAR",
                        "TRIBUTARIA_PREDIAL_EXPORTAR"
                ))
                .authorities(List.of(
                        new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("TRIBUTARIA_PREDIAL_VER"),
                        new SimpleGrantedAuthority("TRIBUTARIA_PREDIAL_SIMULAR"),
                        new SimpleGrantedAuthority("TRIBUTARIA_PREDIAL_CALCULAR"),
                        new SimpleGrantedAuthority("TRIBUTARIA_PREDIAL_REVISAR"),
                        new SimpleGrantedAuthority("TRIBUTARIA_PREDIAL_APROBAR"),
                        new SimpleGrantedAuthority("TRIBUTARIA_PREDIAL_ANULAR"),
                        new SimpleGrantedAuthority("TRIBUTARIA_PREDIAL_EXPORTAR")
                ))
                .build();
        String token = jwtTokenProvider.generateAccessToken(adminDetails);
        adminAuthCookie = new Cookie(SecurityConstants.TOKEN_COOKIE_NAME, token);
        adminAuthCookie.setPath("/");

        // 1. Setup Contribuyente
        testContribuyente = contribuyenteRepository.save(ContribuyenteTributario.builder()
                .codigoContribuyente("CONT-TEST-001")
                .tipoContribuyente(TipoContribuyente.PERSONA_NATURAL)
                .tipoDocumento("DNI")
                .numeroDocumento("44556677")
                .nombreRazonSocial("CARLOS QUISPE MENDOZA")
                .nombres("Carlos")
                .primerApellido("Quispe")
                .segundoApellido("Mendoza")
                .estado(EstadoContribuyente.ACTIVO)
                .build());

        // 2. Setup Predio
        testPredio = predioRepository.save(PredioTributario.builder()
                .codigoPredio("PRED-TEST-001")
                .tipoPredio(TipoPredio.URBANO)
                .clasificacionPredio(ClasificacionPredio.CASA_HABITACION)
                .usoDeclarado(UsoDeclaradoPredio.VIVIENDA)
                .departamento("Ayacucho")
                .provincia("Huamanga")
                .distrito("Quinua")
                .ubigeo("050108")
                .direccion("Jr. Sucre 200, Quinua")
                .areaTerreno(new BigDecimal("180.00"))
                .areaTerrenoM2(new BigDecimal("180.00"))
                .areaConstruida(new BigDecimal("100.00"))
                .valorArancelarioReferencial(new BigDecimal("120.00"))
                .valorAutovaluoReferencial(new BigDecimal("71600.00"))
                .estado(EstadoPredio.ACTIVO)
                .build());

        // 3. Setup Relacion Contribuyente - Predio
        relacionRepository.save(RelacionContribuyentePredio.builder()
                .contribuyente(testContribuyente)
                .predio(testPredio)
                .tipoRelacion(TipoRelacionContribuyentePredio.PROPIETARIO_UNICO)
                .porcentajeParticipacion(new BigDecimal("100.00"))
                .fechaInicio(LocalDate.of(2020, 1, 1))
                .estado(EstadoRelacionContribuyentePredio.ACTIVO)
                .build());

        // 4. Setup Periodo Fiscal 2026
        testPeriodo = periodoTributarioRepository.findByAnio(2026)
                .orElseGet(() -> periodoTributarioRepository.save(PeriodoTributario.builder()
                        .anio(2026)
                        .fechaInicio(LocalDate.of(2026, 1, 1))
                        .fechaFin(LocalDate.of(2026, 12, 31))
                        .uitVigente(new BigDecimal("5150.00"))
                        .estado(EstadoPeriodoTributario.APERTURADO)
                        .resolucionAprobacion("DECRETO-SUPREMO-MEF-2026")
                        .build()));
    }

    @Test
    @Order(1)
    @DisplayName("Simulación Predial: cálculo exacto sin persistir en base de datos")
    void testSimulacionPredial() {
        SimulacionPredialRequest req = SimulacionPredialRequest.builder()
                .contribuyenteId(testContribuyente.getId())
                .periodoId(testPeriodo.getId())
                .fechaReferencia(LocalDate.of(2026, 1, 1))
                .beneficio(TipoBeneficioPredial.NINGUNO)
                .build();

        CalculoPredialDetailResponse res = calculoService.simularCalculo(req, "admin_predial_test");

        assertNotNull(res);
        assertEquals(EstadoCalculoPredial.SIMULACION, res.getEstado());
        assertEquals(testPeriodo.getUitVigente(), res.getUitAplicada());
        // Base imponible calculada técnica: 21,600.00 terreno + 19,800.00 edificación = 41,400.00
        assertEquals(new BigDecimal("41400.00"), res.getTotalAutovaluoBruto());
        assertEquals(new BigDecimal("41400.00"), res.getBaseImponibleNeta());
        // Impuesto = 41,400 * 0.002 = 82.80
        assertEquals(new BigDecimal("82.80"), res.getImpuestoAnualDeterminado());
        assertFalse(res.isAplicaImpuestoMinimo());
        assertEquals(1, res.getPredios().size());
        assertEquals(3, res.getTramos().size());
    }

    @Test
    @Order(2)
    @DisplayName("Ciclo de Vida Completo: PRELIMINAR -> EN_REVISION -> OBSERVADO -> EN_REVISION -> APROBADO")
    void testCicloDeVidaCalculoPredial() {
        // 1. Guardar Cálculo Preliminar
        CalculoPredialCreateRequest createReq = CalculoPredialCreateRequest.builder()
                .contribuyenteId(testContribuyente.getId())
                .periodoId(testPeriodo.getId())
                .fechaReferencia(LocalDate.of(2026, 1, 1))
                .beneficio(TipoBeneficioPredial.NINGUNO)
                .observaciones("Cálculo preliminar para liquidación tributaria anual")
                .build();

        CalculoPredialDetailResponse preliminar = calculoService.registrarCalculo(createReq, "admin_predial_test");
        assertNotNull(preliminar.getId());
        assertEquals(EstadoCalculoPredial.PRELIMINAR, preliminar.getEstado());
        assertTrue(preliminar.getCodigoCalculo().contains("PRED-2026-"));

        UUID calculoId = preliminar.getId();

        // 2. Enviar a Revisión
        CalculoPredialDetailResponse enRevision = calculoService.enviarARevision(calculoId, "Remisión a supervisor", "admin_predial_test");
        assertEquals(EstadoCalculoPredial.EN_REVISION, enRevision.getEstado());

        // 3. Observar
        CalculoPredialDetailResponse observado = calculoService.registrarObservaciones(calculoId, "Requiere cotejo de declaración jurada", "supervisor_test");
        assertEquals(EstadoCalculoPredial.OBSERVADO, observado.getEstado());

        // 4. Volver a Enviar a Revisión
        calculoService.enviarARevision(calculoId, "Subsanación de observaciones", "admin_predial_test");

        // 5. Aprobar formalmente
        CalculoPredialDetailResponse aprobado = calculoService.aprobarCalculo(calculoId, "RESOLUCION-SUBGERENCIA-001-2026", "Conforme a ley", "subgerente_test");
        assertEquals(EstadoCalculoPredial.APROBADO, aprobado.getEstado());
        assertEquals("RESOLUCION-SUBGERENCIA-001-2026", aprobado.getResolucionAprobacion());

        // 6. Recalcular (Nueva versión v2)
        CalculoPredialDetailResponse v2 = calculoService.recalcularNuevaVersion(calculoId, "Ajuste de áreas catastrales", "admin_predial_test");
        assertNotEquals(calculoId, v2.getId());
        assertEquals(2, v2.getVersionCalculo());
        assertEquals(EstadoCalculoPredial.PRELIMINAR, v2.getEstado());
    }

    @Test
    @Order(3)
    @DisplayName("Exportaciones institucionales: Excel y PDF de determinaciones prediales")
    void testExportacionesExcelYPdf() {
        // Crear un registro para exportar
        CalculoPredialCreateRequest createReq = CalculoPredialCreateRequest.builder()
                .contribuyenteId(testContribuyente.getId())
                .periodoId(testPeriodo.getId())
                .fechaReferencia(LocalDate.of(2026, 1, 1))
                .beneficio(TipoBeneficioPredial.NINGUNO)
                .build();
        CalculoPredialDetailResponse c = calculoService.registrarCalculo(createReq, "admin_predial_test");

        // Excel
        byte[] excel = exportService.exportarExcel(new CalculoPredialFilterDto());
        assertNotNull(excel);
        assertTrue(excel.length > 500);

        // PDF Listado
        byte[] pdf = exportService.exportarPdf(new CalculoPredialFilterDto());
        assertNotNull(pdf);
        assertTrue(pdf.length > 500);

        // PDF Hoja de Liquidación individual (HR/PU)
        byte[] hojaPdf = exportService.exportarHojaLiquidacionPdf(c.getId());
        assertNotNull(hojaPdf);
        assertTrue(hojaPdf.length > 500);
    }

    @Test
    @Order(4)
    @DisplayName("Endpoints Web y MVC: Navegación de rutas de Impuesto Predial")
    void testControladoresWeb() throws Exception {
        // 1. Listado principal
        mockMvc.perform(get("/dashboard/tributaria/impuesto-predial")
                        .cookie(adminAuthCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/tributaria/predial/list"))
                .andExpect(model().attributeExists("calculos", "stats"));

        // 2. Formulario de simulación
        mockMvc.perform(get("/dashboard/tributaria/impuesto-predial/simular")
                        .cookie(adminAuthCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/tributaria/predial/simular"))
                .andExpect(model().attributeExists("contribuyentes", "periodos", "beneficios"));

        // 3. Simulación POST
        mockMvc.perform(post("/dashboard/tributaria/impuesto-predial/simular")
                        .cookie(adminAuthCookie)
                        .with(csrf())
                        .param("contribuyenteId", testContribuyente.getId().toString())
                        .param("periodoId", testPeriodo.getId().toString())
                        .param("fechaReferencia", "2026-01-01")
                        .param("beneficio", "NINGUNO"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/tributaria/predial/simular"))
                .andExpect(model().attributeExists("resultado", "simulacionExitosa"));
    }
}
