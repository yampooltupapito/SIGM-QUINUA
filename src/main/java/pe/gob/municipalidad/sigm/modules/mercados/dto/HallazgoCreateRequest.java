package pe.gob.municipalidad.sigm.modules.mercados.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.GravedadHallazgo;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HallazgoCreateRequest {

    @NotBlank(message = "La categoría del hallazgo es obligatoria")
    private String categoria;

    @NotBlank(message = "La descripción del hallazgo es obligatoria")
    private String descripcion;

    private GravedadHallazgo gravedadTecnica;
}
