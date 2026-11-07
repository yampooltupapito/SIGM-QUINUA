package pe.gob.municipalidad.sigm.modules.mercados.controller.api;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import pe.gob.municipalidad.sigm.modules.mercados.dto.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoEvaluacionInfraccion;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoIncidencia;
import pe.gob.municipalidad.sigm.modules.mercados.enums.OrigenIncidencia;
import pe.gob.municipalidad.sigm.modules.mercados.enums.PrioridadIncidencia;
import pe.gob.municipalidad.sigm.modules.mercados.service.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/mercados")
public class MercadoIncidenciasApiController {

    private final IncidenciaMercadoService incidenciaService;
    private final TipoIncidenciaService tipoService;
    private final TipificacionInfraccionService tipificacionService;
    private final PresuntaInfraccionService infraccionService;
    private final IncidenciasReporteService reporteService;

    public MercadoIncidenciasApiController(
            IncidenciaMercadoService incidenciaService,
            TipoIncidenciaService tipoService,
            TipificacionInfraccionService tipificacionService,
            PresuntaInfraccionService infraccionService,
            IncidenciasReporteService reporteService) {
        this.incidenciaService = incidenciaService;
        this.tipoService = tipoService;
        this.tipificacionService = tipificacionService;
        this.infraccionService = infraccionService;
        this.reporteService = reporteService;
    }

    // 1. Catálogo de Tipos de Incidencia
    @GetMapping("/tipos-incidencia")
    @PreAuthorize("hasAuthority('MERCADOS_INCIDENCIA_VER')")
    public ResponseEntity<List<TipoIncidenciaResponse>> listarTipos() {
        return ResponseEntity.ok(tipoService.listarActivos());
    }

    // 2. Bandeja de Incidencias Paginada
    @GetMapping("/incidencias")
    @PreAuthorize("hasAuthority('MERCADOS_INCIDENCIA_VER')")
    public ResponseEntity<Page<IncidenciaResponse>> listarIncidencias(
            @RequestParam(required = false) UUID mercadoId,
            @RequestParam(required = false) UUID sectorId,
            @RequestParam(required = false) UUID pabellonId,
            @RequestParam(required = false) UUID puestoId,
            @RequestParam(required = false) UUID comercianteId,
            @RequestParam(required = false) UUID tipoId,
            @RequestParam(required = false) OrigenIncidencia origen,
            @RequestParam(required = false) PrioridadIncidencia prioridad,
            @RequestParam(required = false) EstadoIncidencia estado,
            @RequestParam(required = false) Long responsableId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime fechaHasta,
            @RequestParam(required = false) UUID inspeccionId,
            @RequestParam(required = false) String q,
            @PageableDefault(size = 20, sort = "fechaReporte", direction = Sort.Direction.DESC) Pageable pageable) {

        return ResponseEntity.ok(incidenciaService.buscarPaginado(
                mercadoId, sectorId, pabellonId, puestoId, comercianteId,
                tipoId, origen, prioridad, estado, responsableId, fechaDesde, fechaHasta, inspeccionId, q, pageable));
    }

