package pe.gob.municipalidad.sigm.modules.mercados.controller.web;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import pe.gob.municipalidad.sigm.modules.mercados.dto.VerificacionDocumentoResponse;
import pe.gob.municipalidad.sigm.modules.mercados.dto.VigenciaResponse;
import pe.gob.municipalidad.sigm.modules.mercados.service.VerificacionDocumentoMercadoService;
import pe.gob.municipalidad.sigm.modules.mercados.service.VigenciaOcupacionService;

import java.util.List;

@Controller
public class DocumentoMercadoWebController {

    private final VigenciaOcupacionService vigenciaService;
    private final VerificacionDocumentoMercadoService verificacionService;

    public DocumentoMercadoWebController(
            VigenciaOcupacionService vigenciaService,
            VerificacionDocumentoMercadoService verificacionService) {
        this.vigenciaService = vigenciaService;
        this.verificacionService = verificacionService;
    }

    @GetMapping("/dashboard/mercados/documentos/vencimientos")
    @PreAuthorize("hasAuthority('MERCADOS_CONTRATO_VER') or hasAuthority('MERCADOS_AUTORIZACION_VER') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public String vencimientos(
            @RequestParam(defaultValue = "30") int dias,
            Model model) {

        List<VigenciaResponse> proximos = vigenciaService.consultarProximosVencimientos(dias);
        List<VigenciaResponse> vencidos = vigenciaService.consultarVencidos();

        model.addAttribute("proximos", proximos);
        model.addAttribute("vencidos", vencidos);
        model.addAttribute("diasFiltro", dias);
        model.addAttribute("stats", vigenciaService.obtenerEstadisticas());

        model.addAttribute("moduloActivo", "mercados");
        model.addAttribute("submenuActivo", "vencimientos");

        return "admin/mercados/documentos/vencimientos";
    }

    /**
     * Ruta pública para verificación de autenticidad documental mediante código QR o enlace web.
     * No requiere autenticación y aplica anonimización de datos privados del comerciante titular.
     */
    @GetMapping("/verificar/mercados/documento/{codigoSeguro}")
    public String verificarPublico(@PathVariable String codigoSeguro, Model model) {
        VerificacionDocumentoResponse verificacion = verificacionService.verificarPorCodigoSeguro(codigoSeguro);
        model.addAttribute("verif", verificacion);
        return "admin/mercados/documentos/verificacion";
    }
}
