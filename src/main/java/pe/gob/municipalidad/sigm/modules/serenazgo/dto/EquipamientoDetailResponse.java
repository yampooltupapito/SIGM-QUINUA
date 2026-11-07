package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoEquipamientoSerenazgo;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoEquipamientoSerenazgo;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipamientoDetailResponse {

    private UUID id;
    private String codigoInterno;
    private TipoEquipamientoSerenazgo tipoEquipo;
    private String tipoEquipoDesc;
    private String descripcion;
    private String marca;
    private String modelo;
    private String numeroSerie;
    private String codigoPatrimonial;
    private Long oficinaResponsableId;
    private String oficinaResponsableNombre;
    private EstadoEquipamientoSerenazgo estado;
    private String estadoDesc;
    private Boolean esControladoPorCantidad;
    private Integer stockTotal;
    private Integer stockDisponible;
    private UUID responsableId;
    private String responsableNombre;
    private UUID unidadMovilId;
    private String unidadMovilInfo;
    private LocalDate fechaAsignacion;
    private LocalDate fechaIncorporacion;
    private String observaciones;
    private Long version;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    private Boolean disponibleParaAsignacion;

    @Builder.Default
    private List<AsignacionEquipamientoResponse> asignaciones = new ArrayList<>();

    @Builder.Default
    private List<MantenimientoResponse> mantenimientos = new ArrayList<>();

    @Builder.Default
    private List<IncidenciaTecnicaResponse> incidencias = new ArrayList<>();
}
