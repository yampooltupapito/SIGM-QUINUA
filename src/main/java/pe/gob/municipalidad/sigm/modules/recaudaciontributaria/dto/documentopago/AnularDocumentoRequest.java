package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.documentopago;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnularDocumentoRequest {

    @NotBlank(message = "El motivo de anulación es obligatorio")
    @Size(min = 10, max = 500, message = "El motivo debe tener entre 10 y 500 caracteres")
    private String motivo;
}
