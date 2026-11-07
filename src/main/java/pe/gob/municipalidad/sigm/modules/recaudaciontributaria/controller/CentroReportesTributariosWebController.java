package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.reportes.KpiDashboardTributarioDto;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.reportes.ReporteFilterRequest;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.reportes.ReporteTributarioDtos.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoMetaRecaudacion;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.FormatoExportacionReporte;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.*;
import pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Controlador MVC para el Centro de Reportes, Inteligencia Tributaria y Estadísticas de Quinua.
 */
@Slf4j
@Controller
@RequestMapping("/dashboard/tributaria/reportes")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('TRIBUTARIA_REPORTES_ACCEDER') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_TRIBUTACION')")
public class CentroReportesTributariosWebController {

    private final CentroReportesTributariosService centroService;
    private final IndicadoresRecaudacionService indicadoresService;
    private final ReporteRecaudacionService recaudacionService;
    private final ReporteCarteraTributariaService carteraService;
    private final ReporteMorosidadService morosidadService;
    private final MetaRecaudacionService metaService;
    private final ExportacionTributariaService exportacionService;
    private final UsuarioRepository usuarioRepository;

    @GetMapping
    public String dashboardPrincipal(
            @ModelAttribute ReporteFilterRequest filtro,
            Model model) {

        int ejercicio = (filtro.getEjercicio() != null && filtro.getEjercicio() > 2000) ?
                filtro.getEjercicio() : LocalDate.now().getYear();

        KpiDashboardTributarioDto kpis = centroService.obtenerResumenEjecutivo();
        List<RecaudacionPeriodoItemDto> recaudacionMensual = recaudacionService.obtenerRecaudacionMensualEjercicio(ejercicio);
        List<RecaudacionConceptoItemDto> recaudacionConceptos = recaudacionService.obtenerRecaudacionPorConcepto(filtro);
        List<RecaudacionMedioItemDto> recaudacionMedios = recaudacionService.obtenerRecaudacionPorMedioPago(filtro);
        List<CarteraTramoItemDto> carteraTramos = carteraService.obtenerDistribucionPorTramosAntiguedad();

        model.addAttribute("kpis", kpis);
        model.addAttribute("recaudacionMensual", recaudacionMensual);
        model.addAttribute("recaudacionConceptos", recaudacionConceptos);
        model.addAttribute("recaudacionMedios", recaudacionMedios);
        model.addAttribute("carteraTramos", carteraTramos);
        model.addAttribute("catalogoReportes", centroService.obtenerCatalogoReportes());
        model.addAttribute("ultimasExportaciones", centroService.obtenerUltimasExportaciones());
        model.addAttribute("filtro", filtro);
        model.addAttribute("ejercicioActual", ejercicio);
        model.addAttribute("pestanaActiva", "resumen");
        model.addAttribute("moduloActivo", "tributaria");
        model.addAttribute("submenuActivo", "reportes");

        return "admin/tributaria/reportes/dashboard";
    }

    @GetMapping("/recaudacion")
    public String reporteRecaudacion(
            @ModelAttribute ReporteFilterRequest filtro,
            Model model) {

        int ejercicio = (filtro.getEjercicio() != null && filtro.getEjercicio() > 2000) ?
                filtro.getEjercicio() : LocalDate.now().getYear();

        model.addAttribute("kpis", centroService.obtenerResumenEjecutivo());
        model.addAttribute("recaudacionMensual", recaudacionService.obtenerRecaudacionMensualEjercicio(ejercicio));
        model.addAttribute("recaudacionConceptos", recaudacionService.obtenerRecaudacionPorConcepto(filtro));
        model.addAttribute("recaudacionMedios", recaudacionService.obtenerRecaudacionPorMedioPago(filtro));
        model.addAttribute("resumenCajas", recaudacionService.obtenerResumenCajasMunicipales());
        model.addAttribute("filtro", filtro);
        model.addAttribute("pestanaActiva", "recaudacion");
        model.addAttribute("moduloActivo", "tributaria");
        model.addAttribute("submenuActivo", "reportes");

        return "admin/tributaria/reportes/recaudacion";
    }

    @GetMapping("/cartera")
    public String reporteCartera(Model model) {
        model.addAttribute("kpis", centroService.obtenerResumenEjecutivo());
        model.addAttribute("carteraTramos", carteraService.obtenerDistribucionPorTramosAntiguedad());
        model.addAttribute("pestanaActiva", "cartera");
        model.addAttribute("moduloActivo", "tributaria");
        model.addAttribute("submenuActivo", "reportes");

        return "admin/tributaria/reportes/cartera";
    }

