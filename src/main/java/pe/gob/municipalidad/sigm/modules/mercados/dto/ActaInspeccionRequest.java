package pe.gob.municipalidad.sigm.modules.mercados.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActaInspeccionRequest {

    @NotBlank(message = "El resumen general de la inspección es obligatorio")
    private String resumen;

    @NotBlank(message = "Las conclusiones del acta son obligatorias")
    private String conclusiones;

    private String recomendaciones;
}
