package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoActuacionIntervencion;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntervencionActuacionResponse {

    private UUID id;
    private TipoActuacionIntervencion tipoActuacion;
    private String tipoActuacionDesc;
    private String descripcion;
    private OffsetDateTime fechaHora;
    private String fechaHoraFormateada;
    private UUID personalResponsableId;
    private String personalResponsableNombre;
    private String observaciones;
    private String createdBy;
}
