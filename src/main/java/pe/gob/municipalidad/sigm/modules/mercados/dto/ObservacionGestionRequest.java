package pe.gob.municipalidad.sigm.modules.mercados.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ObservacionGestionRequest {

    @NotBlank(message = "La descripción de la observación es obligatoria")
    private String descripcion;

    private String requisitoRelacionado;

    private Long documentoId;
}
