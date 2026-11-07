package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.fraccionamiento;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluarSolicitudRequest {

    @NotNull(message = "Debe indicar la decisión de evaluación")
    private Boolean conforme;

    private String observaciones;

    // Mapa opcional obligacionId -> "CONFORME" / "EXCLUIDO"
    private Map<UUID, String> validacionObligaciones;
}
