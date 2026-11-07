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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pe.gob.municipalidad.sigm.modules.mercados.dto.*;
import pe.gob.municipalidad.sigm.modules.mercados.entity.CajaMercado;
import pe.gob.municipalidad.sigm.modules.mercados.service.*;
import pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/mercados")
public class MercadoFinanzasApiController {

    private final ConceptoCobroMercadoService conceptoService;
    private final TarifaMercadoService tarifaService;
    private final ObligacionMercadoService obligacionService;
    private final PagoMercadoService pagoService;
    private final CajaMercadoService cajaService;
    private final CuentaCorrienteMercadoService cuentaCorrienteService;
    private final FinanzasMercadoReporteService reporteService;

    public MercadoFinanzasApiController(ConceptoCobroMercadoService conceptoService,
                                       TarifaMercadoService tarifaService,
                                       ObligacionMercadoService obligacionService,
                                       PagoMercadoService pagoService,
                                       CajaMercadoService cajaService,
                                       CuentaCorrienteMercadoService cuentaCorrienteService,
                                       FinanzasMercadoReporteService reporteService) {
        this.conceptoService = conceptoService;
        this.tarifaService = tarifaService;
        this.obligacionService = obligacionService;
        this.pagoService = pagoService;
        this.cajaService = cajaService;
        this.cuentaCorrienteService = cuentaCorrienteService;
        this.reporteService = reporteService;
    }

    // --- 1. CONCEPTOS DE COBRO ---
    @GetMapping("/conceptos-cobro")
    @PreAuthorize("hasAuthority('MERCADOS_FINANZAS_VER') or hasAuthority('MERCADOS_CONCEPTO_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Page<ConceptoCobroResponse>> listarConceptos(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String q,
            @PageableDefault(size = 10, sort = "nombre") Pageable pageable) {
        return ResponseEntity.ok(conceptoService.buscarPaginado(estado, q, pageable));
    }

    @GetMapping("/conceptos-cobro/activos")
    @PreAuthorize("hasAuthority('MERCADOS_FINANZAS_VER') or hasAuthority('MERCADOS_OBLIGACION_CREAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<List<ConceptoCobroResponse>> listarConceptosActivos() {
        return ResponseEntity.ok(conceptoService.listarActivos());
    }

    @PostMapping("/conceptos-cobro")
    @PreAuthorize("hasAuthority('MERCADOS_CONCEPTO_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<ConceptoCobroResponse> crearConcepto(
            @Valid @RequestBody ConceptoCobroRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String username = userDetails != null ? userDetails.getUsername() : "sistema";
        return ResponseEntity.status(HttpStatus.CREATED).body(conceptoService.crear(request, username));
    }

    @GetMapping("/conceptos-cobro/{id}")
    @PreAuthorize("hasAuthority('MERCADOS_FINANZAS_VER') or hasAuthority('MERCADOS_CONCEPTO_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<ConceptoCobroResponse> obtenerConcepto(@PathVariable UUID id) {
        return ResponseEntity.ok(conceptoService.obtenerPorId(id));
    }

    @PutMapping("/conceptos-cobro/{id}")
    @PreAuthorize("hasAuthority('MERCADOS_CONCEPTO_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<ConceptoCobroResponse> actualizarConcepto(
            @PathVariable UUID id,
            @Valid @RequestBody ConceptoCobroRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String username = userDetails != null ? userDetails.getUsername() : "sistema";
        return ResponseEntity.ok(conceptoService.actualizar(id, request, username));
    }

    // --- 2. TARIFAS AUTORIZADAS ---
    @GetMapping("/tarifas")
    @PreAuthorize("hasAuthority('MERCADOS_FINANZAS_VER') or hasAuthority('MERCADOS_TARIFA_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Page<TarifaMercadoResponse>> listarTarifas(
            @RequestParam(required = false) UUID conceptoId,
            @RequestParam(required = false) UUID mercadoId,
            @RequestParam(required = false) String estado,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(tarifaService.buscarPaginado(conceptoId, mercadoId, estado, pageable));
    }

    @PostMapping("/tarifas")
    @PreAuthorize("hasAuthority('MERCADOS_TARIFA_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<TarifaMercadoResponse> crearTarifa(
            @Valid @RequestBody TarifaMercadoRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String username = userDetails != null ? userDetails.getUsername() : "sistema";
        return ResponseEntity.status(HttpStatus.CREATED).body(tarifaService.crear(request, username));
    }

    @GetMapping("/tarifas/aplicable")
    @PreAuthorize("hasAuthority('MERCADOS_FINANZAS_VER') or hasAuthority('MERCADOS_OBLIGACION_CREAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<TarifaMercadoResponse> obtenerTarifaAplicable(
            @RequestParam UUID conceptoId,
            @RequestParam(required = false) UUID mercadoId,
            @RequestParam(required = false) Long tipoPuestoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(tarifaService.obtenerTarifaAplicable(conceptoId, mercadoId, tipoPuestoId, fecha));
    }

