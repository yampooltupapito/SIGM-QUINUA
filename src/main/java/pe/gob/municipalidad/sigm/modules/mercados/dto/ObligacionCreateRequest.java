package pe.gob.municipalidad.sigm.modules.mercados.dto;

import jakarta.validation.constraints.DecimalMin;
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
public class ObligacionCreateRequest {

    @NotNull(message = "El comerciante es obligatorio.")
    private UUID comercianteId;

    private UUID asignacionId;

    private UUID puestoId;

    @NotNull(message = "El concepto de cobro es obligatorio.")
    private UUID conceptoId;

    private UUID tarifaId;

    private LocalDate periodoInicio;

    private LocalDate periodoFin;

    @Builder.Default
    private LocalDate fechaEmision = LocalDate.now();

    private LocalDate fechaVencimiento;

    @NotNull(message = "El monto es obligatorio.")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0.00.")
    private BigDecimal montoOriginal;

    @Builder.Default
    private String moneda = "PEN";

    @Builder.Default
    private Integer numeroCuotas = 1;

    private UUID expedienteId;

    private UUID documentoSustentoId;

    private String observaciones;
}
