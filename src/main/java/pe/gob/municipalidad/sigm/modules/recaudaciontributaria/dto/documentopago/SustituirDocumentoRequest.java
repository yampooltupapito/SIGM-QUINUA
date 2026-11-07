package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.documentopago;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SustituirDocumentoRequest {

    @NotBlank(message = "El motivo de sustitución es obligatorio")
    @Size(min = 10, max = 500, message = "El motivo debe tener entre 10 y 500 caracteres")
    private String motivo;

    private String serieCodigo;
}
