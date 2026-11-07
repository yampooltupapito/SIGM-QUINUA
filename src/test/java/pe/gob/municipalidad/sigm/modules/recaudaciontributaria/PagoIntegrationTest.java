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
import pe.gob.municipalidad.sigm.common.model.Persona;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.PersonaRepository;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.cajapago.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.CajaMunicipalService;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.PagoExportService;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.PagoTributarioService;
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
class PagoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PagoTributarioService pagoService;

    @Autowired
    private CajaMunicipalService cajaService;

    @Autowired
    private PagoExportService exportService;

    @Autowired
    private PagoTributarioRepository pagoRepository;

    @Autowired
    private CajaMunicipalRepository cajaRepository;

    @Autowired
    private SesionCajaMunicipalRepository sesionRepository;

    @Autowired
    private ObligacionTributariaRepository obligacionRepository;

    @Autowired
    private ContribuyenteTributarioRepository contribuyenteRepository;

    @Autowired
    private ConceptoTributarioRepository conceptoRepository;

    @Autowired
    private PeriodoTributarioRepository periodoRepository;

    @Autowired
    private CuentaCorrienteTributariaRepository cuentaRepository;

    @Autowired
    private MovimientoCuentaCorrienteTributariaRepository movimientoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PersonaRepository personaRepository;

    private Cookie authCookie;
    private Usuario testUser;
    private ContribuyenteTributario testContribuyente;
    private ObligacionTributaria testObligacion;
    private CajaMunicipal testCaja;

    @BeforeEach
    void setUp() {
        long suffix = System.currentTimeMillis() % 100000000;
        Persona persona = Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento(String.format("%08d", suffix))
                .nombres("Cajero")
                .apellidoPaterno("Quinua")
                .apellidoMaterno("Oficial")
                .build();
        persona = personaRepository.save(persona);

        testUser = Usuario.builder()
                .username("cajero_test_" + System.currentTimeMillis())
                .passwordHash("$2a$10$abcdefghijklmnopqrstuvwxyz123456")
                .persona(persona)
                .enabled(true)
                .build();
        testUser = usuarioRepository.save(testUser);

        CustomUserDetails userDetails = CustomUserDetails.builder()
                .id(testUser.getId())
                .username(testUser.getUsername())
                .password("dummy")
                .enabled(true)
                .accountNonLocked(true)
                .roles(Set.of("ROLE_ADMIN", "ROLE_CAJERO"))
                .permisos(Set.of(
                        "TRIBUTARIA_PAGOS_VER",
                        "TRIBUTARIA_PAGOS_REGISTRAR",
                        "TRIBUTARIA_PAGOS_CONFIRMAR",
                        "TRIBUTARIA_PAGOS_REVERTIR",
                        "TRIBUTARIA_CAJA_VER",
                        "TRIBUTARIA_CAJA_ADMINISTRAR",
                        "TRIBUTARIA_CAJA_ABRIR",
                        "TRIBUTARIA_CAJA_CERRAR",
                        "TRIBUTARIA_CONCILIACION_VER"
                ))
                .authorities(List.of(
                        new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("ROLE_CAJERO"),
                        new SimpleGrantedAuthority("TRIBUTARIA_PAGOS_VER"),
                        new SimpleGrantedAuthority("TRIBUTARIA_PAGOS_REGISTRAR"),
                        new SimpleGrantedAuthority("TRIBUTARIA_PAGOS_CONFIRMAR"),
                        new SimpleGrantedAuthority("TRIBUTARIA_PAGOS_REVERTIR"),
                        new SimpleGrantedAuthority("TRIBUTARIA_CAJA_VER"),
                        new SimpleGrantedAuthority("TRIBUTARIA_CAJA_ADMINISTRAR"),
                        new SimpleGrantedAuthority("TRIBUTARIA_CAJA_ABRIR"),
                        new SimpleGrantedAuthority("TRIBUTARIA_CAJA_CERRAR"),
                        new SimpleGrantedAuthority("TRIBUTARIA_CONCILIACION_VER")
                ))
                .build();

        String token = jwtTokenProvider.generateAccessToken(userDetails);
        authCookie = new Cookie(SecurityConstants.TOKEN_COOKIE_NAME, token);
        authCookie.setPath("/");

        testContribuyente = ContribuyenteTributario.builder()
                .codigoContribuyente("CT-TEST-" + System.currentTimeMillis())
                .tipoContribuyente(TipoContribuyente.PERSONA_NATURAL)
                .tipoDocumento("DNI")
                .numeroDocumento(String.format("%08d", (suffix + 7777) % 100000000))
                .nombreRazonSocial("Contribuyente Pruebas Quinua")
                .estado(EstadoContribuyente.ACTIVO)
                .build();
        testContribuyente = contribuyenteRepository.save(testContribuyente);

        PeriodoTributario periodo = periodoRepository.findAll().stream().findFirst().orElseGet(() ->
                periodoRepository.save(PeriodoTributario.builder()
                        .anio(2026)
                        .fechaInicio(LocalDate.of(2026, 1, 1))
                        .fechaFin(LocalDate.of(2026, 12, 31))
                        .uitVigente(new BigDecimal("5500.00"))
                        .estado(EstadoPeriodoTributario.APERTURADO)
                        .build())
        );

        ConceptoTributario concepto = conceptoRepository.findAll().stream().findFirst().orElseGet(() ->
                conceptoRepository.save(ConceptoTributario.builder()
                        .codigo("IMP_PREDIAL_TEST")
                        .nombre("Impuesto Predial Test")
                        .tipoConcepto(TipoConceptoTributario.IMPUESTO_PREDIAL)
                        .periodicidad(PeriodicidadConcepto.ANUAL)
                        .aplicaAPredio(true)
                        .estado(EstadoConceptoTributario.VIGENTE)
                        .fechaInicioVigencia(LocalDate.of(2026, 1, 1))
                        .build())
        );

        testObligacion = ObligacionTributaria.builder()
                .codigoObligacion("OBL-TEST-" + System.currentTimeMillis())
                .contribuyente(testContribuyente)
                .concepto(concepto)
                .periodo(periodo)
                .tipoDeterminacion(TipoDeterminacionTributaria.IMPUESTO_PREDIAL)
                .cuotaNumero(1)
                .totalCuotas(1)
                .importeOriginal(new BigDecimal("150.00"))
                .importeVigente(new BigDecimal("150.00"))
                .importeDeterminado(new BigDecimal("150.00"))
                .importePagado(BigDecimal.ZERO)
                .saldo(new BigDecimal("150.00"))
                .estado(EstadoObligacionTributaria.VIGENTE)
                .estadoFinanciero(EstadoFinancieroObligacion.PENDIENTE)
                .estadoExigibilidad(EstadoExigibilidadObligacion.EXIGIBLE)
                .fechaDeterminacion(LocalDate.now())
                .fechaRegistro(OffsetDateTime.now())
                .build();
        testObligacion = obligacionRepository.save(testObligacion);

        testCaja = CajaMunicipal.builder()
                .codigo("CAJA-T-" + System.currentTimeMillis())
                .nombre("Caja de Pruebas")
                .estado(EstadoCajaMunicipal.ACTIVA)
                .cajeroAsignado(testUser)
                .build();
        testCaja = cajaRepository.save(testCaja);
    }

    @Test
    @Order(1)
    @DisplayName("Flujo completo de sesión de caja, cobro en efectivo, cancelación y arqueo cuadrado")
    void testFlujoCompletoCajaYCobro() {
        // 1. Apertura de sesión con fondo de sencillo S/ 50.00
        AbrirSesionCajaRequest abrirReq = AbrirSesionCajaRequest.builder()
                .cajaId(testCaja.getId())
                .montoApertura(new BigDecimal("50.00"))
                .observaciones("Turno mañana pruebas")
                .build();

        SesionCajaResponse sesion = cajaService.abrirSesion(abrirReq, testUser.getUsername());
        assertNotNull(sesion);
        assertEquals(EstadoSesionCaja.ABIERTA, sesion.getEstado());
        assertEquals(new BigDecimal("50.00"), sesion.getMontoApertura());

        // 2. Registrar cobro en efectivo por el total de la obligación (S/ 150.00)
        ItemAplicacionPagoRequest appReq = ItemAplicacionPagoRequest.builder()
                .obligacionId(testObligacion.getId())
                .importeAplicado(new BigDecimal("150.00"))
                .build();

        RegistrarPagoRequest pagoReq = RegistrarPagoRequest.builder()
                .contribuyenteId(testContribuyente.getId())
                .sesionCajaId(sesion.getId())
                .importeTotal(new BigDecimal("150.00"))
                .medioPago(MedioPagoTributario.EFECTIVO_CAJA)
                .idempotencyKey("IDEMP-TEST-001")
                .aplicaciones(List.of(appReq))
                .build();

        PagoResponse pago = pagoService.registrarPago(pagoReq, testUser.getUsername());
        assertNotNull(pago);
        assertEquals(EstadoPagoTributario.CONFIRMADO, pago.getEstado());
        assertEquals(new BigDecimal("150.00"), pago.getImporte());

        // 3. Verificar que la obligación se canceló a 0.00
        ObligacionTributaria oblActualizada = obligacionRepository.findById(testObligacion.getId()).orElseThrow();
        assertEquals(0, BigDecimal.ZERO.compareTo(oblActualizada.getSaldo()));
        assertEquals(EstadoFinancieroObligacion.CANCELADA, oblActualizada.getEstadoFinanciero());

        // 4. Verificar que la sesión de caja acumuló el efectivo recaudado
        SesionCajaMunicipal sesionActualizada = sesionRepository.findById(sesion.getId()).orElseThrow();
        assertEquals(new BigDecimal("150.00"), sesionActualizada.getTotalRecaudadoEfectivo());
        assertEquals(1, sesionActualizada.getTotalOperaciones());

        // 5. Cierre de sesión y arqueo: esperado = 50 + 150 = 200.00. Contado = 200.00 -> Diferencia 0.00
        CerrarSesionCajaRequest cierreReq = CerrarSesionCajaRequest.builder()
                .montoCierreDeclarado(new BigDecimal("200.00"))
                .observaciones("Cierre conforme sin diferencias")
                .build();

        SesionCajaResponse sesionCerrada = cajaService.cerrarSesion(sesion.getId(), cierreReq, testUser.getUsername());
        assertNotNull(sesionCerrada);
        assertEquals(EstadoSesionCaja.CERRADA, sesionCerrada.getEstado());
        assertEquals(0, BigDecimal.ZERO.compareTo(sesionCerrada.getDiferencia()));
    }

    @Test
    @Order(2)
    @DisplayName("Reversión de pago confirmada genera contraasiento y restaura deuda")
    void testReversionPagoRestauraDeuda() {
        // Registrar cobro por transferencia
        ItemAplicacionPagoRequest appReq = ItemAplicacionPagoRequest.builder()
                .obligacionId(testObligacion.getId())
                .importeAplicado(new BigDecimal("150.00"))
                .build();

        RegistrarPagoRequest pagoReq = RegistrarPagoRequest.builder()
                .contribuyenteId(testContribuyente.getId())
                .importeTotal(new BigDecimal("150.00"))
                .medioPago(MedioPagoTributario.TRANSFERENCIA_BANCARIA)
                .numeroOperacionBancaria("BN-4455")
                .idempotencyKey("IDEMP-REV-001")
                .aplicaciones(List.of(appReq))
                .build();

        PagoResponse pago = pagoService.registrarPago(pagoReq, testUser.getUsername());
        pagoService.confirmarPago(pago.getId(), new ConfirmarPagoRequest("Voucher validado"), testUser.getUsername());

        // Revertir pago
        RevertirPagoRequest revReq = RevertirPagoRequest.builder()
                .motivoReversion("Voucher duplicado presentado por error")
                .build();

        PagoResponse pagoRevertido = pagoService.revertirPago(pago.getId(), revReq, testUser.getUsername());
        assertEquals(EstadoPagoTributario.REVERTIDO, pagoRevertido.getEstado());

        // Comprobar que el saldo de la obligación volvió a 150.00 y estado PENDIENTE
        ObligacionTributaria oblRestaurada = obligacionRepository.findById(testObligacion.getId()).orElseThrow();
        assertEquals(new BigDecimal("150.00"), oblRestaurada.getSaldo());
        assertEquals(EstadoFinancieroObligacion.PENDIENTE, oblRestaurada.getEstadoFinanciero());

        // Comprobar que en la cuenta corriente existe el movimiento de REVERSO_APLICACION_PAGO
        CuentaCorrienteTributaria cta = cuentaRepository.findByContribuyenteId(testContribuyente.getId()).orElseThrow();
        List<MovimientoCuentaCorrienteTributaria> movs = movimientoRepository.findByCuentaCorrienteIdOrderByNumeroSecuenciaAsc(cta.getId());
        boolean tieneReverso = movs.stream().anyMatch(m -> m.getTipoMovimiento() == TipoMovimientoCuentaCorriente.REVERSO_APLICACION_PAGO);
        assertTrue(tieneReverso);
    }

    @Test
    @Order(3)
    @DisplayName("Endpoint MVC /dashboard/tributaria/pagos responde 200 OK")
    void testMvcListarPagos() throws Exception {
        mockMvc.perform(get("/dashboard/tributaria/pagos")
                        .cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/tributaria/pagos/list"))
                .andExpect(model().attributeExists("pagos", "dashboard"));
    }

    @Test
    @Order(4)
    @DisplayName("Endpoint MVC /dashboard/tributaria/cajas responde 200 OK")
    void testMvcListarCajas() throws Exception {
        mockMvc.perform(get("/dashboard/tributaria/cajas")
                        .cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/tributaria/cajas/list"))
                .andExpect(model().attributeExists("cajas", "dashboard"));
    }

    @Test
    @Order(5)
    @DisplayName("Endpoint MVC /dashboard/tributaria/conciliaciones responde 200 OK")
    void testMvcListarConciliaciones() throws Exception {
        mockMvc.perform(get("/dashboard/tributaria/conciliaciones")
                        .cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/tributaria/conciliaciones/list"))
                .andExpect(model().attributeExists("conciliaciones"));
    }

    @Test
    @Order(6)
    @DisplayName("Exportaciones de pagos a Excel y PDF generan bytes válidos")
    void testExportacionesPagos() {
        byte[] excel = exportService.exportarPagosExcel(null, null, null, null, null, null, null);
        assertNotNull(excel);
        assertTrue(excel.length > 100);

        byte[] pdf = exportService.exportarPagosPdf(null, null, null, null, null, null, null);
        assertNotNull(pdf);
        assertTrue(pdf.length > 100);
    }
}
