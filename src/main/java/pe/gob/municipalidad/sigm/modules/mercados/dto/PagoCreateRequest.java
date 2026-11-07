package pe.gob.municipalidad.sigm.modules.mercados.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.MedioPagoMercado;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagoCreateRequest {

    @NotNull(message = "El comerciante pagador es obligatorio.")
    private UUID comercianteId;

    @NotNull(message = "El monto recibido es obligatorio.")
    @DecimalMin(value = "0.01", message = "El monto recibido debe ser mayor a 0.00.")
    private BigDecimal montoRecibido;

    @NotNull(message = "El medio de pago es obligatorio.")
    private MedioPagoMercado medioPago;

    private String referenciaOperacion;

    private UUID cajaSesionId;

    private String observaciones;

    @NotEmpty(message = "Debe indicar al menos una obligación o cuota para aplicar el pago.")
    @Valid
    @Builder.Default
    private List<AplicacionItemRequest> aplicaciones = new ArrayList<>();
}
