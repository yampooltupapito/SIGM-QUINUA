package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.deuda;

import lombok.*;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardDeudasResponse {

    private BigDecimal saldoTotalPendiente;
    private BigDecimal saldoNoVencido;
    private BigDecimal saldoVencido;
    private BigDecimal saldoExigible;

    private long totalObligacionesPendientes;
    private long totalObligacionesVencidas;
    private long totalContribuyentesConDeuda;
    private long totalSeguimientosActivos;
    private long totalCompromisosPendientes;

    private Map<String, BigDecimal> distribucionPorConcepto;
    private Map<String, BigDecimal> distribucionPorTramo;
    private Map<String, BigDecimal> distribucionPorPeriodo;
}
