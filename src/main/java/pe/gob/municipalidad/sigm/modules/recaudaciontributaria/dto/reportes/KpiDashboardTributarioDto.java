package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.reportes;

import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Indicadores Clave de Desempeño (KPIs) del Centro de Reportes Tributarios de Quinua.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KpiDashboardTributarioDto {

    private BigDecimal recaudacionConfirmadaTotal;
    private BigDecimal recaudacionMesActual;
    private BigDecimal recaudacionAnioActual;

    private BigDecimal metaAnualImporte;
    private BigDecimal porcentajeCumplimientoMeta;
    private String metaDescripcion; // ej: "Meta no configurada" o "74.50%"

    private BigDecimal saldoPendienteTotal;
    private BigDecimal saldoVencido;
    private BigDecimal saldoNoVencido;
    private BigDecimal saldoExigible;

    private BigDecimal tasaMorosidadFinanciera; // % sobre universo de emisión/cartera
    private BigDecimal tasaMorosidadObligaciones; // % cantidad obligaciones vencidas

    private long totalPagosConfirmados;
    private long totalContribuyentesRegistrados;
    private long totalContribuyentesConDeuda;
    private long totalPrediosRegistrados;
    private long totalConveniosVigentes;
    private long notificacionesPendientesRevisar;

    private OffsetDateTime fechaCorte;
}

