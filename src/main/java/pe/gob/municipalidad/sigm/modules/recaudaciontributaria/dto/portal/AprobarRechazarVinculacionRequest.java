package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.portal;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AprobarRechazarVinculacionRequest {

    @NotBlank(message = "Debe registrar un sustento u observación de la decisión administrativa.")
    private String motivo;

    private LocalDate fechaVigenciaHasta;
}