    @GetMapping("/tarifas/{id}")
    @PreAuthorize("hasAuthority('MERCADOS_FINANZAS_VER') or hasAuthority('MERCADOS_TARIFA_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<TarifaMercadoResponse> obtenerTarifa(@PathVariable UUID id) {
        return ResponseEntity.ok(tarifaService.obtenerPorId(id));
    }

    @PutMapping("/tarifas/{id}")
    @PreAuthorize("hasAuthority('MERCADOS_TARIFA_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<TarifaMercadoResponse> actualizarTarifa(
            @PathVariable UUID id,
            @Valid @RequestBody TarifaMercadoRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String username = userDetails != null ? userDetails.getUsername() : "sistema";
        return ResponseEntity.ok(tarifaService.actualizarConVersionamiento(id, request, username));
    }

    // --- 3. OBLIGACIONES Y CUOTAS ---
    @GetMapping("/obligaciones")
    @PreAuthorize("hasAuthority('MERCADOS_OBLIGACION_VER') or hasAuthority('MERCADOS_FINANZAS_VER') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Page<ObligacionResponse>> listarObligaciones(
            @RequestParam(required = false) UUID mercadoId,
            @RequestParam(required = false) UUID comercianteId,
            @RequestParam(required = false) UUID puestoId,
            @RequestParam(required = false) UUID conceptoId,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(required = false) String q,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(obligacionService.buscarPaginado(
                mercadoId, comercianteId, puestoId, conceptoId, estado, fechaDesde, fechaHasta, q, pageable));
    }

    @PostMapping("/obligaciones")
    @PreAuthorize("hasAuthority('MERCADOS_OBLIGACION_CREAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<ObligacionResponse> crearObligacion(
            @Valid @RequestBody ObligacionCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String username = userDetails != null ? userDetails.getUsername() : "sistema";
        return ResponseEntity.status(HttpStatus.CREATED).body(obligacionService.crear(request, username));
    }

    @GetMapping("/obligaciones/{id}")
    @PreAuthorize("hasAuthority('MERCADOS_OBLIGACION_VER') or hasAuthority('MERCADOS_FINANZAS_VER') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<ObligacionDetailResponse> obtenerDetalleObligacion(@PathVariable UUID id) {
        return ResponseEntity.ok(obligacionService.obtenerDetalle(id));
    }

    @PostMapping("/obligaciones/{id}/anular")
    @PreAuthorize("hasAuthority('MERCADOS_OBLIGACION_AJUSTAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Void> anularObligacion(
            @PathVariable UUID id,
            @RequestParam String motivo,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String username = userDetails != null ? userDetails.getUsername() : "sistema";
        obligacionService.anular(id, motivo, username);
        return ResponseEntity.noContent().build();
    }

    // --- 4. PAGOS Y RECAUDACIÓN ---
    @GetMapping("/pagos")
    @PreAuthorize("hasAuthority('MERCADOS_PAGO_VER') or hasAuthority('MERCADOS_FINANZAS_VER') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Page<PagoResponse>> listarPagos(
            @RequestParam(required = false) UUID comercianteId,
            @RequestParam(required = false) UUID cajaSesionId,
            @RequestParam(required = false) String medioPago,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime fechaHasta,
            @RequestParam(required = false) String q,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(pagoService.buscarPaginado(comercianteId, cajaSesionId, medioPago, estado, fechaDesde, fechaHasta, q, pageable));
    }

    @PostMapping("/pagos")
    @PreAuthorize("hasAuthority('MERCADOS_PAGO_REGISTRAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<PagoResponse> registrarPago(
            @Valid @RequestBody PagoCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String username = userDetails != null ? userDetails.getUsername() : "caja";
        Long usuarioId = userDetails != null ? userDetails.getId() : null;
        return ResponseEntity.status(HttpStatus.CREATED).body(pagoService.registrarPago(request, username, usuarioId));
    }

    @GetMapping("/pagos/{id}")
    @PreAuthorize("hasAuthority('MERCADOS_PAGO_VER') or hasAuthority('MERCADOS_FINANZAS_VER') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<PagoResponse> obtenerPago(@PathVariable UUID id) {
        return ResponseEntity.ok(pagoService.obtenerPorId(id));
    }

    @PostMapping("/pagos/{id}/reversar")
    @PreAuthorize("hasAuthority('MERCADOS_PAGO_REVERSAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<PagoResponse> reversarPago(
            @PathVariable UUID id,
            @Valid @RequestBody ReversoPagoRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String username = userDetails != null ? userDetails.getUsername() : "supervisor";
        Long usuarioId = userDetails != null ? userDetails.getId() : null;
        return ResponseEntity.ok(pagoService.reversarPago(pagoId(id), request, username, usuarioId));
    }

    private UUID pagoId(UUID id) { return id; }

