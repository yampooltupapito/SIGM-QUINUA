package pe.gob.municipalidad.sigm.modules.portal.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pe.gob.municipalidad.sigm.modules.portal.dto.PortalContactoDtos;
import pe.gob.municipalidad.sigm.modules.portal.dto.PortalFaqDtos;
import pe.gob.municipalidad.sigm.modules.portal.dto.PortalNoticiaDtos;
import pe.gob.municipalidad.sigm.modules.portal.dto.PortalServicioDtos;
import pe.gob.municipalidad.sigm.modules.portal.service.PortalContactoService;
import pe.gob.municipalidad.sigm.modules.portal.service.PortalContenidoService;
import pe.gob.municipalidad.sigm.modules.portal.service.PortalServicioService;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/admin/portal")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'PORTAL_MANAGE', 'ADMINISTRACION_MANAGE')")
public class PortalAdminWebController {

    private final PortalContenidoService contenidoService;
    private final PortalServicioService servicioService;
    private final PortalContactoService contactoService;

    @GetMapping("/noticias")
    public String adminNoticias(
            @RequestParam(value = "estado", required = false) String estado,
            @RequestParam(value = "tipo", required = false) String tipo,
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {

        Page<PortalNoticiaDtos.NoticiaResponse> noticias =
                contenidoService.listarNoticiasAdmin(estado, tipo, q, page, 10);

        model.addAttribute("noticias", noticias);
        model.addAttribute("moduloActivo", "portal-noticias");
        model.addAttribute("filtroEstado", estado != null ? estado : "TODOS");
        model.addAttribute("filtroTipo", tipo != null ? tipo : "TODOS");
        model.addAttribute("busquedaQ", q != null ? q : "");
        return "admin/portal/noticias";
    }

    @GetMapping("/faq")
    public String adminFaq(Model model) {
        List<PortalFaqDtos.FaqResponse> faqs = contenidoService.listarTodasFaqAdmin();
        List<String> categorias = contenidoService.obtenerCategoriasFaq();

        model.addAttribute("faqs", faqs);
        model.addAttribute("categorias", categorias);
        model.addAttribute("moduloActivo", "portal-faq");
        return "admin/portal/faq";
    }

    @GetMapping("/contactos")
    public String adminContactos(
            @RequestParam(value = "estado", required = false) String estado,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {

        Page<PortalContactoDtos.ContactoResponse> contactos =
                contactoService.listarContactosAdmin(estado, page, 10);
        long pendientes = contactoService.contarPendientes();

        model.addAttribute("contactos", contactos);
        model.addAttribute("pendientes", pendientes);
        model.addAttribute("filtroEstado", estado != null ? estado : "TODOS");
        model.addAttribute("moduloActivo", "portal-contactos");
        return "admin/portal/contactos";
    }

    @GetMapping("/servicios")
    public String adminServicios(Model model) {
        List<PortalServicioDtos.ServicioResponse> servicios = servicioService.listarServiciosAdmin();
        model.addAttribute("servicios", servicios);
        model.addAttribute("moduloActivo", "portal-servicios");
        return "admin/portal/servicios";
    }
}
