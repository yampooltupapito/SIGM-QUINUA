package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoSectorPatrullaje;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.PrioridadOperativaZona;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZonaDetailResponse {
    private UUID id;
    private UUID sectorId;
    private String sectorCodigo;
    private String sectorNombre;
    private String sectorColorHex;
    private String codigo;
    private String nombre;
    private String descripcion;
    private EstadoSectorPatrullaje estado;
    private String estadoDesc;
    private PrioridadOperativaZona prioridadOperativa;
    private String prioridadOperativaDesc;
    private LocalDate fechaInicioVigencia;
    private LocalDate fechaFinVigencia;
    private String observaciones;
    private Boolean tieneDelimitacion;
    private String geometriaGeojson;
    private Long version;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    private List<HistorialReasignacionResponse> historialReasignaciones;
    private List<HistorialDelimitacionResponse> historialDelimitaciones;
    private List<HistorialEstadoTerritorioResponse> historialEstados;
}
