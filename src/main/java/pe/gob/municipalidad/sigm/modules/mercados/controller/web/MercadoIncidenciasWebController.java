package pe.gob.municipalidad.sigm.modules.mercados.controller.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.common.repository.OficinaRepository;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.mercados.dto.*;
import pe.gob.municipalidad.sigm.modules.mercados.entity.EvidenciaIncidenciaMercado;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoEvaluacionInfraccion;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoIncidencia;
import pe.gob.municipalidad.sigm.modules.mercados.enums.OrigenIncidencia;
import pe.gob.municipalidad.sigm.modules.mercados.enums.PrioridadIncidencia;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoActuacionIncidencia;
import pe.gob.municipalidad.sigm.modules.mercados.repository.EvidenciaIncidenciaMercadoRepository;
import pe.gob.municipalidad.sigm.modules.mercados.repository.MercadoMunicipalRepository;
import pe.gob.municipalidad.sigm.modules.mercados.service.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Slf4j
@Controller
@RequestMapping("/dashboard/mercados")
public class MercadoIncidenciasWebController {

    private final IncidenciaMercadoService incidenciaService;
    private final TipoIncidenciaService tipoService;
    private final TipificacionInfraccionService tipificacionService;
    private final PresuntaInfraccionService infraccionService;
    private final IncidenciasReporteService reporteService;
    private final MercadoMunicipalRepository mercadoRepository;
    private final OficinaRepository oficinaRepository;
    private final UsuarioRepository usuarioRepository;
    private final EvidenciaIncidenciaMercadoRepository evidenciaRepository;

    public MercadoIncidenciasWebController(
            IncidenciaMercadoService incidenciaService,
            TipoIncidenciaService tipoService,
            TipificacionInfraccionService tipificacionService,
            PresuntaInfraccionService infraccionService,
            IncidenciasReporteService reporteService,
            MercadoMunicipalRepository mercadoRepository,
            OficinaRepository oficinaRepository,
            UsuarioRepository usuarioRepository,
            EvidenciaIncidenciaMercadoRepository evidenciaRepository) {
        this.incidenciaService = incidenciaService;
        this.tipoService = tipoService;
        this.tipificacionService = tipificacionService;
        this.infraccionService = infraccionService;
        this.reporteService = reporteService;
        this.mercadoRepository = mercadoRepository;
        this.oficinaRepository = oficinaRepository;
        this.usuarioRepository = usuarioRepository;
        this.evidenciaRepository = evidenciaRepository;
    }

    // 1. Dashboard de Incidencias
    @GetMapping("/incidencias/dashboard")
    @PreAuthorize("hasAuthority('MERCADOS_INCIDENCIA_VER')")
    public String dashboard(Model model) {
        EstadisticasIncidenciasResponse stats = reporteService.obtenerEstadisticas();
        model.addAttribute("stats", stats);
        model.addAttribute("mercados", mercadoRepository.findAll());
        return "admin/mercados/incidencias/dashboard";
    }

    // 2. Listado Principal de Incidencias
    @GetMapping("/incidencias")
    @PreAuthorize("hasAuthority('MERCADOS_INCIDENCIA_VER')")
    public String listarIncidencias(
            @RequestParam(required = false) UUID mercadoId,
            @RequestParam(required = false) UUID tipoId,
            @RequestParam(required = false) PrioridadIncidencia prioridad,
            @RequestParam(required = false) EstadoIncidencia estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            Model model) {

        OffsetDateTime dtDesde = fechaDesde != null ? fechaDesde.atStartOfDay().atOffset(ZoneOffset.UTC) : null;
        OffsetDateTime dtHasta = fechaHasta != null ? fechaHasta.atTime(23, 59, 59).atOffset(ZoneOffset.UTC) : null;
        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaReporte").descending());

        Page<IncidenciaResponse> incidenciasPage = incidenciaService.buscarPaginado(
                mercadoId, null, null, null, null, tipoId, null, prioridad, estado, null,
                dtDesde, dtHasta, null, q, pageable);

        model.addAttribute("incidenciasPage", incidenciasPage);
        model.addAttribute("mercados", mercadoRepository.findAll());
        model.addAttribute("tipos", tipoService.listarActivos());
        model.addAttribute("prioridades", PrioridadIncidencia.values());
        model.addAttribute("estados", EstadoIncidencia.values());
        model.addAttribute("mercadoId", mercadoId);
        model.addAttribute("tipoId", tipoId);
        model.addAttribute("prioridad", prioridad);
        model.addAttribute("estado", estado);
        model.addAttribute("fechaDesde", fechaDesde);
        model.addAttribute("fechaHasta", fechaHasta);
        model.addAttribute("q", q);

        return "admin/mercados/incidencias/listado";
    }

