package pe.gob.municipalidad.sigm.modules.mesapartes.controller;

import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pe.gob.municipalidad.sigm.common.response.ApiResponse;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.ReporteDtos.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.*;

import java.util.List;

/**
 * Endpoints REST protegidos para reportes, estadísticas, indicadores y exportaciones de Mesa de Partes Virtual.
 */
@Slf4j
@RestController
@RequestMapping("/api/mesa-partes/reportes")
public class ReporteMesaPartesRestController {

    private final ReporteMesaPartesService reporteService;
    private final IndicadoresMesaPartesService indicadoresService;
    private final EstadisticasExpedienteService estadisticasExpedienteService;
    private final EstadisticasOficinaService estadisticasOficinaService;
    private final EstadisticasTramiteService estadisticasTramiteService;
    private final SupervisionMesaPartesService supervisionService;
    private final ExportacionExcelService exportacionExcelService;
    private final ExportacionPdfService exportacionPdfService;

    public ReporteMesaPartesRestController(ReporteMesaPartesService reporteService,
                                           IndicadoresMesaPartesService indicadoresService,
                                           EstadisticasExpedienteService estadisticasExpedienteService,
                                           EstadisticasOficinaService estadisticasOficinaService,
                                           EstadisticasTramiteService estadisticasTramiteService,
                                           SupervisionMesaPartesService supervisionService,
                                           ExportacionExcelService exportacionExcelService,
                                           ExportacionPdfService exportacionPdfService) {
        this.reporteService = reporteService;
        this.indicadoresService = indicadoresService;
        this.estadisticasExpedienteService = estadisticasExpedienteService;
        this.estadisticasOficinaService = estadisticasOficinaService;
        this.estadisticasTramiteService = estadisticasTramiteService;
        this.supervisionService = supervisionService;
        this.exportacionExcelService = exportacionExcelService;
        this.exportacionPdfService = exportacionPdfService;
    }

    /**
     * Resumen cuantitativo de estados para tarjetas de control.
     */
    @GetMapping("/resumen")
    @PreAuthorize("hasAuthority('MESAPARTES_REPORTES_VER') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA', 'MESA_PARTES', 'MESA_PARTES_OPERADOR')")
    public ResponseEntity<ApiResponse<ResumenEstadisticoDto>> obtenerResumen(
            @ModelAttribute FiltroReporteRequest filtro,
            Authentication auth
    ) {
        ResumenEstadisticoDto resumen = reporteService.obtenerResumen(filtro, auth.getName());
        return ApiResponse.ok("Resumen estadístico obtenido con éxito", resumen);
    }

    /**
     * Indicadores de eficiencia operativa y tiempos de atención.
     */
    @GetMapping("/eficiencia")
    @PreAuthorize("hasAuthority('MESAPARTES_REPORTES_VER') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA', 'MESA_PARTES', 'MESA_PARTES_OPERADOR')")
    public ResponseEntity<ApiResponse<IndicadoresEficienciaDto>> obtenerIndicadoresEficiencia(
            @ModelAttribute FiltroReporteRequest filtro,
            Authentication auth
    ) {
        IndicadoresEficienciaDto dto = indicadoresService.calcularIndicadoresEficiencia(filtro, auth.getName());
        return ApiResponse.ok("Indicadores de eficiencia calculados con éxito", dto);
    }

    /**
     * Indicadores de control de plazos legales (TUO Ley N° 27444).
     */
    @GetMapping("/plazos")
    @PreAuthorize("hasAuthority('MESAPARTES_REPORTES_VER') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA', 'MESA_PARTES', 'MESA_PARTES_OPERADOR')")
    public ResponseEntity<ApiResponse<IndicadoresPlazosDto>> obtenerIndicadoresPlazos(
            @ModelAttribute FiltroReporteRequest filtro,
            Authentication auth
    ) {
        IndicadoresPlazosDto dto = indicadoresService.calcularIndicadoresPlazos(filtro, auth.getName());
        return ApiResponse.ok("Indicadores de plazos obtenidos con éxito", dto);
    }

