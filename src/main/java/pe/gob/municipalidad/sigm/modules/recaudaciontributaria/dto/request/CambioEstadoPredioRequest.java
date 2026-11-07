package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoPredio;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CambioEstadoPredioRequest {

    @NotNull(message = "El nuevo estado es obligatorio")
    private EstadoPredio nuevoEstado;

    @NotBlank(message = "El motivo del cambio de estado es obligatorio")
    private String motivo;

    private String resolucionSustento;
}
