package pe.gob.municipalidad.sigm.modules.mesapartes.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.SeguimientoDtos.VerificarDocumentoResponse;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.VerificacionDocumentalService;
import pe.gob.municipalidad.sigm.modules.portal.service.PortalContenidoService;

import java.util.Map;

@Slf4j
@Controller
public class VerificacionDocumentoWebController {

    private final VerificacionDocumentalService verificacionService;
    private final PortalContenidoService portalContenidoService;

    public VerificacionDocumentoWebController(VerificacionDocumentalService verificacionService,
                                              PortalContenidoService portalContenidoService) {
        this.verificacionService = verificacionService;
        this.portalContenidoService = portalContenidoService;
    }

    @ModelAttribute("config")
    public Map<String, String> populateConfig() {
        return portalContenidoService.obtenerMapaConfiguracion();
    }

    @GetMapping("/verificar-documento")
    public String paginaVerificarDocumento(
            @RequestParam(value = "codigo", required = false) String codigo,
            @RequestParam(value = "code", required = false) String code,
            HttpServletRequest request,
            Model model
    ) {
        String codigoIngresado = (codigo != null && !codigo.isBlank()) ? codigo.trim() : (code != null ? code.trim() : "");
        model.addAttribute("paginaActiva", "verificar-documento");
        model.addAttribute("codigoIngresado", codigoIngresado);

        if (!codigoIngresado.isBlank()) {
            String ip = obtenerIpCliente(request);
            String ua = request.getHeader("User-Agent");
            VerificarDocumentoResponse resultado = verificacionService.verificarDocumento(codigoIngresado, ip, ua);
            model.addAttribute("resultado", resultado);
            model.addAttribute("consultado", true);
        } else {
            model.addAttribute("consultado", false);
        }

        return "mesa-partes/verificar-documento";
    }

    @GetMapping("/verificar/{codigo}")
    public String verificarPorRuta(@org.springframework.web.bind.annotation.PathVariable("codigo") String codigo) {
        return "redirect:/verificar-documento?codigo=" + (codigo != null ? codigo.trim() : "");
    }

    private String obtenerIpCliente(HttpServletRequest request) {
        String xf = request.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isBlank()) {
            return xf.split(",")[0].trim();
        }
        return request.getRemoteAddr() != null ? request.getRemoteAddr() : "127.0.0.1";
    }
}