    /**
     * Gráfico de expedientes por estado (Doughnut / Pie).
     */
    @GetMapping("/por-estado")
    @PreAuthorize("hasAuthority('MESAPARTES_ESTADISTICAS_VER') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA', 'MESA_PARTES', 'MESA_PARTES_OPERADOR')")
    public ResponseEntity<ApiResponse<GraficoEstadoDto>> obtenerGraficoEstado(
            @ModelAttribute FiltroReporteRequest filtro,
            Authentication auth
    ) {
        GraficoEstadoDto dto = estadisticasExpedienteService.obtenerGraficoEstado(filtro, auth.getName());
        return ApiResponse.ok("Datos de gráfico por estado obtenidos con éxito", dto);
    }

    /**
     * Gráfico de evolución mensual cronológica.
     */
    @GetMapping("/evolucion")
    @PreAuthorize("hasAuthority('MESAPARTES_ESTADISTICAS_VER') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA', 'MESA_PARTES', 'MESA_PARTES_OPERADOR')")
    public ResponseEntity<ApiResponse<EvolucionMensualDto>> obtenerEvolucionMensual(
            @RequestParam(value = "anio", required = false) Integer anio,
            @RequestParam(value = "oficinaId", required = false) Long oficinaId,
            Authentication auth
    ) {
        EvolucionMensualDto dto = estadisticasExpedienteService.obtenerEvolucionMensual(anio, oficinaId, auth.getName());
        return ApiResponse.ok("Evolución mensual obtenida con éxito", dto);
    }

    /**
     * Gráfico y desglose por unidad orgánica municipal.
     */
    @GetMapping("/por-oficina")
    @PreAuthorize("hasAuthority('MESAPARTES_REPORTES_VER') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA', 'MESA_PARTES', 'MESA_PARTES_OPERADOR')")
    public ResponseEntity<ApiResponse<List<EstadisticaOficinaDto>>> obtenerPorOficina(
            @ModelAttribute FiltroReporteRequest filtro,
            Authentication auth
    ) {
        List<EstadisticaOficinaDto> lista = estadisticasOficinaService.obtenerEstadisticasPorOficina(filtro, auth.getName());
        return ApiResponse.ok("Estadísticas por oficina obtenidas con éxito", lista);
    }

    /**
     * Gráfico y desglose por tipo de procedimiento administrativo.
     */
    @GetMapping("/por-tramite")
    @PreAuthorize("hasAuthority('MESAPARTES_REPORTES_VER') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA', 'MESA_PARTES', 'MESA_PARTES_OPERADOR')")
    public ResponseEntity<ApiResponse<List<EstadisticaTramiteDto>>> obtenerPorTramite(
            @ModelAttribute FiltroReporteRequest filtro,
            Authentication auth
    ) {
        List<EstadisticaTramiteDto> lista = estadisticasTramiteService.obtenerEstadisticasPorTramite(filtro, auth.getName());
        return ApiResponse.ok("Estadísticas por tipo de trámite obtenidas con éxito", lista);
    }

    /**
     * Comparativa de períodos (mes, trimestre o año) con variación absoluta y porcentual.
     */
    @GetMapping("/comparacion")
    @PreAuthorize("hasAuthority('MESAPARTES_REPORTES_VER') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA', 'MESA_PARTES', 'MESA_PARTES_OPERADOR')")
    public ResponseEntity<ApiResponse<ComparacionPeriodoDto>> compararPeriodos(
            @RequestParam(value = "periodo", defaultValue = "MES") String periodo,
            @RequestParam(value = "oficinaId", required = false) Long oficinaId,
            Authentication auth
    ) {
        ComparacionPeriodoDto dto = estadisticasExpedienteService.compararPeriodos(periodo, oficinaId, auth.getName());
        return ApiResponse.ok("Comparación de períodos realizada con éxito", dto);
    }

    /**
     * Supervisión de derivaciones entre oficinas.
     */
    @GetMapping("/derivaciones")
    @PreAuthorize("hasAuthority('MESAPARTES_SUPERVISION_VER') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA')")
    public ResponseEntity<ApiResponse<AnalisisDerivacionesDto>> analizarDerivaciones(
            @ModelAttribute FiltroReporteRequest filtro,
            Authentication auth
    ) {
        AnalisisDerivacionesDto dto = supervisionService.analizarDerivaciones(filtro, auth.getName());
        return ApiResponse.ok("Análisis de derivaciones obtenido con éxito", dto);
    }

