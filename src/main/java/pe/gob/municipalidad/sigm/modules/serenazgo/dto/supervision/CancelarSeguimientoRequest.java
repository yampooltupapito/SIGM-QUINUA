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
public class CancelarSeguimientoRequest {

    @NotBlank(message = "El motivo de la cancelación es obligatorio")
    @Size(min = 10, max = 1000, message = "El motivo debe contener entre 10 y 1000 caracteres")
    private String motivo;
}
