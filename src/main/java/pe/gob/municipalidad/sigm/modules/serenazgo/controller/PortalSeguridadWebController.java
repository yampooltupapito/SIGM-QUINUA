package pe.gob.municipalidad.sigm.modules.serenazgo.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.repository.SectorPatrullajeRepository;
import pe.gob.municipalidad.sigm.modules.serenazgo.repository.TipoIncidenciaSeguridadRepository;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.AlertaCiudadanaSeguridadService;

import java.util.List;
import java.util.UUID;

@Slf4j
@Controller
@RequestMapping("/portal/seguridad")
@RequiredArgsConstructor
public class PortalSeguridadWebController {

    private final AlertaCiudadanaSeguridadService alertaService;
    private final TipoIncidenciaSeguridadRepository tipoIncidenciaRepository;
    private final SectorPatrullajeRepository sectorRepository;

    // 1. Formulario Ciudadano de Reportes
    @GetMapping("/reportar")
    public String nuevoReporte(Model model, Authentication auth) {
        if (!model.containsAttribute("reporte")) {
            AlertaRegistroCiudadanoRequest req = new AlertaRegistroCiudadanoRequest();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
                req.setReportanteNombre(auth.getName());
            }
            model.addAttribute("reporte", req);
        }
        cargarCatalogos(model);
        model.addAttribute("paginaActiva", "seguridad_reportar");
        return "portal/seguridad/reportar";
    }

    @PostMapping("/reportar")
    public String registrarReporte(@Valid @ModelAttribute("reporte") AlertaRegistroCiudadanoRequest reporte,
                                   BindingResult result,
                                   @RequestParam(value = "archivos", required = false) List<MultipartFile> archivos,
                                   HttpServletRequest httpRequest,
                                   Authentication auth,
                                   RedirectAttributes redirectAttributes,
                                   Model model) {
        if (result.hasErrors()) {
            cargarCatalogos(model);
            return "portal/seguridad/reportar";
        }

        String username = (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName()))
                ? auth.getName() : null;
        String ipCliente = httpRequest.getRemoteAddr();

        try {
            AlertaConfirmacionResponse conf = alertaService.registrarAlertaCiudadana(reporte, archivos, username, ipCliente);
            redirectAttributes.addFlashAttribute("confirmacion", conf);
            return "redirect:/portal/seguridad/confirmacion";
        } catch (Exception e) {
            log.error("Error al registrar alerta ciudadana", e);
            model.addAttribute("mensajeError", e.getMessage());
            cargarCatalogos(model);
            return "portal/seguridad/reportar";
        }
    }

    // 2. Pantalla de Confirmación
    @GetMapping("/confirmacion")
    public String confirmacion(Model model) {
        if (!model.containsAttribute("confirmacion")) {
            return "redirect:/portal/seguridad/reportar";
        }
        return "portal/seguridad/confirmacion";
    }

    // 3. Consulta de Seguimiento por Código y PIN
    @GetMapping("/seguimiento")
    public String seguimientoForm(@RequestParam(value = "codigo", required = false) String codigo,
                                  Model model) {
        AlertaSeguimientoConsultaRequest req = new AlertaSeguimientoConsultaRequest();
        if (codigo != null) {
            req.setCodigo(codigo.trim().toUpperCase());
        }
        model.addAttribute("consulta", req);
        model.addAttribute("paginaActiva", "seguridad_seguimiento");
        return "portal/seguridad/seguimiento";
    }

    @PostMapping("/seguimiento")
    public String consultarSeguimiento(@Valid @ModelAttribute("consulta") AlertaSeguimientoConsultaRequest consulta,
                                       BindingResult result,
                                       Authentication auth,
                                       Model model) {
        if (result.hasErrors()) {
            return "portal/seguridad/seguimiento";
        }

        String username = (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName()))
                ? auth.getName() : null;

        try {
            AlertaDetailResponse detalle = alertaService.consultarSeguimiento(consulta.getCodigo(), consulta.getPinConsulta(), username);
            model.addAttribute("detalle", detalle);
            model.addAttribute("pinUsado", consulta.getPinConsulta());
            return "portal/seguridad/seguimiento-detalle";
        } catch (AccessDeniedException e) {
            model.addAttribute("mensajeError", e.getMessage());
            return "portal/seguridad/seguimiento";
        } catch (IllegalArgumentException e) {
            model.addAttribute("mensajeError", e.getMessage());
            return "portal/seguridad/seguimiento";
        }
    }

    // 4. Respuesta Ciudadana a Solicitud de Información Complementaria
    @PostMapping("/responder-solicitud")
    public String responderSolicitud(@Valid @ModelAttribute("respuesta") AlertaRespuestaCiudadanoRequest request,
                                     BindingResult result,
                                     Authentication auth,
                                     RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("mensajeError", "Debe proporcionar una respuesta válida");
            return "redirect:/portal/seguridad/seguimiento?codigo=" + request.getCodigo();
        }

        String username = (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName()))
                ? auth.getName() : null;

        try {
            alertaService.responderSolicitudInformacion(request, username);
            redirectAttributes.addFlashAttribute("mensajeExito", "Su respuesta e información complementaria ha sido remitida a la Central de Serenazgo satisfactoriamente.");
        } catch (Exception e) {
            log.error("Fallo al responder solicitud ciudadana", e);
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }

        return "redirect:/portal/seguridad/seguimiento?codigo=" + request.getCodigo();
    }

    // 5. Mis Reportes (Para usuarios autenticados en el portal)
    @GetMapping("/mis-reportes")
    public String misReportes(@RequestParam(value = "page", defaultValue = "0") int page,
                              @RequestParam(value = "size", defaultValue = "10") int size,
                              Authentication auth,
                              Model model) {
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return "redirect:/login";
        }

        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(size, 20));
        Page<AlertaResponse> reportes = alertaService.listarMisReportes(auth.getName(), pageable);

        model.addAttribute("reportes", reportes);
        model.addAttribute("paginaActiva", "seguridad_mis_reportes");
        return "portal/seguridad/mis-reportes";
    }

    @GetMapping("/mis-reportes/{id}")
    public String detalleMiReporte(@PathVariable("id") UUID id,
                                   Authentication auth,
                                   Model model) {
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return "redirect:/login";
        }

        try {
            AlertaDetailResponse detalle = alertaService.obtenerMiReporte(id, auth.getName());
            model.addAttribute("detalle", detalle);
            return "portal/seguridad/seguimiento-detalle";
        } catch (Exception e) {
            return "redirect:/portal/seguridad/mis-reportes";
        }
    }

    private void cargarCatalogos(Model model) {
        model.addAttribute("tiposReporte", tipoIncidenciaRepository.findByActivoTrue());
        model.addAttribute("sectores", sectorRepository.findByEstadoOrderByCodigoAsc(pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoSectorPatrullaje.ACTIVO));
    }
}
