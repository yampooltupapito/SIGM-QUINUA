package pe.gob.municipalidad.sigm.modules.administracion.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.gob.municipalidad.sigm.common.response.ApiResponse;
import pe.gob.municipalidad.sigm.modules.administracion.dto.PermisoDtos.MatrizPermisosResponse;
import pe.gob.municipalidad.sigm.modules.administracion.dto.PermisoDtos.PermisoResponse;
import pe.gob.municipalidad.sigm.modules.administracion.service.PermisoAdminService;

import java.util.List;

/**
 * Endpoints REST para catálogo y matriz visual de permisos granulares por módulo y acción.
 */
@RestController
@RequestMapping("/api/admin/permisos")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ADMINISTRACION_MANAGE')")
public class PermisoAdminRestController {

    private final PermisoAdminService permisoAdminService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PermisoResponse>>> listarTodos() {
        List<PermisoResponse> permisos = permisoAdminService.listarTodos();
        return ApiResponse.ok("Catálogo de permisos del sistema.", permisos);
    }

    @GetMapping("/matriz")
    public ResponseEntity<ApiResponse<MatrizPermisosResponse>> obtenerMatrizPermisos(
            @RequestParam(required = false) Long rolId
    ) {
        MatrizPermisosResponse matriz = permisoAdminService.obtenerMatrizPermisos(rolId);
        return ApiResponse.ok("Matriz de permisos por módulo y acción.", matriz);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PermisoResponse>> crearPermiso(
            @RequestParam Long moduloId,
            @RequestParam String codigo,
            @RequestParam String nombre,
            @RequestParam String accion,
            @RequestParam(required = false) String descripcion
    ) {
        PermisoResponse creado = permisoAdminService.crearPermiso(moduloId, codigo, nombre, accion, descripcion);
        return ApiResponse.created("Permiso creado exitosamente.", creado);
    }
}
