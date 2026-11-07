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
public class RegistrarObservacionRequest {

    @NotBlank(message = "El motivo de la observación es obligatorio")
    @Size(max = 150, message = "El motivo no puede exceder 150 caracteres")
    private String motivo;

    @NotBlank(message = "El detalle de la observación técnica o administrativa es obligatorio")
    @Size(min = 10, max = 2000, message = "El detalle debe contener entre 10 y 2000 caracteres")
    private String detalle;
}
