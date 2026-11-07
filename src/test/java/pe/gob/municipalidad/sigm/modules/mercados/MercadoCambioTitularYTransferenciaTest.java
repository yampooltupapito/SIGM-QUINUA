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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MercadoCambioTitularYTransferenciaTest {

    @Autowired private CambioTitularService cambioTitularService;
    @Autowired private TransferenciaPuestoService transferenciaService;
    @Autowired private EvaluacionGestionOcupacionService evaluacionService;
    @Autowired private FormalizacionGestionOcupacionService formalizacionService;
    @Autowired private HistorialTitularidadService historialTitularidadService;

    @Autowired private SolicitudGestionOcupacionRepository solicitudRepository;
    @Autowired private ProcedimientoGestionOcupacionRepository procedimientoRepository;
    @Autowired private AsignacionPuestoRepository asignacionRepository;
    @Autowired private PuestoComercialRepository puestoRepository;
    @Autowired private MercadoMunicipalRepository mercadoRepository;
    @Autowired private ComercianteRepository comercianteRepository;
    @Autowired private PersonaRepository personaRepository;

    private MercadoMunicipal mercado;
    private PuestoComercial puesto;
    private Comerciante comercianteActual;
    private Comerciante comerciantePropuesto;
    private AsignacionPuesto asignacion;
    private ProcedimientoGestionOcupacion procCambio;
    private ProcedimientoGestionOcupacion procTransferenciaNoPermitida;
    private ProcedimientoGestionOcupacion procTransferenciaPermitida;

    @BeforeEach
    void setUp() {
        long ts = System.currentTimeMillis();

        mercado = mercadoRepository.save(MercadoMunicipal.builder()
                .nombre("Mercado Test Titularidad " + ts)
                .codigo("MKT-T-" + (ts % 10000))
                .direccion("Jr. Comercio 123")
                .estado(EstadoMercado.ACTIVO)
                .build());

        puesto = puestoRepository.save(PuestoComercial.builder()
                .mercado(mercado)
                .codigo("PT-" + (ts % 10000))
                .numero("PT-" + (ts % 10000))
                .areaMetrosCuadrados(new BigDecimal("15.00"))
                .estado(EstadoPuesto.OCUPADO)
                .build());

        Persona personaActual = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento("41" + String.format("%06d", (int)(ts % 1000000)))
                .nombres("Juan")
                .apellidoPaterno("Actual")
                .correo("actual" + ts + "@test.gob.pe")
                .estado("ACTIVO")
                .build());

        comercianteActual = comercianteRepository.save(Comerciante.builder()
                .persona(personaActual)
                .codigoComerciante("COM-ACT-" + (ts % 10000))
                .tipoComerciante(TipoComerciante.PERSONA_NATURAL)
                .estado(EstadoComerciante.ACTIVO)
                .build());

        Persona personaPropuesta = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento("42" + String.format("%06d", (int)(ts % 1000000)))
                .nombres("Maria")
                .apellidoPaterno("Propuesta")
                .correo("propuesta" + ts + "@test.gob.pe")
                .estado("ACTIVO")
                .build());

        comerciantePropuesto = comercianteRepository.save(Comerciante.builder()
                .persona(personaPropuesta)
                .codigoComerciante("COM-PRO-" + (ts % 10000))
                .tipoComerciante(TipoComerciante.PERSONA_NATURAL)
                .estado(EstadoComerciante.ACTIVO)
                .build());

        asignacion = asignacionRepository.save(AsignacionPuesto.builder()
                .codigoAsignacion("ASG-TIT-" + (ts % 10000))
                .puesto(puesto)
                .comerciante(comercianteActual)
                .fechaInicio(LocalDate.now().minusMonths(12))
                .fechaFin(LocalDate.now().plusMonths(6))
                .estado(EstadoAsignacion.VIGENTE)
                .build());

        procCambio = procedimientoRepository.findByCodigo("PROC-CAM-TIT").orElseGet(() ->
                procedimientoRepository.save(ProcedimientoGestionOcupacion.builder()
                        .codigo("PROC-CAM-TIT")
                        .nombre("Cambio de Titular por Sucesión o Incapacidad")
                        .tipoSolicitud(TipoSolicitudGestion.CAMBIO_TITULAR)
                        .baseNormativa("Reglamento de Mercados Art. 30")
                        .activo(true)
                        .permiteTransferencia(false)
                        .build()));

        procTransferenciaNoPermitida = procedimientoRepository.save(ProcedimientoGestionOcupacion.builder()
                .codigo("PROC-TRF-BLOCKED-" + ts)
                .nombre("Transferencia Denegada por Defecto")
                .tipoSolicitud(TipoSolicitudGestion.TRANSFERENCIA_AUTORIZADA)
                .baseNormativa("Ley N° 27972 Art. 83")
                .activo(true)
                .permiteTransferencia(false) // Bloqueado por regla de no transferibilidad libre
                .build());

        procTransferenciaPermitida = procedimientoRepository.save(ProcedimientoGestionOcupacion.builder()
                .codigo("PROC-TRF-ENABLED-" + ts)
                .nombre("Transferencia Excepcional Autorizada por Ordenanza")
                .tipoSolicitud(TipoSolicitudGestion.TRANSFERENCIA_AUTORIZADA)
                .baseNormativa("Ordenanza Municipal Específica N° 012-2026")
                .activo(true)
                .permiteTransferencia(true) // Excepcionalmente habilitada
                .build());
    }

    @Test
    @DisplayName("Debe rechazar cambio de titular si el titular actual y propuesto son la misma persona")
    void testRechazarCambioTitularMismaPersona() {
        CambioTitularRequest req = CambioTitularRequest.builder()
                .puestoId(puesto.getId())
                .asignacionId(asignacion.getId())
                .comercianteActualId(comercianteActual.getId())
                .comerciantePropuestoId(comercianteActual.getId()) // Mismo comerciante
                .procedimientoId(procCambio.getId())
                .motivo("Mismo comerciante no válido")
                .build();

        assertThatThrownBy(() -> cambioTitularService.solicitarCambioTitular(req, "admin"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("distinto");
    }

    @Test
    @DisplayName("Debe rechazar transferencia si el procedimiento municipal no permite transferencias")
    void testRechazarTransferenciaNoPermitida() {
        TransferenciaRequest req = TransferenciaRequest.builder()
                .puestoId(puesto.getId())
                .asignacionId(asignacion.getId())
                .comercianteActualId(comercianteActual.getId())
                .comerciantePropuestoId(comerciantePropuesto.getId())
                .procedimientoId(procTransferenciaNoPermitida.getId())
                .motivo("Intento de transferencia no habilitada normativamente")
                .build();

        assertThatThrownBy(() -> transferenciaService.solicitarTransferencia(req, "operador"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("no autoriza transferencias");
    }

    @Test
    @DisplayName("Debe admitir solicitud de transferencia cuando el procedimiento expresamente lo habilita")
    void testAdmitirTransferenciaHabilitada() {
        TransferenciaRequest req = TransferenciaRequest.builder()
                .puestoId(puesto.getId())
                .asignacionId(asignacion.getId())
                .comercianteActualId(comercianteActual.getId())
                .comerciantePropuestoId(comerciantePropuesto.getId())
                .procedimientoId(procTransferenciaPermitida.getId())
                .motivo("Transferencia autorizada por causal sobreviniente legal")
                .build();

        SolicitudGestionOcupacionResponse resp = transferenciaService.solicitarTransferencia(req, "operador");

        assertThat(resp).isNotNull();
        assertThat(resp.getTipoSolicitud()).isEqualTo(TipoSolicitudGestion.TRANSFERENCIA_AUTORIZADA);
        assertThat(resp.getComerciantePropuestoId()).isEqualTo(comerciantePropuesto.getId());
    }

    @Test
    @DisplayName("Cambio de Titular formalizado debe cerrar asignación previa, crear nueva y registrar historial inmutable")
    void testFormalizacionCambioTitularPreservaInmutabilidadYRegistraHistorial() {
        // 1. Crear solicitud de cambio de titular
        CambioTitularRequest req = CambioTitularRequest.builder()
                .puestoId(puesto.getId())
                .asignacionId(asignacion.getId())
                .comercianteActualId(comercianteActual.getId())
                .comerciantePropuestoId(comerciantePropuesto.getId())
                .procedimientoId(procCambio.getId())
                .motivo("Sucesión por causa justificada comprobada")
                .build();
        SolicitudGestionOcupacionResponse sol = cambioTitularService.solicitarCambioTitular(req, "operador");

        // 2. Solicitud ya se encuentra presentada formalmente
        assertThat(sol.getEstado()).isEqualTo(EstadoSolicitudGestion.PRESENTADA);

        // 3. Evaluar favorable
        EvaluacionGestionRequest evalReq = EvaluacionGestionRequest.builder()
                .sentidoEvaluacion(SentidoEvaluacionGestion.FAVORABLE)
                .informeTecnico("Cumple condiciones sucesorias y no posee impedimento legal")
                .observaciones("Evaluación favorable sin observaciones")
                .build();
        evaluacionService.evaluarSolicitud(sol.getId(), evalReq, "evaluador_juridico");

        // 4. Aprobar
        AprobacionGestionRequest aprReq = AprobacionGestionRequest.builder()
                .motivo("Aprobado cambio de titularidad de puesto")
                .build();
        evaluacionService.aprobarSolicitud(sol.getId(), aprReq, "gerente_municipal");

        // 5. Formalizar cambio
        FormalizacionGestionRequest formReq = FormalizacionGestionRequest.builder()
                .fechaEfectiva(LocalDate.now())
                .nuevaFechaFin(LocalDate.now().plusYears(1))
                .motivo("ACTO-FORM-TIT-2026-001: Cambio formalizado en registro oficial de puestos")
                .build();

        SolicitudGestionOcupacionResponse formalizada = formalizacionService.formalizarSolicitud(sol.getId(), formReq, "jefe_mercados");

        assertThat(formalizada.getEstado()).isEqualTo(EstadoSolicitudGestion.FORMALIZADA);
        assertThat(formalizada.getAsignacionNuevaId()).isNotNull();

        // 6. Verificar que la asignación histórica NO fue editada (mantiene titular original)
        AsignacionPuesto asigAnterior = asignacionRepository.findById(asignacion.getId()).orElseThrow();
        assertThat(asigAnterior.getComerciante().getId()).isEqualTo(comercianteActual.getId());
        assertThat(asigAnterior.getEstado()).isEqualTo(EstadoAsignacion.FINALIZADA);

        // 7. Verificar que la nueva asignación pertenece al comerciante propuesto y al mismo puesto
        AsignacionPuesto asigNueva = asignacionRepository.findById(formalizada.getAsignacionNuevaId()).orElseThrow();
        assertThat(asigNueva.getComerciante().getId()).isEqualTo(comerciantePropuesto.getId());
        assertThat(asigNueva.getPuesto().getId()).isEqualTo(puesto.getId());
        assertThat(asigNueva.getEstado()).isEqualTo(EstadoAsignacion.VIGENTE);

        // 8. Verificar que se grabó la trazabilidad inmutable en HistorialTitularidadPuesto
        List<HistorialTitularidadResponse> historial = historialTitularidadService.listarPorPuesto(puesto.getId());
        assertThat(historial).isNotEmpty();
        HistorialTitularidadResponse ultimo = historial.get(0);
        assertThat(ultimo.getTitularAnteriorId()).isEqualTo(comercianteActual.getId());
        assertThat(ultimo.getTitularNuevoId()).isEqualTo(comerciantePropuesto.getId());
    }

    @Test
    @DisplayName("Debe impedir doble formalización sobre la misma solicitud (Idempotencia y concurrencia)")
    void testImpedirDobleFormalizacion() {
        CambioTitularRequest req = CambioTitularRequest.builder()
                .puestoId(puesto.getId())
                .asignacionId(asignacion.getId())
                .comercianteActualId(comercianteActual.getId())
                .comerciantePropuestoId(comerciantePropuesto.getId())
                .procedimientoId(procCambio.getId())
                .motivo("Formalización simple")
                .build();
        SolicitudGestionOcupacionResponse sol = cambioTitularService.solicitarCambioTitular(req, "operador");

        evaluacionService.aprobarSolicitud(sol.getId(), AprobacionGestionRequest.builder()
                .motivo("Aprobado")
                .build(), "autoridad");

        FormalizacionGestionRequest formReq = FormalizacionGestionRequest.builder()
                .fechaEfectiva(LocalDate.now())
                .nuevaFechaFin(LocalDate.now().plusMonths(6))
                .motivo("ACTO-01")
                .build();

        // Primera formalización exitosa
        formalizacionService.formalizarSolicitud(sol.getId(), formReq, "admin");

        // Segunda formalización debe fallar por estado incompatible
        assertThatThrownBy(() -> formalizacionService.formalizarSolicitud(sol.getId(), formReq, "admin"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("APROBADA");
    }
}