    // 3. Formulario de Registro de Incidencia
    @GetMapping("/incidencias/nueva")
    @PreAuthorize("hasAuthority('MERCADOS_INCIDENCIA_CREAR')")
    public String formularioNueva(Model model) {
        model.addAttribute("mercados", mercadoRepository.findAll());
        model.addAttribute("tipos", tipoService.listarActivos());
        model.addAttribute("origenes", OrigenIncidencia.values());
        model.addAttribute("prioridades", PrioridadIncidencia.values());
        model.addAttribute("oficinas", oficinaRepository.findAll());
        model.addAttribute("usuarios", usuarioRepository.findAll());
        return "admin/mercados/incidencias/formulario";
    }

    // 4. Detalle Integral de Incidencia
    @GetMapping("/incidencias/{id}")
    @PreAuthorize("hasAuthority('MERCADOS_INCIDENCIA_VER')")
    public String detalleIncidencia(@PathVariable UUID id, Model model) {
        IncidenciaDetailResponse incidencia = incidenciaService.obtenerDetalle(id);
        model.addAttribute("incidencia", incidencia);
        model.addAttribute("tipos", tipoService.listarActivos());
        model.addAttribute("prioridades", PrioridadIncidencia.values());
        model.addAttribute("estados", EstadoIncidencia.values());
        model.addAttribute("oficinas", oficinaRepository.findAll());
        model.addAttribute("usuarios", usuarioRepository.findAll());
        model.addAttribute("tipificaciones", tipificacionService.listarVigentes());
        model.addAttribute("tiposActuacion", TipoActuacionIncidencia.values());
        return "admin/mercados/incidencias/detalle";
    }

    // 4.1. Asignar Responsable
    @PostMapping("/incidencias/{id}/asignar")
    @PreAuthorize("hasAuthority('MERCADOS_INCIDENCIA_ASIGNAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public String asignarResponsableWeb(
            @PathVariable UUID id,
            @ModelAttribute IncidenciaAsignacionRequest request,
            Authentication auth,
            RedirectAttributes ra) {
        try {
            String username = auth != null ? auth.getName() : "admin";
            incidenciaService.asignarResponsable(id, request, username);
            ra.addFlashAttribute("mensajeExito", "Responsable asignado correctamente.");
        } catch (Exception e) {
            log.error("Error al asignar responsable a incidencia {}", id, e);
            ra.addFlashAttribute("mensajeError", "Error al asignar responsable: " + e.getMessage());
        }
        return "redirect:/dashboard/mercados/incidencias/" + id;
    }

    // 4.2. Registrar Actuación
    @PostMapping("/incidencias/{id}/actuaciones")
    @PreAuthorize("hasAuthority('MERCADOS_INCIDENCIA_ATENDER') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public String registrarActuacionWeb(
            @PathVariable UUID id,
            @ModelAttribute IncidenciaActuacionRequest request,
            Authentication auth,
            RedirectAttributes ra) {
        try {
            String username = auth != null ? auth.getName() : "admin";
            incidenciaService.registrarActuacion(id, request, username);
            ra.addFlashAttribute("mensajeExito", "Actuación registrada satisfactoriamente.");
        } catch (Exception e) {
            log.error("Error al registrar actuación en incidencia {}", id, e);
            ra.addFlashAttribute("mensajeError", "Error al registrar actuación: " + e.getMessage());
        }
        return "redirect:/dashboard/mercados/incidencias/" + id;
    }

