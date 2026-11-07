package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoServicioArbitrio;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetalleCalculoArbitrioResponse {
    private UUID id;
    private UUID servicioId;
    private String codigoServicio;
    private String nombreServicio;
    private TipoServicioArbitrio tipoServicio;
    private String criterioAplicado;
    private BigDecimal parametroBaseValor;
    private String unidadBase;
    private BigDecimal tasaUnitarioMensual;
    private BigDecimal costoMensualServicio;
    private BigDecimal costoAnualServicio;
    private BigDecimal descuentoAplicado;
    private BigDecimal importeFinalMensual;
    private BigDecimal importeFinalAnual;
    private String explicacionDetalle;

    public BigDecimal getImporteBeneficio() {
        return descuentoAplicado != null ? descuentoAplicado : BigDecimal.ZERO;
    }

    public String getServicioNombre() {
        return nombreServicio;
    }

    public String getServicioCodigo() {
        return codigoServicio;
    }

    public String getCriterioNombre() {
        return criterioAplicado != null ? criterioAplicado : "Criterio General";
    }

    public BigDecimal getFactorPredio() {
        return parametroBaseValor != null ? parametroBaseValor : BigDecimal.ONE;
    }

    public BigDecimal getCostoServicioTotal() {
        return costoAnualServicio != null ? costoAnualServicio : BigDecimal.ZERO;
    }

    public BigDecimal getImporteSubtotal() {
        return costoAnualServicio != null ? costoAnualServicio : BigDecimal.ZERO;
    }

    public BigDecimal getImporteCalculado() {
        return importeFinalAnual != null ? importeFinalAnual : BigDecimal.ZERO;
    }

    public String getMetodoDistribucion() {
        return tipoServicio != null ? tipoServicio.getDescripcion() : "-";
    }
}
