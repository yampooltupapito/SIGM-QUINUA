package pe.gob.municipalidad.sigm.common.document.controller;

import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pe.gob.municipalidad.sigm.common.document.dto.DocumentDtos.*;
import pe.gob.municipalidad.sigm.common.document.entity.CategoriaDocumento;
import pe.gob.municipalidad.sigm.common.document.entity.ClasificacionAcceso;
import pe.gob.municipalidad.sigm.common.document.entity.EstadoDocumento;
import pe.gob.municipalidad.sigm.common.document.service.DocumentoService;
import pe.gob.municipalidad.sigm.common.response.ApiResponse;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/documentos")
public class DocumentoRestController {

    private final DocumentoService documentoService;

    public DocumentoRestController(DocumentoService documentoService) {
        this.documentoService = documentoService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'OPERADOR', 'MESA_PARTES', 'JEFE_OFICINA') or hasAuthority('DOCUMENTO_CREAR')")
    public ResponseEntity<ApiResponse<DocumentoResponse>> subirDocumento(
            @RequestPart("archivo") MultipartFile archivo,
            @RequestParam(value = "categoria", required = false) CategoriaDocumento categoria,
            @RequestParam(value = "clasificacionAcceso", required = false) ClasificacionAcceso clasificacionAcceso,
            @RequestParam(value = "descripcion", required = false) String descripcion,
            @RequestParam(value = "oficinaId", required = false) Long oficinaId,
            @RequestParam(value = "moduloOrigen", required = false) String moduloOrigen,
            @RequestParam(value = "tipoEntidad", required = false) String tipoEntidad,
            @RequestParam(value = "entidadId", required = false) String entidadId,
            @RequestParam(value = "rolDocumento", required = false) String rolDocumento,
            Authentication authentication
    ) {
        String username = authentication != null ? authentication.getName() : null;
        SubirDocumentoRequest req = SubirDocumentoRequest.builder()
                .categoria(categoria)
                .clasificacionAcceso(clasificacionAcceso)
                .descripcion(descripcion)
                .oficinaId(oficinaId)
                .moduloOrigen(moduloOrigen)
                .tipoEntidad(tipoEntidad)
                .entidadId(entidadId)
                .rolDocumento(rolDocumento)
                .build();

        DocumentoResponse response = documentoService.subirDocumento(archivo, req, username);
        return ApiResponse.ok("Documento subido y registrado exitosamente.", response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<DocumentoResponse>>> listarDocumentos(
            @RequestParam(value = "estado", required = false) EstadoDocumento estado,
            @RequestParam(value = "categoria", required = false) CategoriaDocumento categoria,
            @RequestParam(value = "clasificacion", required = false) ClasificacionAcceso clasificacion,
            @RequestParam(value = "termino", required = false) String termino,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "15") int size,
            Authentication authentication
    ) {
        String username = authentication != null ? authentication.getName() : null;
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(size, 100));
        Page<DocumentoResponse> resultado = documentoService.listarDocumentos(estado, categoria, clasificacion, termino, pageable, username);
        return ApiResponse.ok(resultado);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DocumentoResponse>> obtenerPorId(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String username = authentication != null ? authentication.getName() : null;
        return ApiResponse.ok(documentoService.obtenerPorId(id, username));
    }

    @GetMapping("/uuid/{uuid}")
    public ResponseEntity<ApiResponse<DocumentoResponse>> obtenerPorUuid(
            @PathVariable UUID uuid,
            Authentication authentication
    ) {
        String username = authentication != null ? authentication.getName() : null;
        return ApiResponse.ok(documentoService.obtenerPorUuid(uuid, username));
    }

    @GetMapping("/{id}/descargar")
    public ResponseEntity<Resource> descargarDocumento(
            @PathVariable Long id,
            @RequestParam(value = "version", required = false) Integer version,
            Authentication authentication
    ) {
        String username = authentication != null ? authentication.getName() : null;
        DocumentoResponse doc = documentoService.obtenerPorId(id, username);
        Resource resource = documentoService.descargarDocumento(id, version, username);

        String encodedFilename = URLEncoder.encode(doc.getNombreOriginal(), StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        String contentDisposition = "attachment; filename=\"" + doc.getNombreOriginal() + "\"; filename*=UTF-8''" + encodedFilename;

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .header(HttpHeaders.CONTENT_TYPE, doc.getMimeType())
                .header("X-Content-Type-Options", "nosniff")
                .header("X-Frame-Options", "DENY")
                .body(resource);
    }

    @GetMapping("/{id}/vista-previa")
    public ResponseEntity<Resource> vistaPreviaDocumento(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String username = authentication != null ? authentication.getName() : null;
        DocumentoResponse doc = documentoService.obtenerPorId(id, username);
        Resource resource = documentoService.vistaPreviaDocumento(id, username);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + doc.getNombreOriginal() + "\"")
                .header(HttpHeaders.CONTENT_TYPE, doc.getMimeType())
                .header("X-Content-Type-Options", "nosniff")
                .header("X-Frame-Options", "SAMEORIGIN")
                .body(resource);
    }

    @GetMapping("/{id}/versiones")
    public ResponseEntity<ApiResponse<List<DocumentoVersionResponse>>> listarVersiones(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String username = authentication != null ? authentication.getName() : null;
        return ApiResponse.ok(documentoService.listarVersiones(id, username));
    }

    @PostMapping(value = "/{id}/versiones", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'OPERADOR', 'MESA_PARTES') or hasAuthority('DOCUMENTO_EDITAR')")
    public ResponseEntity<ApiResponse<DocumentoResponse>> crearNuevaVersion(
            @PathVariable Long id,
            @RequestPart("archivo") MultipartFile archivo,
            @RequestParam("motivoCambio") String motivoCambio,
            Authentication authentication
    ) {
        String username = authentication != null ? authentication.getName() : null;
        NuevaVersionRequest req = NuevaVersionRequest.builder().motivoCambio(motivoCambio).build();
        DocumentoResponse response = documentoService.crearNuevaVersion(id, archivo, req, username);
        return ApiResponse.ok("Nueva versión registrada exitosamente.", response);
    }

    @GetMapping("/{id}/integridad")
    public ResponseEntity<ApiResponse<VerificacionIntegridadResponse>> verificarIntegridad(
            @PathVariable Long id,
            @RequestParam(value = "version", required = false) Integer version,
            Authentication authentication
    ) {
        String username = authentication != null ? authentication.getName() : null;
        return ApiResponse.ok(documentoService.verificarIntegridad(id, version, username));
    }

    @GetMapping("/{id}/auditoria")
    public ResponseEntity<ApiResponse<List<DocumentoAuditoriaResponse>>> listarAuditoria(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String username = authentication != null ? authentication.getName() : null;
        return ApiResponse.ok(documentoService.listarAuditoria(id, username));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'OPERADOR') or hasAuthority('DOCUMENTO_EDITAR')")
    public ResponseEntity<ApiResponse<DocumentoResponse>> actualizarMetadatos(
            @PathVariable Long id,
            @RequestBody @Valid ActualizarDocumentoRequest request,
            Authentication authentication
    ) {
        String username = authentication != null ? authentication.getName() : null;
        return ApiResponse.ok("Metadatos actualizados.", documentoService.actualizarMetadatos(id, request, username));
    }

    @PostMapping("/{id}/archivar")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN') or hasAuthority('DOCUMENTO_ELIMINAR')")
    public ResponseEntity<ApiResponse<DocumentoResponse>> archivarDocumento(
            @PathVariable Long id,
            @RequestBody @Valid EliminarDocumentoRequest request,
            Authentication authentication
    ) {
        String username = authentication != null ? authentication.getName() : null;
        return ApiResponse.ok("Documento archivado institucionalmente.", documentoService.archivarDocumento(id, request, username));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN') or hasAuthority('DOCUMENTO_ELIMINAR')")
    public ResponseEntity<ApiResponse<DocumentoResponse>> eliminarLogicamente(
            @PathVariable Long id,
            @RequestBody @Valid EliminarDocumentoRequest request,
            Authentication authentication
    ) {
        String username = authentication != null ? authentication.getName() : null;
        return ApiResponse.ok("Documento eliminado lógicamente del catálogo activo.", documentoService.eliminarLogicamente(id, request, username));
    }

    @PatchMapping("/{id}/conservacion")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN') or hasAuthority('DOCUMENTO_ADMIN')")
    public ResponseEntity<ApiResponse<DocumentoResponse>> cambiarBloqueoConservacion(
            @PathVariable Long id,
            @RequestParam boolean bloqueo,
            Authentication authentication
    ) {
        String username = authentication != null ? authentication.getName() : null;
        return ApiResponse.ok("Bloqueo de conservación actualizado.", documentoService.cambiarBloqueoConservacion(id, bloqueo, username));
    }

    @GetMapping("/estadisticas")
    public ResponseEntity<ApiResponse<DocumentoEstadisticasResponse>> obtenerEstadisticas() {
        return ApiResponse.ok(documentoService.obtenerEstadisticas());
    }

    @PostMapping("/generar-pdf")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'OPERADOR') or hasAuthority('DOCUMENTO_CREAR')")
    public ResponseEntity<ApiResponse<DocumentoResponse>> generarPdf(
            @RequestBody @Valid GenerarPdfRequest request,
            Authentication authentication
    ) {
        String username = authentication != null ? authentication.getName() : null;
        DocumentoResponse response = documentoService.generarYGuardarPdf(request, username);
        return ApiResponse.ok("Documento PDF generado y archivado exitosamente.", response);
    }
}
