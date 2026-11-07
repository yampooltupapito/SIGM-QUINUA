package pe.gob.municipalidad.sigm.modules.serenazgo.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pe.gob.municipalidad.sigm.common.response.ApiResponse;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.estadisticas.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoReporteEstadistico;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.EstadisticasSerenazgoService;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.ReportesEstadisticosSerenazgoService;
import pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/serenazgo")
@RequiredArgsConstructor
public class EstadisticasRestController {

    private final EstadisticasSerenazgoService estadisticasService;
    private final ReportesEstadisticosSerenazgoService reportesService;

    // =========================================================================
    // Endpoints Analíticos y Estadísticos
    // =========================================================================

    @GetMapping("/estadisticas/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'SUPERVISOR_SERENAZGO') or hasAuthority('SERENAZGO_ESTADISTICAS_VER')")
    public ResponseEntity<ApiResponse<DashboardEstadisticoResponse>> obtenerDashboard(
            @ModelAttribute FiltroEstadisticoRequest filtro,
            @AuthenticationPrincipal CustomUserDetails user) {

        DashboardEstadisticoResponse response = estadisticasService.obtenerDashboardEstadistico(filtro, user);
        return ResponseEntity.ok(ApiResponse.of("Panel estadístico consultado exitosamente", response));
    }

    @GetMapping("/estadisticas/indicadores")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'SUPERVISOR_SERENAZGO') or hasAuthority('SERENAZGO_ESTADISTICAS_VER')")
    public ResponseEntity<ApiResponse<List<CatalogoIndicadorDto>>> obtenerCatalogoIndicadores() {
        List<CatalogoIndicadorDto> catalogo = estadisticasService.obtenerCatalogoIndicadores();
        return ResponseEntity.ok(ApiResponse.of("Catálogo técnico de indicadores obtenido", catalogo));
    }

    @GetMapping("/estadisticas/sectores")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'SUPERVISOR_SERENAZGO') or hasAuthority('SERENAZGO_ESTADISTICAS_VER_MAPAS')")
    public ResponseEntity<ApiResponse<List<EstadisticasTerritorialesDto>>> obtenerPorSector(
            @ModelAttribute FiltroEstadisticoRequest filtro) {

        List<EstadisticasTerritorialesDto> list = estadisticasService.obtenerEstadisticasPorSector(filtro);
        return ResponseEntity.ok(ApiResponse.of("Distribución territorial por sector obtenida", list));
    }

    @GetMapping("/estadisticas/tiempos")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'SUPERVISOR_SERENAZGO') or hasAuthority('SERENAZGO_ESTADISTICAS_VER_ATENCIONES')")
    public ResponseEntity<ApiResponse<TiemposAtencionEstadisticasDto>> obtenerTiemposAtencion(
            @ModelAttribute FiltroEstadisticoRequest filtro) {

        TiemposAtencionEstadisticasDto tiempos = estadisticasService.calcularTiemposAtencion(filtro);
        return ResponseEntity.ok(ApiResponse.of("Métricas y percentiles de tiempos calculados", tiempos));
    }

    @GetMapping("/estadisticas/patrullajes")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'SUPERVISOR_SERENAZGO') or hasAuthority('SERENAZGO_ESTADISTICAS_VER_PATRULLAJES')")
    public ResponseEntity<ApiResponse<EstadisticasPatrullajeDto>> obtenerEstadisticasPatrullaje(
            @ModelAttribute FiltroEstadisticoRequest filtro) {

        EstadisticasPatrullajeDto stats = estadisticasService.calcularEstadisticasPatrullaje(filtro);
        return ResponseEntity.ok(ApiResponse.of("Estadísticas de patrullajes calculadas", stats));
    }

    // =========================================================================
    // Endpoints de Generación y Descarga de Reportes
    // =========================================================================

    @PostMapping("/reportes/generar")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'SUPERVISOR_SERENAZGO') or hasAuthority('SERENAZGO_REPORTES_GENERAR')")
    public ResponseEntity<ApiResponse<ReporteHistorialDto>> generarReporte(
            @Valid @RequestBody GenerarReporteRequest req,
            @AuthenticationPrincipal CustomUserDetails user,
            HttpServletRequest request) {

        String ip = request.getRemoteAddr();
        ReporteHistorialDto rep = reportesService.generarReporte(req, user, ip);
        return ResponseEntity.ok(ApiResponse.of("Reporte generado satisfactoriamente", rep));
    }

    @GetMapping("/reportes/historial")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'SUPERVISOR_SERENAZGO') or hasAuthority('SERENAZGO_REPORTES_VER_HISTORIAL')")
    public ResponseEntity<ApiResponse<Page<ReporteHistorialDto>>> listarHistorial(
            @RequestParam(required = false) TipoReporteEstadistico tipo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<ReporteHistorialDto> p = reportesService.listarHistorialReportes(tipo, desde, hasta, pageable);
        return ResponseEntity.ok(ApiResponse.of("Historial de reportes recuperado", p));
    }

    @GetMapping("/reportes/{id}/descargar")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'SUPERVISOR_SERENAZGO') or hasAuthority('SERENAZGO_REPORTES_DESCARGAR')")
    public ResponseEntity<Resource> descargarReporte(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails user) {

        Resource res = reportesService.descargarReporte(id, user);
        var entidad = reportesService.obtenerPorId(id);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(entidad.getMimeType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + entidad.getNombreArchivo() + "\"")
                .body(res);
    }
}
