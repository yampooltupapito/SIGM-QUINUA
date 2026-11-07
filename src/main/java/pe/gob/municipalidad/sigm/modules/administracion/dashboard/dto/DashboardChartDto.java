package pe.gob.municipalidad.sigm.modules.administracion.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO que empaqueta los datos agregados para los gráficos interactivos de Chart.js.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardChartDto {

    // Gráfico 1: Estado de usuarios (Doughnut)
    @Builder.Default
    private ChartDataset usuariosPorEstado = new ChartDataset();

    // Gráfico 2: Personal asignado por Oficina (Bar)
    @Builder.Default
    private ChartDataset usuariosPorOficina = new ChartDataset();

    // Gráfico 3: Distribución por Rol institucional (Bar / Polar)
    @Builder.Default
    private ChartDataset usuariosPorRol = new ChartDataset();

    // Gráfico 4: Actividad administrativa por tipo de acción (Bar)
    @Builder.Default
    private ChartDataset actividadPorAccion = new ChartDataset();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChartDataset {
        @Builder.Default
        private List<String> labels = new ArrayList<>();
        @Builder.Default
        private List<Long> data = new ArrayList<>();
        @Builder.Default
        private List<String> backgroundColors = new ArrayList<>();
        @Builder.Default
        private List<String> borderColors = new ArrayList<>();
    }
}
