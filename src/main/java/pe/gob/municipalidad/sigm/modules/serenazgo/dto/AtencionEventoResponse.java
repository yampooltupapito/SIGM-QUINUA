package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoEventoAtencion;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AtencionEventoResponse {

    private UUID id;
    private TipoEventoAtencion tipoEvento;
    private String tipoEventoDesc;
    private String descripcion;
    private String estadoAnterior;
    private String estadoNuevo;
    private String personalNombre;
    private String usuarioNombre;
    private OffsetDateTime fechaEvento;
    private String detallesJson;
    private Boolean visibleCiudadano;

    public String getRegistradoPorNombre() {
        return usuarioNombre != null ? usuarioNombre : personalNombre;
    }

    public String getFechaHoraFormateada() {
        return FormatoFechaVista.fechaHora(fechaEvento);
    }

    public boolean isVisibleCiudadano() {
        return Boolean.TRUE.equals(visibleCiudadano);
    }
}
