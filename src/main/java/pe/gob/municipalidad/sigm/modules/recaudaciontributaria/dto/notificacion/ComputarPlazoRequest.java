package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.notificacion;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComputarPlazoRequest {

    @NotBlank(message = "El tipo de plazo es obligatorio")
    private String tipoPlazo;

    @NotNull(message = "Debe indicar si el cómputo es en días hábiles")
    private Boolean diasHabiles;

    @NotNull(message = "El número de días es obligatorio")
    @Min(value = 1, message = "El plazo debe ser de al menos 1 día")
    private Integer numeroDias;

    @NotNull(message = "La fecha hito inicial es obligatoria")
    private LocalDate fechaHitoInicial;
}

