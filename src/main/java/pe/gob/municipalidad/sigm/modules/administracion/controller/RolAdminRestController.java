package pe.gob.municipalidad.sigm.modules.administracion.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.gob.municipalidad.sigm.common.pagination.PagedResponse;
import pe.gob.municipalidad.sigm.common.response.ApiResponse;
import pe.gob.municipalidad.sigm.modules.administracion.dto.PermisoDtos.ActualizarPermisosRolRequest;
import pe.gob.municipalidad.sigm.modules.administracion.dto.RolDtos.RolCreateRequest;
import pe.gob.municipalidad.sigm.modules.administracion.dto.RolDtos.RolResponse;
import pe.gob.municipalidad.sigm.modules.administracion.dto.RolDtos.RolUpdateRequest;
import pe.gob.municipalidad.sigm.modules.administracion.service.RolAdminService;

import java.util.List;

/**
 * Endpoints REST para administración dinámica de roles institucionales y sus permisos.
 */
@RestController
@RequestMapping("/api/admin/roles")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ADMINISTRACION_MANAGE')")
public class RolAdminRestController {

    private final RolAdminService rolAdminService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<RolResponse>>> listarRoles(
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) String estado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "nombre"));
        Page<RolResponse> result = rolAdminService.buscarRoles(busqueda, estado, pageRequest);
        return ApiResponse.paginated("Roles consultados exitosamente.", PagedResponse.from(result));
    }

    @GetMapping("/todos")
    public ResponseEntity<ApiResponse<List<RolResponse>>> listarTodosActivos() {
        List<RolResponse> roles = rolAdminService.listarTodosActivos();
        return ApiResponse.ok("Lista de roles activos.", roles);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RolResponse>> obtenerDetalle(@PathVariable Long id) {
        RolResponse rol = rolAdminService.obtenerPorId(id);
        return ApiResponse.ok("Detalle de rol recuperado.", rol);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RolResponse>> crearRol(@Valid @RequestBody RolCreateRequest request) {
        RolResponse creado = rolAdminService.crearRol(request);
        return ApiResponse.created("Rol creado exitosamente.", creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RolResponse>> actualizarRol(
            @PathVariable Long id,
            @Valid @RequestBody RolUpdateRequest request
    ) {
        RolResponse actualizado = rolAdminService.actualizarRol(id, request);
        return ApiResponse.ok("Rol actualizado exitosamente.", actualizado);
    }

    @PatchMapping("/{id}/activar")
    public ResponseEntity<ApiResponse<RolResponse>> activarRol(@PathVariable Long id) {
        RolResponse resp = rolAdminService.cambiarEstadoRol(id, "ACTIVO");
        return ApiResponse.ok("Rol activado correctamente.", resp);
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<ApiResponse<RolResponse>> desactivarRol(@PathVariable Long id) {
        RolResponse resp = rolAdminService.cambiarEstadoRol(id, "INACTIVO");
        return ApiResponse.ok("Rol desactivado correctamente.", resp);
    }

    @PutMapping("/{id}/permisos")
    public ResponseEntity<ApiResponse<RolResponse>> actualizarPermisos(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarPermisosRolRequest request
    ) {
        RolResponse resp = rolAdminService.actualizarPermisosRol(id, request.getPermisoIds());
        return ApiResponse.ok("Permisos asignados al rol exitosamente.", resp);
    }
}
