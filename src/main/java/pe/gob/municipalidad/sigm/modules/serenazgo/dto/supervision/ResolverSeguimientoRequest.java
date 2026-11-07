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
public class ResolverSeguimientoRequest {

    @NotBlank(message = "El pronunciamiento o resolución administrativa es obligatorio")
    @Size(min = 10, max = 2000, message = "La resolución debe contener entre 10 y 2000 caracteres")
    private String resolucion;

    @Size(max = 2000, message = "Las disposiciones complementarias no pueden superar 2000 caracteres")
    private String disposiciones;
}
