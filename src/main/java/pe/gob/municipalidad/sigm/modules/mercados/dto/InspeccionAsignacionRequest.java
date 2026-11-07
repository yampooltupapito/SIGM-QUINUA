package pe.gob.municipalidad.sigm.modules.mercados.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InspeccionAsignacionRequest {

    @NotNull(message = "El inspector responsable es obligatorio")
    private Long inspectorResponsableId;

    private List<ParticipanteAsignacionDto> participantesAdicionales;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParticipanteAsignacionDto {
        @NotNull(message = "El ID del usuario es obligatorio")
        private Long usuarioId;

        private String funcion;
    }
}
