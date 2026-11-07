package pe.gob.municipalidad.sigm.modules.mercados.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActividadComercianteRequest {

    @NotNull(message = "El identificador de la actividad comercial es obligatorio")
    private Long actividadId;

    private boolean principal;

    private String estado;
}
