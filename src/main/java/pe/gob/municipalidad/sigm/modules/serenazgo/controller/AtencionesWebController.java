package pe.gob.municipalidad.sigm.modules.serenazgo.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.IncidenciaSeguridadCiudadana;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.repository.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.AtencionIncidenciaSerenazgoService;

import java.security.Principal;
import java.util.UUID;

@Slf4j
@Controller
@RequestMapping("/dashboard/serenazgo/atenciones")
@RequiredArgsConstructor
public class AtencionesWebController {

    private final AtencionIncidenciaSerenazgoService atencionService;
    private final IncidenciaSeguridadCiudadanaRepository incidenciaRepository;
    private final SectorPatrullajeRepository sectorRepository;
    private final UnidadMovilSerenazgoRepository unidadMovilRepository;
    private final PersonalSerenazgoRepository personalRepository;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('SERENAZGO_ATENCIONES_VER', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_SERENO')")
    public String index(@ModelAttribute("filtro") AtencionFilter filtro,
                        @RequestParam(value = "page", defaultValue = "0") int page,
                        @RequestParam(value = "size", defaultValue = "15") int size,
                        Model model) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(size, 50), Sort.by(Sort.Direction.DESC, "fechaHoraAsignacion"));
        Page<AtencionResponse> atenciones = atencionService.listarAtenciones(filtro, pageable);
        DashboardAtencionesResponse metricas = atencionService.obtenerMetricas();

        model.addAttribute("atenciones", atenciones);
        model.addAttribute("metricas", metricas);
        model.addAttribute("sectores", sectorRepository.findByEstadoOrderByCodigoAsc(EstadoSectorPatrullaje.ACTIVO));
        model.addAttribute("estados", EstadoAtencionIncidencia.values());
        model.addAttribute("prioridades", PrioridadIncidenciaSeguridad.values());
        model.addAttribute("unidades", unidadMovilRepository.findByEstadoOperativo(EstadoUnidadMovil.DISPONIBLE));
        model.addAttribute("filtro", filtro != null ? filtro : new AtencionFilter());
        model.addAttribute("paginaActiva", "serenazgo_atenciones");

