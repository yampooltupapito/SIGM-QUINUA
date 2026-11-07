package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertaCierreRequest {

    @NotBlank(message = "El motivo de cierre administrativo es obligatorio")
    private String motivo;

    private String resultadoCiudadano;
}
