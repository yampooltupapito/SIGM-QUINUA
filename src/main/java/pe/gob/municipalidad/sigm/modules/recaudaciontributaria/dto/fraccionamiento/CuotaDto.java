package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.fraccionamiento;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoCuotaFraccionamiento;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CuotaDto {
    private UUID id;
    private Integer numeroCuota;
    private LocalDate fechaVencimiento;
    private BigDecimal importeCapital;
    private BigDecimal importeInteres;
    private BigDecimal importeTotal;
    private BigDecimal importePagado;
    private BigDecimal saldo;
    private EstadoCuotaFraccionamiento estado;
    private String estadoDescripcion;
    private String estadoBadgeClass;
    private LocalDate fechaPago;
    private boolean vencida;
}
