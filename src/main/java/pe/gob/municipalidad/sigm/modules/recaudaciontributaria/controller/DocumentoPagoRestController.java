package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.documentopago.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoDocumentoPagoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.DocumentoPagoTributarioService;
import pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/tributaria/documentos")
@RequiredArgsConstructor
public class DocumentoPagoRestController {

    private final DocumentoPagoTributarioService documentoService;

    @PostMapping("/emitir")
    @PreAuthorize("hasAuthority('TRIBUTARIA_DOCUMENTOS_EMITIR') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_CAJERO', 'ROLE_TRIBUTACION')")
    public ResponseEntity<DocumentoPagoResponse> emitirDocumento(
            @Valid @RequestBody EmitirDocumentoRequest request,
            @AuthenticationPrincipal CustomUserDetails user,
            HttpServletRequest httpRequest) {

        DocumentoPagoResponse res = documentoService.emitirDocumento(
                request, user, httpRequest.getRemoteAddr(), httpRequest.getHeader("User-Agent"));
        return ResponseEntity.ok(res);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('TRIBUTARIA_DOCUMENTOS_VER') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_CAJERO', 'ROLE_TRIBUTACION')")
    public ResponseEntity<DocumentoPagoResponse> obtenerPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(documentoService.obtenerPorId(id));
    }

    @GetMapping("/{id}/pdf")
    @PreAuthorize("hasAuthority('TRIBUTARIA_DOCUMENTOS_DESCARGAR') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_CAJERO', 'ROLE_TRIBUTACION')")
    public ResponseEntity<byte[]> descargarPdf(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails user,
            HttpServletRequest httpRequest) {

        DocumentoPagoResponse doc = documentoService.obtenerPorId(id);
        byte[] pdf = documentoService.obtenerContenidoPdf(
                id, user, httpRequest.getRemoteAddr(), httpRequest.getHeader("User-Agent"));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + doc.getCodigoDocumento() + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/{id}/qr")
    @PreAuthorize("hasAuthority('TRIBUTARIA_DOCUMENTOS_VER') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_CAJERO', 'ROLE_TRIBUTACION')")
    public ResponseEntity<byte[]> obtenerQr(@PathVariable UUID id) {
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(documentoService.obtenerQrPng(id));
    }

    @PostMapping("/{id}/anular")
    @PreAuthorize("hasAuthority('TRIBUTARIA_DOCUMENTOS_ANULAR') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_TRIBUTACION')")
    public ResponseEntity<DocumentoPagoResponse> anularDocumento(
            @PathVariable UUID id,
            @Valid @RequestBody AnularDocumentoRequest request,
            @AuthenticationPrincipal CustomUserDetails user,
            HttpServletRequest httpRequest) {

        DocumentoPagoResponse res = documentoService.anularDocumento(
                id, request, user, httpRequest.getRemoteAddr(), httpRequest.getHeader("User-Agent"));
        return ResponseEntity.ok(res);
    }

    @PostMapping("/{id}/sustituir")
    @PreAuthorize("hasAuthority('TRIBUTARIA_DOCUMENTOS_SUSTITUIR') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_TRIBUTACION')")
    public ResponseEntity<DocumentoPagoResponse> sustituirDocumento(
            @PathVariable UUID id,
            @Valid @RequestBody SustituirDocumentoRequest request,
            @AuthenticationPrincipal CustomUserDetails user,
            HttpServletRequest httpRequest) {

        DocumentoPagoResponse res = documentoService.sustituirDocumento(
                id, request, user, httpRequest.getRemoteAddr(), httpRequest.getHeader("User-Agent"));
        return ResponseEntity.ok(res);
    }

    @GetMapping("/verificar/{codigo}")
    public ResponseEntity<VerificacionPublicaDocumentoResponse> verificarPublico(
            @PathVariable String codigo,
            HttpServletRequest httpRequest) {

        return ResponseEntity.ok(documentoService.verificarPublicamente(
                codigo, httpRequest.getRemoteAddr(), httpRequest.getHeader("User-Agent")));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('TRIBUTARIA_DOCUMENTOS_VER') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_CAJERO', 'ROLE_TRIBUTACION')")
    public ResponseEntity<Page<DocumentoPagoResponse>> listar(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) UUID tipoDocId,
            @RequestParam(required = false) UUID serieId,
            @RequestParam(required = false) UUID contribuyenteId,
            @RequestParam(required = false) UUID pagoId,
            @RequestParam(required = false) EstadoDocumentoPagoTributario estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            Pageable pageable) {

        return ResponseEntity.ok(documentoService.listarDocumentos(
                query, tipoDocId, serieId, contribuyenteId, pagoId, estado, desde, hasta, pageable));
    }
}
