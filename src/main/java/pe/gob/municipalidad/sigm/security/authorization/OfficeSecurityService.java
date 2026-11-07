package pe.gob.municipalidad.sigm.security.authorization;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.common.model.PersonaOficina;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.PersonaOficinaRepository;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Servicio de control de acceso institucional por oficina municipal y competencia de módulos.
 * Permite evaluar mediante expresiones @PreAuthorize("@officeSecurity.canAccessResourceOfOffice(#oficinaId)")
 * que los servidores públicos solo accedan a información, módulos y expedientes de su respectiva unidad orgánica.
 */
@Slf4j
@Component("officeSecurity")
@org.springframework.transaction.annotation.Transactional(readOnly = true)
@RequiredArgsConstructor
public class OfficeSecurityService {

    private final UsuarioRepository usuarioRepository;
    private final PersonaOficinaRepository personaOficinaRepository;

    /**
     * Comprueba si el usuario autenticado tiene acceso a los recursos de una oficina determinada.
     * Los administradores generales (ROLE_ADMIN / ROLE_SUPERADMIN) tienen acceso transversal.
     */
    public boolean canAccessResourceOfOffice(Long oficinaId) {
        if (oficinaId == null) {
            return false;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        // 1. Acceso transversal para administradores
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority())
                        || "ROLE_SUPERADMIN".equals(a.getAuthority())
                        || "ADMINISTRACION_MANAGE".equals(a.getAuthority()));
        if (isAdmin) {
            return true;
        }

