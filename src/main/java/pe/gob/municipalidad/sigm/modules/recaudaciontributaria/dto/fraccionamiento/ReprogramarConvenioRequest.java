package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.fraccionamiento;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReprogramarConvenioRequest {

    @NotNull(message = "El nuevo número de cuotas restantes es obligatorio")
    @Min(value = 1, message = "Debe reprogramar al menos a 1 cuota")
    private Integer nuevasCuotas;

    @NotNull(message = "La fecha de vencimiento de la primera cuota reprogramada es obligatoria")
    private LocalDate nuevaFechaInicio;

    @NotBlank(message = "El fundamento legal y resolución de reprogramación son obligatorios")
    private String resolucionSustento;

    @NotBlank(message = "El motivo de la reprogramación es obligatorio")
    private String motivo;

    private UUID documentoSustentoId;
}