    // 3. Registrar Incidencia
    @PostMapping("/incidencias")
    @PreAuthorize("hasAuthority('MERCADOS_INCIDENCIA_CREAR')")
    public ResponseEntity<IncidenciaResponse> registrarIncidencia(
            @Valid @RequestBody IncidenciaCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails != null ? userDetails.getUsername() : "SISTEMA";
        IncidenciaResponse response = incidenciaService.registrar(request, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 4. Detalle Completo de Incidencia
    @GetMapping("/incidencias/{id}")
    @PreAuthorize("hasAuthority('MERCADOS_INCIDENCIA_VER')")
    public ResponseEntity<IncidenciaDetailResponse> obtenerDetalleIncidencia(@PathVariable UUID id) {
        return ResponseEntity.ok(incidenciaService.obtenerDetalle(id));
    }

    // 5. Asignar Responsable
    @PostMapping("/incidencias/{id}/asignar")
    @PreAuthorize("hasAuthority('MERCADOS_INCIDENCIA_ASIGNAR')")
    public ResponseEntity<IncidenciaResponse> asignarResponsable(
            @PathVariable UUID id,
            @Valid @RequestBody IncidenciaAsignacionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(incidenciaService.asignarResponsable(id, request, userDetails.getUsername()));
    }

    // 6. Registrar Actuación
    @PostMapping("/incidencias/{id}/actuaciones")
    @PreAuthorize("hasAuthority('MERCADOS_INCIDENCIA_ATENDER')")
    public ResponseEntity<IncidenciaResponse> registrarActuacion(
            @PathVariable UUID id,
            @Valid @RequestBody IncidenciaActuacionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(incidenciaService.registrarActuacion(id, request, userDetails.getUsername()));
    }

    // 7. Adjuntar Evidencia
    @PostMapping("/incidencias/{id}/evidencias")
    @PreAuthorize("hasAuthority('MERCADOS_INCIDENCIA_EVIDENCIA_GESTIONAR')")
    public ResponseEntity<EvidenciaIncidenciaDto> adjuntarEvidencia(
            @PathVariable UUID id,
            @RequestParam String tipoEvidencia,
            @RequestParam String nombreArchivo,
            @RequestParam String rutaArchivo,
            @RequestParam String contentType,
            @RequestParam Long tamanoBytes,
            @RequestParam String hashSha256,
            @RequestParam(required = false) String descripcion,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED).body(incidenciaService.adjuntarEvidencia(
                id, tipoEvidencia, nombreArchivo, rutaArchivo, contentType, tamanoBytes, hashSha256, descripcion, userDetails.getUsername()));
    }

    // 8. Resolver Incidencia
    @PostMapping("/incidencias/{id}/resolver")
    @PreAuthorize("hasAuthority('MERCADOS_INCIDENCIA_RESOLVER')")
    public ResponseEntity<IncidenciaResponse> resolverIncidencia(
            @PathVariable UUID id,
            @Valid @RequestBody IncidenciaResolucionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(incidenciaService.resolver(id, request, userDetails.getUsername()));
    }

    // 9. Cerrar o Descartar Incidencia
    @PostMapping("/incidencias/{id}/cerrar")
    @PreAuthorize("hasAuthority('MERCADOS_INCIDENCIA_CERRAR')")
    public ResponseEntity<IncidenciaResponse> cerrarIncidencia(
            @PathVariable UUID id,
            @Valid @RequestBody IncidenciaCierreRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(incidenciaService.cerrar(id, request, userDetails.getUsername()));
    }

    // 10. Estadísticas de Incidencias
    @GetMapping("/incidencias/estadisticas")
    @PreAuthorize("hasAuthority('MERCADOS_INCIDENCIA_VER')")
    public ResponseEntity<EstadisticasIncidenciasResponse> obtenerEstadisticas() {
        return ResponseEntity.ok(reporteService.obtenerEstadisticas());
    }

    // 11. Presuntas Infracciones - Listado
    @GetMapping("/presuntas-infracciones")
    @PreAuthorize("hasAuthority('MERCADOS_INFRACCION_VER')")
    public ResponseEntity<Page<PresuntaInfraccionResponse>> listarPresuntasInfracciones(
            @RequestParam(required = false) UUID mercadoId,
            @RequestParam(required = false) UUID comercianteId,
            @RequestParam(required = false) UUID puestoId,
            @RequestParam(required = false) UUID tipificacionId,
            @RequestParam(required = false) EstadoEvaluacionInfraccion estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(required = false) String q,
            @PageableDefault(size = 20, sort = "fechaDeteccion", direction = Sort.Direction.DESC) Pageable pageable) {

        return ResponseEntity.ok(infraccionService.buscarPaginado(
                mercadoId, comercianteId, puestoId, tipificacionId, estado, fechaDesde, fechaHasta, q, pageable));
    }

    // 12. Presuntas Infracciones - Registrar
    @PostMapping("/presuntas-infracciones")
    @PreAuthorize("hasAuthority('MERCADOS_INFRACCION_REGISTRAR')")
    public ResponseEntity<PresuntaInfraccionResponse> registrarPresuntaInfraccion(
            @Valid @RequestBody PresuntaInfraccionCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        PresuntaInfraccionResponse response = infraccionService.registrar(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 13. Presuntas Infracciones - Detalle
    @GetMapping("/presuntas-infracciones/{id}")
    @PreAuthorize("hasAuthority('MERCADOS_INFRACCION_VER')")
    public ResponseEntity<PresuntaInfraccionResponse> obtenerPresuntaInfraccion(@PathVariable UUID id) {
        return ResponseEntity.ok(infraccionService.obtenerPorId(id));
    }

    // 14. Presuntas Infracciones - Evaluar Preliminarmente
    @PostMapping("/presuntas-infracciones/{id}/evaluar")
    @PreAuthorize("hasAuthority('MERCADOS_INFRACCION_EVALUAR')")
    public ResponseEntity<PresuntaInfraccionResponse> evaluarPresuntaInfraccion(
            @PathVariable UUID id,
            @Valid @RequestBody EvaluacionInfraccionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(infraccionService.evaluar(id, request, userDetails.getUsername()));
    }

    // 15. Presuntas Infracciones - Registrar Descargo
    @PostMapping("/presuntas-infracciones/{id}/descargo")
    @PreAuthorize("hasAuthority('MERCADOS_INFRACCION_REGISTRAR')")
    public ResponseEntity<PresuntaInfraccionResponse> registrarDescargo(
            @PathVariable UUID id,
            @Valid @RequestBody DescargoInfraccionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(infraccionService.registrarDescargo(id, request, userDetails.getUsername()));
    }

    // 16. Presuntas Infracciones - Derivar a Procedimiento Administrativo
    @PostMapping("/presuntas-infracciones/{id}/derivar")
    @PreAuthorize("hasAuthority('MERCADOS_INFRACCION_DERIVAR')")
    public ResponseEntity<PresuntaInfraccionResponse> derivarPresuntaInfraccion(
            @PathVariable UUID id,
            @Valid @RequestBody DerivacionInfraccionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(infraccionService.derivar(id, request, userDetails.getUsername()));
    }

    // 17. Presuntas Infracciones - Registrar Decisión Administrativa
    @PostMapping("/presuntas-infracciones/{id}/decision")
    @PreAuthorize("hasAuthority('MERCADOS_INFRACCION_DERIVAR')")
    public ResponseEntity<PresuntaInfraccionResponse> registrarDecision(
            @PathVariable UUID id,
            @Valid @RequestBody DecisionInfraccionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(infraccionService.registrarDecision(id, request, userDetails.getUsername()));
    }

    // 17. Catálogo de Tipificaciones
    @GetMapping("/tipificaciones")
    @PreAuthorize("hasAuthority('MERCADOS_INFRACCION_VER')")
    public ResponseEntity<List<TipificacionInfraccionResponse>> listarTipificaciones() {
        return ResponseEntity.ok(tipificacionService.listarVigentes());
    }

    @PostMapping("/tipificaciones")
    @PreAuthorize("hasAuthority('MERCADOS_TIPIFICACION_GESTIONAR')")
    public ResponseEntity<TipificacionInfraccionResponse> crearTipificacion(
            @Valid @RequestBody TipificacionInfraccionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tipificacionService.crear(request, userDetails.getUsername()));
    }
}
