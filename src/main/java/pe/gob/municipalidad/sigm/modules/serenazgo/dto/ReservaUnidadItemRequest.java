package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservaUnidadItemRequest {

    @NotNull(message = "El identificador de la unidad es obligatorio")
    private UUID unidadId;

    private String observaciones;
}
