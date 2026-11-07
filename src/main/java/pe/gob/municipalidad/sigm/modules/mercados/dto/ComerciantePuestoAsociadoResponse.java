package pe.gob.municipalidad.sigm.modules.mercados.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder
public class ComerciantePuestoAsociadoResponse {
    private UUID asignacionId;
    private UUID mercadoId;
    private String mercadoNombre;
    private UUID puestoId;
    private String puestoCodigo;
    private String puestoNumero;
    private String sectorNombre;
    private String pabellonNombre;
    private String tipoAsignacion;
    private String estadoAsignacion;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
}
