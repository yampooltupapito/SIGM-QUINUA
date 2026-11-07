package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalculoPredialTramoResponse {

    private UUID id;
    private Integer numeroTramo;
    private String descripcionTramo;
    private BigDecimal limiteInferiorUit;
    private BigDecimal limiteSuperiorUit;
    private BigDecimal limiteInferiorMonto;
    private BigDecimal limiteSuperiorMonto;
    private BigDecimal alicuotaPorcentaje;
    private BigDecimal baseImponibleTramo;
    private BigDecimal impuestoTramo;
}
