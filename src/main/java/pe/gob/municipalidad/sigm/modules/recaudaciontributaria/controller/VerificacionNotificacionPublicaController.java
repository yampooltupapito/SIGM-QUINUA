package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.notificacion.VerificacionPublicaNotificacionResponse;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.NotificacionTributariaService;

/**
 * Portal público de consulta y validación de autenticidad documental institucional
 * para cédulas y comunicaciones emitidas por la Municipalidad Distrital de Quinua.
 * Aplica anonimización y minimización estricta de datos personales (Art. 59).
 */
@Slf4j
@Controller
@RequestMapping("/verificar/notificacion")
@RequiredArgsConstructor
public class VerificacionNotificacionPublicaController {

    private final NotificacionTributariaService notificacionService;

    @GetMapping
    public String portalConsulta(
            @RequestParam(required = false) String codigo,
            Model model) {
        if (codigo != null && !codigo.trim().isEmpty()) {
            return "redirect:/verificar/notificacion/" + codigo.trim();
        }
        model.addAttribute("codigoConsultado", "");
        return "portal/verificacion-notificacion";
    }

    @GetMapping("/{codigo}")
    public String verificarPublicamente(
            @PathVariable String codigo,
            HttpServletRequest request,
            Model model) {
        VerificacionPublicaNotificacionResponse resultado = notificacionService.verificarPublicamente(codigo);
        model.addAttribute("resultado", resultado);
        model.addAttribute("codigoConsultado", codigo);
        return "portal/verificacion-notificacion";
    }
}

