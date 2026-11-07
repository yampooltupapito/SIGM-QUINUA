package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoUnidadMovil;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CambioEstadoUnidadRequest {

    @NotNull(message = "El nuevo estado operativo es obligatorio")
    private EstadoUnidadMovil nuevoEstado;

    @NotBlank(message = "Debe fundamentar el motivo del cambio de estado")
    private String motivo;

    private Long documentoSustentoId;
}
