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
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.AccionObligacionRequest;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.GeneracionLoteRequest;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.GeneracionObligacionRequest;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.ObligacionDetailResponse;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.CuentaCorrienteTributariaService;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.ObligacionTributariaService;
import pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails;
import pe.gob.municipalidad.sigm.security.jwt.JwtTokenProvider;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
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
class ObligacionesIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObligacionTributariaService obligacionService;

    @Autowired
    private CuentaCorrienteTributariaService cuentaCorrienteService;

    @Autowired
    private ObligacionTributariaRepository obligacionRepository;

    @Autowired
    private CuentaCorrienteTributariaRepository cuentaRepository;

    @Autowired
    private MovimientoCuentaCorrienteTributariaRepository movimientoRepository;

    @Autowired
    private ContribuyenteTributarioRepository contribuyenteRepository;

    @Autowired
    private PredioTributarioRepository predioRepository;

    @Autowired
    private PeriodoTributarioRepository periodoRepository;

    @Autowired
    private ConceptoTributarioRepository conceptoRepository;

    @Autowired
    private CalculoImpuestoPredialRepository calculoPredialRepository;

    @Autowired
    private CalculoArbitrioMunicipalRepository calculoArbitrioRepository;

    private Cookie adminAuthCookie;
    private ContribuyenteTributario testContribuyente;
    private PredioTributario testPredio;
    private PeriodoTributario testPeriodo;
    private CalculoImpuestoPredial testCalculoPredial;
    private CalculoArbitrioMunicipal testCalculoArbitrio;

    @BeforeEach
    void setUp() {
        CustomUserDetails adminDetails = CustomUserDetails.builder()
                .id(106L)
                .username("admin_obligaciones_test")
                .password("dummy")
                .enabled(true)
                .accountNonLocked(true)
                .roles(Set.of("ROLE_ADMIN"))
                .permisos(Set.of(
                        "TRIBUTARIA_OBLIGACIONES_VER",
                        "TRIBUTARIA_OBLIGACIONES_GENERAR",
                        "TRIBUTARIA_OBLIGACIONES_GENERAR_LOTE",
                        "TRIBUTARIA_OBLIGACIONES_REVISAR",
                        "TRIBUTARIA_OBLIGACIONES_VALIDAR",
                        "TRIBUTARIA_OBLIGACIONES_SUSPENDER",
                        "TRIBUTARIA_OBLIGACIONES_RECTIFICAR",
                        "TRIBUTARIA_OBLIGACIONES_ANULAR",
                        "TRIBUTARIA_OBLIGACIONES_VER_HISTORIAL",
                        "TRIBUTARIA_CUENTA_CORRIENTE_VER",
                        "TRIBUTARIA_CUENTA_CORRIENTE_VER_MOVIMIENTOS",
                        "TRIBUTARIA_CUENTA_CORRIENTE_EXPORTAR",
                        "TRIBUTARIA_OBLIGACIONES_EXPORTAR"
                ))
                .authorities(List.of(
                        new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("TRIBUTARIA_OBLIGACIONES_VER"),
                        new SimpleGrantedAuthority("TRIBUTARIA_OBLIGACIONES_GENERAR"),
                        new SimpleGrantedAuthority("TRIBUTARIA_OBLIGACIONES_GENERAR_LOTE"),
                        new SimpleGrantedAuthority("TRIBUTARIA_OBLIGACIONES_VALIDAR"),
                        new SimpleGrantedAuthority("TRIBUTARIA_OBLIGACIONES_SUSPENDER"),
                        new SimpleGrantedAuthority("TRIBUTARIA_OBLIGACIONES_RECTIFICAR"),
                        new SimpleGrantedAuthority("TRIBUTARIA_OBLIGACIONES_ANULAR"),
                        new SimpleGrantedAuthority("TRIBUTARIA_CUENTA_CORRIENTE_VER"),
                        new SimpleGrantedAuthority("TRIBUTARIA_OBLIGACIONES_EXPORTAR")
                ))
                .build();
        String token = jwtTokenProvider.generateAccessToken(adminDetails);
        adminAuthCookie = new Cookie(SecurityConstants.TOKEN_COOKIE_NAME, token);
        adminAuthCookie.setPath("/");

        testPeriodo = periodoRepository.findByAnio(2026)
                .orElseGet(() -> periodoRepository.save(PeriodoTributario.builder()
                        .anio(2026)
                        .fechaInicio(LocalDate.of(2026, 1, 1))
                        .fechaFin(LocalDate.of(2026, 12, 31))
                        .uitVigente(new BigDecimal("5500.00"))
                        .estado(EstadoPeriodoTributario.APERTURADO)
                        .build()));

        testContribuyente = contribuyenteRepository.save(ContribuyenteTributario.builder()
                .codigoContribuyente("CT-OBL-TEST-" + System.currentTimeMillis())
                .tipoContribuyente(TipoContribuyente.PERSONA_NATURAL)
                .tipoDocumento("DNI")
                .numeroDocumento("41829304")
                .nombreRazonSocial("Teodoro Mendoza Quispe")
                .domicilioFiscal("Jr. San Martín 150, Quinua")
                .estado(EstadoContribuyente.ACTIVO)
                .build());

        testPredio = predioRepository.save(PredioTributario.builder()
                .codigoPredio("PR-OBL-TEST-" + System.currentTimeMillis())
                .tipoPredio(TipoPredio.URBANO)
                .clasificacionPredio(ClasificacionPredio.CASA_HABITACION)
                .usoDeclarado(UsoDeclaradoPredio.VIVIENDA)
                .direccion("Jr. San Martín 150, Quinua")
                .areaTerreno(new BigDecimal("220.00"))
                .valorArancelarioReferencial(new BigDecimal("45.00"))
                .valorAutovaluoReferencial(new BigDecimal("48500.00"))
                .estado(EstadoPredio.ACTIVO)
                .build());

        // Cálculo Predial Aprobado
        testCalculoPredial = calculoPredialRepository.save(CalculoImpuestoPredial.builder()
                .codigoCalculo("CALC-PRED-TEST-" + System.currentTimeMillis())
                .contribuyente(testContribuyente)
                .periodo(testPeriodo)
                .tipoCalculo(TipoCalculoPredial.DETERMINACION_INDIVIDUAL)
                .estado(EstadoCalculoPredial.APROBADO)
                .uitAplicada(new BigDecimal("5500.00"))
                .totalAutovaluoBruto(new BigDecimal("48500.00"))
                .baseImponibleNeta(new BigDecimal("48500.00"))
                .impuestoAnualDeterminado(new BigDecimal("380.00"))
                .resolucionAprobacion("Resolución N° 001-2026-MDQ")
                .usuarioCalculo("admin")
                .versionCalculo(1)
                .build());

        // Cálculo Arbitrios Aprobado
        testCalculoArbitrio = calculoArbitrioRepository.save(CalculoArbitrioMunicipal.builder()
                .codigoCalculo("CALC-ARB-TEST-" + System.currentTimeMillis())
                .contribuyente(testContribuyente)
                .predio(testPredio)
                .periodo(testPeriodo)
                .tipoCalculo(TipoCalculoArbitrio.DETERMINACION_INDIVIDUAL)
                .estado(EstadoCalculoArbitrio.APROBADO)
                .importeAnualTotal(new BigDecimal("160.00"))
                .importePeriodoTotal(new BigDecimal("160.00"))
                .resolucionAprobacion("Resolución N° 002-2026-MDQ")
                .usuarioCalculo("admin")
                .versionCalculo(1)
                .build());
    }

    @Test
    @Order(1)
    @DisplayName("1. Bandeja web de obligaciones carga con autenticación y datos reales")
    void testListarObligacionesWebAutenticado() throws Exception {
        mockMvc.perform(get("/dashboard/tributaria/obligaciones")
                        .cookie(adminAuthCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/tributaria/obligaciones/list"))
                .andExpect(model().attributeExists("obligaciones"))
                .andExpect(model().attributeExists("metricas"));
    }

    @Test
    @Order(2)
    @DisplayName("2. Generar obligación tributaria desde determinación del Impuesto Predial y verificar cuotas")
    void testGenerarObligacionDesdeCalculoPredial() {
        GeneracionObligacionRequest req = GeneracionObligacionRequest.builder()
                .tipoDeterminacion(TipoDeterminacionTributaria.IMPUESTO_PREDIAL)
                .determinacionId(testCalculoPredial.getId())
                .cuotas(4)
                .observaciones("Prueba de generación predial")
                .build();

        ObligacionDetailResponse response = obligacionService.generarDesdeDeterminacion(req, "test_admin");

        assertNotNull(response);
        assertNotNull(response.getId());
        assertTrue(response.getCodigoObligacion().startsWith("OBL-PRED-2026-"));
        assertEquals(new BigDecimal("380.00"), response.getImporteDeterminado());
        assertEquals(new BigDecimal("380.00"), response.getSaldo());
        assertEquals(EstadoObligacionTributaria.REGISTRADA, response.getEstado());
        assertEquals(EstadoExigibilidadObligacion.NO_EXIGIBLE, response.getEstadoExigibilidad());
        assertEquals(4, response.getCuotas().size());

        // Suma de cuotas
        BigDecimal suma = response.getCuotas().stream()
                .map(pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.CuotaResponse::getImporte)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(new BigDecimal("380.00"), suma);
    }

    @Test
    @Order(3)
    @DisplayName("3. Verificar que la generación crea la Cuenta Corriente y el movimiento contable CARGO_OBLIGACION")
    void testVerificarCuentaCorrienteYMovimientosContables() {
        GeneracionObligacionRequest req = GeneracionObligacionRequest.builder()
                .tipoDeterminacion(TipoDeterminacionTributaria.IMPUESTO_PREDIAL)
                .determinacionId(testCalculoPredial.getId())
                .cuotas(1)
                .build();

        ObligacionDetailResponse obl = obligacionService.generarDesdeDeterminacion(req, "test_admin");

        CuentaCorrienteTributaria cta = cuentaRepository.findByContribuyenteId(testContribuyente.getId()).orElse(null);
        assertNotNull(cta);
        assertEquals(new BigDecimal("380.00"), cta.getSaldoActual());
        assertEquals(new BigDecimal("380.00"), cta.getTotalCargos());

        List<MovimientoCuentaCorrienteTributaria> movs = movimientoRepository.findByCuentaCorrienteIdOrderByNumeroSecuenciaAsc(cta.getId());
        assertFalse(movs.isEmpty());
        MovimientoCuentaCorrienteTributaria m1 = movs.get(0);
        assertEquals(TipoMovimientoCuentaCorriente.CARGO_OBLIGACION, m1.getTipoMovimiento());
        assertEquals(NaturalezaMovimiento.DEBITO, m1.getNaturaleza());
        assertEquals(new BigDecimal("380.00"), m1.getImporte());
        assertEquals(new BigDecimal("380.00"), m1.getSaldoPosterior());
    }

    @Test
    @Order(4)
    @DisplayName("4. Generar obligación tributaria desde determinación de Arbitrios Municipales")
    void testGenerarObligacionDesdeCalculoArbitrios() {
        GeneracionObligacionRequest req = GeneracionObligacionRequest.builder()
                .tipoDeterminacion(TipoDeterminacionTributaria.ARBITRIOS_MUNICIPALES)
                .determinacionId(testCalculoArbitrio.getId())
                .cuotas(4)
                .build();

        ObligacionDetailResponse response = obligacionService.generarDesdeDeterminacion(req, "test_admin");

        assertNotNull(response);
        assertTrue(response.getCodigoObligacion().startsWith("OBL-ARB-2026-"));
        assertEquals(new BigDecimal("160.00"), response.getImporteDeterminado());
        assertEquals(4, response.getCuotas().size());
        assertEquals(testPredio.getId(), response.getPredioId());
    }

    @Test
    @Order(5)
    @DisplayName("5. Rechazar generación duplicada para una misma determinación activa")
    void testRechazarGeneracionDuplicada() {
        GeneracionObligacionRequest req = GeneracionObligacionRequest.builder()
                .tipoDeterminacion(TipoDeterminacionTributaria.IMPUESTO_PREDIAL)
                .determinacionId(testCalculoPredial.getId())
                .cuotas(1)
                .build();

        obligacionService.generarDesdeDeterminacion(req, "test_admin");

        assertThrows(pe.gob.municipalidad.sigm.common.exception.BusinessException.class, () ->
                obligacionService.generarDesdeDeterminacion(req, "test_admin"));
    }

    @Test
    @Order(6)
    @DisplayName("6. Validar formalmente una obligación tributaria cambiando su estado a VALIDADA y EXIGIBLE")
    void testValidarObligacionHabilitandoExigibilidad() {
        GeneracionObligacionRequest req = GeneracionObligacionRequest.builder()
                .tipoDeterminacion(TipoDeterminacionTributaria.IMPUESTO_PREDIAL)
                .determinacionId(testCalculoPredial.getId())
                .cuotas(1)
                .build();
        ObligacionDetailResponse inicial = obligacionService.generarDesdeDeterminacion(req, "test_admin");

        ObligacionDetailResponse validada = obligacionService.validarObligacion(inicial.getId(), "Resolución Gerencial N° 100", "supervisor");

        assertEquals(EstadoObligacionTributaria.VALIDADA, validada.getEstado());
        assertEquals(EstadoExigibilidadObligacion.EXIGIBLE, validada.getEstadoExigibilidad());
        assertNotNull(validada.getFechaValidacion());
        assertEquals("supervisor", validada.getUsuarioValidador());
    }

    @Test
    @Order(7)
    @DisplayName("7. Suspender exigibilidad de la obligación por procedimiento legal")
    void testSuspenderObligacionConMotivoLegal() {
        GeneracionObligacionRequest req = GeneracionObligacionRequest.builder()
                .tipoDeterminacion(TipoDeterminacionTributaria.IMPUESTO_PREDIAL)
                .determinacionId(testCalculoPredial.getId())
                .cuotas(1)
                .build();
        ObligacionDetailResponse inicial = obligacionService.generarDesdeDeterminacion(req, "test_admin");

        AccionObligacionRequest accion = AccionObligacionRequest.builder()
                .motivo("Reclamo formal presentado ante Mesa de Partes en evaluación")
                .resolucionSustento("Expediente N° 2026-99")
                .build();

        ObligacionDetailResponse suspendida = obligacionService.suspenderObligacion(inicial.getId(), accion, "abogado_rentas");

        assertEquals(EstadoObligacionTributaria.SUSPENDIDA, suspendida.getEstado());
        assertEquals(EstadoExigibilidadObligacion.SUSPENDIDA_LEGALMENTE, suspendida.getEstadoExigibilidad());
    }

    @Test
    @Order(8)
    @DisplayName("8. Anular formalmente obligación revirtiendo el saldo en cuenta corriente")
    void testAnularObligacionRevertiendoSaldo() {
        GeneracionObligacionRequest req = GeneracionObligacionRequest.builder()
                .tipoDeterminacion(TipoDeterminacionTributaria.IMPUESTO_PREDIAL)
                .determinacionId(testCalculoPredial.getId())
                .cuotas(1)
                .build();
        ObligacionDetailResponse inicial = obligacionService.generarDesdeDeterminacion(req, "test_admin");

        AccionObligacionRequest accion = AccionObligacionRequest.builder()
                .motivo("Inafectación legal debidamente acreditada mediante expediente de inspección")
                .resolucionSustento("Resolución de Alcaldía N° 055-2026-MDQ")
                .build();

        ObligacionDetailResponse anulada = obligacionService.anularObligacion(inicial.getId(), accion, "alcalde");

        assertEquals(EstadoObligacionTributaria.ANULADA, anulada.getEstado());
        assertEquals(BigDecimal.ZERO.setScale(2), anulada.getSaldo().setScale(2));

        // Verificar que la cuenta corriente refleja el abono de anulación
        CuentaCorrienteTributaria cta = cuentaRepository.findByContribuyenteId(testContribuyente.getId()).orElseThrow();
        assertEquals(BigDecimal.ZERO.setScale(2), cta.getSaldoActual().setScale(2));
        assertEquals(new BigDecimal("380.00"), cta.getTotalAbonos());
    }

    @Test
    @Order(9)
    @DisplayName("9. Previsualizar y ejecutar lote de generación de obligaciones")
    void testPrevisualizarYGenerarLoteObligaciones() {
        GeneracionLoteRequest loteReq = GeneracionLoteRequest.builder()
                .tipoDeterminacion(TipoDeterminacionTributaria.IMPUESTO_PREDIAL)
                .periodoId(testPeriodo.getId())
                .observaciones("Lote de prueba de integración")
                .build();

        var prev = obligacionService.previsualizarLote(loteReq);
        assertNotNull(prev);
        assertTrue(prev.getTotalElegibles() >= 1);

        var loteResp = obligacionService.generarLote(loteReq, "operador_lotes");
        assertNotNull(loteResp);
        assertTrue(loteResp.getTotalGeneradas() >= 1);
        assertEquals(EstadoLoteGeneracion.COMPLETADO, loteResp.getEstado());
    }

    @Test
    @Order(10)
    @DisplayName("10. Exportaciones a Excel y PDF responden correctamente")
    void testExportarPadronExcelYPdf() throws Exception {
        mockMvc.perform(get("/dashboard/tributaria/obligaciones/exportar/excel")
                        .cookie(adminAuthCookie))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));

        mockMvc.perform(get("/dashboard/tributaria/obligaciones/exportar/pdf")
                        .cookie(adminAuthCookie))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF));

        mockMvc.perform(get("/dashboard/tributaria/cuentas-corrientes/exportar/excel")
                        .cookie(adminAuthCookie))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    }

    @Test
    @Order(11)
    @DisplayName("11. Acceso a endpoints protegidos sin autenticación es denegado o redirige")
    void testAccesoNoAutenticadoRechazado() throws Exception {
        mockMvc.perform(get("/dashboard/tributaria/obligaciones"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(get("/api/v1/tributaria/obligaciones"))
                .andExpect(status().isUnauthorized());
    }
}
