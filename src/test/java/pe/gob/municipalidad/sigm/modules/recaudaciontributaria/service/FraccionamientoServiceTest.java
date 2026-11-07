package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.fraccionamiento.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FraccionamientoServiceTest {

    @Mock
    private SolicitudFraccionamientoTributarioRepository solicitudRepository;
    @Mock
    private DetalleSolicitudFraccionamientoRepository detalleRepository;
    @Mock
    private ProgramaFraccionamientoTributarioRepository programaRepository;
    @Mock
    private ContribuyenteTributarioRepository contribuyenteRepository;
    @Mock
    private ObligacionTributariaRepository obligacionRepository;
    @Mock
    private CronogramaFraccionamientoTributarioRepository cronogramaRepository;
    @Mock
    private CuotaFraccionamientoTributarioRepository cuotaRepository;
    @Mock
    private ConvenioFraccionamientoTributarioRepository convenioRepository;
    @Mock
    private IncidenciaConvenioFraccionamientoRepository incidenciaRepository;
    @Mock
    private ElegibilidadFraccionamientoService elegibilidadService;
    @Mock
    private SimulacionFraccionamientoService simulacionService;
    @Mock
    private SeguimientoDeudaTributariaRepository seguimientoDeudaRepository;
    @Mock
    private ParametroFraccionamientoTributarioRepository parametroRepository;

    @InjectMocks
    private SolicitudFraccionamientoService solicitudService;

    private ConvenioFraccionamientoService convenioService;
    private AplicacionPagoFraccionamientoService aplicacionPagoService;

    private Usuario usuario;
    private ContribuyenteTributario contribuyente;
    private ProgramaFraccionamientoTributario programa;
    private ObligacionTributaria obligacion;
    private SolicitudFraccionamientoTributario solicitud;
    private CronogramaFraccionamientoTributario cronograma;

    @BeforeEach
    void setUp() {
        convenioService = new ConvenioFraccionamientoService(
                convenioRepository,
                solicitudRepository,
                detalleRepository,
                cronogramaRepository,
                cuotaRepository,
                incidenciaRepository,
                obligacionRepository,
                seguimientoDeudaRepository,
                null, // documentoRepository
                parametroRepository
        );

        aplicacionPagoService = new AplicacionPagoFraccionamientoService(
                cuotaRepository,
                convenioRepository,
                incidenciaRepository,
                detalleRepository,
                obligacionRepository
        );

        usuario = Usuario.builder()
                .id(1L)
                .username("tributacion.user")
                .build();

        contribuyente = ContribuyenteTributario.builder()
                .id(UUID.randomUUID())
                .codigoContribuyente("CONT-0001")
                .nombres("Juan Carlos")
                .primerApellido("Quispe")
                .segundoApellido("Huamán")
                .nombreRazonSocial("Juan Carlos Quispe Huamán")
                .tipoDocumento("DNI")
                .numeroDocumento("44556677")
                .build();

        programa = ProgramaFraccionamientoTributario.builder()
                .id(UUID.randomUUID())
                .codigo("PROG-FRAC-2026-01")
                .nombre("Programa General 2026")
                .estado(EstadoProgramaFraccionamiento.ACTIVO)
                .fechaInicioVigencia(LocalDate.of(2026, 1, 1))
                .build();

        obligacion = ObligacionTributaria.builder()
                .id(UUID.randomUUID())
                .codigoObligacion("OBL-2026-0001")
                .contribuyente(contribuyente)
                .estado(EstadoObligacionTributaria.VIGENTE)
                .importeDeterminado(new BigDecimal("1200.00"))
                .saldo(new BigDecimal("1200.00"))
                .cuotaNumero(1)
                .build();

        solicitud = SolicitudFraccionamientoTributario.builder()
                .id(UUID.randomUUID())
                .codigoSolicitud("SOL-FRAC-2026-0001")
                .contribuyente(contribuyente)
                .programa(programa)
                .estado(EstadoSolicitudFraccionamiento.PRESENTADA)
                .montoTotalDeuda(new BigDecimal("1200.00"))
                .montoCuotaInicial(new BigDecimal("120.00"))
                .numeroCuotasPropuestas(6)
                .motivo("Dificultad económica temporal")
                .build();

        cronograma = CronogramaFraccionamientoTributario.builder()
                .id(UUID.randomUUID())
                .solicitud(solicitud)
                .versionCronograma(1)
                .importeBase(new BigDecimal("1200.00"))
                .importeCuotaInicial(new BigDecimal("120.00"))
                .importeFinanciado(new BigDecimal("1080.00"))
                .importeAccesorios(new BigDecimal("27.24"))
                .importeTotal(new BigDecimal("1107.24"))
                .cuotas(new ArrayList<>())
                .build();
    }

    @Test
    @DisplayName("Crear Solicitud - Validar flujo exitoso y generación de cronograma")
    void testCrearSolicitudExitosa() {
        SolicitudFraccionamientoRequest req = SolicitudFraccionamientoRequest.builder()
                .contribuyenteId(contribuyente.getId())
                .programaId(programa.getId())
                .motivo("Dificultad económica temporal")
                .numeroCuotasPropuestas(6)
                .montoCuotaInicialPropuesta(new BigDecimal("120.00"))
                .obligacionIds(List.of(obligacion.getId()))
                .build();

        when(contribuyenteRepository.findById(contribuyente.getId())).thenReturn(Optional.of(contribuyente));
        when(programaRepository.findById(programa.getId())).thenReturn(Optional.of(programa));
        when(obligacionRepository.findById(obligacion.getId())).thenReturn(Optional.of(obligacion));
        when(elegibilidadService.evaluarObligacion(any(UUID.class)))
                .thenReturn(new ElegibilidadFraccionamientoService.ResultadoElegibilidad(true, "Obligación conforme"));
        when(solicitudRepository.save(any(SolicitudFraccionamientoTributario.class))).thenAnswer(inv -> inv.getArgument(0));

        SimulacionFraccionamientoResponse simResponse = SimulacionFraccionamientoResponse.builder()
                .importeBase(new BigDecimal("1200.00"))
                .cuotaInicial(new BigDecimal("120.00"))
                .importeFinanciado(new BigDecimal("1080.00"))
                .totalIntereses(new BigDecimal("27.24"))
                .importeTotalFinanciado(new BigDecimal("1107.24"))
                .numeroCuotas(6)
                .cuotas(List.of(
                        CuotaSimuladaDto.builder()
                                .numeroCuota(1)
                                .fechaVencimiento(LocalDate.now().plusDays(30))
                                .capital(new BigDecimal("180.00"))
                                .interes(new BigDecimal("7.78"))
                                .totalCuota(new BigDecimal("187.78"))
                                .saldoRestante(new BigDecimal("900.00"))
                                .build()
                ))
                .build();

        when(simulacionService.simular(any(SimulacionFraccionamientoRequest.class)))
                .thenReturn(simResponse);
        when(cronogramaRepository.save(any(CronogramaFraccionamientoTributario.class))).thenAnswer(inv -> inv.getArgument(0));

        SolicitudFraccionamientoResponse resp = solicitudService.crearSolicitud(req, usuario);

        assertThat(resp).isNotNull();
        assertThat(resp.getEstado()).isEqualTo(EstadoSolicitudFraccionamiento.PRESENTADA);
        assertThat(resp.getMontoTotalDeuda()).isEqualByComparingTo("1200.00");
        verify(solicitudRepository).save(any(SolicitudFraccionamientoTributario.class));
        verify(cronogramaRepository).save(any(CronogramaFraccionamientoTributario.class));
    }

    @Test
    @DisplayName("Evaluar Solicitud - Transición exitosa a EN_EVALUACION")
    void testEvaluarSolicitud() {
        when(solicitudRepository.findById(solicitud.getId())).thenReturn(Optional.of(solicitud));
        when(solicitudRepository.save(any(SolicitudFraccionamientoTributario.class))).thenAnswer(inv -> inv.getArgument(0));

        EvaluarSolicitudRequest req = EvaluarSolicitudRequest.builder()
                .observaciones("Evaluación técnica conforme al reglamento")
                .conforme(true)
                .build();

        SolicitudFraccionamientoResponse resp = solicitudService.evaluarSolicitud(solicitud.getId(), req, usuario);

        assertThat(resp.getEstado()).isEqualTo(EstadoSolicitudFraccionamiento.EN_EVALUACION);
        assertThat(solicitud.getEvaluador()).isEqualTo(usuario);
    }

    @Test
    @DisplayName("Observar Solicitud - Transición a OBSERVADA con motivo")
    void testObservarSolicitud() {
        when(solicitudRepository.findById(solicitud.getId())).thenReturn(Optional.of(solicitud));
        when(solicitudRepository.save(any(SolicitudFraccionamientoTributario.class))).thenAnswer(inv -> inv.getArgument(0));

        ObservarSolicitudRequest req = ObservarSolicitudRequest.builder()
                .observacion("Falta adjuntar copia de documento de identidad del cónyuge")
                .build();

        SolicitudFraccionamientoResponse resp = solicitudService.observarSolicitud(solicitud.getId(), req, usuario);

        assertThat(resp.getEstado()).isEqualTo(EstadoSolicitudFraccionamiento.OBSERVADA);
        assertThat(solicitud.getObservacionEvaluacion()).contains("Falta adjuntar copia");
    }

    @Test
    @DisplayName("Subsanar Solicitud - Transición a SUBSANADA")
    void testSubsanarSolicitud() {
        solicitud.setEstado(EstadoSolicitudFraccionamiento.OBSERVADA);

        when(solicitudRepository.findById(solicitud.getId())).thenReturn(Optional.of(solicitud));
        when(solicitudRepository.save(any(SolicitudFraccionamientoTributario.class))).thenAnswer(inv -> inv.getArgument(0));

        SubsanarSolicitudRequest req = SubsanarSolicitudRequest.builder()
                .detalleSubsanacion("Se adjunta DNI del cónyuge debidamente autenticado")
                .build();

        SolicitudFraccionamientoResponse resp = solicitudService.subsanarSolicitud(solicitud.getId(), req, usuario);

        assertThat(resp.getEstado()).isEqualTo(EstadoSolicitudFraccionamiento.SUBSANADA);
    }

    @Test
    @DisplayName("Aprobar Convenio - Genera convenio formal y actualiza obligaciones a FRACCIONADA")
    void testAprobarConvenio() {
        solicitud.setEstado(EstadoSolicitudFraccionamiento.EN_EVALUACION);

        when(solicitudRepository.findById(solicitud.getId())).thenReturn(Optional.of(solicitud));
        when(cronogramaRepository.findBySolicitudIdOrderByVersionCronogramaDesc(solicitud.getId()))
                .thenReturn(List.of(cronograma));
        when(convenioRepository.save(any(ConvenioFraccionamientoTributario.class))).thenAnswer(inv -> {
            ConvenioFraccionamientoTributario c = inv.getArgument(0);
            c.setId(UUID.randomUUID());
            return c;
        });

        DetalleSolicitudFraccionamiento det = DetalleSolicitudFraccionamiento.builder()
                .id(UUID.randomUUID())
                .solicitud(solicitud)
                .obligacion(obligacion)
                .saldoEvaluado(obligacion.getSaldo())
                .build();
        when(detalleRepository.findBySolicitudId(solicitud.getId())).thenReturn(List.of(det));

        AprobarConvenioRequest req = AprobarConvenioRequest.builder()
                .resolucionAprobacion("RES-GER-TRIB-001-2026")
                .fechaInicioVigencia(LocalDate.now())
                .observaciones("Aprobación conforme a Ley Orgánica de Municipalidades")
                .build();

        ConvenioFraccionamientoResponse resp = convenioService.aprobarConvenio(solicitud.getId(), req, usuario);

        assertThat(resp).isNotNull();
        assertThat(resp.getEstado()).isEqualTo(EstadoConvenioFraccionamiento.VIGENTE);
        assertThat(solicitud.getEstado()).isEqualTo(EstadoSolicitudFraccionamiento.APROBADA);
        assertThat(obligacion.getEstado()).isEqualTo(EstadoObligacionTributaria.FRACCIONADA);

        verify(obligacionRepository).save(obligacion);
        verify(incidenciaRepository).save(any(IncidenciaConvenioFraccionamiento.class));
    }

    @Test
    @DisplayName("Aplicar Pago - Pago parcial y pago total de cuota")
    void testAplicarPagoACuota() {
        ConvenioFraccionamientoTributario convenio = ConvenioFraccionamientoTributario.builder()
                .id(UUID.randomUUID())
                .codigoConvenio("CONV-2026-0001")
                .solicitud(solicitud)
                .cronograma(cronograma)
                .estado(EstadoConvenioFraccionamiento.VIGENTE)
                .build();

        CuotaFraccionamientoTributario cuota = CuotaFraccionamientoTributario.builder()
                .id(UUID.randomUUID())
                .cronograma(cronograma)
                .numeroCuota(1)
                .fechaVencimiento(LocalDate.now().plusDays(30))
                .importeCapital(new BigDecimal("180.00"))
                .importeInteres(new BigDecimal("10.00"))
                .importeTotal(new BigDecimal("190.00"))
                .importePagado(BigDecimal.ZERO)
                .saldo(new BigDecimal("190.00"))
                .estado(EstadoCuotaFraccionamiento.PENDIENTE)
                .build();

        when(cuotaRepository.findById(cuota.getId())).thenReturn(Optional.of(cuota));
        when(convenioRepository.findByCronogramaId(cronograma.getId())).thenReturn(Optional.of(convenio));
        when(cuotaRepository.findByCronogramaIdOrderByNumeroCuotaAsc(cronograma.getId())).thenReturn(List.of(cuota));

        // 1. Pago Parcial
        aplicacionPagoService.aplicarPagoACuota(cuota.getId(), new BigDecimal("100.00"), usuario);

        assertThat(cuota.getEstado()).isEqualTo(EstadoCuotaFraccionamiento.PARCIALMENTE_PAGADA);
        assertThat(cuota.getImportePagado()).isEqualByComparingTo("100.00");
        assertThat(cuota.getSaldo()).isEqualByComparingTo("90.00");

        // 2. Pago Restante (Cancelación de la cuota única -> culminación del convenio)
        DetalleSolicitudFraccionamiento det = DetalleSolicitudFraccionamiento.builder()
                .id(UUID.randomUUID())
                .solicitud(solicitud)
                .obligacion(obligacion)
                .saldoEvaluado(obligacion.getSaldo())
                .build();
        when(detalleRepository.findBySolicitudId(solicitud.getId())).thenReturn(List.of(det));

        aplicacionPagoService.aplicarPagoACuota(cuota.getId(), new BigDecimal("90.00"), usuario);

        assertThat(cuota.getEstado()).isEqualTo(EstadoCuotaFraccionamiento.PAGADA);
        assertThat(cuota.getSaldo()).isEqualByComparingTo("0.00");
        assertThat(convenio.getEstado()).isEqualTo(EstadoConvenioFraccionamiento.CUMPLIDO);
        assertThat(obligacion.getEstado()).isEqualTo(EstadoObligacionTributaria.PAGADA);
        assertThat(obligacion.getSaldo()).isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("Rechazar Solicitud - Transición a RECHAZADA con motivo formal")
    void testRechazarSolicitud() {
        solicitud.setEstado(EstadoSolicitudFraccionamiento.EN_EVALUACION);
        when(solicitudRepository.findById(solicitud.getId())).thenReturn(Optional.of(solicitud));
        when(solicitudRepository.save(any(SolicitudFraccionamientoTributario.class))).thenAnswer(inv -> inv.getArgument(0));

        SolicitudFraccionamientoResponse resp = solicitudService.rechazarSolicitud(solicitud.getId(), "No acredita titularidad sobre el predio gravado", usuario);

        assertThat(resp.getEstado()).isEqualTo(EstadoSolicitudFraccionamiento.RECHAZADA);
        assertThat(solicitud.getObservacionEvaluacion()).contains("No acredita titularidad");
    }

    @Test
    @DisplayName("No permitir pagar un importe mayor al saldo de la cuota")
    void testPagoExcedeSaldoFalla() {
        CuotaFraccionamientoTributario cuota = CuotaFraccionamientoTributario.builder()
                .id(UUID.randomUUID())
                .cronograma(cronograma)
                .numeroCuota(1)
                .importeTotal(new BigDecimal("190.00"))
                .importePagado(BigDecimal.ZERO)
                .saldo(new BigDecimal("190.00"))
                .estado(EstadoCuotaFraccionamiento.PENDIENTE)
                .build();

        when(cuotaRepository.findById(cuota.getId())).thenReturn(Optional.of(cuota));

        assertThatThrownBy(() -> aplicacionPagoService.aplicarPagoACuota(cuota.getId(), new BigDecimal("200.00"), usuario))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("excede el saldo de la cuota");
    }
}
