package pe.gob.municipalidad.sigm.modules.mercados.dto;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoActaInspeccion;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActaInspeccionResponse {

    private UUID id;
    private UUID inspeccionId;
    private String codigoInspeccion;
    private String codigoActa;
    private OffsetDateTime fechaElaboracion;
    private String resumen;
    private String conclusiones;
    private String recomendaciones;
    private EstadoActaInspeccion estado;
    private String estadoDescripcion;
    private String codigoVerificacion;
    private String hashSha256;
    private Long responsableId;
    private String responsableNombre;
}
