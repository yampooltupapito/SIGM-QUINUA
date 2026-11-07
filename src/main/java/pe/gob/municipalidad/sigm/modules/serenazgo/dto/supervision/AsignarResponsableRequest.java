package pe.gob.municipalidad.sigm.modules.serenazgo.dto.supervision;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AsignarResponsableRequest {

    @NotNull(message = "El identificador del responsable asignado es obligatorio")
    private Long responsableId;

    private Long oficinaId;

    private String motivo;
}
