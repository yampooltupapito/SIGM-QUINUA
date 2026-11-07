package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstadisticasPersonalResponse {
    private long totalRegistrados;
    private long totalActivos;
    private long totalInactivos;
    private long totalSuspendidos;
    private long totalRetirados;
    private long totalDisponiblesHoy;
    private Map<String, Long> distribucionPorCargo;
}
