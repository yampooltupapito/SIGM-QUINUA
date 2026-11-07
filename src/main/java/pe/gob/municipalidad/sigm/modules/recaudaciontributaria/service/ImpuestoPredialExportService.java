package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service;

import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request.CalculoPredialFilterDto;

import java.util.UUID;

public interface ImpuestoPredialExportService {

    byte[] exportarExcel(CalculoPredialFilterDto filtro);

    byte[] exportarPdf(CalculoPredialFilterDto filtro);

    byte[] exportarHojaLiquidacionPdf(UUID calculoId);
}
