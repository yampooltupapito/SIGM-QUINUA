package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.cajapago;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.MedioPagoTributario;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrarPagoRequest {

    @NotNull(message = "El contribuyente obligado es requerido")
    private UUID contribuyenteId;

    private UUID sesionCajaId;

    @NotNull(message = "El importe total recaudado es obligatorio")
    @DecimalMin(value = "0.01", message = "El importe total debe ser mayor a 0")
    private BigDecimal importeTotal;

    @NotNull(message = "El medio de pago es obligatorio")
    private MedioPagoTributario medioPago;

    @Size(max = 100, message = "La referencia externa no debe exceder 100 caracteres")
    private String referenciaExterna;

    @Size(max = 100, message = "El número de operación no debe exceder 100 caracteres")
    private String numeroOperacionBancaria;

    @Size(max = 100, message = "El banco de origen no debe exceder 100 caracteres")
    private String bancoOrigen;

    private String idempotencyKey;

    private String observaciones;

    @NotEmpty(message = "Debe especificar al menos una aplicación de pago a obligación")
    @Valid
    @Builder.Default
    private List<ItemAplicacionPagoRequest> aplicaciones = new ArrayList<>();
}
