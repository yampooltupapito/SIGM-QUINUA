package pe.gob.municipalidad.sigm.modules.serenazgo.dto.estadisticas;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Respuesta integral del panel estadístico de Seguridad Ciudadana.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardEstadisticoResponse {

    private OffsetDateTime fechaConsulta;
    private String zonaHoraria;
    private String periodoEtiqueta;
    private String periodoComparacionEtiqueta;

    // Tarjetas KPI Principales
    @Builder.Default
    private List<IndicadorKpiDto> kpis = new ArrayList<>();

    // Series Temporales para Gráficos
    private SerieTemporalDto seriesIncidencias;
    private SerieTemporalDto seriesPatrullajes;

    // Distribuciones Categóricas
    @Builder.Default
    private List<DistribucionItemDto> incidenciasPorTipo = new ArrayList<>();

    @Builder.Default
    private List<DistribucionItemDto> incidenciasPorEstado = new ArrayList<>();

    @Builder.Default
    private List<DistribucionItemDto> incidenciasPorPrioridad = new ArrayList<>();

    @Builder.Default
    private List<DistribucionItemDto> alertasPorEstado = new ArrayList<>();

    @Builder.Default
    private List<DistribucionItemDto> intervencionesPorTipo = new ArrayList<>();

    @Builder.Default
    private List<DistribucionItemDto> intervencionesPorResultado = new ArrayList<>();

    @Builder.Default
    private List<DistribucionItemDto> patrullajesPorModalidad = new ArrayList<>();

    // Tiempos y Rendimiento Operativo
    private TiemposAtencionEstadisticasDto tiemposAtencion;

    // Cumplimiento de Patrullaje
    private EstadisticasPatrullajeDto estadisticasPatrullaje;

    // Análisis Territorial por Sector
    @Builder.Default
    private List<EstadisticasTerritorialesDto> estadisticasPorSector = new ArrayList<>();

    // Catálogo de Indicadores
    @Builder.Default
    private List<CatalogoIndicadorDto> catalogoIndicadores = new ArrayList<>();
}
