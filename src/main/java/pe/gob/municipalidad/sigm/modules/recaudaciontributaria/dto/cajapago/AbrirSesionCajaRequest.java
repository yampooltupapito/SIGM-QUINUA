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
public class AbrirSesionCajaRequest {

    @NotNull(message = "Debe especificar la caja municipal a aperturar")
    private UUID cajaId;

    @NotNull(message = "El monto inicial de apertura (fondo de sencillo) es obligatorio")
    @DecimalMin(value = "0.00", message = "El fondo inicial no puede ser negativo")
    private BigDecimal montoApertura;

    private String observaciones;
}
