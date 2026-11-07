package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardTributarioResponse {
    private long totalContribuyentes;
    private long totalPredios;
    private long totalConceptos;
    private long totalPeriodos;
    private long totalObligaciones;
    private long totalPagos;
    private BigDecimal recaudacionTotal;
    private String periodoActivo;
    private BigDecimal uitVigente;

    @Builder.Default
    private List<ContribuyenteSummaryDto> contribuyentesRecientes = new ArrayList<>();

    @Builder.Default
    private List<PeriodoSummaryDto> periodosRegistrados = new ArrayList<>();

    @Builder.Default
    private List<ConceptoSummaryDto> conceptosVigentes = new ArrayList<>();
}
