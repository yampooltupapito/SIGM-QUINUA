package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoContribuyente;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialEstadoResponse {
    private UUID id;
    private EstadoContribuyente estadoAnterior;
    private String estadoAnteriorDesc;
    private EstadoContribuyente nuevoEstado;
    private String nuevoEstadoDesc;
    private String nuevoEstadoBadgeClass;
    private String motivo;
    private String resolucionSustento;
    private String usuarioRegistro;
    private OffsetDateTime fechaRegistro;
}
