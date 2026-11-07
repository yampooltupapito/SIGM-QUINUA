package pe.gob.municipalidad.sigm.modules.administracion.dashboard.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pe.gob.municipalidad.sigm.modules.administracion.dashboard.dto.DashboardActivityDto;
import pe.gob.municipalidad.sigm.modules.administracion.dashboard.dto.DashboardChartDto;
import pe.gob.municipalidad.sigm.modules.administracion.dashboard.dto.DashboardSummaryDto;
import pe.gob.municipalidad.sigm.modules.administracion.dashboard.dto.ModuloMenuDto;
import pe.gob.municipalidad.sigm.modules.administracion.dashboard.dto.UserProfileDto;
import pe.gob.municipalidad.sigm.modules.administracion.dashboard.service.DashboardService;
import pe.gob.municipalidad.sigm.modules.notificaciones.dto.NotificacionDto;
import pe.gob.municipalidad.sigm.modules.notificaciones.service.NotificacionService;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class DashboardWebController {

    private final DashboardService dashboardService;
    private final NotificacionService notificacionService;

    @GetMapping({"/admin/dashboard", "/dashboard"})
    @PreAuthorize("isAuthenticated()")
    public String vistaDashboard(
            @RequestParam(required = false, defaultValue = "todos") String periodo,
            @RequestParam(required = false, defaultValue = "todos") String resultado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size,
            Authentication authentication,
            Model model
    ) {
        String username = authentication != null ? authentication.getName() : "admin.quinua";

        boolean esAdmin = authentication != null && authentication.getAuthorities().stream().anyMatch(a ->
                "ROLE_ADMIN".equals(a.getAuthority())
                        || "ROLE_SUPERADMIN".equals(a.getAuthority())
                        || "ADMINISTRACION_MANAGE".equals(a.getAuthority())
                        || "PORTAL_MANAGE".equals(a.getAuthority()));
        if (!esAdmin) {
            return "redirect:/bienvenida";
        }

        DashboardSummaryDto summary = dashboardService.obtenerResumenEstadistico();
        DashboardChartDto charts = dashboardService.obtenerDatosGraficos();
        Page<DashboardActivityDto> actividad = dashboardService.obtenerActividadReciente(
                periodo, resultado, PageRequest.of(page, size)
        );
        UserProfileDto userProfile = dashboardService.obtenerPerfilUsuario(username);
        List<ModuloMenuDto> modulos = dashboardService.obtenerModulosSidebar(username, "/admin/dashboard");
        List<NotificacionDto> notificaciones = notificacionService.obtenerNotificacionesRecientes(username);
        long noLeidas = notificacionService.contarNoLeidas(username);

        model.addAttribute("summary", summary);
        model.addAttribute("charts", charts);
        model.addAttribute("actividadPage", actividad);
        model.addAttribute("userProfile", userProfile);
        model.addAttribute("modulosSidebar", modulos);
        model.addAttribute("notificaciones", notificaciones);
        model.addAttribute("noLeidas", noLeidas);
        model.addAttribute("periodoFiltro", periodo);
        model.addAttribute("resultadoFiltro", resultado);
        model.addAttribute("moduloActivo", "dashboard");

        return "admin/dashboard";
    }
}
