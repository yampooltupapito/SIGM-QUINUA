package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service;

import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request.ArbitrioFilterDto;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response.ArbitrioDetailResponse;

import java.util.UUID;

public interface ArbitrioExportService {

    byte[] exportarExcel(ArbitrioFilterDto filtro);

    byte[] exportarPdf(ArbitrioFilterDto filtro);

    byte[] exportarHojaLiquidacionPdf(UUID calculoId);

    /**
     * Genera la hoja de liquidación a partir de un resultado ya calculado. Permite imprimir una simulación,
     * que no se guarda en la base de datos y por eso no puede buscarse por id.
     */
    byte[] exportarHojaLiquidacionPdf(ArbitrioDetailResponse calculo);
}
