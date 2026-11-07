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
import org.springframework.web.bind.annotation.*;
import pe.gob.municipalidad.sigm.modules.mercados.dto.*;
import pe.gob.municipalidad.sigm.modules.mercados.entity.ProcedimientoGestionOcupacion;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoSolicitudGestion;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoSolicitudGestion;
import pe.gob.municipalidad.sigm.modules.mercados.repository.ProcedimientoGestionOcupacionRepository;
import pe.gob.municipalidad.sigm.modules.mercados.service.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/mercados")
public class MercadoGestionOcupacionApiController {

    private final RenovacionOcupacionService renovacionService;
    private final CambioTitularService cambioTitularService;
    private final TransferenciaPuestoService transferenciaService;
    private final EvaluacionGestionOcupacionService evaluacionService;
    private final FormalizacionGestionOcupacionService formalizacionService;
    private final HistorialTitularidadService historialTitularidadService;
    private final ReporteGestionOcupacionService reporteService;
    private final ProcedimientoGestionOcupacionRepository procedimientoRepository;

    public MercadoGestionOcupacionApiController(
            RenovacionOcupacionService renovacionService,
            CambioTitularService cambioTitularService,
            TransferenciaPuestoService transferenciaService,
            EvaluacionGestionOcupacionService evaluacionService,
            FormalizacionGestionOcupacionService formalizacionService,
            HistorialTitularidadService historialTitularidadService,
            ReporteGestionOcupacionService reporteService,
            ProcedimientoGestionOcupacionRepository procedimientoRepository) {
        this.renovacionService = renovacionService;
        this.cambioTitularService = cambioTitularService;
        this.transferenciaService = transferenciaService;
        this.evaluacionService = evaluacionService;
        this.formalizacionService = formalizacionService;
        this.historialTitularidadService = historialTitularidadService;
        this.reporteService = reporteService;
        this.procedimientoRepository = procedimientoRepository;
    }

    // --- Catálogo de Procedimientos ---
    @GetMapping("/procedimientos-gestion")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ProcedimientoGestionOcupacion>> listarProcedimientos() {
        return ResponseEntity.ok(procedimientoRepository.findByActivoTrueOrderByNombreAsc());
    }

    // --- 1. Renovaciones ---
    @GetMapping("/renovaciones")
    @PreAuthorize("hasAuthority('MERCADOS_RENOVACION_VER')")
    public ResponseEntity<Page<SolicitudGestionOcupacionResponse>> listarRenovaciones(
            @RequestParam(required = false) UUID mercadoId,
            @RequestParam(required = false) UUID puestoId,
            @RequestParam(required = false) UUID comercianteId,
            @RequestParam(required = false) TipoSolicitudGestion tipo,
            @RequestParam(required = false) EstadoSolicitudGestion estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(required = false) String q,
            @PageableDefault(size = 20, sort = "fechaPresentacion", direction = Sort.Direction.DESC) Pageable pageable) {

        return ResponseEntity.ok(renovacionService.buscarPaginado(mercadoId, puestoId, comercianteId, tipo, estado, fechaDesde, fechaHasta, q, pageable));
    }

