package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidenciaDocumentoResponse {

    private UUID id;
    private String nombreArchivo;
    private String tipoMime;
    private Long tamanoBytes;
    private String tamanoFormateado;
    private String rutaArchivo;
    private String hashSha256;
    private String descripcion;
    private Boolean esFotografia;
    private Long documentoId;
    private String usuarioNombre;
    private OffsetDateTime createdAt;
}
