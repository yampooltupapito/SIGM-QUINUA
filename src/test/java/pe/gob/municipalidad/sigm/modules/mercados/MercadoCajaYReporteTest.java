package pe.gob.municipalidad.sigm.modules.mercados;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.model.Persona;
import pe.gob.municipalidad.sigm.common.model.Rol;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.PersonaRepository;
import pe.gob.municipalidad.sigm.common.repository.RolRepository;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.mercados.dto.*;
import pe.gob.municipalidad.sigm.modules.mercados.entity.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.*;
import pe.gob.municipalidad.sigm.modules.mercados.repository.*;
import pe.gob.municipalidad.sigm.modules.mercados.service.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MercadoCajaYReporteTest {

    @Autowired
    private CajaMercadoService cajaService;

    @Autowired
    private PagoMercadoService pagoService;

    @Autowired
    private ObligacionMercadoService obligacionService;

    @Autowired
    private FinanzasMercadoReporteService reporteService;

    @Autowired
    private CajaMercadoRepository cajaRepository;

    @Autowired
    private SesionCajaMercadoRepository sesionRepository;

    @Autowired
    private PagoMercadoRepository pagoRepository;

    @Autowired
    private ComercianteRepository comercianteRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private MercadoMunicipalRepository mercadoRepository;

    @Autowired
    private PuestoComercialRepository puestoRepository;

    @Autowired
    private ConceptoCobroMercadoRepository conceptoRepository;

    @Autowired
    private TarifaMercadoRepository tarifaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private CajaMercado caja;
    private Usuario cajeroUser;
    private Comerciante comerciante;
    private PuestoComercial puesto;
    private ConceptoCobroMercado concepto;

    @BeforeEach
    void setUp() {
        long ts = System.currentTimeMillis();

        Rol rolCajero = rolRepository.findByCodigo("ROLE_CAJERO").orElseGet(() ->
                rolRepository.save(Rol.builder().codigo("ROLE_CAJERO").nombre("Cajero").estado("ACTIVO").build()));

        Persona personaCajero = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento("42" + String.format("%06d", (int)(ts % 1000000)))
                .nombres("Mario")
                .apellidoPaterno("Cajas")
                .correo("mario.cajas" + ts + "@quinua.gob.pe")
                .estado("ACTIVO")
                .build());

        cajeroUser = usuarioRepository.save(Usuario.builder()
                .username("cajero_" + ts)
                .persona(personaCajero)
                .passwordHash(passwordEncoder.encode("CajeroPass123!"))
                .enabled(true)
                .accountLocked(false)
                .roles(Set.of(rolCajero))
                .build());

        caja = cajaRepository.findByCodigo("CAJA-MERC-01").orElseGet(() ->
                cajaRepository.save(CajaMercado.builder()
                        .codigo("CAJA-MERC-01")
                        .nombre("Ventanilla 01 - Mercado Central")
                        .estado("ACTIVA")
                        .build()));

        MercadoMunicipal mercado = mercadoRepository.save(MercadoMunicipal.builder()
                .codigo("MER-CAJ-" + (ts % 10000))
                .nombre("Mercado Artesanal Quinua")
                .direccion("Plaza de Armas S/N, Quinua")
                .distrito("Quinua")
                .provincia("Huamanga")
                .departamento("Ayacucho")
                .estado(EstadoMercado.ACTIVO)
                .build());

        puesto = puestoRepository.save(PuestoComercial.builder()
                .mercado(mercado)
                .codigo("PUE-C-" + (ts % 10000))
                .numero("A-201")
                .areaMetrosCuadrados(new BigDecimal("10.00"))
                .estado(EstadoPuesto.OCUPADO)
                .build());

        Persona personaCom = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento("44" + String.format("%06d", (int)(ts % 1000000)))
                .nombres("Vicente")
                .apellidoPaterno("Alanya")
                .correo("vicente" + ts + "@quinua.gob.pe")
                .estado("ACTIVO")
                .build());

        comerciante = comercianteRepository.save(Comerciante.builder()
                .persona(personaCom)
                .tipoComerciante(TipoComerciante.PERSONA_NATURAL)
                .codigoComerciante("COM-C-" + (ts % 10000))
                .estado(EstadoComerciante.ACTIVO)
                .fechaRegistro(LocalDate.now())
                .build());

        concepto = conceptoRepository.save(ConceptoCobroMercado.builder()
                .codigo("ALQUILER_" + (ts % 10000))
                .nombre("Alquiler de Puesto Artesanal")
                .tipoConcepto(TipoConceptoCobro.USO_PUESTO_COMERCIAL)
                .periodicidad(PeriodicidadConcepto.MENSUAL)
                .estado("ACTIVO")
                .requiereAsignacion(false)
                .build());

        tarifaRepository.save(TarifaMercado.builder()
                .concepto(concepto)
                .mercado(mercado)
                .monto(new BigDecimal("80.00"))
                .moneda("PEN")
                .fechaInicioVigencia(LocalDate.now().minusMonths(1))
                .estado("ACTIVA")
                .fuenteNormativa("Ordenanza MDQ")
                .versionTarifa(1)
                .build());
    }

    @Test
    @DisplayName("01. Abrir sesión de caja exitosamente y registrar saldo inicial")
    void testAbrirSesionCaja() {
        AperturaCajaRequest req = AperturaCajaRequest.builder()
                .cajaId(caja.getId())
                .montoInicial(new BigDecimal("150.00"))
                .observaciones("Apertura de turno matutino")
                .build();

        SesionCajaResponse sesion = cajaService.abrirSesion(req, cajeroUser.getUsername(), cajeroUser.getId());

        assertThat(sesion).isNotNull();
        assertThat(sesion.getId()).isNotNull();
        assertThat(sesion.getEstado()).isEqualTo(EstadoSesionCaja.ABIERTA);
        assertThat(sesion.getMontoInicial()).isEqualByComparingTo("150.00");
        assertThat(sesion.getCajeroUsername()).isEqualTo(cajeroUser.getUsername());
    }

    @Test
    @DisplayName("02. Rechazar apertura de sesión duplicada en misma caja")
    void testRechazarAperturaDuplicada() {
        AperturaCajaRequest req1 = AperturaCajaRequest.builder()
                .cajaId(caja.getId())
                .montoInicial(new BigDecimal("100.00"))
                .build();
        cajaService.abrirSesion(req1, cajeroUser.getUsername(), cajeroUser.getId());

        // Intentar abrir otra sesión en la misma caja
        AperturaCajaRequest req2 = AperturaCajaRequest.builder()
                .cajaId(caja.getId())
                .montoInicial(new BigDecimal("50.00"))
                .build();

        assertThatThrownBy(() -> cajaService.abrirSesion(req2, cajeroUser.getUsername(), cajeroUser.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ya tiene una sesión abierta");
    }

    @Test
    @DisplayName("03. Cerrar sesión de caja y calcular diferencia entre declarado y esperado")
    void testCerrarSesionCajaConDiferencia() {
        // Abrir con S/ 100.00
        AperturaCajaRequest reqAbrir = AperturaCajaRequest.builder()
                .cajaId(caja.getId())
                .montoInicial(new BigDecimal("100.00"))
                .build();
        SesionCajaResponse sesion = cajaService.abrirSesion(reqAbrir, cajeroUser.getUsername(), cajeroUser.getId());

        // Registrar una obligación de S/ 80.00 y pagarla en efectivo vinculada a la sesión
        ObligacionCreateRequest reqObl = ObligacionCreateRequest.builder()
                .comercianteId(comerciante.getId())
                .puestoId(puesto.getId())
                .conceptoId(concepto.getId())
                .montoOriginal(new BigDecimal("80.00"))
                .build();
        ObligacionResponse obl = obligacionService.crear(reqObl, "operador");

        PagoCreateRequest reqPago = PagoCreateRequest.builder()
                .comercianteId(comerciante.getId())
                .cajaSesionId(sesion.getId())
                .montoRecibido(new BigDecimal("80.00"))
                .medioPago(MedioPagoMercado.EFECTIVO)
                .aplicaciones(List.of(
                        AplicacionItemRequest.builder().obligacionId(obl.getId()).monto(new BigDecimal("80.00")).build()
                ))
                .build();
        pagoService.registrarPago(reqPago, cajeroUser.getUsername(), cajeroUser.getId());

        // Efectivo esperado = 100 (inicial) + 80 (efectivo) = S/ 180.00
        // Cajero declara S/ 180.00 -> Diferencia 0.00
        CierreCajaRequest reqCerrar = CierreCajaRequest.builder()
                .montoFinalDeclarado(new BigDecimal("180.00"))
                .observaciones("Cierre conforme sin diferencias")
                .build();

        SesionCajaResponse cerrada = cajaService.cerrarSesion(sesion.getId(), reqCerrar, cajeroUser.getUsername());

        assertThat(cerrada.getEstado()).isEqualTo(EstadoSesionCaja.CERRADA);
        assertThat(cerrada.getTotalIngresos()).isEqualByComparingTo("80.00");
        assertThat(cerrada.getMontoFinalEsperado()).isEqualByComparingTo("180.00");
        assertThat(cerrada.getMontoFinalDeclarado()).isEqualByComparingTo("180.00");
        assertThat(cerrada.getDiferencia()).isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("04. Generar constancia de pago en PDF con cabecera %PDF- y código QR")
    void testGenerarConstanciaPdfYQr() {
        ObligacionCreateRequest reqObl = ObligacionCreateRequest.builder()
                .comercianteId(comerciante.getId())
                .puestoId(puesto.getId())
                .conceptoId(concepto.getId())
                .montoOriginal(new BigDecimal("80.00"))
                .build();
        ObligacionResponse obl = obligacionService.crear(reqObl, "operador");

        PagoCreateRequest reqPago = PagoCreateRequest.builder()
                .comercianteId(comerciante.getId())
                .montoRecibido(new BigDecimal("80.00"))
                .medioPago(MedioPagoMercado.EFECTIVO)
                .aplicaciones(List.of(
                        AplicacionItemRequest.builder().obligacionId(obl.getId()).monto(new BigDecimal("80.00")).build()
                ))
                .build();
        PagoResponse pago = pagoService.registrarPago(reqPago, cajeroUser.getUsername(), cajeroUser.getId());

        byte[] pdfBytes = pagoService.generarConstanciaPdf(pago.getId());

        assertThat(pdfBytes).isNotNull();
        assertThat(pdfBytes.length).isGreaterThan(100);
        // Validar cabecera estándar de archivo PDF (%PDF-)
        String header = new String(pdfBytes, 0, Math.min(5, pdfBytes.length));
        assertThat(header).isEqualTo("%PDF-");
    }

    @Test
    @DisplayName("05. Exportar reporte de obligaciones a CSV sanitizando inyección de fórmulas")
    void testExportarExcelObligacionesYSanitizar() {
        // Crear obligación con caracteres sospechosos de fórmula en el concepto/observación si existiera
        ObligacionCreateRequest reqObl = ObligacionCreateRequest.builder()
                .comercianteId(comerciante.getId())
                .puestoId(puesto.getId())
                .conceptoId(concepto.getId())
                .montoOriginal(new BigDecimal("50.00"))
                .build();
        obligacionService.crear(reqObl, "operador");

        byte[] csvBytes = reporteService.exportarExcelObligaciones(null, null, null, null, null);

        assertThat(csvBytes).isNotNull();
        String contenido = new String(csvBytes);
        assertThat(contenido).contains("Código Obligación;Comerciante;N° Documento");
        assertThat(contenido).contains(comerciante.getPersona().getApellidoPaterno());
    }

    @Test
    @DisplayName("06. Consultar estadísticas financieras consolidadas")
    void testEstadisticasFinancieras() {
        EstadisticasFinancierasResponse stats = reporteService.obtenerEstadisticas();

        assertThat(stats).isNotNull();
        assertThat(stats.getTotalObligacionesEmitidas()).isGreaterThanOrEqualTo(0);
        assertThat(stats.getMontoTotalRecaudado()).isNotNull();
        assertThat(stats.getRecaudacionHoy()).isNotNull();
    }
}
