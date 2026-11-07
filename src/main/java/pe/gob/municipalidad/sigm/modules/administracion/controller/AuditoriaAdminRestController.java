package pe.gob.municipalidad.sigm.modules.administracion.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.gob.municipalidad.sigm.common.pagination.PagedResponse;
import pe.gob.municipalidad.sigm.common.response.ApiResponse;
import pe.gob.municipalidad.sigm.modules.administracion.dto.AuditoriaDtos.AuditoriaAdministrativaResponse;
import pe.gob.municipalidad.sigm.modules.administracion.service.AuditoriaAdministrativaService;

/**
 * Endpoints REST para consulta y visualización de la pista de auditoría administrativa.
 */
@RestController
@RequestMapping("/api/admin/auditoria")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ADMINISTRACION_MANAGE')")
public class AuditoriaAdminRestController {

    private final AuditoriaAdministrativaService auditoriaService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<AuditoriaAdministrativaResponse>>> buscarAuditoria(
            @RequestParam(required = false) String usuario,
            @RequestParam(required = false) String entidad,
            @RequestParam(required = false) String accion,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size
    ) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<AuditoriaAdministrativaResponse> result = auditoriaService.buscarAuditoria(usuario, entidad, accion, pageRequest);
        return ApiResponse.paginated("Registros de auditoría administrativa consultados.", PagedResponse.from(result));
    }
}
