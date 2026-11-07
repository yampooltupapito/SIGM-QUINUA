package pe.gob.municipalidad.sigm.modules.mercados;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.model.Persona;
import pe.gob.municipalidad.sigm.common.repository.PersonaRepository;
import pe.gob.municipalidad.sigm.modules.mercados.dto.*;
import pe.gob.municipalidad.sigm.modules.mercados.entity.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.*;
import pe.gob.municipalidad.sigm.modules.mercados.repository.*;
import pe.gob.municipalidad.sigm.modules.mercados.service.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MercadoFinanzasServiceTest {

    @Autowired
    private ConceptoCobroMercadoService conceptoService;

    @Autowired
    private TarifaMercadoService tarifaService;

    @Autowired
    private ObligacionMercadoService obligacionService;

    @Autowired
    private PagoMercadoService pagoService;

    @Autowired
    private CuentaCorrienteMercadoService cuentaCorrienteService;

    @Autowired
    private MercadoMunicipalRepository mercadoRepository;

    @Autowired
    private PuestoComercialRepository puestoRepository;

    @Autowired
    private ComercianteRepository comercianteRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private ConceptoCobroMercadoRepository conceptoRepository;

    @Autowired
    private TarifaMercadoRepository tarifaRepository;

    private MercadoMunicipal mercado;
    private PuestoComercial puesto;
    private Comerciante comerciante;
    private ConceptoCobroMercado concepto;
    private TarifaMercado tarifa;

    @BeforeEach
    void setUp() {
        long ts = System.currentTimeMillis();

        mercado = mercadoRepository.save(MercadoMunicipal.builder()
                .codigo("MER-FIN-" + (ts % 10000))
                .nombre("Mercado Central de Quinua")
                .direccion("Plaza de Armas S/N")
                .distrito("Quinua")
                .provincia("Huamanga")
                .departamento("Ayacucho")
                .estado(EstadoMercado.ACTIVO)
                .build());

        puesto = puestoRepository.save(PuestoComercial.builder()
                .mercado(mercado)
                .codigo("PUE-F-" + (ts % 10000))
                .numero("B-105")
                .areaMetrosCuadrados(new BigDecimal("12.00"))
                .estado(EstadoPuesto.OCUPADO)
                .build());

        Persona persona = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento("41" + String.format("%06d", (int)(ts % 1000000)))
                .nombres("Saturnino")
                .apellidoPaterno("Huaman")
                .apellidoMaterno("Quispe")
                .correo("saturnino" + ts + "@quinua.gob.pe")
                .direccion("Jr. Sucre 123, Quinua")
                .estado("ACTIVO")
                .build());

        comerciante = comercianteRepository.save(Comerciante.builder()
                .persona(persona)
                .tipoComerciante(TipoComerciante.PERSONA_NATURAL)
                .codigoComerciante("COM-F-" + (ts % 10000))
                .estado(EstadoComerciante.ACTIVO)
                .fechaRegistro(LocalDate.now())
                .build());

        concepto = conceptoRepository.findByCodigo("USO_PUESTO_TEST_" + (ts % 10000))
                .orElseGet(() -> conceptoRepository.save(ConceptoCobroMercado.builder()
                        .codigo("USO_PUESTO_TEST_" + (ts % 10000))
                        .nombre("Merced Conductiva Puesto")
                        .descripcion("Canon mensual de ocupación")
                        .tipoConcepto(TipoConceptoCobro.USO_PUESTO_COMERCIAL)
                        .periodicidad(PeriodicidadConcepto.MENSUAL)
                        .estado("ACTIVO")
                        .requiereAsignacion(false)
                        .build()));

        tarifa = tarifaRepository.save(TarifaMercado.builder()
                .concepto(concepto)
                .mercado(mercado)
                .monto(new BigDecimal("60.00"))
                .moneda("PEN")
                .fechaInicioVigencia(LocalDate.now().minusMonths(1))
                .estado("ACTIVA")
                .fuenteNormativa("Ordenanza MDQ")
                .versionTarifa(1)
                .build());
    }

    @Test
    @DisplayName("01. Configurar concepto de cobro comercial exitosamente")
    void testCrearConceptoCobro() {
        ConceptoCobroRequest req = ConceptoCobroRequest.builder()
                .codigo("SERV_AGUA_" + (System.currentTimeMillis() % 10000))
                .nombre("Servicio Común de Agua Potable")
                .descripcion("Cuota proporcional de consumo de agua")
                .tipoConcepto(TipoConceptoCobro.SERVICIO_MERCADO)
                .periodicidad(PeriodicidadConcepto.MENSUAL)
                .requiereAsignacion(false)
                .build();

        ConceptoCobroResponse resp = conceptoService.crear(req, "admin");

        assertThat(resp).isNotNull();
        assertThat(resp.getId()).isNotNull();
        assertThat(resp.getCodigo()).isEqualTo(req.getCodigo());
        assertThat(resp.getEstado()).isEqualTo("ACTIVO");
    }

    @Test
    @DisplayName("02. Registrar tarifa autorizada y rechazar montos negativos o fechas inválidas")
    void testValidacionTarifas() {
        // Rechazar monto negativo
        TarifaMercadoRequest reqNegativo = TarifaMercadoRequest.builder()
                .conceptoId(concepto.getId())
                .monto(new BigDecimal("-10.00"))
                .fechaInicioVigencia(LocalDate.now())
                .build();

        assertThatThrownBy(() -> tarifaService.crear(reqNegativo, "admin"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("no puede ser negativo");

        // Rechazar fin antes de inicio
        TarifaMercadoRequest reqFechas = TarifaMercadoRequest.builder()
                .conceptoId(concepto.getId())
                .monto(new BigDecimal("45.00"))
                .fechaInicioVigencia(LocalDate.now())
                .fechaFinVigencia(LocalDate.now().minusDays(5))
                .build();

        assertThatThrownBy(() -> tarifaService.crear(reqFechas, "admin"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("no puede ser anterior");
    }

    @Test
    @DisplayName("03. Versionar tarifa autorizada sin modificar retroactivamente la anterior")
    void testVersionamientoTarifa() {
        TarifaMercadoRequest nuevaVersionReq = TarifaMercadoRequest.builder()
                .conceptoId(concepto.getId())
                .monto(new BigDecimal("75.00"))
                .fechaInicioVigencia(LocalDate.now())
                .fuenteNormativa("Nueva Ordenanza 2026")
                .build();

        TarifaMercadoResponse version2 = tarifaService.actualizarConVersionamiento(tarifa.getId(), nuevaVersionReq, "admin");

        assertThat(version2.getVersionTarifa()).isEqualTo(2);
        assertThat(version2.getMonto()).isEqualByComparingTo("75.00");

        // Verificar que la versión anterior quedó como HISTORICA
        TarifaMercado anterior = tarifaRepository.findById(tarifa.getId()).orElseThrow();
        assertThat(anterior.getEstado()).isEqualTo("HISTORICA");
    }

    @Test
    @DisplayName("04. Generar obligación comercial con cuotas y verificar suma exacta de cuotas")
    void testGenerarObligacionConCuotas() {
        ObligacionCreateRequest req = ObligacionCreateRequest.builder()
                .comercianteId(comerciante.getId())
                .puestoId(puesto.getId())
                .conceptoId(concepto.getId())
                .periodoInicio(LocalDate.now())
                .periodoFin(LocalDate.now().plusMonths(3))
                .fechaVencimiento(LocalDate.now().plusDays(30))
                .montoOriginal(new BigDecimal("100.00"))
                .numeroCuotas(3) // 100 dividido entre 3: 33.33 + 33.33 + 33.34 = 100.00
                .build();

        ObligacionResponse resp = obligacionService.crear(req, "operador");

        assertThat(resp).isNotNull();
        assertThat(resp.getCodigoObligacion()).startsWith("OBL-");
        assertThat(resp.getEstado()).isEqualTo(EstadoObligacion.PENDIENTE);
        assertThat(resp.getSaldoPendiente()).isEqualByComparingTo("100.00");

        ObligacionDetailResponse detalle = obligacionService.obtenerDetalle(resp.getId());
        assertThat(detalle.getCuotas()).hasSize(3);

        BigDecimal sumaCuotas = detalle.getCuotas().stream()
                .map(CuotaResponse::getMontoProgramado)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        assertThat(sumaCuotas).isEqualByComparingTo("100.00");
    }

    @Test
    @DisplayName("05. Impedir duplicidad de obligación para mismo comerciante, puesto, concepto y período")
    void testPrevenirObligacionDuplicada() {
        LocalDate desde = LocalDate.of(2026, 5, 1);
        LocalDate hasta = LocalDate.of(2026, 5, 31);

        ObligacionCreateRequest req1 = ObligacionCreateRequest.builder()
                .comercianteId(comerciante.getId())
                .puestoId(puesto.getId())
                .conceptoId(concepto.getId())
                .periodoInicio(desde)
                .periodoFin(hasta)
                .montoOriginal(new BigDecimal("50.00"))
                .build();
        obligacionService.crear(req1, "operador");

        ObligacionCreateRequest req2 = ObligacionCreateRequest.builder()
                .comercianteId(comerciante.getId())
                .puestoId(puesto.getId())
                .conceptoId(concepto.getId())
                .periodoInicio(desde)
                .periodoFin(hasta)
                .montoOriginal(new BigDecimal("50.00"))
                .build();

        assertThatThrownBy(() -> obligacionService.crear(req2, "operador"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Ya existe una obligación comercial registrada");
    }

    @Test
    @DisplayName("06. Registrar pago parcial y pago completo actualizando saldos y estados")
    void testRegistrarPagoParcialYCompleto() {
        // Crear obligación de S/ 150.00
        ObligacionCreateRequest reqObl = ObligacionCreateRequest.builder()
                .comercianteId(comerciante.getId())
                .puestoId(puesto.getId())
                .conceptoId(concepto.getId())
                .montoOriginal(new BigDecimal("150.00"))
                .build();
        ObligacionResponse obl = obligacionService.crear(reqObl, "operador");

        // 1. Pago Parcial de S/ 50.00 -> Saldo restante: S/ 100.00
        PagoCreateRequest reqPago1 = PagoCreateRequest.builder()
                .comercianteId(comerciante.getId())
                .montoRecibido(new BigDecimal("50.00"))
                .medioPago(MedioPagoMercado.EFECTIVO)
                .aplicaciones(List.of(
                        AplicacionItemRequest.builder().obligacionId(obl.getId()).monto(new BigDecimal("50.00")).build()
                ))
                .build();
        PagoResponse p1 = pagoService.registrarPago(reqPago1, "caja", 1L);

        assertThat(p1.getCodigoPago()).startsWith("REC-");
        assertThat(p1.getMontoAplicado()).isEqualByComparingTo("50.00");

        ObligacionDetailResponse oblActualizada1 = obligacionService.obtenerDetalle(obl.getId());
        assertThat(oblActualizada1.getSaldoPendiente()).isEqualByComparingTo("100.00");
        assertThat(oblActualizada1.getEstado()).isEqualTo(EstadoObligacion.PARCIALMENTE_PAGADA);

        // 2. Pago Completo restante de S/ 100.00 -> Saldo: S/ 0.00, Estado: PAGADA
        PagoCreateRequest reqPago2 = PagoCreateRequest.builder()
                .comercianteId(comerciante.getId())
                .montoRecibido(new BigDecimal("100.00"))
                .medioPago(MedioPagoMercado.TRANSFERENCIA_BANCARIA)
                .referenciaOperacion("TRF-987654")
                .aplicaciones(List.of(
                        AplicacionItemRequest.builder().obligacionId(obl.getId()).monto(new BigDecimal("100.00")).build()
                ))
                .build();
        pagoService.registrarPago(reqPago2, "caja", 1L);

        ObligacionDetailResponse oblActualizada2 = obligacionService.obtenerDetalle(obl.getId());
        assertThat(oblActualizada2.getSaldoPendiente()).isEqualByComparingTo("0.00");
        assertThat(oblActualizada2.getEstado()).isEqualTo(EstadoObligacion.PAGADA);
    }

    @Test
    @DisplayName("07. Rechazar sobrepago superior al saldo pendiente de la obligación")
    void testRechazarSobrepago() {
        ObligacionCreateRequest reqObl = ObligacionCreateRequest.builder()
                .comercianteId(comerciante.getId())
                .puestoId(puesto.getId())
                .conceptoId(concepto.getId())
                .montoOriginal(new BigDecimal("80.00"))
                .build();
        ObligacionResponse obl = obligacionService.crear(reqObl, "operador");

        // Intentar pagar S/ 120.00 cuando el saldo es S/ 80.00
        PagoCreateRequest reqPago = PagoCreateRequest.builder()
                .comercianteId(comerciante.getId())
                .montoRecibido(new BigDecimal("120.00"))
                .medioPago(MedioPagoMercado.EFECTIVO)
                .aplicaciones(List.of(
                        AplicacionItemRequest.builder().obligacionId(obl.getId()).monto(new BigDecimal("120.00")).build()
                ))
                .build();

        assertThatThrownBy(() -> pagoService.registrarPago(reqPago, "caja", 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("supera el saldo pendiente");
    }

    @Test
    @DisplayName("08. Reversar pago restableciendo saldo pendiente y registrando auditoría")
    void testReversarPago() {
        ObligacionCreateRequest reqObl = ObligacionCreateRequest.builder()
                .comercianteId(comerciante.getId())
                .puestoId(puesto.getId())
                .conceptoId(concepto.getId())
                .montoOriginal(new BigDecimal("70.00"))
                .build();
        ObligacionResponse obl = obligacionService.crear(reqObl, "operador");

        // Pagar S/ 70.00
        PagoCreateRequest reqPago = PagoCreateRequest.builder()
                .comercianteId(comerciante.getId())
                .montoRecibido(new BigDecimal("70.00"))
                .medioPago(MedioPagoMercado.EFECTIVO)
                .aplicaciones(List.of(
                        AplicacionItemRequest.builder().obligacionId(obl.getId()).monto(new BigDecimal("70.00")).build()
                ))
                .build();
        PagoResponse pago = pagoService.registrarPago(reqPago, "caja", 1L);

        // Reversar el pago
        ReversoPagoRequest reqRev = ReversoPagoRequest.builder()
                .motivo("Error de digitación en ventanilla")
                .documentoSustento("MEMO-001-MDQ")
                .build();
        PagoResponse reversado = pagoService.reversarPago(pago.getId(), reqRev, "supervisor", 1L);

        assertThat(reversado.getEstado()).isEqualTo(EstadoPagoMercado.REVERSADO);

        // Obligación vuelve a PENDIENTE con saldo S/ 70.00
        ObligacionDetailResponse oblRevertida = obligacionService.obtenerDetalle(obl.getId());
        assertThat(oblRevertida.getSaldoPendiente()).isEqualByComparingTo("70.00");
        assertThat(oblRevertida.getEstado()).isEqualTo(EstadoObligacion.PENDIENTE);
    }

    @Test
    @DisplayName("09. Consultar cuenta corriente del comerciante con cargos y abonos")
    void testConsultarCuentaCorriente() {
        ObligacionCreateRequest reqObl = ObligacionCreateRequest.builder()
                .comercianteId(comerciante.getId())
                .puestoId(puesto.getId())
                .conceptoId(concepto.getId())
                .montoOriginal(new BigDecimal("90.00"))
                .build();
        ObligacionResponse obl = obligacionService.crear(reqObl, "operador");

        // Pagar S/ 40.00
        PagoCreateRequest reqPago = PagoCreateRequest.builder()
                .comercianteId(comerciante.getId())
                .montoRecibido(new BigDecimal("40.00"))
                .medioPago(MedioPagoMercado.EFECTIVO)
                .aplicaciones(List.of(
                        AplicacionItemRequest.builder().obligacionId(obl.getId()).monto(new BigDecimal("40.00")).build()
                ))
                .build();
        pagoService.registrarPago(reqPago, "caja", 1L);

        CuentaCorrienteResponse cc = cuentaCorrienteService.obtenerCuentaCorrienteComerciante(comerciante.getId());

        assertThat(cc).isNotNull();
        assertThat(cc.getComercianteNombre()).contains("Saturnino");
        assertThat(cc.getSaldoTotalPendiente()).isEqualByComparingTo("50.00");
        assertThat(cc.getMovimientos()).isNotEmpty();
    }
}
