package pe.gob.municipalidad.sigm.modules.mercados.dto;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoIncidencia;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoActuacionIncidencia;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActuacionIncidenciaDto {
    private UUID id;
    private TipoActuacionIncidencia tipoActuacion;
    private String tipoActuacionLabel;
    private String descripcion;
    private OffsetDateTime fecha;
    private Long responsableId;
    private String responsableNombre;
    private UUID documentoId;
    private EstadoIncidencia estadoAnterior;
    private EstadoIncidencia estadoNuevo;
}
