package pe.gob.municipalidad.sigm.common.notification.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pe.gob.municipalidad.sigm.common.notification.dto.NotificationDtos.NotificacionItemDto;
import pe.gob.municipalidad.sigm.common.notification.dto.NotificationDtos.NotificacionPreferenciasDto;
import pe.gob.municipalidad.sigm.common.notification.service.CentralNotificationService;

/**
 * Controlador web para el Centro de Notificaciones y Gestión de Preferencias de SIGM-QUINUA.
 */
@Controller
@RequestMapping({"/notificaciones", "/dashboard/notificaciones"})
@RequiredArgsConstructor
public class NotificacionWebController {

    private final CentralNotificationService centralNotificationService;

    @GetMapping
    public String verCentroNotificaciones(
            @RequestParam(required = false) Boolean leida,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "todas") String tab,
            Authentication authentication,
            Model model
    ) {
        String username = authentication.getName();

        // Si la pestaña seleccionada es 'no-leidas', forzar leida = false
        if ("no-leidas".equalsIgnoreCase(tab)) {
            leida = false;
        } else if ("tramites".equalsIgnoreCase(tab)) {
            tipo = "TRAMITE";
        } else if ("seguridad".equalsIgnoreCase(tab)) {
            tipo = "SEGURIDAD";
        }

        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size), Sort.by(Sort.Direction.DESC, "fechaCreacion"));
        Page<NotificacionItemDto> notificacionesPage = centralNotificationService.buscarNotificacionesFiltradas(username, leida, tipo, search, pageable);
        long noLeidas = centralNotificationService.contarNoLeidas(username);
        NotificacionPreferenciasDto preferencias = centralNotificationService.obtenerOInicializarPreferencias(username);

        model.addAttribute("moduloActivo", "notificaciones");
        model.addAttribute("notificacionesPage", notificacionesPage);
        model.addAttribute("notificaciones", notificacionesPage.getContent());
        model.addAttribute("noLeidas", noLeidas);
        model.addAttribute("filtroLeida", leida);
        model.addAttribute("filtroTipo", tipo != null ? tipo : "");
        model.addAttribute("filtroSearch", search != null ? search : "");
        model.addAttribute("tabActiva", tab);
        model.addAttribute("preferencias", preferencias);

        return "admin/notificaciones/index";
    }
}
