package pe.gob.municipalidad.sigm.modules.mesapartes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class MpvDtos {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CrearBorradorRequest {
        @NotBlank(message = "El asunto del expediente es obligatorio.")
        @Size(max = 255, message = "El asunto no puede superar los 255 caracteres.")
        private String asunto;

        private String descripcion;

        @NotNull(message = "Debe especificar el tipo de trámite.")
        private Long tipoTramiteId;

        private Long solicitanteId; // Opcional si se deduce del usuario autenticado

        private Long oficinaResponsableId; // Opcional si se deduce del tipo de trámite

        @Builder.Default
        private PrioridadExpediente prioridad = PrioridadExpediente.NORMAL;

        @Builder.Default
        private ConfidencialidadExpediente confidencialidad = ConfidencialidadExpediente.INTERNO;

        @Builder.Default
        private CanalIngreso canalIngreso = CanalIngreso.VIRTUAL;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ActualizarBorradorRequest {
        @NotBlank(message = "El asunto del expediente es obligatorio.")
        @Size(max = 255, message = "El asunto no puede superar los 255 caracteres.")
        private String asunto;

        private String descripcion;

        private Long tipoTramiteId;

        private PrioridadExpediente prioridad;

        private ConfidencialidadExpediente confidencialidad;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PresentarExpedienteRequest {
        private String observaciones;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CambiarEstadoRequest {
        @NotNull(message = "El nuevo estado es obligatorio.")
        private EstadoExpediente nuevoEstado;

        @NotBlank(message = "El motivo del cambio de estado es obligatorio.")
        @Size(max = 1000, message = "El motivo no puede exceder 1000 caracteres.")
        private String motivo;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DerivarExpedienteRequest {
        @NotNull(message = "La oficina de destino es obligatoria.")
        private Long oficinaDestinoId;

        @NotBlank(message = "El motivo de la derivación es obligatorio.")
        @Size(max = 1000, message = "El motivo no puede exceder 1000 caracteres.")
        private String motivo;

        private String observaciones;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ObservarExpedienteRequest {
        @NotBlank(message = "El detalle de la observación es obligatorio.")
        private String descripcion;

        private Integer diasPlazoSubsanacion; // Si se omite, se usa el plazo configurado por defecto
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SubsanarExpedienteRequest {
        @NotBlank(message = "La respuesta o descargo de la subsanación es obligatoria.")
        private String respuestaCiudadano;

        private List<Long> documentoIds; // Documentos cargados previamente para asociar como subsanación
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AsociarDocumentoRequest {
        @NotNull(message = "El ID del documento es obligatorio.")
        private Long documentoId;

        @Builder.Default
        private TipoRelacionDocumentoExpediente tipoRelacion = TipoRelacionDocumentoExpediente.ANEXO;

        private Integer orden;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TipoTramiteRequest {
        @NotBlank(message = "El código es obligatorio.")
        @Size(max = 50, message = "El código no puede exceder 50 caracteres.")
        private String codigo;

        @NotBlank(message = "El nombre del trámite es obligatorio.")
        @Size(max = 200, message = "El nombre no puede exceder 200 caracteres.")
        private String nombre;

        private String descripcion;

        private Long oficinaResponsableId;

        @Builder.Default
        private Boolean requiereDocumentos = true;

        @Builder.Default
        private Boolean permitePresentacionVirtual = true;

        @Builder.Default
        private Integer plazoDias = 30;

        @Builder.Default
        private UnidadPlazo unidadPlazo = UnidadPlazo.DIAS_HABILES;

        @Builder.Default
        private String estado = "ACTIVO";
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ExpedienteResponse {
        private Long id;
        private UUID uuid;
        private String numeroExpediente;
        private Integer anio;
        private String asunto;
        private String descripcion;
        private Long tipoTramiteId;
        private String tipoTramiteNombre;
        private String tipoTramiteCodigo;
        private Long solicitanteId;
        private String solicitanteNombreCompleto;
        private String solicitanteNumeroDocumento;
        private Long oficinaResponsableId;
        private String oficinaResponsableNombre;
        private Long responsableActualId;
        private String responsableActualNombre;
        private EstadoExpediente estadoActual;
        private OffsetDateTime fechaPresentacion;
        private OffsetDateTime fechaRecepcion;
        private OffsetDateTime fechaLimite;
        private OffsetDateTime fechaCierre;
        private CanalIngreso canalIngreso;
        private PrioridadExpediente prioridad;
        private ConfidencialidadExpediente confidencialidad;
        private Integer totalDocumentos;
        private OffsetDateTime createdAt;
        private OffsetDateTime updatedAt;

        public String getSolicitanteTipoNumeroDocumento() {
            if (solicitanteNumeroDocumento == null || solicitanteNumeroDocumento.isBlank()) {
                return "-";
            }
            String prefix = solicitanteNumeroDocumento.trim().length() == 11 ? "RUC: " : "DNI: ";
            return prefix + solicitanteNumeroDocumento;
        }

        public String getSolicitanteNombre() {
            return solicitanteNombreCompleto;
        }

        public String getSolicitanteDocumento() {
            return solicitanteNumeroDocumento;
        }

        public String getTipoTramite() {
            return tipoTramiteNombre;
        }

        public String getEstado() {
            return estadoActual != null ? estadoActual.name() : null;
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ExpedienteDetalleResponse {
        private ExpedienteResponse expediente;
        private List<ExpedienteDocumentoResponse> documentos;
        private List<ExpedienteHistorialResponse> historial;
        private List<DerivacionResponse> derivaciones;
        private List<ObservacionResponse> observaciones;

        public Long getId() {
            return expediente != null ? expediente.getId() : null;
        }

        public UUID getUuid() {
            return expediente != null ? expediente.getUuid() : null;
        }

        public String getAsunto() {
            return expediente != null ? expediente.getAsunto() : null;
        }

        public EstadoExpediente getEstadoActual() {
            return expediente != null ? expediente.getEstadoActual() : null;
        }

        public String getNumeroExpediente() {
            return expediente != null ? expediente.getNumeroExpediente() : null;
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ExpedienteDocumentoResponse {
        private Long id;
        private Long documentoId;
        private UUID documentoUuid;
        private String nombreOriginal;
        private String extension;
        private String mimeType;
        private Long tamanoBytes;
        private String hashSha256;
        private TipoRelacionDocumentoExpediente tipoRelacion;
        private OffsetDateTime fechaAsociacion;
        private String usuarioAsociaNombre;
        private Integer orden;
        private String estado;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ExpedienteHistorialResponse {
        private Long id;
        private EstadoExpediente estadoAnterior;
        private EstadoExpediente estadoNuevo;
        private String accion;
        private String observacion;
        private String usuarioResponsableNombre;
        private String oficinaNombre;
        private OffsetDateTime fechaMovimiento;
        private String identificadorCorrelacion;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DerivacionResponse {
        private Long id;
        private Long oficinaOrigenId;
        private String oficinaOrigenNombre;
        private Long oficinaDestinoId;
        private String oficinaDestinoNombre;
        private String usuarioDerivadorNombre;
        private String motivo;
        private OffsetDateTime fechaDerivacion;
        private OffsetDateTime fechaRecepcion;
        private EstadoDerivacion estado;
        private String observaciones;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ObservacionResponse {
        private Long id;
        private String descripcion;
        private String usuarioObservadorNombre;
        private OffsetDateTime fechaRegistro;
        private OffsetDateTime fechaNotificacion;
        private OffsetDateTime fechaLimite;
        private EstadoObservacion estado;
        private pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.TipoObservacionExpediente tipoObservacion;
        private pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.VisibilidadObservacion visibilidad;
        private Long requisitoId;
        private String requisitoNombre;
        private OffsetDateTime fechaRespuesta;
        private List<SubsanacionResponse> subsanaciones;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SubsanacionResponse {
        private Long id;
        private Long observacionId;
        private String respuestaCiudadano;
        private OffsetDateTime fechaSubsanacion;
        private String usuarioSubsanadorNombre;
        private EstadoSubsanacion estado;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TipoTramiteResponse {
        private Long id;
        private String codigo;
        private String nombre;
        private String descripcion;
        private Long oficinaResponsableId;
        private String oficinaResponsableNombre;
        private Boolean requiereDocumentos;
        private Boolean permitePresentacionVirtual;
        private Integer plazoDias;
        private UnidadPlazo unidadPlazo;
        private String estado;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MpvEstadisticasResponse {
        private long totalExpedientes;
        private long borradores;
        private long presentados;
        private long recibidos;
        private long enRevision;
        private long observados;
        private long enAtencion;
        private long resueltos;
        private long archivados;
    }
}
