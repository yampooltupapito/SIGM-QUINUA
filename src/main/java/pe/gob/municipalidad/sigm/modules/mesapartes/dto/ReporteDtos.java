package pe.gob.municipalidad.sigm.modules.mesapartes.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.CanalIngreso;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoExpediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.PrioridadExpediente;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Transfer Objects para el sistema de Reportes, Estadísticas y Supervisión de Mesa de Partes.
 */
public final class ReporteDtos {

    private ReporteDtos() {
    }

    /**
     * Criterios y filtros avanzados para consultas estadísticas y generación de reportes.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FiltroReporteRequest {
        private LocalDate fechaInicio;
        private LocalDate fechaFin;
        private Long oficinaId;
        private Long tipoTramiteId;
        private EstadoExpediente estado;
        private CanalIngreso canalIngreso;
        private PrioridadExpediente prioridad;
        private Long responsableId;
        private String termino;
        private String periodo; // MES_ACTUAL, MES_ANTERIOR, TRIMESTRE_ACTUAL, ANIO_ACTUAL, PERSONALIZADO
        private Integer anio;
        
        @Builder.Default
        private Integer page = 0;
        @Builder.Default
        private Integer size = 20;
    }

    /**
     * Resumen cuantitativo de los estados principales de expedientes en el período.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResumenEstadisticoDto {
        private long totalPresentados;
        private long totalRecibidos;
        private long totalEnRevision;
        private long totalObservados;
        private long totalEnAtencion;
        private long totalResueltos;
        private long totalCerrados;
        private long totalArchivados;
        private long totalPendientes; // presentados + recibidos + en revision + observados + en atencion
    }

    /**
     * Indicadores de eficiencia calculados con fórmulas estadísticas exactas.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IndicadoresEficienciaDto {
        private Double tiempoPromedioAtencionDias;
        private Double tiempoMedianoAtencionDias;
        private Double tiempoPromedioRecepcionHoras;
        private Double tiempoPromedioDerivacionHoras;
        private Double porcentajeResueltos;
        private Double porcentajeObservados;
        private Double porcentajePendientes;
        private long totalAtendidosPeriodo;
        private boolean datosSuficientes;
    }

    /**
     * Indicadores de control de plazos legales (TUO Ley N° 27444).
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IndicadoresPlazosDto {
        private long enPlazo;
        private long proximosVencer;
        private long vencidos;
        private long suspendidos;
        private long sinPlazoConfigurado;
        private long totalConPlazo;
    }

    /**
     * Datos para visualización en gráfico de distribución por estado (Doughnut / Pie).
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GraficoEstadoDto {
        @Builder.Default
        private List<String> labels = new ArrayList<>();
        @Builder.Default
        private List<Long> datos = new ArrayList<>();
        @Builder.Default
        private List<String> colores = new ArrayList<>();
        private long total;
    }

    /**
     * Datos para gráfico de evolución mensual cronológica.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EvolucionMensualDto {
        private int anio;
        @Builder.Default
        private List<String> meses = new ArrayList<>();
        @Builder.Default
        private List<Long> presentados = new ArrayList<>();
        @Builder.Default
        private List<Long> resueltos = new ArrayList<>();
        @Builder.Default
        private List<Long> cerrados = new ArrayList<>();
    }

    /**
     * Desglose estadístico por oficina municipal.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EstadisticaOficinaDto {
        private Long oficinaId;
        private String nombreOficina;
        private long recibidos;
        private long pendientes;
        private long enAtencion;
        private long resueltos;
        private long total;
    }

    /**
     * Desglose estadístico por tipo de trámite oficial.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EstadisticaTramiteDto {
        private Long tipoTramiteId;
        private String nombreTramite;
        private String codigoTramite;
        private long cantidad;
        private double porcentaje;
    }

    /**
     * Métricas de análisis de derivaciones inter-oficinas.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnalisisDerivacionesDto {
        private long enviadas;
        private long recibidas;
        private long pendientes;
        private long rechazadas;
        private long devueltas;
        private long expedientesUnicos;
        private Double tiempoPromedioRecepcionHoras;
    }

    /**
     * Métricas de análisis de observaciones y subsanaciones de admisibilidad.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnalisisObservacionesDto {
        private long expedientesObservados;
        private long observacionesTotales;
        private long solicitudesSubsanacion;
        private long subsanacionesPresentadas;
        private long subsanacionesPendientes;
    }

    /**
     * Métricas de supervisión de carga y productividad operativa agregada.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductividadOperativaDto {
        private long expedientesAsignados;
        private long expedientesEnAtencion;
        private long actuacionesRegistradas;
        private long expedientesFinalizados;
        @Builder.Default
        private List<OficinaPendienteDto> oficinasPendientes = new ArrayList<>();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OficinaPendienteDto {
        private Long oficinaId;
        private String nombreOficina;
        private long pendientes;
    }

    /**
     * Comparación cronológica entre dos períodos con variaciones absolutas y porcentuales.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComparacionPeriodoDto {
        private String periodoActualLabel;
        private String periodoAnteriorLabel;

        private long presentadosActual;
        private long presentadosAnterior;
        private long variacionPresentados;
        private String variacionPctPresentados; // "+12.5%", "-5.0%", "N/A"

        private long resueltosActual;
        private long resueltosAnterior;
        private long variacionResueltos;
        private String variacionPctResueltos;

        private long observadosActual;
        private long observadosAnterior;
        private long variacionObservados;
        private String variacionPctObservados;
    }

    /**
     * Elemento individual para la tabla de reportes y exportaciones detalladas.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExpedienteReporteItemDto {
        private Long id;
        private String numeroExpediente;
        private String tipoTramite;
        private String solicitante;
        private String solicitanteDoc;
        private String asunto;
        private OffsetDateTime fechaPresentacion;
        private String oficina;
        private String estado;
        private String prioridad;
        private Long diasTranscurridos;
        private OffsetDateTime fechaCierre;
        private String canalIngreso;
    }

    /**
     * Registro de auditoría del historial de exportaciones.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExportacionHistorialDto {
        private Long id;
        private String uuid;
        private String usuarioNombre;
        private String tipoReporte;
        private String formato;
        private int cantidadRegistros;
        private String estado;
        private Long tiempoGeneracionMs;
        private OffsetDateTime fechaExportacion;
    }
}