    // 4.3. Resolver Incidencia
    @PostMapping("/incidencias/{id}/resolver")
    @PreAuthorize("hasAuthority('MERCADOS_INCIDENCIA_RESOLVER') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public String resolverIncidenciaWeb(
            @PathVariable UUID id,
            @ModelAttribute IncidenciaResolucionRequest request,
            Authentication auth,
            RedirectAttributes ra) {
        try {
            String username = auth != null ? auth.getName() : "admin";
            incidenciaService.resolver(id, request, username);
            ra.addFlashAttribute("mensajeExito", "Incidencia resuelta exitosamente.");
        } catch (Exception e) {
            log.error("Error al resolver incidencia {}", id, e);
            ra.addFlashAttribute("mensajeError", "Error al resolver incidencia: " + e.getMessage());
        }
        return "redirect:/dashboard/mercados/incidencias/" + id;
    }

    // 4.4. Cerrar o Descartar
    @PostMapping("/incidencias/{id}/cerrar")
    @PreAuthorize("hasAuthority('MERCADOS_INCIDENCIA_CERRAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public String cerrarIncidenciaWeb(
            @PathVariable UUID id,
            @ModelAttribute IncidenciaCierreRequest request,
            Authentication auth,
            RedirectAttributes ra) {
        try {
            String username = auth != null ? auth.getName() : "admin";
            incidenciaService.cerrar(id, request, username);
            ra.addFlashAttribute("mensajeExito", "Incidencia finalizada.");
        } catch (Exception e) {
            log.error("Error al cerrar incidencia {}", id, e);
            ra.addFlashAttribute("mensajeError", "Error al cerrar incidencia: " + e.getMessage());
        }
        return "redirect:/dashboard/mercados/incidencias/" + id;
    }

    // 4.5. Evaluar y Derivar a Infracción
    @PostMapping("/incidencias/{id}/infraccion")
    @PreAuthorize("hasAuthority('MERCADOS_INFRACCION_REGISTRAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public String derivarInfraccionWeb(
            @PathVariable UUID id,
            @ModelAttribute PresuntaInfraccionCreateRequest request,
            Authentication auth,
            RedirectAttributes ra) {
        try {
            String username = auth != null ? auth.getName() : "admin";
            request.setIncidenciaId(id);
            infraccionService.registrar(request, username);
            ra.addFlashAttribute("mensajeExito", "Presunta infracción tipificada y derivada exitosamente.");
        } catch (Exception e) {
            log.error("Error al derivar presunta infracción desde incidencia {}", id, e);
            ra.addFlashAttribute("mensajeError", "Error al tipificar infracción: " + e.getMessage());
        }
        return "redirect:/dashboard/mercados/incidencias/" + id;
    }

