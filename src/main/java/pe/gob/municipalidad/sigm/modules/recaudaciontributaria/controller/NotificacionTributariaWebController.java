package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.OficinaRepository;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.notificacion.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.specification.NotificacionSpecification;
import pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Controller
@RequestMapping("/dashboard/tributaria/notificaciones")
@RequiredArgsConstructor
public class NotificacionTributariaWebController {

    private final NotificacionTributariaService notificacionService;
    private final PreparacionNotificacionService preparacionService;
    private final LoteNotificacionesService loteService;
    private final AsignacionNotificadorService asignacionService;
    private final IntentoNotificacionService intentoService;
    private final EvidenciaNotificacionService evidenciaService;
    private final CargoRecepcionService cargoService;
    private final ValidacionNotificacionService validacionService;
    private final PlazoNotificacionService plazoService;
    private final HistorialNotificacionService historialService;
    private final ReporteNotificacionesService reporteService;

    private final TipoNotificacionTributariaRepository tipoRepository;
    private final MedioNotificacionTributariaRepository medioRepository;
    private final ContribuyenteTributarioRepository contribuyenteRepository;
    private final ObligacionTributariaRepository obligacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final OficinaRepository oficinaRepository;
    private final DetalleNotificacionObligacionRepository detalleObligacionRepository;

    // =========================================================================
    // 1. DASHBOARD Y LISTADO PRINCIPAL
    // =========================================================================
    @GetMapping
    @PreAuthorize("hasAuthority('TRIBUTARIA_NOTIFICACIONES_VER') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_TRIBUTACION', 'ROLE_CAJERO')")
    public String dashboard(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) UUID tipoId,
            @RequestParam(required = false) CategoriaTipoNotificacion categoria,
            @RequestParam(required = false) UUID medioId,
            @RequestParam(required = false) EstadoAdministrativoNotificacion estadoAdm,
            @RequestParam(required = false) EstadoEntregaNotificacion estadoEntrega,
            @RequestParam(required = false) EstadoValidezLegalNotificacion estadoValidez,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        DashboardNotificacionResponse dashboardMetrics = notificacionService.obtenerDashboardMetrics();

        Specification<NotificacionTributaria> spec = NotificacionSpecification.conFiltros(
                q, tipoId, categoria, medioId, estadoAdm, estadoEntrega, estadoValidez, fechaDesde, fechaHasta);

