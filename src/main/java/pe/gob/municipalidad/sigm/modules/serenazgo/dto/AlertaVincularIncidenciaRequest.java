package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertaVincularIncidenciaRequest {

    private UUID incidenciaId;

    @Builder.Default
    private Boolean crearNuevaIncidencia = false;

    @NotBlank(message = "El motivo o justificación de vinculación es obligatorio")
    private String motivo;
}
