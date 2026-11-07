package pe.gob.municipalidad.sigm.modules.mercados.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class ComercianteEstadisticasResponse {
    private long totalComerciantes;
    private long personasNaturales;
    private long personasJuridicas;
    private long activos;
    private long pendientesVerificacion;
    private long suspendidos;
    private long inactivos;
    private Map<String, Long> porActividad;
}
