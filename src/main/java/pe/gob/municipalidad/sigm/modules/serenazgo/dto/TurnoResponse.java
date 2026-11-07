package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoTurnoSerenazgo;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoTurnoSerenazgo;

import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TurnoResponse {

    private UUID id;
    private String codigo;
    private String nombre;
    private String descripcion;
    private TipoTurnoSerenazgo tipoTurno;
    private String tipoTurnoDesc;
    private String colorHex;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private Boolean cruzaMedianoche;
    private Integer duracionReferencialMinutos;
    private String duracionFormateada;
    private EstadoTurnoSerenazgo estado;
    private String estadoDesc;
    private Long oficinaResponsableId;
    private String oficinaResponsableNombre;
    private String observaciones;
    private Long version;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
