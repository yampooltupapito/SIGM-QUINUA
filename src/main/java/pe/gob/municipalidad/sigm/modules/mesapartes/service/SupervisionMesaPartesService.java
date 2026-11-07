package pe.gob.municipalidad.sigm.modules.mesapartes.service;

import pe.gob.municipalidad.sigm.modules.mesapartes.dto.ReporteDtos.*;

/**
 * Servicio de supervisión institucional de Mesa de Partes (derivaciones, observaciones y carga laboral).
 */
public interface SupervisionMesaPartesService {

    /**
     * Análisis cuantitativo y de tiempos de derivaciones entre dependencias.
     */
    AnalisisDerivacionesDto analizarDerivaciones(FiltroReporteRequest filtro, String username);

    /**
     * Análisis de observaciones formuladas y subsanaciones recibidas.
     */
    AnalisisObservacionesDto analizarObservaciones(FiltroReporteRequest filtro, String username);

    /**
     * Métricas de carga operativa y expedientes pendientes por oficina.
     */
    ProductividadOperativaDto obtenerProductividadOperativa(FiltroReporteRequest filtro, String username);
}
