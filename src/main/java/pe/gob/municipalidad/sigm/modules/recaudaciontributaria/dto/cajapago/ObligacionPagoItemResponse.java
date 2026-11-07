package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.cajapago;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ObligacionPagoItemResponse {
    private UUID obligacionId;
    private String codigoObligacion;
    private String conceptoNombre;
    private Integer anioPeriodo;
    private String predioCodigo;
    private String predioDireccion;
    private Integer cuotaNumero;
    private Integer totalCuotas;
    private BigDecimal importeVigente;
    private BigDecimal saldoPendiente;
    private LocalDate fechaVencimiento;
    private String estadoFinanciero;
    private String estadoExigibilidad;
    @Builder.Default
    private List<CuotaItemResponse> cuotas = new ArrayList<>();

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CuotaItemResponse {
        private UUID cuotaId;
        private Integer numeroCuota;
        private BigDecimal importe;
        private BigDecimal importePagado;
        private BigDecimal saldo;
        private LocalDate fechaVencimiento;
        private String estado;
    }
}
