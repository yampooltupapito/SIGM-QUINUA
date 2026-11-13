package pe.gob.municipalidad.sigm.modules.serenazgo.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.gob.municipalidad.sigm.common.repository.OficinaRepository;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.SectorPatrullaje;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.TipoIncidenciaSeguridad;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.ZonaPatrullaje;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.repository.PersonalSerenazgoRepository;
import pe.gob.municipalidad.sigm.modules.serenazgo.repository.SectorPatrullajeRepository;
import pe.gob.municipalidad.sigm.modules.serenazgo.repository.TipoIncidenciaSeguridadRepository;
import pe.gob.municipalidad.sigm.modules.serenazgo.repository.ZonaPatrullajeRepository;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.IncidenciaSeguridadCiudadanaService;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/dashboard/serenazgo/incidencias")
@RequiredArgsConstructor
@Slf4j
public class IncidenciasWebController {

    private final IncidenciaSeguridadCiudadanaService incidenciaService;
    private final TipoIncidenciaSeguridadRepository tipoIncidenciaRepository;
    private final SectorPatrullajeRepository sectorRepository;
    private final ZonaPatrullajeRepository zonaRepository;
    private final PersonalSerenazgoRepository personalRepository;
    private final OficinaRepository oficinaRepository;

    // =========================================================================
    // 1. BANDEJA GENERAL Y DASHBOARD DE INCIDENCIAS
    // =========================================================================

    @GetMapping
    @PreAuthorize("hasAuthority('SERENAZGO_INCIDENCIAS_VER') or hasRole('ROLE_ADMIN') or hasRole('ROLE_SERENO') or hasRole('ROLE_SUPERADMIN')")
    public String indexIncidencias(@RequestParam(name = "termino", required = false) String termino,
                                   @RequestParam(name = "desde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                   @RequestParam(name = "hasta", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                                   @RequestParam(name = "estado", required = false) EstadoIncidenciaSeguridad estado,
                                   @RequestParam(name = "prioridad", required = false) PrioridadIncidenciaSeguridad prioridad,
                                   @RequestParam(name = "tipoId", required = false) UUID tipoId,
                                   @RequestParam(name = "sectorId", required = false) UUID sectorId,
                                   @RequestParam(name = "zonaId", required = false) UUID zonaId,
                                   @RequestParam(name = "confidencialidad", required = false) NivelConfidencialidad confidencialidad,
                                   @RequestParam(name = "page", defaultValue = "0") int page,
                                   @RequestParam(name = "size", defaultValue = "10") int size,
                                   Model model) {

        IncidenciaFilter filtro = IncidenciaFilter.builder()
                .termino(termino)
                .desde(desde)
                .hasta(hasta)
                .estado(estado)
                .prioridad(prioridad)
                .tipoId(tipoId)
                .sectorId(sectorId)
                .zonaId(zonaId)
                .confidencialidad(confidencialidad)
                .build();

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "fechaOcurrencia"));
        Page<IncidenciaResponse> incidencias = incidenciaService.listarConFiltros(filtro, pageable);
        DashboardIncidenciasResponse kpis = incidenciaService.obtenerDashboardIncidencias();

        model.addAttribute("incidencias", incidencias);
        model.addAttribute("kpis", kpis);
        model.addAttribute("filtro", filtro);

        model.addAttribute("moduloActivo", "serenazgo");
        model.addAttribute("submenuActivo", "incidencias");
        model.addAttribute("tiposIncidencia", tipoIncidenciaRepository.findByActivoTrueOrderByNombreAsc());
        model.addAttribute("sectores", sectorRepository.findByEstado(EstadoSectorPatrullaje.ACTIVO));
        model.addAttribute("estados", EstadoIncidenciaSeguridad.values());
        model.addAttribute("prioridades", PrioridadIncidenciaSeguridad.values());
        model.addAttribute("nivelesConfidencialidad", NivelConfidencialidad.values());

