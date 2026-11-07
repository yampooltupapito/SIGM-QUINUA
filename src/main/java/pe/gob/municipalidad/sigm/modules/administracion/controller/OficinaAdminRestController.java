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
import pe.gob.municipalidad.sigm.modules.administracion.dto.OficinaDtos.AsignacionOficinaRequest;
import pe.gob.municipalidad.sigm.modules.administracion.dto.OficinaDtos.AsignacionOficinaResponse;
import pe.gob.municipalidad.sigm.modules.administracion.dto.OficinaDtos.OficinaCreateRequest;
import pe.gob.municipalidad.sigm.modules.administracion.dto.OficinaDtos.OficinaResponse;
import pe.gob.municipalidad.sigm.modules.administracion.dto.OficinaDtos.OficinaTreeDto;
import pe.gob.municipalidad.sigm.modules.administracion.dto.OficinaDtos.OficinaUpdateRequest;
import pe.gob.municipalidad.sigm.modules.administracion.service.OficinaAdminService;

import java.util.List;

/**
 * Endpoints REST para administración de oficinas y unidades orgánicas municipales.
 */
@RestController
@RequestMapping("/api/admin/oficinas")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ADMINISTRACION_MANAGE')")
public class OficinaAdminRestController {

    private final OficinaAdminService oficinaAdminService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<OficinaResponse>>> listarOficinas(
            @RequestParam(required = false) String term,
            @RequestParam(required = false) String estado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "nombre"));
        Page<OficinaResponse> result = oficinaAdminService.buscarOficinas(term, estado, pageRequest);
        return ApiResponse.paginated("Oficinas consultadas exitosamente.", PagedResponse.from(result));
    }

    @GetMapping("/todas")
    public ResponseEntity<ApiResponse<List<OficinaResponse>>> listarTodasActivas() {
        List<OficinaResponse> oficinas = oficinaAdminService.listarTodasActivas();
        return ApiResponse.ok("Lista de oficinas activas.", oficinas);
    }

    @GetMapping("/arbol")
    public ResponseEntity<ApiResponse<List<OficinaTreeDto>>> obtenerArbolJerarquico() {
        List<OficinaTreeDto> arbol = oficinaAdminService.obtenerArbolJerarquico();
        return ApiResponse.ok("Jerarquía organizacional de oficinas.", arbol);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OficinaResponse>> obtenerPorId(@PathVariable Long id) {
        OficinaResponse oficina = oficinaAdminService.obtenerPorId(id);
        return ApiResponse.ok("Detalle de oficina recuperado.", oficina);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OficinaResponse>> crearOficina(@Valid @RequestBody OficinaCreateRequest request) {
        OficinaResponse creada = oficinaAdminService.crearOficina(request);
        return ApiResponse.created("Oficina creada exitosamente.", creada);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<OficinaResponse>> actualizarOficina(
            @PathVariable Long id,
            @Valid @RequestBody OficinaUpdateRequest request
    ) {
        OficinaResponse actualizada = oficinaAdminService.actualizarOficina(id, request);
        return ApiResponse.ok("Oficina actualizada exitosamente.", actualizada);
    }

    @PatchMapping("/{id}/activar")
    public ResponseEntity<ApiResponse<OficinaResponse>> activarOficina(@PathVariable Long id) {
        OficinaResponse resp = oficinaAdminService.cambiarEstado(id, "ACTIVO");
        return ApiResponse.ok("Oficina activada correctamente.", resp);
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<ApiResponse<OficinaResponse>> desactivarOficina(@PathVariable Long id) {
        OficinaResponse resp = oficinaAdminService.cambiarEstado(id, "INACTIVO");
        return ApiResponse.ok("Oficina desactivada correctamente.", resp);
    }

    @PostMapping("/asignar-trabajador")
    public ResponseEntity<ApiResponse<AsignacionOficinaResponse>> asignarTrabajador(
            @Valid @RequestBody AsignacionOficinaRequest request
    ) {
        AsignacionOficinaResponse resp = oficinaAdminService.asignarTrabajador(request);
        return ApiResponse.created("Trabajador asignado a oficina exitosamente.", resp);
    }
}
