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
public class CerrarSeguimientoRequest {

    @NotBlank(message = "El resultado o conclusión final del seguimiento es obligatorio")
    @Size(min = 10, max = 2000, message = "El resultado debe contener entre 10 y 2000 caracteres")
    private String resultado;

    @Size(max = 2000, message = "Las observaciones de archivo no pueden superar 2000 caracteres")
    private String observacionesFinales;
}
