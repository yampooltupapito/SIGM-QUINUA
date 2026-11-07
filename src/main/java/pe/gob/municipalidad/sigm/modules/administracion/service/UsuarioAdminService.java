package pe.gob.municipalidad.sigm.modules.administracion.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.exception.ConflictException;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.common.model.Persona;
import pe.gob.municipalidad.sigm.common.model.PersonaOficina;
import pe.gob.municipalidad.sigm.common.model.Rol;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.PersonaOficinaRepository;
import pe.gob.municipalidad.sigm.common.repository.PersonaRepository;
import pe.gob.municipalidad.sigm.common.repository.RolRepository;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.administracion.dto.OficinaDtos.AsignacionOficinaResponse;
import pe.gob.municipalidad.sigm.modules.administracion.dto.UsuarioDtos.PasswordResetRequest;
import pe.gob.municipalidad.sigm.modules.administracion.dto.UsuarioDtos.UsuarioCreateRequest;
import pe.gob.municipalidad.sigm.modules.administracion.dto.UsuarioDtos.UsuarioDetailResponse;
import pe.gob.municipalidad.sigm.modules.administracion.dto.UsuarioDtos.UsuarioResponse;
import pe.gob.municipalidad.sigm.modules.administracion.dto.UsuarioDtos.UsuarioUpdateRequest;
import pe.gob.municipalidad.sigm.security.service.RefreshTokenService;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Servicio de administración de cuentas de usuario, asignación de roles y control de acceso.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UsuarioAdminService {

    private final UsuarioRepository usuarioRepository;
    private final PersonaRepository personaRepository;
    private final RolRepository rolRepository;
    private final PersonaOficinaRepository personaOficinaRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final AuditoriaAdministrativaService auditoriaService;

    @Transactional(readOnly = true)
    public Page<UsuarioResponse> buscarUsuarios(String term, Boolean enabled, Boolean locked, Pageable pageable) {
        return usuarioRepository.searchUsuarios(
                (term != null && !term.isBlank()) ? term : null,
                enabled,
                locked,
                pageable
        ).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public UsuarioDetailResponse obtenerDetalleUsuario(Long id) {
        Usuario usuario = usuarioRepository.findWithRolesAndPersonaById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));

        List<PersonaOficina> asignaciones = personaOficinaRepository.findByPersonaId(usuario.getPersona().getId());
        List<AsignacionOficinaResponse> asignacionesResp = asignaciones.stream()
                .map(a -> AsignacionOficinaResponse.builder()
                        .id(a.getId())
                        .personaId(a.getPersona().getId())
                        .personaNombre(a.getPersona().getNombreCompleto())
                        .oficinaId(a.getOficina().getId())
                        .oficinaNombre(a.getOficina().getNombre())
                        .cargoId(a.getCargo() != null ? a.getCargo().getId() : null)
                        .cargoNombre(a.getCargo() != null ? a.getCargo().getNombre() : null)
                        .tipoActorId(a.getTipoActor().getId())
                        .tipoActorNombre(a.getTipoActor().getNombre())
                        .fechaInicio(a.getFechaInicio())
                        .fechaFin(a.getFechaFin())
                        .principal(a.getPrincipal())
                        .estado(a.getEstado())
                        .build())
                .collect(Collectors.toList());

        return toDetailResponse(usuario, asignacionesResp);
    }

    @Transactional
    public UsuarioResponse registrarUsuario(UsuarioCreateRequest request) {
        String usernameNormalizado = request.getUsername().trim().toLowerCase();
        if (usuarioRepository.existsByUsername(usernameNormalizado)) {
            throw new ConflictException("Ya existe un usuario registrado con el username: " + usernameNormalizado);
        }

        // Buscar o crear la Persona asociada
        Persona persona = obtenerOCrearPersona(request);

        if (usuarioRepository.existsByPersonaId(persona.getId())) {
            throw new ConflictException("La persona identificada con DNI/Documento " +
                    persona.getNumeroDocumento() + " ya tiene una cuenta de usuario asignada.");
        }

        // Validar y resolver roles
        Set<Rol> roles = resolverRoles(request.getRolIds());

        // Cifrar contraseña inicial con BCrypt
        String passwordCifrada = passwordEncoder.encode(request.getPassword());

        Usuario usuario = Usuario.builder()
                .username(usernameNormalizado)
                .passwordHash(passwordCifrada)
                .persona(persona)
                .enabled(true)
                .accountLocked(false)
                .failedAttempts(0)
                .roles(roles)
                .build();

        Usuario guardado = usuarioRepository.save(usuario);

        auditoriaService.registrar("REGISTRAR_USUARIO", "USUARIO", guardado.getId(), "EXITO",
                "Usuario registrado: " + guardado.getUsername() + " para persona ID: " + persona.getId());

        return toResponse(guardado);
    }

    @Transactional
    public UsuarioResponse actualizarUsuario(Long id, UsuarioUpdateRequest request) {
        Usuario usuario = usuarioRepository.findWithRolesAndPersonaById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));

        Persona persona = usuario.getPersona();
        persona.setNombres(request.getNombres().trim());
        persona.setApellidoPaterno(request.getApellidoPaterno().trim());
        persona.setApellidoMaterno(request.getApellidoMaterno() != null ? request.getApellidoMaterno().trim() : null);
        persona.setCorreo(request.getCorreo().trim());
        persona.setTelefono(request.getTelefono());
        persona.setDireccion(request.getDireccion());
        personaRepository.save(persona);

        // Actualizar roles con validación de auto-revocación
        if (request.getRolIds() != null && !request.getRolIds().isEmpty()) {
            validarNoAutoRevocacionRolesCriticos(usuario, request.getRolIds());
            Set<Rol> nuevosRoles = resolverRoles(request.getRolIds());
            usuario.setRoles(nuevosRoles);
            refreshTokenService.revokeAllUserTokens(usuario.getId());
        }

        Usuario guardado = usuarioRepository.save(usuario);

        auditoriaService.registrar("ACTUALIZAR_USUARIO", "USUARIO", id, "EXITO",
                "Datos de usuario actualizados: " + usuario.getUsername());

        return toResponse(guardado);
    }

    @Transactional
    public UsuarioResponse activarCuenta(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));

        usuario.setEnabled(true);
        Usuario guardado = usuarioRepository.save(usuario);

        auditoriaService.registrar("ACTIVAR_USUARIO", "USUARIO", id, "EXITO",
                "Cuenta de usuario activada: " + usuario.getUsername());

        return toResponse(guardado);
    }

    @Transactional
    public UsuarioResponse desactivarCuenta(Long id) {
        Usuario usuario = usuarioRepository.findWithRolesAndPersonaById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));

        validarNoAutoAccion(usuario.getUsername(), "desactivar");
        validarNoUltimoSuperAdmin(usuario, "desactivar");

        usuario.setEnabled(false);
        Usuario guardado = usuarioRepository.save(usuario);

        // Invalidar tokens activos de inmediato
        refreshTokenService.revokeAllUserTokens(id);

        auditoriaService.registrar("DESACTIVAR_USUARIO", "USUARIO", id, "EXITO",
                "Cuenta de usuario desactivada: " + usuario.getUsername());

        return toResponse(guardado);
    }

    @Transactional
    public UsuarioResponse bloquearCuenta(Long id) {
        Usuario usuario = usuarioRepository.findWithRolesAndPersonaById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));

        validarNoAutoAccion(usuario.getUsername(), "bloquear");
        validarNoUltimoSuperAdmin(usuario, "bloquear");

        usuario.setAccountLocked(true);
        Usuario guardado = usuarioRepository.save(usuario);

        refreshTokenService.revokeAllUserTokens(id);

        auditoriaService.registrar("BLOQUEAR_USUARIO", "USUARIO", id, "EXITO",
                "Cuenta de usuario bloqueada administrativamente: " + usuario.getUsername());

        return toResponse(guardado);
    }

    @Transactional
    public UsuarioResponse desbloquearCuenta(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));

        usuario.setAccountLocked(false);
        usuario.setFailedAttempts(0);
        Usuario guardado = usuarioRepository.save(usuario);

        auditoriaService.registrar("DESBLOQUEAR_USUARIO", "USUARIO", id, "EXITO",
                "Cuenta de usuario desbloqueada: " + usuario.getUsername());

        return toResponse(guardado);
    }

    @Transactional
    public void resetearPassword(Long id, PasswordResetRequest request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));

        String nuevaPasswordHash = passwordEncoder.encode(request.getNuevaPassword());
        usuario.setPasswordHash(nuevaPasswordHash);
        usuario.setFailedAttempts(0);
        usuario.setAccountLocked(false);
        usuarioRepository.save(usuario);

        // Revocar todas las sesiones previas
        refreshTokenService.revokeAllUserTokens(id);

        auditoriaService.registrar("RESET_PASSWORD", "USUARIO", id, "EXITO",
                "Restablecimiento administrativo de credenciales para el usuario: " + usuario.getUsername());
    }

    @Transactional
    public UsuarioResponse asignarRoles(Long id, Set<Long> rolIds) {
        Usuario usuario = usuarioRepository.findWithRolesAndPersonaById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));

        validarNoAutoRevocacionRolesCriticos(usuario, rolIds);
        Set<Rol> nuevosRoles = resolverRoles(rolIds);
        validarNoDespojoUltimoSuperAdmin(usuario, nuevosRoles);
        usuario.setRoles(nuevosRoles);

        Usuario guardado = usuarioRepository.save(usuario);
        refreshTokenService.revokeAllUserTokens(id);

        auditoriaService.registrar("ASIGNAR_ROLES_USUARIO", "USUARIO", id, "EXITO",
                "Roles reasignados al usuario " + usuario.getUsername() + ": " +
                        nuevosRoles.stream().map(Rol::getCodigo).collect(Collectors.joining(", ")));

        return toResponse(guardado);
    }

    private Persona obtenerOCrearPersona(UsuarioCreateRequest req) {
        Optional<Persona> opt = personaRepository.findByTipoDocumentoAndNumeroDocumento(
                req.getTipoDocumento(), req.getNumeroDocumento());

        if (opt.isPresent()) {
            return opt.get();
        }

        Persona nueva = Persona.builder()
                .tipoDocumento(req.getTipoDocumento().toUpperCase())
                .numeroDocumento(req.getNumeroDocumento().trim())
                .nombres(req.getNombres().trim())
                .apellidoPaterno(req.getApellidoPaterno().trim())
                .apellidoMaterno(req.getApellidoMaterno() != null ? req.getApellidoMaterno().trim() : null)
                .correo(req.getCorreo().trim())
                .telefono(req.getTelefono())
                .direccion(req.getDireccion())
                .estado("ACTIVO")
                .build();

        return personaRepository.save(nueva);
    }

    private Set<Rol> resolverRoles(Set<Long> rolIds) {
        if (rolIds == null || rolIds.isEmpty()) {
            throw new BusinessException("ERR_ROLES_VACIOS", "Debe asignarse al menos un rol al usuario.");
        }

        List<Rol> roles = rolRepository.findAllById(rolIds);
        if (roles.isEmpty()) {
            throw new BusinessException("ERR_ROLES_INVALIDOS", "Ninguno de los roles especificados existe en el sistema.");
        }

        // Control de escalamiento de privilegios
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isSuperAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_SUPERADMIN".equals(a.getAuthority()));

        for (Rol r : roles) {
            if ("ROLE_SUPERADMIN".equals(r.getCodigo()) && !isSuperAdmin) {
                throw new BusinessException("ERR_ESCALAMIENTO_PROHIBIDO",
                        "No tiene privilegios para asignar el rol ROLE_SUPERADMIN a otros usuarios.");
            }
        }

        return new HashSet<>(roles);
    }

    private void validarNoAutoAccion(String targetUsername, String accion) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && targetUsername.equalsIgnoreCase(auth.getName())) {
            throw new BusinessException("ERR_AUTO_ACCION",
                    "Un administrador no puede " + accion + " su propia cuenta de acceso.");
        }
    }

    private void validarNoAutoRevocacionRolesCriticos(Usuario usuario, Set<Long> nuevosRolIds) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && usuario.getUsername().equalsIgnoreCase(auth.getName())) {
            // Verificar si el usuario actualmente tiene ROLE_ADMIN o ROLE_SUPERADMIN
            boolean tieneCritico = usuario.getRoles().stream()
                    .anyMatch(r -> "ROLE_SUPERADMIN".equals(r.getCodigo()) || "ROLE_ADMIN".equals(r.getCodigo()));

            if (tieneCritico) {
                List<Rol> nuevosRoles = rolRepository.findAllById(nuevosRolIds);
                boolean conservaCritico = nuevosRoles.stream()
                        .anyMatch(r -> "ROLE_SUPERADMIN".equals(r.getCodigo()) || "ROLE_ADMIN".equals(r.getCodigo()));

                if (!conservaCritico) {
                    throw new BusinessException("ERR_AUTO_REVOCACION",
                            "Por seguridad, no puede revocar sus propios privilegios de administración.");
                }
            }
        }
    }

    private void validarNoUltimoSuperAdmin(Usuario usuario, String accion) {
        boolean esSuperAdmin = usuario.getRoles() != null && usuario.getRoles().stream()
                .anyMatch(r -> "ROLE_SUPERADMIN".equals(r.getCodigo()));
        if (esSuperAdmin && Boolean.TRUE.equals(usuario.getEnabled()) && !Boolean.TRUE.equals(usuario.getAccountLocked())) {
            long totalActivos = usuarioRepository.countActiveUsersWithRole("ROLE_SUPERADMIN");
            if (totalActivos <= 1) {
                throw new BusinessException("ERR_ULTIMO_SUPERADMIN",
                        "Operación rechazada: No es posible " + accion + " al único Superadministrador activo del sistema.");
            }
        }
    }

    private void validarNoDespojoUltimoSuperAdmin(Usuario usuario, Set<Rol> nuevosRoles) {
        boolean eraSuperAdmin = usuario.getRoles() != null && usuario.getRoles().stream()
                .anyMatch(r -> "ROLE_SUPERADMIN".equals(r.getCodigo()));
        boolean conservaSuperAdmin = nuevosRoles.stream()
                .anyMatch(r -> "ROLE_SUPERADMIN".equals(r.getCodigo()));

        if (eraSuperAdmin && !conservaSuperAdmin) {
            long totalActivos = usuarioRepository.countActiveUsersWithRole("ROLE_SUPERADMIN");
            if (totalActivos <= 1) {
                throw new BusinessException("ERR_ULTIMO_SUPERADMIN",
                        "Operación rechazada: No es posible revocar el rol ROLE_SUPERADMIN al único Superadministrador activo del sistema.");
            }
        }
    }

    private UsuarioResponse toResponse(Usuario u) {
        Set<String> roles = u.getRoles() != null
                ? u.getRoles().stream().map(Rol::getCodigo).collect(Collectors.toSet())
                : Set.of();

        return UsuarioResponse.builder()
                .id(u.getId())
                .username(u.getUsername())
                .enabled(u.getEnabled())
                .accountLocked(u.getAccountLocked())
                .failedAttempts(u.getFailedAttempts())
                .personaId(u.getPersona().getId())
                .personaNombre(u.getPersona().getNombreCompleto())
                .tipoDocumento(u.getPersona().getTipoDocumento())
                .numeroDocumento(u.getPersona().getNumeroDocumento())
                .correo(u.getPersona().getCorreo())
                .roles(roles)
                .createdAt(u.getCreatedAt())
                .updatedAt(u.getUpdatedAt())
                .build();
    }

    private UsuarioDetailResponse toDetailResponse(Usuario u, List<AsignacionOficinaResponse> asignaciones) {
        UsuarioResponse base = toResponse(u);
        return UsuarioDetailResponse.builder()
                .id(base.getId())
                .username(base.getUsername())
                .enabled(base.getEnabled())
                .accountLocked(base.getAccountLocked())
                .failedAttempts(base.getFailedAttempts())
                .personaId(base.getPersonaId())
                .personaNombre(base.getPersonaNombre())
                .tipoDocumento(base.getTipoDocumento())
                .numeroDocumento(base.getNumeroDocumento())
                .correo(base.getCorreo())
                .telefono(u.getPersona().getTelefono())
                .direccion(u.getPersona().getDireccion())
                .roles(base.getRoles())
                .asignacionesOficina(asignaciones)
                .createdAt(base.getCreatedAt())
                .updatedAt(base.getUpdatedAt())
                .build();
    }
}
