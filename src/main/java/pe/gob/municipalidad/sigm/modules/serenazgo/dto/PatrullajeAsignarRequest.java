package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatrullajeAsignarRequest {

    @NotNull(message = "El responsable del patrullaje es obligatorio")
    private UUID responsableId;

    private UUID unidadMovilId;

    @Builder.Default
    private List<UUID> participantesIds = new ArrayList<>();

    @Builder.Default
    private List<UUID> equipamientoIds = new ArrayList<>();

    private String observaciones;
}
