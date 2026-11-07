package pe.gob.municipalidad.sigm.modules.serenazgo.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.repository.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.AtencionIncidenciaSerenazgoService;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.IntervencionSerenazgoService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.UUID;

@Slf4j
@Controller
@RequestMapping("/dashboard/serenazgo/intervenciones")
@RequiredArgsConstructor
public class IntervencionesWebController {

    private final IntervencionSerenazgoService intervencionService;
    private final IncidenciaSeguridadCiudadanaRepository incidenciaRepository;
    private final AtencionIncidenciaSerenazgoRepository atencionRepository;
    private final SectorPatrullajeRepository sectorRepository;
    private final ZonaPatrullajeRepository zonaRepository;
    private final PersonalSerenazgoRepository personalRepository;
    private final UnidadMovilSerenazgoRepository unidadMovilRepository;
    private final EquipamientoSerenazgoRepository equipamientoRepository;
    private final AtencionIncidenciaSerenazgoService atencionService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('SERENAZGO_INTERVENCIONES_VER', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_OPERADOR_CENTRAL', 'ROLE_SERENO')")
    public String listarIntervenciones(@ModelAttribute("filtro") IntervencionFilter filtro,
                                       @PageableDefault(size = 10) Pageable pageable,
                                       Model model) {
        log.debug("Acceso a bandeja de intervenciones de Serenazgo");

        IntervencionFilter filtroSeguro = (filtro != null) ? filtro : new IntervencionFilter();
        Page<IntervencionResponse> page = intervencionService.listarIntervenciones(filtroSeguro, pageable);
        DashboardIntervencionesResponse dashboard = intervencionService.obtenerDashboard();

        model.addAttribute("intervenciones", page);
        model.addAttribute("kpis", dashboard);
        model.addAttribute("filtro", filtroSeguro);
        model.addAttribute("tipos", TipoIntervencionSerenazgo.values());
        model.addAttribute("estados", EstadoIntervencionSerenazgo.values());
        model.addAttribute("sectores", sectorRepository.findByEstadoOrderByCodigoAsc(EstadoSectorPatrullaje.ACTIVO));
        model.addAttribute("paginaActiva", "serenazgo_intervenciones");
        model.addAttribute("submenuActivo", "intervenciones");

        return "admin/serenazgo/intervenciones/index";
    }

    @GetMapping("/nueva")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_INTERVENCIONES_CREAR', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_OPERADOR_CENTRAL', 'ROLE_SERENO')")
    public String mostrarFormularioNueva(@RequestParam(value = "incidenciaId", required = false) UUID incidenciaId,
                                         @RequestParam(value = "atencionId", required = false) UUID atencionId,
                                         Model model) {
        IntervencionCrearRequest req = new IntervencionCrearRequest();
        if (incidenciaId != null) {
            req.setIncidenciaId(incidenciaId);
            incidenciaRepository.findById(incidenciaId).ifPresent(inc -> {
                model.addAttribute("incidenciaPreseleccionada", inc);
                if (inc.getSector() != null) req.setSectorId(inc.getSector().getId());
                if (inc.getZona() != null) req.setZonaId(inc.getZona().getId());
                req.setUbicacionDescripcion(inc.getUbicacionReferencia());
            });
        }
        if (atencionId != null) {
            req.setAtencionId(atencionId);
            atencionRepository.findById(atencionId).ifPresent(atn -> {
                model.addAttribute("atencionPreseleccionada", atn);
                if (req.getIncidenciaId() == null) req.setIncidenciaId(atn.getIncidencia().getId());
                if (atn.getResponsableOperativo() != null) req.setResponsableId(atn.getResponsableOperativo().getId());
                if (atn.getUnidadMovil() != null) req.setUnidadMovilId(atn.getUnidadMovil().getId());
            });
        }

        cargarModelosFormulario(model);
        model.addAttribute("nuevaIntervencion", req);
        model.addAttribute("paginaActiva", "serenazgo_intervenciones");
        model.addAttribute("submenuActivo", "intervenciones");

        return "admin/serenazgo/intervenciones/nueva";
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('SERENAZGO_INTERVENCIONES_CREAR', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_OPERADOR_CENTRAL', 'ROLE_SERENO')")
    public String registrarIntervencion(@Valid @ModelAttribute("nuevaIntervencion") IntervencionCrearRequest request,
                                        BindingResult result,
                                        Principal principal,
                                        RedirectAttributes redirectAttributes,
                                        Model model) {
        if (result.hasErrors()) {
            cargarModelosFormulario(model);
            model.addAttribute("paginaActiva", "serenazgo_intervenciones");
            model.addAttribute("submenuActivo", "intervenciones");
            return "admin/serenazgo/intervenciones/nueva";
        }

        try {
            String usuario = principal != null ? principal.getName() : "SISTEMA";
            IntervencionDetailResponse det = intervencionService.crearIntervencion(request, usuario);
            redirectAttributes.addFlashAttribute("mensajeExito", "Intervención " + det.getCodigo() + " registrada correctamente.");
            return "redirect:/dashboard/serenazgo/intervenciones/" + det.getId();
        } catch (Exception e) {
            log.error("Error al registrar intervención", e);
            cargarModelosFormulario(model);
            model.addAttribute("mensajeError", e.getMessage());
            model.addAttribute("paginaActiva", "serenazgo_intervenciones");
            model.addAttribute("submenuActivo", "intervenciones");
            return "admin/serenazgo/intervenciones/nueva";
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_INTERVENCIONES_VER', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_OPERADOR_CENTRAL', 'ROLE_SERENO')")
    public String verDetalleIntervencion(@PathVariable("id") UUID id,
                                         Principal principal,
                                         Model model) {
        String usuario = principal != null ? principal.getName() : "SISTEMA";
        IntervencionDetailResponse det = intervencionService.obtenerPorId(id, usuario);

        model.addAttribute("intervencion", det);
        model.addAttribute("tiposActuacion", TipoActuacionIntervencion.values());
        model.addAttribute("entidadesComunicacion", EntidadComunicacion.values());
        model.addAttribute("canalesComunicacion", CanalComunicacionDespacho.values());
        model.addAttribute("estadosComunicacion", EstadoComunicacionIntervencion.values());
        model.addAttribute("tiposDocumento", TipoDocumentoIntervencion.values());
        model.addAttribute("personalDisponible", personalRepository.findByEstado(EstadoPersonalSerenazgo.ACTIVO));
        model.addAttribute("equiposDisponibles", equipamientoRepository.findAll());

        model.addAttribute("paginaActiva", "serenazgo_intervenciones");
        model.addAttribute("submenuActivo", "intervenciones");

        return "admin/serenazgo/intervenciones/detalle";
    }

    @GetMapping("/{id}/editar")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_INTERVENCIONES_EDITAR', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_OPERADOR_CENTRAL', 'ROLE_SERENO')")
    public String mostrarFormularioEditar(@PathVariable("id") UUID id,
                                          Principal principal,
                                          Model model) {
        String usuario = principal != null ? principal.getName() : "SISTEMA";
        IntervencionDetailResponse det = intervencionService.obtenerPorId(id, usuario);

        if (!det.isEditable()) {
            return "redirect:/dashboard/serenazgo/intervenciones/" + id + "?error=no_editable";
        }

        IntervencionEditarRequest req = IntervencionEditarRequest.builder()
                .atencionId(det.getAtencionId())
                .tipoIntervencion(det.getTipoIntervencion())
                .fechaInicio(det.getFechaInicio() != null ? det.getFechaInicio().toLocalDateTime() : null)
                .fechaFin(det.getFechaFin() != null ? det.getFechaFin().toLocalDateTime() : null)
                .responsableId(det.getResponsableId())
                .unidadMovilId(det.getUnidadMovilId())
                .sectorId(det.getSectorId())
                .zonaId(det.getZonaId())
                .ubicacionDescripcion(det.getUbicacionDescripcion())
                .latitud(det.getLatitud())
                .longitud(det.getLongitud())
                .descripcionActuacion(det.getDescripcionActuacion())
                .resultado(det.getResultado())
                .nivelConfidencialidad(det.getNivelConfidencialidad())
                .observaciones(det.getObservaciones())
                .build();

        cargarModelosFormulario(model);
        model.addAttribute("editarIntervencion", req);
        model.addAttribute("intervencion", det);
        model.addAttribute("paginaActiva", "serenazgo_intervenciones");
        model.addAttribute("submenuActivo", "intervenciones");

        return "admin/serenazgo/intervenciones/editar";
    }

    @PostMapping("/{id}/editar")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_INTERVENCIONES_EDITAR', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_OPERADOR_CENTRAL', 'ROLE_SERENO')")
    public String guardarEdicion(@PathVariable("id") UUID id,
                                 @Valid @ModelAttribute("editarIntervencion") IntervencionEditarRequest request,
                                 BindingResult result,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        if (result.hasErrors()) {
            cargarModelosFormulario(model);
            model.addAttribute("intervencion", intervencionService.obtenerPorId(id, principal != null ? principal.getName() : "SISTEMA"));
            model.addAttribute("paginaActiva", "serenazgo_intervenciones");
            model.addAttribute("submenuActivo", "intervenciones");
            return "admin/serenazgo/intervenciones/editar";
        }

        try {
            String usuario = principal != null ? principal.getName() : "SISTEMA";
            intervencionService.actualizarIntervencion(id, request, usuario);
            redirectAttributes.addFlashAttribute("mensajeExito", "Intervención actualizada exitosamente.");
            return "redirect:/dashboard/serenazgo/intervenciones/" + id;
        } catch (Exception e) {
            log.error("Error al actualizar intervención", e);
            cargarModelosFormulario(model);
            model.addAttribute("intervencion", intervencionService.obtenerPorId(id, principal != null ? principal.getName() : "SISTEMA"));
            model.addAttribute("mensajeError", e.getMessage());
            model.addAttribute("paginaActiva", "serenazgo_intervenciones");
            model.addAttribute("submenuActivo", "intervenciones");
            return "admin/serenazgo/intervenciones/editar";
        }
    }

    @PostMapping("/{id}/participantes")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_INTERVENCIONES_GESTIONAR_PARTICIPANTES', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_OPERADOR_CENTRAL', 'ROLE_SERENO')")
    public String agregarParticipante(@PathVariable("id") UUID id,
                                      @Valid @ModelAttribute IntervencionParticipanteRequest request,
                                      Principal principal,
                                      RedirectAttributes redirectAttributes) {
        try {
            intervencionService.agregarParticipante(id, request, principal != null ? principal.getName() : "SISTEMA");
            redirectAttributes.addFlashAttribute("mensajeExito", "Personal participante incorporado debidamente.");
        } catch (Exception e) {
            log.error("Error al agregar participante", e);
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/dashboard/serenazgo/intervenciones/" + id;
    }

    @PostMapping("/{id}/participantes/{personalId}/remover")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_INTERVENCIONES_GESTIONAR_PARTICIPANTES', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_OPERADOR_CENTRAL', 'ROLE_SERENO')")
    public String removerParticipante(@PathVariable("id") UUID id,
                                      @PathVariable("personalId") UUID personalId,
                                      Principal principal,
                                      RedirectAttributes redirectAttributes) {
        try {
            intervencionService.removerParticipante(id, personalId, principal != null ? principal.getName() : "SISTEMA");
            redirectAttributes.addFlashAttribute("mensajeExito", "Participante desvinculado de la intervención.");
        } catch (Exception e) {
            log.error("Error al remover participante", e);
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/dashboard/serenazgo/intervenciones/" + id;
    }

    @PostMapping("/{id}/actuaciones")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_INTERVENCIONES_REGISTRAR_ACTUACION', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_OPERADOR_CENTRAL', 'ROLE_SERENO')")
    public String registrarActuacion(@PathVariable("id") UUID id,
                                     @Valid @ModelAttribute IntervencionActuacionRequest request,
                                     Principal principal,
                                     RedirectAttributes redirectAttributes) {
        try {
            intervencionService.registrarActuacion(id, request, principal != null ? principal.getName() : "SISTEMA");
            redirectAttributes.addFlashAttribute("mensajeExito", "Actuación operativa registrada en la bitácora.");
        } catch (Exception e) {
            log.error("Error al registrar actuación", e);
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/dashboard/serenazgo/intervenciones/" + id;
    }

    @PostMapping("/{id}/comunicaciones")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_INTERVENCIONES_REGISTRAR_ACTUACION', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_OPERADOR_CENTRAL', 'ROLE_SERENO')")
    public String registrarComunicacion(@PathVariable("id") UUID id,
                                        @Valid @ModelAttribute IntervencionComunicacionRequest request,
                                        Principal principal,
                                        RedirectAttributes redirectAttributes) {
        try {
            intervencionService.registrarComunicacion(id, request, principal != null ? principal.getName() : "SISTEMA");
            redirectAttributes.addFlashAttribute("mensajeExito", "Comunicación institucional registrada exitosamente.");
        } catch (Exception e) {
            log.error("Error al registrar comunicación", e);
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/dashboard/serenazgo/intervenciones/" + id;
    }

    @PostMapping("/{id}/comunicaciones/{comunicacionId}/actualizar")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_INTERVENCIONES_REGISTRAR_ACTUACION', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO')")
    public String actualizarComunicacion(@PathVariable("id") UUID id,
                                         @PathVariable("comunicacionId") UUID comunicacionId,
                                         @RequestParam("estado") EstadoComunicacionIntervencion estado,
                                         @RequestParam(value = "confirmadoPor", required = false) String confirmadoPor,
                                         @RequestParam(value = "resultado", required = false) String resultado,
                                         Principal principal,
                                         RedirectAttributes redirectAttributes) {
        try {
            intervencionService.actualizarEstadoComunicacion(comunicacionId, estado, confirmadoPor, resultado,
                    principal != null ? principal.getName() : "SISTEMA");
            redirectAttributes.addFlashAttribute("mensajeExito", "Estado de comunicación actualizado.");
        } catch (Exception e) {
            log.error("Error al actualizar comunicación", e);
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/dashboard/serenazgo/intervenciones/" + id;
    }

    @PostMapping("/{id}/equipamiento")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_INTERVENCIONES_EDITAR', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_SERENO')")
    public String asociarEquipamiento(@PathVariable("id") UUID id,
                                      @RequestParam("equipamientoId") UUID equipamientoId,
                                      @RequestParam(value = "cantidad", defaultValue = "1") Integer cantidad,
                                      @RequestParam(value = "estadoUso", defaultValue = "UTILIZADO_CONFORME") String estadoUso,
                                      @RequestParam(value = "observaciones", required = false) String observaciones,
                                      Principal principal,
                                      RedirectAttributes redirectAttributes) {
        try {
            intervencionService.asociarEquipamiento(id, equipamientoId, cantidad, estadoUso, observaciones,
                    principal != null ? principal.getName() : "SISTEMA");
            redirectAttributes.addFlashAttribute("mensajeExito", "Equipamiento asignado a la intervención.");
        } catch (Exception e) {
            log.error("Error al asociar equipamiento", e);
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/dashboard/serenazgo/intervenciones/" + id;
    }

    @PostMapping("/{id}/documentos")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_INTERVENCIONES_ADJUNTAR', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_OPERADOR_CENTRAL', 'ROLE_SERENO')")
    public String adjuntarDocumento(@PathVariable("id") UUID id,
                                    @RequestParam("tipoDocumento") TipoDocumentoIntervencion tipoDocumento,
                                    @RequestParam("archivo") MultipartFile archivo,
                                    @RequestParam(value = "descripcion", required = false) String descripcion,
                                    @RequestParam(value = "visibleCiudadano", defaultValue = "false") Boolean visibleCiudadano,
                                    Principal principal,
                                    RedirectAttributes redirectAttributes) {
        if (archivo.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensajeError", "Debe seleccionar un archivo válido para adjuntar.");
            return "redirect:/dashboard/serenazgo/intervenciones/" + id;
        }

        try {
            String uploadDir = "uploads/serenazgo/intervenciones/" + id;
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String filename = System.currentTimeMillis() + "_" + archivo.getOriginalFilename();
            Path filePath = uploadPath.resolve(filename);
            Files.copy(archivo.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            intervencionService.adjuntarDocumento(id, tipoDocumento, archivo.getOriginalFilename(),
                    filePath.toString(), archivo.getContentType(), archivo.getSize(), descripcion, visibleCiudadano,
                    principal != null ? principal.getName() : "SISTEMA");

            redirectAttributes.addFlashAttribute("mensajeExito", "Documento adjuntado exitosamente.");
        } catch (Exception e) {
            log.error("Error al adjuntar archivo", e);
            redirectAttributes.addFlashAttribute("mensajeError", "Error al procesar el archivo: " + e.getMessage());
        }

        return "redirect:/dashboard/serenazgo/intervenciones/" + id;
    }

    @PostMapping("/{id}/enviar-revision")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_INTERVENCIONES_EDITAR', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_OPERADOR_CENTRAL', 'ROLE_SERENO')")
    public String enviarARevision(@PathVariable("id") UUID id,
                                  Principal principal,
                                  RedirectAttributes redirectAttributes) {
        try {
            intervencionService.enviarARevision(id, principal != null ? principal.getName() : "SISTEMA");
            redirectAttributes.addFlashAttribute("mensajeExito", "Intervención enviada para revisión por el supervisor.");
        } catch (Exception e) {
            log.error("Error al enviar a revisión", e);
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/dashboard/serenazgo/intervenciones/" + id;
    }

    @PostMapping("/{id}/observar")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_INTERVENCIONES_OBSERVAR', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO')")
    public String observar(@PathVariable("id") UUID id,
                           @Valid @ModelAttribute IntervencionObservarRequest request,
                           Principal principal,
                           RedirectAttributes redirectAttributes) {
        try {
            intervencionService.observarIntervencion(id, request, principal != null ? principal.getName() : "SISTEMA");
            redirectAttributes.addFlashAttribute("mensajeExito", "Intervención marcada como observada para subsanación.");
        } catch (Exception e) {
            log.error("Error al observar intervención", e);
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/dashboard/serenazgo/intervenciones/" + id;
    }

    @PostMapping("/{id}/subsanar")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_INTERVENCIONES_EDITAR', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_OPERADOR_CENTRAL', 'ROLE_SERENO')")
    public String subsanar(@PathVariable("id") UUID id,
                           @RequestParam("motivoSubsanacion") String motivoSubsanacion,
                           Principal principal,
                           RedirectAttributes redirectAttributes) {
        try {
            intervencionService.subsanarIntervencion(id, motivoSubsanacion, principal != null ? principal.getName() : "SISTEMA");
            redirectAttributes.addFlashAttribute("mensajeExito", "Subsanación registrada y devuelta a revisión.");
        } catch (Exception e) {
            log.error("Error al subsanar intervención", e);
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/dashboard/serenazgo/intervenciones/" + id;
    }

    @PostMapping("/{id}/validar")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_INTERVENCIONES_VALIDAR', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO')")
    public String validar(@PathVariable("id") UUID id,
                          @ModelAttribute IntervencionValidarRequest request,
                          Principal principal,
                          RedirectAttributes redirectAttributes) {
        try {
            intervencionService.validarIntervencion(id, request, principal != null ? principal.getName() : "SISTEMA");
            redirectAttributes.addFlashAttribute("mensajeExito", "Intervención validada administrativamente.");
        } catch (Exception e) {
            log.error("Error al validar intervención", e);
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/dashboard/serenazgo/intervenciones/" + id;
    }

    @PostMapping("/{id}/archivar")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_INTERVENCIONES_ARCHIVAR', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO')")
    public String archivar(@PathVariable("id") UUID id,
                           @Valid @ModelAttribute IntervencionArchivarRequest request,
                           Principal principal,
                           RedirectAttributes redirectAttributes) {
        try {
            intervencionService.archivarIntervencion(id, request, principal != null ? principal.getName() : "SISTEMA");
            redirectAttributes.addFlashAttribute("mensajeExito", "Expediente archivado conforme al procedimiento.");
        } catch (Exception e) {
            log.error("Error al archivar intervención", e);
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/dashboard/serenazgo/intervenciones/" + id;
    }

    @GetMapping("/exportar/pdf")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_INTERVENCIONES_EXPORTAR', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO')")
    public ResponseEntity<byte[]> exportarPdf(@ModelAttribute IntervencionFilter filtro) {
        byte[] pdf = intervencionService.exportarPdf(filtro);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"intervenciones_serenazgo.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/exportar/excel")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_INTERVENCIONES_EXPORTAR', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO')")
    public ResponseEntity<byte[]> exportarExcel(@ModelAttribute IntervencionFilter filtro) {
        byte[] excel = intervencionService.exportarExcel(filtro);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"intervenciones_serenazgo.xlsx\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel);
    }

    @GetMapping("/{id}/ficha-pdf")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_INTERVENCIONES_EXPORTAR', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO')")
    public ResponseEntity<byte[]> descargarFichaPdf(@PathVariable("id") UUID id) {
        byte[] pdf = intervencionService.generarFichaPdf(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"ficha_intervencion.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    private void cargarModelosFormulario(Model model) {
        model.addAttribute("incidencias", incidenciaRepository.findAll());
        model.addAttribute("atenciones", atencionRepository.findAll());
        model.addAttribute("tipos", TipoIntervencionSerenazgo.values());
        model.addAttribute("resultados", ResultadoIntervencionSerenazgo.values());
        model.addAttribute("confidencialidades", NivelConfidencialidad.values());
        model.addAttribute("sectores", sectorRepository.findByEstadoOrderByCodigoAsc(EstadoSectorPatrullaje.ACTIVO));
        model.addAttribute("zonas", zonaRepository.findAll());
        model.addAttribute("personalDisponible", personalRepository.findByEstado(EstadoPersonalSerenazgo.ACTIVO));
        model.addAttribute("unidadesDisponibles", unidadMovilRepository.findAll());
        model.addAttribute("equiposDisponibles", equipamientoRepository.findAll());
    }
}