        return "admin/serenazgo/atenciones/index";
    }

    @GetMapping("/nueva")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_ATENCIONES_CREAR', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO')")
    public String nuevaAtencion(@RequestParam(value = "incidenciaId", required = false) UUID incidenciaId,
                                Model model) {
        AtencionCrearRequest request = new AtencionCrearRequest();
        if (incidenciaId != null) {
            request.setIncidenciaId(incidenciaId);
            incidenciaRepository.findById(incidenciaId).ifPresent(inc -> {
                model.addAttribute("incidenciaSeleccionada", inc);
                request.setPrioridadOperativa(inc.getPrioridad());
                if (inc.getSector() != null) request.setSectorId(inc.getSector().getId());
                if (inc.getZona() != null) request.setZonaId(inc.getZona().getId());
            });
        }

        model.addAttribute("nuevaAtencion", request);
        model.addAttribute("incidenciasPendientes", incidenciaRepository.findTop200ByEstadoOrderByFechaReporteDesc(EstadoIncidenciaSeguridad.REGISTRADA));
        model.addAttribute("sectores", sectorRepository.findByEstadoOrderByCodigoAsc(EstadoSectorPatrullaje.ACTIVO));
        model.addAttribute("personalDisponible", atencionService.listarPersonalDisponible());
        model.addAttribute("unidadesDisponibles", atencionService.listarUnidadesDisponibles());
        model.addAttribute("equiposDisponibles", atencionService.listarEquipamientoDisponible());
        model.addAttribute("prioridades", PrioridadIncidenciaSeguridad.values());
        model.addAttribute("canales", CanalComunicacionDespacho.values());
        model.addAttribute("paginaActiva", "serenazgo_atenciones");

        return "admin/serenazgo/atenciones/nueva";
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('SERENAZGO_ATENCIONES_CREAR', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO')")
    public String registrarAtencion(@Valid @ModelAttribute("nuevaAtencion") AtencionCrearRequest request,
                                    BindingResult result,
                                    Principal principal,
                                    RedirectAttributes redirectAttributes,
                                    Model model) {
        if (result.hasErrors()) {
            model.addAttribute("incidenciasPendientes", incidenciaRepository.findTop200ByEstadoOrderByFechaReporteDesc(EstadoIncidenciaSeguridad.REGISTRADA));
            model.addAttribute("sectores", sectorRepository.findByEstadoOrderByCodigoAsc(EstadoSectorPatrullaje.ACTIVO));
            model.addAttribute("personalDisponible", atencionService.listarPersonalDisponible());
            model.addAttribute("unidadesDisponibles", atencionService.listarUnidadesDisponibles());
            model.addAttribute("equiposDisponibles", atencionService.listarEquipamientoDisponible());
            model.addAttribute("prioridades", PrioridadIncidenciaSeguridad.values());
            model.addAttribute("canales", CanalComunicacionDespacho.values());
            return "admin/serenazgo/atenciones/nueva";
        }

        try {
            AtencionDetailResponse atn = atencionService.crearAtencion(request, principal != null ? principal.getName() : "SISTEMA");
            redirectAttributes.addFlashAttribute("mensajeExito", "Orden de atención " + atn.getCodigo() + " generada exitosamente.");
            return "redirect:/dashboard/serenazgo/atenciones/" + atn.getId();
        } catch (Exception e) {
            log.error("Error al crear atención", e);
            // La aplicación es stateless: un flash attribute se perdería en el redirect, por eso se
            // vuelve a mostrar el formulario con el mensaje de error.
            model.addAttribute("mensajeError", e.getMessage() != null ? e.getMessage() : "No se pudo generar la orden de atención.");
            if (request.getIncidenciaId() != null) {
                incidenciaRepository.findById(request.getIncidenciaId())
                        .ifPresent(inc -> model.addAttribute("incidenciaSeleccionada", inc));
            }
            model.addAttribute("incidenciasPendientes", incidenciaRepository.findTop200ByEstadoOrderByFechaReporteDesc(EstadoIncidenciaSeguridad.REGISTRADA));
            model.addAttribute("sectores", sectorRepository.findByEstadoOrderByCodigoAsc(EstadoSectorPatrullaje.ACTIVO));
            model.addAttribute("personalDisponible", atencionService.listarPersonalDisponible());
            model.addAttribute("unidadesDisponibles", atencionService.listarUnidadesDisponibles());
            model.addAttribute("equiposDisponibles", atencionService.listarEquipamientoDisponible());
            model.addAttribute("prioridades", PrioridadIncidenciaSeguridad.values());
            model.addAttribute("canales", CanalComunicacionDespacho.values());
            return "admin/serenazgo/atenciones/nueva";
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_ATENCIONES_VER', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_SERENO')")
    public String verDetalle(@PathVariable("id") UUID id, Model model) {
        AtencionDetailResponse atencion = atencionService.obtenerDetallePorId(id);

        model.addAttribute("atencion", atencion);
        model.addAttribute("personalDisponible", atencionService.listarPersonalDisponible());
        model.addAttribute("unidadesDisponibles", atencionService.listarUnidadesDisponibles());
        model.addAttribute("equiposDisponibles", atencionService.listarEquipamientoDisponible());
        model.addAttribute("canales", CanalComunicacionDespacho.values());
        model.addAttribute("entidadesEscalamiento", EntidadEscalamiento.values());
        model.addAttribute("resultadosAtencion", ResultadoAtencionIncidencia.values());
        model.addAttribute("tiposNovedad", TipoEventoAtencion.values());
        model.addAttribute("paginaActiva", "serenazgo_atenciones");

        return "admin/serenazgo/atenciones/atencion-detalle";
    }

    @PostMapping("/{id}/asignar")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_ATENCIONES_ASIGNAR', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO')")
    public String asignarRecursos(@PathVariable("id") UUID id,
                                  @ModelAttribute AtencionAsignarRecursosRequest request,
                                  Principal principal,
                                  RedirectAttributes redirectAttributes) {
        try {
            atencionService.asignarRecursos(id, request, principal != null ? principal.getName() : "SISTEMA");
            redirectAttributes.addFlashAttribute("mensajeExito", "Recursos asignados a la atención correctamente.");
        } catch (Exception e) {
            log.error("Error al asignar recursos", e);
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/dashboard/serenazgo/atenciones/" + id;
    }

    @PostMapping("/{id}/despachar")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_ATENCIONES_DESPACHAR', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO')")
    public String despachar(@PathVariable("id") UUID id,
                            @ModelAttribute AtencionDespacharRequest request,
                            Principal principal,
                            RedirectAttributes redirectAttributes) {
        try {
            atencionService.despachar(id, request, principal != null ? principal.getName() : "SISTEMA");
            redirectAttributes.addFlashAttribute("mensajeExito", "Despacho emitido satisfactoriamente a la unidad de Serenazgo.");
        } catch (Exception e) {
            log.error("Error al despachar atención", e);
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/dashboard/serenazgo/atenciones/" + id;
    }

    @PostMapping("/{id}/aceptar")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_ATENCIONES_ACEPTAR', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_SERENO')")
    public String aceptar(@PathVariable("id") UUID id,
                          Principal principal,
                          RedirectAttributes redirectAttributes) {
        try {
            AtencionAceptarRechazarRequest req = AtencionAceptarRechazarRequest.builder().aceptar(true).build();
            atencionService.responderDespacho(id, req, principal != null ? principal.getName() : "SISTEMA");
            redirectAttributes.addFlashAttribute("mensajeExito", "Recepción y aceptación de la asignación confirmada por la unidad.");
        } catch (Exception e) {
            log.error("Error al aceptar asignación", e);
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/dashboard/serenazgo/atenciones/" + id;
    }

    @PostMapping("/{id}/rechazar")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_ATENCIONES_ACEPTAR', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_SERENO')")
    public String rechazar(@PathVariable("id") UUID id,
                           @RequestParam("motivoRechazo") String motivoRechazo,
                           Principal principal,
                           RedirectAttributes redirectAttributes) {
        try {
            AtencionAceptarRechazarRequest req = AtencionAceptarRechazarRequest.builder().aceptar(false).motivoRechazo(motivoRechazo).build();
            atencionService.responderDespacho(id, req, principal != null ? principal.getName() : "SISTEMA");
            redirectAttributes.addFlashAttribute("mensajeExito", "Rechazo justificado del despacho registrado para conocimiento de la Central.");
        } catch (Exception e) {
            log.error("Error al rechazar asignación", e);
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/dashboard/serenazgo/atenciones/" + id;
    }

    @PostMapping("/{id}/iniciar")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_ATENCIONES_INICIAR', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_SERENO')")
    public String iniciar(@PathVariable("id") UUID id,
                          @ModelAttribute AtencionIniciarRequest request,
                          Principal principal,
                          RedirectAttributes redirectAttributes) {
        try {
            atencionService.iniciarAtencion(id, request, principal != null ? principal.getName() : "SISTEMA");
            redirectAttributes.addFlashAttribute("mensajeExito", "Llegada al punto e inicio de atención registrado.");
        } catch (Exception e) {
            log.error("Error al iniciar atención", e);
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/dashboard/serenazgo/atenciones/" + id;
    }

    @PostMapping("/{id}/novedades")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_ATENCIONES_REGISTRAR_NOVEDAD', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_SERENO')")
    public String registrarNovedad(@PathVariable("id") UUID id,
                                   @Valid @ModelAttribute AtencionNovedadRequest request,
                                   Principal principal,
                                   RedirectAttributes redirectAttributes) {
        try {
            atencionService.registrarNovedad(id, request, principal != null ? principal.getName() : "SISTEMA");
            redirectAttributes.addFlashAttribute("mensajeExito", "Novedad operativa añadida a la bitácora.");
        } catch (Exception e) {
            log.error("Error al registrar novedad", e);
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/dashboard/serenazgo/atenciones/" + id;
    }

    @PostMapping("/{id}/pausar")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_ATENCIONES_REGISTRAR_NOVEDAD', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_SERENO')")
    public String pausar(@PathVariable("id") UUID id,
                         @RequestParam("motivo") String motivo,
                         Principal principal,
                         RedirectAttributes redirectAttributes) {
        try {
            AtencionPausarReanudarRequest req = AtencionPausarReanudarRequest.builder().motivo(motivo).build();
            atencionService.pausarEnEspera(id, req, principal != null ? principal.getName() : "SISTEMA");
            redirectAttributes.addFlashAttribute("mensajeExito", "Atención colocada en estado de espera.");
        } catch (Exception e) {
            log.error("Error al pausar atención", e);
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/dashboard/serenazgo/atenciones/" + id;
    }

    @PostMapping("/{id}/reanudar")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_ATENCIONES_REGISTRAR_NOVEDAD', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_SERENO')")
    public String reanudar(@PathVariable("id") UUID id,
                           Principal principal,
                           RedirectAttributes redirectAttributes) {
        try {
            atencionService.reanudarAtencion(id, new AtencionPausarReanudarRequest(), principal != null ? principal.getName() : "SISTEMA");
            redirectAttributes.addFlashAttribute("mensajeExito", "Atención reanudada en sitio.");
        } catch (Exception e) {
            log.error("Error al reanudar atención", e);
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/dashboard/serenazgo/atenciones/" + id;
    }

    @PostMapping("/{id}/reasignar")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_ATENCIONES_REASIGNAR', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO')")
    public String reasignar(@PathVariable("id") UUID id,
                            @Valid @ModelAttribute AtencionReasignarRequest request,
                            Principal principal,
                            RedirectAttributes redirectAttributes) {
        try {
            atencionService.reasignar(id, request, principal != null ? principal.getName() : "SISTEMA");
            redirectAttributes.addFlashAttribute("mensajeExito", "Reasignación de recursos registrada exitosamente.");
        } catch (Exception e) {
            log.error("Error al reasignar recursos", e);
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/dashboard/serenazgo/atenciones/" + id;
    }

    @PostMapping("/{id}/escalar")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_ATENCIONES_ESCALAR', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO')")
    public String escalar(@PathVariable("id") UUID id,
                          @Valid @ModelAttribute AtencionEscalarRequest request,
                          Principal principal,
                          RedirectAttributes redirectAttributes) {
        try {
            atencionService.escalar(id, request, principal != null ? principal.getName() : "SISTEMA");
            redirectAttributes.addFlashAttribute("mensajeExito", "Escalamiento institucional registrado en la bitácora.");
        } catch (Exception e) {
            log.error("Error al registrar escalamiento", e);
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/dashboard/serenazgo/atenciones/" + id;
    }

    @PostMapping("/{id}/finalizar")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_ATENCIONES_FINALIZAR', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO')")
    public String finalizar(@PathVariable("id") UUID id,
                            @Valid @ModelAttribute AtencionFinalizarRequest request,
                            Principal principal,
                            RedirectAttributes redirectAttributes) {
        try {
            atencionService.finalizarAtencion(id, request, principal != null ? principal.getName() : "SISTEMA");
            redirectAttributes.addFlashAttribute("mensajeExito", "Atención operativa finalizada con éxito. Recursos liberados.");
        } catch (Exception e) {
            log.error("Error al finalizar atención", e);
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/dashboard/serenazgo/atenciones/" + id;
    }

    @PostMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_ATENCIONES_CANCELAR', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO')")
    public String cancelar(@PathVariable("id") UUID id,
                           @Valid @ModelAttribute AtencionCancelarRequest request,
                           Principal principal,
                           RedirectAttributes redirectAttributes) {
        try {
            atencionService.cancelarAtencion(id, request, principal != null ? principal.getName() : "SISTEMA");
            redirectAttributes.addFlashAttribute("mensajeExito", "Orden de atención cancelada debidamente.");
        } catch (Exception e) {
            log.error("Error al cancelar atención", e);
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/dashboard/serenazgo/atenciones/" + id;
    }

    @GetMapping("/exportar/pdf")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_ATENCIONES_EXPORTAR', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO')")
    public ResponseEntity<byte[]> exportarPdf(@ModelAttribute AtencionFilter filtro) {
        byte[] pdf = atencionService.exportarPdf(filtro);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"atenciones_serenazgo.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/exportar/excel")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_ATENCIONES_EXPORTAR', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO')")
    public ResponseEntity<byte[]> exportarExcel(@ModelAttribute AtencionFilter filtro) {
        byte[] excel = atencionService.exportarExcel(filtro);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"atenciones_serenazgo.xlsx\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel);
    }
}
