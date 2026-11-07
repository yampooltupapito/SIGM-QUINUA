package pe.gob.municipalidad.sigm.modules.mercados.service;

import pe.gob.municipalidad.sigm.modules.mercados.dto.DashboardMercadosResponse;
import pe.gob.municipalidad.sigm.modules.mercados.dto.ReporteMercadoFilter;

public interface IndicadoresMercadosService {
    DashboardMercadosResponse obtenerDashboard(ReporteMercadoFilter filtro);
}