        // 2. Validación de asignación activa en la unidad orgánica
        String username = authentication.getName();
        return usuarioRepository.findByUsername(username)
                .map(Usuario::getPersona)
                .map(persona -> {
                    List<PersonaOficina> asignaciones = personaOficinaRepository.findByPersonaId(persona.getId());
                    return asignaciones.stream()
                            .anyMatch(a -> a.getOficina() != null
                                    && a.getOficina().getId().equals(oficinaId)
                                    && "ACTIVO".equalsIgnoreCase(a.getEstado()));
                })
                .orElse(false);
    }

    /**
     * Comprueba si el usuario autenticado es el titular de un registro o pertenece a la oficina responsable.
     */
    public boolean isOwnerOrOfficeMember(Long resourceOwnerPersonaId, Long oficinaId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String username = authentication.getName();
        return usuarioRepository.findByUsername(username)
                .map(usuario -> {
                    if (usuario.getPersona() != null && usuario.getPersona().getId().equals(resourceOwnerPersonaId)) {
                        return true;
                    }
                    return canAccessResourceOfOffice(oficinaId);
                })
                .orElse(false);
    }

    /**
     * Obtiene la Oficina activa principal del usuario indicado.
     */
    public Oficina obtenerOficinaActiva(String username) {
        if (username == null || username.isBlank()) {
            return null;
        }
        return usuarioRepository.findByUsernameWithRolesAndPersona(username)
                .map(Usuario::getPersona)
                .map(persona -> {
                    List<PersonaOficina> asignaciones = personaOficinaRepository.findByPersonaIdAndEstado(persona.getId(), "ACTIVO");
                    if (asignaciones.isEmpty()) {
                        return null;
                    }
                    return asignaciones.stream()
                            .filter(po -> Boolean.TRUE.equals(po.getPrincipal()))
                            .findFirst()
                            .orElse(asignaciones.get(0))
                            .getOficina();
                })
                .orElse(null);
    }

    /**
     * Obtiene el ID de la Oficina activa principal del usuario indicado.
     */
    public Long obtenerOficinaActivaId(String username) {
        Oficina ofi = obtenerOficinaActiva(username);
        return ofi != null ? ofi.getId() : null;
    }

    /**
     * Determina si el usuario tiene acceso transversal a los expedientes de todas las oficinas
     * (Administradores generales o personal central de Mesa de Partes Virtual - OFI-MPV).
     */
    public boolean tieneAccesoTransversalMesaPartes(Usuario usuario) {
        if (usuario == null) {
            return false;
        }
        if (usuario.getRoles() != null) {
            boolean esAdminOMesaCentral = usuario.getRoles().stream().anyMatch(r -> {
                String cod = r.getCodigo() != null ? r.getCodigo().toUpperCase() : "";
                return "ROLE_ADMIN".equals(cod)
                        || "ROLE_SUPERADMIN".equals(cod)
                        || "ADMIN".equals(cod)
                        || "SUPERADMIN".equals(cod)
                        || "ROLE_MESA_PARTES".equals(cod);
            });
            if (esAdminOMesaCentral) {
                return true;
            }
        }
        Oficina ofi = obtenerOficinaActiva(usuario.getUsername());
        return ofi != null && "OFI-MPV".equalsIgnoreCase(ofi.getCodigo());
    }

    /**
     * Evalúa si el usuario autenticado actual puede acceder a un módulo municipal determinado.
     */
    public boolean canAccessModule(String moduleCode) {
        return canAccessModule(SecurityContextHolder.getContext().getAuthentication(), moduleCode);
    }

    /**
     * Evalúa si la autenticación indicada tiene competencia y permisos efectivos para un módulo municipal.
     */
    public boolean canAccessModule(Authentication authentication, String moduleCode) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return false;
        }

        Set<String> auths = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        if (auths.contains("ROLE_ADMIN") || auths.contains("ROLE_SUPERADMIN") || auths.contains("ADMINISTRACION_MANAGE")) {
            return true;
        }

        boolean sinOficinaAsignada = obtenerOficinaActiva(authentication.getName()) == null;

        if ("MESAPARTES".equalsIgnoreCase(moduleCode)) {
            return auths.contains("MESAPARTES_EXPEDIENTE_VER_OFICINA")
                    || auths.contains("MESAPARTES_EXPEDIENTE_VER_TODOS")
                    || auths.contains("MESAPARTES_BANDEJA_VER")
                    || auths.contains("MESAPARTES_ATENCION_VER")
                    || auths.contains("ROLE_MESA_PARTES")
                    || auths.contains("ROLE_JEFE_OFICINA")
                    || auths.contains("ROLE_OPERADOR");
        }

        if ("MERCADOS".equalsIgnoreCase(moduleCode)) {
            boolean tienePermisoMercados = auths.stream().anyMatch(a -> a.startsWith("MERCADOS_"))
                    || auths.contains("ROLE_MERCADOS");
            if (tienePermisoMercados) {
                return true;
            }
            return sinOficinaAsignada && (auths.contains("ROLE_OPERADOR") || auths.contains("ROLE_JEFE_OFICINA"));
        }

        if ("SERENAZGO".equalsIgnoreCase(moduleCode)) {
            boolean tienePermisoSerenazgo = auths.stream().anyMatch(a -> a.startsWith("SERENAZGO_"))
                    || auths.contains("ROLE_SERENO")
                    || auths.contains("ROLE_SUPERVISOR_SERENAZGO")
                    || auths.contains("ROLE_OPERADOR_SERENAZGO")
                    || auths.contains("ROLE_OPERADOR_CENTRAL")
                    || auths.contains("ROLE_OPERADOR_CC");
            if (tienePermisoSerenazgo) {
                return true;
            }
            return sinOficinaAsignada && (auths.contains("ROLE_OPERADOR") || auths.contains("ROLE_JEFE_OFICINA"));
        }

        if ("TRIBUTARIA".equalsIgnoreCase(moduleCode)) {
            boolean tienePermisoTributaria = auths.stream().anyMatch(a -> a.startsWith("TRIBUTARIA_"))
                    || auths.contains("ROLE_CAJERO")
                    || auths.contains("ROLE_TRIBUTACION")
                    || auths.contains("ROLE_ESPECIALISTA_TRIBUTARIO");
            if (tienePermisoTributaria) {
                return true;
            }
            return sinOficinaAsignada && (auths.contains("ROLE_OPERADOR") || auths.contains("ROLE_JEFE_OFICINA"));
        }

        return false;
    }
}
