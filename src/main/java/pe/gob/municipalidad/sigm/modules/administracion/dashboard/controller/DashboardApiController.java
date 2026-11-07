package pe.gob.municipalidad.sigm.modules.administracion.dashboard.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.gob.municipalidad.sigm.common.response.ApiResponse;
import pe.gob.municipalidad.sigm.modules.administracion.dashboard.dto.DashboardActivityDto;
import pe.gob.municipalidad.sigm.modules.administracion.dashboard.dto.DashboardChartDto;
import pe.gob.municipalidad.sigm.modules.administracion.dashboard.dto.DashboardSummaryDto;
import pe.gob.municipalidad.sigm.modules.administracion.dashboard.service.DashboardService;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class DashboardApiController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<DashboardSummaryDto>> obtenerEstadisticas() {
        DashboardSummaryDto stats = dashboardService.obtenerResumenEstadistico();
        return ApiResponse.ok("Estadísticas del dashboard obtenidas", stats);
    }

    @GetMapping("/charts")
    public ResponseEntity<ApiResponse<DashboardChartDto>> obtenerGraficos() {
        DashboardChartDto charts = dashboardService.obtenerDatosGraficos();
        return ApiResponse.ok("Datos de gráficos interactivos obtenidos", charts);
    }

    @GetMapping("/actividad")
    public ResponseEntity<ApiResponse<Page<DashboardActivityDto>>> obtenerActividad(
            @RequestParam(required = false, defaultValue = "todos") String periodo,
            @RequestParam(required = false, defaultValue = "todos") String resultado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<DashboardActivityDto> actividad = dashboardService.obtenerActividadReciente(
                periodo, resultado, PageRequest.of(page, size)
        );
        return ApiResponse.ok("Actividad reciente obtenida", actividad);
    }
}
