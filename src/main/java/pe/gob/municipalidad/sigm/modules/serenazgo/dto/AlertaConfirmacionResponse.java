package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoAlertaCiudadana;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertaConfirmacionResponse {

    private UUID id;
    private String codigo;
    private String pinConsulta;
    private OffsetDateTime fechaRecepcion;
    private EstadoAlertaCiudadana estado;
    private String estadoDesc;
    private String mensaje;

    public String getCodigoSeguimiento() {
        return codigo;
    }
}
