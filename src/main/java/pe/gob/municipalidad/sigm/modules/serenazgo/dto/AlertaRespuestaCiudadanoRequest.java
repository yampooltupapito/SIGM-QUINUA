package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertaRespuestaCiudadanoRequest {

    private String codigo;

    private String pinConsulta;

    private UUID solicitudId;

    @NotBlank(message = "La respuesta o aclaración ciudadana es obligatoria")
    @Size(max = 2000, message = "La respuesta no puede exceder los 2000 caracteres")
    private String respuesta;
}
