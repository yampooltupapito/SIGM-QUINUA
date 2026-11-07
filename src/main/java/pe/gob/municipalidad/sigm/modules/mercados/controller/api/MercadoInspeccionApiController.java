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
import org.springframework.web.multipart.MultipartFile;
import pe.gob.municipalidad.sigm.modules.mercados.dto.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoInspeccion;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoMedidaCorrectiva;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoInspeccionMercado;
import pe.gob.municipalidad.sigm.modules.mercados.service.*;
import pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
public class MercadoInspeccionApiController {

    private final InspeccionMercadoService inspeccionService;
    private final VerificacionYHallazgoService verificacionService;
    private final MedidaCorrectivaService medidaService;
    private final ActaInspeccionService actaService;
    private final PlantillaInspeccionService plantillaService;
    private final SupervisionMercadoReporteService reporteService;

    public MercadoInspeccionApiController(InspeccionMercadoService inspeccionService,
                                         VerificacionYHallazgoService verificacionService,
                                         MedidaCorrectivaService medidaService,
                                         ActaInspeccionService actaService,
                                         PlantillaInspeccionService plantillaService,
                                         SupervisionMercadoReporteService reporteService) {
        this.inspeccionService = inspeccionService;
        this.verificacionService = verificacionService;
        this.medidaService = medidaService;
        this.actaService = actaService;
        this.plantillaService = plantillaService;
        this.reporteService = reporteService;
    }

    // --- 1. BANDEJA DE INSPECCIONES ---
    @GetMapping("/api/mercados/inspecciones")
    @PreAuthorize("hasAuthority('MERCADOS_INSPECCION_VER') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Page<InspeccionResponse>> listarInspecciones(
            @RequestParam(required = false) UUID mercadoId,
            @RequestParam(required = false) UUID sectorId,
            @RequestParam(required = false) UUID pabellonId,
            @RequestParam(required = false) UUID puestoId,
            @RequestParam(required = false) UUID comercianteId,
            @RequestParam(required = false) TipoInspeccionMercado tipo,
            @RequestParam(required = false) EstadoInspeccion estado,
            @RequestParam(required = false) Long inspectorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(required = false) String q,
            @PageableDefault(size = 10, sort = "fechaProgramada") Pageable pageable) {
        return ResponseEntity.ok(inspeccionService.buscarPaginado(mercadoId, sectorId, pabellonId, puestoId, comercianteId,
                tipo, estado, inspectorId, fechaDesde, fechaHasta, q, pageable));
    }

    @PostMapping("/api/mercados/inspecciones")
    @PreAuthorize("hasAuthority('MERCADOS_INSPECCION_CREAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<InspeccionResponse> crearInspeccion(
            @Valid @RequestBody InspeccionCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String username = userDetails != null ? userDetails.getUsername() : "sistema";
        return ResponseEntity.status(HttpStatus.CREATED).body(inspeccionService.crear(request, username));
    }

    @GetMapping("/api/mercados/inspecciones/{id}")
    @PreAuthorize("hasAuthority('MERCADOS_INSPECCION_VER') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<InspeccionDetailResponse> obtenerDetalle(@PathVariable UUID id) {
        return ResponseEntity.ok(inspeccionService.obtenerDetalle(id));
    }

    @PostMapping("/api/mercados/inspecciones/{id}/programar")
    @PreAuthorize("hasAuthority('MERCADOS_INSPECCION_PROGRAMAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<InspeccionResponse> programar(
            @PathVariable UUID id,
            @Valid @RequestBody InspeccionProgramacionRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String username = resolverUsername(userDetails);
        return ResponseEntity.ok(inspeccionService.programar(id, request, username));
    }

    @PostMapping("/api/mercados/inspecciones/{id}/asignar")
    @PreAuthorize("hasAuthority('MERCADOS_INSPECCION_ASIGNAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<InspeccionResponse> asignarInspectores(
            @PathVariable UUID id,
            @Valid @RequestBody InspeccionAsignacionRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String username = resolverUsername(userDetails);
        return ResponseEntity.ok(inspeccionService.asignarInspectores(id, request, username));
    }

    @PostMapping("/api/mercados/inspecciones/{id}/iniciar")
    @PreAuthorize("hasAuthority('MERCADOS_INSPECCION_EJECUTAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<InspeccionResponse> iniciarInspeccion(
            @PathVariable UUID id,
            @RequestBody(required = false) InspeccionInicioRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String username = resolverUsername(userDetails);
        return ResponseEntity.ok(inspeccionService.iniciarInspeccion(id, request, username));
    }

