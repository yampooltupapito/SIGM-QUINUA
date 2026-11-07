package pe.gob.municipalidad.sigm.modules.mercados.controller.api;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.gob.municipalidad.sigm.modules.mercados.dto.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoAsignacion;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoAsignacion;
import pe.gob.municipalidad.sigm.modules.mercados.service.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/mercados/asignaciones")
public class AsignacionApiController {

    private final AsignacionPuestoService asignacionService;
    private final EvaluacionAsignacionService evaluacionService;
    private final OcupacionPuestoService ocupacionService;
    private final DisponibilidadPuestoService disponibilidadService;
    private final HistorialOcupacionService historialService;
    private final ConstanciaAsignacionService constanciaService;
    private final AsignacionExportService exportService;

    public AsignacionApiController(
            AsignacionPuestoService asignacionService,
            EvaluacionAsignacionService evaluacionService,
            OcupacionPuestoService ocupacionService,
            DisponibilidadPuestoService disponibilidadService,
            HistorialOcupacionService historialService,
            ConstanciaAsignacionService constanciaService,
            AsignacionExportService exportService) {
        this.asignacionService = asignacionService;
        this.evaluacionService = evaluacionService;
        this.ocupacionService = ocupacionService;
        this.disponibilidadService = disponibilidadService;
        this.historialService = historialService;
        this.constanciaService = constanciaService;
        this.exportService = exportService;
    }

    // ==========================================
    // LISTADO Y DETALLE
    // ==========================================

