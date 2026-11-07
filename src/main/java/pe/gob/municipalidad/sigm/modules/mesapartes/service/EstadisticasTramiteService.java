package pe.gob.municipalidad.sigm.modules.mesapartes.service;

import pe.gob.municipalidad.sigm.modules.mesapartes.dto.ReporteDtos.*;

import java.util.List;

/**
 * Servicio de métricas agregadas por tipo de trámite oficial.
 */
public interface EstadisticasTramiteService {

    /**
     * Obtiene el desglose de expedientes por tipo de procedimiento administrativo.
     */
    List<EstadisticaTramiteDto> obtenerEstadisticasPorTramite(FiltroReporteRequest filtro, String username);
}
