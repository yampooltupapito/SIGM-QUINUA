package pe.gob.municipalidad.sigm.modules.mercados.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortalComercianteEstadoCuentaResponse {
    private String codigoComerciante;
    private String titularNombre;
    private LocalDate fechaCorte;
    private BigDecimal deudaTotalExigible;
    private BigDecimal totalHistoricoPagado;
    private int cantidadObligacionesPendientes;
    private List<PortalComercianteObligacionResponse> obligacionesPendientes;
    private List<PortalComerciantePagoResponse> ultimosPagos;
}
