package pe.gob.municipalidad.sigm.modules.mercados.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CierreCajaRequest {

    @NotNull(message = "El monto final en efectivo contado/declarado es obligatorio.")
    @DecimalMin(value = "0.00", message = "El monto declarado no puede ser negativo.")
    private BigDecimal montoFinalDeclarado;

    private String observaciones;
}
