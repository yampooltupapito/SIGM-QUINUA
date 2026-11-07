package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.deuda;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrarCompromisoDeudaRequest {

    @NotNull(message = "La fecha comprometida de pago es obligatoria")
    @FutureOrPresent(message = "La fecha comprometida no puede ser anterior a hoy")
    private LocalDate fechaComprometida;

    @DecimalMin(value = "0.01", message = "El monto comprometido debe ser mayor a cero")
    private BigDecimal montoComprometido;

    private String observaciones;
}
