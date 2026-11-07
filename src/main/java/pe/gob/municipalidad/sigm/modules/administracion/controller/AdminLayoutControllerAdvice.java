package pe.gob.municipalidad.sigm.modules.administracion.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import pe.gob.municipalidad.sigm.modules.administracion.dashboard.service.DashboardService;
import pe.gob.municipalidad.sigm.modules.notificaciones.service.NotificacionService;

@ControllerAdvice(basePackages = {
        "pe.gob.municipalidad.sigm"
})
@RequiredArgsConstructor
public class AdminLayoutControllerAdvice {

    private final DashboardService dashboardService;
    private final NotificacionService notificacionService;

    @ModelAttribute
    public void populateCommonAdminAttributes(Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            String username = authentication.getName();
            if (!model.containsAttribute("userProfile")) {
                model.addAttribute("userProfile", dashboardService.obtenerPerfilUsuario(username));
            }
            if (!model.containsAttribute("notificaciones")) {
                model.addAttribute("notificaciones", notificacionService.obtenerNotificacionesRecientes(username));
            }
            if (!model.containsAttribute("noLeidas")) {
                model.addAttribute("noLeidas", notificacionService.contarNoLeidas(username));
            }
        }
    }
}
