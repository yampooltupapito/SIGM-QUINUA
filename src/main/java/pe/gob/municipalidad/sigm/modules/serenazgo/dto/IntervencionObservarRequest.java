package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntervencionObservarRequest {

    @NotBlank(message = "Debe consignar el motivo fundamentado de la observación")
    @Size(min = 10, max = 2000, message = "El motivo debe tener entre 10 y 2000 caracteres")
    private String motivo;
}
