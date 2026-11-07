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
import pe.gob.municipalidad.sigm.modules.administracion.dto.UsuarioDtos.PasswordResetRequest;
import pe.gob.municipalidad.sigm.modules.administracion.dto.UsuarioDtos.UsuarioCreateRequest;
import pe.gob.municipalidad.sigm.modules.administracion.dto.UsuarioDtos.UsuarioDetailResponse;
import pe.gob.municipalidad.sigm.modules.administracion.dto.UsuarioDtos.UsuarioResponse;
import pe.gob.municipalidad.sigm.modules.administracion.dto.UsuarioDtos.UsuarioUpdateRequest;
import pe.gob.municipalidad.sigm.modules.administracion.service.UsuarioAdminService;

import java.util.Set;

/**
 * Endpoints REST para administración dinámica de usuarios del sistema municipal.
 */
@RestController
@RequestMapping("/api/admin/usuarios")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ADMINISTRACION_MANAGE')")
public class UsuarioAdminRestController {

    private final UsuarioAdminService usuarioAdminService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<UsuarioResponse>>> listarUsuarios(
            @RequestParam(required = false) String term,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(required = false) Boolean locked,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,desc") String sort
    ) {
        String[] sortParts = sort.split(",");
        Sort.Direction dir = sortParts.length > 1 && "asc".equalsIgnoreCase(sortParts[1])
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(dir, sortParts[0]));

        Page<UsuarioResponse> result = usuarioAdminService.buscarUsuarios(term, enabled, locked, pageRequest);
        return ApiResponse.paginated("Usuarios consultados exitosamente.", PagedResponse.from(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UsuarioDetailResponse>> obtenerDetalle(@PathVariable Long id) {
        UsuarioDetailResponse detail = usuarioAdminService.obtenerDetalleUsuario(id);
        return ApiResponse.ok("Detalle de usuario recuperado.", detail);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UsuarioResponse>> registrarUsuario(@Valid @RequestBody UsuarioCreateRequest request) {
        UsuarioResponse creado = usuarioAdminService.registrarUsuario(request);
        return ApiResponse.created("Usuario registrado exitosamente.", creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UsuarioResponse>> actualizarUsuario(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioUpdateRequest request
    ) {
        UsuarioResponse actualizado = usuarioAdminService.actualizarUsuario(id, request);
        return ApiResponse.ok("Usuario actualizado exitosamente.", actualizado);
    }

    @PatchMapping("/{id}/activar")
    public ResponseEntity<ApiResponse<UsuarioResponse>> activarCuenta(@PathVariable Long id) {
        UsuarioResponse resp = usuarioAdminService.activarCuenta(id);
        return ApiResponse.ok("Cuenta de usuario activada correctamente.", resp);
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<ApiResponse<UsuarioResponse>> desactivarCuenta(@PathVariable Long id) {
        UsuarioResponse resp = usuarioAdminService.desactivarCuenta(id);
        return ApiResponse.ok("Cuenta de usuario desactivada correctamente.", resp);
    }

    @PatchMapping("/{id}/bloquear")
    public ResponseEntity<ApiResponse<UsuarioResponse>> bloquearCuenta(@PathVariable Long id) {
        UsuarioResponse resp = usuarioAdminService.bloquearCuenta(id);
        return ApiResponse.ok("Cuenta de usuario bloqueada preventivamente.", resp);
    }

    @PatchMapping("/{id}/desbloquear")
    public ResponseEntity<ApiResponse<UsuarioResponse>> desbloquearCuenta(@PathVariable Long id) {
        UsuarioResponse resp = usuarioAdminService.desbloquearCuenta(id);
        return ApiResponse.ok("Cuenta de usuario desbloqueada correctamente.", resp);
    }

    @PostMapping("/{id}/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetearPassword(
            @PathVariable Long id,
            @Valid @RequestBody PasswordResetRequest request
    ) {
        usuarioAdminService.resetearPassword(id, request);
        return ApiResponse.ok("Contraseña restablecida exitosamente. Sesiones previas invalidadas.");
    }

    @PutMapping("/{id}/roles")
    public ResponseEntity<ApiResponse<UsuarioResponse>> asignarRoles(
            @PathVariable Long id,
            @RequestBody Set<Long> rolIds
    ) {
        UsuarioResponse resp = usuarioAdminService.asignarRoles(id, rolIds);
        return ApiResponse.ok("Roles de usuario actualizados correctamente.", resp);
    }
}
