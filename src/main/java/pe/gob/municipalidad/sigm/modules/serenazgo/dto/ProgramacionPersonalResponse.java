package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoAsignacionPersonal;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.FuncionOperativaPersonal;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgramacionPersonalResponse {

    private UUID id;
    private UUID personalId;
    private String codigoInterno;
    private String nombreCompleto;
    private String cargo;
    private FuncionOperativaPersonal funcionOperativa;
    private String funcionOperativaDesc;
    private Boolean esResponsable;
    private EstadoAsignacionPersonal estadoAsignacion;
    private String estadoAsignacionDesc;
    private OffsetDateTime fechaAsignacion;
    private String observaciones;
}
