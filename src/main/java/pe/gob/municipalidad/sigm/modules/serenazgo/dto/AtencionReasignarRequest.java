package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.FuncionOperativaPersonal;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AtencionReasignarRequest {

    private UUID personalAnteriorId;

    private UUID personalNuevoId;

    private FuncionOperativaPersonal funcionNueva;

    private UUID unidadNuevaId;

    private UUID responsableNuevoId;

    @NotBlank(message = "El motivo de la reasignación es obligatorio")
    @Size(max = 1000, message = "El motivo no puede superar los 1000 caracteres")
    private String motivo;
}
