package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.controller;

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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.OficinaRepository;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.fraccionamiento.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ContribuyenteTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ObligacionTributaria;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ProgramaFraccionamientoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoConvenioFraccionamiento;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoObligacionTributaria;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoProgramaFraccionamiento;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoSolicitudFraccionamiento;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.*;
import pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Controller
@RequestMapping("/dashboard/tributaria/fraccionamientos")
@RequiredArgsConstructor
public class FraccionamientoWebController {

    private final SolicitudFraccionamientoService solicitudService;
    private final ConvenioFraccionamientoService convenioService;
    private final SimulacionFraccionamientoService simulacionService;
    private final PdfConvenioFraccionamientoService pdfConvenioService;
    private final ProgramaFraccionamientoTributarioRepository programaRepository;
    private final ContribuyenteTributarioRepository contribuyenteRepository;
    private final ObligacionTributariaRepository obligacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final OficinaRepository oficinaRepository;
    private final ConvenioFraccionamientoTributarioRepository convenioRepository;

    @GetMapping
    @PreAuthorize("hasAuthority('TRIBUTARIA_FRACCIONAMIENTOS_VER') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_TRIBUTACION', 'ROLE_CAJERO')")
    public String dashboard(Model model) {
        DashboardFraccionamientoResponse dashboard = solicitudService.obtenerDashboardMetrics();
        List<SolicitudFraccionamientoResponse> solicitudesRecientes = solicitudService.listarSolicitudes(
                (root, query, cb) -> cb.conjunction(), PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt"))
        ).getContent();

        List<ConvenioFraccionamientoResponse> conveniosRecientes = convenioService.listarConvenios(
                (root, query, cb) -> cb.conjunction(), PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "fechaAprobacion"))
        ).getContent();

        model.addAttribute("dashboard", dashboard);
        model.addAttribute("solicitudesRecientes", solicitudesRecientes);
        model.addAttribute("conveniosRecientes", conveniosRecientes);
        model.addAttribute("moduloActivo", "tributaria");
        model.addAttribute("submenuActivo", "fraccionamientos");
        return "admin/tributaria/fraccionamientos/dashboard";
    }

    // =========================================================================
    // SOLICITUDES DE FRACCIONAMIENTO
    // =========================================================================

    @GetMapping("/solicitudes")
    @PreAuthorize("hasAuthority('TRIBUTARIA_FRACCIONAMIENTOS_VER') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_TRIBUTACION', 'ROLE_CAJERO')")
    public String listarSolicitudes(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UUID contribuyenteId,
            @RequestParam(required = false) UUID programaId,
            @RequestParam(required = false) EstadoSolicitudFraccionamiento estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        Specification<pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.SolicitudFraccionamientoTributario> spec =
                SolicitudFraccionamientoSpecification.conFiltros(search, contribuyenteId, programaId, estado, fechaDesde, fechaHasta);

        Page<SolicitudFraccionamientoResponse> solicitudes = solicitudService.listarSolicitudes(
                spec, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "fechaSolicitud", "createdAt"))
        );

        model.addAttribute("solicitudes", solicitudes);
        model.addAttribute("search", search);
        model.addAttribute("estadoSeleccionado", estado);
        model.addAttribute("programaIdSeleccionado", programaId);
        model.addAttribute("estados", EstadoSolicitudFraccionamiento.values());
        model.addAttribute("programas", programaRepository.findAll());
        model.addAttribute("moduloActivo", "tributaria");
        model.addAttribute("submenuActivo", "fraccionamientos");
        return "admin/tributaria/fraccionamientos/solicitudes_list";
    }

    @GetMapping("/solicitudes/nueva")
    @PreAuthorize("hasAuthority('TRIBUTARIA_FRACCIONAMIENTOS_SOLICITAR') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_TRIBUTACION', 'ROLE_CAJERO')")
    public String formularioNuevaSolicitud(
            @RequestParam(required = false) UUID contribuyenteId,
            Model model) {

        ContribuyenteTributario contribuyente = null;
        List<ObligacionTributaria> obligacionesElegibles = List.of();

        if (contribuyenteId != null) {
            contribuyente = contribuyenteRepository.findById(contribuyenteId).orElse(null);
            if (contribuyente != null) {
                obligacionesElegibles = obligacionRepository.findByContribuyenteIdAndEstadoNotIn(
                        contribuyente.getId(),
                        List.of(EstadoObligacionTributaria.ANULADA, EstadoObligacionTributaria.PAGADA, EstadoObligacionTributaria.FRACCIONADA)
                );
            }
        }

        List<ProgramaFraccionamientoTributario> programas = programaRepository.findByEstado(EstadoProgramaFraccionamiento.ACTIVO);

        model.addAttribute("contribuyente", contribuyente);
        model.addAttribute("obligacionesElegibles", obligacionesElegibles);
        model.addAttribute("programas", programas);
        model.addAttribute("formSolicitud", SolicitudFraccionamientoRequest.builder()
                .contribuyenteId(contribuyenteId)
                .numeroCuotasPropuestas(6)
                .montoCuotaInicialPropuesta(BigDecimal.ZERO)
                .build());
        model.addAttribute("moduloActivo", "tributaria");
        model.addAttribute("submenuActivo", "fraccionamientos");
        return "admin/tributaria/fraccionamientos/solicitud_nueva";
    }

    @PostMapping("/solicitudes")
    @PreAuthorize("hasAuthority('TRIBUTARIA_FRACCIONAMIENTOS_SOLICITAR') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_TRIBUTACION', 'ROLE_CAJERO')")
    public String crearSolicitud(
            @Valid @ModelAttribute("formSolicitud") SolicitudFraccionamientoRequest request,
            BindingResult bindingResult,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Verifique los campos obligatorios del formulario");
            return "redirect:/dashboard/tributaria/fraccionamientos/solicitudes/nueva?contribuyenteId=" + request.getContribuyenteId();
        }

        try {
            Usuario usuario = obtenerUsuarioAutenticado(userDetails);
            SolicitudFraccionamientoResponse sol = solicitudService.crearSolicitud(request, usuario);
            redirectAttributes.addFlashAttribute("successMessage", "Solicitud de fraccionamiento registrada con éxito: " + sol.getCodigoSolicitud());
            return "redirect:/dashboard/tributaria/fraccionamientos/solicitudes/" + sol.getId();
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/dashboard/tributaria/fraccionamientos/solicitudes/nueva?contribuyenteId=" + request.getContribuyenteId();
        }
    }

    @GetMapping("/solicitudes/{id}")
    @PreAuthorize("hasAuthority('TRIBUTARIA_FRACCIONAMIENTOS_VER') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_TRIBUTACION', 'ROLE_CAJERO')")
    public String verDetalleSolicitud(@PathVariable UUID id, Model model) {
        SolicitudFraccionamientoResponse solicitud = solicitudService.obtenerPorId(id);

        model.addAttribute("solicitud", solicitud);
        model.addAttribute("formEvaluar", new EvaluarSolicitudRequest());
        model.addAttribute("formObservar", new ObservarSolicitudRequest());
        model.addAttribute("formSubsanar", new SubsanarSolicitudRequest());
        model.addAttribute("formAprobar", AprobarConvenioRequest.builder()
                .fechaInicioVigencia(LocalDate.now())
                .resolucionAprobacion("Resolución de Gerencia Municipal N° " + LocalDate.now().getYear() + "-MDQ")
                .build());
        model.addAttribute("moduloActivo", "tributaria");
        model.addAttribute("submenuActivo", "fraccionamientos");
        return "admin/tributaria/fraccionamientos/solicitud_detail";
    }

    @PostMapping("/solicitudes/{id}/evaluar")
    @PreAuthorize("hasAuthority('TRIBUTARIA_FRACCIONAMIENTOS_EVALUAR') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_TRIBUTACION')")
    public String evaluarSolicitud(
            @PathVariable UUID id,
            @ModelAttribute EvaluarSolicitudRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            Usuario usuario = obtenerUsuarioAutenticado(userDetails);
            solicitudService.evaluarSolicitud(id, request, usuario);
            redirectAttributes.addFlashAttribute("successMessage", "Evaluación registrada correctamente");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/dashboard/tributaria/fraccionamientos/solicitudes/" + id;
    }

    @PostMapping("/solicitudes/{id}/observar")
    @PreAuthorize("hasAuthority('TRIBUTARIA_FRACCIONAMIENTOS_OBSERVAR') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_TRIBUTACION')")
    public String observarSolicitud(
            @PathVariable UUID id,
            @ModelAttribute ObservarSolicitudRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            Usuario usuario = obtenerUsuarioAutenticado(userDetails);
            solicitudService.observarSolicitud(id, request, usuario);
            redirectAttributes.addFlashAttribute("successMessage", "Observación formulada formalmente a la solicitud");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/dashboard/tributaria/fraccionamientos/solicitudes/" + id;
    }

    @PostMapping("/solicitudes/{id}/subsanar")
    @PreAuthorize("hasAuthority('TRIBUTARIA_FRACCIONAMIENTOS_SUBSANAR') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_TRIBUTACION')")
    public String subsanarSolicitud(
            @PathVariable UUID id,
            @ModelAttribute SubsanarSolicitudRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            Usuario usuario = obtenerUsuarioAutenticado(userDetails);
            solicitudService.subsanarSolicitud(id, request, usuario);
            redirectAttributes.addFlashAttribute("successMessage", "Subsanación registrada con éxito");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/dashboard/tributaria/fraccionamientos/solicitudes/" + id;
    }

    @PostMapping("/solicitudes/{id}/rechazar")
    @PreAuthorize("hasAuthority('TRIBUTARIA_FRACCIONAMIENTOS_RECHAZAR') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_TRIBUTACION')")
    public String rechazarSolicitud(
            @PathVariable UUID id,
            @RequestParam String motivo,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            Usuario usuario = obtenerUsuarioAutenticado(userDetails);
            solicitudService.rechazarSolicitud(id, motivo, usuario);
            redirectAttributes.addFlashAttribute("successMessage", "Solicitud rechazada formalmente");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/dashboard/tributaria/fraccionamientos/solicitudes/" + id;
    }

    @PostMapping("/solicitudes/{id}/aprobar")
    @PreAuthorize("hasAuthority('TRIBUTARIA_FRACCIONAMIENTOS_APROBAR') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_TRIBUTACION')")
    public String aprobarConvenio(
            @PathVariable UUID id,
            @Valid @ModelAttribute AprobarConvenioRequest request,
            BindingResult bindingResult,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Debe completar la resolución de aprobación y fecha de vigencia");
            return "redirect:/dashboard/tributaria/fraccionamientos/solicitudes/" + id;
        }

        try {
            Usuario usuario = obtenerUsuarioAutenticado(userDetails);
            ConvenioFraccionamientoResponse conv = convenioService.aprobarConvenio(id, request, usuario);
            redirectAttributes.addFlashAttribute("successMessage", "Convenio formalizado con éxito: " + conv.getCodigoConvenio());
            return "redirect:/dashboard/tributaria/fraccionamientos/convenios/" + conv.getId();
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/dashboard/tributaria/fraccionamientos/solicitudes/" + id;
        }
    }

    // =========================================================================
    // CONVENIOS DE PAGO
    // =========================================================================

    @GetMapping("/convenios")
    @PreAuthorize("hasAuthority('TRIBUTARIA_FRACCIONAMIENTOS_VER') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_TRIBUTACION', 'ROLE_CAJERO')")
    public String listarConvenios(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UUID contribuyenteId,
            @RequestParam(required = false) UUID programaId,
            @RequestParam(required = false) EstadoConvenioFraccionamiento estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        Specification<pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ConvenioFraccionamientoTributario> spec =
                ConvenioFraccionamientoSpecification.conFiltros(search, contribuyenteId, programaId, estado, fechaDesde, fechaHasta);

        Page<ConvenioFraccionamientoResponse> convenios = convenioService.listarConvenios(
                spec, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "fechaAprobacion", "createdAt"))
        );

        model.addAttribute("convenios", convenios);
        model.addAttribute("search", search);
        model.addAttribute("estadoSeleccionado", estado);
        model.addAttribute("programaIdSeleccionado", programaId);
        model.addAttribute("estados", EstadoConvenioFraccionamiento.values());
        model.addAttribute("programas", programaRepository.findAll());
        model.addAttribute("moduloActivo", "tributaria");
        model.addAttribute("submenuActivo", "fraccionamientos");
        return "admin/tributaria/fraccionamientos/convenios_list";
    }

    @GetMapping("/convenios/{id}")
    @PreAuthorize("hasAuthority('TRIBUTARIA_FRACCIONAMIENTOS_VER') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_TRIBUTACION', 'ROLE_CAJERO')")
    public String verDetalleConvenio(@PathVariable UUID id, Model model) {
        ConvenioFraccionamientoResponse convenio = convenioService.obtenerPorId(id);

        model.addAttribute("convenio", convenio);
        model.addAttribute("formReprogramar", ReprogramarConvenioRequest.builder()
                .nuevasCuotas(convenio.getNumeroCuotas() > 0 ? convenio.getNumeroCuotas() : 6)
                .nuevaFechaInicio(LocalDate.now().plusDays(30))
                .resolucionSustento("Resolución de Gerencia N° " + LocalDate.now().getYear() + "-MDQ")
                .build());
        model.addAttribute("formIncidencia", new RegistrarIncidenciaConvenioRequest());
        model.addAttribute("moduloActivo", "tributaria");
        model.addAttribute("submenuActivo", "fraccionamientos");
        return "admin/tributaria/fraccionamientos/convenio_detail";
    }

    @GetMapping("/convenios/{id}/pdf")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('TRIBUTARIA_FRACCIONAMIENTOS_VER') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_TRIBUTACION', 'ROLE_CAJERO')")
    public ResponseEntity<byte[]> descargarPdfConvenio(@PathVariable UUID id) {
        pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ConvenioFraccionamientoTributario conv =
                convenioRepository.findById(id).orElseThrow(() -> new BusinessException("Convenio no encontrado"));

        String urlVerificacion = "https://quinua.gob.pe/verificar-convenio?c=" + conv.getCodigoVerificacion();
        byte[] pdfBytes = pdfConvenioService.generarPdfConvenio(conv, urlVerificacion);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + conv.getCodigoConvenio() + ".pdf\"")
                .body(pdfBytes);
    }

    @PostMapping("/convenios/{id}/reprogramar")
    @PreAuthorize("hasAuthority('TRIBUTARIA_FRACCIONAMIENTOS_REPROGRAMAR') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_TRIBUTACION')")
    public String reprogramarConvenio(
            @PathVariable UUID id,
            @Valid @ModelAttribute ReprogramarConvenioRequest request,
            BindingResult bindingResult,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Verifique los datos obligatorios para la reprogramación");
            return "redirect:/dashboard/tributaria/fraccionamientos/convenios/" + id;
        }

        try {
            Usuario usuario = obtenerUsuarioAutenticado(userDetails);
            convenioService.reprogramarConvenio(id, request, usuario);
            redirectAttributes.addFlashAttribute("successMessage", "Convenio reprogramado correctamente con nuevo cronograma amortizado");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/dashboard/tributaria/fraccionamientos/convenios/" + id;
    }

    @PostMapping("/convenios/{id}/incidencias")
    @PreAuthorize("hasAuthority('TRIBUTARIA_FRACCIONAMIENTOS_GESTIONAR_CONVENIO') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_TRIBUTACION')")
    public String registrarIncidencia(
            @PathVariable UUID id,
            @Valid @ModelAttribute RegistrarIncidenciaConvenioRequest request,
            BindingResult bindingResult,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Debe ingresar una descripción válida");
            return "redirect:/dashboard/tributaria/fraccionamientos/convenios/" + id;
        }

        try {
            Usuario usuario = obtenerUsuarioAutenticado(userDetails);
            convenioService.registrarIncidencia(id, request, usuario);
            redirectAttributes.addFlashAttribute("successMessage", "Actuación administrativa registrada en la bitácora del convenio");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/dashboard/tributaria/fraccionamientos/convenios/" + id;
    }

    @PostMapping("/convenios/{id}/revisar-incumplimiento")
    @PreAuthorize("hasAuthority('TRIBUTARIA_FRACCIONAMIENTOS_REVISAR_INCUMPLIMIENTO') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_TRIBUTACION')")
    public String revisarIncumplimiento(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            Usuario usuario = obtenerUsuarioAutenticado(userDetails);
            convenioService.revisarIncumplimiento(id, usuario);
            redirectAttributes.addFlashAttribute("successMessage", "Evaluación de cumplimiento de cuotas efectuada exitosamente");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/dashboard/tributaria/fraccionamientos/convenios/" + id;
    }

    // =========================================================================
    // SIMULADOR MATEMÁTICO DE FRACCIONAMIENTO
    // =========================================================================

    @GetMapping("/simulador")
    @PreAuthorize("hasAuthority('TRIBUTARIA_FRACCIONAMIENTOS_SIMULAR') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_TRIBUTACION', 'ROLE_CAJERO')")
    public String vistaSimulador(Model model) {
        model.addAttribute("programas", programaRepository.findByEstado(EstadoProgramaFraccionamiento.ACTIVO));
        model.addAttribute("moduloActivo", "tributaria");
        model.addAttribute("submenuActivo", "fraccionamientos");
        return "admin/tributaria/fraccionamientos/simulador";
    }

    @PostMapping("/simular")
    @ResponseBody
    @PreAuthorize("hasAuthority('TRIBUTARIA_FRACCIONAMIENTOS_SIMULAR') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_TRIBUTACION', 'ROLE_CAJERO')")
    public ResponseEntity<?> simularAjax(@RequestBody SimulacionFraccionamientoRequest request) {
        try {
            SimulacionFraccionamientoResponse sim = simulacionService.simular(request);
            return ResponseEntity.ok(sim);
        } catch (BusinessException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", e.getMessage()));
        }
    }

    private Usuario obtenerUsuarioAutenticado(CustomUserDetails userDetails) {
        String username = userDetails != null ? userDetails.getUsername() : "admin";
        return usuarioRepository.findByUsername(username).orElse(null);
    }
}
