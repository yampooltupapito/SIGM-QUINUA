package pe.gob.municipalidad.sigm.modules.administracion.perfil.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import pe.gob.municipalidad.sigm.common.pagination.PagedResponse;
import pe.gob.municipalidad.sigm.common.response.ApiResponse;
import pe.gob.municipalidad.sigm.modules.administracion.dto.AuditoriaDtos.AuditoriaAdministrativaResponse;
import pe.gob.municipalidad.sigm.modules.administracion.perfil.dto.PerfilDtos;
import pe.gob.municipalidad.sigm.modules.administracion.perfil.service.PerfilService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/perfil")
@RequiredArgsConstructor
public class PerfilRestController {

    private final PerfilService perfilService;

    // 1. Obtener perfil del usuario autenticado
    @GetMapping
    public ResponseEntity<ApiResponse<PerfilDtos.PerfilResponse>> obtenerPerfil(Authentication auth) {
        PerfilDtos.PerfilResponse data = perfilService.obtenerPerfilUsuario(auth.getName());
        return ApiResponse.ok(data);
    }

    // 2. Actualizar datos personales permitidos (teléfono, correo, dirección)
    @PatchMapping
    public ResponseEntity<ApiResponse<PerfilDtos.PerfilResponse>> actualizarDatosPersonales(
            Authentication auth,
            @Valid @RequestBody PerfilDtos.ActualizarDatosPersonalesRequest request) {
        PerfilDtos.PerfilResponse data = perfilService.actualizarDatosPersonales(auth.getName(), request);
        return ApiResponse.ok("Datos personales actualizados correctamente.", data);
    }

    // 3. Cambiar contraseña de forma segura con verificación previa
    @PostMapping("/cambiar-password")
    public ResponseEntity<ApiResponse<Void>> cambiarPassword(
            Authentication auth,
            @Valid @RequestBody PerfilDtos.CambiarPasswordRequest request) {
        perfilService.cambiarPassword(auth.getName(), request);
        return ApiResponse.ok("Contraseña actualizada exitosamente. Las demás sesiones activas han sido cerradas.");
    }

    // 4. Consultar roles y permisos efectivos
    @GetMapping("/permisos")
    public ResponseEntity<ApiResponse<PerfilDtos.PermisosConsolidadosDto>> obtenerPermisos(Authentication auth) {
        PerfilDtos.PermisosConsolidadosDto data = perfilService.obtenerPermisosUsuario(auth.getName());
        return ApiResponse.ok(data);
    }

    // 5. Consultar asignaciones institucionales (oficinas y cargos)
    @GetMapping("/asignaciones")
    public ResponseEntity<ApiResponse<List<PerfilDtos.AsignacionOficinaDto>>> obtenerAsignaciones(Authentication auth) {
        List<PerfilDtos.AsignacionOficinaDto> data = perfilService.obtenerAsignacionesUsuario(auth.getName());
        return ApiResponse.ok(data);
    }

    // 6. Consultar preferencias de usuario
    @GetMapping("/preferencias")
    public ResponseEntity<ApiResponse<PerfilDtos.UsuarioPreferenciaDto>> obtenerPreferencias(Authentication auth) {
        PerfilDtos.UsuarioPreferenciaDto data = perfilService.obtenerPreferencias(auth.getName());
        return ApiResponse.ok(data);
    }

    // 7. Guardar preferencias de usuario
    @PatchMapping("/preferencias")
    public ResponseEntity<ApiResponse<PerfilDtos.UsuarioPreferenciaDto>> guardarPreferencias(
            Authentication auth,
            @Valid @RequestBody PerfilDtos.UsuarioPreferenciaDto request) {
        PerfilDtos.UsuarioPreferenciaDto data = perfilService.guardarPreferencias(auth.getName(), request);
        return ApiResponse.ok("Preferencias guardadas exitosamente.", data);
    }

    // 8. Subir fotografía de perfil
    @PostMapping("/fotografia")
    public ResponseEntity<ApiResponse<Map<String, String>>> subirFotografia(
            Authentication auth,
            @RequestParam("archivo") MultipartFile archivo) {
        String fotoUrl = perfilService.guardarFotografia(auth.getName(), archivo);
        return ApiResponse.ok("Fotografía de perfil actualizada con éxito.", Map.of("fotoUrl", fotoUrl));
    }

    // 9. Eliminar fotografía de perfil
    @DeleteMapping("/fotografia")
    public ResponseEntity<ApiResponse<Void>> eliminarFotografia(Authentication auth) {
        perfilService.eliminarFotografia(auth.getName());
        return ApiResponse.ok("Fotografía de perfil eliminada correctamente.");
    }

    // 10. Servir fotografía de perfil
    @GetMapping("/fotografia/{filename}")
    public ResponseEntity<Resource> servirFotografia(@PathVariable String filename) {
        Resource recurso = perfilService.obtenerRecursoFotografia(filename);
        String contentType = MediaType.IMAGE_JPEG_VALUE;
        if (filename.toLowerCase().endsWith(".png")) {
            contentType = MediaType.IMAGE_PNG_VALUE;
        } else if (filename.toLowerCase().endsWith(".webp")) {
            contentType = "image/webp";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400")
                .body(recurso);
    }

    // 11. Consultar sesiones activas
    @GetMapping("/sesiones")
    public ResponseEntity<ApiResponse<List<PerfilDtos.SesionActivaDto>>> obtenerSesiones(Authentication auth) {
        List<PerfilDtos.SesionActivaDto> data = perfilService.obtenerSesionesActivas(auth.getName());
        return ApiResponse.ok(data);
    }

    // 12. Revocar otras sesiones
    @PostMapping("/sesiones/revocar-otras")
    public ResponseEntity<ApiResponse<Void>> revocarOtrasSesiones(
            Authentication auth,
            @RequestParam(value = "sesionActualId", required = false) Long sesionActualId) {
        perfilService.revocarOtrasSesiones(auth.getName(), sesionActualId);
        return ApiResponse.ok("Las demás sesiones han sido revocadas exitosamente.");
    }

    // 13. Revocar sesión específica
    @DeleteMapping("/sesiones/{id}")
    public ResponseEntity<ApiResponse<Void>> revocarSesion(Authentication auth, @PathVariable Long id) {
        perfilService.revocarSesion(auth.getName(), id);
        return ApiResponse.ok("Sesión revocada correctamente.");
    }

    // 14. Consultar actividad de seguridad propia
    @GetMapping("/actividad")
    public ResponseEntity<ApiResponse<PagedResponse<AuditoriaAdministrativaResponse>>> obtenerActividad(
            Authentication auth,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, Math.min(50, size)));
        Page<AuditoriaAdministrativaResponse> result = perfilService.obtenerActividadSeguridad(auth.getName(), pageable);
        return ApiResponse.paginated(PagedResponse.from(result));
    }
}
