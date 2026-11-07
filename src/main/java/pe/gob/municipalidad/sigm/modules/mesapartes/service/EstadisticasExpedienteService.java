package pe.gob.municipalidad.sigm.modules.mesapartes.service;

import pe.gob.municipalidad.sigm.modules.mesapartes.dto.ReporteDtos.*;

/**
 * Servicio para estadísticas cuantitativas, evolución mensual y comparaciones de períodos.
 */
public interface EstadisticasExpedienteService {

    /**
     * Gráfico de distribución de expedientes por estado.
     */
    GraficoEstadoDto obtenerGraficoEstado(FiltroReporteRequest filtro, String username);

    /**
     * Evolución cronológica de expedientes presentados, resueltos y cerrados por mes.
     */
    EvolucionMensualDto obtenerEvolucionMensual(Integer anio, Long oficinaId, String username);

    /**
     * Comparativa de desempeño entre período actual y período anterior equivalente.
     */
    ComparacionPeriodoDto compararPeriodos(String tipoPeriodo, Long oficinaId, String username);
}
