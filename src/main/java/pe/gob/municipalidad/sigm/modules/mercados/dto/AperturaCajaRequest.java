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
public class AperturaCajaRequest {

    @NotNull(message = "La caja es obligatoria.")
    private UUID cajaId;

    @NotNull(message = "El monto inicial en efectivo es obligatorio.")
    @DecimalMin(value = "0.00", message = "El monto inicial no puede ser negativo.")
    private BigDecimal montoInicial;

    private String observaciones;
}
