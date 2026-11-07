package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.fraccionamiento;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardFraccionamientoResponse {
    private long totalSolicitudes;
    private long solicitudesEnEvaluacion;
    private long solicitudesObservadas;
    private long solicitudesAprobadas;

    private long conveniosVigentes;
    private long conveniosCumplidos;
    private long cuotasPendientes;
    private long cuotasVencidas;

    private BigDecimal montoFinanciadoTotal;
    private BigDecimal montoRecaudadoTotal;
    private BigDecimal saldoPendienteTotal;
}
