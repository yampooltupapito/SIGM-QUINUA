package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AtencionEquipamientoResponse {

    private UUID id;
    private UUID equipamientoId;
    private String codigoInterno;
    private String descripcion;
    private String tipoEquipoDesc;
    private Integer cantidad;
    private OffsetDateTime fechaAsignacion;
    private OffsetDateTime fechaDevolucion;
    private Boolean devuelto;
    private String observaciones;
}
