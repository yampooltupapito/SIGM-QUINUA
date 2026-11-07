package pe.gob.municipalidad.sigm.modules.mercados.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RechazoGestionRequest {

    @NotBlank(message = "El motivo fundado del rechazo es obligatorio")
    private String motivo;

    private Long documentoDecisionId;
}
