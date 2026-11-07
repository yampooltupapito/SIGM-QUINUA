package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalendarioCuotaRequest {

    @NotNull(message = "El número de cuota es obligatorio")
    private Integer numeroCuota;

    @NotBlank(message = "La descripción de la cuota es obligatoria")
    private String descripcionCuota;

    @NotNull(message = "La fecha de vencimiento es obligatoria")
    private LocalDate fechaVencimiento;

    @Builder.Default
    private Boolean permiteDescuento = false;

    private BigDecimal porcentajeDescuento;

    private UUID normaId;
}
