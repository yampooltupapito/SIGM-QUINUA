package pe.gob.municipalidad.sigm.modules.mercados.service;

import pe.gob.municipalidad.sigm.modules.mercados.dto.MercadoFilter;

public interface MercadosExportService {

    byte[] exportarExcel(MercadoFilter filter);

    byte[] exportarPdf(MercadoFilter filter);
}
