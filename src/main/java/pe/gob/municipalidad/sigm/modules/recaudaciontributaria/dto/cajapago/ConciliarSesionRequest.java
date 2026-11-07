package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.cajapago;

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
public class ConciliarSesionRequest {

    @NotNull(message = "El identificador de la sesión de caja es obligatorio")
    private UUID sesionCajaId;

    @NotNull(message = "El importe verificado en tesorería o arqueo físico es obligatorio")
    @DecimalMin(value = "0.00", message = "El importe verificado no puede ser negativo")
    private BigDecimal importeVerificado;

    private String desgloseDenominacionesJson;

    private String observaciones;
}
