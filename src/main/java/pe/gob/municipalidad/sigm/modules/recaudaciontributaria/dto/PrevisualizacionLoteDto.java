package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoDeterminacionTributaria;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrevisualizacionLoteDto {
    private TipoDeterminacionTributaria tipoDeterminacion;
    private UUID periodoId;
    private Integer periodoAnio;
    private int totalEvaluadas;
    private int totalElegibles;
    private int totalDuplicadas;
    private int totalNoElegibles;
    private BigDecimal importeTotalEstimado;
}
