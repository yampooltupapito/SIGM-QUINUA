package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReasignarZonaRequest {

    @NotNull(message = "El nuevo sector de destino es obligatorio")
    private UUID nuevoSectorId;

    @NotBlank(message = "El motivo de la reasignación es obligatorio")
    private String motivo;

    private String documentoReferencia;
}
