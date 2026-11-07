package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.portal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Indicadores consolidados en tiempo real para el panel ciudadano "Mi Portal Tributario".
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortalTributarioDashboardDto {
    private String nombreCiudadano;
    private PortalTributarioContextoDto contribuyenteActivo;
    private long totalPredios;
    private long totalObligacionesPendientes;
    private BigDecimal saldoPendienteTotal;
    private BigDecimal saldoVencido;
    private BigDecimal totalPagadoUltimoAnio;
    private long totalConveniosVigentes;
    private long totalNotificacionesSinLeer;
    private OffsetDateTime fechaCorte;
    private List<PagoCiudadanoDto> ultimosPagos;
    private List<ObligacionCiudadanoDto> proximosVencimientos;
    private List<NotificacionCiudadanoDto> comunicacionesRecientes;
}

