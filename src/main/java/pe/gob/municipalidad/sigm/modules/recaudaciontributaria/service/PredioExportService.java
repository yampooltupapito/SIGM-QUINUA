package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service;

import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request.PredioFilterDto;

/**
 * Servicio para la exportación autorizada del padrón de predios municipales en formatos Excel y PDF.
 */
public interface PredioExportService {

    byte[] exportarExcel(PredioFilterDto filtro);

    byte[] exportarPdf(PredioFilterDto filtro);
}
