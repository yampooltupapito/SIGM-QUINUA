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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import pe.gob.municipalidad.sigm.modules.mercados.dto.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoAutorizacion;
import pe.gob.municipalidad.sigm.modules.mercados.service.AutorizacionComercialService;
import pe.gob.municipalidad.sigm.modules.mercados.service.AutorizacionExportService;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/mercados/autorizaciones")
public class AutorizacionApiController {

    private final AutorizacionComercialService autorizacionService;
    private final AutorizacionExportService exportService;

    public AutorizacionApiController(
            AutorizacionComercialService autorizacionService,
            AutorizacionExportService exportService) {
        this.autorizacionService = autorizacionService;
        this.exportService = exportService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('MERCADOS_AUTORIZACION_VER') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Page<AutorizacionResponse>> listar(
            @RequestParam(required = false) String termino,
            @RequestParam(required = false) UUID mercadoId,
            @RequestParam(required = false) EstadoAutorizacion estado,
            @RequestParam(required = false) String tipoAutorizacion,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @PageableDefault(size = 10, sort = "fechaSolicitud") Pageable pageable) {
        return ResponseEntity.ok(autorizacionService.buscarConFiltros(
                termino, mercadoId, estado, tipoAutorizacion, fechaDesde, fechaHasta, pageable));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('MERCADOS_AUTORIZACION_CREAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<AutorizacionResponse> crearBorrador(
            @Valid @RequestBody AutorizacionCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails != null ? userDetails.getUsername() : "sistema";
        return ResponseEntity.status(HttpStatus.CREATED).body(autorizacionService.crearBorrador(request, username));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('MERCADOS_AUTORIZACION_VER') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<AutorizacionDetailResponse> obtenerDetalle(@PathVariable UUID id) {
        return ResponseEntity.ok(autorizacionService.obtenerDetalle(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('MERCADOS_AUTORIZACION_CREAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<AutorizacionResponse> actualizarBorrador(
            @PathVariable UUID id,
            @Valid @RequestBody AutorizacionUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails != null ? userDetails.getUsername() : "sistema";
        return ResponseEntity.ok(autorizacionService.actualizarBorrador(id, request, username));
    }

    @PostMapping("/{id}/evaluar")
    @PreAuthorize("hasAuthority('MERCADOS_AUTORIZACION_EVALUAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<AutorizacionResponse> enviarAEvaluacion(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails != null ? userDetails.getUsername() : "sistema";
        return ResponseEntity.ok(autorizacionService.enviarAEvaluacion(id, username));
    }

    @PostMapping("/{id}/observar")
    @PreAuthorize("hasAuthority('MERCADOS_AUTORIZACION_EVALUAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<AutorizacionResponse> observar(
            @PathVariable UUID id,
            @RequestBody AutorizacionEstadoRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails != null ? userDetails.getUsername() : "sistema";
        return ResponseEntity.ok(autorizacionService.observar(id, request, username));
    }

    @PostMapping("/{id}/aprobar")
    @PreAuthorize("hasAuthority('MERCADOS_AUTORIZACION_APROBAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<AutorizacionResponse> aprobar(
            @PathVariable UUID id,
            @RequestBody AutorizacionEstadoRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails != null ? userDetails.getUsername() : "sistema";
        return ResponseEntity.ok(autorizacionService.aprobar(id, request, username));
    }

    @PostMapping("/{id}/emitir")
    @PreAuthorize("hasAuthority('MERCADOS_AUTORIZACION_EMITIR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<AutorizacionResponse> emitir(
            @PathVariable UUID id,
            @RequestBody AutorizacionEstadoRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails != null ? userDetails.getUsername() : "sistema";
        return ResponseEntity.ok(autorizacionService.emitir(id, request, username));
    }

    @PostMapping("/{id}/suspender")
    @PreAuthorize("hasAuthority('MERCADOS_AUTORIZACION_SUSPENDER') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<AutorizacionResponse> suspender(
            @PathVariable UUID id,
            @RequestBody AutorizacionEstadoRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails != null ? userDetails.getUsername() : "sistema";
        return ResponseEntity.ok(autorizacionService.suspender(id, request, username));
    }

    @PostMapping("/{id}/revocar")
    @PreAuthorize("hasAuthority('MERCADOS_AUTORIZACION_REVOCAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<AutorizacionResponse> revocar(
            @PathVariable UUID id,
            @RequestBody AutorizacionEstadoRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails != null ? userDetails.getUsername() : "sistema";
        return ResponseEntity.ok(autorizacionService.revocar(id, request, username));
    }

    @PostMapping("/{id}/anular")
    @PreAuthorize("hasAuthority('MERCADOS_AUTORIZACION_REVOCAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<AutorizacionResponse> anular(
            @PathVariable UUID id,
            @RequestBody AutorizacionEstadoRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails != null ? userDetails.getUsername() : "sistema";
        return ResponseEntity.ok(autorizacionService.anular(id, request, username));
    }

    @GetMapping("/{id}/pdf")
    @PreAuthorize("hasAuthority('MERCADOS_AUTORIZACION_VER') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<byte[]> descargarPdf(@PathVariable UUID id) {
        byte[] pdfBytes = autorizacionService.generarPdf(id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "autorizacion_" + id + ".pdf");
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    @GetMapping("/exportar/excel")
    @PreAuthorize("hasAuthority('MERCADOS_DOCUMENTO_EXPORTAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<byte[]> exportarExcel(
            @RequestParam(required = false) String termino,
            @RequestParam(required = false) UUID mercadoId,
            @RequestParam(required = false) EstadoAutorizacion estado,
            @RequestParam(required = false) String tipoAutorizacion,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta) {
        byte[] bytes = exportService.exportarExcel(termino, mercadoId, estado, tipoAutorizacion, fechaDesde, fechaHasta);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.ms-excel;charset=UTF-8"));
        headers.setContentDispositionFormData("attachment", "autorizaciones_quinua.csv");
        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }

    @GetMapping("/exportar/pdf")
    @PreAuthorize("hasAuthority('MERCADOS_DOCUMENTO_EXPORTAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<byte[]> exportarPdf(
            @RequestParam(required = false) String termino,
            @RequestParam(required = false) UUID mercadoId,
            @RequestParam(required = false) EstadoAutorizacion estado,
            @RequestParam(required = false) String tipoAutorizacion,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta) {
        byte[] bytes = exportService.exportarPdf(termino, mercadoId, estado, tipoAutorizacion, fechaDesde, fechaHasta);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "reporte_autorizaciones_quinua.pdf");
        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }
}
