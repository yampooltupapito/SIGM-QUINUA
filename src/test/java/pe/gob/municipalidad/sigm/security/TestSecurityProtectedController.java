package pe.gob.municipalidad.sigm.security;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import pe.gob.municipalidad.sigm.common.response.ApiResponse;

import java.util.Map;

/**
 * Controlador de prueba para validación de Spring Security, RBAC y autorización por oficina.
 */
@RestController
public class TestSecurityProtectedController {

    @GetMapping("/api/v1/admin/recursos")
    public ResponseEntity<ApiResponse<Map<String, String>>> getAdminResources() {
        return ApiResponse.ok("Recurso administrativo consultado con éxito.", Map.of("status", "ADMIN_OK"));
    }

    @GetMapping("/api/v1/mesa-partes/expedientes")
    @PreAuthorize("hasAuthority('MESAPARTES_VIEW')")
    public ResponseEntity<ApiResponse<Map<String, String>>> getMesaPartesExpedientes() {
        return ApiResponse.ok("Expedientes de Mesa de Partes obtenidos con éxito.", Map.of("status", "EXPEDIENTES_OK"));
    }

    @GetMapping("/api/v1/oficina/{oficinaId}/documentos")
    @PreAuthorize("@officeSecurity.canAccessResourceOfOffice(#oficinaId)")
    public ResponseEntity<ApiResponse<Map<String, String>>> getDocumentosOficina(@PathVariable Long oficinaId) {
        return ApiResponse.ok("Documentos de la oficina autorizada.", Map.of("oficinaId", String.valueOf(oficinaId)));
    }
}
