package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoIncidenciaSeguridad;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CambioEstadoIncidenciaRequest {

    @NotNull(message = "El nuevo estado es obligatorio")
    private EstadoIncidenciaSeguridad nuevoEstado;

    @NotBlank(message = "El motivo de la transición de estado es obligatorio")
    private String motivo;
}
