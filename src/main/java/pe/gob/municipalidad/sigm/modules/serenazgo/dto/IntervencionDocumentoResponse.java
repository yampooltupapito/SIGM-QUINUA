package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoDocumentoIntervencion;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntervencionDocumentoResponse {

    private UUID id;
    private TipoDocumentoIntervencion tipoDocumento;
    private String tipoDocumentoDesc;
    private String nombreArchivo;
    private String rutaArchivo;
    private String mimeType;
    private Long tamanioBytes;
    private String descripcion;
    private Boolean visibleCiudadano;
    private OffsetDateTime createdAt;
    private String createdAtFormateada;
    private String createdBy;

    public boolean isVisibleCiudadano() {
        return Boolean.TRUE.equals(visibleCiudadano);
    }
}
