package pe.gob.municipalidad.sigm.modules.mercados.controller.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pe.gob.municipalidad.sigm.modules.mercados.service.*;

import java.util.UUID;

@Slf4j
@Controller
@RequestMapping("/portal/comerciante")
public class PortalComercianteWebController {

    private final ComerciantePortalContextService contextService;
    private final PortalComercianteDashboardService dashboardService;
    private final PortalComerciantePerfilService perfilService;
    private final PortalComerciantePuestoService puestoService;
    private final PortalComercianteContratoService contratoService;
    private final PortalComercianteAutorizacionService autorizacionService;
    private final PortalComercianteFinanzasService finanzasService;
    private final PortalComercianteSolicitudService solicitudService;
    private final PortalComercianteInspeccionService inspeccionService;
    private final PortalComercianteIncidenciaService incidenciaService;
    private final PortalComercianteDocumentoService documentoService;

    public PortalComercianteWebController(
            ComerciantePortalContextService contextService,
            PortalComercianteDashboardService dashboardService,
            PortalComerciantePerfilService perfilService,
            PortalComerciantePuestoService puestoService,
            PortalComercianteContratoService contratoService,
            PortalComercianteAutorizacionService autorizacionService,
            PortalComercianteFinanzasService finanzasService,
            PortalComercianteSolicitudService solicitudService,
            PortalComercianteInspeccionService inspeccionService,
            PortalComercianteIncidenciaService incidenciaService,
            PortalComercianteDocumentoService documentoService) {
        this.contextService = contextService;
        this.dashboardService = dashboardService;
        this.perfilService = perfilService;
        this.puestoService = puestoService;
        this.contratoService = contratoService;
        this.autorizacionService = autorizacionService;
        this.finanzasService = finanzasService;
        this.solicitudService = solicitudService;
        this.inspeccionService = inspeccionService;
        this.incidenciaService = incidenciaService;
        this.documentoService = documentoService;
    }

