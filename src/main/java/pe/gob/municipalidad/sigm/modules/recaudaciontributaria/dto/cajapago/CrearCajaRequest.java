package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.cajapago;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrearCajaRequest {

    @NotBlank(message = "El código institucional de la caja es obligatorio")
    @Size(max = 50, message = "El código no debe superar 50 caracteres")
    private String codigo;

    @NotBlank(message = "El nombre o descripción de la caja es obligatorio")
    @Size(max = 150, message = "El nombre no debe superar 150 caracteres")
    private String nombre;

    private Long oficinaId;

    @Size(max = 255, message = "La ubicación no debe superar 255 caracteres")
    private String ubicacion;

    private Long cajeroAsignadoId;

    private String observaciones;
}
