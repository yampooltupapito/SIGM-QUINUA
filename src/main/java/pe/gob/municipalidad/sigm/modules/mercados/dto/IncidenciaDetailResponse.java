package pe.gob.municipalidad.sigm.modules.mercados.dto;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoIncidencia;
import pe.gob.municipalidad.sigm.modules.mercados.enums.OrigenIncidencia;
import pe.gob.municipalidad.sigm.modules.mercados.enums.PrioridadIncidencia;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidenciaDetailResponse {
    private UUID id;
    private String codigoIncidencia;
    private UUID mercadoId;
    private String mercadoNombre;
    private UUID sectorId;
    private String sectorNombre;
    private UUID pabellonId;
    private String pabellonNombre;
    private UUID puestoId;
    private String puestoNumero;
    private UUID comercianteId;
    private String comercianteNombre;
    private String comercianteDocumento;
    private UUID tipoIncidenciaId;
    private String tipoIncidenciaNombre;
    private String tipoIncidenciaCodigo;
    private OrigenIncidencia origen;
    private String descripcion;
    private OffsetDateTime fechaReporte;
    private OffsetDateTime fechaOcurrencia;
    private PrioridadIncidencia prioridad;
    private EstadoIncidencia estado;

    // Reportante protegido
    private String reportanteNombre;
    private String reportanteContacto;
    private Boolean esAnonimo;
    private Long reportanteUsuarioId;

    // Asignación
    private Long responsableId;
    private String responsableNombre;
    private Long oficinaResponsableId;
    private String oficinaResponsableNombre;

    // Relaciones
    private UUID inspeccionId;
    private String inspeccionCodigo;
    private UUID expedienteId;
    private String expedienteNumero;
    private String observaciones;

    // Resolución / Cierre
    private OffsetDateTime fechaResolucion;
    private String motivoResolucion;
    private OffsetDateTime fechaCierre;
    private String motivoCierre;

    @Builder.Default
    private List<ActuacionIncidenciaDto> actuaciones = new ArrayList<>();

    @Builder.Default
    private List<EvidenciaIncidenciaDto> evidencias = new ArrayList<>();

    @Builder.Default
    private List<PresuntaInfraccionResponse> presuntasInfracciones = new ArrayList<>();
}