    /**
     * Supervisión de observaciones y subsanaciones de admisibilidad.
     */
    @GetMapping("/observaciones")
    @PreAuthorize("hasAuthority('MESAPARTES_SUPERVISION_VER') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA')")
    public ResponseEntity<ApiResponse<AnalisisObservacionesDto>> analizarObservaciones(
            @ModelAttribute FiltroReporteRequest filtro,
            Authentication auth
    ) {
        AnalisisObservacionesDto dto = supervisionService.analizarObservaciones(filtro, auth.getName());
        return ApiResponse.ok("Análisis de observaciones obtenido con éxito", dto);
    }

    /**
     * Supervisión agregada de carga y productividad operativa por oficina.
     */
    @GetMapping("/productividad")
    @PreAuthorize("hasAuthority('MESAPARTES_SUPERVISION_VER') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA')")
    public ResponseEntity<ApiResponse<ProductividadOperativaDto>> obtenerProductividad(
            @ModelAttribute FiltroReporteRequest filtro,
            Authentication auth
    ) {
        ProductividadOperativaDto dto = supervisionService.obtenerProductividadOperativa(filtro, auth.getName());
        return ApiResponse.ok("Productividad operativa obtenida con éxito", dto);
    }

    /**
     * Lista paginada y filtrada de expedientes para la tabla detallada.
     */
    @GetMapping("/expedientes")
    @PreAuthorize("hasAuthority('MESAPARTES_REPORTES_VER') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA', 'MESA_PARTES', 'MESA_PARTES_OPERADOR')")
    public ResponseEntity<ApiResponse<Page<ExpedienteReporteItemDto>>> listarExpedientes(
            @ModelAttribute FiltroReporteRequest filtro,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "15") int size,
            Authentication auth
    ) {
        Pageable pageable = PageRequest.of(Math.max(0, page), size, Sort.by("createdAt").descending());
        Page<ExpedienteReporteItemDto> resultado = reporteService.listarExpedientes(filtro, pageable, auth.getName());
        return ApiResponse.ok("Expedientes para reporte obtenidos con éxito", resultado);
    }

    /**
     * Historial de auditoría de exportaciones efectuadas.
     */
    @GetMapping("/historial-exportaciones")
    @PreAuthorize("hasAuthority('MESAPARTES_REPORTES_EXPORTAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA')")
    public ResponseEntity<ApiResponse<Page<ExportacionHistorialDto>>> obtenerHistorialExportaciones(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Authentication auth
    ) {
        Pageable pageable = PageRequest.of(Math.max(0, page), size, Sort.by("fechaExportacion").descending());
        Page<ExportacionHistorialDto> historial = reporteService.obtenerHistorialExportaciones(pageable, auth.getName());
        return ApiResponse.ok("Historial de exportaciones obtenido con éxito", historial);
    }

    /**
     * Descarga de reporte en formato Excel (XLSX).
     */
    @GetMapping("/exportar/excel")
    @PreAuthorize("hasAuthority('MESAPARTES_REPORTES_EXPORTAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA', 'MESA_PARTES', 'MESA_PARTES_OPERADOR')")
    public ResponseEntity<byte[]> exportarExcel(
            @ModelAttribute FiltroReporteRequest filtro,
            HttpServletRequest request,
            Authentication auth
    ) {
        String ip = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");

        byte[] bytes = exportacionExcelService.exportarReporteExcel(filtro, auth.getName(), ip, userAgent);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", "Reporte-MesaPartes-" + System.currentTimeMillis() + ".xlsx");
        headers.setContentLength(bytes.length);

        return ResponseEntity.ok().headers(headers).body(bytes);
    }

    /**
     * Descarga de reporte en formato PDF institucional.
     */
    @GetMapping("/exportar/pdf")
    @PreAuthorize("hasAuthority('MESAPARTES_REPORTES_EXPORTAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA', 'MESA_PARTES', 'MESA_PARTES_OPERADOR')")
    public ResponseEntity<byte[]> exportarPdf(
            @ModelAttribute FiltroReporteRequest filtro,
            HttpServletRequest request,
            Authentication auth
    ) {
        String ip = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");

        byte[] bytes = exportacionPdfService.exportarReportePdf(filtro, auth.getName(), ip, userAgent);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("inline", "Reporte-MesaPartes-" + System.currentTimeMillis() + ".pdf");
        headers.setContentLength(bytes.length);

        return ResponseEntity.ok().headers(headers).body(bytes);
    }
}
