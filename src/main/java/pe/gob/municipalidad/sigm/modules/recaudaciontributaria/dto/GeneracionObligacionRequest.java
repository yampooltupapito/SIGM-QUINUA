package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoDeterminacionTributaria;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneracionObligacionRequest {

    @NotNull(message = "El tipo de determinación es obligatorio")
    private TipoDeterminacionTributaria tipoDeterminacion;

    @NotNull(message = "El ID de la determinación aprobada es obligatorio")
    private UUID determinacionId;

    @Min(value = 1, message = "Debe registrar al menos 1 cuota")
    @Max(value = 12, message = "El máximo permitido son 12 cuotas")
    @Builder.Default
    private Integer cuotas = 1;

    private String observaciones;
}
