package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoUnidadMovil;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoUnidadMovil;

import java.math.BigDecimal;
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
public class UnidadMovilDetailResponse {

    private UUID id;
    private String codigoInterno;
    private TipoUnidadMovil tipoUnidad;
    private String tipoUnidadDesc;
    private String placa;
    private String marca;
    private String modelo;
    private Integer anio;
    private String color;
    private String numeroMotor;
    private String numeroChasis;
    private String codigoPatrimonial;
    private EstadoUnidadMovil estadoOperativo;
    private String estadoOperativoDesc;
    private Long oficinaId;
    private String oficinaNombre;
    private BigDecimal kilometrajeActual;
    private LocalDate fechaIncorporacion;
    private String observaciones;
    private Long version;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    // Relaciones activas e históricas
    private String responsableActualNombre;
    private UUID asignacionActivaId;
    private Boolean disponibleParaAsignacion;

    @Builder.Default
    private List<AsignacionUnidadResponse> asignaciones = new ArrayList<>();

    @Builder.Default
    private List<MantenimientoResponse> mantenimientos = new ArrayList<>();

    @Builder.Default
    private List<IncidenciaTecnicaResponse> incidencias = new ArrayList<>();

    @Builder.Default
    private List<HistorialEstadoUnidadResponse> historialEstados = new ArrayList<>();

    @Builder.Default
    private List<EquipamientoResponse> equipamientosAsignados = new ArrayList<>();
}