        Page<NotificacionResponse> notificaciones = notificacionService.listar(
                spec, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "fechaCreacion")));

        model.addAttribute("dashboard", dashboardMetrics);
        model.addAttribute("notificacionesTributarias", notificaciones);
        model.addAttribute("tipos", tipoRepository.findByEstadoOrderByNombreAsc("ACTIVO"));
        model.addAttribute("medios", medioRepository.findByEstadoOrderByNombreAsc("ACTIVO"));

        // Parámetros de búsqueda actuales
        model.addAttribute("q", q);
        model.addAttribute("tipoId", tipoId);
        model.addAttribute("categoria", categoria);
        model.addAttribute("medioId", medioId);
        model.addAttribute("estadoAdm", estadoAdm);
        model.addAttribute("estadoEntrega", estadoEntrega);
        model.addAttribute("estadoValidez", estadoValidez);
        model.addAttribute("fechaDesde", fechaDesde);
        model.addAttribute("fechaHasta", fechaHasta);

        model.addAttribute("moduloActivo", "tributaria");
        model.addAttribute("submenuActivo", "notificaciones");
        return "admin/tributaria/notificaciones/dashboard";
    }

    // =========================================================================
    // 2. FORMULARIO DE NUEVA NOTIFICACIÓN INDIVIDUAL
    // =========================================================================
    @GetMapping("/nueva")
    @PreAuthorize("hasAuthority('TRIBUTARIA_NOTIFICACIONES_CREAR') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_TRIBUTACION')")
    public String nuevaNotificacionForm(
            @RequestParam(required = false) UUID contribuyenteId,
            @RequestParam(required = false) UUID obligacionId,
            Model model) {

        CrearNotificacionRequest req = new CrearNotificacionRequest();
        if (contribuyenteId != null) {
            contribuyenteRepository.findById(contribuyenteId).ifPresent(c -> {
                req.setContribuyenteId(c.getId());
                req.setDestinatarioNombre(c.getPersona() != null ? c.getPersona().getNombreCompleto() : c.getCodigoContribuyente());
                req.setDestinatarioNumeroDoc(c.getPersona() != null ? c.getPersona().getNumeroDocumento() : c.getCodigoContribuyente());
            });
        }
        if (obligacionId != null) {
            req.setObligacionId(obligacionId);
        }

        model.addAttribute("notificacionRequest", req);
        model.addAttribute("tipos", tipoRepository.findByEstadoOrderByNombreAsc("ACTIVO"));
        model.addAttribute("medios", medioRepository.findByEstadoOrderByNombreAsc("ACTIVO"));
        model.addAttribute("contribuyentes", contribuyenteRepository.findAll(PageRequest.of(0, 100, Sort.by("codigoContribuyente"))).getContent());

        model.addAttribute("moduloActivo", "tributaria");
        model.addAttribute("submenuActivo", "notificaciones");
        return "admin/tributaria/notificaciones/nueva";
    }

    @PostMapping
    @PreAuthorize("hasAuthority('TRIBUTARIA_NOTIFICACIONES_CREAR') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_TRIBUTACION')")
    public String registrarNotificacion(
            @Valid @ModelAttribute("notificacionRequest") CrearNotificacionRequest request,
            BindingResult bindingResult,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("tipos", tipoRepository.findByEstadoOrderByNombreAsc("ACTIVO"));
            model.addAttribute("medios", medioRepository.findByEstadoOrderByNombreAsc("ACTIVO"));
            model.addAttribute("contribuyentes", contribuyenteRepository.findAll(PageRequest.of(0, 100, Sort.by("codigoContribuyente"))).getContent());
            model.addAttribute("moduloActivo", "tributaria");
            model.addAttribute("submenuActivo", "notificaciones");
            return "admin/tributaria/notificaciones/nueva";
        }

        try {
            Usuario operador = obtenerUsuarioAutenticado(userDetails);
            Oficina oficina = null;

            NotificacionTributaria guardada = preparacionService.prepararNotificacion(request, operador, oficina);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Notificación " + guardada.getCodigoNotificacion() + " registrada satisfactoriamente en estado Borrador.");
            return "redirect:/dashboard/tributaria/notificaciones/" + guardada.getId();
        } catch (BusinessException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("tipos", tipoRepository.findByEstadoOrderByNombreAsc("ACTIVO"));
            model.addAttribute("medios", medioRepository.findByEstadoOrderByNombreAsc("ACTIVO"));
            model.addAttribute("contribuyentes", contribuyenteRepository.findAll(PageRequest.of(0, 100, Sort.by("codigoContribuyente"))).getContent());
            model.addAttribute("moduloActivo", "tributaria");
            model.addAttribute("submenuActivo", "notificaciones");
            return "admin/tributaria/notificaciones/nueva";
        }
    }

    // =========================================================================
    // 3. FICHA DETALLADA DE NOTIFICACIÓN (PESTAÑAS)
    // =========================================================================
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('TRIBUTARIA_NOTIFICACIONES_VER') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_TRIBUTACION', 'ROLE_CAJERO')")
    public String detalleNotificacion(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "resumen") String tab,
            Model model) {

        // obtenerPorId es transaccional: convierte a DTO con la sesión Hibernate abierta (evita LazyInitializationException)
        NotificacionResponse response = notificacionService.obtenerPorId(id);

        List<DetalleNotificacionObligacion> detalles = detalleObligacionRepository.findByNotificacionId(id);
        List<AsignacionNotificadorTributario> asignaciones = asignacionService.listarPorNotificacion(id);
        List<IntentoNotificacionTributaria> intentos = intentoService.listarPorNotificacion(id);
        List<EvidenciaNotificacionTributaria> evidencias = evidenciaService.listarPorNotificacion(id);
        var cargoOpt = cargoService.obtenerPorNotificacion(id);
        var validaciones = validacionService.listarPorNotificacion(id);
        var plazoOpt = plazoService.obtenerPorNotificacion(id);
        var historial = historialService.listarEventos(id);

        model.addAttribute("notificacion", response);
        model.addAttribute("detalles", detalles);
        model.addAttribute("asignaciones", asignaciones);
        model.addAttribute("intentos", intentos);
        model.addAttribute("evidencias", evidencias);
        model.addAttribute("cargo", cargoOpt.orElse(null));
        model.addAttribute("validaciones", validaciones);
        model.addAttribute("controlPlazo", plazoOpt.orElse(null));
        model.addAttribute("historial", historial);
        model.addAttribute("tabActiva", tab);

        // Notificadores y personal autorizado para asignar
        model.addAttribute("usuariosNotificadores", usuarioRepository.findAll());

        model.addAttribute("moduloActivo", "tributaria");
        model.addAttribute("submenuActivo", "notificaciones");
        return "admin/tributaria/notificaciones/detalle";
    }

    // =========================================================================
    // 4. ACCIONES ADMINISTRATIVAS: VALIDAR, AUTORIZAR, ANULAR
    // =========================================================================
    @PostMapping("/{id}/validar")
    @PreAuthorize("hasAuthority('TRIBUTARIA_NOTIFICACIONES_VALIDAR') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_TRIBUTACION')")
    public String validar(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            Usuario operador = obtenerUsuarioAutenticado(userDetails);
            notificacionService.validarNotificacion(id, operador);
            redirectAttributes.addFlashAttribute("successMessage", "La notificación ha sido validada formalmente.");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/dashboard/tributaria/notificaciones/" + id;
    }

    @PostMapping("/{id}/autorizar")
    @PreAuthorize("hasAuthority('TRIBUTARIA_NOTIFICACIONES_AUTORIZAR') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_TRIBUTACION')")
    public String autorizar(
            @PathVariable UUID id,
            HttpServletRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            String baseUrl = request.getScheme() + "://" + request.getServerName() +
                    (request.getServerPort() != 80 && request.getServerPort() != 443 ? ":" + request.getServerPort() : "");
            Usuario operador = obtenerUsuarioAutenticado(userDetails);
            NotificacionResponse resp = notificacionService.autorizarNotificacion(id, baseUrl, operador);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Notificación autorizada formalmente. Se generó cédula oficial con código QR y Hash: " + resp.getHashDocumento());
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/dashboard/tributaria/notificaciones/" + id;
    }

    @PostMapping("/{id}/anular")
    @PreAuthorize("hasAuthority('TRIBUTARIA_NOTIFICACIONES_ANULAR') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_TRIBUTACION')")
    public String anular(
            @PathVariable UUID id,
            @Valid @ModelAttribute AnularNotificacionRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            Usuario operador = obtenerUsuarioAutenticado(userDetails);
            notificacionService.anularNotificacion(id, request.getMotivoAnulacion(), operador);
            redirectAttributes.addFlashAttribute("successMessage", "La notificación fue anulada conforme a ley.");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/dashboard/tributaria/notificaciones/" + id;
    }

    // =========================================================================
    // 5. ASIGNACIÓN DE NOTIFICADOR
    // =========================================================================
    @PostMapping("/{id}/asignar")
    @PreAuthorize("hasAuthority('TRIBUTARIA_NOTIFICACIONES_ASIGNAR') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_TRIBUTACION')")
    public String asignar(
            @PathVariable UUID id,
            @Valid @ModelAttribute AsignarNotificadorRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            Usuario operador = obtenerUsuarioAutenticado(userDetails);
            asignacionService.asignarNotificador(id, request.getNotificadorId(), request.getFechaProgramada(),
                    request.getSectorRuta(), request.getObservaciones(), operador);
            redirectAttributes.addFlashAttribute("successMessage", "Notificación asignada exitosamente al notificador institucional.");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/dashboard/tributaria/notificaciones/" + id + "?tab=programacion";
    }

    // =========================================================================
    // 6. REGISTRO DE INTENTO DE NOTIFICACIÓN
    // =========================================================================
    @PostMapping("/{id}/intentos")
    @PreAuthorize("hasAuthority('TRIBUTARIA_NOTIFICACIONES_REGISTRAR_INTENTO') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_TRIBUTACION')")
    public String registrarIntento(
            @PathVariable UUID id,
            @Valid @ModelAttribute RegistrarIntentoRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            Usuario notificador = obtenerUsuarioAutenticado(userDetails);
            intentoService.registrarIntento(id, request, notificador);
            redirectAttributes.addFlashAttribute("successMessage", "Intento de notificación asentado con éxito en el expediente.");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/dashboard/tributaria/notificaciones/" + id + "?tab=intentos";
    }

    // =========================================================================
    // 7. CARGA DE EVIDENCIAS
    // =========================================================================
    @PostMapping("/{id}/evidencias")
    @PreAuthorize("hasAuthority('TRIBUTARIA_NOTIFICACIONES_ADJUNTAR_EVIDENCIA') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_TRIBUTACION')")
    public String adjuntarEvidencia(
            @PathVariable UUID id,
            @RequestParam(required = false) UUID intentoId,
            @RequestParam TipoEvidenciaNotificacion tipoEvidencia,
            @RequestParam(required = false) MultipartFile archivo,
            @RequestParam(required = false) Long documentoId,
            @RequestParam(required = false) String observaciones,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            Usuario operador = obtenerUsuarioAutenticado(userDetails);
            evidenciaService.adjuntarEvidencia(id, intentoId, tipoEvidencia, archivo, documentoId, observaciones, operador);
            redirectAttributes.addFlashAttribute("successMessage", "Evidencia incorporada y asegurada con hash SHA-256.");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/dashboard/tributaria/notificaciones/" + id + "?tab=evidencias";
    }

    // =========================================================================
    // 8. REGISTRO DE CARGO DE RECEPCIÓN
    // =========================================================================
    @PostMapping("/{id}/cargo")
    @PreAuthorize("hasAuthority('TRIBUTARIA_NOTIFICACIONES_REGISTRAR_CARGO') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_TRIBUTACION')")
    public String registrarCargo(
            @PathVariable UUID id,
            @Valid @ModelAttribute RegistrarCargoRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            Usuario operador = obtenerUsuarioAutenticado(userDetails);
            cargoService.registrarCargo(id, request, operador);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Cargo de recepción registrado con éxito. Estado de entrega actualizado a: Entregada con Evidencia.");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/dashboard/tributaria/notificaciones/" + id + "?tab=cargo";
    }

    // =========================================================================
    // 9. VALIDACIÓN LEGAL PROCEDIMENTAL (CHECKLIST)
    // =========================================================================
    @PostMapping("/{id}/validar-procedimiento")
    @PreAuthorize("hasAuthority('TRIBUTARIA_NOTIFICACIONES_VALIDAR_PROCEDIMIENTO') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_TRIBUTACION')")
    public String validarProcedimiento(
            @PathVariable UUID id,
            @Valid @ModelAttribute ValidarProcedimientoRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            Usuario revisor = obtenerUsuarioAutenticado(userDetails);
            validacionService.calificarProcedimiento(id, request, revisor);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Calificación legal procedimental asentada formalmente: " + request.getResultado().getEtiqueta());
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/dashboard/tributaria/notificaciones/" + id + "?tab=validacion";
    }

    // =========================================================================
    // 10. CÓMPUTO DE PLAZOS
    // =========================================================================
    @PostMapping("/{id}/plazos")
    @PreAuthorize("hasAuthority('TRIBUTARIA_NOTIFICACIONES_VALIDAR_PROCEDIMIENTO') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_TRIBUTACION')")
    public String computarPlazo(
            @PathVariable UUID id,
            @Valid @ModelAttribute ComputarPlazoRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            NotificacionTributaria notif = notificacionService.obtenerEntidadPorId(id);
            Usuario operador = obtenerUsuarioAutenticado(userDetails);
            plazoService.registrarOActualizarPlazo(notif, request.getTipoPlazo(), request.getDiasHabiles(),
                    request.getNumeroDias(), request.getFechaHitoInicial(), operador);
            redirectAttributes.addFlashAttribute("successMessage", "Plazo legal computado y monitoreado activamente.");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/dashboard/tributaria/notificaciones/" + id + "?tab=plazos";
    }

    // =========================================================================
    // 11. DESCARGA DE DOCUMENTO OFICIAL PDF
    // =========================================================================
    @GetMapping("/{id}/pdf")
    @PreAuthorize("hasAuthority('TRIBUTARIA_NOTIFICACIONES_VER') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_TRIBUTACION', 'ROLE_CAJERO')")
    public ResponseEntity<byte[]> descargarPdf(
            @PathVariable UUID id,
            HttpServletRequest request) {
        String baseUrl = request.getScheme() + "://" + request.getServerName() +
                (request.getServerPort() != 80 && request.getServerPort() != 443 ? ":" + request.getServerPort() : "");

        byte[] pdfBytes = notificacionService.descargarPdfNotificacion(id, baseUrl);
        NotificacionResponse notif = notificacionService.obtenerPorId(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"cedula_" + notif.getCodigoNotificacion() + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    // =========================================================================
    // 12. GESTIÓN DE LOTES
    // =========================================================================
    @GetMapping("/lotes")
    @PreAuthorize("hasAuthority('TRIBUTARIA_NOTIFICACIONES_GENERAR_LOTES') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_TRIBUTACION')")
    public String lotesDashboard(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        Page<LoteNotificacionesTributarias> lotes = loteService.listarLotes(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "fechaCreacion")));
        model.addAttribute("lotes", lotes);
        model.addAttribute("tipos", tipoRepository.findByEstadoOrderByNombreAsc("ACTIVO"));
        model.addAttribute("medios", medioRepository.findByEstadoOrderByNombreAsc("ACTIVO"));
        model.addAttribute("moduloActivo", "tributaria");
        model.addAttribute("submenuActivo", "notificaciones");
        return "admin/tributaria/notificaciones/lotes";
    }

    @PostMapping("/lotes")
    @PreAuthorize("hasAuthority('TRIBUTARIA_NOTIFICACIONES_GENERAR_LOTES') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_TRIBUTACION')")
    public String crearLote(
            @Valid @ModelAttribute CrearLoteNotificacionesRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            Usuario operador = obtenerUsuarioAutenticado(userDetails);
            Oficina oficina = null;
            LoteNotificacionesTributarias lote = loteService.crearLote(request, operador, oficina);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Lote " + lote.getCodigo() + " generado con " + lote.getTotalRegistros() + " comunicaciones preparadas.");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/dashboard/tributaria/notificaciones/lotes";
    }

    @PostMapping("/lotes/{loteId}/autorizar")
    @PreAuthorize("hasAuthority('TRIBUTARIA_NOTIFICACIONES_AUTORIZAR') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_TRIBUTACION')")
    public String autorizarLote(
            @PathVariable UUID loteId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            Usuario operador = obtenerUsuarioAutenticado(userDetails);
            LoteNotificacionesTributarias lote = loteService.autorizarLote(loteId, operador);
            redirectAttributes.addFlashAttribute("successMessage", "Lote " + lote.getCodigo() + " autorizado formalmente.");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/dashboard/tributaria/notificaciones/lotes";
    }

    // =========================================================================
    // 13. REPORTES Y EXPORTACIONES
    // =========================================================================
    @GetMapping("/reportes")
    @PreAuthorize("hasAuthority('TRIBUTARIA_NOTIFICACIONES_REPORTES') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_TRIBUTACION')")
    public String reportes(Model model) {
        DashboardNotificacionResponse dashboard = notificacionService.obtenerDashboardMetrics();
        var proximosAVencer = plazoService.listarProximosAVencer();

        model.addAttribute("dashboard", dashboard);
        model.addAttribute("plazosProximos", proximosAVencer);
        model.addAttribute("tipos", tipoRepository.findByEstadoOrderByNombreAsc("ACTIVO"));
        model.addAttribute("medios", medioRepository.findByEstadoOrderByNombreAsc("ACTIVO"));
        model.addAttribute("moduloActivo", "tributaria");
        model.addAttribute("submenuActivo", "notificaciones");
        return "admin/tributaria/notificaciones/reportes";
    }

    @GetMapping("/exportar/csv")
    @PreAuthorize("hasAuthority('TRIBUTARIA_NOTIFICACIONES_EXPORTAR') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_TRIBUTACION')")
    public ResponseEntity<byte[]> exportarCsv(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) UUID tipoId,
            @RequestParam(required = false) CategoriaTipoNotificacion categoria,
            @RequestParam(required = false) UUID medioId,
            @RequestParam(required = false) EstadoAdministrativoNotificacion estadoAdm,
            @RequestParam(required = false) EstadoEntregaNotificacion estadoEntrega,
            @RequestParam(required = false) EstadoValidezLegalNotificacion estadoValidez) {

        Specification<NotificacionTributaria> spec = NotificacionSpecification.conFiltros(
                q, tipoId, categoria, medioId, estadoAdm, estadoEntrega, estadoValidez, null, null);
        byte[] csv = reporteService.exportarCsv(spec);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"notificaciones_tributarias_quinua.csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csv);
    }

    @GetMapping("/exportar/pdf")
    @PreAuthorize("hasAuthority('TRIBUTARIA_NOTIFICACIONES_EXPORTAR') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_TRIBUTACION')")
    public ResponseEntity<byte[]> exportarPdf(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) UUID tipoId,
            @RequestParam(required = false) CategoriaTipoNotificacion categoria,
            @RequestParam(required = false) UUID medioId,
            @RequestParam(required = false) EstadoAdministrativoNotificacion estadoAdm,
            @RequestParam(required = false) EstadoEntregaNotificacion estadoEntrega,
            @RequestParam(required = false) EstadoValidezLegalNotificacion estadoValidez) {

        Specification<NotificacionTributaria> spec = NotificacionSpecification.conFiltros(
                q, tipoId, categoria, medioId, estadoAdm, estadoEntrega, estadoValidez, null, null);
        byte[] pdf = reporteService.exportarPdfConsolidado(spec);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"reporte_notificaciones_quinua.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    private Usuario obtenerUsuarioAutenticado(CustomUserDetails userDetails) {
        String username = userDetails != null ? userDetails.getUsername() : "admin";
        return usuarioRepository.findByUsername(username).orElse(null);
    }
}

