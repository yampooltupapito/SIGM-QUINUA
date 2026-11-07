package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.portal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraccionamientoCiudadanoDto {
    private UUID id;
    private String codigoConvenio;
    private String resolucionAprobacion;
    private LocalDate fechaAprobacion;
    private BigDecimal montoTotalFraccionado;
    private BigDecimal cuotaInicial;
    private BigDecimal saldoFinanciado;
    private Integer numeroCuotas;
    private Integer cuotasPagadas;
    private BigDecimal saldoPendiente;
    private String estado;
    private String programaNombre;
    private List<CuotaConvenioCiudadanoDto> cronogramaCuotas;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CuotaConvenioCiudadanoDto {
        private Integer numeroCuota;
        private LocalDate fechaVencimiento;
        private BigDecimal montoAmortizacion;
        private BigDecimal montoInteres;
        private BigDecimal montoCuotaTotal;
        private BigDecimal montoPagado;
        private String estadoCuota;
        private LocalDate fechaPago;
        private boolean vencida;
    }
}

