package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DescarteIncidenciaRequest {

    @NotBlank(message = "El motivo de descarte es obligatorio (ej. duplicidad, error de registro, falsa alarma)")
    private String motivoDescarte;
}
