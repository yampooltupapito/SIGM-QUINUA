package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.documentopago.VerificacionPublicaDocumentoResponse;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.DocumentoPagoTributarioService;

@Slf4j
@Controller
@RequestMapping("/verificar/documento-tributario")
@RequiredArgsConstructor
public class VerificacionPublicaWebController {

    private final DocumentoPagoTributarioService documentoService;

    @GetMapping
    public String portalConsultaVerificacion(
            @RequestParam(required = false) String codigo,
            HttpServletRequest request,
            Model model) {

        if (codigo != null && !codigo.trim().isEmpty()) {
            return "redirect:/verificar/documento-tributario/" + codigo.trim();
        }

        model.addAttribute("codigoConsultado", "");
        return "portal/verificacion-tributaria";
    }

    @GetMapping("/{codigo}")
    public String verificarDocumentoPublico(
            @PathVariable String codigo,
            HttpServletRequest request,
            Model model) {

        String ip = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");

        VerificacionPublicaDocumentoResponse resultado = documentoService.verificarPublicamente(codigo, ip, userAgent);

        model.addAttribute("resultado", resultado);
        model.addAttribute("codigoConsultado", codigo);
        return "portal/verificacion-tributaria";
    }
}
