package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoIncidenciaTecnica;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResolverIncidenciaTecnicaRequest {

    @NotBlank(message = "Debe describir las acciones técnicas o correctivas tomadas")
    private String accionesTomadas;

    @Builder.Default
    private EstadoIncidenciaTecnica nuevoEstado = EstadoIncidenciaTecnica.RESUELTA;
}
