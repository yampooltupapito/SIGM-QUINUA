package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoEventoHistorialAlerta;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertaHistorialResponse {

    private UUID id;
    private TipoEventoHistorialAlerta tipoEvento;
    private String tipoEventoDesc;
    private String estadoAnterior;
    private String estadoNuevo;
    private String motivo;
    private Boolean esVisibleCiudadano;
    private String usuarioNombre;
    private OffsetDateTime createdAt;
}
