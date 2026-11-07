package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CambioEstadoConfiguracionRequest {

    @NotBlank(message = "El motivo o justificación técnica es obligatoria")
    private String motivo;

    private String resolucionSustento;
}
