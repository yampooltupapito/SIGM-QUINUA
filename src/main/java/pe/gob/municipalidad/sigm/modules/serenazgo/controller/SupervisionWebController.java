package pe.gob.municipalidad.sigm.modules.serenazgo.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
import pe.gob.municipalidad.sigm.common.repository.OficinaRepository;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.supervision.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.repository.SectorPatrullajeRepository;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.SupervisionSerenazgoService;
import pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails;

import java.util.List;
import java.util.UUID;

@Slf4j
@Controller
@RequestMapping("/dashboard/serenazgo/supervision")
@RequiredArgsConstructor
public class SupervisionWebController {

    private final SupervisionSerenazgoService supervisionService;
    private final SectorPatrullajeRepository sectorRepository;
    private final UsuarioRepository usuarioRepository;
    private final OficinaRepository oficinaRepository;

    @GetMapping
    @PreAuthorize("hasAuthority('SERENAZGO_SUPERVISION_VER') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA', 'SUPERVISOR_SERENAZGO', 'OPERADOR_CENTRAL')")
    public String centroSupervision(
            @ModelAttribute("filtro") FiltroSupervisionRequest filtro,
            Model model,
            @AuthenticationPrincipal CustomUserDetails user) {

        IndicadoresSupervisionResponse indicadores = supervisionService.obtenerIndicadores(filtro, user);
        List<BandejaPendienteItemDto> pendientes = supervisionService.obtenerBandejaPendientes(filtro, user);
        GraficosSupervisionDto graficos = supervisionService.obtenerGraficos(filtro, user);

        model.addAttribute("indicadores", indicadores);
        model.addAttribute("pendientes", pendientes);
        model.addAttribute("graficos", graficos);
        model.addAttribute("sectores", sectorRepository.findAll());
        model.addAttribute("tiposRegistro", TipoRegistroSupervision.values());
        model.addAttribute("prioridades", PrioridadAdministrativaSupervision.values());
        model.addAttribute("submenuActivo", "supervision");
        return "admin/serenazgo/supervision/index";
    }

    @GetMapping("/seguimientos")
    @PreAuthorize("hasAuthority('SERENAZGO_SUPERVISION_VER') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA', 'SUPERVISOR_SERENAZGO')")
    public String listarSeguimientos(
            @ModelAttribute("filtro") FiltroSupervisionRequest filtro,
            @PageableDefault(size = 12, sort = "fechaCreacion") Pageable pageable,
            Model model,
            @AuthenticationPrincipal CustomUserDetails user) {

        Page<SeguimientoResponse> pagina = supervisionService.listarSeguimientos(filtro, pageable, user);
        model.addAttribute("pagina", pagina);
        model.addAttribute("estados", EstadoSeguimientoSupervision.values());
        model.addAttribute("tiposRegistro", TipoRegistroSupervision.values());
        model.addAttribute("prioridades", PrioridadAdministrativaSupervision.values());
        model.addAttribute("submenuActivo", "supervision");
        return "admin/serenazgo/supervision/seguimientos";
    }

    @GetMapping("/seguimientos/nuevo")
    @PreAuthorize("hasAuthority('SERENAZGO_SUPERVISION_CREAR_SEGUIMIENTO') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA', 'SUPERVISOR_SERENAZGO')")
    public String formularioNuevoSeguimiento(
            @RequestParam(required = false) TipoRegistroSupervision tipo,
            @RequestParam(required = false) UUID id,
            @RequestParam(required = false) String codigo,
            Model model) {

        CrearSeguimientoRequest req = CrearSeguimientoRequest.builder()
                .tipoRegistroOrigen(tipo != null ? tipo : TipoRegistroSupervision.INCIDENCIA)
                .registroOrigenId(id)
                .registroOrigenCodigo(codigo)
                .tipoSeguimiento(TipoSeguimientoSupervision.REVISION)
                .prioridadAdministrativa(PrioridadAdministrativaSupervision.MEDIA)
                .build();

        model.addAttribute("seguimiento", req);
        model.addAttribute("tiposRegistro", TipoRegistroSupervision.values());
        model.addAttribute("tiposSeguimiento", TipoSeguimientoSupervision.values());
        model.addAttribute("prioridades", PrioridadAdministrativaSupervision.values());
        model.addAttribute("usuarios", usuarioRepository.findAll());
        model.addAttribute("oficinas", oficinaRepository.findAll());
        model.addAttribute("submenuActivo", "supervision");
        return "admin/serenazgo/supervision/form";
    }

