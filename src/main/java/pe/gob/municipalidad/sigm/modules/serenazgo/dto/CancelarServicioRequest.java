package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CancelarServicioRequest {

    @NotBlank(message = "El motivo de la cancelación es obligatorio")
    private String motivo;
}
