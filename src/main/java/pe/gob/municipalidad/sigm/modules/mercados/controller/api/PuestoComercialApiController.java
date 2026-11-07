package pe.gob.municipalidad.sigm.modules.mercados.controller.api;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.gob.municipalidad.sigm.modules.mercados.dto.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoFisicoPuesto;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoPuesto;
import pe.gob.municipalidad.sigm.modules.mercados.repository.TipoPuestoComercialRepository;
import pe.gob.municipalidad.sigm.modules.mercados.service.PuestoComercialService;
import pe.gob.municipalidad.sigm.modules.mercados.service.PuestoExportService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/mercados")
public class PuestoComercialApiController {

    private final PuestoComercialService puestoService;
    private final PuestoExportService exportService;
    private final TipoPuestoComercialRepository tipoPuestoRepository;

    public PuestoComercialApiController(
            PuestoComercialService puestoService,
            PuestoExportService exportService,
            TipoPuestoComercialRepository tipoPuestoRepository) {
        this.puestoService = puestoService;
        this.exportService = exportService;
        this.tipoPuestoRepository = tipoPuestoRepository;
    }

    // ==========================================
    // LISTADO Y DETALLES
    // ==========================================

    @GetMapping("/{mercadoId}/puestos")
    @PreAuthorize("hasAuthority('MERCADOS_PUESTO_VER') or hasAuthority('MERCADOS_PUESTO_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'OPERADOR', 'JEFE_OFICINA')")
    public ResponseEntity<Page<PuestoComercialResponse>> listarPuestos(
            @PathVariable UUID mercadoId,
            @RequestParam(required = false) String termino,
            @RequestParam(required = false) UUID sectorId,
            @RequestParam(required = false) UUID pabellonId,
            @RequestParam(required = false) UUID zonaId,
            @RequestParam(required = false) Long tipoPuestoId,
            @RequestParam(required = false) EstadoFisicoPuesto estadoFisico,
            @RequestParam(required = false) EstadoPuesto estado,
            @RequestParam(required = false) Integer nivel,
            @RequestParam(required = false) BigDecimal minArea,
            @RequestParam(required = false) BigDecimal maxArea,
            @PageableDefault(size = 15, sort = "codigo") Pageable pageable) {
        return ResponseEntity.ok(puestoService.listarPuestosPaginado(
                mercadoId, termino, sectorId, pabellonId, zonaId, tipoPuestoId,
                estadoFisico, estado, nivel, minArea, maxArea, pageable));
    }

    @PostMapping("/{mercadoId}/puestos")
    @PreAuthorize("hasAuthority('MERCADOS_PUESTO_CREAR') or hasAuthority('MERCADOS_PUESTO_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<PuestoComercialResponse> crearPuesto(
            @PathVariable UUID mercadoId,
            @Valid @RequestBody PuestoComercialCreateRequest request) {
        request.setMercadoId(mercadoId);
        return ResponseEntity.status(HttpStatus.CREATED).body(puestoService.registrarPuesto(request));
    }

    @GetMapping("/{mercadoId}/puestos/{puestoId}")
    @PreAuthorize("hasAuthority('MERCADOS_PUESTO_VER') or hasAuthority('MERCADOS_PUESTO_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'OPERADOR', 'JEFE_OFICINA')")
    public ResponseEntity<PuestoComercialResponse> obtenerPuesto(
            @PathVariable UUID mercadoId,
            @PathVariable UUID puestoId) {
        return ResponseEntity.ok(puestoService.obtenerPorIdYMercado(puestoId, mercadoId));
    }

    @GetMapping("/{mercadoId}/puestos/{puestoId}/detalle")
    @PreAuthorize("hasAuthority('MERCADOS_PUESTO_VER') or hasAuthority('MERCADOS_PUESTO_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'OPERADOR', 'JEFE_OFICINA')")
    public ResponseEntity<PuestoComercialDetailResponse> obtenerDetallePuesto(
            @PathVariable UUID mercadoId,
            @PathVariable UUID puestoId) {
        return ResponseEntity.ok(puestoService.obtenerDetalleCompleto(puestoId, mercadoId));
    }

    @PutMapping("/{mercadoId}/puestos/{puestoId}")
    @PreAuthorize("hasAuthority('MERCADOS_PUESTO_EDITAR') or hasAuthority('MERCADOS_PUESTO_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<PuestoComercialResponse> actualizarPuesto(
            @PathVariable UUID mercadoId,
            @PathVariable UUID puestoId,
            @Valid @RequestBody PuestoComercialUpdateRequest request) {
        return ResponseEntity.ok(puestoService.actualizarPuesto(puestoId, mercadoId, request));
    }

    @PatchMapping("/{mercadoId}/puestos/{puestoId}/estado")
    @PreAuthorize("hasAuthority('MERCADOS_PUESTO_CAMBIAR_ESTADO') or hasAuthority('MERCADOS_PUESTO_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA')")
    public ResponseEntity<PuestoComercialResponse> cambiarEstadoPuesto(
            @PathVariable UUID mercadoId,
            @PathVariable UUID puestoId,
            @Valid @RequestBody PuestoEstadoRequest request) {
        return ResponseEntity.ok(puestoService.cambiarEstado(puestoId, mercadoId, request));
    }

