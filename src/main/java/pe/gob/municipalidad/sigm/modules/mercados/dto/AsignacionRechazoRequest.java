package pe.gob.municipalidad.sigm.modules.mercados.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AsignacionRechazoRequest {

    @NotBlank(message = "El motivo de rechazo o denegación es obligatorio")
    @Size(max = 2000, message = "El motivo no puede exceder 2000 caracteres")
    private String motivoRechazo;

    private Long documentoSustentoId;
}
