package pe.gob.municipalidad.sigm.modules.serenazgo.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.documentos.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.DocumentoSeguridadCiudadanaService;
import pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/serenazgo/documentos")
@RequiredArgsConstructor
public class DocumentosRestController {

    private final DocumentoSeguridadCiudadanaService documentoService;

    @GetMapping
    @PreAuthorize("hasAuthority('SERENAZGO_DOCUMENTOS_VER')")
    public ResponseEntity<Page<DocumentoSeguridadResponse>> listar(
            @ModelAttribute DocumentoFiltroRequest filtro,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(documentoService.listar(filtro, pageable));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('SERENAZGO_DOCUMENTOS_CREAR')")
    public ResponseEntity<DocumentoSeguridadResponse> registrar(
            @ModelAttribute @Valid DocumentoCargaRequest request,
            @AuthenticationPrincipal CustomUserDetails user,
            HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        return ResponseEntity.ok(documentoService.registrarDocumento(request, user, ip));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SERENAZGO_DOCUMENTOS_VER')")
    public ResponseEntity<DocumentoSeguridadDetalleResponse> obtenerDetalle(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(documentoService.obtenerDetalle(id, user));
    }

    @GetMapping("/{id}/descargar")
    @PreAuthorize("hasAuthority('SERENAZGO_DOCUMENTOS_DESCARGAR')")
    public ResponseEntity<Resource> descargar(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails user,
            HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        Resource resource = documentoService.descargar(id, user, ip);
        DocumentoSeguridadDetalleResponse det = documentoService.obtenerDetalle(id, user);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(det.getMimeType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + det.getNombreOriginal() + "\"")
                .body(resource);
    }

    @GetMapping("/{id}/vista-previa")
    @PreAuthorize("hasAuthority('SERENAZGO_DOCUMENTOS_VER')")
    public ResponseEntity<Resource> vistaPrevia(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails user) {
        Resource resource = documentoService.obtenerVistaPrevia(id, user);
        DocumentoSeguridadDetalleResponse det = documentoService.obtenerDetalle(id, user);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(det.getMimeType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + det.getNombreOriginal() + "\"")
                .body(resource);
    }

    @PostMapping("/{id}/verificar")
    @PreAuthorize("hasAuthority('SERENAZGO_DOCUMENTOS_VERIFICAR')")
    public ResponseEntity<VerificacionIntegridadResponse> verificarIntegridad(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(documentoService.verificarIntegridad(id, user));
    }

    @PostMapping("/{id}/revisar")
    @PreAuthorize("hasAuthority('SERENAZGO_DOCUMENTOS_REVISAR')")
    public ResponseEntity<DocumentoSeguridadResponse> revisar(
            @PathVariable UUID id,
            @RequestBody(required = false) TransicionEstadoRequest req,
            @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(documentoService.revisar(id, req, user));
    }

    @PostMapping("/{id}/observar")
    @PreAuthorize("hasAuthority('SERENAZGO_DOCUMENTOS_REVISAR')")
    public ResponseEntity<DocumentoSeguridadResponse> observar(
            @PathVariable UUID id,
            @RequestBody(required = false) TransicionEstadoRequest req,
            @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(documentoService.observar(id, req, user));
    }

    @PostMapping("/{id}/validar")
    @PreAuthorize("hasAuthority('SERENAZGO_DOCUMENTOS_VALIDAR')")
    public ResponseEntity<DocumentoSeguridadResponse> validar(
            @PathVariable UUID id,
            @RequestBody(required = false) TransicionEstadoRequest req,
            @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(documentoService.validarAdministrativamente(id, req, user));
    }

    @PostMapping("/{id}/restringir")
    @PreAuthorize("hasAuthority('SERENAZGO_DOCUMENTOS_RESTRINGIR')")
    public ResponseEntity<DocumentoSeguridadResponse> restringir(
            @PathVariable UUID id,
            @RequestBody(required = false) TransicionEstadoRequest req,
            @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(documentoService.restringir(id, req, user));
    }

    @PostMapping("/{id}/archivar")
    @PreAuthorize("hasAuthority('SERENAZGO_DOCUMENTOS_ARCHIVAR')")
    public ResponseEntity<DocumentoSeguridadResponse> archivar(
            @PathVariable UUID id,
            @RequestBody(required = false) TransicionEstadoRequest req,
            @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(documentoService.archivar(id, req, user));
    }

    @PostMapping(value = "/{id}/versiones", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('SERENAZGO_DOCUMENTOS_VERSIONAR')")
    public ResponseEntity<DocumentoSeguridadResponse> crearVersion(
            @PathVariable UUID id,
            @ModelAttribute NuevaVersionRequest req,
            @AuthenticationPrincipal CustomUserDetails user,
            HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        return ResponseEntity.ok(documentoService.crearNuevaVersion(id, req, user, ip));
    }

    @GetMapping("/estadisticas")
    @PreAuthorize("hasAuthority('SERENAZGO_DOCUMENTOS_VER')")
    public ResponseEntity<DocumentoEstadisticasResponse> estadisticas() {
        return ResponseEntity.ok(documentoService.obtenerEstadisticas());
    }

    @GetMapping("/exportar/excel")
    @PreAuthorize("hasAuthority('SERENAZGO_DOCUMENTOS_EXPORTAR')")
    public ResponseEntity<byte[]> exportarExcel(@ModelAttribute DocumentoFiltroRequest filtro) {
        byte[] bytes = documentoService.exportarExcel(filtro);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"evidencias_serenazgo.xlsx\"")
                .body(bytes);
    }

    @GetMapping("/{id}/exportar/pdf")
    @PreAuthorize("hasAuthority('SERENAZGO_DOCUMENTOS_EXPORTAR')")
    public ResponseEntity<byte[]> exportarPdf(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails user) {
        byte[] bytes = documentoService.exportarPdfFicha(id, user);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"ficha_evidencia_" + id + ".pdf\"")
                .body(bytes);
    }
}
