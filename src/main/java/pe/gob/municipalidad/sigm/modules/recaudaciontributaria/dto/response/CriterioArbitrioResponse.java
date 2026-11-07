package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.MetodoDistribucionArbitrio;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoDatoCriterio;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CriterioArbitrioResponse {
    private UUID id;
    private String codigo;
    private String nombre;
    private TipoDatoCriterio tipoDato;
    private String unidad;
    private MetodoDistribucionArbitrio metodoDistribucion;
    private String metodoDescripcion;
    private BigDecimal ponderacionPorcentaje;
    private BigDecimal factorMinimo;
    private BigDecimal factorMaximo;
    private String fuenteNormativa;
    private String estado;
}
