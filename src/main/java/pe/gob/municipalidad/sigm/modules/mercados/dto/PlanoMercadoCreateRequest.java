package pe.gob.municipalidad.sigm.modules.mercados.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanoMercadoCreateRequest {

    @NotNull(message = "El mercado es obligatorio")
    private UUID mercadoId;

    @NotBlank(message = "El código del plano es obligatorio")
    @Size(max = 50, message = "El código no debe exceder 50 caracteres")
    private String codigo;

    @NotBlank(message = "El nombre del plano es obligatorio")
    @Size(max = 150, message = "El nombre no debe exceder 150 caracteres")
    private String nombre;

    @Size(max = 500, message = "La descripción no debe exceder 500 caracteres")
    private String descripcion;

    @NotNull(message = "El ancho base es obligatorio")
    @DecimalMin(value = "100.0", message = "El ancho base debe ser de al menos 100")
    private BigDecimal anchoBase;

    @NotNull(message = "El alto base es obligatorio")
    @DecimalMin(value = "100.0", message = "El alto base debe ser de al menos 100")
    private BigDecimal altoBase;

    private String unidadReferencia;
}
