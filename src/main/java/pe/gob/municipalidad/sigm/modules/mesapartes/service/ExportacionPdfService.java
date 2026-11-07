package pe.gob.municipalidad.sigm.modules.mesapartes.service;

import pe.gob.municipalidad.sigm.modules.mesapartes.dto.ReporteDtos.*;

/**
 * Servicio para generación de reportes institucionales en PDF (OpenPDF).
 */
public interface ExportacionPdfService {

    /**
     * Genera el documento PDF oficial con membrete municipal, tarjetas de resumen y tabla de expedientes.
     */
    byte[] exportarReportePdf(FiltroReporteRequest filtro, String username, String ip, String userAgent);
}
