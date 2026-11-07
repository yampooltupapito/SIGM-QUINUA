package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.controller;

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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.PeriodoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoObligacionTributaria;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoDeterminacionTributaria;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.PeriodoTributarioRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.ObligacionExportService;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.ObligacionTributariaService;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Controller
@RequestMapping("/dashboard/tributaria/obligaciones")
@RequiredArgsConstructor
public class ObligacionWebController {

    private final ObligacionTributariaService obligacionService;
    private final ObligacionExportService exportService;
    private final PeriodoTributarioRepository periodoRepository;

    @GetMapping
    @PreAuthorize("hasAuthority('TRIBUTARIA_OBLIGACIONES_VER') or hasRole('ADMIN')")
    public String listarObligaciones(
            @ModelAttribute("filtro") ObligacionFilterDto filtro,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            Model model
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "fechaRegistro"));
        Page<ObligacionSummaryDto> obligaciones = obligacionService.listarObligaciones(filtro, pageable);
        Map<String, Object> metricas = obligacionService.obtenerMetricasGenerales();

        model.addAttribute("obligaciones", obligaciones);
        model.addAttribute("metricas", metricas);
        model.addAttribute("periodos", periodoRepository.findAll(Sort.by(Sort.Direction.DESC, "anio")));
        model.addAttribute("estados", EstadoObligacionTributaria.values());
        model.addAttribute("tiposDeterminacion", TipoDeterminacionTributaria.values());
        model.addAttribute("filtro", filtro != null ? filtro : new ObligacionFilterDto());

        return "admin/tributaria/obligaciones/list";
    }

    @GetMapping("/generar")
    @PreAuthorize("hasAuthority('TRIBUTARIA_OBLIGACIONES_GENERAR') or hasRole('ADMIN')")
    public String formularioGenerar(Model model) {
        model.addAttribute("request", new GeneracionObligacionRequest());
        model.addAttribute("tiposDeterminacion", TipoDeterminacionTributaria.values());
        model.addAttribute("periodos", periodoRepository.findAll(Sort.by(Sort.Direction.DESC, "anio")));
        return "admin/tributaria/obligaciones/generar";
    }

    @PostMapping("/generar")
    @PreAuthorize("hasAuthority('TRIBUTARIA_OBLIGACIONES_GENERAR') or hasRole('ADMIN')")
    public String procesarGeneracion(
            @ModelAttribute GeneracionObligacionRequest request,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {
        try {
            String usuario = userDetails != null ? userDetails.getUsername() : "SISTEMA";
            ObligacionDetailResponse response = obligacionService.generarDesdeDeterminacion(request, usuario);
            redirectAttributes.addFlashAttribute("mensajeExito", "Obligación tributaria generada exitosamente: " + response.getCodigoObligacion());
            return "redirect:/dashboard/tributaria/obligaciones/" + response.getId();
        } catch (Exception e) {
            log.error("Error al generar obligación individual: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("mensajeError", "No se pudo generar la obligación: " + e.getMessage());
            return "redirect:/dashboard/tributaria/obligaciones/generar";
        }
    }

    @GetMapping("/generar-lote")
    @PreAuthorize("hasAuthority('TRIBUTARIA_OBLIGACIONES_GENERAR_LOTE') or hasRole('ADMIN')")
    public String vistaGenerarLote(Model model) {
        model.addAttribute("request", new GeneracionLoteRequest());
        model.addAttribute("tiposDeterminacion", TipoDeterminacionTributaria.values());
        model.addAttribute("periodos", periodoRepository.findAll(Sort.by(Sort.Direction.DESC, "anio")));
        return "admin/tributaria/obligaciones/lote";
    }

    @PostMapping("/generar-lote")
    @PreAuthorize("hasAuthority('TRIBUTARIA_OBLIGACIONES_GENERAR_LOTE') or hasRole('ADMIN')")
    public String procesarGenerarLote(
            @ModelAttribute GeneracionLoteRequest request,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {
        try {
            String usuario = userDetails != null ? userDetails.getUsername() : "SISTEMA";
            LoteGeneracionResponse response = obligacionService.generarLote(request, usuario);
            redirectAttributes.addFlashAttribute("mensajeExito",
                    String.format("Lote %s procesado con éxito. Generadas: %d, Duplicadas: %d, Errores: %d. Total: S/ %s",
                            response.getCodigoLote(), response.getTotalGeneradas(), response.getTotalDuplicadas(),
                            response.getTotalErrores(), response.getImporteTotalGenerado()));
            redirectAttributes.addFlashAttribute("loteResultado", response);
            return "redirect:/dashboard/tributaria/obligaciones/generar-lote";
        } catch (Exception e) {
            log.error("Error al procesar lote de obligaciones: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("mensajeError", "Error al procesar el lote: " + e.getMessage());
            return "redirect:/dashboard/tributaria/obligaciones/generar-lote";
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('TRIBUTARIA_OBLIGACIONES_VER') or hasRole('ADMIN')")
    public String verDetalle(@PathVariable UUID id, Model model) {
        ObligacionDetailResponse obligacion = obligacionService.obtenerPorId(id);
        model.addAttribute("obligacion", obligacion);
        model.addAttribute("accionRequest", new AccionObligacionRequest());
        return "admin/tributaria/obligaciones/detail";
    }

    @PostMapping("/{id}/validar")
    @PreAuthorize("hasAuthority('TRIBUTARIA_OBLIGACIONES_VALIDAR') or hasRole('ADMIN')")
    public String validarObligacion(
            @PathVariable UUID id,
            @RequestParam(required = false) String resolucionSustento,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {
        try {
            String usuario = userDetails != null ? userDetails.getUsername() : "SISTEMA";
            obligacionService.validarObligacion(id, resolucionSustento, usuario);
            redirectAttributes.addFlashAttribute("mensajeExito", "Obligación tributaria validada y habilitada para exigibilidad formal.");
        } catch (Exception e) {
            log.error("Error al validar obligación {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("mensajeError", "Error al validar: " + e.getMessage());
        }
        return "redirect:/dashboard/tributaria/obligaciones/" + id;
    }

    @PostMapping("/{id}/suspender")
    @PreAuthorize("hasAuthority('TRIBUTARIA_OBLIGACIONES_SUSPENDER') or hasRole('ADMIN')")
    public String suspenderObligacion(
            @PathVariable UUID id,
            @ModelAttribute AccionObligacionRequest request,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {
        try {
            String usuario = userDetails != null ? userDetails.getUsername() : "SISTEMA";
            obligacionService.suspenderObligacion(id, request, usuario);
            redirectAttributes.addFlashAttribute("mensajeExito", "Exigibilidad de la obligación suspendida formalmente.");
        } catch (Exception e) {
            log.error("Error al suspender obligación {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("mensajeError", "Error al suspender: " + e.getMessage());
        }
        return "redirect:/dashboard/tributaria/obligaciones/" + id;
    }

    @PostMapping("/{id}/rectificar")
    @PreAuthorize("hasAuthority('TRIBUTARIA_OBLIGACIONES_RECTIFICAR') or hasRole('ADMIN')")
    public String rectificarObligacion(
            @PathVariable UUID id,
            @ModelAttribute AccionObligacionRequest request,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {
        try {
            String usuario = userDetails != null ? userDetails.getUsername() : "SISTEMA";
            ObligacionDetailResponse nueva = obligacionService.rectificarObligacion(id, request, usuario);
            redirectAttributes.addFlashAttribute("mensajeExito", "Obligación rectificada exitosamente. Nueva obligación: " + nueva.getCodigoObligacion());
            return "redirect:/dashboard/tributaria/obligaciones/" + nueva.getId();
        } catch (Exception e) {
            log.error("Error al rectificar obligación {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("mensajeError", "Error al rectificar: " + e.getMessage());
            return "redirect:/dashboard/tributaria/obligaciones/" + id;
        }
    }

    @PostMapping("/{id}/anular")
    @PreAuthorize("hasAuthority('TRIBUTARIA_OBLIGACIONES_ANULAR') or hasRole('ADMIN')")
    public String anularObligacion(
            @PathVariable UUID id,
            @ModelAttribute AccionObligacionRequest request,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {
        try {
            String usuario = userDetails != null ? userDetails.getUsername() : "SISTEMA";
            obligacionService.anularObligacion(id, request, usuario);
            redirectAttributes.addFlashAttribute("mensajeExito", "Obligación tributaria anulada formalmente con descargo de saldo en cuenta corriente.");
        } catch (Exception e) {
            log.error("Error al anular obligación {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("mensajeError", "Error al anular: " + e.getMessage());
        }
        return "redirect:/dashboard/tributaria/obligaciones/" + id;
    }

    @GetMapping("/exportar/excel")
    @PreAuthorize("hasAuthority('TRIBUTARIA_OBLIGACIONES_EXPORTAR') or hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportarExcel(@ModelAttribute ObligacionFilterDto filtro) {
        byte[] bytes = exportService.exportarPadronObligacionesExcel(filtro);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=padron_obligaciones_quinua.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }

    @GetMapping("/exportar/pdf")
    @PreAuthorize("hasAuthority('TRIBUTARIA_OBLIGACIONES_EXPORTAR') or hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportarPdf(@ModelAttribute ObligacionFilterDto filtro) {
        byte[] bytes = exportService.exportarPadronObligacionesPdf(filtro);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=padron_obligaciones_quinua.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(bytes);
    }

    @GetMapping("/{id}/pdf")
    @PreAuthorize("hasAuthority('TRIBUTARIA_OBLIGACIONES_EXPORTAR') or hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportarFichaPdf(@PathVariable UUID id) {
        byte[] bytes = exportService.exportarFichaObligacionPdf(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=ficha_obligacion_" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(bytes);
    }
}