    @GetMapping
    @PreAuthorize("hasAuthority('MERCADOS_ASIGNACION_VER') or hasAuthority('MERCADOS_ASIGNACION_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'OPERADOR', 'JEFE_OFICINA')")
    public ResponseEntity<Page<AsignacionResponse>> listarAsignaciones(
            @RequestParam(required = false) String termino,
            @RequestParam(required = false) UUID mercadoId,
            @RequestParam(required = false) UUID sectorId,
            @RequestParam(required = false) UUID pabellonId,
            @RequestParam(required = false) UUID comercianteId,
            @RequestParam(required = false) EstadoAsignacion estado,
            @RequestParam(required = false) TipoAsignacion tipoAsignacion,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicioDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicioHasta,
            @PageableDefault(size = 15, sort = "fechaSolicitud", direction = Sort.Direction.DESC) Pageable pageable) {

        AsignacionFilter filter = AsignacionFilter.builder()
                .termino(termino)
                .mercadoId(mercadoId)
                .sectorId(sectorId)
                .pabellonId(pabellonId)
                .comercianteId(comercianteId)
                .estado(estado)
                .tipoAsignacion(tipoAsignacion)
                .fechaInicioDesde(fechaInicioDesde)
                .fechaInicioHasta(fechaInicioHasta)
                .build();

        return ResponseEntity.ok(asignacionService.listarConFiltros(filter, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('MERCADOS_ASIGNACION_VER') or hasAuthority('MERCADOS_ASIGNACION_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'OPERADOR', 'JEFE_OFICINA')")
    public ResponseEntity<AsignacionDetailResponse> obtenerDetalle(@PathVariable UUID id) {
        return ResponseEntity.ok(asignacionService.obtenerPorIdCompleto(id));
    }

    // ==========================================
    // REGISTRO Y EDICIÓN DE SOLICITUD
    // ==========================================

    @PostMapping
    @PreAuthorize("hasAuthority('MERCADOS_ASIGNACION_CREAR') or hasAuthority('MERCADOS_ASIGNACION_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<AsignacionDetailResponse> registrarSolicitud(@Valid @RequestBody AsignacionCreateRequest request) {
        AsignacionDetailResponse response = asignacionService.registrarSolicitud(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('MERCADOS_ASIGNACION_CREAR') or hasAuthority('MERCADOS_ASIGNACION_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<AsignacionDetailResponse> actualizarSolicitud(
            @PathVariable UUID id,
            @Valid @RequestBody AsignacionUpdateRequest request) {
        return ResponseEntity.ok(asignacionService.actualizarSolicitud(id, request));
    }

    @PostMapping("/{id}/presentar")
    @PreAuthorize("hasAuthority('MERCADOS_ASIGNACION_CREAR') or hasAuthority('MERCADOS_ASIGNACION_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<AsignacionDetailResponse> presentarSolicitud(@PathVariable UUID id) {
        return ResponseEntity.ok(asignacionService.presentarSolicitud(id));
    }

    // ==========================================
    // EVALUACIÓN, OBSERVACIÓN Y SUBSANACIÓN
    // ==========================================

    @PostMapping("/{id}/evaluar")
    @PreAuthorize("hasAuthority('MERCADOS_ASIGNACION_EVALUAR') or hasAuthority('MERCADOS_ASIGNACION_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<AsignacionDetailResponse> evaluarSolicitud(
            @PathVariable UUID id,
            @Valid @RequestBody AsignacionEvaluacionRequest request) {
        return ResponseEntity.ok(evaluacionService.evaluarSolicitud(id, request));
    }

    @PostMapping("/{id}/observar")
    @PreAuthorize("hasAuthority('MERCADOS_ASIGNACION_OBSERVAR') or hasAuthority('MERCADOS_ASIGNACION_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<AsignacionDetailResponse> formularObservacion(
            @PathVariable UUID id,
            @Valid @RequestBody AsignacionObservacionRequest request) {
        return ResponseEntity.ok(evaluacionService.formularObservacion(id, request));
    }

    @PostMapping("/{id}/subsanar")
    @PreAuthorize("hasAuthority('MERCADOS_ASIGNACION_CREAR') or hasAuthority('MERCADOS_ASIGNACION_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<AsignacionDetailResponse> subsanarObservacion(
            @PathVariable UUID id,
            @RequestParam String motivoSubsanacion,
            @RequestParam(required = false) Long documentoSustentoId) {
        return ResponseEntity.ok(evaluacionService.subsanarObservacion(id, motivoSubsanacion, documentoSustentoId));
    }

    // ==========================================
    // APROBACIÓN Y RECHAZO
    // ==========================================

    @PostMapping("/{id}/aprobar")
    @PreAuthorize("hasAuthority('MERCADOS_ASIGNACION_APROBAR') or hasAuthority('MERCADOS_ASIGNACION_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<AsignacionDetailResponse> aprobarSolicitud(
            @PathVariable UUID id,
            @Valid @RequestBody AsignacionAprobacionRequest request) {
        AsignacionDetailResponse aprobada = evaluacionService.aprobarSolicitud(id, request);
        if (request.isActivarInmediatamente()) {
            return ResponseEntity.ok(ocupacionService.activarOcupacion(id));
        }
        return ResponseEntity.ok(aprobada);
    }

    @PostMapping("/{id}/rechazar")
    @PreAuthorize("hasAuthority('MERCADOS_ASIGNACION_RECHAZAR') or hasAuthority('MERCADOS_ASIGNACION_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<AsignacionDetailResponse> rechazarSolicitud(
            @PathVariable UUID id,
            @Valid @RequestBody AsignacionRechazoRequest request) {
        return ResponseEntity.ok(evaluacionService.rechazarSolicitud(id, request));
    }

    // ==========================================
    // CONTROL DE OCUPACIÓN Y CICLO VIGENTE
    // ==========================================

    @PostMapping("/{id}/activar")
    @PreAuthorize("hasAuthority('MERCADOS_ASIGNACION_ACTIVAR') or hasAuthority('MERCADOS_ASIGNACION_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<AsignacionDetailResponse> activarOcupacion(@PathVariable UUID id) {
        return ResponseEntity.ok(ocupacionService.activarOcupacion(id));
    }

    @PostMapping("/{id}/suspender")
    @PreAuthorize("hasAuthority('MERCADOS_ASIGNACION_SUSPENDER') or hasAuthority('MERCADOS_ASIGNACION_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<AsignacionDetailResponse> suspenderAsignacion(
            @PathVariable UUID id,
            @Valid @RequestBody AsignacionSuspensionRequest request) {
        return ResponseEntity.ok(ocupacionService.suspenderAsignacion(id, request));
    }

    @PostMapping("/{id}/reanudar")
    @PreAuthorize("hasAuthority('MERCADOS_ASIGNACION_ACTIVAR') or hasAuthority('MERCADOS_ASIGNACION_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<AsignacionDetailResponse> reanudarAsignacion(
            @PathVariable UUID id,
            @RequestParam(required = false) String motivo) {
        return ResponseEntity.ok(ocupacionService.reanudarAsignacion(id, motivo));
    }

    @PostMapping("/{id}/finalizar")
    @PreAuthorize("hasAuthority('MERCADOS_ASIGNACION_FINALIZAR') or hasAuthority('MERCADOS_ASIGNACION_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<AsignacionDetailResponse> finalizarAsignacion(
            @PathVariable UUID id,
            @Valid @RequestBody AsignacionFinalizacionRequest request) {
        return ResponseEntity.ok(ocupacionService.finalizarAsignacion(id, request));
    }

    @PostMapping("/{id}/devolucion")
    @PreAuthorize("hasAuthority('MERCADOS_ASIGNACION_DEVOLUCION') or hasAuthority('MERCADOS_ASIGNACION_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<DevolucionPuestoResponse> registrarDevolucion(
            @PathVariable UUID id,
            @Valid @RequestBody DevolucionPuestoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ocupacionService.registrarDevolucion(id, request));
    }

    @PostMapping("/{id}/anular")
    @PreAuthorize("hasAuthority('MERCADOS_ASIGNACION_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<AsignacionDetailResponse> anularAsignacion(
            @PathVariable UUID id,
            @RequestParam(required = false) String motivo) {
        return ResponseEntity.ok(asignacionService.anularAsignacion(id, motivo));
    }

    // ==========================================
    // DISPONIBILIDAD, HISTORIAL, ESTADÍSTICAS Y DOCUMENTOS
    // ==========================================

    @GetMapping("/puestos/{puestoId}/disponibilidad")
    @PreAuthorize("hasAuthority('MERCADOS_ASIGNACION_VER') or hasAuthority('MERCADOS_ASIGNACION_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'OPERADOR')")
    public ResponseEntity<DisponibilidadPuestoResponse> consultarDisponibilidadPuesto(@PathVariable UUID puestoId) {
        return ResponseEntity.ok(disponibilidadService.consultarDisponibilidad(puestoId));
    }

    @GetMapping("/{id}/historial")
    @PreAuthorize("hasAuthority('MERCADOS_ASIGNACION_HISTORIAL_VER') or hasAuthority('MERCADOS_ASIGNACION_VER') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<List<HistorialOcupacionResponse>> consultarHistorial(@PathVariable UUID id) {
        return ResponseEntity.ok(historialService.obtenerHistorialPorAsignacion(id));
    }

    @GetMapping("/estadisticas")
    @PreAuthorize("hasAuthority('MERCADOS_ASIGNACION_VER') or hasAuthority('MERCADOS_ASIGNACION_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<AsignacionEstadisticasResponse> obtenerEstadisticas() {
        return ResponseEntity.ok(asignacionService.obtenerEstadisticas());
    }

    @GetMapping("/{id}/constancia")
    @PreAuthorize("hasAuthority('MERCADOS_ASIGNACION_EXPORTAR') or hasAuthority('MERCADOS_ASIGNACION_VER') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<byte[]> descargarConstanciaPdf(@PathVariable UUID id) {
        byte[] pdfBytes = constanciaService.generarConstanciaPdf(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"constancia_asignacion_" + id + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @GetMapping("/exportar/excel")
    @PreAuthorize("hasAuthority('MERCADOS_ASIGNACION_EXPORTAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<byte[]> exportarExcel(
            @RequestParam(required = false) String termino,
            @RequestParam(required = false) UUID mercadoId,
            @RequestParam(required = false) UUID sectorId,
            @RequestParam(required = false) UUID pabellonId,
            @RequestParam(required = false) UUID comercianteId,
            @RequestParam(required = false) EstadoAsignacion estado,
            @RequestParam(required = false) TipoAsignacion tipoAsignacion,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicioDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicioHasta) {

        AsignacionFilter filter = AsignacionFilter.builder()
                .termino(termino)
                .mercadoId(mercadoId)
                .sectorId(sectorId)
                .pabellonId(pabellonId)
                .comercianteId(comercianteId)
                .estado(estado)
                .tipoAsignacion(tipoAsignacion)
                .fechaInicioDesde(fechaInicioDesde)
                .fechaInicioHasta(fechaInicioHasta)
                .build();

        byte[] bytes = exportService.exportarExcel(filter);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"padron_asignaciones_puestos.csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(bytes);
    }

    @GetMapping("/exportar/pdf")
    @PreAuthorize("hasAuthority('MERCADOS_ASIGNACION_EXPORTAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<byte[]> exportarPdf(
            @RequestParam(required = false) String termino,
            @RequestParam(required = false) UUID mercadoId,
            @RequestParam(required = false) UUID sectorId,
            @RequestParam(required = false) UUID pabellonId,
            @RequestParam(required = false) UUID comercianteId,
            @RequestParam(required = false) EstadoAsignacion estado,
            @RequestParam(required = false) TipoAsignacion tipoAsignacion,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicioDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicioHasta) {

        AsignacionFilter filter = AsignacionFilter.builder()
                .termino(termino)
                .mercadoId(mercadoId)
                .sectorId(sectorId)
                .pabellonId(pabellonId)
                .comercianteId(comercianteId)
                .estado(estado)
                .tipoAsignacion(tipoAsignacion)
                .fechaInicioDesde(fechaInicioDesde)
                .fechaInicioHasta(fechaInicioHasta)
                .build();

        byte[] bytes = exportService.exportarPdf(filter);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"reporte_asignaciones_puestos.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(bytes);
    }
}
