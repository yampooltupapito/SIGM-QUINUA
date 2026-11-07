package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.CategoriaCostoArbitrio;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CostoItemRequest {

    @NotNull(message = "La categoría del costo es obligatoria")
    private CategoriaCostoArbitrio categoriaCosto;

    @NotBlank(message = "La descripción del componente de costo es obligatoria")
    private String descripcion;

    @NotNull(message = "El importe es obligatorio")
    @DecimalMin(value = "0.01", message = "El importe del costo debe ser mayor a 0")
    private BigDecimal importe;

    private String periodoCosto;
    private String documentoSustentoRef;
    private String fuenteSustento;
}
