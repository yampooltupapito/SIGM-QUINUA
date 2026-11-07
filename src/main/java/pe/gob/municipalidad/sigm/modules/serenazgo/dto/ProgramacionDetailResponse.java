package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoProgramacionServicio;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoServicioSerenazgo;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgramacionDetailResponse {

    private UUID id;
    private String codigo;
    private LocalDate fechaServicio;
    private OffsetDateTime fechaHoraInicio;
    private OffsetDateTime fechaHoraFin;
    private String horarioFormateado;
    private Integer duracionMinutos;

    // Turno
    private UUID turnoId;
    private String turnoCodigo;
    private String turnoNombre;
    private LocalTime turnoHoraInicio;
    private LocalTime turnoHoraFin;
    private Boolean turnoCruzaMedianoche;

    // Sector y Zona
    private UUID sectorId;
    private String sectorCodigo;
    private String sectorNombre;
    private UUID zonaId;
    private String zonaCodigo;
    private String zonaNombre;

    // Oficina
    private Long oficinaResponsableId;
    private String oficinaResponsableNombre;

    // Tipo y Estado
    private TipoServicioSerenazgo tipoServicio;
    private String tipoServicioDesc;
    private EstadoProgramacionServicio estado;
    private String estadoDesc;

    // Responsable del servicio
    private UUID responsableServicioId;
    private String responsableServicioNombre;
    private String responsableServicioCodigo;

    // Usuario programador
    private String usuarioProgramacionNombre;

    private String descripcion;
    private String observaciones;
    private String motivoReprogramacion;
    private String motivoCancelacion;
    private Long version;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    // Colecciones asociadas
    @Builder.Default
    private List<ProgramacionPersonalResponse> personalAsignado = new ArrayList<>();

    @Builder.Default
    private List<ProgramacionUnidadResponse> unidadesReservadas = new ArrayList<>();

    @Builder.Default
    private List<ProgramacionEquipamientoResponse> equipamientoReservado = new ArrayList<>();

    @Builder.Default
    private List<ProgramacionHistorialResponse> historial = new ArrayList<>();
}
