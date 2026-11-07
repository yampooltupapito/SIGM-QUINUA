package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.fraccionamiento;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoValidacionDetalleFraccionamiento;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetalleObligacionFraccionadaDto {
    private UUID id;
    private UUID obligacionId;
    private String codigoObligacion;
    private String conceptoNombre;
    private String periodoAnio;
    private Integer cuotaNumero;
    private LocalDate fechaVencimiento;
    private BigDecimal saldoEvaluado;
    private LocalDate fechaCorte;
    private EstadoValidacionDetalleFraccionamiento estadoValidacion;
    private String estadoDescripcion;
    private String estadoBadgeClass;
    private String observaciones;
}
