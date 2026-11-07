package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.portal.SolicitarVinculacionRequest;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.portal.VinculacionResponse;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.SeccionPortalTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.*;

import java.util.UUID;

/**
 * Controlador Web MVC del Portal Tributario Digital del Contribuyente.
 * Ofrece acceso ciudadano seguro, responsivo y accesible bajo arquitectura monolítica integrada.
 */
@Slf4j
@Controller
@RequestMapping("/portal/tributario")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('PORTAL_TRIBUTARIO_ACCEDER') or hasAnyRole('ROLE_CIUDADANO', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_TRIBUTACION')")
public class PortalTributarioWebController {

    private final PortalTributarioContextService contextService;
    private final VinculacionContribuyenteService vinculacionService;
    private final ResumenTributarioCiudadanoService resumenService;
    private final ConsultaPrediosCiudadanoService prediosService;
    private final ConsultaObligacionesCiudadanoService obligacionesService;
    private final ConsultaCuentaCorrienteCiudadanoService cuentaCorrienteService;
    private final ConsultaPagosCiudadanoService pagosService;
    private final ConsultaDocumentosCiudadanoService documentosService;
    private final ConsultaDeudasCiudadanoService deudasService;
    private final ConsultaFraccionamientosCiudadanoService fraccionamientosService;
    private final ConsultaNotificacionesCiudadanoService notificacionesService;

    @ModelAttribute
    public void agregarAtributosComunes(Model model) {
        try {
            model.addAttribute("contextos", contextService.listarContextosDisponibles());
            contextService.obtenerContribuyenteActivo().ifPresent(c -> model.addAttribute("contribuyenteActivo", c));
        } catch (Exception e) {
            log.debug("No se pudieron cargar contextos comunes de sesión: {}", e.getMessage());
        }
    }

    @GetMapping
    public String inicio() {
        return "redirect:/portal/tributario/dashboard";
    }

    @PostMapping("/cambiar-contexto")
    public String cambiarContexto(@RequestParam("contribuyenteId") UUID contribuyenteId,
                                  RedirectAttributes redirectAttributes) {
        try {
            contextService.cambiarContexto(contribuyenteId);
            redirectAttributes.addFlashAttribute("successMessage", "Perfil tributario activo actualizado correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/portal/tributario/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        contextService.registrarAcceso(SeccionPortalTributario.DASHBOARD);
        model.addAttribute("seccionActiva", "dashboard");
        model.addAttribute("dashboard", resumenService.obtenerDashboardCiudadano());
        return "portal/tributario/dashboard";
    }

    @GetMapping("/perfil")
    public String perfil(Model model) {
        contextService.registrarAcceso(SeccionPortalTributario.PERFIL);
        model.addAttribute("seccionActiva", "perfil");
        model.addAttribute("usuario", contextService.obtenerUsuarioAutenticado());
        model.addAttribute("vinculaciones", vinculacionService.listarVinculacionesPorUsuario(contextService.obtenerUsuarioAutenticado().getId()));
        return "portal/tributario/perfil";
    }

    @GetMapping("/vinculaciones")
    public String vinculaciones(Model model) {
        contextService.registrarAcceso(SeccionPortalTributario.VINCULACIONES);
        model.addAttribute("seccionActiva", "vinculaciones");
        model.addAttribute("vinculaciones", vinculacionService.listarVinculacionesPorUsuario(contextService.obtenerUsuarioAutenticado().getId()));
        if (!model.containsAttribute("solicitudRequest")) {
            model.addAttribute("solicitudRequest", new SolicitarVinculacionRequest());
        }
        return "portal/tributario/vinculaciones";
    }

    @PostMapping("/vinculaciones/solicitar")
    public String solicitarVinculacion(@Valid @ModelAttribute("solicitudRequest") SolicitarVinculacionRequest request,
                                       BindingResult bindingResult,
                                       RedirectAttributes redirectAttributes,
                                       Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("seccionActiva", "vinculaciones");
            model.addAttribute("vinculaciones", vinculacionService.listarVinculacionesPorUsuario(contextService.obtenerUsuarioAutenticado().getId()));
            return "portal/tributario/vinculaciones";
        }

        try {
            Usuario usuario = contextService.obtenerUsuarioAutenticado();
            VinculacionResponse resp = vinculacionService.solicitarVinculacion(request, usuario);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Solicitud de vinculación para " + resp.getContribuyenteNombre() + " registrada con estado: " + resp.getEstado().getEtiqueta());
            return "redirect:/portal/tributario/vinculaciones";
        } catch (BusinessException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("seccionActiva", "vinculaciones");
            model.addAttribute("vinculaciones", vinculacionService.listarVinculacionesPorUsuario(contextService.obtenerUsuarioAutenticado().getId()));
            return "portal/tributario/vinculaciones";
        }
    }

    @GetMapping("/predios")
    public String misPredios(Model model) {
        contextService.registrarAcceso(SeccionPortalTributario.PREDIOS);
        model.addAttribute("seccionActiva", "predios");
        model.addAttribute("predios", prediosService.listarMisPredios());
        return "portal/tributario/predios";
    }

    @GetMapping("/predios/{id}")
    public String detallePredio(@PathVariable("id") UUID id, Model model) {
        contextService.registrarAcceso(SeccionPortalTributario.PREDIOS);
        model.addAttribute("seccionActiva", "predios");
        model.addAttribute("predio", prediosService.obtenerDetallePredio(id));
        return "portal/tributario/predio-detalle";
    }

    @GetMapping("/obligaciones")
    public String misObligaciones(@PageableDefault(size = 15) Pageable pageable, Model model) {
        contextService.registrarAcceso(SeccionPortalTributario.OBLIGACIONES);
        model.addAttribute("seccionActiva", "obligaciones");
        model.addAttribute("obligacionesPage", obligacionesService.listarMisObligaciones(pageable));
        return "portal/tributario/obligaciones";
    }

    @GetMapping("/obligaciones/{id}")
    public String detalleObligacion(@PathVariable("id") UUID id, Model model) {
        contextService.registrarAcceso(SeccionPortalTributario.OBLIGACIONES);
        model.addAttribute("seccionActiva", "obligaciones");
        model.addAttribute("obligacion", obligacionesService.obtenerDetalleObligacion(id));
        return "portal/tributario/obligacion-detalle";
    }

    @GetMapping("/impuesto-predial")
    public String impuestoPredial(Model model) {
        contextService.registrarAcceso(SeccionPortalTributario.IMPUESTO_PREDIAL);
        model.addAttribute("seccionActiva", "impuesto-predial");
        model.addAttribute("obligaciones", obligacionesService.listarObligacionesPorTipo("IMPUESTO_PREDIAL"));
        return "portal/tributario/impuesto-predial";
    }

    @GetMapping("/arbitrios")
    public String arbitrios(Model model) {
        contextService.registrarAcceso(SeccionPortalTributario.ARBITRIOS);
        model.addAttribute("seccionActiva", "arbitrios");
        model.addAttribute("obligaciones", obligacionesService.listarObligacionesPorTipo("ARBITRIOS_MUNICIPALES"));
        return "portal/tributario/arbitrios";
    }

    @GetMapping("/cuenta-corriente")
    public String miCuentaCorriente(@PageableDefault(size = 20) Pageable pageable, Model model) {
        contextService.registrarAcceso(SeccionPortalTributario.CUENTA_CORRIENTE);
        model.addAttribute("seccionActiva", "cuenta-corriente");
        cuentaCorrienteService.obtenerMiCuentaCorriente().ifPresent(cta -> model.addAttribute("cuenta", cta));
        model.addAttribute("movimientosPage", cuentaCorrienteService.listarMisMovimientos(pageable));
        return "portal/tributario/cuenta-corriente";
    }

    @GetMapping("/cuenta-corriente/pdf")
    public ResponseEntity<byte[]> descargarEstadoCuentaPdf() {
        contextService.registrarAcceso(SeccionPortalTributario.CUENTA_CORRIENTE);
        byte[] pdf = cuentaCorrienteService.generarEstadoCuentaPdf();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"estado_cuenta_tributario_quinua.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/pagos")
    public String misPagos(@PageableDefault(size = 15) Pageable pageable, Model model) {
        contextService.registrarAcceso(SeccionPortalTributario.PAGOS);
        model.addAttribute("seccionActiva", "pagos");
        model.addAttribute("pagosPage", pagosService.listarMisPagos(pageable));
        return "portal/tributario/pagos";
    }

    @GetMapping("/pagos/{id}")
    public String detallePago(@PathVariable("id") UUID id, Model model) {
        contextService.registrarAcceso(SeccionPortalTributario.PAGOS);
        model.addAttribute("seccionActiva", "pagos");
        model.addAttribute("pago", pagosService.obtenerDetallePago(id));
        return "portal/tributario/pago-detalle";
    }

    @GetMapping("/recibos")
    public String misRecibos(Model model) {
        contextService.registrarAcceso(SeccionPortalTributario.RECIBOS);
        model.addAttribute("seccionActiva", "recibos");
        model.addAttribute("recibos", documentosService.listarMisRecibos());
        return "portal/tributario/recibos";
    }

    @GetMapping("/recibos/{id}/pdf")
    public ResponseEntity<byte[]> descargarReciboPdf(@PathVariable("id") UUID id) {
        contextService.registrarAcceso(SeccionPortalTributario.RECIBOS);
        byte[] pdf = documentosService.descargarReciboPdf(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"recibo_tributario_" + id + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/deudas")
    public String misDeudas(Model model) {
        contextService.registrarAcceso(SeccionPortalTributario.DEUDAS);
        model.addAttribute("seccionActiva", "deudas");
        model.addAttribute("resumenDeudas", deudasService.obtenerResumenDeudas());
        return "portal/tributario/deudas";
    }

    @GetMapping("/fraccionamientos")
    public String misFraccionamientos(@PageableDefault(size = 10) Pageable pageable, Model model) {
        contextService.registrarAcceso(SeccionPortalTributario.FRACCIONAMIENTOS);
        model.addAttribute("seccionActiva", "fraccionamientos");
        model.addAttribute("fraccionamientosPage", fraccionamientosService.listarMisFraccionamientos(pageable));
        return "portal/tributario/fraccionamientos";
    }

    @GetMapping("/fraccionamientos/{id}")
    public String detalleFraccionamiento(@PathVariable("id") UUID id, Model model) {
        contextService.registrarAcceso(SeccionPortalTributario.FRACCIONAMIENTOS);
        model.addAttribute("seccionActiva", "fraccionamientos");
        model.addAttribute("fraccionamiento", fraccionamientosService.obtenerDetalleFraccionamiento(id));
        return "portal/tributario/fraccionamiento-detalle";
    }

    @GetMapping("/notificaciones")
    public String misNotificaciones(Model model) {
        contextService.registrarAcceso(SeccionPortalTributario.NOTIFICACIONES);
        model.addAttribute("seccionActiva", "notificaciones");
        model.addAttribute("notificaciones", notificacionesService.listarMisNotificaciones());
        return "portal/tributario/notificaciones";
    }

    @GetMapping("/notificaciones/{id}")
    public String detalleNotificacion(@PathVariable("id") UUID id, Model model) {
        model.addAttribute("seccionActiva", "notificaciones");
        model.addAttribute("notificacion", notificacionesService.obtenerDetalleNotificacion(id));
        return "portal/tributario/notificacion-detalle";
    }

    @GetMapping("/notificaciones/{id}/pdf")
    public ResponseEntity<byte[]> descargarNotificacionPdf(@PathVariable("id") UUID id) {
        byte[] pdf = notificacionesService.descargarCedulaPdf(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"cedula_notificacion_" + id + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}