    @PatchMapping("/{mercadoId}/puestos/{puestoId}/ubicacion")
    @PreAuthorize("hasAuthority('MERCADOS_PUESTO_CAMBIAR_UBICACION') or hasAuthority('MERCADOS_PUESTO_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<PuestoComercialResponse> cambiarUbicacionPuesto(
            @PathVariable UUID mercadoId,
            @PathVariable UUID puestoId,
            @Valid @RequestBody PuestoUbicacionRequest request) {
        return ResponseEntity.ok(puestoService.cambiarUbicacion(puestoId, mercadoId, request));
    }

    @GetMapping("/{mercadoId}/puestos/{puestoId}/disponibilidad")
    @PreAuthorize("hasAuthority('MERCADOS_PUESTO_VER') or hasAuthority('MERCADOS_PUESTO_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'OPERADOR', 'JEFE_OFICINA')")
    public ResponseEntity<Map<String, Object>> consultarDisponibilidad(
            @PathVariable UUID mercadoId,
            @PathVariable UUID puestoId) {
        return ResponseEntity.ok(puestoService.consultarDisponibilidad(puestoId, mercadoId));
    }

    @GetMapping("/{mercadoId}/puestos/{puestoId}/historial")
    @PreAuthorize("hasAuthority('MERCADOS_PUESTO_HISTORIAL_VER') or hasAuthority('MERCADOS_PUESTO_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'OPERADOR', 'JEFE_OFICINA')")
    public ResponseEntity<PuestoComercialDetailResponse> consultarHistorial(
            @PathVariable UUID mercadoId,
            @PathVariable UUID puestoId) {
        return ResponseEntity.ok(puestoService.obtenerDetalleCompleto(puestoId, mercadoId));
    }

    @GetMapping("/{mercadoId}/puestos/estadisticas")
    @PreAuthorize("hasAuthority('MERCADOS_PUESTO_VER') or hasAuthority('MERCADOS_ESTRUCTURA_VER') or hasAuthority('MERCADOS_MERCADO_VER') or hasAnyRole('ADMIN', 'SUPERADMIN', 'OPERADOR', 'JEFE_OFICINA')")
    public ResponseEntity<PuestoEstadisticasResponse> obtenerEstadisticas(@PathVariable UUID mercadoId) {
        return ResponseEntity.ok(puestoService.obtenerEstadisticas(mercadoId));
    }

    // ==========================================
    // EXPORTACIONES
    // ==========================================

    @GetMapping("/{mercadoId}/puestos/exportar/excel")
    @PreAuthorize("hasAuthority('MERCADOS_PUESTO_EXPORTAR') or hasAuthority('MERCADOS_PUESTO_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA')")
    public ResponseEntity<byte[]> exportarExcel(
            @PathVariable UUID mercadoId,
            @RequestParam(required = false) String termino,
            @RequestParam(required = false) UUID sectorId,
            @RequestParam(required = false) UUID pabellonId,
            @RequestParam(required = false) UUID zonaId,
            @RequestParam(required = false) Long tipoPuestoId,
            @RequestParam(required = false) EstadoFisicoPuesto estadoFisico,
            @RequestParam(required = false) EstadoPuesto estado,
            @RequestParam(required = false) Integer nivel,
            @RequestParam(required = false) BigDecimal minArea,
            @RequestParam(required = false) BigDecimal maxArea) {
        byte[] bytes = exportService.exportarExcel(mercadoId, termino, sectorId, pabellonId, zonaId,
                tipoPuestoId, estadoFisico, estado, nivel, minArea, maxArea);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"puestos-mercado-" + mercadoId + ".xlsx\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }

    @GetMapping("/{mercadoId}/puestos/exportar/pdf")
    @PreAuthorize("hasAuthority('MERCADOS_PUESTO_EXPORTAR') or hasAuthority('MERCADOS_PUESTO_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA')")
    public ResponseEntity<byte[]> exportarPdf(
            @PathVariable UUID mercadoId,
            @RequestParam(required = false) String termino,
            @RequestParam(required = false) UUID sectorId,
            @RequestParam(required = false) UUID pabellonId,
            @RequestParam(required = false) UUID zonaId,
            @RequestParam(required = false) Long tipoPuestoId,
            @RequestParam(required = false) EstadoFisicoPuesto estadoFisico,
            @RequestParam(required = false) EstadoPuesto estado,
            @RequestParam(required = false) Integer nivel,
            @RequestParam(required = false) BigDecimal minArea,
            @RequestParam(required = false) BigDecimal maxArea) {
        byte[] bytes = exportService.exportarPdf(mercadoId, termino, sectorId, pabellonId, zonaId,
                tipoPuestoId, estadoFisico, estado, nivel, minArea, maxArea);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"puestos-mercado-" + mercadoId + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(bytes);
    }

    // ==========================================
    // CATÁLOGO DE TIPOS DE PUESTO
    // ==========================================

    @GetMapping("/tipos-puesto")
    public ResponseEntity<List<TipoPuestoResponse>> listarTiposPuesto() {
        List<TipoPuestoResponse> tipos = tipoPuestoRepository.findByEstadoOrderByNombreAsc("ACTIVO")
                .stream()
                .map(t -> TipoPuestoResponse.builder()
                        .id(t.getId())
                        .codigo(t.getCodigo())
                        .nombre(t.getNombre())
                        .descripcion(t.getDescripcion())
                        .permiteInstalacionFija(t.getPermiteInstalacionFija())
                        .estado(t.getEstado())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(tipos);
    }
}
