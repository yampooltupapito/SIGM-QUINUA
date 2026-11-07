package pe.gob.municipalidad.sigm.modules.administracion.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.exception.ConflictException;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.common.model.Permiso;
import pe.gob.municipalidad.sigm.common.model.Rol;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.PermisoRepository;
import pe.gob.municipalidad.sigm.common.repository.RolRepository;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.administracion.dto.RolDtos.RolCreateRequest;
import pe.gob.municipalidad.sigm.modules.administracion.dto.RolDtos.RolResponse;
import pe.gob.municipalidad.sigm.modules.administracion.dto.RolDtos.RolUpdateRequest;
import pe.gob.municipalidad.sigm.security.service.RefreshTokenService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Servicio de administración de roles dinámicos institucionales y sus permisos asociados.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RolAdminService {

    private static final Set<String> ROLES_CRITICOS = Set.of("ROLE_SUPERADMIN", "ROLE_ADMIN");

    private final RolRepository rolRepository;
    private final PermisoRepository permisoRepository;
    private final UsuarioRepository usuarioRepository;
    private final RefreshTokenService refreshTokenService;
    private final AuditoriaAdministrativaService auditoriaService;

    @Transactional(readOnly = true)
    public Page<RolResponse> buscarRoles(String busqueda, String estado, Pageable pageable) {
        return rolRepository.searchRoles(
                (busqueda != null && !busqueda.isBlank()) ? busqueda : null,
                (estado != null && !estado.isBlank()) ? estado : null,
                pageable
        ).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<RolResponse> listarTodosActivos() {
        return rolRepository.findByEstadoOrderByNombreAsc("ACTIVO").stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RolResponse obtenerPorId(Long id) {
        Rol rol = rolRepository.findWithPermisosById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado con ID: " + id));
        return toResponse(rol);
    }

    @Transactional
    public RolResponse crearRol(RolCreateRequest request) {
        String codigoNormalizado = normalizarCodigoRol(request.getCodigo());

        if (rolRepository.existsByCodigo(codigoNormalizado)) {
            throw new ConflictException("Ya existe un rol con el código: " + codigoNormalizado);
        }

        // Control de escalamiento: solo SUPERADMIN puede crear roles con prefijo especial
        validarAutorizacionCreacion(codigoNormalizado);

        Set<Permiso> permisos = new HashSet<>();
        if (request.getPermisoIds() != null && !request.getPermisoIds().isEmpty()) {
            permisos.addAll(permisoRepository.findAllById(request.getPermisoIds()));
        }

        Rol rol = Rol.builder()
                .codigo(codigoNormalizado)
                .nombre(request.getNombre().trim())
                .descripcion(request.getDescripcion())
                .estado("ACTIVO")
                .permisos(permisos)
                .build();

        Rol guardado = rolRepository.save(rol);

        auditoriaService.registrar("CREAR_ROL", "ROL", guardado.getId(), "EXITO",
                "Rol creado: " + guardado.getCodigo() + " con " + permisos.size() + " permisos.");

        return toResponse(guardado);
    }

    @Transactional
    public RolResponse actualizarRol(Long id, RolUpdateRequest request) {
        Rol rol = rolRepository.findWithPermisosById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado con ID: " + id));

        rol.setNombre(request.getNombre().trim());
        rol.setDescripcion(request.getDescripcion());

        Rol actualizado = rolRepository.save(rol);

        auditoriaService.registrar("ACTUALIZAR_ROL", "ROL", id, "EXITO",
                "Rol actualizado: " + actualizado.getCodigo());

        return toResponse(actualizado);
    }

    @Transactional
    public RolResponse cambiarEstadoRol(Long id, String nuevoEstado) {
        Rol rol = rolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado con ID: " + id));

        if (ROLES_CRITICOS.contains(rol.getCodigo()) && !"ACTIVO".equalsIgnoreCase(nuevoEstado)) {
            throw new BusinessException("ERR_ROL_CRITICO", "No está permitido desactivar los roles críticos del sistema: " + rol.getCodigo());
        }

        rol.setEstado(nuevoEstado.toUpperCase());
        Rol guardado = rolRepository.save(rol);

        // Si se desactiva, invalidar tokens de los usuarios que tengan este rol
        if (!"ACTIVO".equalsIgnoreCase(nuevoEstado)) {
            invalidarSesionesDeUsuariosConRol(rol);
        }

        auditoriaService.registrar("CAMBIO_ESTADO_ROL", "ROL", id, "EXITO",
                "Estado de rol " + rol.getCodigo() + " cambiado a " + nuevoEstado);

        return toResponse(guardado);
    }

    @Transactional
    public RolResponse actualizarPermisosRol(Long rolId, Set<Long> permisoIds) {
        Rol rol = rolRepository.findWithPermisosById(rolId)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado con ID: " + rolId));

        validarModificacionPermisosRolCritico(rol);

        Set<Permiso> nuevosPermisos = new HashSet<>();
        if (permisoIds != null && !permisoIds.isEmpty()) {
            nuevosPermisos.addAll(permisoRepository.findAllById(permisoIds));
        }

        rol.setPermisos(nuevosPermisos);
        Rol guardado = rolRepository.save(rol);

        // Invalidar sesiones activas de los usuarios con este rol para forzar recarga de autorizaciones
        invalidarSesionesDeUsuariosConRol(guardado);

        auditoriaService.registrar("ASIGNAR_PERMISOS_ROL", "ROL", rolId, "EXITO",
                "Permisos actualizados para rol " + rol.getCodigo() + ". Total permisos: " + nuevosPermisos.size());

        return toResponse(guardado);
    }

    private void invalidarSesionesDeUsuariosConRol(Rol rol) {
        List<Usuario> usuarios = usuarioRepository.findAll().stream()
                .filter(u -> u.getRoles().contains(rol))
                .toList();

        for (Usuario u : usuarios) {
            try {
                refreshTokenService.revokeAllUserTokens(u.getId());
            } catch (Exception e) {
                log.warn("No se pudo invalidar token para usuario {}: {}", u.getUsername(), e.getMessage());
            }
        }
    }

    private String normalizarCodigoRol(String codigo) {
        String trimmed = codigo.trim().toUpperCase();
        if (!trimmed.startsWith("ROLE_")) {
            trimmed = "ROLE_" + trimmed;
        }
        return trimmed;
    }

    private void validarAutorizacionCreacion(String codigo) {
        if ("ROLE_SUPERADMIN".equals(codigo)) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isSuperAdmin = auth != null && auth.getAuthorities().stream()
                    .anyMatch(a -> "ROLE_SUPERADMIN".equals(a.getAuthority()));
            if (!isSuperAdmin) {
                throw new BusinessException("ERR_ACCESO_DENEGADO", "Solo un SUPERADMIN puede aprovisionar roles de tipo SUPERADMIN.");
            }
        }
    }

    private void validarModificacionPermisosRolCritico(Rol rol) {
        if ("ROLE_SUPERADMIN".equals(rol.getCodigo())) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isSuperAdmin = auth != null && auth.getAuthorities().stream()
                    .anyMatch(a -> "ROLE_SUPERADMIN".equals(a.getAuthority()));
            if (!isSuperAdmin) {
                throw new BusinessException("ERR_ACCESO_DENEGADO", "No tiene privilegios para alterar los permisos del rol SUPERADMIN.");
            }
        }
    }

    private RolResponse toResponse(Rol r) {
        Set<String> codigosPermisos = r.getPermisos() != null
                ? r.getPermisos().stream().map(Permiso::getCodigo).collect(Collectors.toSet())
                : Set.of();

        long usuariosCount = usuarioRepository.findAll().stream()
                .filter(u -> u.getRoles() != null && u.getRoles().stream().anyMatch(rol -> rol.getId().equals(r.getId())))
                .count();

        return RolResponse.builder()
                .id(r.getId())
                .codigo(r.getCodigo())
                .nombre(r.getNombre())
                .descripcion(r.getDescripcion())
                .estado(r.getEstado())
                .permisosCodigos(codigosPermisos)
                .cantidadUsuarios(usuariosCount)
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }
}
