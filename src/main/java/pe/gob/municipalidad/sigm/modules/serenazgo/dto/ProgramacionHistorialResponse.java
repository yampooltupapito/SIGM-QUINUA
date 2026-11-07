package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoEventoHistorialProgramacion;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgramacionHistorialResponse {

    private UUID id;
    private TipoEventoHistorialProgramacion tipoEvento;
    private String tipoEventoDesc;
    private String estadoAnterior;
    private String estadoNuevo;
    private String motivo;
    private String usuarioNombre;
    private String detallesJson;
    private OffsetDateTime createdAt;
}