    @PostMapping("/api/mercados/inspecciones/{id}/finalizar")
    @PreAuthorize("hasAuthority('MERCADOS_INSPECCION_EJECUTAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<InspeccionResponse> finalizarInspeccion(
            @PathVariable UUID id,
            @RequestParam(required = false, defaultValue = "CONFORME") String resultadoGeneral,
            @RequestParam(required = false) String observaciones,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String username = resolverUsername(userDetails);
        return ResponseEntity.ok(inspeccionService.finalizarInspeccion(id, resultadoGeneral, observaciones, username));
    }

    @PostMapping("/api/mercados/inspecciones/{id}/cancelar")
    @PreAuthorize("hasAuthority('MERCADOS_INSPECCION_PROGRAMAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<InspeccionResponse> cancelarInspeccion(
            @PathVariable UUID id,
            @RequestParam String motivo,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String username = resolverUsername(userDetails);
        return ResponseEntity.ok(inspeccionService.cancelarInspeccion(id, motivo, username));
    }

    @PostMapping("/api/mercados/inspecciones/{id}/seguimiento")
    @PreAuthorize("hasAuthority('MERCADOS_INSPECCION_CREAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<InspeccionResponse> crearSeguimiento(
            @PathVariable UUID id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaProgramada,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String username = resolverUsername(userDetails);
        return ResponseEntity.status(HttpStatus.CREATED).body(inspeccionService.crearInspeccionSeguimiento(id, fechaProgramada, username));
    }

    // --- 2. VERIFICACIONES, HALLAZGOS Y EVIDENCIAS ---
    @PostMapping("/api/mercados/inspecciones/{id}/verificaciones")
    @PreAuthorize("hasAuthority('MERCADOS_INSPECCION_EJECUTAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<List<RespuestaVerificacionDto>> registrarVerificaciones(
            @PathVariable UUID id,
            @RequestBody List<VerificacionItemRequest> items,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String username = resolverUsername(userDetails);
        Long usuarioId = userDetails != null ? userDetails.getId() : null;
        return ResponseEntity.ok(verificacionService.registrarVerificaciones(id, items, username, usuarioId));
    }

    @PostMapping("/api/mercados/inspecciones/{id}/hallazgos")
    @PreAuthorize("hasAuthority('MERCADOS_INSPECCION_HALLAZGO_REGISTRAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<HallazgoResponse> registrarHallazgo(
            @PathVariable UUID id,
            @Valid @RequestBody HallazgoCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String username = resolverUsername(userDetails);
        Long usuarioId = userDetails != null ? userDetails.getId() : null;
        return ResponseEntity.status(HttpStatus.CREATED).body(verificacionService.registrarHallazgo(id, request, username, usuarioId));
    }

    @GetMapping("/api/mercados/inspecciones/{id}/hallazgos")
    @PreAuthorize("hasAuthority('MERCADOS_INSPECCION_VER') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<List<HallazgoResponse>> listarHallazgos(@PathVariable UUID id) {
        return ResponseEntity.ok(verificacionService.listarHallazgos(id));
    }

