package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoProgramacionServicio;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoServicioSerenazgo;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgramacionResponse {

    private UUID id;
    private String codigo;
    private LocalDate fechaServicio;
    private OffsetDateTime fechaHoraInicio;
    private OffsetDateTime fechaHoraFin;
    private LocalTime turnoHoraInicio;
    private LocalTime turnoHoraFin;
    private Boolean turnoCruzaMedianoche;
    private String turnoNombre;
    private UUID turnoId;
    private String sectorNombre;
    private String sectorCodigo;
    private UUID sectorId;
    private String zonaNombre;
    private String zonaCodigo;
    private UUID zonaId;
    private TipoServicioSerenazgo tipoServicio;
    private String tipoServicioDesc;
    private EstadoProgramacionServicio estado;
    private String estadoDesc;
    private String responsableServicioNombre;
    private Integer totalPersonalAsignado;
    private Integer totalUnidadesReservadas;
    private Integer totalEquipamientoReservado;
    private String descripcion;
    private Long version;
    private OffsetDateTime createdAt;
}
