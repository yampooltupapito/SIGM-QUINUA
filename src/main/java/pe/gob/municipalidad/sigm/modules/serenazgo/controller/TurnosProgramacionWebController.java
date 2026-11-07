package pe.gob.municipalidad.sigm.modules.serenazgo.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.gob.municipalidad.sigm.common.repository.OficinaRepository;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoProgramacionServicio;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoSectorPatrullaje;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoTurnoSerenazgo;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoServicioSerenazgo;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoTurnoSerenazgo;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoPersonalSerenazgo;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoUnidadMovil;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoEquipamientoSerenazgo;
import pe.gob.municipalidad.sigm.modules.serenazgo.repository.PersonalSerenazgoRepository;
import pe.gob.municipalidad.sigm.modules.serenazgo.repository.UnidadMovilSerenazgoRepository;
import pe.gob.municipalidad.sigm.modules.serenazgo.repository.EquipamientoSerenazgoRepository;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.UUID;

@Controller
@RequestMapping("/dashboard/serenazgo")
@RequiredArgsConstructor
@Slf4j
public class TurnosProgramacionWebController {

    private final ProgramacionServicioService programacionService;
    private final TurnoSerenazgoService turnoService;
    private final SectorPatrullajeService sectorService;
    private final PersonalSerenazgoRepository personalRepository;
    private final UnidadMovilSerenazgoRepository unidadRepository;
    private final EquipamientoSerenazgoRepository equipamientoRepository;
    private final OficinaRepository oficinaRepository;

    // =========================================================================
    // VISTA PRINCIPAL: CALENDARIO, PROGRAMACIÓN Y TURNOS
    // =========================================================================

