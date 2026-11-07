package pe.gob.municipalidad.sigm.modules.serenazgo.dto.supervision;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrarSubsanacionRequest {

    @NotBlank(message = "El asunto o referencia de la subsanación es obligatorio")
    @Size(max = 150, message = "El asunto no puede superar 150 caracteres")
    private String motivo;

    @NotBlank(message = "El detalle de la subsanación o descargo es obligatorio")
    @Size(min = 10, max = 2000, message = "El detalle debe contener entre 10 y 2000 caracteres")
    private String detalle;
}
