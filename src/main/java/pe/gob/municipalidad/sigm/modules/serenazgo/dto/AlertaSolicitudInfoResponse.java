package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoSolicitudInfoAlerta;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertaSolicitudInfoResponse {

    private UUID id;
    private String solicitud;
    private OffsetDateTime fechaSolicitud;
    private String usuarioSolicitaNombre;
    private String respuestaCiudadano;
    private OffsetDateTime fechaRespuesta;
    private EstadoSolicitudInfoAlerta estado;
    private String estadoDesc;

    public String getRequerimiento() {
        return solicitud;
    }
}
