package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.deuda;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoGestionDeuda;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrarGestionDeudaRequest {

    @NotNull(message = "El tipo de gestión es obligatorio")
    private TipoGestionDeuda tipoGestion;

    @NotBlank(message = "El resultado de la gestión es obligatorio")
    @Size(max = 150, message = "El resultado no puede exceder 150 caracteres")
    private String resultado;

    private String observaciones;

    private Long documentoSustentoId;
}
