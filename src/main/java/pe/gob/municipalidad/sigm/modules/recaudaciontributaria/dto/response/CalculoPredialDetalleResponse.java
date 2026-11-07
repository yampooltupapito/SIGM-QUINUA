package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalculoPredialDetalleResponse {

    private UUID id;
    private UUID predioId;
    private String codigoPredio;
    private String direccionPredio;
    private String tipoPredio;
    private String clasificacionPredio;
    private BigDecimal porcentajeParticipacion;
    private BigDecimal areaTerreno;
    private String unidadMedidaTerreno;
    private BigDecimal arancelUnitarioM2;
    private BigDecimal valorTerreno;
    private Integer cantidadConstrucciones;
    private BigDecimal areaTotalConstruida;
    private BigDecimal valorConstrucciones;
    private BigDecimal valorInstalaciones;
    private BigDecimal valorAutovaluoTotal;
    private BigDecimal valorAutovaluoImputable;
}
