package pe.gob.municipalidad.sigm.modules.mercados.controller.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.gob.municipalidad.sigm.modules.mercados.dto.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoComerciante;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoComerciante;
import pe.gob.municipalidad.sigm.modules.mercados.service.*;
import pe.gob.municipalidad.sigm.modules.mercados.service.verification.IdentidadVerificationService;
import pe.gob.municipalidad.sigm.modules.mercados.service.verification.VerificationResult;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Controlador REST para el Registro y Administración de Comerciantes Municipales.
 */
@RestController
@RequestMapping("/api/mercados/comerciantes")
public class ComercianteApiController {

    private final ComercianteService comercianteService;
    private final ComercianteActividadService actividadService;
    private final ComercianteRepresentanteService representanteService;
    private final ComercianteExportService exportService;
    private final IdentidadVerificationService identidadVerificationService;

    public ComercianteApiController(
            ComercianteService comercianteService,
            ComercianteActividadService actividadService,
            ComercianteRepresentanteService representanteService,
            ComercianteExportService exportService,
            IdentidadVerificationService identidadVerificationService) {
        this.comercianteService = comercianteService;
        this.actividadService = actividadService;
        this.representanteService = representanteService;
        this.exportService = exportService;
        this.identidadVerificationService = identidadVerificationService;
    }

    // ==========================================
    // LISTADO Y DETALLE
    // ==========================================

