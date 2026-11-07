package pe.gob.municipalidad.sigm.modules.serenazgo.dto.mapa;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MapaEstadisticasResponse {

    private long totalIncidencias;
    private long incidenciasGeorreferenciadas;
    private long incidenciasSinCoordenadas;

    private long totalAlertas;
    private long alertasGeorreferenciadas;

    private long totalIntervenciones;
    private long intervencionesGeorreferenciadas;

    private long totalPatrullajesActivos;
    private long totalRutasActivas;
    private long totalPuntosControl;

    @Builder.Default
    private Map<String, Long> incidenciasPorSector = new LinkedHashMap<>();

    @Builder.Default
    private Map<String, Long> incidenciasPorTipo = new LinkedHashMap<>();

    @Builder.Default
    private Map<String, Long> incidenciasPorPrioridad = new LinkedHashMap<>();

    @Builder.Default
    private Map<String, Long> patrullajesPorEstado = new LinkedHashMap<>();
}