        return "admin/serenazgo/incidencias/index";
    }

    // =========================================================================
    // 2. NUEVA INCIDENCIA (FORMULARIO Y PERSISTENCIA)
    // =========================================================================

    @GetMapping("/nueva")
    @PreAuthorize("hasAuthority('SERENAZGO_INCIDENCIAS_CREAR') or hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPERADMIN')")
    public String nuevaIncidenciaForm(Model model) {
        if (!model.containsAttribute("incidenciaForm")) {
            IncidenciaCreateRequest form = new IncidenciaCreateRequest();
            form.setFechaHoraOcurrencia(OffsetDateTime.now());
            form.setPrioridad(PrioridadIncidenciaSeguridad.MEDIA);
            form.setCanalReporte(CanalReporteIncidencia.LLAMADA_OPERADOR);
            form.setOrigenHecho(OrigenHechoIncidencia.HECHO_REPORTADO);
            form.setNivelConfidencialidad(NivelConfidencialidad.ORDINARIO);
            form.setReportanteAnonimo(false);
            model.addAttribute("incidenciaForm", form);
        }

        cargarCatalogosFormulario(model);
        return "admin/serenazgo/incidencias/incidencia-form";
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SERENAZGO_INCIDENCIAS_CREAR') or hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPERADMIN')")
    public String registrarIncidencia(@Valid @ModelAttribute("incidenciaForm") IncidenciaCreateRequest form,
                                      BindingResult result,
                                      Principal principal,
                                      RedirectAttributes redirectAttributes,
                                      Model model) {

        if (result.hasErrors()) {
            cargarCatalogosFormulario(model);
            return "admin/serenazgo/incidencias/incidencia-form";
        }

        try {
            String username = principal != null ? principal.getName() : "SISTEMA";
            IncidenciaResponse creada = incidenciaService.registrarIncidencia(form, username);
            redirectAttributes.addFlashAttribute("mensajeExito", "Incidencia " + creada.getCodigo() + " registrada satisfactoriamente.");
            return "redirect:/dashboard/serenazgo/incidencias/" + creada.getId();
        } catch (Exception ex) {
            log.error("Error al registrar incidencia: {}", ex.getMessage(), ex);
            model.addAttribute("mensajeError", "Error al registrar la incidencia: " + ex.getMessage());
            cargarCatalogosFormulario(model);
            return "admin/serenazgo/incidencias/incidencia-form";
        }
    }

    // =========================================================================
    // 3. DETALLE DE INCIDENCIA (FICHA TÉCNICA Y AUDITORÍA)
    // =========================================================================

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SERENAZGO_INCIDENCIAS_VER') or hasRole('ROLE_ADMIN') or hasRole('ROLE_SERENO') or hasRole('ROLE_SUPERADMIN')")
    public String detalleIncidencia(@PathVariable UUID id,
                                    Authentication authentication,
                                    Model model) {

        boolean puedeVerSensibles = authentication != null && (
                authentication.getAuthorities().stream().anyMatch(a ->
                        a.getAuthority().equals("ROLE_ADMIN") ||
                        a.getAuthority().equals("ROLE_SUPERADMIN") ||
                        a.getAuthority().equals("SERENAZGO_INCIDENCIAS_EDITAR"))
        );

        IncidenciaDetailResponse detalle = incidenciaService.obtenerDetallePorId(id, puedeVerSensibles);

        model.addAttribute("moduloActivo", "serenazgo");
        model.addAttribute("submenuActivo", "incidencias");
        model.addAttribute("incidencia", detalle);
        model.addAttribute("puedeVerSensibles", puedeVerSensibles);
        model.addAttribute("estados", EstadoIncidenciaSeguridad.values());
        model.addAttribute("prioridades", PrioridadIncidenciaSeguridad.values());

        return "admin/serenazgo/incidencias/incidencia-detalle";
    }

    // =========================================================================
    // 4. EDICIÓN DE INCIDENCIA
    // =========================================================================

    @GetMapping("/{id}/editar")
    @PreAuthorize("hasAuthority('SERENAZGO_INCIDENCIAS_EDITAR') or hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPERADMIN')")
    public String editarIncidenciaForm(@PathVariable UUID id,
                                       Authentication authentication,
                                       Model model) {

        boolean puedeVerSensibles = authentication != null && (
                authentication.getAuthorities().stream().anyMatch(a ->
                        a.getAuthority().equals("ROLE_ADMIN") ||
                        a.getAuthority().equals("ROLE_SUPERADMIN") ||
                        a.getAuthority().equals("SERENAZGO_INCIDENCIAS_EDITAR"))
        );

        IncidenciaDetailResponse detalle = incidenciaService.obtenerDetallePorId(id, puedeVerSensibles);

        if (detalle.getEstado() == EstadoIncidenciaSeguridad.CERRADA || detalle.getEstado() == EstadoIncidenciaSeguridad.DESCARTADA) {
            model.addAttribute("mensajeError", "No se puede editar una incidencia que se encuentra cerrada o descartada.");
            return "redirect:/dashboard/serenazgo/incidencias/" + id;
        }

        if (!model.containsAttribute("incidenciaForm")) {
            IncidenciaUpdateRequest form = new IncidenciaUpdateRequest();
            form.setTitulo(detalle.getTitulo());
            form.setTipoIncidenciaId(detalle.getTipoId());
            form.setCanalReporte(detalle.getCanalReporte());
            form.setOrigenHecho(detalle.getOrigenHecho());
            form.setNivelConfidencialidad(detalle.getNivelConfidencialidad());
            form.setSectorId(detalle.getSectorId());
            form.setZonaId(detalle.getZonaId());
            form.setUbicacionReferencia(detalle.getUbicacionReferencia());
            form.setLatitud(detalle.getLatitud());
            form.setLongitud(detalle.getLongitud());
            form.setReportanteAnonimo(detalle.getReportanteAnonimo());
            form.setReportanteNombre(detalle.getReportanteNombre());
            form.setReportanteTelefono(detalle.getReportanteTelefono());
            form.setReportanteDireccion(detalle.getReportanteDireccion());
            form.setDescripcion(detalle.getDescripcion());
            form.setObservacionesInternas(detalle.getObservacionesInternas());
            form.setOficinaResponsableId(detalle.getOficinaResponsableId());
            form.setPersonalReportaId(detalle.getPersonalReportaId());
            form.setMotivoModificacion("Actualización administrativa de información de la incidencia");
            model.addAttribute("incidenciaForm", form);
        }

        model.addAttribute("incidenciaId", id);
        model.addAttribute("codigoIncidencia", detalle.getCodigo());
        cargarCatalogosFormulario(model);
        return "admin/serenazgo/incidencias/incidencia-form";
    }

    @PostMapping("/{id}/editar")
    @PreAuthorize("hasAuthority('SERENAZGO_INCIDENCIAS_EDITAR') or hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPERADMIN')")
    public String actualizarIncidencia(@PathVariable UUID id,
                                       @Valid @ModelAttribute("incidenciaForm") IncidenciaUpdateRequest form,
                                       BindingResult result,
                                       Principal principal,
                                       RedirectAttributes redirectAttributes,
                                       Model model) {

        if (result.hasErrors()) {
            model.addAttribute("incidenciaId", id);
            cargarCatalogosFormulario(model);
            return "admin/serenazgo/incidencias/incidencia-form";
        }

        try {
            String username = principal != null ? principal.getName() : "SISTEMA";
            incidenciaService.actualizarIncidencia(id, form, username);
            redirectAttributes.addFlashAttribute("mensajeExito", "Incidencia actualizada satisfactoriamente.");
            return "redirect:/dashboard/serenazgo/incidencias/" + id;
        } catch (Exception ex) {
            log.error("Error al actualizar incidencia {}: {}", id, ex.getMessage(), ex);
            model.addAttribute("mensajeError", "Error al actualizar la incidencia: " + ex.getMessage());
            model.addAttribute("incidenciaId", id);
            cargarCatalogosFormulario(model);
            return "admin/serenazgo/incidencias/incidencia-form";
        }
    }

    // =========================================================================
    // 5. ACCIONES MODALES: ESTADO, PRIORIDAD, CIERRE, DESCARTE, NOTAS, ARCHIVOS
    // =========================================================================

    @PostMapping("/{id}/estado")
    @PreAuthorize("hasAuthority('SERENAZGO_INCIDENCIAS_CAMBIAR_ESTADO') or hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPERADMIN')")
    public String cambiarEstado(@PathVariable UUID id,
                                @RequestParam("nuevoEstado") EstadoIncidenciaSeguridad nuevoEstado,
                                @RequestParam("motivo") String motivo,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {
        try {
            CambioEstadoIncidenciaRequest req = new CambioEstadoIncidenciaRequest();
            req.setNuevoEstado(nuevoEstado);
            req.setMotivo(motivo);
            String username = principal != null ? principal.getName() : "SISTEMA";
            incidenciaService.cambiarEstado(id, req, username);
            redirectAttributes.addFlashAttribute("mensajeExito", "Estado actualizado correctamente a: " + nuevoEstado);
        } catch (Exception ex) {
            log.error("Error al cambiar estado de incidencia {}: {}", id, ex.getMessage());
            redirectAttributes.addFlashAttribute("mensajeError", "No se pudo cambiar el estado: " + ex.getMessage());
        }
        return "redirect:/dashboard/serenazgo/incidencias/" + id;
    }

    @PostMapping("/{id}/prioridad")
    @PreAuthorize("hasAuthority('SERENAZGO_INCIDENCIAS_PRIORIZAR') or hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPERADMIN')")
    public String cambiarPrioridad(@PathVariable UUID id,
                                   @RequestParam("nuevaPrioridad") PrioridadIncidenciaSeguridad nuevaPrioridad,
                                   @RequestParam("justificacion") String justificacion,
                                   Principal principal,
                                   RedirectAttributes redirectAttributes) {
        try {
            CambioPrioridadIncidenciaRequest req = new CambioPrioridadIncidenciaRequest();
            req.setNuevaPrioridad(nuevaPrioridad);
            req.setJustificacion(justificacion);
            String username = principal != null ? principal.getName() : "SISTEMA";
            incidenciaService.cambiarPrioridad(id, req, username);
            redirectAttributes.addFlashAttribute("mensajeExito", "Prioridad actualizada correctamente a: " + nuevaPrioridad);
        } catch (Exception ex) {
            log.error("Error al cambiar prioridad de incidencia {}: {}", id, ex.getMessage());
            redirectAttributes.addFlashAttribute("mensajeError", "No se pudo cambiar la prioridad: " + ex.getMessage());
        }
        return "redirect:/dashboard/serenazgo/incidencias/" + id;
    }

    @PostMapping("/{id}/cerrar")
    @PreAuthorize("hasAuthority('SERENAZGO_INCIDENCIAS_CERRAR') or hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPERADMIN')")
    public String cerrarIncidencia(@PathVariable UUID id,
                                   @RequestParam("motivoCierre") String motivoCierre,
                                   @RequestParam(name = "conclusiones", required = false) String conclusiones,
                                   Principal principal,
                                   RedirectAttributes redirectAttributes) {
        try {
            CierreIncidenciaRequest req = new CierreIncidenciaRequest();
            req.setMotivoCierre(motivoCierre);
            req.setResultadoAtencion(conclusiones != null && !conclusiones.trim().isEmpty() ? conclusiones : motivoCierre);
            String username = principal != null ? principal.getName() : "SISTEMA";
            incidenciaService.cerrarIncidencia(id, req, username);
            redirectAttributes.addFlashAttribute("mensajeExito", "Incidencia cerrada administrativamente con éxito.");
        } catch (Exception ex) {
            log.error("Error al cerrar incidencia {}: {}", id, ex.getMessage());
            redirectAttributes.addFlashAttribute("mensajeError", "No se pudo cerrar la incidencia: " + ex.getMessage());
        }
        return "redirect:/dashboard/serenazgo/incidencias/" + id;
    }

    @PostMapping("/{id}/descartar")
    @PreAuthorize("hasAuthority('SERENAZGO_INCIDENCIAS_CERRAR') or hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPERADMIN')")
    public String descartarIncidencia(@PathVariable UUID id,
                                      @RequestParam("motivoDescarte") String motivoDescarte,
                                      @RequestParam(name = "sustento", required = false) String sustento,
                                      Principal principal,
                                      RedirectAttributes redirectAttributes) {
        try {
            DescarteIncidenciaRequest req = new DescarteIncidenciaRequest();
            req.setMotivoDescarte(motivoDescarte);
            String username = principal != null ? principal.getName() : "SISTEMA";
            incidenciaService.descartarIncidencia(id, req, username);
            redirectAttributes.addFlashAttribute("mensajeExito", "Incidencia descartada formalmente.");
        } catch (Exception ex) {
            log.error("Error al descartar incidencia {}: {}", id, ex.getMessage());
            redirectAttributes.addFlashAttribute("mensajeError", "No se pudo descartar la incidencia: " + ex.getMessage());
        }
        return "redirect:/dashboard/serenazgo/incidencias/" + id;
    }

    @PostMapping("/{id}/comentarios")
    @PreAuthorize("hasAuthority('SERENAZGO_INCIDENCIAS_EDITAR') or hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPERADMIN')")
    public String agregarComentario(@PathVariable UUID id,
                                    @RequestParam("comentario") String textoComentario,
                                    @RequestParam(name = "esInterno", defaultValue = "true") boolean esInterno,
                                    Principal principal,
                                    RedirectAttributes redirectAttributes) {
        try {
            AgregarComentarioRequest req = new AgregarComentarioRequest();
            req.setComentario(textoComentario);
            req.setEsInterno(esInterno);
            String username = principal != null ? principal.getName() : "SISTEMA";
            incidenciaService.agregarComentario(id, req, username);
            redirectAttributes.addFlashAttribute("mensajeExito", "Nota administrativa agregada con éxito.");
        } catch (Exception ex) {
            log.error("Error al agregar nota a incidencia {}: {}", id, ex.getMessage());
            redirectAttributes.addFlashAttribute("mensajeError", "No se pudo agregar la nota: " + ex.getMessage());
        }
        return "redirect:/dashboard/serenazgo/incidencias/" + id;
    }

    @PostMapping("/{id}/adjuntos")
    @PreAuthorize("hasAuthority('SERENAZGO_INCIDENCIAS_ADJUNTAR') or hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPERADMIN')")
    public String subirAdjunto(@PathVariable UUID id,
                               @RequestParam("archivo") MultipartFile archivo,
                               @RequestParam(name = "descripcion", required = false) String descripcion,
                               @RequestParam(name = "esFotografia", defaultValue = "false") boolean esFotografia,
                               Principal principal,
                               RedirectAttributes redirectAttributes) {
        if (archivo.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensajeError", "Debe seleccionar un archivo válido para adjuntar.");
            return "redirect:/dashboard/serenazgo/incidencias/" + id;
        }

        try {
            String username = principal != null ? principal.getName() : "SISTEMA";
            incidenciaService.adjuntarDocumento(
                    id,
                    archivo.getOriginalFilename(),
                    archivo.getContentType(),
                    archivo.getBytes(),
                    descripcion,
                    esFotografia,
                    username
            );
            redirectAttributes.addFlashAttribute("mensajeExito", "Documento adjuntado satisfactoriamente.");
        } catch (IOException ex) {
            log.error("Error de E/S al leer archivo adjunto para incidencia {}: {}", id, ex.getMessage());
            redirectAttributes.addFlashAttribute("mensajeError", "Error al procesar el archivo: " + ex.getMessage());
        } catch (Exception ex) {
            log.error("Error al adjuntar archivo en incidencia {}: {}", id, ex.getMessage());
            redirectAttributes.addFlashAttribute("mensajeError", "No se pudo adjuntar el archivo: " + ex.getMessage());
        }
        return "redirect:/dashboard/serenazgo/incidencias/" + id;
    }

    // =========================================================================
    // 6. EXPORTACIONES (PDF Y EXCEL)
    // =========================================================================

    @GetMapping("/{id}/pdf")
    @PreAuthorize("hasAuthority('SERENAZGO_INCIDENCIAS_EXPORTAR') or hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPERADMIN')")
    public ResponseEntity<byte[]> exportarPdf(@PathVariable UUID id) {
        byte[] pdfBytes = incidenciaService.exportarFichaPdf(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"Ficha_Incidencia_" + id + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @GetMapping("/exportar/excel")
    @PreAuthorize("hasAuthority('SERENAZGO_INCIDENCIAS_EXPORTAR') or hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPERADMIN')")
    public ResponseEntity<byte[]> exportarExcel(@RequestParam(name = "desde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                                @RequestParam(name = "hasta", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        byte[] xlsxBytes = incidenciaService.exportarIncidenciasExcel(desde, hasta);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"Reporte_Incidencias_Quinua.xlsx\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(xlsxBytes);
    }

    // =========================================================================
    // 7. ENDPOINTS AUXILIARES JSON PARA FORMULARIOS DINÁMICOS
    // =========================================================================

    @GetMapping("/api/sectores/{sectorId}/zonas")
    @ResponseBody
    public ResponseEntity<List<ZonaPatrullaje>> obtenerZonasPorSector(@PathVariable UUID sectorId) {
        List<ZonaPatrullaje> zonas = zonaRepository.findBySectorIdAndEstado(sectorId, EstadoSectorPatrullaje.ACTIVO);
        return ResponseEntity.ok(zonas);
    }

    @GetMapping("/api/duplicados")
    @ResponseBody
    public ResponseEntity<List<PosibleDuplicadoResponse>> verificarPosiblesDuplicados(
            @RequestParam("tipoId") UUID tipoId,
            @RequestParam(name = "sectorId", required = false) UUID sectorId,
            @RequestParam("fecha") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime fecha,
            @RequestParam(name = "excluirId", required = false) UUID excluirId) {

        List<PosibleDuplicadoResponse> duplicados = incidenciaService.buscarPosiblesDuplicados(tipoId, sectorId, fecha, excluirId);
        return ResponseEntity.ok(duplicados);
    }

    // =========================================================================
    // MÉTODOS PRIVADOS DE APOYO
    // =========================================================================

    private void cargarCatalogosFormulario(Model model) {
        model.addAttribute("moduloActivo", "serenazgo");
        model.addAttribute("submenuActivo", "incidencias");
        model.addAttribute("tiposIncidencia", tipoIncidenciaRepository.findByActivoTrueOrderByNombreAsc());
        model.addAttribute("sectores", sectorRepository.findByEstado(EstadoSectorPatrullaje.ACTIVO));
        model.addAttribute("canalesReporte", CanalReporteIncidencia.values());
        model.addAttribute("origenesHecho", OrigenHechoIncidencia.values());
        model.addAttribute("nivelesConfidencialidad", NivelConfidencialidad.values());
        model.addAttribute("prioridades", PrioridadIncidenciaSeguridad.values());
        model.addAttribute("personalList", personalRepository.findTop200ByEstadoOrderByCodigoInternoAsc(EstadoPersonalSerenazgo.ACTIVO));
        model.addAttribute("oficinas", oficinaRepository.findByEstado("ACTIVO"));
    }
}
