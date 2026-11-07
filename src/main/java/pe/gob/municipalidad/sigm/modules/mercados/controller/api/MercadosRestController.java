package pe.gob.municipalidad.sigm.modules.mercados.controller.api;

import jakarta.validation.Valid;
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
import pe.gob.municipalidad.sigm.modules.mercados.dto.MercadosDtos.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoComerciante;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoMercado;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoPuesto;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoZona;
import pe.gob.municipalidad.sigm.modules.mercados.repository.CategoriaComercialRepository;
import pe.gob.municipalidad.sigm.modules.mercados.service.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/mercados")
public class MercadosRestController {

    private final MercadoMunicipalService mercadoService;
    private final SectorMercadoService sectorService;
    private final PabellonMercadoService pabellonService;
    private final ZonaComercialService zonaService;
    private final EstructuraMercadoService estructuraService;
    private final PuestoComercialService puestoService;
    private final ComercianteService comercianteService;
    private final AsignacionPuestoService asignacionService;
    private final MercadosDashboardService dashboardService;
    private final MercadosExportService exportService;
    private final CategoriaComercialRepository categoriaRepository;

    public MercadosRestController(
            MercadoMunicipalService mercadoService,
            SectorMercadoService sectorService,
            PabellonMercadoService pabellonService,
            ZonaComercialService zonaService,
            EstructuraMercadoService estructuraService,
            PuestoComercialService puestoService,
            ComercianteService comercianteService,
            AsignacionPuestoService asignacionService,
            MercadosDashboardService dashboardService,
            MercadosExportService exportService,
            CategoriaComercialRepository categoriaRepository) {
        this.mercadoService = mercadoService;
        this.sectorService = sectorService;
        this.pabellonService = pabellonService;
        this.zonaService = zonaService;
        this.estructuraService = estructuraService;
        this.puestoService = puestoService;
        this.comercianteService = comercianteService;
        this.asignacionService = asignacionService;
        this.dashboardService = dashboardService;
        this.exportService = exportService;
        this.categoriaRepository = categoriaRepository;
    }

    // ==========================================
    // MÉTRICAS & MERCADOS GLOBALES
    // ==========================================

    @GetMapping("/metricas")
    @PreAuthorize("hasAuthority('MERCADOS_MODULO_ACCEDER') or hasAnyRole('ADMIN', 'SUPERADMIN', 'OPERADOR', 'JEFE_OFICINA')")
    public ResponseEntity<DashboardMercadosMetrics> obtenerMetricas() {
        return ResponseEntity.ok(dashboardService.obtenerMetricas());
    }

    @GetMapping("/estadisticas")
    @PreAuthorize("hasAuthority('MERCADOS_MERCADO_VER') or hasAnyRole('ADMIN', 'SUPERADMIN', 'OPERADOR', 'JEFE_OFICINA')")
    public ResponseEntity<MercadoEstadisticasResponse> obtenerEstadisticasMercados() {
        return ResponseEntity.ok(mercadoService.obtenerEstadisticas());
    }

    @GetMapping("/oficinas-disponibles")
    @PreAuthorize("hasAuthority('MERCADOS_MERCADO_VER') or hasAnyRole('ADMIN', 'SUPERADMIN', 'OPERADOR', 'JEFE_OFICINA')")
    public ResponseEntity<List<OficinaSimpleResponse>> listarOficinasDisponibles() {
        return ResponseEntity.ok(mercadoService.listarOficinasDisponibles());
    }