    @PostMapping("/renovaciones")
    @PreAuthorize("hasAuthority('MERCADOS_RENOVACION_CREAR')")
    public ResponseEntity<SolicitudGestionOcupacionResponse> solicitarRenovacion(
            @Valid @RequestBody RenovacionRequest request,
            Principal principal) {
        String username = principal != null ? principal.getName() : "SISTEMA";
        SolicitudGestionOcupacionResponse resp = renovacionService.solicitarRenovacion(request, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @GetMapping("/renovaciones/{id}")
    @PreAuthorize("hasAuthority('MERCADOS_RENOVACION_VER')")
    public ResponseEntity<SolicitudGestionOcupacionDetailResponse> obtenerDetalleRenovacion(@PathVariable UUID id) {
        return ResponseEntity.ok(evaluacionService.obtenerDetalle(id));
    }

    // --- 2. Cambios de Titular ---
    @GetMapping("/cambios-titular")
    @PreAuthorize("hasAuthority('MERCADOS_CAMBIO_TITULAR_VER')")
    public ResponseEntity<Page<SolicitudGestionOcupacionResponse>> listarCambiosTitular(
            @RequestParam(required = false) UUID mercadoId,
            @RequestParam(required = false) UUID puestoId,
            @RequestParam(required = false) UUID comercianteId,
            @RequestParam(required = false) EstadoSolicitudGestion estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(required = false) String q,
            @PageableDefault(size = 20, sort = "fechaPresentacion", direction = Sort.Direction.DESC) Pageable pageable) {

        return ResponseEntity.ok(cambioTitularService.buscarPaginado(mercadoId, puestoId, comercianteId, estado, fechaDesde, fechaHasta, q, pageable));
    }

    @PostMapping("/cambios-titular")
    @PreAuthorize("hasAuthority('MERCADOS_CAMBIO_TITULAR_CREAR')")
    public ResponseEntity<SolicitudGestionOcupacionResponse> solicitarCambioTitular(
            @Valid @RequestBody CambioTitularRequest request,
            Principal principal) {
        String username = principal != null ? principal.getName() : "SISTEMA";
        SolicitudGestionOcupacionResponse resp = cambioTitularService.solicitarCambioTitular(request, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @GetMapping("/cambios-titular/{id}")
    @PreAuthorize("hasAuthority('MERCADOS_CAMBIO_TITULAR_VER')")
    public ResponseEntity<SolicitudGestionOcupacionDetailResponse> obtenerDetalleCambioTitular(@PathVariable UUID id) {
        return ResponseEntity.ok(evaluacionService.obtenerDetalle(id));
    }

    // --- 3. Transferencias Autorizadas ---
    @GetMapping("/transferencias")
    @PreAuthorize("hasAuthority('MERCADOS_TRANSFERENCIA_VER')")
    public ResponseEntity<Page<SolicitudGestionOcupacionResponse>> listarTransferencias(
            @RequestParam(required = false) UUID mercadoId,
            @RequestParam(required = false) UUID puestoId,
            @RequestParam(required = false) UUID comercianteId,
            @RequestParam(required = false) EstadoSolicitudGestion estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(required = false) String q,
            @PageableDefault(size = 20, sort = "fechaPresentacion", direction = Sort.Direction.DESC) Pageable pageable) {

        return ResponseEntity.ok(transferenciaService.buscarPaginado(mercadoId, puestoId, comercianteId, estado, fechaDesde, fechaHasta, q, pageable));
    }

    @PostMapping("/transferencias")
    @PreAuthorize("hasAuthority('MERCADOS_TRANSFERENCIA_CREAR')")
    public ResponseEntity<SolicitudGestionOcupacionResponse> solicitarTransferencia(
            @Valid @RequestBody TransferenciaRequest request,
            Principal principal) {
        String username = principal != null ? principal.getName() : "SISTEMA";
        SolicitudGestionOcupacionResponse resp = transferenciaService.solicitarTransferencia(request, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @GetMapping("/transferencias/{id}")
    @PreAuthorize("hasAuthority('MERCADOS_TRANSFERENCIA_VER')")
    public ResponseEntity<SolicitudGestionOcupacionDetailResponse> obtenerDetalleTransferencia(@PathVariable UUID id) {
        return ResponseEntity.ok(evaluacionService.obtenerDetalle(id));
    }

    // --- 4. Flujo de Evaluación, Observaciones y Decisión ---
    @PostMapping("/gestiones-ocupacion/{id}/presentar")
    @PreAuthorize("hasAnyAuthority('MERCADOS_RENOVACION_CREAR', 'MERCADOS_CAMBIO_TITULAR_CREAR', 'MERCADOS_TRANSFERENCIA_CREAR')")
    public ResponseEntity<SolicitudGestionOcupacionResponse> presentarSolicitud(
            @PathVariable UUID id,
            Principal principal) {
        String username = principal != null ? principal.getName() : "SISTEMA";
        return ResponseEntity.ok(evaluacionService.presentarSolicitud(id, username));
    }

    @PostMapping("/gestiones-ocupacion/{id}/observar")
    @PreAuthorize("hasAnyAuthority('MERCADOS_RENOVACION_EVALUAR', 'MERCADOS_CAMBIO_TITULAR_EVALUAR', 'MERCADOS_TRANSFERENCIA_EVALUAR')")
    public ResponseEntity<ObservacionGestionResponse> registrarObservacion(
            @PathVariable UUID id,
            @Valid @RequestBody ObservacionGestionRequest request,
            Principal principal) {
        String username = principal != null ? principal.getName() : "SISTEMA";
        return ResponseEntity.status(HttpStatus.CREATED).body(evaluacionService.registrarObservacion(id, request, username));
    }

    @PostMapping("/gestiones-ocupacion/{id}/subsanar")
    @PreAuthorize("hasAnyAuthority('MERCADOS_RENOVACION_CREAR', 'MERCADOS_CAMBIO_TITULAR_CREAR', 'MERCADOS_TRANSFERENCIA_CREAR')")
    public ResponseEntity<SubsanacionGestionResponse> registrarSubsanacion(
            @PathVariable UUID id,
            @Valid @RequestBody SubsanacionGestionRequest request,
            Principal principal) {
        String username = principal != null ? principal.getName() : "SISTEMA";
        return ResponseEntity.status(HttpStatus.CREATED).body(evaluacionService.registrarSubsanacion(id, request, username));
    }

    @PostMapping("/gestiones-ocupacion/{id}/evaluar")
    @PreAuthorize("hasAnyAuthority('MERCADOS_RENOVACION_EVALUAR', 'MERCADOS_CAMBIO_TITULAR_EVALUAR', 'MERCADOS_TRANSFERENCIA_EVALUAR')")
    public ResponseEntity<SolicitudGestionOcupacionResponse> evaluarSolicitud(
            @PathVariable UUID id,
            @Valid @RequestBody EvaluacionGestionRequest request,
            Principal principal) {
        String username = principal != null ? principal.getName() : "SISTEMA";
        return ResponseEntity.ok(evaluacionService.evaluarSolicitud(id, request, username));
    }

    @PostMapping("/gestiones-ocupacion/{id}/aprobar")
    @PreAuthorize("hasAnyAuthority('MERCADOS_RENOVACION_APROBAR', 'MERCADOS_CAMBIO_TITULAR_APROBAR', 'MERCADOS_TRANSFERENCIA_APROBAR')")
    public ResponseEntity<SolicitudGestionOcupacionResponse> aprobarSolicitud(
            @PathVariable UUID id,
            @Valid @RequestBody AprobacionGestionRequest request,
            Principal principal) {
        String username = principal != null ? principal.getName() : "SISTEMA";
        return ResponseEntity.ok(evaluacionService.aprobarSolicitud(id, request, username));
    }

    @PostMapping("/gestiones-ocupacion/{id}/rechazar")
    @PreAuthorize("hasAnyAuthority('MERCADOS_RENOVACION_APROBAR', 'MERCADOS_CAMBIO_TITULAR_APROBAR', 'MERCADOS_TRANSFERENCIA_APROBAR')")
    public ResponseEntity<SolicitudGestionOcupacionResponse> rechazarSolicitud(
            @PathVariable UUID id,
            @Valid @RequestBody RechazoGestionRequest request,
            Principal principal) {
        String username = principal != null ? principal.getName() : "SISTEMA";
        return ResponseEntity.ok(evaluacionService.rechazarSolicitud(id, request, username));
    }

    // --- 5. Formalización Efectiva ---
    @PostMapping("/gestiones-ocupacion/{id}/formalizar")
    @PreAuthorize("hasAuthority('MERCADOS_GESTION_OCUPACION_FORMALIZAR')")
    public ResponseEntity<SolicitudGestionOcupacionResponse> formalizarSolicitud(
            @PathVariable UUID id,
            @Valid @RequestBody FormalizacionGestionRequest request,
            Principal principal) {
        String username = principal != null ? principal.getName() : "SISTEMA";
        return ResponseEntity.ok(formalizacionService.formalizarSolicitud(id, request, username));
    }

    // --- 6. Historial de Titularidad ---
    @GetMapping("/puestos/{id}/historial-titularidad")
    @PreAuthorize("hasAuthority('MERCADOS_GESTION_OCUPACION_HISTORIAL_VER')")
    public ResponseEntity<List<HistorialTitularidadResponse>> obtenerHistorialTitularidadPuesto(@PathVariable UUID id) {
        return ResponseEntity.ok(historialTitularidadService.listarPorPuesto(id));
    }

    // --- 7. Estadísticas ---
    @GetMapping("/gestiones-ocupacion/estadisticas")
    @PreAuthorize("hasAnyAuthority('MERCADOS_RENOVACION_VER', 'MERCADOS_CAMBIO_TITULAR_VER', 'MERCADOS_TRANSFERENCIA_VER')")
    public ResponseEntity<EstadisticasGestionOcupacionResponse> obtenerEstadisticas() {
        return ResponseEntity.ok(reporteService.obtenerEstadisticas());
    }
}
