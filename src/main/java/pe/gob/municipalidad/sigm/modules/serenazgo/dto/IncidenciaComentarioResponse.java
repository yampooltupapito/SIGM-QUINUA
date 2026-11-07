package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidenciaComentarioResponse {

    private UUID id;
    private String comentario;
    private Boolean esInterno;
    private String usuarioNombre;
    private OffsetDateTime createdAt;
}
