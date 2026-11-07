package pe.gob.municipalidad.sigm.modules.serenazgo.dto.documentos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentoSeguridadDetalleResponse {

    private UUID id;
    private String codigo;
    private String nombreOriginal;
    private String nombreAlmacenado;
    private String extension;
    private String mimeType;
    private Long tamanoBytes;
    private String tamanoFormateado;
    private String hashSha256;
    private TipoDocumentoSeguridad tipoDocumento;
    private String tipoDocumentoDesc;
    private String tipoDocumentoIcono;
    private EstadoDocumentoSeguridad estado;
    private String estadoDesc;
    private String estadoBadgeClase;
    private OrigenDocumentoSeguridad origenDocumento;
    private String origenDocumentoDesc;
    private NivelConfidencialidad nivelConfidencialidad;
    private String nivelConfidencialidadDesc;
    private String descripcion;
    private String observaciones;
    private String motivoRestriccion;
    private String motivoArchivado;
    private OffsetDateTime fechaHoraCarga;
    private String fechaHoraCargaFormateada;
    private String usuarioCargaNombre;
    private String usuarioCargaUsername;
    private OffsetDateTime fechaHoraValidacion;
    private String fechaHoraValidacionFormateada;
    private String usuarioValidadorNombre;
    private Integer versionActual;
    private UUID documentoPadreId;

    // Metadatos de campo
    private OffsetDateTime fechaCaptura;
    private String fechaCapturaFormateada;
    private String ubicacionCaptura;
    private BigDecimal latitud;
    private BigDecimal longitud;
    private String autoriaDeclarada;

    // Verificación
    private ResultadoVerificacionIntegridad ultimoResultadoVerificacion;
    private String ultimoResultadoVerificacionDesc;
    private String verificacionBadgeClase;
    private OffsetDateTime fechaUltimaVerificacion;
    private String fechaUltimaVerificacionFormateada;
    private String usuarioUltimaVerificacionNombre;

    private boolean esImagen;
    private boolean esVideo;
    private boolean esPdf;

    // Listas tabulares
    @Builder.Default
    private List<RelacionItemResponse> relaciones = new ArrayList<>();

    @Builder.Default
    private List<VerificacionItemResponse> verificaciones = new ArrayList<>();

    @Builder.Default
    private List<HistorialItemResponse> historial = new ArrayList<>();

    @Builder.Default
    private List<VersionItemResponse> versiones = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RelacionItemResponse {
        private UUID id;
        private TipoRelacionDocumentoSeguridad tipoEntidad;
        private String tipoEntidadDesc;
        private UUID entidadId;
        private String entidadCodigo;
        private String urlDetalle;
        private RolRelacionDocumento rolRelacion;
        private String rolRelacionDesc;
        private String fechaAsociacionFormateada;
        private String usuarioAsociaNombre;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VerificacionItemResponse {
        private UUID id;
        private String hashRegistrado;
        private String hashCalculado;
        private ResultadoVerificacionIntegridad resultado;
        private String resultadoDesc;
        private String badgeClase;
        private String fechaVerificacionFormateada;
        private String usuarioNombre;
        private String detalleTecnico;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class HistorialItemResponse {
        private UUID id;
        private String accion;
        private String descripcion;
        private String usuarioNombre;
        private String fechaHoraFormateada;
        private String ipOrigen;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VersionItemResponse {
        private UUID id;
        private String codigo;
        private Integer version;
        private String nombreOriginal;
        private Long tamanoBytes;
        private String tamanoFormateado;
        private String hashSha256;
        private String fechaHoraCargaFormateada;
        private String usuarioCargaNombre;
        private String motivo;
    }
}
