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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MercadoRenovacionServiceTest {

    @Autowired private RenovacionOcupacionService renovacionService;
    @Autowired private EvaluacionGestionOcupacionService evaluacionService;
    @Autowired private FormalizacionGestionOcupacionService formalizacionService;

    @Autowired private SolicitudGestionOcupacionRepository solicitudRepository;
    @Autowired private ProcedimientoGestionOcupacionRepository procedimientoRepository;
    @Autowired private AsignacionPuestoRepository asignacionRepository;
    @Autowired private PuestoComercialRepository puestoRepository;
    @Autowired private MercadoMunicipalRepository mercadoRepository;
    @Autowired private ComercianteRepository comercianteRepository;
    @Autowired private PersonaRepository personaRepository;

    private MercadoMunicipal mercado;
    private PuestoComercial puesto;
    private Comerciante comerciante;
    private AsignacionPuesto asignacion;
    private ProcedimientoGestionOcupacion procRenovacion;

    @BeforeEach
    void setUp() {
        long ts = System.currentTimeMillis();

        mercado = mercadoRepository.save(MercadoMunicipal.builder()
                .nombre("Mercado Test Renovacion " + ts)
                .codigo("MKT-" + (ts % 10000))
                .direccion("Av. Central s/n")
                .estado(EstadoMercado.ACTIVO)
                .build());

        puesto = puestoRepository.save(PuestoComercial.builder()
                .mercado(mercado)
                .codigo("P-" + (ts % 10000))
                .numero("P-" + (ts % 10000))
                .areaMetrosCuadrados(new BigDecimal("12.50"))
                .estado(EstadoPuesto.OCUPADO)
                .build());

        Persona persona = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento("47" + String.format("%06d", (int)(ts % 1000000)))
                .nombres("Titular")
                .apellidoPaterno("Renovacion")
                .correo("titular" + ts + "@test.gob.pe")
                .estado("ACTIVO")
                .build());

        comerciante = comercianteRepository.save(Comerciante.builder()
                .persona(persona)
                .codigoComerciante("COM-" + (ts % 10000))
                .tipoComerciante(TipoComerciante.PERSONA_NATURAL)
                .estado(EstadoComerciante.ACTIVO)
                .build());

        asignacion = asignacionRepository.save(AsignacionPuesto.builder()
                .codigoAsignacion("ASG-TEST-" + (ts % 10000))
                .puesto(puesto)
                .comerciante(comerciante)
                .fechaInicio(LocalDate.now().minusMonths(6))
                .fechaFin(LocalDate.now().plusMonths(1))
                .estado(EstadoAsignacion.VIGENTE)
                .build());

        procRenovacion = procedimientoRepository.findByCodigo("PROC-REN-ASG").orElseGet(() ->
                procedimientoRepository.save(ProcedimientoGestionOcupacion.builder()
                        .codigo("PROC-REN-ASG")
                        .nombre("Renovación de Asignación de Puesto")
                        .tipoSolicitud(TipoSolicitudGestion.RENOVACION_ASIGNACION)
                        .baseNormativa("Reglamento de Mercados Art. 25")
                        .activo(true)
                        .permiteTransferencia(false)
                        .build()));
    }

    @Test
    @DisplayName("Debe crear solicitud de renovación con código correlativo SOL-MER-YYYY-NNNNNN")
    void testCrearRenovacionExitosamente() {
        RenovacionRequest request = RenovacionRequest.builder()
                .asignacionId(asignacion.getId())
                .tipoSolicitud(TipoSolicitudGestion.RENOVACION_ASIGNACION)
                .procedimientoId(procRenovacion.getId())
                .fechaInicioSolicitada(LocalDate.now().plusMonths(1).plusDays(1))
                .fechaFinSolicitada(LocalDate.now().plusYears(1))
                .motivo("Continuidad de actividades comerciales de abarrotes")
                .build();

        SolicitudGestionOcupacionResponse resp = renovacionService.solicitarRenovacion(request, "admin_user");

        assertThat(resp).isNotNull();
        assertThat(resp.getId()).isNotNull();
        assertThat(resp.getCodigoSolicitud()).startsWith("SOL-MER-");
        assertThat(resp.getTipoSolicitud()).isEqualTo(TipoSolicitudGestion.RENOVACION_ASIGNACION);
        assertThat(resp.getEstado()).isEqualTo(EstadoSolicitudGestion.PRESENTADA);
        assertThat(resp.getMercadoNombre()).isEqualTo(mercado.getNombre());
        assertThat(resp.getPuestoNumero()).isEqualTo(puesto.getNumero());
        assertThat(resp.getComercianteActualNombre()).contains("Titular");
    }

    @Test
    @DisplayName("Debe rechazar renovación si la asignación no existe o fechas son incoherentes")
    void testRechazarRenovacionInvalida() {
        RenovacionRequest requestInvalido = RenovacionRequest.builder()
                .asignacionId(UUID.randomUUID())
                .tipoSolicitud(TipoSolicitudGestion.RENOVACION_ASIGNACION)
                .procedimientoId(procRenovacion.getId())
                .fechaInicioSolicitada(LocalDate.now().plusMonths(2))
                .fechaFinSolicitada(LocalDate.now().plusMonths(1)) // Incompatible
                .motivo("Prueba invalida")
                .build();

        assertThatThrownBy(() -> renovacionService.solicitarRenovacion(requestInvalido, "admin"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Flujo completo: Presentar -> Observar -> Subsanar -> Evaluar Favorable -> Aprobar -> Formalizar")
    void testFlujoCompletoRenovacion() {
        // 1. Crear
        RenovacionRequest req = RenovacionRequest.builder()
                .asignacionId(asignacion.getId())
                .tipoSolicitud(TipoSolicitudGestion.RENOVACION_ASIGNACION)
                .procedimientoId(procRenovacion.getId())
                .fechaInicioSolicitada(LocalDate.now().plusMonths(1).plusDays(1))
                .fechaFinSolicitada(LocalDate.now().plusYears(1))
                .motivo("Renovación de ocupación habitual")
                .build();
        SolicitudGestionOcupacionResponse creada = renovacionService.solicitarRenovacion(req, "operador");

        // Regresar a BORRADOR para verificar la transición formal del flujo de presentación
        SolicitudGestionOcupacion ent = solicitudRepository.findById(creada.getId()).orElseThrow();
        ent.setEstado(EstadoSolicitudGestion.BORRADOR);
        solicitudRepository.save(ent);

        // 2. Presentar
        SolicitudGestionOcupacionResponse presentada = evaluacionService.presentarSolicitud(creada.getId(), "operador");
        assertThat(presentada.getEstado()).isEqualTo(EstadoSolicitudGestion.PRESENTADA);

        // 3. Observar
        ObservacionGestionRequest obsReq = ObservacionGestionRequest.builder()
                .descripcion("Falta adjuntar constancia de no adeudo actualizada")
                .requisitoRelacionado("Constancia No Adeudo")
                .build();
        ObservacionGestionResponse obs = evaluacionService.registrarObservacion(creada.getId(), obsReq, "inspector");
        assertThat(obs.getId()).isNotNull();

        SolicitudGestionOcupacion sObs = solicitudRepository.findById(creada.getId()).orElseThrow();
        assertThat(sObs.getEstado()).isEqualTo(EstadoSolicitudGestion.OBSERVADA);

        // 4. Subsanar
        SubsanacionGestionRequest subReq = SubsanacionGestionRequest.builder()
                .observacionId(obs.getId())
                .descripcion("Se anexa constancia de no adeudo N° 2026-99")
                .build();
        SubsanacionGestionResponse sub = evaluacionService.registrarSubsanacion(creada.getId(), subReq, "operador");
        assertThat(sub.getId()).isNotNull();

        SolicitudGestionOcupacion sSub = solicitudRepository.findById(creada.getId()).orElseThrow();
        assertThat(sSub.getEstado()).isEqualTo(EstadoSolicitudGestion.EN_EVALUACION);

        // 5. Evaluar
        EvaluacionGestionRequest evalReq = EvaluacionGestionRequest.builder()
                .sentidoEvaluacion(SentidoEvaluacionGestion.FAVORABLE)
                .informeTecnico("Cumple cabalmente con todos los requisitos y pagos al día.")
                .observaciones("Evaluación técnica favorable")
                .build();
        evaluacionService.evaluarSolicitud(creada.getId(), evalReq, "evaluador");

        SolicitudGestionOcupacion sEval = solicitudRepository.findById(creada.getId()).orElseThrow();
        assertThat(sEval.getEstado()).isEqualTo(EstadoSolicitudGestion.EN_EVALUACION);

        // 6. Aprobar
        AprobacionGestionRequest aprReq = AprobacionGestionRequest.builder()
                .motivo("Aprobado conforme al informe técnico legal favorable")
                .build();
        evaluacionService.aprobarSolicitud(creada.getId(), aprReq, "subgerente");

        SolicitudGestionOcupacion sApr = solicitudRepository.findById(creada.getId()).orElseThrow();
        assertThat(sApr.getEstado()).isEqualTo(EstadoSolicitudGestion.APROBADA);

        // 7. Formalizar
        FormalizacionGestionRequest formReq = FormalizacionGestionRequest.builder()
                .fechaEfectiva(LocalDate.now().plusMonths(1).plusDays(1))
                .nuevaFechaFin(LocalDate.now().plusYears(1))
                .motivo("Renovación formalizada y vigente en sistema")
                .build();
        SolicitudGestionOcupacionResponse formalizada = formalizacionService.formalizarSolicitud(creada.getId(), formReq, "administrador");

        assertThat(formalizada.getEstado()).isEqualTo(EstadoSolicitudGestion.FORMALIZADA);
        assertThat(formalizada.getAsignacionOrigenId()).isEqualTo(asignacion.getId());

        // Verificar que la asignación original extendió su vigencia y mantiene su titular
        AsignacionPuesto asigOrigen = asignacionRepository.findById(asignacion.getId()).orElseThrow();
        assertThat(asigOrigen.getComerciante().getId()).isEqualTo(comerciante.getId());
        assertThat(asigOrigen.getEstado()).isEqualTo(EstadoAsignacion.VIGENTE);
        assertThat(asigOrigen.getFechaFin()).isEqualTo(formReq.getNuevaFechaFin());
    }
}
