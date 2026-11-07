package pe.gob.municipalidad.sigm.modules.mercados.service;

import pe.gob.municipalidad.sigm.modules.mercados.dto.ReporteMercadoFilter;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoReporteMercado;

public interface ExportacionExcelService {
    byte[] generarReporteExcel(TipoReporteMercado tipoReporte, ReporteMercadoFilter filtro, boolean puedeVerDocumentos);
}
