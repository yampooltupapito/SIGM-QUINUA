package pe.gob.municipalidad.sigm.modules.serenazgo.dto.supervision;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndicadoresSupervisionResponse {

    // Incidencias
    private long incidenciasRegistradasPeriodo;
    private long incidenciasPendientesRevision;
    private long incidenciasEnAtencion;
    private long incidenciasCerradas;

    // Alertas Ciudadanas
    private long alertasPendientes;
    private long alertasEnEvaluacion;

    // Atenciones de Serenazgo
    private long atencionesEnCurso;
    private long atencionesEnEspera;
    private long atencionesDespachadasSinConfirmar;

    // Intervenciones
    private long intervencionesPendientesValidacion;
    private long intervencionesObservadas;

    // Patrullajes
    private long patrullajesProgramados;
    private long patrullajesEnCurso;
    private long patrullajesPausados;
    private long patrullajesFinalizados;

    // Evidencias y Documentos
    private long documentosEnRevision;
    private long documentosObservados;
    private long documentosIntegridadNoCoincidente;

    // Seguimientos de Supervisión
    private long seguimientosAbiertos;
    private long seguimientosEnRevision;
    private long seguimientosObservados;
    private long seguimientosEnSubsanacion;
    private long seguimientosResueltos;
    private long seguimientosCerrados;
    private long seguimientosVencidos;

    // Recursos
    private long personalProgramadoHoy;
    private long unidadesOperativas;
    private long unidadesEnMantenimiento;

    // Metadatos de consulta
    private OffsetDateTime fechaHoraActualizacion;
    @Builder.Default
    private String zonaHoraria = "America/Lima";
    private String periodoDescripcion;
}
