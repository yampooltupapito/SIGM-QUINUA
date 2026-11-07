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
public class PatrullajeCambioRutaRequest {

    @NotNull(message = "Debe seleccionar la nueva ruta autorizada")
    private UUID nuevaRutaId;

    @NotBlank(message = "El motivo del cambio de ruta es obligatorio")
    private String motivo;
}
