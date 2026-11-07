package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.MetodoDistribucionArbitrio;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoDatoCriterio;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CriterioItemRequest {

    @NotBlank(message = "El código de criterio es obligatorio")
    private String codigo;

    @NotBlank(message = "El nombre de criterio es obligatorio")
    private String nombre;

    @NotNull(message = "El tipo de dato es obligatorio")
    private TipoDatoCriterio tipoDato;

    private String unidad;

    @NotNull(message = "El método de distribución es obligatorio")
    private MetodoDistribucionArbitrio metodoDistribucion;

    @Builder.Default
    private BigDecimal ponderacionPorcentaje = new BigDecimal("100.00");

    private BigDecimal factorMinimo;
    private BigDecimal factorMaximo;
    private String fuenteNormativa;
}
