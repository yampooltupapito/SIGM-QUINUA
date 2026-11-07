package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.notificacion;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnularNotificacionRequest {

    @NotBlank(message = "El motivo de anulación es obligatorio y debe sustentarse")
    private String motivoAnulacion;
}

