package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.deuda;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.ClasificacionDeudaTributaria;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClasificarDeudaRequest {

    @NotNull(message = "La clasificación de deuda es obligatoria")
    private ClasificacionDeudaTributaria clasificacion;

    private String observaciones;
}
