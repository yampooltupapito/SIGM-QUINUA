package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PeriodoSummaryDto {
    private UUID id;
    private Integer anio;
    private String fechaInicio;
    private String fechaFin;
    private BigDecimal uitVigente;
    private String estado;
    private String badgeClass;
    private String resolucionAprobacion;
    private String observaciones;
}