    @GetMapping("/contribuyentes")
    public String reporteContribuyentes(Model model) {
        model.addAttribute("kpis", centroService.obtenerResumenEjecutivo());
        model.addAttribute("estadisticas", centroService.obtenerEstadisticasContribuyentes());
        model.addAttribute("pestanaActiva", "contribuyentes");
        model.addAttribute("moduloActivo", "tributaria");
        model.addAttribute("submenuActivo", "reportes");

        return "admin/tributaria/reportes/contribuyentes";
    }

    @GetMapping("/predios")
    public String reportePredios(Model model) {
        model.addAttribute("kpis", centroService.obtenerResumenEjecutivo());
        model.addAttribute("estadisticas", centroService.obtenerEstadisticasPredios());
        model.addAttribute("pestanaActiva", "predios");
        model.addAttribute("moduloActivo", "tributaria");
        model.addAttribute("submenuActivo", "reportes");

        return "admin/tributaria/reportes/predios";
    }

    @GetMapping("/fraccionamientos")
    public String reporteFraccionamientos(Model model) {
        model.addAttribute("kpis", centroService.obtenerResumenEjecutivo());
        model.addAttribute("resumen", centroService.obtenerEstadisticasFraccionamientos());
        model.addAttribute("pestanaActiva", "fraccionamientos");
        model.addAttribute("moduloActivo", "tributaria");
        model.addAttribute("submenuActivo", "reportes");

        return "admin/tributaria/reportes/fraccionamientos";
    }

    @GetMapping("/notificaciones")
    public String reporteNotificaciones(Model model) {
        model.addAttribute("kpis", centroService.obtenerResumenEjecutivo());
        model.addAttribute("estadisticas", centroService.obtenerEstadisticasNotificaciones());
        model.addAttribute("pestanaActiva", "notificaciones");
        model.addAttribute("moduloActivo", "tributaria");
        model.addAttribute("submenuActivo", "reportes");

        return "admin/tributaria/reportes/notificaciones";
    }

    @GetMapping("/metas")
    public String reporteMetas(
            @RequestParam(value = "ejercicio", required = false) Integer ejercicio,
            @RequestParam(value = "estado", required = false) EstadoMetaRecaudacion estado,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {

        int anio = (ejercicio != null && ejercicio > 2000) ? ejercicio : LocalDate.now().getYear();

        model.addAttribute("kpis", centroService.obtenerResumenEjecutivo());
        model.addAttribute("metas", metaService.listarMetas(anio, estado, PageRequest.of(page, 15)));
        model.addAttribute("ejercicio", anio);
        model.addAttribute("estado", estado);
        model.addAttribute("pestanaActiva", "metas");
        model.addAttribute("moduloActivo", "tributaria");
        model.addAttribute("submenuActivo", "reportes");

        return "admin/tributaria/reportes/metas";
    }

    @PostMapping("/metas")
    public String crearMeta(
            @ModelAttribute CrearMetaRecaudacionRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            Usuario autor = obtenerUsuario(userDetails);
            metaService.crearMeta(request, autor);
            redirectAttributes.addFlashAttribute("successMessage", "Meta de recaudación creada exitosamente.");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/dashboard/tributaria/reportes/metas?ejercicio=" + request.getEjercicio();
    }

    @PostMapping("/metas/{id}/aprobar")
    public String aprobarMeta(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            Usuario aprobador = obtenerUsuario(userDetails);
            metaService.aprobarMeta(id, aprobador);
            redirectAttributes.addFlashAttribute("successMessage", "Meta de recaudación aprobada satisfactoriamente.");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/dashboard/tributaria/reportes/metas";
    }

    @GetMapping("/exportaciones")
    public String historialExportaciones(Model model) {
        model.addAttribute("kpis", centroService.obtenerResumenEjecutivo());
        model.addAttribute("exportaciones", centroService.obtenerUltimasExportaciones());
        model.addAttribute("pestanaActiva", "exportaciones");
        model.addAttribute("moduloActivo", "tributaria");
        model.addAttribute("submenuActivo", "reportes");

        return "admin/tributaria/reportes/exportaciones";
    }

    @GetMapping("/exportar/pdf")
    public ResponseEntity<byte[]> exportarPdf(
            @ModelAttribute ReporteFilterRequest filtro,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Usuario usuario = obtenerUsuario(userDetails);
        byte[] pdfBytes = exportacionService.exportarPdfConsolidado(filtro, usuario);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"Reporte_Tributario_Quinua.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @GetMapping("/exportar/excel")
    public ResponseEntity<byte[]> exportarExcel(
            @ModelAttribute ReporteFilterRequest filtro,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Usuario usuario = obtenerUsuario(userDetails);
        byte[] excelBytes = exportacionService.exportarExcelConsolidado(filtro, usuario);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"Reporte_Tributario_Quinua.xlsx\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }

    private Usuario obtenerUsuario(CustomUserDetails userDetails) {
        if (userDetails != null && userDetails.getId() != null) {
            return usuarioRepository.findById(userDetails.getId()).orElse(null);
        }
        return null;
    }
}

