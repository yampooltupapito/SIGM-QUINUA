package pe.gob.municipalidad.sigm.modules.mercados.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoComerciante;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComercianteEstadoRequest {

    @NotNull(message = "El nuevo estado del comerciante es obligatorio")
    private EstadoComerciante nuevoEstado;

    @NotBlank(message = "El motivo del cambio de estado es obligatorio")
    @Size(min = 5, max = 500, message = "El motivo debe tener entre 5 y 500 caracteres")
    private String motivo;

    private Long documentoSustentoId;

    @Size(max = 1000, message = "Las observaciones no pueden exceder 1000 caracteres")
    private String observaciones;
}
