package pe.gob.municipalidad.sigm.modules.mesapartes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class AtencionDtos {

    // ==========================================
    // 1. ATENCIÓN DE EXPEDIENTES
    // ==========================================
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class IniciarAtencionRequest {
        @Size(max = 500, message = "Las indicaciones no pueden exceder 500 caracteres.")
        private String indicaciones;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AtencionItemResponse {
        private Long id;
        private UUID uuid;
        private String numeroExpediente;
        private String asunto;
        private String tipoTramite;
        private String solicitanteNombre;
        private String solicitanteDocumento;
        private Long oficinaId;
        private String oficinaNombre;
        private Long responsableId;
        private String responsableNombre;
        private EstadoExpediente estado;
        private PrioridadExpediente prioridad;
        private OffsetDateTime fechaPresentacion;
        private OffsetDateTime fechaRecepcion;
        private OffsetDateTime fechaInicioAtencion;
        private OffsetDateTime fechaLimite;
        private Long diasRestantes;
        private boolean vencido;
        private boolean reabierto;
    }

    // ==========================================
    // 2. EVALUACIONES TÉCNICAS Y LEGALES
    // ==========================================
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CrearEvaluacionRequest {
        @NotNull(message = "El tipo de evaluación es obligatorio.")
        private TipoEvaluacionExpediente tipoEvaluacion;

        @NotBlank(message = "La descripción de la evaluación es obligatoria.")
        private String descripcion;

        @NotBlank(message = "Las conclusiones de la evaluación son obligatorias.")
        private String conclusiones;

        private String recomendacion;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EvaluacionResponse {
        private Long id;
        private Long expedienteId;
        private String numeroExpediente;
        private Long oficinaId;
        private String oficinaNombre;
        private Long usuarioEvaluadorId;
        private String usuarioEvaluadorNombre;
        private TipoEvaluacionExpediente tipoEvaluacion;
        private String descripcion;
        private String conclusiones;
        private String recomendacion;
        private EstadoEvaluacion estado;
        private OffsetDateTime fechaEvaluacion;
        private Long version;
        private List<EvaluacionDocumentoResponse> documentos;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EvaluacionDocumentoResponse {
        private Long id;
        private Long documentoId;
        private String nombreOriginal;
        private String mimeType;
        private String tamanoFormateado;
        private String tipoDocumento;
        private String hashSha256;
    }

    // ==========================================
    // 3. PROPUESTAS DE DECISIÓN
    // ==========================================
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CrearPropuestaRequest {
        @NotNull(message = "El tipo de decisión es obligatorio.")
        private TipoDecisionExpediente tipoDecision;

        @NotNull(message = "El resultado propuesto es obligatorio.")
        private ResultadoDecision resultadoPropuesto;

        @NotBlank(message = "El fundamento de la propuesta es obligatorio.")
        private String fundamento;

        @NotBlank(message = "La descripción o parte resolutiva propuesta es obligatoria.")
        private String descripcion;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PropuestaResponse {
        private Long id;
        private Long expedienteId;
        private String numeroExpediente;
        private TipoDecisionExpediente tipoDecision;
        private ResultadoDecision resultadoPropuesto;
        private String fundamento;
        private String descripcion;
        private Long usuarioElaboradorId;
        private String usuarioElaboradorNombre;
        private Long oficinaId;
        private String oficinaNombre;
        private EstadoPropuesta estado;
        private OffsetDateTime fechaCreacion;
        private OffsetDateTime fechaEnvioRevision;
        private OffsetDateTime fechaResolucionRevision;
        private Long version;
        private List<RevisionPropuestaResponse> revisiones;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RevisarPropuestaRequest {
        @NotNull(message = "La acción de revisión es obligatoria.")
        private AccionRevisionPropuesta accion;

        @NotBlank(message = "Las observaciones o fundamentación son obligatorias.")
        @Size(min = 10, max = 2000, message = "Las observaciones deben tener entre 10 y 2000 caracteres.")
        private String observaciones;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RevisionPropuestaResponse {
        private Long id;
        private Long propuestaId;
        private Long usuarioRevisorId;
        private String usuarioRevisorNombre;
        private Long oficinaId;
        private String oficinaNombre;
        private AccionRevisionPropuesta accion;
        private String observaciones;
        private OffsetDateTime fechaRevision;
    }

    // ==========================================
    // 4. DECISIONES Y ACTOS ADMINISTRATIVOS
    // ==========================================
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RegistrarDecisionRequest {
        private Long propuestaId;

        @NotNull(message = "El tipo de decisión es obligatorio.")
        private TipoDecisionExpediente tipoDecision;

        @NotNull(message = "El resultado es obligatorio.")
        private ResultadoDecision resultado;

        @NotBlank(message = "El fundamento es obligatorio.")
        private String fundamento;

        private String numeroDocumento;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FormalizarDecisionRequest {
        @NotBlank(message = "El número de documento formal emitido es obligatorio.")
        private String numeroDocumento;

        @NotBlank(message = "La evidencia de formalización es obligatoria.")
        private String evidenciaFormalizacion;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DecisionResponse {
        private Long id;
        private Long expedienteId;
        private String numeroExpediente;
        private Long propuestaId;
        private TipoDecisionExpediente tipoDecision;
        private ResultadoDecision resultado;
        private String fundamento;
        private Long autoridadCompetenteId;
        private String autoridadCompetenteNombre;
        private String cargoAutoridad;
        private Long oficinaEmisoraId;
        private String oficinaEmisoraNombre;
        private OffsetDateTime fechaDecision;
        private String numeroDocumento;
        private Long documentoId;
        private EstadoEmisionDocumento estadoEmision;
        private String evidenciaFormalizacion;
        private OffsetDateTime fechaEmision;
        private Long version;
    }

    // ==========================================
    // 5. NOTIFICACIONES ADMINISTRATIVAS
    // ==========================================
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RegistrarNotificacionRequest {
        private Long decisionId;

        @NotBlank(message = "El nombre del destinatario es obligatorio.")
        private String destinatarioNombre;

        private String destinatarioDocumento;
        private String destinatarioDireccion;
        private String destinatarioEmail;

        @NotNull(message = "El medio de notificación es obligatorio.")
        private MedioNotificacion medioNotificacion;

        private String observaciones;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AcreditarNotificacionRequest {
        @NotBlank(message = "El número o código de acuse de recibo es obligatorio.")
        private String numeroAcuseRecibo;

        private String observaciones;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class NotificacionResponse {
        private Long id;
        private Long expedienteId;
        private String numeroExpediente;
        private Long decisionId;
        private String numeroDocumentoDecision;
        private String destinatarioNombre;
        private String destinatarioDocumento;
        private String destinatarioDireccion;
        private String destinatarioEmail;
        private MedioNotificacion medioNotificacion;
        private EstadoNotificacion estadoNotificacion;
        private OffsetDateTime fechaIntento;
        private OffsetDateTime fechaPracticaValida;
        private String numeroAcuseRecibo;
        private Long evidenciaDocumentalId;
        private String observaciones;
        private Long usuarioResponsableId;
        private String usuarioResponsableNombre;
        private boolean esLegalmenteValida;
        private OffsetDateTime createdAt;
    }

    // ==========================================
    // 6. CIERRE, ARCHIVO Y REAPERTURA
    // ==========================================
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CerrarExpedienteRequest {
        @NotNull(message = "El tipo de cierre es obligatorio.")
        private TipoCierreExpediente tipoCierre;

        @NotBlank(message = "El motivo de cierre es obligatorio.")
        @Size(min = 10, max = 2000, message = "El motivo debe contener entre 10 y 2000 caracteres.")
        private String motivoCierre;

        private Long decisionId;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CierreResponse {
        private Long id;
        private Long expedienteId;
        private String numeroExpediente;
        private Long decisionId;
        private Long usuarioCierreId;
        private String usuarioCierreNombre;
        private Long oficinaCierreId;
        private String oficinaCierreNombre;
        private String motivoCierre;
        private TipoCierreExpediente tipoCierre;
        private OffsetDateTime fechaCierre;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ArchivarExpedienteRequest {
        @NotBlank(message = "El motivo de archivo es obligatorio.")
        private String motivoArchivo;

        private String codigoArchivador;
        private String ubicacionFisica;
        private String tomoFolio;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ArchivoResponse {
        private Long id;
        private Long expedienteId;
        private String numeroExpediente;
        private Long usuarioArchivoId;
        private String usuarioArchivoNombre;
        private Long oficinaArchivoId;
        private String oficinaArchivoNombre;
        private String codigoArchivador;
        private String ubicacionFisica;
        private String tomoFolio;
        private String motivoArchivo;
        private OffsetDateTime fechaArchivo;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReabrirExpedienteRequest {
        @NotBlank(message = "El motivo legal o administrativo de reapertura es obligatorio.")
        @Size(min = 15, max = 2000, message = "El motivo debe ser una fundamentación de al menos 15 caracteres.")
        private String motivoLegal;

        private String actoReaperturaReferencia;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReaperturaResponse {
        private Long id;
        private Long expedienteId;
        private String numeroExpediente;
        private Long usuarioReaperturaId;
        private String usuarioReaperturaNombre;
        private String motivoLegal;
        private String actoReaperturaReferencia;
        private OffsetDateTime fechaReapertura;
    }

    // ==========================================
    // 7. DETALLE INTEGRAL Y ESTADÍSTICAS
    // ==========================================
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DetalleAtencionResponse {
        private Long expedienteId;
        private String numeroExpediente;
        private String asunto;
        private String tipoTramite;
        private String solicitanteNombre;
        private String solicitanteDocumento;
        private Long oficinaResponsableId;
        private String oficinaResponsableNombre;
        private Long responsableActualId;
        private String responsableActualNombre;
        private EstadoExpediente estadoActual;
        private PrioridadExpediente prioridad;
        private OffsetDateTime fechaPresentacion;
        private OffsetDateTime fechaRecepcion;
        private OffsetDateTime fechaInicioAtencion;
        private OffsetDateTime fechaLimite;
        private Long diasRestantes;
        private boolean vencido;
        private boolean reabierto;
        private String resultadoFinal;
        private String ubicacionArchivo;

        private List<EvaluacionResponse> evaluaciones;
        private List<PropuestaResponse> propuestas;
        private List<DecisionResponse> decisiones;
        private List<NotificacionResponse> notificaciones;
        private CierreResponse cierre;
        private ArchivoResponse archivo;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EstadisticasAtencionResponse {
        private long expedientesEnAtencion;
        private long evaluacionesPendientes;
        private long propuestasEnRevision;
        private long documentosPendientesFirma;
        private long expedientesResueltos;
        private long notificacionesPendientes;
        private long expedientesCerrados;
        private long expedientesArchivados;
    }
}
