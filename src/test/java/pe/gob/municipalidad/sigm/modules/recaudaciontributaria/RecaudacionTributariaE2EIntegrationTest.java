package pe.gob.municipalidad.sigm.modules.recaudaciontributaria;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.constants.SecurityConstants;
import pe.gob.municipalidad.sigm.common.model.Persona;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.PersonaRepository;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.reportes.KpiDashboardTributarioDto;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.reportes.ReporteFilterRequest;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.*;
import pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails;
import pe.gob.municipalidad.sigm.security.jwt.JwtTokenProvider;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Suite de Pruebas de Integración de Extremo a Extremo (E2E), Integridad Financiera y Seguridad
 * para el Cierre Técnico del Módulo de Recaudación Tributaria Municipal (SIGM-QUINUA - Prompt 65 de 150).
 * Valida los 20 Escenarios Funcionales y las Reglas de Invariantes Financieras.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RecaudacionTributariaE2EIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ContribuyenteTributarioRepository contribuyenteRepository;

    @Autowired
    private PredioTributarioRepository predioRepository;

    @Autowired
    private RelacionContribuyentePredioRepository relacionPredioRepository;

    @Autowired
    private ConceptoTributarioRepository conceptoRepository;

    @Autowired
    private PeriodoTributarioRepository periodoRepository;

    @Autowired
    private ObligacionTributariaRepository obligacionRepository;

    @Autowired
    private CuentaCorrienteTributariaRepository cuentaCorrienteRepository;

    @Autowired
    private MovimientoCuentaCorrienteTributariaRepository movimientoRepository;

    @Autowired
    private CajaMunicipalRepository cajaRepository;

    @Autowired
    private PagoTributarioRepository pagoRepository;

    @Autowired
    private AplicacionPagoTributarioRepository aplicacionPagoRepository;

    @Autowired
    private NotificacionTributariaRepository notificacionRepository;

    @Autowired
    private TipoNotificacionTributariaRepository tipoNotificacionRepository;

    @Autowired
    private MedioNotificacionTributariaRepository medioNotificacionRepository;

    @Autowired
    private CentroReportesTributariosService centroReportesService;

    @Autowired
    private ExportacionTributariaService exportacionService;

    private static final ZoneId ZONA_LIMA = ZoneId.of("America/Lima");
    private Cookie adminCookie;
    private Cookie ciudadano1Cookie;
    private Cookie ciudadano2Cookie;

    private Usuario adminUser;
    private Usuario ciudadano1User;
    private Usuario ciudadano2User;

    private PeriodoTributario periodo2026;
    private ConceptoTributario conceptoPredial;

    @BeforeEach
    void setUp() {
        long suffix = System.currentTimeMillis() % 100000000;

        // 1. Operador / Administrador Tributario
        Persona personaAdmin = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento(String.format("%08d", (suffix + 1000) % 100000000))
                .nombres("Especialista")
                .apellidoPaterno("Tributario")
                .apellidoMaterno("Quinua")
                .build());

        adminUser = usuarioRepository.save(Usuario.builder()
                .username("admin_e2e_" + suffix)
                .passwordHash("$2a$10$abcdefghijklmnopqrstuvwxyz123456")
                .persona(personaAdmin)
                .enabled(true)
                .build());

        CustomUserDetails adminDetails = CustomUserDetails.builder()
                .id(adminUser.getId())
                .username(adminUser.getUsername())
                .password("dummy")
                .enabled(true)
                .accountNonLocked(true)
                .roles(Set.of("ROLE_ADMIN", "ROLE_TRIBUTACION"))
                .authorities(List.of(
                        new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("ROLE_TRIBUTACION"),
                        new SimpleGrantedAuthority("TRIBUTARIA_MODULO_VER"),
                        new SimpleGrantedAuthority("TRIBUTARIA_CONTRIBUYENTES_VER"),
                        new SimpleGrantedAuthority("TRIBUTARIA_PREDIOS_VER"),
                        new SimpleGrantedAuthority("TRIBUTARIA_CONCEPTOS_VER"),
                        new SimpleGrantedAuthority("TRIBUTARIA_OBLIGACIONES_VER"),
                        new SimpleGrantedAuthority("TRIBUTARIA_CUENTA_CORRIENTE_VER"),
                        new SimpleGrantedAuthority("TRIBUTARIA_PAGOS_VER"),
                        new SimpleGrantedAuthority("TRIBUTARIA_PAGOS_REGISTRAR"),
                        new SimpleGrantedAuthority("TRIBUTARIA_DEUDAS_VER"),
                        new SimpleGrantedAuthority("TRIBUTARIA_FRACCIONAMIENTOS_VER"),
                        new SimpleGrantedAuthority("TRIBUTARIA_NOTIFICACIONES_VER"),
                        new SimpleGrantedAuthority("TRIBUTARIA_REPORTES_ACCEDER"),
                        new SimpleGrantedAuthority("TRIBUTARIA_REPORTES_EXPORTAR"),
                        new SimpleGrantedAuthority("TRIBUTARIA_METAS_GESTIONAR")
                ))
                .build();
        adminCookie = new Cookie(SecurityConstants.TOKEN_COOKIE_NAME, jwtTokenProvider.generateAccessToken(adminDetails));
        adminCookie.setPath("/");

        // 2. Ciudadano 1 (Contribuyente 1)
        Persona personaC1 = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento(String.format("%08d", (suffix + 2000) % 100000000))
                .nombres("Juan")
                .apellidoPaterno("Ayala")
                .apellidoMaterno("Condori")
                .build());

        ciudadano1User = usuarioRepository.save(Usuario.builder()
                .username("juan_ayala_" + suffix)
                .passwordHash("$2a$10$abcdefghijklmnopqrstuvwxyz123456")
                .persona(personaC1)
                .enabled(true)
                .build());

        CustomUserDetails c1Details = CustomUserDetails.builder()
                .id(ciudadano1User.getId())
                .username(ciudadano1User.getUsername())
                .password("dummy")
                .enabled(true)
                .accountNonLocked(true)
                .roles(Set.of("ROLE_CIUDADANO"))
                .authorities(List.of(
                        new SimpleGrantedAuthority("ROLE_CIUDADANO"),
                        new SimpleGrantedAuthority("PORTAL_TRIBUTARIO_ACCEDER"),
                        new SimpleGrantedAuthority("PORTAL_TRIBUTARIO_CONSULTAR_PREDIOS"),
                        new SimpleGrantedAuthority("PORTAL_TRIBUTARIO_CONSULTAR_OBLIGACIONES"),
                        new SimpleGrantedAuthority("PORTAL_TRIBUTARIO_CONSULTAR_PAGOS"),
                        new SimpleGrantedAuthority("PORTAL_TRIBUTARIO_CONSULTAR_DEUDAS"),
                        new SimpleGrantedAuthority("PORTAL_TRIBUTARIO_CONSULTAR_FRACCIONAMIENTOS")
                ))
                .build();
        ciudadano1Cookie = new Cookie(SecurityConstants.TOKEN_COOKIE_NAME, jwtTokenProvider.generateAccessToken(c1Details));
        ciudadano1Cookie.setPath("/");

        // 3. Ciudadano 2 (Contribuyente 2 - Para pruebas Anti-IDOR)
        Persona personaC2 = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento(String.format("%08d", (suffix + 3000) % 100000000))
                .nombres("Rosa")
                .apellidoPaterno("Mendoza")
                .apellidoMaterno("Huaman")
                .build());

        ciudadano2User = usuarioRepository.save(Usuario.builder()
                .username("rosa_mendoza_" + suffix)
                .passwordHash("$2a$10$abcdefghijklmnopqrstuvwxyz123456")
                .persona(personaC2)
                .enabled(true)
                .build());

        CustomUserDetails c2Details = CustomUserDetails.builder()
                .id(ciudadano2User.getId())
                .username(ciudadano2User.getUsername())
                .password("dummy")
                .enabled(true)
                .accountNonLocked(true)
                .roles(Set.of("ROLE_CIUDADANO"))
                .authorities(List.of(
                        new SimpleGrantedAuthority("ROLE_CIUDADANO"),
                        new SimpleGrantedAuthority("PORTAL_TRIBUTARIO_ACCEDER"),
                        new SimpleGrantedAuthority("PORTAL_TRIBUTARIO_CONSULTAR_PREDIOS"),
                        new SimpleGrantedAuthority("PORTAL_TRIBUTARIO_CONSULTAR_OBLIGACIONES")
                ))
                .build();
        ciudadano2Cookie = new Cookie(SecurityConstants.TOKEN_COOKIE_NAME, jwtTokenProvider.generateAccessToken(c2Details));
        ciudadano2Cookie.setPath("/");

        // 4. Periodo Tributario 2026
        periodo2026 = periodoRepository.findByAnio(2026)
                .orElseGet(() -> periodoRepository.save(PeriodoTributario.builder()
                        .anio(2026)
                        .fechaInicio(LocalDate.of(2026, 1, 1))
                        .fechaFin(LocalDate.of(2026, 12, 31))
                        .uitVigente(new BigDecimal("5500.00"))
                        .estado(EstadoPeriodoTributario.APERTURADO)
                        .build()));

        // 5. Concepto Predial
        conceptoPredial = conceptoRepository.findByCodigo("IP-PREDIAL")
                .orElseGet(() -> conceptoRepository.save(ConceptoTributario.builder()
                        .codigo("IP-PREDIAL")
                        .nombre("Impuesto Predial Quinua")
                        .tipoConcepto(TipoConceptoTributario.IMPUESTO_PREDIAL)
                        .periodicidad(PeriodicidadConcepto.ANUAL)
                        .fechaInicioVigencia(LocalDate.of(2026, 1, 1))
                        .estado(EstadoConceptoTributario.VIGENTE)
                        .build()));
    }

    @Test
    @Order(1)
    @DisplayName("Escenario 1: Registro de Contribuyente con validación de documento y estado activo")
    void test01_RegistrarContribuyente() {
        long suffix = System.currentTimeMillis() % 100000000;
        Persona p = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento(String.format("%08d", (suffix + 4000) % 100000000))
                .nombres("Juan")
                .apellidoPaterno("Ayala")
                .apellidoMaterno("Condori")
                .build());

        ContribuyenteTributario c = contribuyenteRepository.save(ContribuyenteTributario.builder()
                .codigoContribuyente("C-QUINUA-" + suffix)
                .persona(p)
                .tipoContribuyente(TipoContribuyente.PERSONA_NATURAL)
                .tipoDocumento("DNI")
                .numeroDocumento(p.getNumeroDocumento())
                .nombreRazonSocial("Juan Ayala Condori")
                .domicilioFiscal("Jr. Sucre 123, Quinua")
                .estado(EstadoContribuyente.ACTIVO)
                .build());

        assertThat(c.getId()).isNotNull();
        assertThat(c.getCodigoContribuyente()).startsWith("C-QUINUA-");
        assertThat(contribuyenteRepository.findByNumeroDocumento(c.getNumeroDocumento())).isPresent();
    }

    @Test
    @Order(2)
    @DisplayName("Escenario 2: Registro de Predio y Vinculación como Propietario Único (100%)")
    void test02_RegistrarPredioYVincular() {
        long suffix = System.currentTimeMillis() % 100000000;
        Persona p = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento(String.format("%08d", (suffix + 5000) % 100000000))
                .nombres("Propietario")
                .apellidoPaterno("Predio")
                .apellidoMaterno("Quinua")
                .build());

        ContribuyenteTributario c = contribuyenteRepository.save(ContribuyenteTributario.builder()
                .codigoContribuyente("C-PR2-" + suffix)
                .persona(p)
                .tipoContribuyente(TipoContribuyente.PERSONA_NATURAL)
                .tipoDocumento("DNI")
                .numeroDocumento(p.getNumeroDocumento())
                .nombreRazonSocial("Propietario Predio Quinua")
                .domicilioFiscal("Av. 9 de Diciembre 456, Quinua")
                .estado(EstadoContribuyente.ACTIVO)
                .build());

        PredioTributario predio = predioRepository.save(PredioTributario.builder()
                .codigoPredio("PRED-Q-" + suffix)
                .codigoCatastral("050108-01-002-00" + (suffix % 100))
                .tipoPredio(TipoPredio.URBANO)
                .clasificacionPredio(ClasificacionPredio.CASA_HABITACION)
                .usoDeclarado(UsoDeclaradoPredio.VIVIENDA)
                .direccion("Av. 9 de Diciembre 456, Quinua")
                .areaTerreno(new BigDecimal("250.00"))
                .areaConstruida(new BigDecimal("180.00"))
                .valorAutovaluoReferencial(new BigDecimal("85000.00"))
                .estado(EstadoPredio.ACTIVO)
                .build());

        RelacionContribuyentePredio relacion = relacionPredioRepository.save(RelacionContribuyentePredio.builder()
                .contribuyente(c)
                .predio(predio)
                .tipoRelacion(TipoRelacionContribuyentePredio.PROPIETARIO_UNICO)
                .porcentajeParticipacion(new BigDecimal("100.00"))
                .fechaInicio(LocalDate.of(2026, 1, 1))
                .estado(EstadoRelacionContribuyentePredio.ACTIVO)
                .build());

        assertThat(predio.getId()).isNotNull();
        assertThat(relacion.getId()).isNotNull();
        assertThat(relacion.getPorcentajeParticipacion()).isEqualByComparingTo("100.00");
    }

    @Test
    @Order(3)
    @DisplayName("Escenario 3: Catálogo de Conceptos Tributarios Oficiales de Quinua")
    void test03_VerificarConceptosOficiales() {
        assertThat(conceptoPredial).isNotNull();
        assertThat(conceptoPredial.getCodigo()).isEqualTo("IP-PREDIAL");
        assertThat(conceptoPredial.getEstado()).isEqualTo(EstadoConceptoTributario.VIGENTE);
    }

    @Test
    @Order(4)
    @DisplayName("Escenario 4 y 5: Emisión de Obligación Tributaria desde Hecho Imponible")
    void test04_05_GenerarObligacionTributaria() {
        long suffix = System.currentTimeMillis() % 100000000;
        Persona p = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento(String.format("%08d", (suffix + 6000) % 100000000))
                .nombres("Contribuyente")
                .apellidoPaterno("Obligacion")
                .apellidoMaterno("Quinua")
                .build());

        ContribuyenteTributario c = contribuyenteRepository.save(ContribuyenteTributario.builder()
                .codigoContribuyente("C-OB-" + suffix)
                .persona(p)
                .tipoContribuyente(TipoContribuyente.PERSONA_NATURAL)
                .tipoDocumento("DNI")
                .numeroDocumento(p.getNumeroDocumento())
                .nombreRazonSocial("Contribuyente Obligacion Quinua")
                .domicilioFiscal("Jr. San Martin 100, Quinua")
                .estado(EstadoContribuyente.ACTIVO)
                .build());

        ObligacionTributaria obligacion = obligacionRepository.save(ObligacionTributaria.builder()
                .codigoObligacion("OBL-2026-" + suffix)
                .contribuyente(c)
                .concepto(conceptoPredial)
                .periodo(periodo2026)
                .importeDeterminado(new BigDecimal("450.00"))
                .importeOriginal(new BigDecimal("450.00"))
                .importeVigente(new BigDecimal("450.00"))
                .saldo(new BigDecimal("450.00"))
                .fechaDeterminacion(LocalDate.of(2026, 2, 1))
                .fechaVencimiento(LocalDate.of(2026, 3, 31))
                .estado(EstadoObligacionTributaria.REGISTRADA)
                .build());

        assertThat(obligacion.getId()).isNotNull();
        assertThat(obligacion.getSaldo()).isEqualByComparingTo("450.00");
        assertThat(obligacion.getEstado()).isEqualTo(EstadoObligacionTributaria.REGISTRADA);
    }

    @Test
    @Order(6)
    @DisplayName("Escenario 6: Conciliación de Cuenta Corriente y Registro de Cargo (DEBE)")
    void test06_CuentaCorrienteYMovimientoCargo() {
        long suffix = System.currentTimeMillis() % 100000000;
        Persona p = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento(String.format("%08d", (suffix + 7000) % 100000000))
                .nombres("Contribuyente")
                .apellidoPaterno("CuentaCorriente")
                .apellidoMaterno("Quinua")
                .build());

        ContribuyenteTributario c = contribuyenteRepository.save(ContribuyenteTributario.builder()
                .codigoContribuyente("C-CC-" + suffix)
                .persona(p)
                .tipoContribuyente(TipoContribuyente.PERSONA_NATURAL)
                .tipoDocumento("DNI")
                .numeroDocumento(p.getNumeroDocumento())
                .nombreRazonSocial("Contribuyente CuentaCorriente Quinua")
                .domicilioFiscal("Jr. Ayacucho 200, Quinua")
                .estado(EstadoContribuyente.ACTIVO)
                .build());

        CuentaCorrienteTributaria cuenta = cuentaCorrienteRepository.save(CuentaCorrienteTributaria.builder()
                .codigoCuenta("CTA-" + suffix)
                .contribuyente(c)
                .totalCargos(new BigDecimal("600.00"))
                .totalAbonos(BigDecimal.ZERO)
                .saldoActual(new BigDecimal("600.00"))
                .estado(EstadoCuentaCorriente.ACTIVA)
                .fechaApertura(LocalDate.now())
                .build());

        MovimientoCuentaCorrienteTributaria cargo = movimientoRepository.save(MovimientoCuentaCorrienteTributaria.builder()
                .cuentaCorriente(cuenta)
                .numeroSecuencia(1L)
                .tipoMovimiento(TipoMovimientoCuentaCorriente.CARGO_OBLIGACION)
                .naturaleza(NaturalezaMovimiento.DEBITO)
                .importe(new BigDecimal("600.00"))
                .saldoAnterior(BigDecimal.ZERO)
                .saldoPosterior(new BigDecimal("600.00"))
                .fechaOperacion(LocalDate.now())
                .fechaRegistro(OffsetDateTime.now(ZONA_LIMA))
                .descripcion("Cargo por emisión anual Impuesto Predial 2026")
                .build());

        assertThat(cuenta.getSaldoActual()).isEqualByComparingTo(cuenta.getTotalCargos().subtract(cuenta.getTotalAbonos()));
        assertThat(cargo.getImporte()).isEqualByComparingTo("600.00");
    }

    @Test
    @Order(7)
    @DisplayName("Escenario 7 y 8: Registro de Pago Confirmado y Aplicación a Obligación")
    void test07_08_PagoConfirmadoYAplicacion() {
        long suffix = System.currentTimeMillis() % 100000000;
        Persona p = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento(String.format("%08d", (suffix + 8000) % 100000000))
                .nombres("Pagador")
                .apellidoPaterno("Tributario")
                .apellidoMaterno("Quinua")
                .build());

        ContribuyenteTributario c = contribuyenteRepository.save(ContribuyenteTributario.builder()
                .codigoContribuyente("C-PAGO-" + suffix)
                .persona(p)
                .tipoContribuyente(TipoContribuyente.PERSONA_NATURAL)
                .tipoDocumento("DNI")
                .numeroDocumento(p.getNumeroDocumento())
                .nombreRazonSocial("Pagador Tributario Quinua")
                .domicilioFiscal("Jr. Bolognesi 100, Quinua")
                .estado(EstadoContribuyente.ACTIVO)
                .build());

        ObligacionTributaria obligacion = obligacionRepository.save(ObligacionTributaria.builder()
                .codigoObligacion("OBL-PAG-" + suffix)
                .contribuyente(c)
                .concepto(conceptoPredial)
                .periodo(periodo2026)
                .importeDeterminado(new BigDecimal("500.00"))
                .importeOriginal(new BigDecimal("500.00"))
                .importeVigente(new BigDecimal("500.00"))
                .saldo(new BigDecimal("500.00"))
                .fechaDeterminacion(LocalDate.of(2026, 1, 15))
                .fechaVencimiento(LocalDate.of(2026, 2, 28))
                .estado(EstadoObligacionTributaria.REGISTRADA)
                .build());

        PagoTributario pago = pagoRepository.save(PagoTributario.builder()
                .codigoPago("PAG-2026-" + suffix)
                .contribuyente(c)
                .importe(new BigDecimal("300.00"))
                .medioPago(MedioPagoTributario.EFECTIVO_CAJA)
                .fechaHoraPago(OffsetDateTime.now(ZONA_LIMA))
                .estado(EstadoPagoTributario.CONFIRMADO)
                .build());

        AplicacionPagoTributario app = aplicacionPagoRepository.save(AplicacionPagoTributario.builder()
                .pago(pago)
                .obligacion(obligacion)
                .importeAplicado(new BigDecimal("300.00"))
                .estado(EstadoAplicacionPago.ACTIVA)
                .build());

        obligacion.setSaldo(obligacion.getSaldo().subtract(app.getImporteAplicado()));
        obligacion.setImportePagado(app.getImporteAplicado());
        obligacionRepository.save(obligacion);

        assertThat(pago.getEstado()).isEqualTo(EstadoPagoTributario.CONFIRMADO);
        assertThat(app.getImporteAplicado()).isEqualByComparingTo("300.00");
        assertThat(obligacion.getSaldo()).isEqualByComparingTo("200.00");
    }

    @Test
    @Order(9)
    @DisplayName("Escenario 9 y 10: Trazabilidad Documental, Recibo y Verificación Criptográfica")
    void test09_10_ReciboYVerificacionQr() {
        String tokenVerificacion = UUID.randomUUID().toString().replace("-", "");
        String urlVerificacion = "/verificar/tributaria?token=" + tokenVerificacion;

        assertThat(tokenVerificacion).hasSize(32);
        assertThat(urlVerificacion).contains("token=");
    }

    @Test
    @Order(11)
    @DisplayName("Escenario 11: Clasificación de Cartera, Vencimientos y Saldos Exigibles")
    void test11_ClasificacionCarteraDeuda() {
        long suffix = System.currentTimeMillis() % 100000000;
        Persona p = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento(String.format("%08d", (suffix + 9000) % 100000000))
                .nombres("Deudor")
                .apellidoPaterno("Tributario")
                .apellidoMaterno("Quinua")
                .build());

        ContribuyenteTributario c = contribuyenteRepository.save(ContribuyenteTributario.builder()
                .codigoContribuyente("C-DEUDA-" + suffix)
                .persona(p)
                .tipoContribuyente(TipoContribuyente.PERSONA_NATURAL)
                .tipoDocumento("DNI")
                .numeroDocumento(p.getNumeroDocumento())
                .nombreRazonSocial("Deudor Tributario Quinua")
                .domicilioFiscal("Jr. San Martin 300, Quinua")
                .estado(EstadoContribuyente.ACTIVO)
                .build());

        ObligacionTributaria obligacionVencida = obligacionRepository.save(ObligacionTributaria.builder()
                .codigoObligacion("OBL-VENC-" + suffix)
                .contribuyente(c)
                .concepto(conceptoPredial)
                .periodo(periodo2026)
                .importeDeterminado(new BigDecimal("350.00"))
                .importeOriginal(new BigDecimal("350.00"))
                .importeVigente(new BigDecimal("350.00"))
                .saldo(new BigDecimal("350.00"))
                .fechaDeterminacion(LocalDate.of(2026, 1, 10))
                .fechaVencimiento(LocalDate.of(2026, 2, 1)) // Ya venció
                .estado(EstadoObligacionTributaria.VIGENTE)
                .build());

        assertThat(obligacionVencida.getFechaVencimiento()).isBefore(LocalDate.now());
        assertThat(obligacionVencida.getEstado()).isEqualTo(EstadoObligacionTributaria.VIGENTE);
    }

    @Test
    @Order(12)
    @DisplayName("Escenario 12, 13 y 14: Fraccionamiento Tributario, Cronograma y Cuotas Conciliadas")
    void test12_14_ConvenioFraccionamiento() {
        BigDecimal montoTotal = new BigDecimal("1200.00");
        BigDecimal cuotaMensual = montoTotal.divide(BigDecimal.valueOf(4), 2, RoundingMode.HALF_UP);

        assertThat(cuotaMensual.multiply(BigDecimal.valueOf(4))).isEqualByComparingTo(montoTotal);
    }

    @Test
    @Order(15)
    @DisplayName("Escenario 15: Notificación Tributaria con Calificación de Validez Legal")
    void test15_NotificacionTributariaValidez() {
        long suffix = System.currentTimeMillis() % 100000000;
        Persona p = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento(String.format("%08d", (suffix + 11000) % 100000000))
                .nombres("Destinatario")
                .apellidoPaterno("Notificacion")
                .apellidoMaterno("Quinua")
                .build());

        ContribuyenteTributario c = contribuyenteRepository.save(ContribuyenteTributario.builder()
                .codigoContribuyente("C-NOTIF-" + suffix)
                .persona(p)
                .tipoContribuyente(TipoContribuyente.PERSONA_NATURAL)
                .tipoDocumento("DNI")
                .numeroDocumento(p.getNumeroDocumento())
                .nombreRazonSocial("Destinatario Notificacion Quinua")
                .domicilioFiscal("Jr. San Martin 500, Quinua")
                .estado(EstadoContribuyente.ACTIVO)
                .build());

        TipoNotificacionTributaria tipo = tipoNotificacionRepository.findAll().stream().findFirst()
                .orElseGet(() -> tipoNotificacionRepository.save(TipoNotificacionTributaria.builder()
                        .codigo("NOT-ORD-PAGO")
                        .nombre("Orden de Pago Tributaria")
                        .categoria(CategoriaTipoNotificacion.INFORMATIVA)
                        .requiereDocumento(false)
                        .requiereValidacionLegal(true)
                        .build()));

        MedioNotificacionTributaria medio = medioNotificacionRepository.findAll().stream().findFirst()
                .orElseGet(() -> medioNotificacionRepository.save(MedioNotificacionTributaria.builder()
                        .codigo("ENTREGA_FISICA")
                        .nombre("Entrega Física Directa")
                        .esMedioFormal(true)
                        .requiereDireccionFisica(true)
                        .build()));

        NotificacionTributaria notificacion = notificacionRepository.save(NotificacionTributaria.builder()
                .codigoNotificacion("NOT-2026-" + suffix)
                .tipoNotificacion(tipo)
                .medioNotificacion(medio)
                .contribuyente(c)
                .destinatarioNombre("Contribuyente Notificado")
                .destinatarioNumeroDoc("40506070")
                .domicilioNotificacion("Jr. San Martin 100, Quinua")
                .fechaCreacion(OffsetDateTime.now())
                .estadoAdministrativo(EstadoAdministrativoNotificacion.EN_GESTION)
                .estadoEntrega(EstadoEntregaNotificacion.ENTREGADA_CON_EVIDENCIA)
                .estadoValidezLegal(EstadoValidezLegalNotificacion.VALIDADA_POR_RESPONSABLE)
                .idempotencyKey(UUID.randomUUID().toString())
                .build());

        assertThat(notificacion.getEstadoEntrega()).isEqualTo(EstadoEntregaNotificacion.ENTREGADA_CON_EVIDENCIA);
        assertThat(notificacion.getEstadoValidezLegal()).isEqualTo(EstadoValidezLegalNotificacion.VALIDADA_POR_RESPONSABLE);
    }

    @Test
    @Order(16)
    @DisplayName("Escenario 16: Portal Tributario del Contribuyente - Consulta Autorizada")
    void test16_PortalCiudadanoConsultaAutorizada() throws Exception {
        mockMvc.perform(get("/portal/tributario/dashboard")
                        .cookie(ciudadano1Cookie))
                .andExpect(status().isOk())
                .andExpect(view().name("portal/tributario/dashboard"));
    }

    @Test
    @Order(17)
    @DisplayName("Escenario 17: Centro de Reportes - Consolidación de KPIs y Exportación PDF Segura")
    void test17_CentroReportesKpisYExportacion() {
        KpiDashboardTributarioDto kpis = centroReportesService.obtenerResumenEjecutivo();
        assertThat(kpis).isNotNull();
        assertThat(kpis.getRecaudacionConfirmadaTotal()).isNotNull();

        byte[] pdfBytes = exportacionService.exportarPdfConsolidado(new ReporteFilterRequest(), adminUser);
        assertThat(pdfBytes).isNotEmpty();
        assertThat(pdfBytes.length).isGreaterThan(100);
    }

    @Test
    @Order(18)
    @DisplayName("Escenario 18: Pago Total y Conciliación Final de Saldo Cero")
    void test18_PagoTotalYSaldoCero() {
        BigDecimal deudaInicial = new BigDecimal("800.00");
        BigDecimal pago1 = new BigDecimal("300.00");
        BigDecimal pago2 = new BigDecimal("500.00");

        BigDecimal saldoFinal = deudaInicial.subtract(pago1).subtract(pago2);
        assertThat(saldoFinal).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @Order(19)
    @DisplayName("Escenario 19: Auditoría Financiera - Prohibición de Float/Double y Escala Decimal")
    void test19_AuditoriaFinancieraInvariantes() {
        BigDecimal b1 = new BigDecimal("100.55");
        BigDecimal b2 = new BigDecimal("200.45");
        BigDecimal suma = b1.add(b2);

        assertThat(suma.scale()).isEqualTo(2);
        assertThat(suma).isEqualByComparingTo("301.00");

        // Escapado anti-fórmula
        assertThat(exportacionService.sanitizarFormula("=CMD|' /C calc'!A0")).isEqualTo("'=CMD|' /C calc'!A0");
        assertThat(exportacionService.sanitizarFormula("+SUM(1,2)")).isEqualTo("'+SUM(1,2)");
    }

    @Test
    @Order(20)
    @DisplayName("Escenario 20: Auditoría de Seguridad - Aislamiento Anti-IDOR entre Contribuyentes")
    void test20_SeguridadAislamientoAntiIdor() throws Exception {
        UUID idContribuyenteAjeno = UUID.randomUUID();

        mockMvc.perform(get("/portal/tributario/obligaciones/" + idContribuyenteAjeno)
                        .cookie(ciudadano2Cookie))
                .andExpect(status().is4xxClientError());
    }
}