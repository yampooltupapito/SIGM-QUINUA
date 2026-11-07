package pe.gob.municipalidad.sigm.modules.mercados.service;

import pe.gob.municipalidad.sigm.modules.mercados.dto.ComercianteFilter;

public interface ComercianteExportService {

    byte[] exportarExcel(ComercianteFilter filter);

    byte[] exportarPdf(ComercianteFilter filter);
}
