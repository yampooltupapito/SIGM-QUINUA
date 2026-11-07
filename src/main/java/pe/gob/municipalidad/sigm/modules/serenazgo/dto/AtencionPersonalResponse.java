package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.FuncionOperativaPersonal;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AtencionPersonalResponse {

    private UUID id;
    private UUID personalId;
    private String codigoInterno;
    private String nombreCompleto;
    private String cargoNombre;
    private FuncionOperativaPersonal funcionOperativa;
    private String funcionOperativaDesc;
    private Boolean esResponsable;
    private OffsetDateTime fechaAsignacion;
    private OffsetDateTime fechaDesasignacion;
    private String motivoDesasignacion;
    private Boolean activo;
    private String observaciones;
}
