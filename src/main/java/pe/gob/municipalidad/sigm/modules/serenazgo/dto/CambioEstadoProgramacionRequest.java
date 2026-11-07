package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoProgramacionServicio;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CambioEstadoProgramacionRequest {

    @NotNull(message = "El nuevo estado es obligatorio")
    private EstadoProgramacionServicio nuevoEstado;

    @NotBlank(message = "El motivo del cambio de estado es obligatorio")
    private String motivo;
}
