package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.NivelGravedadIncidencia;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoIncidenciaTecnica;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoRecursoSeguridad;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistroIncidenciaTecnicaRequest {

    @NotNull(message = "El tipo de recurso es obligatorio")
    private TipoRecursoSeguridad tipoRecurso;

    private UUID unidadId;
    private UUID equipamientoId;

    @NotNull(message = "El tipo de incidencia es obligatorio")
    private TipoIncidenciaTecnica tipoIncidencia;

    @NotBlank(message = "La descripción de la falla o problema es obligatoria")
    private String descripcion;

    private UUID personalReportaId;

    @Builder.Default
    private NivelGravedadIncidencia nivelGravedad = NivelGravedadIncidencia.MEDIA;
}
