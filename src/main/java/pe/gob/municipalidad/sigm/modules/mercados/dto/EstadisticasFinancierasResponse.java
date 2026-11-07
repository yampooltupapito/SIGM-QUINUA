package pe.gob.municipalidad.sigm.modules.mercados.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstadisticasFinancierasResponse {
    private long totalObligacionesEmitidas;
    private BigDecimal montoTotalExigible;
    private BigDecimal montoTotalRecaudado;
    private BigDecimal saldoTotalPendiente;
    private long totalObligacionesVencidas;
    private BigDecimal recaudacionHoy;
    private BigDecimal totalReversos;
    private long cajasAbiertasCount;
    @Builder.Default
    private Map<String, BigDecimal> recaudacionPorMedio = new HashMap<>();
}
