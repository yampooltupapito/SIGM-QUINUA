package pe.gob.municipalidad.sigm.modules.mercados.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.ResultadoVerificacion;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerificacionItemRequest {

    @NotNull(message = "El ID del ítem de verificación es obligatorio")
    private UUID itemVerificacionId;

    @NotNull(message = "El resultado de verificación es obligatorio")
    private ResultadoVerificacion resultado;

    private String observaciones;
}
