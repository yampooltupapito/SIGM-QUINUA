package pe.gob.municipalidad.sigm.modules.mercados.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AsignacionObservacionRequest {

    @NotBlank(message = "El detalle de la observación administrativa es obligatorio")
    @Size(max = 2000, message = "La observación no puede exceder 2000 caracteres")
    private String detalleObservacion;

    private Long documentoSustentoId;
}
