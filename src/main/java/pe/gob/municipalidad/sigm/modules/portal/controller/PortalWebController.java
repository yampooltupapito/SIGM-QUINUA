package pe.gob.municipalidad.sigm.modules.portal.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.exception.ConflictException;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.common.repository.OficinaRepository;
import pe.gob.municipalidad.sigm.modules.portal.dto.CiudadanoRegistroDto;
import pe.gob.municipalidad.sigm.modules.portal.dto.PortalContactoDtos;
import pe.gob.municipalidad.sigm.modules.portal.dto.PortalFaqDtos;
import pe.gob.municipalidad.sigm.modules.portal.dto.PortalNoticiaDtos;
import pe.gob.municipalidad.sigm.modules.portal.dto.PortalServicioDtos;
import pe.gob.municipalidad.sigm.modules.portal.service.CiudadanoRegistroService;
import pe.gob.municipalidad.sigm.modules.portal.service.PortalContactoService;
import pe.gob.municipalidad.sigm.modules.portal.service.PortalContenidoService;
import pe.gob.municipalidad.sigm.modules.portal.service.PortalServicioService;

import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class PortalWebController {

    private final PortalContenidoService contenidoService;
    private final PortalServicioService servicioService;
    private final PortalContactoService contactoService;
    private final CiudadanoRegistroService registroService;
    private final OficinaRepository oficinaRepository;

    @ModelAttribute("config")
    public Map<String, String> populateConfig() {
        return contenidoService.obtenerMapaConfiguracion();
    }

    // ==========================================
    // 1. PÁGINA PRINCIPAL (GET /)
    // ==========================================
    @GetMapping("/")
    public String index(Model model) {
        List<PortalServicioDtos.ServicioResponse> servicios = servicioService.listarServiciosPublicos();
        List<PortalNoticiaDtos.NoticiaResponse> noticias = contenidoService.obtenerNoticiasDestacadas();
        List<PortalFaqDtos.FaqResponse> faqs = contenidoService.listarFaqPublicas();

        model.addAttribute("servicios", servicios);
        model.addAttribute("noticias", noticias);
        model.addAttribute("faqsDestacadas", faqs.stream().limit(4).toList());
        model.addAttribute("paginaActiva", "inicio");
        return "portal/index";
    }

    // ==========================================
    // 2. CATÁLOGO DE SERVICIOS (GET /servicios)
    // ==========================================
    @GetMapping("/servicios")
    public String servicios(Model model) {
        List<PortalServicioDtos.ServicioResponse> servicios = servicioService.listarServiciosPublicos();
        model.addAttribute("servicios", servicios);
        model.addAttribute("paginaActiva", "servicios");
        return "portal/servicios";
    }

    // ==========================================
    // 3. INFORMACIÓN INSTITUCIONAL (GET /nosotros)
    // ==========================================
    @GetMapping({"/nosotros", "/institucional"})
    public String nosotros(Model model) {
        List<Oficina> oficinas = oficinaRepository.findByEstado("ACTIVO");
        model.addAttribute("oficinas", oficinas);
        model.addAttribute("paginaActiva", "nosotros");
        return "portal/nosotros";
    }

    // ==========================================
    // 4. MESA DE PARTES VIRTUAL (GET /mesa-partes)
    // ==========================================
    @GetMapping("/mesa-partes")
    public String mesaPartes(Model model) {
        model.addAttribute("paginaActiva", "mesa-partes");
        return "portal/mesa-partes";
    }

    // ==========================================
    // 5. CONSULTA PÚBLICA DE TRÁMITES (GET /tramites/consulta)
    // ==========================================
    @GetMapping({"/tramites/consulta", "/consulta-tramite"})
    public String consultaTramite(Model model) {
        model.addAttribute("paginaActiva", "tramites");
        return "portal/consulta-tramite";
    }

    // ==========================================
    // 6. NOTICIAS Y COMUNICADOS (GET /noticias)
    // ==========================================
    @GetMapping("/noticias")
    public String noticias(
            @RequestParam(value = "tipo", required = false) String tipo,
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {

        Page<PortalNoticiaDtos.NoticiaResponse> noticiasPage =
                contenidoService.listarNoticiasPublicas(tipo, q, page, 9);

        model.addAttribute("noticiasPage", noticiasPage);
        model.addAttribute("filtroTipo", tipo != null ? tipo : "TODOS");
        model.addAttribute("busquedaQ", q != null ? q : "");
        model.addAttribute("paginaActiva", "noticias");
        return "portal/noticias";
    }

    @GetMapping("/noticias/{slug}")
    public String detalleNoticia(@PathVariable("slug") String slug, Model model) {
        try {
            PortalNoticiaDtos.NoticiaDetailResponse noticia = contenidoService.obtenerDetalleNoticiaPublica(slug);
            model.addAttribute("noticia", noticia);
            model.addAttribute("noticiasRelacionadas", contenidoService.obtenerNoticiasDestacadas().stream()
                    .filter(n -> !n.getSlug().equals(slug))
                    .limit(3)
                    .toList());
            model.addAttribute("paginaActiva", "noticias");
            return "portal/noticia-detalle";
        } catch (ResourceNotFoundException ex) {
            return "redirect:/noticias?error=not_found";
        }
    }

    @GetMapping("/comunicados")
    public String comunicados(Model model) {
        return "redirect:/noticias?tipo=COMUNICADO";
    }

    // ==========================================
    // 7. PREGUNTAS FRECUENTES (GET /faq)
    // ==========================================
    @GetMapping("/faq")
    public String faq(
            @RequestParam(value = "q", required = false) String q,
            Model model) {

        List<PortalFaqDtos.FaqResponse> faqs = contenidoService.buscarFaqPublicas(q);
        List<String> categorias = contenidoService.obtenerCategoriasFaq();

        model.addAttribute("faqs", faqs);
        model.addAttribute("categorias", categorias);
        model.addAttribute("busquedaQ", q != null ? q : "");
        model.addAttribute("paginaActiva", "faq");
        return "portal/faq";
    }

    // ==========================================
    // 8. FORMULARIO DE CONTACTO (GET y POST /contacto)
    // ==========================================
    @GetMapping("/contacto")
    public String contactoForm(Model model) {
        if (!model.containsAttribute("contactoRequest")) {
            model.addAttribute("contactoRequest", new PortalContactoDtos.ContactoPublicRequest());
        }
        model.addAttribute("paginaActiva", "contacto");
        return "portal/contacto";
    }

    @PostMapping("/contacto")
    public String enviarContacto(
            @Valid @ModelAttribute("contactoRequest") PortalContactoDtos.ContactoPublicRequest request,
            BindingResult bindingResult,
            HttpServletRequest httpRequest,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("paginaActiva", "contacto");
            return "portal/contacto";
        }

        try {
            String clientIp = obtenerIpCliente(httpRequest);
            PortalContactoDtos.ContactoResponse response = contactoService.registrarContactoCiudadano(request, clientIp);

            redirectAttributes.addFlashAttribute("mensajeExito",
                    "Su consulta ha sido registrada exitosamente con el código de seguimiento: " + response.getCodigo() +
                    ". Nuestro equipo municipal revisará su solicitud.");
            redirectAttributes.addFlashAttribute("codigoConsulta", response.getCodigo());
            return "redirect:/contacto?exito=true";
        } catch (BusinessException ex) {
            model.addAttribute("errorAlerta", ex.getMessage());
            model.addAttribute("paginaActiva", "contacto");
            return "portal/contacto";
        }
    }

    // ==========================================
    // 9. REGISTRO CIUDADANO (GET y POST /registro)
    // ==========================================
    @GetMapping("/registro")
    public String registroForm(Model model) {
        if (!model.containsAttribute("registroRequest")) {
            model.addAttribute("registroRequest", new CiudadanoRegistroDto.CiudadanoRegistroRequest());
        }
        model.addAttribute("paginaActiva", "registro");
        return "portal/registro";
    }

    @PostMapping("/registro")
    public String procesarRegistro(
            @Valid @ModelAttribute("registroRequest") CiudadanoRegistroDto.CiudadanoRegistroRequest request,
            BindingResult bindingResult,
            HttpServletRequest httpRequest,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("paginaActiva", "registro");
            return "portal/registro";
        }

        try {
            String clientIp = obtenerIpCliente(httpRequest);
            CiudadanoRegistroDto.CiudadanoRegistroResponse response = registroService.registrarCiudadano(request, clientIp);

            redirectAttributes.addFlashAttribute("mensajeExito", response.getMessage());
            return "redirect:/login?registroExitoso=true";
        } catch (BusinessException | ConflictException ex) {
            model.addAttribute("errorAlerta", ex.getMessage());
            model.addAttribute("paginaActiva", "registro");
            return "portal/registro";
        }
    }

    private String obtenerIpCliente(HttpServletRequest request) {
        String xf = request.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isEmpty()) {
            return xf.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
