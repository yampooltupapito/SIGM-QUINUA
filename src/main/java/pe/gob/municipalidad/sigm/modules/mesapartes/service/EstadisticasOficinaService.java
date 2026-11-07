package pe.gob.municipalidad.sigm.modules.mesapartes.service;

import pe.gob.municipalidad.sigm.modules.mesapartes.dto.ReporteDtos.*;

import java.util.List;

/**
 * Servicio de métricas agregadas por unidad orgánica municipal.
 */
public interface EstadisticasOficinaService {

    /**
     * Obtiene el resumen de expedientes por oficina (recibidos, pendientes, en atención, resueltos).
     */
    List<EstadisticaOficinaDto> obtenerEstadisticasPorOficina(FiltroReporteRequest filtro, String username);
}
