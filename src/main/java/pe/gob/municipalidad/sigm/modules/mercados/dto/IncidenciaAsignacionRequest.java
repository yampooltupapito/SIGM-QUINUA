package pe.gob.municipalidad.sigm.modules.mercados.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.PrioridadIncidencia;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidenciaAsignacionRequest {

    @NotNull(message = "El usuario responsable es obligatorio")
    private Long responsableId;

    private Long oficinaResponsableId;
    private PrioridadIncidencia prioridad;
    private String observaciones;
}
