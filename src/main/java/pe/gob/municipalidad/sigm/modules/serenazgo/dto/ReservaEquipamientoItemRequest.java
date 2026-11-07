package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservaEquipamientoItemRequest {

    @NotNull(message = "El identificador del equipamiento es obligatorio")
    private UUID equipamientoId;

    @Min(value = 1, message = "La cantidad reservada debe ser al menos 1")
    @Builder.Default
    private Integer cantidad = 1;

    private String observaciones;
}
