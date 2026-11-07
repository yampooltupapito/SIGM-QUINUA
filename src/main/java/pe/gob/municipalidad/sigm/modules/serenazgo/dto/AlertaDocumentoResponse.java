package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertaDocumentoResponse {

    private UUID id;
    private String nombreArchivo;
    private String tipoMime;
    private Long tamanoBytes;
    private String hashSha256;
    private Boolean esFotografia;
    private String contenidoBase64;
    private OffsetDateTime createdAt;
}
