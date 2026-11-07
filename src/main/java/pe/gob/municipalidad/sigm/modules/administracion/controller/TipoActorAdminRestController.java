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
import pe.gob.municipalidad.sigm.modules.administracion.dto.TipoActorDtos.TipoActorCreateRequest;
import pe.gob.municipalidad.sigm.modules.administracion.dto.TipoActorDtos.TipoActorResponse;
import pe.gob.municipalidad.sigm.modules.administracion.dto.TipoActorDtos.TipoActorUpdateRequest;
import pe.gob.municipalidad.sigm.modules.administracion.service.TipoActorAdminService;

import java.util.List;

/**
 * Endpoints REST para administración del catálogo de tipos de actores institucionales.
 */
@RestController
@RequestMapping("/api/admin/actores")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ADMINISTRACION_MANAGE')")
public class TipoActorAdminRestController {

    private final TipoActorAdminService tipoActorAdminService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<TipoActorResponse>>> listarTiposActores(
            @RequestParam(required = false) String term,
            @RequestParam(required = false) String estado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "nombre"));
        Page<TipoActorResponse> result = tipoActorAdminService.buscarTiposActores(term, estado, pageRequest);
        return ApiResponse.paginated("Tipos de actores consultados exitosamente.", PagedResponse.from(result));
    }

    @GetMapping("/todos")
    public ResponseEntity<ApiResponse<List<TipoActorResponse>>> listarTodosActivos() {
        List<TipoActorResponse> actores = tipoActorAdminService.listarTodosActivos();
        return ApiResponse.ok("Lista de tipos de actores activos.", actores);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TipoActorResponse>> obtenerPorId(@PathVariable Long id) {
        TipoActorResponse actor = tipoActorAdminService.obtenerPorId(id);
        return ApiResponse.ok("Detalle de tipo de actor recuperado.", actor);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TipoActorResponse>> crearTipoActor(@Valid @RequestBody TipoActorCreateRequest request) {
        TipoActorResponse creado = tipoActorAdminService.crearTipoActor(request);
        return ApiResponse.created("Tipo de actor creado exitosamente.", creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TipoActorResponse>> actualizarTipoActor(
            @PathVariable Long id,
            @Valid @RequestBody TipoActorUpdateRequest request
    ) {
        TipoActorResponse actualizado = tipoActorAdminService.actualizarTipoActor(id, request);
        return ApiResponse.ok("Tipo de actor actualizado exitosamente.", actualizado);
    }

    @PatchMapping("/{id}/activar")
    public ResponseEntity<ApiResponse<TipoActorResponse>> activarTipoActor(@PathVariable Long id) {
        TipoActorResponse resp = tipoActorAdminService.cambiarEstado(id, "ACTIVO");
        return ApiResponse.ok("Tipo de actor activado correctamente.", resp);
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<ApiResponse<TipoActorResponse>> desactivarTipoActor(@PathVariable Long id) {
        TipoActorResponse resp = tipoActorAdminService.cambiarEstado(id, "INACTIVO");
        return ApiResponse.ok("Tipo de actor desactivado correctamente.", resp);
    }
}
