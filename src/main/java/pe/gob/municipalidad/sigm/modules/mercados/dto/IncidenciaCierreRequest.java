package pe.gob.municipalidad.sigm.modules.mercados.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoIncidencia;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidenciaCierreRequest {

    @NotNull(message = "El estado final es obligatorio")
    private EstadoIncidencia estadoFinal; // CERRADA o DESCARTADA

    @NotBlank(message = "El motivo de cierre o descarte es obligatorio")
    @Size(min = 10, max = 2000, message = "El motivo debe tener entre 10 y 2000 caracteres")
    private String motivoCierre;
}
