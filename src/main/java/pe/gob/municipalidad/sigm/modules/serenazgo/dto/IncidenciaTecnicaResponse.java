package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoIncidenciaTecnica;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.NivelGravedadIncidencia;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoIncidenciaTecnica;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoRecursoSeguridad;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidenciaTecnicaResponse {

    private UUID id;
    private String codigo;
    private TipoRecursoSeguridad tipoRecurso;
    private String tipoRecursoDesc;
    private UUID unidadId;
    private String unidadInfo;
    private UUID equipamientoId;
    private String equipamientoInfo;
    private TipoIncidenciaTecnica tipoIncidencia;
    private String tipoIncidenciaDesc;
    private OffsetDateTime fechaReporte;
    private String descripcion;
    private UUID personalReportaId;
    private String personalReportaNombre;
    private NivelGravedadIncidencia nivelGravedad;
    private String nivelGravedadDesc;
    private EstadoIncidenciaTecnica estado;
    private String estadoDesc;
    private String accionesTomadas;
    private String usuarioRegistro;
    private OffsetDateTime createdAt;
}
