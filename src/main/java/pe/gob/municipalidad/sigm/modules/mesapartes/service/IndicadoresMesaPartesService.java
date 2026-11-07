package pe.gob.municipalidad.sigm.modules.mesapartes.service;

import pe.gob.municipalidad.sigm.modules.mesapartes.dto.ReporteDtos.*;

/**
 * Servicio para el cálculo de indicadores de eficiencia y control de plazos de atención.
 */
public interface IndicadoresMesaPartesService {

    /**
     * Calcula métricas de eficiencia (tiempos promedio, mediana, tasas de resolución y observación).
     */
    IndicadoresEficienciaDto calcularIndicadoresEficiencia(FiltroReporteRequest filtro, String username);

    /**
     * Evalúa el estado de plazos legales (TUO Ley N° 27444).
     */
    IndicadoresPlazosDto calcularIndicadoresPlazos(FiltroReporteRequest filtro, String username);
}
