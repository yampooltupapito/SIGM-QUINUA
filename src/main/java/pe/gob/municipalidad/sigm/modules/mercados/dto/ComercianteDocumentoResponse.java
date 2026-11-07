package pe.gob.municipalidad.sigm.modules.mercados.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Builder
public class ComercianteDocumentoResponse {
    private Long id;
    private Long documentoId;
    private UUID documentoUuid;
    private String tipoDocumento;
    private String numeroDocumento;
    private String titulo;
    private String extension;
    private Long tamanioBytes;
    private String observaciones;
    private OffsetDateTime fechaSubida;
    private String subidoPor;
}
