package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service;

import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request.ArbitrioFilterDto;

import java.util.UUID;

public interface ArbitrioExportService {

    byte[] exportarExcel(ArbitrioFilterDto filtro);

    byte[] exportarPdf(ArbitrioFilterDto filtro);

    byte[] exportarHojaLiquidacionPdf(UUID calculoId);
}
