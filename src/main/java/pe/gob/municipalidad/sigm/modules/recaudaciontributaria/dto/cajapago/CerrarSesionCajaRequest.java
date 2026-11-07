package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.cajapago;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CerrarSesionCajaRequest {

    @NotNull(message = "El monto físico de cierre contado en arqueo es obligatorio")
    @DecimalMin(value = "0.00", message = "El monto de cierre no puede ser negativo")
    private BigDecimal montoCierreDeclarado;

    private String desgloseDenominacionesJson;

    private String justificacionDiferencia;

    private String observaciones;
}
