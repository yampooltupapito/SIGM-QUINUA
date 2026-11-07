package pe.gob.municipalidad.sigm.modules.mercados.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoAsignacion;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoAsignacion;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortalComercianteAsignacionResponse {
    private UUID asignacionId;
    private String codigoAsignacion;
    private UUID puestoId;
    private String puestoCodigo;
    private String mercadoNombre;
    private String sectorNombre;
    private String pabellonNombre;
    private LocalDate fechaSolicitud;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private EstadoAsignacion estado;
    private String estadoLabel;
    private TipoAsignacion tipoAsignacion;
    private boolean vigente;
}