    // 4.6. Adjuntar Evidencia
    @PostMapping(value = "/incidencias/{id}/evidencias", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('MERCADOS_INCIDENCIA_EVIDENCIA_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public String adjuntarEvidenciaWeb(
            @PathVariable UUID id,
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam(required = false, defaultValue = "FOTOGRAFIA") String tipoEvidencia,
            @RequestParam(required = false) String descripcion,
            Authentication auth,
            RedirectAttributes ra) {
        try {
            String username = auth != null ? auth.getName() : "admin";
            incidenciaService.adjuntarEvidenciaArchivo(id, archivo, tipoEvidencia, descripcion, username);
            ra.addFlashAttribute("mensajeExito", "Evidencia adjuntada exitosamente.");
        } catch (Exception e) {
            log.error("Error al adjuntar evidencia a incidencia {}", id, e);
            ra.addFlashAttribute("mensajeError", "Error al adjuntar evidencia: " + e.getMessage());
        }
        return "redirect:/dashboard/mercados/incidencias/" + id;
    }

    // 4.7. Descargar o Visualizar Evidencia
    @GetMapping("/incidencias/evidencias/{evidenciaId}/descargar")
    @PreAuthorize("hasAuthority('MERCADOS_INCIDENCIA_VER') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Resource> descargarEvidencia(@PathVariable UUID evidenciaId) {
        EvidenciaIncidenciaMercado ev = evidenciaRepository.findById(evidenciaId)
                .orElseThrow(() -> new ResourceNotFoundException("Evidencia no encontrada: " + evidenciaId));
        try {
            Path path = Paths.get(ev.getRutaArchivo());
            if (!Files.exists(path)) {
                return ResponseEntity.notFound().build();
            }
            Resource resource = new UrlResource(path.toUri());
            String mediaType = ev.getContentType() != null ? ev.getContentType() : "application/octet-stream";
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + ev.getNombreArchivo() + "\"")
                    .contentType(MediaType.parseMediaType(mediaType))
                    .body(resource);
        } catch (Exception e) {
            log.error("Error al descargar evidencia {}", evidenciaId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // 5. Consola de Seguimiento
    @GetMapping("/incidencias/seguimiento")
    @PreAuthorize("hasAuthority('MERCADOS_INCIDENCIA_VER')")
    public String seguimiento(Model model) {
        Pageable pageable = PageRequest.of(0, 50, Sort.by("fechaReporte").descending());
        Page<IncidenciaResponse> activas = incidenciaService.buscarPaginado(
                null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, pageable);

        model.addAttribute("incidencias", activas.getContent());
        model.addAttribute("mercados", mercadoRepository.findAll());
        model.addAttribute("prioridades", PrioridadIncidencia.values());
        return "admin/mercados/incidencias/seguimiento";
    }

    // 6. Consola de Reportes
    @GetMapping("/incidencias/reportes")
    @PreAuthorize("hasAuthority('MERCADOS_INCIDENCIA_REPORTES')")
    public String reportes(Model model) {
        model.addAttribute("mercados", mercadoRepository.findAll());
        model.addAttribute("tipos", tipoService.listarActivos());
        return "admin/mercados/incidencias/reportes";
    }

    // 7. Descarga Excel
    @GetMapping("/incidencias/reportes/excel")
    @PreAuthorize("hasAuthority('MERCADOS_INCIDENCIA_REPORTES') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<byte[]> descargarExcel(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {

        byte[] bytes = reporteService.exportarIncidenciasExcel(desde, hasta);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"reporte_incidencias_mercados.xlsx\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }

    // 8. Descarga PDF
    @GetMapping("/incidencias/reportes/pdf")
    @PreAuthorize("hasAuthority('MERCADOS_INCIDENCIA_REPORTES') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<byte[]> descargarPdf(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {

        byte[] bytes = reporteService.exportarIncidenciasPdf(desde, hasta);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"reporte_incidencias_mercados.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(bytes);
    }

    // 9. Presuntas Infracciones - Bandeja Web
    @GetMapping("/infracciones")
    @PreAuthorize("hasAuthority('MERCADOS_INFRACCION_VER')")
    public String listarInfracciones(
            @RequestParam(required = false) UUID mercadoId,
            @RequestParam(required = false) UUID tipificacionId,
            @RequestParam(required = false) EstadoEvaluacionInfraccion estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaDeteccion").descending());
        Page<PresuntaInfraccionResponse> infraccionesPage = infraccionService.buscarPaginado(
                mercadoId, null, null, tipificacionId, estado, fechaDesde, fechaHasta, q, pageable);

        model.addAttribute("infraccionesPage", infraccionesPage);
        model.addAttribute("mercados", mercadoRepository.findAll());
        model.addAttribute("tipificaciones", tipificacionService.listarVigentes());
        model.addAttribute("estados", EstadoEvaluacionInfraccion.values());
        model.addAttribute("mercadoId", mercadoId);
        model.addAttribute("tipificacionId", tipificacionId);
        model.addAttribute("estado", estado);
        model.addAttribute("fechaDesde", fechaDesde);
        model.addAttribute("fechaHasta", fechaHasta);
        model.addAttribute("q", q);

        return "admin/mercados/infracciones/listado";
    }

    // 10. Presuntas Infracciones - Formulario de Registro
    @GetMapping("/infracciones/nueva")
    @PreAuthorize("hasAuthority('MERCADOS_INFRACCION_REGISTRAR')")
    public String formularioNuevaInfraccion(Model model) {
        model.addAttribute("mercados", mercadoRepository.findAll());
        model.addAttribute("tipificaciones", tipificacionService.listarVigentes());
        return "admin/mercados/infracciones/formulario";
    }

    // 11. Presuntas Infracciones - Detalle y Evaluación
    @GetMapping("/infracciones/{id}")
    @PreAuthorize("hasAuthority('MERCADOS_INFRACCION_VER')")
    public String detalleInfraccion(@PathVariable UUID id, Model model) {
        PresuntaInfraccionResponse infraccion = infraccionService.obtenerPorId(id);
        model.addAttribute("infraccion", infraccion);
        model.addAttribute("estadosEvaluacion", EstadoEvaluacionInfraccion.values());
        return "admin/mercados/infracciones/detalle";
    }
}
