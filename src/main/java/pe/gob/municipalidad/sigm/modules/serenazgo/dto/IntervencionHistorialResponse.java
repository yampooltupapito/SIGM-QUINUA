package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.AccionHistorialIntervencion;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntervencionHistorialResponse {

    private UUID id;
    private AccionHistorialIntervencion accion;
    private String accionDesc;
    private String estadoAnterior;
    private String estadoNuevo;
    private String motivo;
    private String usuario;
    private OffsetDateTime fecha;
    private String fechaFormateada;
}
