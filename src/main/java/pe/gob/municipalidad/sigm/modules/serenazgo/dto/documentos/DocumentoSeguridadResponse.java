package pe.gob.municipalidad.sigm.modules.serenazgo.dto.documentos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentoSeguridadResponse {

    private UUID id;
    private String codigo;
    private String nombreOriginal;
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
    private OffsetDateTime fechaHoraCarga;
    private String fechaHoraCargaFormateada;
    private String usuarioCargaNombre;
    private Integer versionActual;
    private String registroAsociadoCodigo;
    private String registroAsociadoTipo;
    private String registroAsociadoUrl;
    private ResultadoVerificacionIntegridad ultimoResultadoVerificacion;
    private String ultimoResultadoVerificacionDesc;
    private String verificacionBadgeClase;
    private String fechaUltimaVerificacionFormateada;
    private boolean esImagen;
    private boolean esVideo;
    private boolean esPdf;
}
