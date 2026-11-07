package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.fraccionamiento;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoIncidenciaConvenio;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrarIncidenciaConvenioRequest {

    @NotNull(message = "El tipo de incidencia es obligatorio")
    private TipoIncidenciaConvenio tipoIncidencia;

    @NotBlank(message = "La descripción de la actuación o incidencia es obligatoria")
    private String descripcion;

    private UUID documentoSustentoId;
}
