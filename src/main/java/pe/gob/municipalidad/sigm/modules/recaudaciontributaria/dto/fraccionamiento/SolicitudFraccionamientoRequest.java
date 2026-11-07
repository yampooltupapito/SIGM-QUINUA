package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.fraccionamiento;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudFraccionamientoRequest {

    @NotNull(message = "El contribuyente es obligatorio")
    private UUID contribuyenteId;

    @NotNull(message = "El programa de fraccionamiento es obligatorio")
    private UUID programaId;

    @NotEmpty(message = "Debe seleccionar al menos una obligación a fraccionar")
    private List<UUID> obligacionIds;

    @NotNull(message = "El número de cuotas es obligatorio")
    @Min(value = 1, message = "Debe proponer al menos 1 cuota")
    private Integer numeroCuotasPropuestas;

    @DecimalMin(value = "0.00", message = "La cuota inicial no puede ser negativa")
    private BigDecimal montoCuotaInicialPropuesta;

    @NotBlank(message = "El motivo o sustento de la solicitud es obligatorio")
    private String motivo;

    private Long oficinaId;

    private UUID documentoSustentoId;
}
