package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CierreIncidenciaRequest {

    @NotBlank(message = "El motivo del cierre administrativo es obligatorio")
    private String motivoCierre;

    @NotBlank(message = "El resumen o resultado de las actuaciones de seguimiento es obligatorio")
    private String resultadoAtencion;
}
