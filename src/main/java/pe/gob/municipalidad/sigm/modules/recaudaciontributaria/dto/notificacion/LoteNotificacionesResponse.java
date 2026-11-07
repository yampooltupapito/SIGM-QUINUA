package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.notificacion;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoLoteNotificaciones;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoteNotificacionesResponse {

    private UUID id;
    private String codigo;
    private UUID tipoNotificacionId;
    private String tipoNotificacionNombre;
    private OffsetDateTime fechaCreacion;
    private EstadoLoteNotificaciones estado;
    private Integer totalRegistros;
    private Integer totalValidos;
    private Integer totalObservados;
    private Long responsableId;
    private String responsableNombre;
    private Long oficinaId;
    private String oficinaNombre;
    private String descripcion;
}