    @PostMapping("/seguimientos")
    @PreAuthorize("hasAuthority('SERENAZGO_SUPERVISION_CREAR_SEGUIMIENTO') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA', 'SUPERVISOR_SERENAZGO')")
    public String registrarSeguimiento(
            @Valid @ModelAttribute("seguimiento") CrearSeguimientoRequest req,
            BindingResult bindingResult,
            Model model,
            @AuthenticationPrincipal CustomUserDetails user,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("tiposRegistro", TipoRegistroSupervision.values());
            model.addAttribute("tiposSeguimiento", TipoSeguimientoSupervision.values());
            model.addAttribute("prioridades", PrioridadAdministrativaSupervision.values());
            model.addAttribute("usuarios", usuarioRepository.findAll());
            model.addAttribute("oficinas", oficinaRepository.findAll());
            model.addAttribute("submenuActivo", "supervision");
            return "admin/serenazgo/supervision/form";
        }

        try {
            SeguimientoResponse resp = supervisionService.crearSeguimiento(req, user, request.getRemoteAddr());
            redirectAttributes.addFlashAttribute("mensajeExito", "Expediente de supervisión " + resp.getCodigo() + " aperturado correctamente.");
            return "redirect:/dashboard/serenazgo/supervision/seguimientos/" + resp.getId();
        } catch (BusinessException e) {
            model.addAttribute("mensajeError", e.getMessage());
            model.addAttribute("tiposRegistro", TipoRegistroSupervision.values());
            model.addAttribute("tiposSeguimiento", TipoSeguimientoSupervision.values());
            model.addAttribute("prioridades", PrioridadAdministrativaSupervision.values());
            model.addAttribute("usuarios", usuarioRepository.findAll());
            model.addAttribute("oficinas", oficinaRepository.findAll());
            model.addAttribute("submenuActivo", "supervision");
            return "admin/serenazgo/supervision/form";
        }
    }

    @GetMapping("/seguimientos/{id}")
    @PreAuthorize("hasAuthority('SERENAZGO_SUPERVISION_VER') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA', 'SUPERVISOR_SERENAZGO')")
    public String detalleSeguimiento(
            @PathVariable UUID id,
            Model model,
            @AuthenticationPrincipal CustomUserDetails user) {

        SeguimientoDetalleResponse detalle = supervisionService.obtenerDetalleSeguimiento(id, user);
        model.addAttribute("detalle", detalle);
        model.addAttribute("usuarios", usuarioRepository.findAll());
        model.addAttribute("oficinas", oficinaRepository.findAll());
        model.addAttribute("submenuActivo", "supervision");
        return "admin/serenazgo/supervision/detalle";
    }

    @PostMapping("/seguimientos/{id}/asignar")
    @PreAuthorize("hasAuthority('SERENAZGO_SUPERVISION_ASIGNAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA', 'SUPERVISOR_SERENAZGO')")
    public String asignarResponsable(
            @PathVariable UUID id,
            @Valid @ModelAttribute AsignarResponsableRequest req,
            @AuthenticationPrincipal CustomUserDetails user,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        supervisionService.asignarResponsable(id, req, user, request.getRemoteAddr());
        redirectAttributes.addFlashAttribute("mensajeExito", "Responsable asignado satisfactoriamente.");
        return "redirect:/dashboard/serenazgo/supervision/seguimientos/" + id;
    }

    @PostMapping("/seguimientos/{id}/observar")
    @PreAuthorize("hasAuthority('SERENAZGO_SUPERVISION_OBSERVAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA', 'SUPERVISOR_SERENAZGO')")
    public String registrarObservacion(
            @PathVariable UUID id,
            @Valid @ModelAttribute RegistrarObservacionRequest req,
            @AuthenticationPrincipal CustomUserDetails user,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        supervisionService.registrarObservacion(id, req, user, request.getRemoteAddr());
        redirectAttributes.addFlashAttribute("mensajeExito", "Observación técnica registrada. Estado actualizado a OBSERVADO.");
        return "redirect:/dashboard/serenazgo/supervision/seguimientos/" + id;
    }

    @PostMapping("/seguimientos/{id}/subsanar")
    @PreAuthorize("hasAuthority('SERENAZGO_SUPERVISION_SUBSANAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA', 'SUPERVISOR_SERENAZGO', 'SERENO')")
    public String registrarSubsanacion(
            @PathVariable UUID id,
            @Valid @ModelAttribute RegistrarSubsanacionRequest req,
            @AuthenticationPrincipal CustomUserDetails user,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        supervisionService.registrarSubsanacion(id, req, user, request.getRemoteAddr());
        redirectAttributes.addFlashAttribute("mensajeExito", "Descargo y subsanación ingresados con éxito. Estado actualizado a EN SUBSANACIÓN.");
        return "redirect:/dashboard/serenazgo/supervision/seguimientos/" + id;
    }

    @PostMapping("/seguimientos/{id}/revisar")
    @PreAuthorize("hasAuthority('SERENAZGO_SUPERVISION_REVISAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA', 'SUPERVISOR_SERENAZGO')")
    public String revisarRespuesta(
            @PathVariable UUID id,
            @Valid @ModelAttribute RevisarRespuestaRequest req,
            @AuthenticationPrincipal CustomUserDetails user,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        supervisionService.revisarRespuesta(id, req, user, request.getRemoteAddr());
        redirectAttributes.addFlashAttribute("mensajeExito", "Pronunciamiento sobre subsanación asentado correctamente.");
        return "redirect:/dashboard/serenazgo/supervision/seguimientos/" + id;
    }

    @PostMapping("/seguimientos/{id}/escalar")
    @PreAuthorize("hasAuthority('SERENAZGO_SUPERVISION_ESCALAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA', 'SUPERVISOR_SERENAZGO')")
    public String escalarSeguimiento(
            @PathVariable UUID id,
            @Valid @ModelAttribute EscalarSeguimientoRequest req,
            @AuthenticationPrincipal CustomUserDetails user,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        supervisionService.escalarSeguimiento(id, req, user, request.getRemoteAddr());
        redirectAttributes.addFlashAttribute("mensajeExito", "Seguimiento escalado a la oficina " + req.getOficinaDestinataria());
        return "redirect:/dashboard/serenazgo/supervision/seguimientos/" + id;
    }

    @PostMapping("/seguimientos/{id}/resolver")
    @PreAuthorize("hasAuthority('SERENAZGO_SUPERVISION_RESOLVER') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA', 'SUPERVISOR_SERENAZGO')")
    public String resolverSeguimiento(
            @PathVariable UUID id,
            @Valid @ModelAttribute ResolverSeguimientoRequest req,
            @AuthenticationPrincipal CustomUserDetails user,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        supervisionService.resolverSeguimiento(id, req, user, request.getRemoteAddr());
        redirectAttributes.addFlashAttribute("mensajeExito", "Resolución administrativa registrada. Expediente RESUELTO.");
        return "redirect:/dashboard/serenazgo/supervision/seguimientos/" + id;
    }

    @PostMapping("/seguimientos/{id}/cerrar")
    @PreAuthorize("hasAuthority('SERENAZGO_SUPERVISION_CERRAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA', 'SUPERVISOR_SERENAZGO')")
    public String cerrarSeguimiento(
            @PathVariable UUID id,
            @Valid @ModelAttribute CerrarSeguimientoRequest req,
            @AuthenticationPrincipal CustomUserDetails user,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        supervisionService.cerrarSeguimiento(id, req, user, request.getRemoteAddr());
        redirectAttributes.addFlashAttribute("mensajeExito", "Expediente formalmente concluido y archivado en supervisión.");
        return "redirect:/dashboard/serenazgo/supervision/seguimientos/" + id;
    }

    @PostMapping("/seguimientos/{id}/cancelar")
    @PreAuthorize("hasAuthority('SERENAZGO_SUPERVISION_CERRAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA', 'SUPERVISOR_SERENAZGO')")
    public String cancelarSeguimiento(
            @PathVariable UUID id,
            @Valid @ModelAttribute CancelarSeguimientoRequest req,
            @AuthenticationPrincipal CustomUserDetails user,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        supervisionService.cancelarSeguimiento(id, req, user, request.getRemoteAddr());
        redirectAttributes.addFlashAttribute("mensajeExito", "Expediente de supervisión cancelado.");
        return "redirect:/dashboard/serenazgo/supervision/seguimientos/" + id;
    }

    @GetMapping("/exportar/excel")
    @PreAuthorize("hasAuthority('SERENAZGO_SUPERVISION_EXPORTAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA', 'SUPERVISOR_SERENAZGO')")
    public ResponseEntity<Resource> exportarExcel(
            FiltroSupervisionRequest filtro,
            @AuthenticationPrincipal CustomUserDetails user) {
        Resource res = supervisionService.exportarExcel(filtro, user);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"supervision_serenazgo_quinua.xlsx\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(res);
    }

    @GetMapping("/exportar/pdf")
    @PreAuthorize("hasAuthority('SERENAZGO_SUPERVISION_EXPORTAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA', 'SUPERVISOR_SERENAZGO')")
    public ResponseEntity<Resource> exportarPdf(
            FiltroSupervisionRequest filtro,
            @AuthenticationPrincipal CustomUserDetails user) {
        Resource res = supervisionService.exportarPdf(filtro, user);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"reporte_supervision_serenazgo.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(res);
    }

    @GetMapping("/seguimientos/{id}/pdf")
    @PreAuthorize("hasAuthority('SERENAZGO_SUPERVISION_EXPORTAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA', 'SUPERVISOR_SERENAZGO')")
    public ResponseEntity<Resource> fichaSeguimientoPdf(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails user) {
        Resource res = supervisionService.generarFichaSeguimientoPdf(id, user);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"expediente_supervision_" + id + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(res);
    }
}
