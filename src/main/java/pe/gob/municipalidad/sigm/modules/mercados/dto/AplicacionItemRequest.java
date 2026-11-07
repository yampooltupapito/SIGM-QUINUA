package pe.gob.municipalidad.sigm.modules.mercados.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AplicacionItemRequest {

    @NotNull(message = "La obligación a cancelar es obligatoria.")
    private UUID obligacionId;

    private UUID cuotaId;

    @NotNull(message = "El monto aplicado es obligatorio.")
    @DecimalMin(value = "0.01", message = "El monto aplicado debe ser mayor a 0.00.")
    private BigDecimal monto;
}