    @PostMapping(value = "/api/mercados/inspecciones/{id}/evidencias", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('MERCADOS_INSPECCION_EVIDENCIA_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<?> adjuntarEvidencia(
            @PathVariable UUID id,
            @RequestParam(required = false) UUID hallazgoId,
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam(required = false, defaultValue = "FOTOGRAFIA") String tipoEvidencia,
            @RequestParam(required = false) String descripcion,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String username = resolverUsername(userDetails);
        Long usuarioId = userDetails != null ? userDetails.getId() : null;
        verificacionService.adjuntarEvidencia(id, hallazgoId, archivo, tipoEvidencia, descripcion, username, usuarioId);
        return ResponseEntity.status(HttpStatus.CREATED).body("{\"message\":\"Evidencia adjunta exitosamente\"}");
    }

    // --- 3. MEDIDAS CORRECTIVAS Y SEGUIMIENTO ---
    @PostMapping("/api/mercados/medidas-correctivas/{hallazgoId}/crear")
    @PreAuthorize("hasAuthority('MERCADOS_INSPECCION_MEDIDA_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<MedidaCorrectivaResponse> crearMedidaCorrectiva(
            @PathVariable UUID hallazgoId,
            @Valid @RequestBody MedidaCorrectivaRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String username = resolverUsername(userDetails);
        return ResponseEntity.status(HttpStatus.CREATED).body(medidaService.crearMedida(hallazgoId, request, username));
    }

    @GetMapping("/api/mercados/medidas-correctivas")
    @PreAuthorize("hasAuthority('MERCADOS_INSPECCION_VER') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Page<MedidaCorrectivaResponse>> listarMedidasSeguimiento(
            @RequestParam(required = false) EstadoMedidaCorrectiva estado,
            @RequestParam(required = false) UUID mercadoId,
            @RequestParam(required = false) UUID comercianteId,
            @RequestParam(required = false, defaultValue = "false") boolean soloVencidas,
            @PageableDefault(size = 10, sort = "fechaLimite") Pageable pageable) {
        return ResponseEntity.ok(medidaService.buscarSeguimientoPaginado(estado, mercadoId, comercianteId, soloVencidas, pageable));
    }

    @PostMapping("/api/mercados/medidas-correctivas/{id}/subsanaciones")
    @PreAuthorize("hasAuthority('MERCADOS_INSPECCION_MEDIDA_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<MedidaCorrectivaResponse> presentarSubsanacion(
            @PathVariable UUID id,
            @Valid @RequestBody SubsanacionRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String username = resolverUsername(userDetails);
        Long usuarioId = userDetails != null ? userDetails.getId() : null;
        return ResponseEntity.ok(medidaService.presentarSubsanacion(id, request, username, usuarioId));
    }

    @PostMapping("/api/mercados/medidas-correctivas/subsanaciones/{subsanacionId}/verificar")
    @PreAuthorize("hasAuthority('MERCADOS_INSPECCION_SUBSANACION_VERIFICAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<MedidaCorrectivaResponse> verificarSubsanacion(
            @PathVariable UUID subsanacionId,
            @Valid @RequestBody VerificacionSubsanacionRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String username = resolverUsername(userDetails);
        Long inspectorId = userDetails != null ? userDetails.getId() : null;
        return ResponseEntity.ok(medidaService.verificarSubsanacion(subsanacionId, request, username, inspectorId));
    }

    // --- 4. ACTAS DE INSPECCIÓN ---
    @PostMapping("/api/mercados/inspecciones/{id}/acta")
    @PreAuthorize("hasAuthority('MERCADOS_INSPECCION_ACTA_ELABORAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<ActaInspeccionResponse> elaborarActa(
            @PathVariable UUID id,
            @Valid @RequestBody ActaInspeccionRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String username = resolverUsername(userDetails);
        Long usuarioId = userDetails != null ? userDetails.getId() : null;
        return ResponseEntity.ok(actaService.elaborarBorradorActa(id, request, username, usuarioId));
    }

    @PostMapping("/api/mercados/inspecciones/{id}/acta/aprobar")
    @PreAuthorize("hasAuthority('MERCADOS_INSPECCION_ACTA_APROBAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<ActaInspeccionResponse> aprobarYEmitirActa(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String username = resolverUsername(userDetails);
        Long usuarioId = userDetails != null ? userDetails.getId() : null;
        return ResponseEntity.ok(actaService.aprobarYEmitirActa(id, username, usuarioId));
    }

    @GetMapping("/api/mercados/inspecciones/{id}/acta/pdf")
    @PreAuthorize("hasAuthority('MERCADOS_INSPECCION_VER') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<byte[]> descargarPdfActa(@PathVariable UUID id) {
        byte[] pdfBytes = actaService.generarPdfActa(id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "Acta_Inspeccion_" + id + ".pdf");
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    // --- 5. VERIFICACIÓN PÚBLICA QR (SIN AUTENTICACIÓN) ---
    @GetMapping("/verificar/mercados/acta/{codigoVerificacion}")
    public ResponseEntity<VerificacionActaPublicaResponse> verificarActaPublica(@PathVariable String codigoVerificacion) {
        return ResponseEntity.ok(actaService.verificarActaPublica(codigoVerificacion));
    }

    // --- 6. ESTADÍSTICAS Y REPORTES ---
    @GetMapping("/api/mercados/inspecciones/estadisticas")
    @PreAuthorize("hasAuthority('MERCADOS_INSPECCION_VER') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<EstadisticasSupervisionResponse> obtenerEstadisticas() {
        return ResponseEntity.ok(reporteService.obtenerEstadisticas());
    }

    @GetMapping("/api/mercados/inspecciones/reportes/exportar")
    @PreAuthorize("hasAuthority('MERCADOS_INSPECCION_REPORTES') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<byte[]> exportarReporte(
            @RequestParam(defaultValue = "EXCEL") String formato,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        HttpHeaders headers = new HttpHeaders();
        if ("PDF".equalsIgnoreCase(formato)) {
            byte[] bytes = reporteService.exportarInspeccionesPdf(desde, hasta);
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "Inspecciones_Mercados_" + LocalDate.now() + ".pdf");
            return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
        } else {
            byte[] bytes = reporteService.exportarInspeccionesExcel(desde, hasta);
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", "Inspecciones_Mercados_" + LocalDate.now() + ".xlsx");
            return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
        }
    }

    private String resolverUsername(CustomUserDetails userDetails) {
        if (userDetails != null && userDetails.getUsername() != null && !userDetails.getUsername().isBlank()) {
            return userDetails.getUsername();
        }
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getName() != null && !auth.getName().isBlank()) {
            return auth.getName();
        }
        return "sistema";
    }
}
