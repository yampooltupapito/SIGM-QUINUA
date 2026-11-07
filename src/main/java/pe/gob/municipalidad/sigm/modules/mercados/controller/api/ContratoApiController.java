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
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoContrato;
import pe.gob.municipalidad.sigm.modules.mercados.service.ContratoExportService;
import pe.gob.municipalidad.sigm.modules.mercados.service.ContratoOcupacionService;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/mercados/contratos")
public class ContratoApiController {

    private final ContratoOcupacionService contratoService;
    private final ContratoExportService exportService;

    public ContratoApiController(ContratoOcupacionService contratoService, ContratoExportService exportService) {
        this.contratoService = contratoService;
        this.exportService = exportService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('MERCADOS_CONTRATO_VER') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Page<ContratoResponse>> listar(
            @RequestParam(required = false) String termino,
            @RequestParam(required = false) UUID mercadoId,
            @RequestParam(required = false) EstadoContrato estado,
            @RequestParam(required = false) String tipoContratoCodigo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @PageableDefault(size = 10, sort = "fechaInicio") Pageable pageable) {
        return ResponseEntity.ok(contratoService.buscarConFiltros(
                termino, mercadoId, estado, tipoContratoCodigo, fechaDesde, fechaHasta, pageable));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('MERCADOS_CONTRATO_CREAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<ContratoResponse> crearBorrador(
            @Valid @RequestBody ContratoCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails != null ? userDetails.getUsername() : "sistema";
        return ResponseEntity.status(HttpStatus.CREATED).body(contratoService.crearBorrador(request, username));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('MERCADOS_CONTRATO_VER') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<ContratoDetailResponse> obtenerDetalle(@PathVariable UUID id) {
        return ResponseEntity.ok(contratoService.obtenerDetalle(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('MERCADOS_CONTRATO_EDITAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<ContratoResponse> actualizarBorrador(
            @PathVariable UUID id,
            @Valid @RequestBody ContratoUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails != null ? userDetails.getUsername() : "sistema";
        return ResponseEntity.ok(contratoService.actualizarBorrador(id, request, username));
    }

    @PostMapping("/{id}/revision")
    @PreAuthorize("hasAuthority('MERCADOS_CONTRATO_REVISAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<ContratoResponse> enviarARevision(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails != null ? userDetails.getUsername() : "sistema";
        return ResponseEntity.ok(contratoService.enviarARevision(id, username));
    }

    @PostMapping("/{id}/observar")
    @PreAuthorize("hasAuthority('MERCADOS_CONTRATO_REVISAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<ContratoResponse> observar(
            @PathVariable UUID id,
            @RequestBody ContratoEstadoRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails != null ? userDetails.getUsername() : "sistema";
        return ResponseEntity.ok(contratoService.observar(id, request, username));
    }

    @PostMapping("/{id}/aprobar")
    @PreAuthorize("hasAuthority('MERCADOS_CONTRATO_APROBAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<ContratoResponse> aprobar(
            @PathVariable UUID id,
            @RequestBody ContratoEstadoRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails != null ? userDetails.getUsername() : "sistema";
        return ResponseEntity.ok(contratoService.aprobar(id, request, username));
    }

    @PostMapping("/{id}/formalizar")
    @PreAuthorize("hasAuthority('MERCADOS_CONTRATO_FORMALIZAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<ContratoResponse> formalizar(
            @PathVariable UUID id,
            @RequestBody ContratoEstadoRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails != null ? userDetails.getUsername() : "sistema";
        return ResponseEntity.ok(contratoService.formalizar(id, request, username));
    }

    @PostMapping("/{id}/suspender")
    @PreAuthorize("hasAuthority('MERCADOS_CONTRATO_SUSPENDER') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<ContratoResponse> suspender(
            @PathVariable UUID id,
            @RequestBody ContratoEstadoRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails != null ? userDetails.getUsername() : "sistema";
        return ResponseEntity.ok(contratoService.suspender(id, request, username));
    }

    @PostMapping("/{id}/terminar")
    @PreAuthorize("hasAuthority('MERCADOS_CONTRATO_TERMINAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<ContratoResponse> terminar(
            @PathVariable UUID id,
            @RequestBody ContratoEstadoRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails != null ? userDetails.getUsername() : "sistema";
        return ResponseEntity.ok(contratoService.terminar(id, request, username));
    }

    @PostMapping("/{id}/anular")
    @PreAuthorize("hasAuthority('MERCADOS_CONTRATO_TERMINAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<ContratoResponse> anular(
            @PathVariable UUID id,
            @RequestBody ContratoEstadoRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails != null ? userDetails.getUsername() : "sistema";
        return ResponseEntity.ok(contratoService.anular(id, request, username));
    }

    @GetMapping("/{id}/pdf")
    @PreAuthorize("hasAuthority('MERCADOS_CONTRATO_VER') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<byte[]> descargarPdf(@PathVariable UUID id) {
        byte[] pdfBytes = contratoService.generarPdf(id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "contrato_" + id + ".pdf");
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    @GetMapping("/exportar/excel")
    @PreAuthorize("hasAuthority('MERCADOS_DOCUMENTO_EXPORTAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<byte[]> exportarExcel(
            @RequestParam(required = false) String termino,
            @RequestParam(required = false) UUID mercadoId,
            @RequestParam(required = false) EstadoContrato estado,
            @RequestParam(required = false) String tipoContratoCodigo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta) {
        byte[] bytes = exportService.exportarExcel(termino, mercadoId, estado, tipoContratoCodigo, fechaDesde, fechaHasta);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.ms-excel;charset=UTF-8"));
        headers.setContentDispositionFormData("attachment", "contratos_quinua.csv");
        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }

    @GetMapping("/exportar/pdf")
    @PreAuthorize("hasAuthority('MERCADOS_DOCUMENTO_EXPORTAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<byte[]> exportarPdf(
            @RequestParam(required = false) String termino,
            @RequestParam(required = false) UUID mercadoId,
            @RequestParam(required = false) EstadoContrato estado,
            @RequestParam(required = false) String tipoContratoCodigo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta) {
        byte[] bytes = exportService.exportarPdf(termino, mercadoId, estado, tipoContratoCodigo, fechaDesde, fechaHasta);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "reporte_contratos_quinua.pdf");
        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }
}
