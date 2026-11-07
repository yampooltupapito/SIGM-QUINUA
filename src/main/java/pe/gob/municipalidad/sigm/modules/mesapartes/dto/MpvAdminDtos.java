package pe.gob.municipalidad.sigm.modules.mesapartes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTOs de gestión administrativa e institucional de Mesa de Partes Virtual (Prompt 14).
 */
public final class MpvAdminDtos {

    private MpvAdminDtos() {}

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RevisionRequisitoResponse {
        private Long id;
        private Long requisitoId;
        private String codigoRequisito;
        private String nombreRequisito;
        private String descripcionRequisito;
        private Boolean obligatorio;
        private Long documentoId;
        private UUID documentoUuid;
        private String nombreDocumento;
        private EstadoRevisionRequisito estadoRevision;
        private String observaciones;
        private String usuarioRevisorNombre;
        private OffsetDateTime fechaRevision;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VerificarRequisitoRequest {
        @NotNull(message = "El estado de revisión es obligatorio.")
        private EstadoRevisionRequisito estadoRevision;

        private Long documentoId;

        @Size(max = 1000, message = "Las observaciones no pueden exceder 1000 caracteres.")
        private String observaciones;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RegistrarObservacionAdminRequest {
        @Builder.Default
        private TipoObservacionExpediente tipoObservacion = TipoObservacionExpediente.FORMA;

        @Builder.Default
        private VisibilidadObservacion visibilidad = VisibilidadObservacion.CIUDADANO;

        private Long requisitoId;

        @NotBlank(message = "La descripción de la observación es obligatoria.")
        @Size(max = 2000, message = "La descripción no puede exceder 2000 caracteres.")
        private String descripcion;

        private Integer diasPlazoSubsanacion;

        @Builder.Default
        private Boolean solicitarSubsanacion = false;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ActualizarPrioridadRequest {
        @NotNull(message = "La nueva prioridad es obligatoria.")
        private PrioridadExpediente prioridad;

        @NotBlank(message = "El motivo del cambio de prioridad es obligatorio.")
        @Size(max = 1000, message = "El motivo no puede exceder 1000 caracteres.")
        private String motivo;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CambioPrioridadResponse {
        private Long id;
        private PrioridadExpediente prioridadAnterior;
        private PrioridadExpediente prioridadNueva;
        private String motivo;
        private String usuarioNombre;
        private OffsetDateTime fechaCambio;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ConstanciaRecepcionResponse {
        private String numeroExpediente;
        private UUID expedienteUuid;
        private String tipoTramiteNombre;
        private String asunto;
        private String solicitanteNombreCompleto;
        private String solicitanteDocumento;
        private String oficinaReceptora;
        private String operadorReceptor;
        private OffsetDateTime fechaPresentacion;
        private OffsetDateTime fechaRecepcion;
        private Integer totalDocumentos;
        private String codigoVerificacion;
        private String urlVerificacion;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BandejaEstadisticasAdminResponse {
        private long totalExpedientes;
        private long presentados;
        private long recibidos;
        private long enRevision;
        private long observados;
        private long pendientesSubsanacion;
        private long resueltos;
    }
}
