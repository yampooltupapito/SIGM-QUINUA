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
import pe.gob.municipalidad.sigm.modules.administracion.dto.CargoDtos.CargoCreateRequest;
import pe.gob.municipalidad.sigm.modules.administracion.dto.CargoDtos.CargoResponse;
import pe.gob.municipalidad.sigm.modules.administracion.dto.CargoDtos.CargoUpdateRequest;
import pe.gob.municipalidad.sigm.modules.administracion.service.CargoAdminService;

import java.util.List;

/**
 * Endpoints REST para administración del catálogo de cargos municipales.
 */
@RestController
@RequestMapping("/api/admin/cargos")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ADMINISTRACION_MANAGE')")
public class CargoAdminRestController {

    private final CargoAdminService cargoAdminService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<CargoResponse>>> listarCargos(
            @RequestParam(required = false) String term,
            @RequestParam(required = false) String estado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "nombre"));
        Page<CargoResponse> result = cargoAdminService.buscarCargos(term, estado, pageRequest);
        return ApiResponse.paginated("Cargos consultados exitosamente.", PagedResponse.from(result));
    }

    @GetMapping("/todos")
    public ResponseEntity<ApiResponse<List<CargoResponse>>> listarTodosActivos() {
        List<CargoResponse> cargos = cargoAdminService.listarTodosActivos();
        return ApiResponse.ok("Lista de cargos activos.", cargos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CargoResponse>> obtenerPorId(@PathVariable Long id) {
        CargoResponse cargo = cargoAdminService.obtenerPorId(id);
        return ApiResponse.ok("Detalle de cargo recuperado.", cargo);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CargoResponse>> crearCargo(@Valid @RequestBody CargoCreateRequest request) {
        CargoResponse creado = cargoAdminService.crearCargo(request);
        return ApiResponse.created("Cargo creado exitosamente.", creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CargoResponse>> actualizarCargo(
            @PathVariable Long id,
            @Valid @RequestBody CargoUpdateRequest request
    ) {
        CargoResponse actualizado = cargoAdminService.actualizarCargo(id, request);
        return ApiResponse.ok("Cargo actualizado exitosamente.", actualizado);
    }

    @PatchMapping("/{id}/activar")
    public ResponseEntity<ApiResponse<CargoResponse>> activarCargo(@PathVariable Long id) {
        CargoResponse resp = cargoAdminService.cambiarEstado(id, "ACTIVO");
        return ApiResponse.ok("Cargo activado correctamente.", resp);
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<ApiResponse<CargoResponse>> desactivarCargo(@PathVariable Long id) {
        CargoResponse resp = cargoAdminService.cambiarEstado(id, "INACTIVO");
        return ApiResponse.ok("Cargo desactivado correctamente.", resp);
    }
}
