package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.fraccionamiento;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubsanarSolicitudRequest {

    @NotBlank(message = "El detalle de la subsanación es obligatorio")
    private String detalleSubsanacion;

    private UUID documentoSustentoId;
}
