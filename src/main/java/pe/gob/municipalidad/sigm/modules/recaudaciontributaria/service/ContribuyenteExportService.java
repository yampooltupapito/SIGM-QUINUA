package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service;

import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request.ContribuyenteFilterDto;

/**
 * Servicio para la exportación autorizada del padrón de contribuyentes en formatos Excel y PDF.
 */
public interface ContribuyenteExportService {

    byte[] exportarExcel(ContribuyenteFilterDto filtro, boolean puedeVerDocumentosCompletos);

    byte[] exportarPdf(ContribuyenteFilterDto filtro, boolean puedeVerDocumentosCompletos);
}
