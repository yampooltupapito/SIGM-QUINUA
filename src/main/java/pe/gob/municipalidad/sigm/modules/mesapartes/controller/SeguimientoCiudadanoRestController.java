package pe.gob.municipalidad.sigm.modules.mesapartes.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pe.gob.municipalidad.sigm.common.response.ApiResponse;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.SeguimientoDtos.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.ConsultaPublicaExpedienteService;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.ConstanciaRecepcionService;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.SeguimientoCiudadanoService;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.VerificacionDocumentalService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api")
public class SeguimientoCiudadanoRestController {

    private final ConsultaPublicaExpedienteService consultaPublicaService;
    private final VerificacionDocumentalService verificacionDocumentalService;
    private final SeguimientoCiudadanoService seguimientoCiudadanoService;
    private final ConstanciaRecepcionService constanciaRecepcionService;

    @org.springframework.beans.factory.annotation.Value("${sigm.app.base-url:http://localhost:8080}")
    private String baseUrl;

    public SeguimientoCiudadanoRestController(ConsultaPublicaExpedienteService consultaPublicaService,
                                              VerificacionDocumentalService verificacionDocumentalService,
                                              SeguimientoCiudadanoService seguimientoCiudadanoService,
                                              ConstanciaRecepcionService constanciaRecepcionService) {
        this.consultaPublicaService = consultaPublicaService;
        this.verificacionDocumentalService = verificacionDocumentalService;
        this.seguimientoCiudadanoService = seguimientoCiudadanoService;
        this.constanciaRecepcionService = constanciaRecepcionService;
    }

    /**
     * Endpoint público para consultar el avance del expediente mediante código seguro o número oficial.
     * Aplica rate limiting por IP y protección contra enumeración de datos.
     */
    @PostMapping("/publico/mesa-partes/consultar")
    public ResponseEntity<ApiResponse<ConsultaPublicaResponse>> consultarPublicamente(
            @Valid @RequestBody ConsultaPublicaRequest request,
            HttpServletRequest httpRequest
    ) {
        String ip = obtenerIpCliente(httpRequest);
        String ua = httpRequest.getHeader("User-Agent");
        ConsultaPublicaResponse resp = consultaPublicaService.consultarPublicamente(request, ip, ua);
        return ResponseEntity.ok(ApiResponse.of("Consulta de estado de trámite realizada con éxito.", resp));
    }

    /**
     * Endpoint público para verificar la autenticidad e integridad de documentos emitidos (POST).
     */
    @PostMapping("/publico/documentos/verificar")
    public ResponseEntity<ApiResponse<VerificarDocumentoResponse>> verificarDocumento(
            @Valid @RequestBody VerificarDocumentoRequest request,
            HttpServletRequest httpRequest
    ) {
        String ip = obtenerIpCliente(httpRequest);
        String ua = httpRequest.getHeader("User-Agent");
        VerificarDocumentoResponse resp = verificacionDocumentalService.verificarDocumento(request.getCodigoVerificacion(), ip, ua);
        return ResponseEntity.ok(ApiResponse.of("Verificación documental procesada.", resp));
    }

    /**
     * Endpoint público para verificar documento vía parámetro URL o escaneo de QR (GET).
     */
    @GetMapping("/publico/documentos/verificar")
    public ResponseEntity<ApiResponse<VerificarDocumentoResponse>> verificarDocumentoPorParametro(
            @RequestParam("codigo") String codigo,
            HttpServletRequest httpRequest
    ) {
        String ip = obtenerIpCliente(httpRequest);
        String ua = httpRequest.getHeader("User-Agent");
        VerificarDocumentoResponse resp = verificacionDocumentalService.verificarDocumento(codigo, ip, ua);
        return ResponseEntity.ok(ApiResponse.of("Verificación documental procesada.", resp));
    }

    /**
     * Ficha integral de seguimiento del expediente para el ciudadano autenticado propietario.
     * Prevención estricta de IDOR.
     */
    @GetMapping("/mesa-partes/mis-solicitudes/{id}/seguimiento")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<SeguimientoCiudadanoDetalleDto>> obtenerSeguimientoCiudadano(
            @PathVariable Long id,
            Authentication auth
    ) {
        SeguimientoCiudadanoDetalleDto detalle = seguimientoCiudadanoService.obtenerSeguimientoDetalle(id, auth.getName());
        return ResponseEntity.ok(ApiResponse.of("Ficha de seguimiento ciudadano recuperada.", detalle));
    }

    /**
     * Línea de tiempo comunicable para el ciudadano.
     */
    @GetMapping("/mesa-partes/mis-solicitudes/{id}/historial")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<EventoLineaTiempoCiudadanaDto>>> obtenerHistorialCiudadano(
            @PathVariable Long id,
            Authentication auth
    ) {
        List<EventoLineaTiempoCiudadanaDto> eventos = seguimientoCiudadanoService.obtenerLineaTiempoCiudadana(id, auth.getName());
        return ResponseEntity.ok(ApiResponse.of("Línea de tiempo del expediente recuperada.", eventos));
    }

    /**
     * Documentos disponibles y descargables del expediente para el ciudadano autenticado.
     */
    @GetMapping("/mesa-partes/mis-solicitudes/{id}/documentos")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<DocumentoDisponibleDto>>> obtenerDocumentosDisponibles(
            @PathVariable Long id,
            Authentication auth
    ) {
        List<DocumentoDisponibleDto> docs = seguimientoCiudadanoService.obtenerDocumentosDisponibles(id, auth.getName());
        return ResponseEntity.ok(ApiResponse.of("Documentos disponibles del expediente.", docs));
    }

    /**
     * Descarga de documento autorizado del expediente para el administrado.
     */
    @GetMapping("/mesa-partes/mis-solicitudes/{id}/documentos/{docId}/descargar")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> descargarDocumentoAutorizado(
            @PathVariable Long id,
            @PathVariable Long docId,
            Authentication auth
    ) {
        byte[] bytes = seguimientoCiudadanoService.descargarDocumentoAutorizado(id, docId, auth.getName());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "Documento-Expediente-" + id + "-" + docId + ".pdf");
        headers.setContentLength(bytes.length);
        return ResponseEntity.ok().headers(headers).body(bytes);
    }

    /**
     * Descarga de constancia oficial de recepción si el trámite fue formalmente recibido.
     */
    @GetMapping("/mesa-partes/mis-solicitudes/{id}/constancia")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> descargarConstancia(
            @PathVariable Long id,
            Authentication auth
    ) {
        byte[] pdfBytes = constanciaRecepcionService.generarConstanciaRecepcionPdf(id, baseUrl, auth.getName());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("inline", "Constancia-Recepcion-" + id + ".pdf");
        headers.setContentLength(pdfBytes.length);
        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }

    private String obtenerIpCliente(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr() != null ? request.getRemoteAddr() : "127.0.0.1";
    }
}
