package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgregarComentarioRequest {

    @NotBlank(message = "El texto de la observación o seguimiento es obligatorio")
    private String comentario;

    @Builder.Default
    private Boolean esInterno = true;
}
