package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.FuncionOperativaPersonal;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsignacionPersonalItemRequest {

    @NotNull(message = "El identificador del personal es obligatorio")
    private UUID personalId;

    @Builder.Default
    private FuncionOperativaPersonal funcionOperativa = FuncionOperativaPersonal.PATRULLERO;

    @Builder.Default
    private Boolean esResponsable = false;

    private String observaciones;
}
