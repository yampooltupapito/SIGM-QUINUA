package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoSectorPatrullaje;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SectorDetailResponse {
    private UUID id;
    private String codigo;
    private String nombre;
    private String descripcion;
    private String colorHex;
    private EstadoSectorPatrullaje estado;
    private String estadoDesc;
    private Long oficinaResponsableId;
    private String oficinaResponsableNombre;
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

    private List<ZonaPatrullajeResponse> zonas;
    private List<HistorialDelimitacionResponse> historialDelimitaciones;
    private List<HistorialEstadoTerritorioResponse> historialEstados;
}
