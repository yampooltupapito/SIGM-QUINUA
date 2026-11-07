package pe.gob.municipalidad.sigm.modules.mercados.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReversoPagoRequest {

    @NotBlank(message = "El motivo del reverso es obligatorio.")
    private String motivo;

    private String documentoSustento;
}
