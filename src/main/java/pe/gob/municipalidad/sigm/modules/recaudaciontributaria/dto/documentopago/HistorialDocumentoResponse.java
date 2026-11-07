package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.documentopago;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialDocumentoResponse {
    private UUID id;
    private String tipoEvento;
    private String descripcion;
    private String ipOrigen;
    private String userAgent;
    private String usuarioNombre;
    private OffsetDateTime fechaEvento;
}
