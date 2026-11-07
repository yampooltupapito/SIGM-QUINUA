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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.repository.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.PatrullajeSerenazgoService;

import java.security.Principal;
import java.util.UUID;

@Slf4j
@Controller
@RequestMapping("/dashboard/serenazgo/patrullajes")
@RequiredArgsConstructor
public class PatrullajesWebController {

    private final PatrullajeSerenazgoService patrullajeService;
    private final SectorPatrullajeRepository sectorRepository;
    private final PersonalSerenazgoRepository personalRepository;
    private final UnidadMovilSerenazgoRepository unidadMovilRepository;
    private final EquipamientoSerenazgoRepository equipamientoRepository;
    private final IncidenciaSeguridadCiudadanaRepository incidenciaRepository;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('SERENAZGO_PATRULLAJES_VER', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_OPERADOR_CENTRAL', 'ROLE_SERENO')")
    public String listarPatrullajes(@ModelAttribute("filtro") PatrullajeFilter filtro,
                                    @PageableDefault(size = 10) Pageable pageable,
                                    Model model) {
        log.debug("Acceso al listado de patrullajes y recorridos de Serenazgo");

        PatrullajeFilter filtroSeguro = (filtro != null) ? filtro : new PatrullajeFilter();
        Page<PatrullajeResponse> page = patrullajeService.listarPatrullajes(filtroSeguro, pageable);
        DashboardPatrullajesResponse kpis = patrullajeService.obtenerDashboardKpis();

        model.addAttribute("patrullajes", page);
        model.addAttribute("kpis", kpis);
        model.addAttribute("filtro", filtroSeguro);
        model.addAttribute("tipos", TipoPatrullajeSerenazgo.values());
        model.addAttribute("estados", EstadoPatrullajeSerenazgo.values());
        model.addAttribute("sectores", sectorRepository.findByEstadoOrderByCodigoAsc(EstadoSectorPatrullaje.ACTIVO));
        model.addAttribute("moduloActivo", "serenazgo");
        model.addAttribute("submenuActivo", "patrullajes");
        model.addAttribute("paginaActiva", "serenazgo_patrullajes");

        return "admin/serenazgo/patrullajes/index";
    }

    @GetMapping("/nuevo")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_PATRULLAJES_CREAR', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_OPERADOR_CENTRAL')")
    public String mostrarFormularioNuevo(@RequestParam(value = "sectorId", required = false) UUID sectorId,
                                         Model model) {
        PatrullajeCrearRequest req = new PatrullajeCrearRequest();
        if (sectorId != null) {
            req.setSectorId(sectorId);
        }

        model.addAttribute("nuevoPatrullaje", req);
        cargarDatosFormulario(model);
        model.addAttribute("moduloActivo", "serenazgo");
        model.addAttribute("submenuActivo", "patrullajes");
        model.addAttribute("paginaActiva", "serenazgo_patrullajes");

        return "admin/serenazgo/patrullajes/nuevo";
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('SERENAZGO_PATRULLAJES_CREAR', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_OPERADOR_CENTRAL')")
    public String registrarPatrullaje(@Valid @ModelAttribute("nuevoPatrullaje") PatrullajeCrearRequest request,
                                      BindingResult bindingResult,
                                      Principal principal,
                                      RedirectAttributes redirectAttributes,
                                      Model model) {
        if (bindingResult.hasErrors()) {
            cargarDatosFormulario(model);
            model.addAttribute("moduloActivo", "serenazgo");
            model.addAttribute("submenuActivo", "patrullajes");
            model.addAttribute("paginaActiva", "serenazgo_patrullajes");
            return "admin/serenazgo/patrullajes/nuevo";
        }

        try {
            String username = (principal != null) ? principal.getName() : "ADMIN_MUNICIPAL";
            PatrullajeDetailResponse nuevo = patrullajeService.crearPatrullaje(request, username);
            redirectAttributes.addFlashAttribute("mensajeExito", "Patrullaje preventivo " + nuevo.getCodigo() + " planificado exitosamente.");
            return "redirect:/dashboard/serenazgo/patrullajes/" + nuevo.getId();
        } catch (Exception e) {
            log.error("Error al registrar patrullaje: {}", e.getMessage(), e);
            model.addAttribute("mensajeError", e.getMessage());
            cargarDatosFormulario(model);
            model.addAttribute("moduloActivo", "serenazgo");
            model.addAttribute("submenuActivo", "patrullajes");
            model.addAttribute("paginaActiva", "serenazgo_patrullajes");
            return "admin/serenazgo/patrullajes/nuevo";
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_PATRULLAJES_VER', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_OPERADOR_CENTRAL', 'ROLE_SERENO')")
    public String verDetallePatrullaje(@PathVariable("id") UUID id, Model model) {
        PatrullajeDetailResponse det = patrullajeService.obtenerPatrullajePorId(id);

        model.addAttribute("patrullaje", det);
        model.addAttribute("rutasDisponibles", patrullajeService.listarRutasPorSector(det.getSectorId()));
        model.addAttribute("tiposNovedad", TipoNovedadPatrullaje.values());
        model.addAttribute("metodosRegistro", MetodoRegistroVisita.values());
        model.addAttribute("incidenciasActivas", incidenciaRepository.findByEstadoIn(
                java.util.List.of(EstadoIncidenciaSeguridad.REGISTRADA, EstadoIncidenciaSeguridad.ASIGNADA, EstadoIncidenciaSeguridad.EN_ATENCION)));

        model.addAttribute("moduloActivo", "serenazgo");
        model.addAttribute("submenuActivo", "patrullajes");
        model.addAttribute("paginaActiva", "serenazgo_patrullajes");

        return "admin/serenazgo/patrullajes/detalle";
    }

    @PostMapping("/{id}/iniciar")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_PATRULLAJES_INICIAR', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_OPERADOR_CENTRAL', 'ROLE_SERENO')")
    public String iniciarPatrullaje(@PathVariable("id") UUID id,
                                   @ModelAttribute PatrullajeIniciarRequest request,
                                   Principal principal,
                                   RedirectAttributes redirectAttributes) {
        try {
            String username = (principal != null) ? principal.getName() : "OPERADOR_CAMPO";
            PatrullajeDetailResponse det = patrullajeService.iniciarPatrullaje(id, request, username);
            redirectAttributes.addFlashAttribute("mensajeExito", "El patrullaje " + det.getCodigo() + " ha sido iniciado formalmente en campo.");
        } catch (Exception e) {
            log.error("Error al iniciar patrullaje {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("mensajeError", "Error al iniciar patrullaje: " + e.getMessage());
        }
        return "redirect:/dashboard/serenazgo/patrullajes/" + id;
    }

    @PostMapping("/{id}/puntos")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_PATRULLAJES_REGISTRAR_RECORRIDO', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_OPERADOR_CENTRAL', 'ROLE_SERENO')")
    public String registrarPasoPunto(@PathVariable("id") UUID id,
                                     @Valid @ModelAttribute PatrullajeRegistrarVisitaRequest request,
                                     BindingResult bindingResult,
                                     Principal principal,
                                     RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("mensajeError", "Datos incompletos para el registro de paso por punto de control.");
            return "redirect:/dashboard/serenazgo/patrullajes/" + id;
        }

        try {
            String username = (principal != null) ? principal.getName() : "SERENO_CAMPO";
            patrullajeService.registrarPasoPunto(id, request, username);
            String accionDesc = request.getEstadoVisita() == EstadoVisitaPunto.VISITADO ? "visita registrada" : "omisión justificada registrada";
            redirectAttributes.addFlashAttribute("mensajeExito", "Paso por punto de control actualizado (" + accionDesc + ").");
        } catch (Exception e) {
            log.error("Error al registrar paso por punto en patrullaje {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("mensajeError", "Error al registrar paso por punto: " + e.getMessage());
        }
        return "redirect:/dashboard/serenazgo/patrullajes/" + id;
    }

    @PostMapping("/{id}/novedades")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_PATRULLAJES_REGISTRAR_NOVEDAD', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_OPERADOR_CENTRAL', 'ROLE_SERENO')")
    public String registrarNovedad(@PathVariable("id") UUID id,
                                   @Valid @ModelAttribute PatrullajeRegistrarNovedadRequest request,
                                   BindingResult bindingResult,
                                   Principal principal,
                                   RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("mensajeError", "Debe completar la descripción y tipo de la novedad.");
            return "redirect:/dashboard/serenazgo/patrullajes/" + id;
        }

        try {
            String username = (principal != null) ? principal.getName() : "SERENO_CAMPO";
            patrullajeService.registrarNovedad(id, request, username);
            redirectAttributes.addFlashAttribute("mensajeExito", "Novedad operativa incorporada a la bitácora del servicio.");
        } catch (Exception e) {
            log.error("Error al registrar novedad en patrullaje {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("mensajeError", "Error al registrar novedad: " + e.getMessage());
        }
        return "redirect:/dashboard/serenazgo/patrullajes/" + id;
    }

    @PostMapping("/{id}/pausar")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_PATRULLAJES_INICIAR', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_OPERADOR_CENTRAL', 'ROLE_SERENO')")
    public String pausarPatrullaje(@PathVariable("id") UUID id,
                                   @Valid @ModelAttribute PatrullajePausarRequest request,
                                   BindingResult bindingResult,
                                   Principal principal,
                                   RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("mensajeError", "El motivo de la pausa operativa es obligatorio.");
            return "redirect:/dashboard/serenazgo/patrullajes/" + id;
        }

