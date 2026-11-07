package pe.gob.municipalidad.sigm.modules.mercados.controller.api;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import pe.gob.municipalidad.sigm.common.document.pdf.QrCodeGenerator;
import pe.gob.municipalidad.sigm.modules.mercados.dto.DocumentoEstadisticasResponse;
import pe.gob.municipalidad.sigm.modules.mercados.dto.VerificacionDocumentoResponse;
import pe.gob.municipalidad.sigm.modules.mercados.dto.VigenciaResponse;
import pe.gob.municipalidad.sigm.modules.mercados.service.VerificacionDocumentoMercadoService;
import pe.gob.municipalidad.sigm.modules.mercados.service.VigenciaOcupacionService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mercados/documentos")
public class DocumentoMercadoApiController {

    private final VigenciaOcupacionService vigenciaService;
    private final VerificacionDocumentoMercadoService verificacionService;
    private final QrCodeGenerator qrCodeGenerator;

    public DocumentoMercadoApiController(
            VigenciaOcupacionService vigenciaService,
            VerificacionDocumentoMercadoService verificacionService,
            QrCodeGenerator qrCodeGenerator) {
        this.vigenciaService = vigenciaService;
        this.verificacionService = verificacionService;
        this.qrCodeGenerator = qrCodeGenerator;
    }

    @GetMapping("/vencimientos")
    @PreAuthorize("hasAuthority('MERCADOS_CONTRATO_VER') or hasAuthority('MERCADOS_AUTORIZACION_VER') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<List<VigenciaResponse>> consultarVencimientos(
            @RequestParam(defaultValue = "30") int dias) {
        return ResponseEntity.ok(vigenciaService.consultarProximosVencimientos(dias));
    }

    @GetMapping("/vencidos")
    @PreAuthorize("hasAuthority('MERCADOS_CONTRATO_VER') or hasAuthority('MERCADOS_AUTORIZACION_VER') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<List<VigenciaResponse>> consultarVencidos() {
        return ResponseEntity.ok(vigenciaService.consultarVencidos());
    }

    @GetMapping("/estadisticas")
    @PreAuthorize("hasAuthority('MERCADOS_CONTRATO_VER') or hasAuthority('MERCADOS_AUTORIZACION_VER') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<DocumentoEstadisticasResponse> obtenerEstadisticas() {
        return ResponseEntity.ok(vigenciaService.obtenerEstadisticas());
    }

    @PostMapping("/alertas/emitir")
    @PreAuthorize("hasAuthority('MERCADOS_CONTRATO_APROBAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Map<String, Object>> emitirAlertas(
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails != null ? userDetails.getUsername() : "sistema";
        int emitidas = vigenciaService.emitirAlertasVencimiento(username);
        return ResponseEntity.ok(Map.of(
                "mensaje", "Alertas de vencimiento procesadas exitosamente.",
                "totalEmitidas", emitidas
        ));
    }

    @GetMapping("/verificar/{codigoSeguro}")
    public ResponseEntity<VerificacionDocumentoResponse> verificarDocumento(@PathVariable String codigoSeguro) {
        return ResponseEntity.ok(verificacionService.verificarPorCodigoSeguro(codigoSeguro));
    }

    @GetMapping(value = "/verificar/{codigoSeguro}/qr", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> obtenerQrPng(@PathVariable String codigoSeguro) {
        String url = "http://localhost:8080/verificar/mercados/documento/" + codigoSeguro;
        return ResponseEntity.ok(qrCodeGenerator.generateQrCodePng(url, 150, 150));
    }
}
