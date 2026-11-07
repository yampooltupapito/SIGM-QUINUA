package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.PrioridadIncidenciaSeguridad;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertaPrioridadRequest {

    @NotNull(message = "Debe seleccionar la nueva prioridad administrativa")
    private PrioridadIncidenciaSeguridad nuevaPrioridad;

    @NotBlank(message = "La justificación de cambio de prioridad es obligatoria")
    private String justificacion;
}
