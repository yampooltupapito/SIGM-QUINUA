package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CambioEstadoCalculoRequest {

    @NotBlank(message = "El motivo de la acción o cambio de estado es obligatorio")
    private String motivo;

    private String resolucionSustento;
}