    @GetMapping("/programacion")
    @PreAuthorize("hasAuthority('SERENAZGO_PROGRAMACION_VER') or hasRole('ROLE_ADMIN') or hasRole('ROLE_SERENO')")
    public String indexProgramacion(@RequestParam(name = "tab", defaultValue = "calendario") String tab,
                                    @RequestParam(name = "q", required = false) String q,
                                    @RequestParam(name = "desde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                    @RequestParam(name = "hasta", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                                    @RequestParam(name = "estado", required = false) EstadoProgramacionServicio estado,
                                    @RequestParam(name = "sectorId", required = false) UUID sectorId,
                                    @RequestParam(name = "turnoId", required = false) UUID turnoId,
                                    @RequestParam(name = "page", defaultValue = "0") int page,
                                    @RequestParam(name = "size", defaultValue = "10") int size,
                                    Model model) {

        Pageable pageable = PageRequest.of(page, size);
        Page<ProgramacionResponse> programaciones = programacionService.buscarProgramaciones(
                q, desde, hasta, estado, sectorId, turnoId, pageable);

        Page<TurnoResponse> turnos = turnoService.buscarTurnos(q, null, PageRequest.of(0, 50));
        DashboardProgramacionResponse kpis = programacionService.obtenerDashboardProgramacion();

        model.addAttribute("activeTab", tab);
        model.addAttribute("programaciones", programaciones);
        model.addAttribute("turnos", turnos);
        model.addAttribute("kpis", kpis);
        model.addAttribute("sectores", sectorService.listarSectoresActivos());
        model.addAttribute("turnosActivos", turnoService.listarTurnosActivos());
        model.addAttribute("estados", EstadoProgramacionServicio.values());
        model.addAttribute("tiposServicio", TipoServicioSerenazgo.values());
        model.addAttribute("q", q);
        model.addAttribute("desde", desde);
        model.addAttribute("hasta", hasta);
        model.addAttribute("estadoSeleccionado", estado);
        model.addAttribute("sectorSeleccionado", sectorId);
        model.addAttribute("turnoSeleccionado", turnoId);
        model.addAttribute("hoy", LocalDate.now());

        return "admin/serenazgo/programacion/index";
    }

    // =========================================================================
    // GESTIÓN DE PROGRAMACIÓN DE SERVICIOS
    // =========================================================================

    @GetMapping("/programacion/nueva")
    @PreAuthorize("hasAuthority('SERENAZGO_PROGRAMACION_CREAR') or hasRole('ROLE_ADMIN')")
    public String nuevaProgramacionForm(Model model) {
        model.addAttribute("programacion", ProgramacionCreateRequest.builder()
                .fechaServicio(LocalDate.now())
                .estado(EstadoProgramacionServicio.PROGRAMADA)
                .build());
        cargarCatalogosFormulario(model);
        return "admin/serenazgo/programacion/programacion-form";
    }

    @PostMapping("/programacion")
    @PreAuthorize("hasAuthority('SERENAZGO_PROGRAMACION_CREAR') or hasRole('ROLE_ADMIN')")
    public String guardarProgramacion(@Valid @ModelAttribute("programacion") ProgramacionCreateRequest request,
                                      BindingResult result,
                                      Principal principal,
                                      RedirectAttributes redirectAttributes,
                                      Model model) {
        if (result.hasErrors()) {
            cargarCatalogosFormulario(model);
            return "admin/serenazgo/programacion/programacion-form";
        }

        try {
            String username = principal != null ? principal.getName() : "admin";
            ProgramacionResponse creada = programacionService.crearProgramacion(request, username);
            redirectAttributes.addFlashAttribute("mensajeExito", "Servicio " + creada.getCodigo() + " programado exitosamente");
            return "redirect:/dashboard/serenazgo/programacion/" + creada.getId();
        } catch (Exception e) {
            log.error("Error al registrar servicio", e);
            model.addAttribute("mensajeError", e.getMessage());
            cargarCatalogosFormulario(model);
            return "admin/serenazgo/programacion/programacion-form";
        }
    }

    @GetMapping("/programacion/{id}")
    @PreAuthorize("hasAuthority('SERENAZGO_PROGRAMACION_VER') or hasRole('ROLE_ADMIN') or hasRole('ROLE_SERENO')")
    public String verDetalleProgramacion(@PathVariable UUID id, Model model) {
        ProgramacionDetailResponse detalle = programacionService.obtenerDetallePorId(id);
        model.addAttribute("servicio", detalle);
        model.addAttribute("sectores", sectorService.listarSectoresActivos());
        model.addAttribute("turnos", turnoService.listarTurnosActivos());
        model.addAttribute("estados", EstadoProgramacionServicio.values());
        return "admin/serenazgo/programacion/programacion-detalle";
    }

    @GetMapping("/programacion/{id}/editar")
    @PreAuthorize("hasAuthority('SERENAZGO_PROGRAMACION_EDITAR') or hasRole('ROLE_ADMIN')")
    public String editarProgramacionForm(@PathVariable UUID id, Model model) {
        ProgramacionDetailResponse detalle = programacionService.obtenerDetallePorId(id);

        ProgramacionUpdateRequest req = ProgramacionUpdateRequest.builder()
                .turnoId(detalle.getTurnoId())
                .fechaServicio(detalle.getFechaServicio())
                .sectorId(detalle.getSectorId())
                .zonaId(detalle.getZonaId())
                .tipoServicio(detalle.getTipoServicio())
                .descripcion(detalle.getDescripcion())
                .observaciones(detalle.getObservaciones())
                .oficinaResponsableId(detalle.getOficinaResponsableId())
                .responsableServicioId(detalle.getResponsableServicioId())
                .build();

        model.addAttribute("servicioId", id);
        model.addAttribute("codigoServicio", detalle.getCodigo());
        model.addAttribute("programacion", req);
        cargarCatalogosFormulario(model);
        return "admin/serenazgo/programacion/programacion-form";
    }

    @PostMapping("/programacion/{id}/editar")
    @PreAuthorize("hasAuthority('SERENAZGO_PROGRAMACION_EDITAR') or hasRole('ROLE_ADMIN')")
    public String actualizarProgramacion(@PathVariable UUID id,
                                         @Valid @ModelAttribute("programacion") ProgramacionUpdateRequest request,
                                         BindingResult result,
                                         Principal principal,
                                         RedirectAttributes redirectAttributes,
                                         Model model) {
        if (result.hasErrors()) {
            model.addAttribute("servicioId", id);
            cargarCatalogosFormulario(model);
            return "admin/serenazgo/programacion/programacion-form";
        }

        try {
            String username = principal != null ? principal.getName() : "admin";
            ProgramacionResponse resp = programacionService.actualizarProgramacion(id, request, username);
            redirectAttributes.addFlashAttribute("mensajeExito", "Servicio " + resp.getCodigo() + " actualizado exitosamente");
            return "redirect:/dashboard/serenazgo/programacion/" + id;
        } catch (Exception e) {
            log.error("Error al actualizar servicio", e);
            model.addAttribute("servicioId", id);
            model.addAttribute("mensajeError", e.getMessage());
            cargarCatalogosFormulario(model);
            return "admin/serenazgo/programacion/programacion-form";
        }
    }

    @PostMapping("/programacion/{id}/confirmar")
    @PreAuthorize("hasAuthority('SERENAZGO_PROGRAMACION_CONFIRMAR') or hasRole('ROLE_ADMIN')")
    public String confirmarServicio(@PathVariable UUID id,
                                    @RequestParam(name = "motivo", required = false) String motivo,
                                    Principal principal,
                                    RedirectAttributes redirectAttributes) {
        try {
            String username = principal != null ? principal.getName() : "admin";
            programacionService.confirmarProgramacion(id, motivo, username);
            redirectAttributes.addFlashAttribute("mensajeExito", "Servicio confirmado y publicado oficialmente");
        } catch (Exception e) {
            log.error("Error al confirmar servicio", e);
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/dashboard/serenazgo/programacion/" + id;
    }

    @PostMapping("/programacion/{id}/reprogramar")
    @PreAuthorize("hasAuthority('SERENAZGO_PROGRAMACION_REPROGRAMAR') or hasRole('ROLE_ADMIN')")
    public String reprogramarServicio(@PathVariable UUID id,
                                      @Valid @ModelAttribute ReprogramarServicioRequest request,
                                      BindingResult result,
                                      Principal principal,
                                      RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("mensajeError", "El motivo y nuevo turno son obligatorios para reprogramar");
            return "redirect:/dashboard/serenazgo/programacion/" + id;
        }

        try {
            String username = principal != null ? principal.getName() : "admin";
            programacionService.reprogramarServicio(id, request, username);
            redirectAttributes.addFlashAttribute("mensajeExito", "Servicio reprogramado exitosamente");
        } catch (Exception e) {
            log.error("Error al reprogramar servicio", e);
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/dashboard/serenazgo/programacion/" + id;
    }

    @PostMapping("/programacion/{id}/cancelar")
    @PreAuthorize("hasAuthority('SERENAZGO_PROGRAMACION_CANCELAR') or hasRole('ROLE_ADMIN')")
    public String cancelarServicio(@PathVariable UUID id,
                                   @Valid @ModelAttribute CancelarServicioRequest request,
                                   BindingResult result,
                                   Principal principal,
                                   RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("mensajeError", "El motivo de cancelación es obligatorio");
            return "redirect:/dashboard/serenazgo/programacion/" + id;
        }

        try {
            String username = principal != null ? principal.getName() : "admin";
            programacionService.cancelarServicio(id, request, username);
            redirectAttributes.addFlashAttribute("mensajeExito", "Servicio cancelado exitosamente y recursos liberados");
        } catch (Exception e) {
            log.error("Error al cancelar servicio", e);
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/dashboard/serenazgo/programacion/" + id;
    }

    @PostMapping("/programacion/{id}/iniciar")
    @PreAuthorize("hasAuthority('SERENAZGO_PROGRAMACION_EDITAR') or hasRole('ROLE_ADMIN')")
    public String iniciarServicio(@PathVariable UUID id,
                                  Principal principal,
                                  RedirectAttributes redirectAttributes) {
        try {
            String username = principal != null ? principal.getName() : "admin";
            programacionService.iniciarServicio(id, username);
            redirectAttributes.addFlashAttribute("mensajeExito", "Servicio iniciado y en curso");
        } catch (Exception e) {
            log.error("Error al iniciar servicio", e);
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/dashboard/serenazgo/programacion/" + id;
    }

    @PostMapping("/programacion/{id}/finalizar")
    @PreAuthorize("hasAuthority('SERENAZGO_PROGRAMACION_EDITAR') or hasRole('ROLE_ADMIN')")
    public String finalizarServicio(@PathVariable UUID id,
                                    @RequestParam(name = "observaciones", required = false) String observaciones,
                                    Principal principal,
                                    RedirectAttributes redirectAttributes) {
        try {
            String username = principal != null ? principal.getName() : "admin";
            programacionService.finalizarServicio(id, observaciones, username);
            redirectAttributes.addFlashAttribute("mensajeExito", "Servicio finalizado exitosamente y recursos liberados");
        } catch (Exception e) {
            log.error("Error al finalizar servicio", e);
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/dashboard/serenazgo/programacion/" + id;
    }

    // =========================================================================
    // GESTIÓN DE TURNOS OPERATIVOS
    // =========================================================================

    @GetMapping("/turnos/nuevo")
    @PreAuthorize("hasAuthority('SERENAZGO_TURNOS_CREAR') or hasRole('ROLE_ADMIN')")
    public String nuevoTurnoForm(Model model) {
        model.addAttribute("turno", TurnoCreateRequest.builder()
                .tipoTurno(TipoTurnoSerenazgo.DIURNO)
                .estado(EstadoTurnoSerenazgo.ACTIVO)
                .build());
        model.addAttribute("tiposTurno", TipoTurnoSerenazgo.values());
        model.addAttribute("oficinas", oficinaRepository.findAll());
        return "admin/serenazgo/programacion/turno-form";
    }

    @PostMapping("/turnos")
    @PreAuthorize("hasAuthority('SERENAZGO_TURNOS_CREAR') or hasRole('ROLE_ADMIN')")
    public String guardarTurno(@Valid @ModelAttribute("turno") TurnoCreateRequest request,
                               BindingResult result,
                               Principal principal,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        if (result.hasErrors()) {
            model.addAttribute("tiposTurno", TipoTurnoSerenazgo.values());
            model.addAttribute("oficinas", oficinaRepository.findAll());
            return "admin/serenazgo/programacion/turno-form";
        }

        try {
            String username = principal != null ? principal.getName() : "admin";
            TurnoResponse creado = turnoService.crearTurno(request, username);
            redirectAttributes.addFlashAttribute("mensajeExito", "Turno " + creado.getNombre() + " registrado exitosamente");
            return "redirect:/dashboard/serenazgo/programacion?tab=turnos";
        } catch (Exception e) {
            log.error("Error al registrar turno", e);
            model.addAttribute("mensajeError", e.getMessage());
            model.addAttribute("tiposTurno", TipoTurnoSerenazgo.values());
            model.addAttribute("oficinas", oficinaRepository.findAll());
            return "admin/serenazgo/programacion/turno-form";
        }
    }

    @GetMapping("/turnos/{id}/editar")
    @PreAuthorize("hasAuthority('SERENAZGO_TURNOS_EDITAR') or hasRole('ROLE_ADMIN')")
    public String editarTurnoForm(@PathVariable UUID id, Model model) {
        TurnoResponse t = turnoService.obtenerTurnoPorId(id);
        TurnoUpdateRequest req = TurnoUpdateRequest.builder()
                .nombre(t.getNombre())
                .descripcion(t.getDescripcion())
                .tipoTurno(t.getTipoTurno())
                .horaInicio(t.getHoraInicio())
                .horaFin(t.getHoraFin())
                .cruzaMedianoche(t.getCruzaMedianoche())
                .oficinaResponsableId(t.getOficinaResponsableId())
                .observaciones(t.getObservaciones())
                .estado(t.getEstado())
                .build();

        model.addAttribute("turnoId", id);
        model.addAttribute("codigoTurno", t.getCodigo());
        model.addAttribute("turno", req);
        model.addAttribute("tiposTurno", TipoTurnoSerenazgo.values());
        model.addAttribute("oficinas", oficinaRepository.findAll());
        return "admin/serenazgo/programacion/turno-form";
    }

    @PostMapping("/turnos/{id}/editar")
    @PreAuthorize("hasAuthority('SERENAZGO_TURNOS_EDITAR') or hasRole('ROLE_ADMIN')")
    public String actualizarTurno(@PathVariable UUID id,
                                  @Valid @ModelAttribute("turno") TurnoUpdateRequest request,
                                  BindingResult result,
                                  Principal principal,
                                  RedirectAttributes redirectAttributes,
                                  Model model) {
        if (result.hasErrors()) {
            model.addAttribute("turnoId", id);
            model.addAttribute("tiposTurno", TipoTurnoSerenazgo.values());
            model.addAttribute("oficinas", oficinaRepository.findAll());
            return "admin/serenazgo/programacion/turno-form";
        }

        try {
            String username = principal != null ? principal.getName() : "admin";
            turnoService.actualizarTurno(id, request, username);
            redirectAttributes.addFlashAttribute("mensajeExito", "Turno actualizado exitosamente");
            return "redirect:/dashboard/serenazgo/programacion?tab=turnos";
        } catch (Exception e) {
            log.error("Error al actualizar turno", e);
            model.addAttribute("turnoId", id);
            model.addAttribute("mensajeError", e.getMessage());
            model.addAttribute("tiposTurno", TipoTurnoSerenazgo.values());
            model.addAttribute("oficinas", oficinaRepository.findAll());
            return "admin/serenazgo/programacion/turno-form";
        }
    }

    @PostMapping("/turnos/{id}/estado")
    @PreAuthorize("hasAuthority('SERENAZGO_TURNOS_EDITAR') or hasRole('ROLE_ADMIN')")
    public String cambiarEstadoTurno(@PathVariable UUID id,
                                     @RequestParam("estado") EstadoTurnoSerenazgo estado,
                                     Principal principal,
                                     RedirectAttributes redirectAttributes) {
        try {
            String username = principal != null ? principal.getName() : "admin";
            turnoService.cambiarEstadoTurno(id, estado, username);
            redirectAttributes.addFlashAttribute("mensajeExito", "Estado de turno actualizado");
        } catch (Exception e) {
            log.error("Error al cambiar estado de turno", e);
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/dashboard/serenazgo/programacion?tab=turnos";
    }

    private void cargarCatalogosFormulario(Model model) {
        model.addAttribute("turnos", turnoService.listarTurnosActivos());
        model.addAttribute("sectores", sectorService.listarSectoresActivos());
        model.addAttribute("tiposServicio", TipoServicioSerenazgo.values());
        model.addAttribute("personalDisponible", personalRepository.findByEstado(EstadoPersonalSerenazgo.ACTIVO));
        model.addAttribute("unidadesDisponibles", unidadRepository.findByEstadoOperativo(EstadoUnidadMovil.DISPONIBLE));
        model.addAttribute("equipamientoDisponible", equipamientoRepository.findByEstado(EstadoEquipamientoSerenazgo.DISPONIBLE));
        model.addAttribute("oficinas", oficinaRepository.findAll());
    }
}
