package pe.gob.municipalidad.sigm.modules.mercados.service;

import pe.gob.municipalidad.sigm.modules.mercados.dto.AsignacionFilter;

public interface AsignacionExportService {

    byte[] exportarExcel(AsignacionFilter filter);

    byte[] exportarPdf(AsignacionFilter filter);
}
