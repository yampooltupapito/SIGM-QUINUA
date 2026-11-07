package pe.gob.municipalidad.sigm.common.document.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import pe.gob.municipalidad.sigm.common.document.entity.CategoriaDocumento;
import pe.gob.municipalidad.sigm.common.document.entity.ClasificacionAcceso;
import pe.gob.municipalidad.sigm.common.document.entity.EstadoDocumento;
import pe.gob.municipalidad.sigm.common.document.entity.ResultadoVerificacion;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

public class DocumentDtos {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DocumentoResponse {
        private Long id;
        private UUID identificadorPublico;
        private String nombreOriginal;
        private String nombreInterno;
        private String extension;
        private String mimeType;
        private Long tamanoBytes;
        private String tamanoFormateado;
        private String hashSha256;
        private CategoriaDocumento categoria;
        private ClasificacionAcceso clasificacionAcceso;
        private String descripcion;
        private EstadoDocumento estado;
        private Integer versionActual;
        private Boolean bloqueoEliminacion;
        private String motivoEliminacion;
        private Long usuarioPropietarioId;
        private String nombrePropietario;
        private Long oficinaId;
        private String nombreOficina;
        private String moduloOrigen;
        private OffsetDateTime fechaCreacion;
        private OffsetDateTime fechaActualizacion;
        private boolean visualizable;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SubirDocumentoRequest {
        private CategoriaDocumento categoria;
        private ClasificacionAcceso clasificacionAcceso;
        @Size(max = 500, message = "La descripción no puede exceder 500 caracteres.")
        private String descripcion;
        private Long oficinaId;
        private String moduloOrigen;
        private String tipoEntidad;
        private String entidadId;
        private String rolDocumento;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ActualizarDocumentoRequest {
        private CategoriaDocumento categoria;
        private ClasificacionAcceso clasificacionAcceso;
        @Size(max = 500, message = "La descripción no puede exceder 500 caracteres.")
        private String descripcion;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class NuevaVersionRequest {
        @NotBlank(message = "El motivo del cambio de versión es obligatorio.")
        @Size(max = 500, message = "El motivo no puede exceder 500 caracteres.")
        private String motivoCambio;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EliminarDocumentoRequest {
        @NotBlank(message = "El motivo de eliminación o archivado es obligatorio.")
        @Size(max = 500, message = "El motivo no puede exceder 500 caracteres.")
        private String motivo;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DocumentoVersionResponse {
        private Long id;
        private Integer numeroVersion;
        private String nombreInterno;
        private Long tamanoBytes;
        private String tamanoFormateado;
        private String mimeType;
        private String hashSha256;
        private String motivoCambio;
        private Long usuarioResponsableId;
        private String nombreUsuario;
        private OffsetDateTime fechaCreacion;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VerificacionIntegridadResponse {
        private Long documentoId;
        private UUID identificadorPublico;
        private Integer versionEvaluada;
        private String hashEsperado;
        private String hashCalculado;
        private ResultadoVerificacion resultado;
        private String verificadoPor;
        private OffsetDateTime fechaVerificacion;
        private String detalle;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DocumentoAuditoriaResponse {
        private Long id;
        private String accion;
        private String usuario;
        private String direccionIp;
        private String userAgent;
        private OffsetDateTime fechaAcceso;
        private String resultado;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DocumentoEstadisticasResponse {
        private long totalActivos;
        private long espacioUtilizadoBytes;
        private String espacioUtilizadoFormateado;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GenerarPdfRequest {
        @NotBlank(message = "El tipo de documento es obligatorio.")
        private String tipoPlantilla; // CONSTANCIA, COMPROBANTE, REPORTE, OFICIO
        @NotBlank(message = "El título institucional es obligatorio.")
        private String titulo;
        private String codigoDocumental;
        private String destinatario;
        private String asunto;
        @NotBlank(message = "El cuerpo del documento es obligatorio.")
        private String cuerpo;
        private Map<String, String> metadatosExtra;
    }

    public static String formatBytes(long bytes) {
        if (bytes <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
        if (digitGroups >= units.length) digitGroups = units.length - 1;
        return String.format("%.2f %s", bytes / Math.pow(1024, digitGroups), units[digitGroups]);
    }
}
