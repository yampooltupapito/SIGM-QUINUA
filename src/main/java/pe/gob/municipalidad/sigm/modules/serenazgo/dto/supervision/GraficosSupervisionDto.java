package pe.gob.municipalidad.sigm.modules.serenazgo.dto.supervision;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GraficosSupervisionDto {

    private Map<String, Long> incidenciasPorEstado;
    private Map<String, Long> patrullajesPorEstado;
    private Map<String, Long> intervencionesPorTipo;
    private Map<String, Long> seguimientosPorEstado;
    private Map<String, Long> seguimientosPorPrioridad;
    private Map<String, Long> atencionesPorEstado;
}
