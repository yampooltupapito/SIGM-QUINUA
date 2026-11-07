package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.cajapago;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RevertirPagoRequest {

    @NotBlank(message = "El motivo o sustento de reversión del pago es estrictamente obligatorio")
    private String motivoReversion;

    private String observaciones;
}
