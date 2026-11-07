package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertaSolicitarInfoRequest {

    @NotBlank(message = "El mensaje o requerimiento de información al ciudadano es obligatorio")
    @Size(max = 1000, message = "El requerimiento no puede superar los 1000 caracteres")
    private String solicitud;
}
