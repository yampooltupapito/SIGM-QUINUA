package pe.gob.municipalidad.sigm.modules.mercados.dto;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoIncidencia;
import pe.gob.municipalidad.sigm.modules.mercados.enums.OrigenIncidencia;
import pe.gob.municipalidad.sigm.modules.mercados.enums.PrioridadIncidencia;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidenciaResponse {
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
    private UUID tipoIncidenciaId;
    private String tipoIncidenciaNombre;
    private String tipoIncidenciaCodigo;
    private OrigenIncidencia origen;
    private String descripcion;
    private OffsetDateTime fechaReporte;
    private OffsetDateTime fechaOcurrencia;
    private PrioridadIncidencia prioridad;
    private EstadoIncidencia estado;
    private Long responsableId;
    private String responsableNombre;
    private Long oficinaResponsableId;
    private String oficinaResponsableNombre;
    private UUID inspeccionId;
    private String inspeccionCodigo;
    private UUID expedienteId;
    private String expedienteNumero;
    private Integer totalActuaciones;
    private Integer totalEvidencias;
}