    @GetMapping("/pagos/{id}/constancia")
    @PreAuthorize("hasAuthority('MERCADOS_PAGO_VER') or hasAuthority('MERCADOS_FINANZAS_VER') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<byte[]> descargarConstanciaPdf(@PathVariable UUID id) {
        byte[] pdfBytes = pagoService.generarConstanciaPdf(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"recibo-pago-" + id + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    // --- 5. CUENTA CORRIENTE ---
    @GetMapping("/comerciantes/{id}/cuenta-corriente")
    @PreAuthorize("hasAuthority('MERCADOS_OBLIGACION_VER') or hasAuthority('MERCADOS_FINANZAS_VER') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<CuentaCorrienteResponse> consultarCuentaCorriente(@PathVariable UUID id) {
        return ResponseEntity.ok(cuentaCorrienteService.obtenerCuentaCorrienteComerciante(id));
    }

    // --- 6. CAJA ---
    @GetMapping("/cajas/activas")
    @PreAuthorize("hasAuthority('MERCADOS_CAJA_ABRIR') or hasAuthority('MERCADOS_PAGO_REGISTRAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<List<CajaMercado>> listarCajasActivas() {
        return ResponseEntity.ok(cajaService.listarCajasActivas());
    }

    @GetMapping("/cajas/sesion-activa")
    @PreAuthorize("hasAuthority('MERCADOS_PAGO_REGISTRAR') or hasAuthority('MERCADOS_CAJA_ABRIR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<SesionCajaResponse> obtenerSesionActiva(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long usuarioId = userDetails != null ? userDetails.getId() : null;
        if (usuarioId == null) {
            return ResponseEntity.notFound().build();
        }
        return cajaService.obtenerSesionActivaUsuario(usuarioId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @PostMapping("/cajas/abrir")
    @PreAuthorize("hasAuthority('MERCADOS_CAJA_ABRIR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<SesionCajaResponse> abrirCaja(
            @Valid @RequestBody AperturaCajaRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String username = userDetails != null ? userDetails.getUsername() : "cajero";
        Long usuarioId = userDetails != null ? userDetails.getId() : 1L;
        return ResponseEntity.status(HttpStatus.CREATED).body(cajaService.abrirSesion(request, username, usuarioId));
    }

    @PostMapping("/cajas/sesiones/{id}/cerrar")
    @PreAuthorize("hasAuthority('MERCADOS_CAJA_CERRAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<SesionCajaResponse> cerrarCaja(
            @PathVariable UUID id,
            @Valid @RequestBody CierreCajaRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String username = userDetails != null ? userDetails.getUsername() : "cajero";
        return ResponseEntity.ok(cajaService.cerrarSesion(id, request, username));
    }

    // --- 7. ESTADÍSTICAS Y REPORTES ---
    @GetMapping("/finanzas/estadisticas")
    @PreAuthorize("hasAuthority('MERCADOS_FINANZAS_VER') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<EstadisticasFinancierasResponse> obtenerEstadisticas() {
        return ResponseEntity.ok(reporteService.obtenerEstadisticas());
    }

    @GetMapping("/finanzas/exportar/obligaciones/excel")
    @PreAuthorize("hasAuthority('MERCADOS_FINANZAS_EXPORTAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<byte[]> exportarObligacionesExcel(
            @RequestParam(required = false) UUID mercadoId,
            @RequestParam(required = false) UUID comercianteId,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta) {
        byte[] csv = reporteService.exportarExcelObligaciones(mercadoId, comercianteId, estado, fechaDesde, fechaHasta);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"reporte-obligaciones.csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csv);
    }

    @GetMapping("/finanzas/exportar/obligaciones/pdf")
    @PreAuthorize("hasAuthority('MERCADOS_FINANZAS_EXPORTAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<byte[]> exportarObligacionesPdf(
            @RequestParam(required = false) UUID mercadoId,
            @RequestParam(required = false) UUID comercianteId,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta) {
        byte[] pdf = reporteService.exportarPdfObligaciones(mercadoId, comercianteId, estado, fechaDesde, fechaHasta);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"reporte-obligaciones.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/finanzas/exportar/pagos/excel")
    @PreAuthorize("hasAuthority('MERCADOS_FINANZAS_EXPORTAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<byte[]> exportarPagosExcel(
            @RequestParam(required = false) UUID comercianteId,
            @RequestParam(required = false) UUID cajaSesionId,
            @RequestParam(required = false) String medioPago,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime fechaHasta) {
        byte[] csv = reporteService.exportarExcelPagos(comercianteId, cajaSesionId, medioPago, estado, fechaDesde, fechaHasta);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"reporte-pagos.csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csv);
    }

    @GetMapping("/finanzas/exportar/pagos/pdf")
    @PreAuthorize("hasAuthority('MERCADOS_FINANZAS_EXPORTAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<byte[]> exportarPagosPdf(
            @RequestParam(required = false) UUID comercianteId,
            @RequestParam(required = false) UUID cajaSesionId,
            @RequestParam(required = false) String medioPago,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime fechaHasta) {
        byte[] pdf = reporteService.exportarPdfPagos(comercianteId, cajaSesionId, medioPago, estado, fechaDesde, fechaHasta);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"reporte-pagos.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
