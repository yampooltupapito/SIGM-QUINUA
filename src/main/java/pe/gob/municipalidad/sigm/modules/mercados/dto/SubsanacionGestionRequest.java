package pe.gob.municipalidad.sigm.modules.mercados.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubsanacionGestionRequest {

    @NotNull(message = "El identificador de la observación es obligatorio")
    private Long observacionId;

    @NotBlank(message = "El detalle de la subsanación es obligatorio")
    private String descripcion;

    private Long documentoId;
}
