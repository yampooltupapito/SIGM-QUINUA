package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.AccionHistorialConfiguracion;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoConfiguracionConcepto;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialConfiguracionResponse {
    private UUID id;
    private UUID configuracionId;
    private AccionHistorialConfiguracion accion;
    private String accionDesc;
    private EstadoConfiguracionConcepto estadoAnterior;
    private String estadoAnteriorDesc;
    private EstadoConfiguracionConcepto nuevoEstado;
    private String nuevoEstadoDesc;
    private String nuevoEstadoBadgeClass;
    private String motivo;
    private String resolucionSustento;
    private String usuarioRegistro;
    private OffsetDateTime fechaRegistro;
    private String detallesCambios;
}
