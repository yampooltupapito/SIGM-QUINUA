package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.fraccionamiento;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimulacionFraccionamientoRequest {

    @NotNull(message = "El programa de fraccionamiento es obligatorio")
    private UUID programaId;

    private List<UUID> obligacionIds;

    @DecimalMin(value = "0.01", message = "El importe base debe ser mayor a 0")
    private BigDecimal importeBase;

    @DecimalMin(value = "0.00", message = "La cuota inicial no puede ser negativa")
    private BigDecimal importeCuotaInicial;

    @NotNull(message = "El número de cuotas es obligatorio")
    @Min(value = 1, message = "Debe solicitar al menos 1 cuota")
    private Integer numeroCuotas;

    private LocalDate fechaInicio;
}
