package pe.gob.municipalidad.sigm.modules.mercados.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AprobacionGestionRequest {

    @NotBlank(message = "El sustento o motivación de la aprobación es obligatorio")
    private String motivo;

    private Long documentoDecisionId;
}
