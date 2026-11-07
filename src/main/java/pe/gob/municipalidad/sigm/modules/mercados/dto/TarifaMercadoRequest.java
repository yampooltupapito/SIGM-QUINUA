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
public class TarifaMercadoRequest {

    @NotNull(message = "El concepto de cobro es obligatorio.")
    private UUID conceptoId;

    private UUID mercadoId;

    private Long tipoPuestoId;

    @NotNull(message = "El monto es obligatorio.")
    @DecimalMin(value = "0.00", message = "El monto de la tarifa no puede ser negativo.")
    private BigDecimal monto;

    @Builder.Default
    private String moneda = "PEN";

    @NotNull(message = "La fecha de inicio de vigencia es obligatoria.")
    private LocalDate fechaInicioVigencia;

    private LocalDate fechaFinVigencia;

    @Builder.Default
    private String estado = "ACTIVA";

    private UUID documentoSustentoId;

    private String fuenteNormativa;
}