    @GetMapping("/categorias-comerciales")
    @PreAuthorize("hasAuthority('MERCADOS_MODULO_ACCEDER') or hasAnyRole('ADMIN', 'SUPERADMIN', 'OPERADOR', 'JEFE_OFICINA')")
    public ResponseEntity<List<CategoriaComercialResponse>> listarCategoriasComerciales() {
        List<CategoriaComercialResponse> categorias = categoriaRepository.findByEstadoOrderByOrdenVisualAsc("ACTIVO")
                .stream()
                .map(c -> CategoriaComercialResponse.builder()
                        .id(c.getId())
                        .codigo(c.getCodigo())
                        .nombre(c.getNombre())
                        .descripcion(c.getDescripcion())
                        .estado(c.getEstado())
                        .ordenVisual(c.getOrdenVisual())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(categorias);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('MERCADOS_MERCADO_VER') or hasAnyRole('ADMIN', 'SUPERADMIN', 'OPERADOR', 'JEFE_OFICINA')")
    public ResponseEntity<Page<MercadoResponse>> listarMercados(
            @RequestParam(required = false) String termino,
            @RequestParam(required = false) EstadoMercado estado,
            @RequestParam(required = false) Long oficinaId,
            @RequestParam(required = false) String distrito,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(defaultValue = "nombre") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        MercadoFilter filter = MercadoFilter.builder()
                .termino(termino)
                .estado(estado)
                .oficinaId(oficinaId)
                .distrito(distrito)
                .fechaDesde(fechaDesde)
                .fechaHasta(fechaHasta)
                .sortBy(sortBy)
                .sortDir(sortDir)
                .page(page)
                .size(size)
                .build();

        return ResponseEntity.ok(mercadoService.listarMercados(filter));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('MERCADOS_MERCADO_VER') or hasAnyRole('ADMIN', 'SUPERADMIN', 'OPERADOR', 'JEFE_OFICINA')")
    public ResponseEntity<MercadoResponse> obtenerMercadoPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(mercadoService.obtenerPorId(id));
    }

    @GetMapping("/{id}/detalle")
    @PreAuthorize("hasAuthority('MERCADOS_MERCADO_VER') or hasAnyRole('ADMIN', 'SUPERADMIN', 'OPERADOR', 'JEFE_OFICINA')")
    public ResponseEntity<MercadoDetalleResponse> obtenerDetalleCompleto(@PathVariable UUID id) {
        return ResponseEntity.ok(mercadoService.obtenerDetalle(id));
    }

    @GetMapping("/{id}/puestos/resumen")
    @PreAuthorize("hasAuthority('MERCADOS_MERCADO_VER') or hasAuthority('MERCADOS_PUESTO_VER') or hasAnyRole('ADMIN', 'SUPERADMIN', 'OPERADOR', 'JEFE_OFICINA')")
    public ResponseEntity<ResumenPuestosMercadoResponse> obtenerResumenPuestos(@PathVariable UUID id) {
        return ResponseEntity.ok(mercadoService.obtenerResumenPuestos(id));
    }

    @GetMapping("/{id}/historial")
    @PreAuthorize("hasAuthority('MERCADOS_MERCADO_HISTORIAL_VER') or hasAuthority('MERCADOS_MERCADO_VER') or hasAnyRole('ADMIN', 'SUPERADMIN', 'OPERADOR', 'JEFE_OFICINA')")
    public ResponseEntity<List<MercadoHistorialItemResponse>> obtenerHistorialEstados(@PathVariable UUID id) {
        return ResponseEntity.ok(mercadoService.obtenerHistorial(id));
    }

    @GetMapping("/{id}/documentos")
    @PreAuthorize("hasAuthority('MERCADOS_MERCADO_DOCUMENTOS') or hasAuthority('MERCADOS_MERCADO_VER') or hasAnyRole('ADMIN', 'SUPERADMIN', 'OPERADOR', 'JEFE_OFICINA')")
    public ResponseEntity<List<MercadoDocumentoResponse>> listarDocumentosMercado(@PathVariable UUID id) {
        return ResponseEntity.ok(mercadoService.listarDocumentos(id));
    }

    @PostMapping("/{id}/documentos")
    @PreAuthorize("hasAuthority('MERCADOS_MERCADO_DOCUMENTOS') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<MercadoDocumentoResponse> asociarDocumento(
            @PathVariable UUID id,
            @Valid @RequestBody MercadoDocumentoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mercadoService.asociarDocumento(id, request));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('MERCADOS_MERCADO_CREAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<MercadoResponse> crearMercado(@Valid @RequestBody MercadoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mercadoService.crearMercado(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('MERCADOS_MERCADO_EDITAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<MercadoResponse> actualizarMercado(
            @PathVariable UUID id,
            @Valid @RequestBody MercadoActualizacionRequest request) {
        return ResponseEntity.ok(mercadoService.actualizarMercado(id, request));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAuthority('MERCADOS_MERCADO_CAMBIAR_ESTADO') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA')")
    public ResponseEntity<MercadoResponse> cambiarEstado(
            @PathVariable UUID id,
            @Valid @RequestBody CambioEstadoMercadoRequest request) {
        return ResponseEntity.ok(mercadoService.cambiarEstado(id, request));
    }

    @GetMapping("/exportar/excel")
    @PreAuthorize("hasAuthority('MERCADOS_MERCADO_EXPORTAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA', 'OPERADOR')")
    public ResponseEntity<byte[]> exportarExcel(
            @RequestParam(required = false) String termino,
            @RequestParam(required = false) EstadoMercado estado,
            @RequestParam(required = false) Long oficinaId,
            @RequestParam(required = false) String distrito,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta) {

        MercadoFilter filter = MercadoFilter.builder()
                .termino(termino)
                .estado(estado)
                .oficinaId(oficinaId)
                .distrito(distrito)
                .fechaDesde(fechaDesde)
                .fechaHasta(fechaHasta)
                .build();

        byte[] bytes = exportService.exportarExcel(filter);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"mercados_municipales_quinua.xlsx\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }

    @GetMapping("/exportar/pdf")
    @PreAuthorize("hasAuthority('MERCADOS_MERCADO_EXPORTAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA', 'OPERADOR')")
    public ResponseEntity<byte[]> exportarPdf(
            @RequestParam(required = false) String termino,
            @RequestParam(required = false) EstadoMercado estado,
            @RequestParam(required = false) Long oficinaId,
            @RequestParam(required = false) String distrito,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta) {

        MercadoFilter filter = MercadoFilter.builder()
                .termino(termino)
                .estado(estado)
                .oficinaId(oficinaId)
                .distrito(distrito)
                .fechaDesde(fechaDesde)
                .fechaHasta(fechaHasta)
                .build();

        byte[] bytes = exportService.exportarPdf(filter);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"mercados_municipales_quinua.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(bytes);
    }

    // ==========================================
    // PROMPT 23: SECTORES COMERCIALES
    // ==========================================

    @GetMapping("/{mercadoId}/sectores")
    @PreAuthorize("hasAuthority('MERCADOS_SECTOR_VER') or hasAuthority('MERCADOS_SECTOR_GESTIONAR') or hasAuthority('MERCADOS_MERCADO_VER') or hasAnyRole('ADMIN', 'SUPERADMIN', 'OPERADOR', 'JEFE_OFICINA')")
    public ResponseEntity<Page<SectorResponse>> listarSectores(
            @PathVariable UUID mercadoId,
            @RequestParam(required = false) String termino,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String categoria,
            @PageableDefault(size = 15, sort = "ordenVisual") Pageable pageable) {
        return ResponseEntity.ok(sectorService.listarSectores(mercadoId, termino, estado, categoria, pageable));
    }

    @GetMapping("/{mercadoId}/sectores/todos")
    @PreAuthorize("hasAuthority('MERCADOS_SECTOR_VER') or hasAuthority('MERCADOS_SECTOR_GESTIONAR') or hasAuthority('MERCADOS_MERCADO_VER') or hasAnyRole('ADMIN', 'SUPERADMIN', 'OPERADOR', 'JEFE_OFICINA')")
    public ResponseEntity<List<SectorResponse>> listarTodosSectoresPorMercado(@PathVariable UUID mercadoId) {
        return ResponseEntity.ok(sectorService.listarPorMercado(mercadoId));
    }

    @PostMapping("/{mercadoId}/sectores")
    @PreAuthorize("hasAuthority('MERCADOS_SECTOR_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<SectorResponse> crearSector(
            @PathVariable UUID mercadoId,
            @Valid @RequestBody SectorRequest request) {
        request.setMercadoId(mercadoId);
        return ResponseEntity.status(HttpStatus.CREATED).body(sectorService.crearSector(request));
    }

    @GetMapping("/{mercadoId}/sectores/{sectorId}")
    @PreAuthorize("hasAuthority('MERCADOS_SECTOR_VER') or hasAuthority('MERCADOS_SECTOR_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'OPERADOR', 'JEFE_OFICINA')")
    public ResponseEntity<SectorResponse> obtenerSector(
            @PathVariable UUID mercadoId,
            @PathVariable UUID sectorId) {
        return ResponseEntity.ok(sectorService.obtenerPorIdYMercado(sectorId, mercadoId));
    }

    @PutMapping("/{mercadoId}/sectores/{sectorId}")
    @PreAuthorize("hasAuthority('MERCADOS_SECTOR_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<SectorResponse> actualizarSector(
            @PathVariable UUID mercadoId,
            @PathVariable UUID sectorId,
            @Valid @RequestBody SectorActualizacionRequest request) {
        return ResponseEntity.ok(sectorService.actualizarSector(sectorId, mercadoId, request));
    }

    @PatchMapping("/{mercadoId}/sectores/{sectorId}/estado")
    @PreAuthorize("hasAuthority('MERCADOS_SECTOR_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA')")
    public ResponseEntity<SectorResponse> cambiarEstadoSector(
            @PathVariable UUID mercadoId,
            @PathVariable UUID sectorId,
            @Valid @RequestBody CambioEstadoEstructuraRequest request) {
        return ResponseEntity.ok(sectorService.cambiarEstado(sectorId, mercadoId, request));
    }

    @PutMapping("/{mercadoId}/sectores/reordenar")
    @PreAuthorize("hasAuthority('MERCADOS_SECTOR_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Map<String, String>> reordenarSectores(
            @PathVariable UUID mercadoId,
            @Valid @RequestBody List<ReordenarItemRequest> items) {
        sectorService.reordenarSectores(mercadoId, items);
        return ResponseEntity.ok(Map.of("mensaje", "Ordenamiento de sectores actualizado con éxito."));
    }

    @GetMapping("/{mercadoId}/sectores/{sectorId}/historial")
    @PreAuthorize("hasAuthority('MERCADOS_SECTOR_VER') or hasAuthority('MERCADOS_SECTOR_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'OPERADOR', 'JEFE_OFICINA')")
    public ResponseEntity<List<HistorialCambioEstadoResponse>> consultarHistorialSector(
            @PathVariable UUID mercadoId,
            @PathVariable UUID sectorId) {
        return ResponseEntity.ok(sectorService.consultarHistorial(sectorId, mercadoId));
    }

    // ==========================================
    // PROMPT 23: PABELLONES
    // ==========================================

    @GetMapping("/{mercadoId}/pabellones")
    @PreAuthorize("hasAuthority('MERCADOS_PABELLON_VER') or hasAuthority('MERCADOS_PABELLON_GESTIONAR') or hasAuthority('MERCADOS_MERCADO_VER') or hasAnyRole('ADMIN', 'SUPERADMIN', 'OPERADOR', 'JEFE_OFICINA')")
    public ResponseEntity<Page<PabellonResponse>> listarPabellones(
            @PathVariable UUID mercadoId,
            @RequestParam(required = false) String termino,
            @RequestParam(required = false) UUID sectorId,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Integer nivel,
            @PageableDefault(size = 15, sort = "ordenVisual") Pageable pageable) {
        return ResponseEntity.ok(pabellonService.listarPabellones(mercadoId, termino, sectorId, estado, nivel, pageable));
    }

    @GetMapping("/{mercadoId}/pabellones/todos")
    @PreAuthorize("hasAuthority('MERCADOS_PABELLON_VER') or hasAuthority('MERCADOS_PABELLON_GESTIONAR') or hasAuthority('MERCADOS_MERCADO_VER') or hasAnyRole('ADMIN', 'SUPERADMIN', 'OPERADOR', 'JEFE_OFICINA')")
    public ResponseEntity<List<PabellonResponse>> listarTodosPabellonesPorMercado(@PathVariable UUID mercadoId) {
        return ResponseEntity.ok(pabellonService.listarPorMercado(mercadoId));
    }

    @PostMapping("/{mercadoId}/pabellones")
    @PreAuthorize("hasAuthority('MERCADOS_PABELLON_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<PabellonResponse> crearPabellon(
            @PathVariable UUID mercadoId,
            @Valid @RequestBody PabellonRequest request) {
        request.setMercadoId(mercadoId);
        return ResponseEntity.status(HttpStatus.CREATED).body(pabellonService.crearPabellon(request));
    }

    @GetMapping("/{mercadoId}/pabellones/{pabellonId}")
    @PreAuthorize("hasAuthority('MERCADOS_PABELLON_VER') or hasAuthority('MERCADOS_PABELLON_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'OPERADOR', 'JEFE_OFICINA')")
    public ResponseEntity<PabellonResponse> obtenerPabellon(
            @PathVariable UUID mercadoId,
            @PathVariable UUID pabellonId) {
        return ResponseEntity.ok(pabellonService.obtenerPorIdYMercado(pabellonId, mercadoId));
    }

    @PutMapping("/{mercadoId}/pabellones/{pabellonId}")
    @PreAuthorize("hasAuthority('MERCADOS_PABELLON_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<PabellonResponse> actualizarPabellon(
            @PathVariable UUID mercadoId,
            @PathVariable UUID pabellonId,
            @Valid @RequestBody PabellonActualizacionRequest request) {
        return ResponseEntity.ok(pabellonService.actualizarPabellon(pabellonId, mercadoId, request));
    }

    @PatchMapping("/{mercadoId}/pabellones/{pabellonId}/estado")
    @PreAuthorize("hasAuthority('MERCADOS_PABELLON_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA')")
    public ResponseEntity<PabellonResponse> cambiarEstadoPabellon(
            @PathVariable UUID mercadoId,
            @PathVariable UUID pabellonId,
            @Valid @RequestBody CambioEstadoEstructuraRequest request) {
        return ResponseEntity.ok(pabellonService.cambiarEstado(pabellonId, mercadoId, request));
    }

    @PutMapping("/{mercadoId}/pabellones/reordenar")
    @PreAuthorize("hasAuthority('MERCADOS_PABELLON_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Map<String, String>> reordenarPabellones(
            @PathVariable UUID mercadoId,
            @Valid @RequestBody List<ReordenarItemRequest> items) {
        pabellonService.reordenarPabellones(mercadoId, items);
        return ResponseEntity.ok(Map.of("mensaje", "Ordenamiento de pabellones actualizado con éxito."));
    }

    @GetMapping("/{mercadoId}/pabellones/{pabellonId}/historial")
    @PreAuthorize("hasAuthority('MERCADOS_PABELLON_VER') or hasAuthority('MERCADOS_PABELLON_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'OPERADOR', 'JEFE_OFICINA')")
    public ResponseEntity<List<HistorialCambioEstadoResponse>> consultarHistorialPabellon(
            @PathVariable UUID mercadoId,
            @PathVariable UUID pabellonId) {
        return ResponseEntity.ok(pabellonService.consultarHistorial(pabellonId, mercadoId));
    }

    // ==========================================
    // PROMPT 23: ZONAS COMERCIALES
    // ==========================================

    @GetMapping("/{mercadoId}/zonas")
    @PreAuthorize("hasAuthority('MERCADOS_ESTRUCTURA_VER') or hasAuthority('MERCADOS_ZONA_GESTIONAR') or hasAuthority('MERCADOS_MERCADO_VER') or hasAnyRole('ADMIN', 'SUPERADMIN', 'OPERADOR', 'JEFE_OFICINA')")
    public ResponseEntity<Page<ZonaResponse>> listarZonas(
            @PathVariable UUID mercadoId,
            @RequestParam(required = false) String termino,
            @RequestParam(required = false) TipoZona tipoZona,
            @RequestParam(required = false) EstadoMercado estado,
            @RequestParam(required = false) Boolean esAsignable,
            @PageableDefault(size = 15, sort = "ordenVisual") Pageable pageable) {
        return ResponseEntity.ok(zonaService.listarPaginado(mercadoId, termino, tipoZona, estado, esAsignable, pageable));
    }

    @PostMapping("/{mercadoId}/zonas")
    @PreAuthorize("hasAuthority('MERCADOS_ZONA_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<ZonaResponse> crearZona(
            @PathVariable UUID mercadoId,
            @Valid @RequestBody ZonaRequest request) {
        request.setMercadoId(mercadoId);
        return ResponseEntity.status(HttpStatus.CREATED).body(zonaService.crearZona(request));
    }

    @GetMapping("/{mercadoId}/zonas/{zonaId}")
    @PreAuthorize("hasAuthority('MERCADOS_ESTRUCTURA_VER') or hasAuthority('MERCADOS_ZONA_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'OPERADOR', 'JEFE_OFICINA')")
    public ResponseEntity<ZonaResponse> obtenerZona(
            @PathVariable UUID mercadoId,
            @PathVariable UUID zonaId) {
        return ResponseEntity.ok(zonaService.obtenerPorIdYMercado(zonaId, mercadoId));
    }

    @PutMapping("/{mercadoId}/zonas/{zonaId}")
    @PreAuthorize("hasAuthority('MERCADOS_ZONA_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<ZonaResponse> actualizarZona(
            @PathVariable UUID mercadoId,
            @PathVariable UUID zonaId,
            @Valid @RequestBody ZonaRequest request) {
        return ResponseEntity.ok(zonaService.actualizarZona(zonaId, mercadoId, request));
    }

    @PatchMapping("/{mercadoId}/zonas/{zonaId}/estado")
    @PreAuthorize("hasAuthority('MERCADOS_ZONA_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA')")
    public ResponseEntity<ZonaResponse> cambiarEstadoZona(
            @PathVariable UUID mercadoId,
            @PathVariable UUID zonaId,
            @RequestParam EstadoMercado nuevoEstado,
            @RequestParam(defaultValue = "Actualización administrativa") String motivo) {
        return ResponseEntity.ok(zonaService.cambiarEstado(zonaId, mercadoId, nuevoEstado, motivo));
    }

    // ==========================================
    // PROMPT 23: ESTRUCTURA JERÁRQUICA & ESTADÍSTICAS
    // ==========================================

    @GetMapping("/{mercadoId}/estructura")
    @PreAuthorize("hasAuthority('MERCADOS_ESTRUCTURA_VER') or hasAuthority('MERCADOS_MERCADO_VER') or hasAnyRole('ADMIN', 'SUPERADMIN', 'OPERADOR', 'JEFE_OFICINA')")
    public ResponseEntity<EstructuraMercadoResponse> obtenerEstructura(@PathVariable UUID mercadoId) {
        return ResponseEntity.ok(estructuraService.obtenerEstructuraCompleta(mercadoId));
    }

    @GetMapping("/{mercadoId}/estructura/estadisticas")
    @PreAuthorize("hasAuthority('MERCADOS_ESTRUCTURA_VER') or hasAuthority('MERCADOS_MERCADO_VER') or hasAnyRole('ADMIN', 'SUPERADMIN', 'OPERADOR', 'JEFE_OFICINA')")
    public ResponseEntity<EstructuraEstadisticasResponse> obtenerEstadisticasEstructura(@PathVariable UUID mercadoId) {
        return ResponseEntity.ok(estructuraService.obtenerEstadisticas(mercadoId));
    }

}
