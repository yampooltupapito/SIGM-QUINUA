package pe.gob.municipalidad.sigm.modules.administracion.dashboard.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pe.gob.municipalidad.sigm.modules.administracion.dashboard.dto.DashboardActivityDto;
import pe.gob.municipalidad.sigm.modules.administracion.dashboard.dto.DashboardChartDto;
import pe.gob.municipalidad.sigm.modules.administracion.dashboard.dto.DashboardSummaryDto;
import pe.gob.municipalidad.sigm.modules.administracion.dashboard.dto.ModuloMenuDto;
import pe.gob.municipalidad.sigm.modules.administracion.dashboard.dto.UserProfileDto;

import java.util.List;

public interface DashboardService {

    DashboardSummaryDto obtenerResumenEstadistico();

    DashboardChartDto obtenerDatosGraficos();

    Page<DashboardActivityDto> obtenerActividadReciente(String periodo, String resultado, Pageable pageable);

    UserProfileDto obtenerPerfilUsuario(String username);

    List<ModuloMenuDto> obtenerModulosSidebar(String username, String rutaActiva);
}
