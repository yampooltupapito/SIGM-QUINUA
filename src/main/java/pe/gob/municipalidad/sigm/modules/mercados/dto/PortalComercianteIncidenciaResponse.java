package pe.gob.municipalidad.sigm.modules.mercados.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoIncidencia;
import pe.gob.municipalidad.sigm.modules.mercados.enums.PrioridadIncidencia;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortalComercianteIncidenciaResponse {
    private UUID incidenciaId;
    private String codigoIncidencia;
    private String tipoNombre;
    private String mercadoNombre;
    private String puestoCodigo;
    private OffsetDateTime fechaReporte;
    private EstadoIncidencia estado;
    private String estadoDescripcion;
    private PrioridadIncidencia prioridad;
    private String prioridadDescripcion;
    private String descripcion;
    private String resolucion;
}