    @GetMapping
    @PreAuthorize("hasAuthority('MERCADOS_COMERCIANTE_VER') or hasAuthority('MERCADOS_COMERCIANTE_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'OPERADOR', 'JEFE_OFICINA')")
    public ResponseEntity<Page<ComercianteResponse>> listarComerciantes(
            @RequestParam(required = false) String termino,
            @RequestParam(required = false) TipoComerciante tipoComerciante,
            @RequestParam(required = false) EstadoComerciante estado,
            @RequestParam(required = false) Long actividadId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(required = false) UUID mercadoId,
            @PageableDefault(size = 15, sort = "codigoComerciante") Pageable pageable) {

        ComercianteFilter filter = ComercianteFilter.builder()
                .termino(termino)
                .tipoComerciante(tipoComerciante)
                .estado(estado)
                .actividadId(actividadId)
                .fechaDesde(fechaDesde)
                .fechaHasta(fechaHasta)
                .mercadoId(mercadoId)
                .build();

        return ResponseEntity.ok(comercianteService.listarComerciantes(filter, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('MERCADOS_COMERCIANTE_VER') or hasAuthority('MERCADOS_COMERCIANTE_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'OPERADOR', 'JEFE_OFICINA')")
    public ResponseEntity<ComercianteDetailResponse> obtenerDetalleComerciante(@PathVariable UUID id) {
        return ResponseEntity.ok(comercianteService.obtenerDetalleCompleto(id));
    }

    // ==========================================
    // REGISTRO
    // ==========================================

    @PostMapping("/natural")
    @PreAuthorize("hasAuthority('MERCADOS_COMERCIANTE_CREAR') or hasAuthority('MERCADOS_COMERCIANTE_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'OPERADOR')")
    public ResponseEntity<ComercianteResponse> registrarPersonaNatural(@Valid @RequestBody ComercianteNaturalCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(comercianteService.registrarComercianteNatural(request));
    }

    @PostMapping("/juridico")
    @PreAuthorize("hasAuthority('MERCADOS_COMERCIANTE_CREAR') or hasAuthority('MERCADOS_COMERCIANTE_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'OPERADOR')")
    public ResponseEntity<ComercianteResponse> registrarPersonaJuridica(@Valid @RequestBody ComercianteJuridicoCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(comercianteService.registrarComercianteJuridico(request));
    }

    // ==========================================
    // EDICIÓN Y ESTADO
    // ==========================================

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('MERCADOS_COMERCIANTE_EDITAR') or hasAuthority('MERCADOS_COMERCIANTE_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<ComercianteResponse> actualizarComerciante(
            @PathVariable UUID id,
            @Valid @RequestBody ComercianteUpdateRequest request) {
        return ResponseEntity.ok(comercianteService.actualizarComerciante(id, request));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAuthority('MERCADOS_COMERCIANTE_CAMBIAR_ESTADO') or hasAuthority('MERCADOS_COMERCIANTE_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA')")
    public ResponseEntity<ComercianteResponse> cambiarEstado(
            @PathVariable UUID id,
            @Valid @RequestBody ComercianteEstadoRequest request) {
        return ResponseEntity.ok(comercianteService.cambiarEstado(id, request));
    }

    // ==========================================
    // ACTIVIDADES Y REPRESENTANTES
    // ==========================================

    @GetMapping("/{id}/actividades")
    @PreAuthorize("hasAuthority('MERCADOS_COMERCIANTE_VER') or hasAuthority('MERCADOS_COMERCIANTE_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'OPERADOR', 'JEFE_OFICINA')")
    public ResponseEntity<List<ActividadComercianteResponse>> listarActividades(@PathVariable UUID id) {
        return ResponseEntity.ok(actividadService.listarActividadesPorComerciante(id));
    }

    @PostMapping("/{id}/actividades")
    @PreAuthorize("hasAuthority('MERCADOS_COMERCIANTE_EDITAR') or hasAuthority('MERCADOS_COMERCIANTE_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<ActividadComercianteResponse> asociarActividad(
            @PathVariable UUID id,
            @Valid @RequestBody ActividadComercianteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(actividadService.asociarActividad(id, request));
    }

    @DeleteMapping("/{id}/actividades/{actividadId}")
    @PreAuthorize("hasAuthority('MERCADOS_COMERCIANTE_EDITAR') or hasAuthority('MERCADOS_COMERCIANTE_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Void> desasociarActividad(@PathVariable UUID id, @PathVariable Long actividadId) {
        actividadService.desasociarActividad(id, actividadId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/representantes")
    @PreAuthorize("hasAuthority('MERCADOS_COMERCIANTE_VER') or hasAuthority('MERCADOS_COMERCIANTE_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'OPERADOR', 'JEFE_OFICINA')")
    public ResponseEntity<List<RepresentanteResponse>> listarRepresentantes(@PathVariable UUID id) {
        return ResponseEntity.ok(representanteService.listarRepresentantesPorComerciante(id));
    }

    @PostMapping("/{id}/representantes")
    @PreAuthorize("hasAuthority('MERCADOS_COMERCIANTE_EDITAR') or hasAuthority('MERCADOS_COMERCIANTE_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<RepresentanteResponse> agregarRepresentante(
            @PathVariable UUID id,
            @Valid @RequestBody RepresentanteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(representanteService.agregarRepresentante(id, request));
    }

    // ==========================================
    // PUESTOS, AUTORIZACIONES E HISTORIAL
    // ==========================================

    @GetMapping("/{id}/puestos")
    @PreAuthorize("hasAuthority('MERCADOS_COMERCIANTE_VER') or hasAuthority('MERCADOS_COMERCIANTE_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'OPERADOR', 'JEFE_OFICINA')")
    public ResponseEntity<List<ComerciantePuestoAsociadoResponse>> consultarPuestosAsociados(@PathVariable UUID id) {
        ComercianteDetailResponse detalle = comercianteService.obtenerDetalleCompleto(id);
        return ResponseEntity.ok(detalle.getPuestosAsociados());
    }

    @GetMapping("/{id}/autorizaciones")
    @PreAuthorize("hasAuthority('MERCADOS_COMERCIANTE_VER') or hasAuthority('MERCADOS_COMERCIANTE_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'OPERADOR', 'JEFE_OFICINA')")
    public ResponseEntity<List<ComercianteAutorizacionResponse>> consultarAutorizaciones(@PathVariable UUID id) {
        ComercianteDetailResponse detalle = comercianteService.obtenerDetalleCompleto(id);
        return ResponseEntity.ok(detalle.getAutorizaciones());
    }

    @GetMapping("/{id}/historial")
    @PreAuthorize("hasAuthority('MERCADOS_COMERCIANTE_HISTORIAL_VER') or hasAuthority('MERCADOS_COMERCIANTE_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'OPERADOR', 'JEFE_OFICINA')")
    public ResponseEntity<List<ComercianteCambioEstadoResponse>> consultarHistorial(@PathVariable UUID id) {
        ComercianteDetailResponse detalle = comercianteService.obtenerDetalleCompleto(id);
        return ResponseEntity.ok(detalle.getHistorialEstados());
    }

    // ==========================================
    // DOCUMENTOS
    // ==========================================

    @PostMapping("/{id}/documentos")
    @PreAuthorize("hasAuthority('MERCADOS_COMERCIANTE_DOCUMENTOS') or hasAuthority('MERCADOS_COMERCIANTE_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA')")
    public ResponseEntity<ComercianteDocumentoResponse> asociarDocumento(
            @PathVariable UUID id,
            @RequestParam Long documentoId,
            @RequestParam(defaultValue = "SUSTENTO") String tipo,
            @RequestParam(required = false) String observaciones) {
        return ResponseEntity.status(HttpStatus.CREATED).body(comercianteService.asociarDocumento(id, documentoId, tipo, observaciones));
    }

    // ==========================================
    // ESTADÍSTICAS Y EXPORTACIONES
    // ==========================================

    @GetMapping("/estadisticas")
    @PreAuthorize("hasAuthority('MERCADOS_COMERCIANTE_VER') or hasAuthority('MERCADOS_COMERCIANTE_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'OPERADOR', 'JEFE_OFICINA')")
    public ResponseEntity<ComercianteEstadisticasResponse> obtenerEstadisticas() {
        return ResponseEntity.ok(comercianteService.obtenerEstadisticas());
    }

    @GetMapping("/exportar/excel")
    @PreAuthorize("hasAuthority('MERCADOS_COMERCIANTE_EXPORTAR') or hasAuthority('MERCADOS_COMERCIANTE_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA')")
    public ResponseEntity<byte[]> exportarExcel(
            @RequestParam(required = false) String termino,
            @RequestParam(required = false) TipoComerciante tipoComerciante,
            @RequestParam(required = false) EstadoComerciante estado,
            @RequestParam(required = false) Long actividadId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(required = false) UUID mercadoId) {

        ComercianteFilter filter = ComercianteFilter.builder()
                .termino(termino)
                .tipoComerciante(tipoComerciante)
                .estado(estado)
                .actividadId(actividadId)
                .fechaDesde(fechaDesde)
                .fechaHasta(fechaHasta)
                .mercadoId(mercadoId)
                .build();

        byte[] bytes = exportService.exportarExcel(filter);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"padron_comerciantes_quinua.xlsx\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }

    @GetMapping("/exportar/pdf")
    @PreAuthorize("hasAuthority('MERCADOS_COMERCIANTE_EXPORTAR') or hasAuthority('MERCADOS_COMERCIANTE_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA')")
    public ResponseEntity<byte[]> exportarPdf(
            @RequestParam(required = false) String termino,
            @RequestParam(required = false) TipoComerciante tipoComerciante,
            @RequestParam(required = false) EstadoComerciante estado,
            @RequestParam(required = false) Long actividadId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(required = false) UUID mercadoId) {

        ComercianteFilter filter = ComercianteFilter.builder()
                .termino(termino)
                .tipoComerciante(tipoComerciante)
                .estado(estado)
                .actividadId(actividadId)
                .fechaDesde(fechaDesde)
                .fechaHasta(fechaHasta)
                .mercadoId(mercadoId)
                .build();

        byte[] bytes = exportService.exportarPdf(filter);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"padron_comerciantes_quinua.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(bytes);
    }

    // ==========================================
    // UTILIDADES DE VALIDACIÓN (RENIEC / SUNAT)
    // ==========================================

    @GetMapping("/verificar-dni")
    @PreAuthorize("hasAuthority('MERCADOS_COMERCIANTE_CREAR') or hasAuthority('MERCADOS_COMERCIANTE_EDITAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'OPERADOR')")
    public ResponseEntity<VerificationResult> verificarDni(@RequestParam String dni) {
        return ResponseEntity.ok(identidadVerificationService.verificarDni(dni));
    }

    @GetMapping("/verificar-ruc")
    @PreAuthorize("hasAuthority('MERCADOS_COMERCIANTE_CREAR') or hasAuthority('MERCADOS_COMERCIANTE_EDITAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'OPERADOR')")
    public ResponseEntity<VerificationResult> verificarRuc(@RequestParam String ruc) {
        return ResponseEntity.ok(identidadVerificationService.verificarRuc(ruc));
    }
}
