package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.cajapago;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemAplicacionPagoRequest {

    @NotNull(message = "El identificador de la obligación es obligatorio")
    private UUID obligacionId;

    private UUID cuotaId;

    @NotNull(message = "El importe a aplicar es obligatorio")
    @DecimalMin(value = "0.01", message = "El importe aplicado debe ser mayor a 0")
    private BigDecimal importeAplicado;

    private String observaciones;
}