        try {
            String username = (principal != null) ? principal.getName() : "OPERADOR_CENTRAL";
            patrullajeService.pausarPatrullaje(id, request, username);
            redirectAttributes.addFlashAttribute("mensajeExito", "El patrullaje ha sido colocado en pausa operativa justificadamente.");
        } catch (Exception e) {
            log.error("Error al pausar patrullaje {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("mensajeError", "Error al pausar patrullaje: " + e.getMessage());
        }
        return "redirect:/dashboard/serenazgo/patrullajes/" + id;
    }

    @PostMapping("/{id}/reanudar")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_PATRULLAJES_INICIAR', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_OPERADOR_CENTRAL', 'ROLE_SERENO')")
    public String reanudarPatrullaje(@PathVariable("id") UUID id,
                                     @ModelAttribute PatrullajeReanudarRequest request,
                                     Principal principal,
                                     RedirectAttributes redirectAttributes) {
        try {
            String username = (principal != null) ? principal.getName() : "OPERADOR_CENTRAL";
            patrullajeService.reanudarPatrullaje(id, request, username);
            redirectAttributes.addFlashAttribute("mensajeExito", "El patrullaje ha sido reanudado en ruta.");
        } catch (Exception e) {
            log.error("Error al reanudar patrullaje {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("mensajeError", "Error al reanudar patrullaje: " + e.getMessage());
        }
        return "redirect:/dashboard/serenazgo/patrullajes/" + id;
    }

    @PostMapping("/{id}/cambiar-ruta")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_PATRULLAJES_CAMBIAR_RUTA', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_OPERADOR_CENTRAL')")
    public String cambiarRutaPatrullaje(@PathVariable("id") UUID id,
                                        @Valid @ModelAttribute PatrullajeCambioRutaRequest request,
                                        BindingResult bindingResult,
                                        Principal principal,
                                        RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("mensajeError", "Debe seleccionar la nueva ruta y detallar el motivo del cambio.");
            return "redirect:/dashboard/serenazgo/patrullajes/" + id;
        }

        try {
            String username = (principal != null) ? principal.getName() : "SUPERVISOR";
            patrullajeService.cambiarRuta(id, request, username);
            redirectAttributes.addFlashAttribute("mensajeExito", "Cambio de ruta autorizado y registrado correctamente.");
        } catch (Exception e) {
            log.error("Error al cambiar ruta de patrullaje {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("mensajeError", "Error al cambiar ruta: " + e.getMessage());
        }
        return "redirect:/dashboard/serenazgo/patrullajes/" + id;
    }

    @PostMapping("/{id}/finalizar")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_PATRULLAJES_FINALIZAR', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_OPERADOR_CENTRAL', 'ROLE_SERENO')")
    public String finalizarPatrullaje(@PathVariable("id") UUID id,
                                      @Valid @ModelAttribute PatrullajeFinalizarRequest request,
                                      BindingResult bindingResult,
                                      Principal principal,
                                      RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("mensajeError", "Debe consignar el resultado administrativo para cerrar el patrullaje.");
            return "redirect:/dashboard/serenazgo/patrullajes/" + id;
        }

        try {
            String username = (principal != null) ? principal.getName() : "SUPERVISOR";
            patrullajeService.finalizarPatrullaje(id, request, username);
            redirectAttributes.addFlashAttribute("mensajeExito", "Patrullaje concluido y cerrado formalmente.");
        } catch (Exception e) {
            log.error("Error al finalizar patrullaje {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("mensajeError", "Error al finalizar patrullaje: " + e.getMessage());
        }
        return "redirect:/dashboard/serenazgo/patrullajes/" + id;
    }

    @PostMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_PATRULLAJES_CANCELAR', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO')")
    public String cancelarPatrullaje(@PathVariable("id") UUID id,
                                     @Valid @ModelAttribute PatrullajeCancelarRequest request,
                                     BindingResult bindingResult,
                                     Principal principal,
                                     RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("mensajeError", "Debe indicar el motivo justificatorio para cancelar el patrullaje.");
            return "redirect:/dashboard/serenazgo/patrullajes/" + id;
        }

        try {
            String username = (principal != null) ? principal.getName() : "SUPERVISOR";
            patrullajeService.cancelarPatrullaje(id, request, username);
            redirectAttributes.addFlashAttribute("mensajeExito", "El patrullaje ha sido cancelado formalmente.");
        } catch (Exception e) {
            log.error("Error al cancelar patrullaje {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("mensajeError", "Error al cancelar patrullaje: " + e.getMessage());
        }
        return "redirect:/dashboard/serenazgo/patrullajes/" + id;
    }

    @GetMapping("/exportar/excel")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_PATRULLAJES_EXPORTAR', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_OPERADOR_CENTRAL')")
    public ResponseEntity<byte[]> exportarExcel(@ModelAttribute PatrullajeFilter filtro) {
        byte[] excelBytes = patrullajeService.exportarExcel(filtro);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"reporte-patrullajes-quinua.xlsx\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }

    @GetMapping("/{id}/ficha-pdf")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_PATRULLAJES_VER', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_OPERADOR_CENTRAL', 'ROLE_SERENO')")
    public ResponseEntity<byte[]> descargarFichaPdf(@PathVariable("id") UUID id) {
        byte[] pdfBytes = patrullajeService.generarFichaPdf(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"ficha-patrullaje-" + id + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    private void cargarDatosFormulario(Model model) {
        model.addAttribute("sectores", sectorRepository.findByEstadoOrderByCodigoAsc(EstadoSectorPatrullaje.ACTIVO));
        model.addAttribute("rutas", patrullajeService.listarRutasActivas());
        model.addAttribute("personalActivo", personalRepository.findByEstado(EstadoPersonalSerenazgo.ACTIVO));
        model.addAttribute("unidadesDisponibles", unidadMovilRepository.findByEstadoOperativo(EstadoUnidadMovil.DISPONIBLE));
        model.addAttribute("equipamientoDisponible", equipamientoRepository.findByEstado(EstadoEquipamientoSerenazgo.DISPONIBLE));
        model.addAttribute("tiposPatrullaje", TipoPatrullajeSerenazgo.values());
    }
}
