package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.deuda;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsignarResponsableRequest {

    @NotNull(message = "El identificador del usuario responsable es obligatorio")
    private Long responsableId;

    private Long oficinaId;

    private String observaciones;
}
