package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoSectorPatrullaje;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CambioEstadoTerritorioRequest {

    @NotNull(message = "El nuevo estado es obligatorio")
    private EstadoSectorPatrullaje nuevoEstado;

    @NotBlank(message = "El motivo del cambio de estado es obligatorio")
    private String motivo;
}