    @GetMapping
    public String index() {
        return "redirect:/portal/comerciante/dashboard";
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasAuthority('PORTAL_COMERCIANTE_ACCEDER') or hasRole('COMERCIANTE') or hasRole('CIUDADANO') or hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public String dashboard(Model model) {
        model.addAttribute("activeMenu", "dashboard");
        model.addAttribute("dashboard", dashboardService.obtenerDashboard());
        model.addAttribute("contextos", contextService.listarContextosDisponibles());
        return "portal/comerciante/dashboard";
    }

    @GetMapping("/perfil")
    @PreAuthorize("hasAuthority('PORTAL_COMERCIANTE_PERFIL_VER') or hasRole('COMERCIANTE') or hasRole('CIUDADANO') or hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public String perfil(Model model) {
        model.addAttribute("activeMenu", "perfil");
        model.addAttribute("perfil", perfilService.obtenerPerfil());
        return "portal/comerciante/perfil";
    }

    @GetMapping("/puestos")
    @PreAuthorize("hasAuthority('PORTAL_COMERCIANTE_PUESTOS_VER') or hasRole('COMERCIANTE') or hasRole('CIUDADANO') or hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public String puestos(Model model) {
        model.addAttribute("activeMenu", "puestos");
        model.addAttribute("puestos", puestoService.listarMisPuestos());
        return "portal/comerciante/puestos";
    }

    @GetMapping("/puestos/{id}")
    @PreAuthorize("hasAuthority('PORTAL_COMERCIANTE_PUESTOS_VER') or hasRole('COMERCIANTE') or hasRole('CIUDADANO') or hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public String puestoDetalle(@PathVariable UUID id, Model model) {
        model.addAttribute("activeMenu", "puestos");
        model.addAttribute("puesto", puestoService.obtenerDetallePuesto(id));
        return "portal/comerciante/puesto-detalle";
    }

    @GetMapping("/asignaciones")
    @PreAuthorize("hasAuthority('PORTAL_COMERCIANTE_PUESTOS_VER') or hasRole('COMERCIANTE') or hasRole('CIUDADANO') or hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public String asignaciones(Model model) {
        model.addAttribute("activeMenu", "asignaciones");
        model.addAttribute("asignaciones", puestoService.listarMisAsignaciones());
        return "portal/comerciante/asignaciones";
    }

    @GetMapping("/contratos")
    @PreAuthorize("hasAuthority('PORTAL_COMERCIANTE_CONTRATOS_VER') or hasRole('COMERCIANTE') or hasRole('CIUDADANO') or hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public String contratos(@PageableDefault(size = 10) Pageable pageable, Model model) {
        model.addAttribute("activeMenu", "contratos");
        model.addAttribute("contratosPage", contratoService.listarMisContratos(null, pageable));
        return "portal/comerciante/contratos";
    }

    @GetMapping("/contratos/{id}")
    @PreAuthorize("hasAuthority('PORTAL_COMERCIANTE_CONTRATOS_VER') or hasRole('COMERCIANTE') or hasRole('CIUDADANO') or hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public String contratoDetalle(@PathVariable UUID id, Model model) {
        model.addAttribute("activeMenu", "contratos");
        model.addAttribute("contrato", contratoService.obtenerDetalleContrato(id));
        return "portal/comerciante/contrato-detalle";
    }

    @GetMapping("/autorizaciones")
    @PreAuthorize("hasAuthority('PORTAL_COMERCIANTE_AUTORIZACIONES_VER') or hasRole('COMERCIANTE') or hasRole('CIUDADANO') or hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public String autorizaciones(@PageableDefault(size = 10) Pageable pageable, Model model) {
        model.addAttribute("activeMenu", "autorizaciones");
        model.addAttribute("autorizacionesPage", autorizacionService.listarMisAutorizaciones(null, pageable));
        return "portal/comerciante/autorizaciones";
    }

    @GetMapping("/obligaciones")
    @PreAuthorize("hasAuthority('PORTAL_COMERCIANTE_FINANZAS_VER') or hasRole('COMERCIANTE') or hasRole('CIUDADANO') or hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public String obligaciones(@PageableDefault(size = 10) Pageable pageable, Model model) {
        model.addAttribute("activeMenu", "obligaciones");
        model.addAttribute("obligacionesPage", finanzasService.listarMisObligaciones(null, pageable));
        return "portal/comerciante/obligaciones";
    }

    @GetMapping("/pagos")
    @PreAuthorize("hasAuthority('PORTAL_COMERCIANTE_FINANZAS_VER') or hasRole('COMERCIANTE') or hasRole('CIUDADANO') or hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public String pagos(@PageableDefault(size = 10) Pageable pageable, Model model) {
        model.addAttribute("activeMenu", "pagos");
        model.addAttribute("pagosPage", finanzasService.listarMisPagos(pageable));
        return "portal/comerciante/pagos";
    }

    @GetMapping("/estado-cuenta")
    @PreAuthorize("hasAuthority('PORTAL_COMERCIANTE_FINANZAS_VER') or hasRole('COMERCIANTE') or hasRole('CIUDADANO') or hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public String estadoCuenta(Model model) {
        model.addAttribute("activeMenu", "estado-cuenta");
        model.addAttribute("estadoCuenta", finanzasService.obtenerEstadoCuenta());
        return "portal/comerciante/estado-cuenta";
    }

    @GetMapping("/solicitudes")
    @PreAuthorize("hasAuthority('PORTAL_COMERCIANTE_SOLICITUDES_VER') or hasRole('COMERCIANTE') or hasRole('CIUDADANO') or hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public String solicitudes(@PageableDefault(size = 10) Pageable pageable, Model model) {
        model.addAttribute("activeMenu", "solicitudes");
        model.addAttribute("solicitudesPage", solicitudService.listarMisSolicitudes(null, pageable));
        return "portal/comerciante/solicitudes";
    }

    @GetMapping("/solicitudes/nueva")
    @PreAuthorize("hasAuthority('PORTAL_COMERCIANTE_SOLICITUDES_CREAR') or hasRole('COMERCIANTE') or hasRole('CIUDADANO') or hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public String nuevaSolicitud(Model model) {
        model.addAttribute("activeMenu", "solicitudes");
        model.addAttribute("puestos", puestoService.listarMisPuestos());
        return "portal/comerciante/solicitud-renovacion";
    }

    @GetMapping("/inspecciones")
    @PreAuthorize("hasAuthority('PORTAL_COMERCIANTE_ACCEDER') or hasRole('COMERCIANTE') or hasRole('CIUDADANO') or hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public String inspecciones(@PageableDefault(size = 10) Pageable pageable, Model model) {
        model.addAttribute("activeMenu", "inspecciones");
        model.addAttribute("inspeccionesPage", inspeccionService.listarMisInspecciones(null, pageable));
        return "portal/comerciante/inspecciones";
    }

    @GetMapping("/incidencias")
    @PreAuthorize("hasAuthority('PORTAL_COMERCIANTE_ACCEDER') or hasRole('COMERCIANTE') or hasRole('CIUDADANO') or hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public String incidencias(@PageableDefault(size = 10) Pageable pageable, Model model) {
        model.addAttribute("activeMenu", "incidencias");
        model.addAttribute("incidenciasPage", incidenciaService.listarMisIncidencias(null, pageable));
        return "portal/comerciante/incidencias";
    }

    @GetMapping("/incidencias/nueva")
    @PreAuthorize("hasAuthority('PORTAL_COMERCIANTE_INCIDENCIAS_CREAR') or hasRole('COMERCIANTE') or hasRole('CIUDADANO') or hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public String nuevaIncidencia(Model model) {
        model.addAttribute("activeMenu", "incidencias");
        model.addAttribute("puestos", puestoService.listarMisPuestos());
        return "portal/comerciante/incidencia-nueva";
    }

    @GetMapping("/notificaciones")
    @PreAuthorize("hasAuthority('PORTAL_COMERCIANTE_ACCEDER') or hasRole('COMERCIANTE') or hasRole('CIUDADANO') or hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public String notificaciones(Model model) {
        model.addAttribute("activeMenu", "notificaciones");
        return "portal/comerciante/notificaciones";
    }

    @GetMapping("/documentos")
    @PreAuthorize("hasAuthority('PORTAL_COMERCIANTE_DOCUMENTOS_DESCARGAR') or hasRole('COMERCIANTE') or hasRole('CIUDADANO') or hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public String documentos(@RequestParam(required = false) String tipo,
                             @PageableDefault(size = 10) Pageable pageable, Model model) {
        model.addAttribute("activeMenu", "documentos");
        model.addAttribute("tipoSeleccionado", tipo);
        model.addAttribute("documentosPage", documentoService.listarMisDocumentos(tipo, pageable));
        return "portal/comerciante/documentos";
    }

    @GetMapping("/plano")
    @PreAuthorize("hasAuthority('PORTAL_COMERCIANTE_ACCEDER') or hasRole('COMERCIANTE') or hasRole('CIUDADANO') or hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public String plano(Model model) {
        model.addAttribute("activeMenu", "plano");
        model.addAttribute("puestos", puestoService.listarMisPuestos());
        return "portal/comerciante/plano";
    }

    @GetMapping("/ayuda")
    @PreAuthorize("hasAuthority('PORTAL_COMERCIANTE_ACCEDER') or hasRole('COMERCIANTE') or hasRole('CIUDADANO') or hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public String ayuda(Model model) {
        model.addAttribute("activeMenu", "ayuda");
        return "portal/comerciante/ayuda";
    }
}
