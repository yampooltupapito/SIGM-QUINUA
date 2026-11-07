package pe.gob.municipalidad.sigm.modules.mercados.controller.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import pe.gob.municipalidad.sigm.modules.mercados.dto.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.FormatoExportacion;
import pe.gob.municipalidad.sigm.modules.mercados.service.*;
import pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails;

import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/mercados/reportes")
public class ReporteMercadosApiController {

    private final IndicadoresMercadosService indicadoresService;
    private final ReporteOcupacionService reporteOcupacionService;
    private final ReporteMercadosService reporteMercadosService;
    private final ReporteRecaudacionService reporteRecaudacionService;
    private final ReporteOperativoService reporteOperativoService;
    private final ExportacionExcelService exportacionExcelService;
    private final ExportacionPdfService exportacionPdfService;
    private final HistorialExportacionService historialExportacionService;

    public ReporteMercadosApiController(
            IndicadoresMercadosService indicadoresService,
            ReporteOcupacionService reporteOcupacionService,
            ReporteMercadosService reporteMercadosService,
            ReporteRecaudacionService reporteRecaudacionService,
            ReporteOperativoService reporteOperativoService,
            @org.springframework.beans.factory.annotation.Qualifier("mercadoExportacionExcelServiceImpl") ExportacionExcelService exportacionExcelService,
            @org.springframework.beans.factory.annotation.Qualifier("mercadoExportacionPdfServiceImpl") ExportacionPdfService exportacionPdfService,
            HistorialExportacionService historialExportacionService) {
        this.indicadoresService = indicadoresService;
        this.reporteOcupacionService = reporteOcupacionService;
        this.reporteMercadosService = reporteMercadosService;
        this.reporteRecaudacionService = reporteRecaudacionService;
        this.reporteOperativoService = reporteOperativoService;
        this.exportacionExcelService = exportacionExcelService;
        this.exportacionPdfService = exportacionPdfService;
        this.historialExportacionService = historialExportacionService;
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasAuthority('MERCADOS_REPORTES_VER') or hasAuthority('MERCADOS_REPORTES_EJECUTIVO') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<DashboardMercadosResponse> obtenerDashboard(ReporteMercadoFilter filtro) {
        return ResponseEntity.ok(indicadoresService.obtenerDashboard(filtro));
    }

    @GetMapping("/ocupacion")
    @PreAuthorize("hasAuthority('MERCADOS_REPORTES_VER') or hasAuthority('MERCADOS_REPORTES_OCUPACION') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<IndicadorOcupacionResponse> obtenerOcupacion(ReporteMercadoFilter filtro) {
        return ResponseEntity.ok(reporteOcupacionService.obtenerIndicadorOcupacion(filtro));
    }

    @GetMapping("/puestos")
    @PreAuthorize("hasAuthority('MERCADOS_REPORTES_VER') or hasAuthority('MERCADOS_REPORTES_OCUPACION') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Page<PuestoReporteItem>> listarPuestos(
            ReporteMercadoFilter filtro,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(reporteOcupacionService.listarPuestos(filtro, pageable));
    }

    @GetMapping("/comerciantes")
    @PreAuthorize("hasAuthority('MERCADOS_REPORTES_VER') or hasAuthority('MERCADOS_REPORTES_COMERCIANTES') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Page<ComercianteReporteItem>> listarComerciantes(
            ReporteMercadoFilter filtro,
            @PageableDefault(size = 20) Pageable pageable,
            Authentication authentication) {
        boolean puedeVerDoc = tienePermisoDocumentos(authentication);
        return ResponseEntity.ok(reporteMercadosService.listarComerciantes(filtro, pageable, puedeVerDoc));
    }

    @GetMapping("/asignaciones")
    @PreAuthorize("hasAuthority('MERCADOS_REPORTES_VER') or hasAuthority('MERCADOS_REPORTES_CONTRATOS') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Page<ContratoReporteItem>> listarAsignaciones(
            ReporteMercadoFilter filtro,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(reporteMercadosService.listarContratos(filtro, pageable));
    }

    @GetMapping("/contratos")
    @PreAuthorize("hasAuthority('MERCADOS_REPORTES_VER') or hasAuthority('MERCADOS_REPORTES_CONTRATOS') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Page<ContratoReporteItem>> listarContratos(
            ReporteMercadoFilter filtro,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(reporteMercadosService.listarContratos(filtro, pageable));
    }

    @GetMapping("/autorizaciones")
    @PreAuthorize("hasAuthority('MERCADOS_REPORTES_VER') or hasAuthority('MERCADOS_REPORTES_CONTRATOS') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Page<ContratoReporteItem>> listarAutorizaciones(
            ReporteMercadoFilter filtro,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(reporteMercadosService.listarAutorizaciones(filtro, pageable));
    }

    @GetMapping("/vencimientos")
    @PreAuthorize("hasAuthority('MERCADOS_REPORTES_VER') or hasAuthority('MERCADOS_REPORTES_CONTRATOS') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Page<ContratoReporteItem>> listarVencimientos(
            ReporteMercadoFilter filtro,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(reporteMercadosService.listarVencimientos(filtro, pageable));
    }

    @GetMapping("/obligaciones")
    @PreAuthorize("hasAuthority('MERCADOS_REPORTES_FINANZAS') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Page<DeudaReporteItem>> listarObligaciones(
            ReporteMercadoFilter filtro,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(reporteRecaudacionService.listarDeudas(filtro, pageable));
    }

    @GetMapping("/recaudacion")
    @PreAuthorize("hasAuthority('MERCADOS_REPORTES_FINANZAS') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Page<RecaudacionReporteItem>> listarRecaudacion(
            ReporteMercadoFilter filtro,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(reporteRecaudacionService.listarRecaudacion(filtro, pageable));
    }

    @GetMapping("/deudas")
    @PreAuthorize("hasAuthority('MERCADOS_REPORTES_FINANZAS') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Page<DeudaReporteItem>> listarDeudas(
            ReporteMercadoFilter filtro,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(reporteRecaudacionService.listarDeudas(filtro, pageable));
    }

    @GetMapping("/deudas/resumen")
    @PreAuthorize("hasAuthority('MERCADOS_REPORTES_FINANZAS') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Map<String, Object>> obtenerResumenDeudas(ReporteMercadoFilter filtro) {
        return ResponseEntity.ok(reporteRecaudacionService.obtenerResumenDeudas(filtro));
    }

    @GetMapping("/inspecciones")
    @PreAuthorize("hasAuthority('MERCADOS_REPORTES_VER') or hasAuthority('MERCADOS_REPORTES_INSPECCIONES') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Page<InspeccionReporteItem>> listarInspecciones(
            ReporteMercadoFilter filtro,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(reporteOperativoService.listarInspecciones(filtro, pageable));
    }

    @GetMapping("/incidencias")
    @PreAuthorize("hasAuthority('MERCADOS_REPORTES_VER') or hasAuthority('MERCADOS_REPORTES_INCIDENCIAS') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Page<IncidenciaReporteItem>> listarIncidencias(
            ReporteMercadoFilter filtro,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(reporteOperativoService.listarIncidencias(filtro, pageable));
    }

    @GetMapping("/renovaciones")
    @PreAuthorize("hasAuthority('MERCADOS_REPORTES_VER') or hasAuthority('MERCADOS_REPORTES_CONTRATOS') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Page<RenovacionReporteItem>> listarRenovaciones(
            ReporteMercadoFilter filtro,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(reporteOperativoService.listarRenovaciones(filtro, pageable));
    }

    @GetMapping("/comparacion")
    @PreAuthorize("hasAuthority('MERCADOS_REPORTES_VER') or hasAuthority('MERCADOS_REPORTES_EJECUTIVO') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<DashboardMercadosResponse> obtenerComparacion(ReporteMercadoFilter filtro) {
        return ResponseEntity.ok(indicadoresService.obtenerDashboard(filtro));
    }

    @PostMapping("/exportar/excel")
    @PreAuthorize("hasAuthority('MERCADOS_REPORTES_EXPORTAR_EXCEL') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<byte[]> exportarExcel(
            @Valid @RequestBody ExportacionReporteRequest request,
            Authentication authentication,
            HttpServletRequest servletRequest) {

        request.setFormato(FormatoExportacion.EXCEL);
        boolean puedeVerDoc = tienePermisoDocumentos(authentication);
        byte[] bytes = exportacionExcelService.generarReporteExcel(request.getTipoReporte(), request.getFiltro(), puedeVerDoc);

        String username = authentication != null ? authentication.getName() : "sistema";
        String ip = servletRequest.getRemoteAddr();
        String userAgent = servletRequest.getHeader("User-Agent");
        historialExportacionService.registrarExportacion(request, bytes, username, ip, userAgent);

        String filename = "REPORTE_" + request.getTipoReporte().name() + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }

    @PostMapping("/exportar/pdf")
    @PreAuthorize("hasAuthority('MERCADOS_REPORTES_EXPORTAR_PDF') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<byte[]> exportarPdf(
            @Valid @RequestBody ExportacionReporteRequest request,
            Authentication authentication,
            HttpServletRequest servletRequest) {

        request.setFormato(FormatoExportacion.PDF);
        boolean puedeVerDoc = tienePermisoDocumentos(authentication);
        byte[] bytes = exportacionPdfService.generarReportePdf(request.getTipoReporte(), request.getFiltro(), puedeVerDoc);

        String username = authentication != null ? authentication.getName() : "sistema";
        String ip = servletRequest.getRemoteAddr();
        String userAgent = servletRequest.getHeader("User-Agent");
        historialExportacionService.registrarExportacion(request, bytes, username, ip, userAgent);

        String filename = "REPORTE_" + request.getTipoReporte().name() + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(bytes);
    }

    @GetMapping("/exportaciones")
    @PreAuthorize("hasAuthority('MERCADOS_REPORTES_HISTORIAL_VER') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Page<HistorialExportacionResponse>> listarHistorial(
            @PageableDefault(size = 15) Pageable pageable,
            Authentication authentication) {
        String username = authentication != null ? authentication.getName() : "sistema";
        boolean esAdmin = esAdministrador(authentication);
        return ResponseEntity.ok(historialExportacionService.listarHistorial(pageable, username, esAdmin));
    }

    @GetMapping("/exportaciones/{id}/descargar")
    @PreAuthorize("hasAuthority('MERCADOS_REPORTES_HISTORIAL_VER') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<byte[]> descargarArchivo(
            @PathVariable UUID id,
            Authentication authentication) {
        String username = authentication != null ? authentication.getName() : "sistema";
        boolean esAdmin = esAdministrador(authentication);
        byte[] bytes = historialExportacionService.descargarArchivo(id, username, esAdmin);

        var reg = historialExportacionService.obtenerRegistro(id);
        String contentType = reg.getMimeType() != null ? reg.getMimeType() : MediaType.APPLICATION_OCTET_STREAM_VALUE;

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + reg.getNombreArchivo() + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .body(bytes);
    }

    private boolean tienePermisoDocumentos(Authentication authentication) {
        if (authentication == null) return false;
        return authentication.getAuthorities().stream().anyMatch(a ->
                a.getAuthority().equals("ROLE_ADMIN") ||
                a.getAuthority().equals("ROLE_SUPERADMIN") ||
                a.getAuthority().equals("MERCADOS_COMERCIANTE_VER") ||
                a.getAuthority().equals("MERCADOS_REPORTES_COMERCIANTES"));
    }

    private boolean esAdministrador(Authentication authentication) {
        if (authentication == null) return false;
        return authentication.getAuthorities().stream().anyMatch(a ->
                a.getAuthority().equals("ROLE_ADMIN") ||
                a.getAuthority().equals("ROLE_SUPERADMIN"));
    }
}
