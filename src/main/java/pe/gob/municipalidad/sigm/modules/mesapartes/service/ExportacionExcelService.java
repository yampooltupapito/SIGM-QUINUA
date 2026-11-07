package pe.gob.municipalidad.sigm.modules.mesapartes.service;

import pe.gob.municipalidad.sigm.modules.mesapartes.dto.ReporteDtos.*;

/**
 * Servicio para generación de reportes en formato Microsoft Excel (XLSX con Apache POI).
 */
public interface ExportacionExcelService {

    /**
     * Genera el libro Excel de reporte de expedientes con protección contra inyección de fórmulas.
     */
    byte[] exportarReporteExcel(FiltroReporteRequest filtro, String username, String ip, String userAgent);
}
