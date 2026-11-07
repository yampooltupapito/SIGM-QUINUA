package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertaDescarteRequest {

    @NotBlank(message = "El motivo de descarte es obligatorio")
    private String motivoDescarte;
}
