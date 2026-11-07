package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.AccionHistorialCalculoPredial;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoCalculoPredial;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialCalculoResponse {

    private UUID id;
    private AccionHistorialCalculoPredial accion;
    private String accionDescripcion;
    private EstadoCalculoPredial estadoAnterior;
    private EstadoCalculoPredial nuevoEstado;
    private String motivo;
    private String resolucionSustento;
    private String usuarioRegistro;
    private OffsetDateTime fechaRegistro;
    private String detallesCambios;
}
