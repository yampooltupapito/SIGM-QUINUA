package pe.gob.municipalidad.sigm.security.authentication;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.common.model.Permiso;
import pe.gob.municipalidad.sigm.common.model.Persona;
import pe.gob.municipalidad.sigm.common.model.PersonaOficina;
import pe.gob.municipalidad.sigm.common.model.Rol;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.PersonaOficinaRepository;
import pe.gob.municipalidad.sigm.common.repository.RolRepository;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Servicio UserDetailsService de Spring Security integrado con la base de datos PostgreSQL de SIGM-QUINUA.
 * Carga usuarios, personas, roles y permisos dinámicos con mapeo unificado de GrantedAuthorities
 * y filtrado de competencia funcional según la unidad orgánica (Oficina) asignada.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PersonaOficinaRepository personaOficinaRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (username == null || username.trim().isEmpty()) {
            throw new UsernameNotFoundException("Credenciales de acceso incorrectas.");
        }

        Usuario usuario = usuarioRepository.findByUsernameWithRolesAndPersona(username.trim())
                .orElseThrow(() -> new UsernameNotFoundException("Credenciales de acceso incorrectas."));

        Persona persona = usuario.getPersona();
        Set<Rol> roles = usuario.getRoles();

        List<Rol> rolesWithPermisos = (roles != null && !roles.isEmpty())
                ? rolRepository.findRolesWithPermisos(roles)
                : List.of();

        Set<String> roleCodes = new HashSet<>();
        Set<String> permissionCodes = new HashSet<>();
        Set<GrantedAuthority> authorities = new HashSet<>();

        for (Rol rol : rolesWithPermisos) {
            String roleCode = rol.getCodigo().trim();
            roleCodes.add(roleCode);
            String springRole = roleCode.startsWith("ROLE_") ? roleCode : "ROLE_" + roleCode;
            authorities.add(new SimpleGrantedAuthority(springRole));
        }

        boolean isAdmin = roleCodes.contains("ROLE_ADMIN")
                || roleCodes.contains("ROLE_SUPERADMIN")
                || roleCodes.contains("ADMIN")
                || roleCodes.contains("SUPERADMIN");

        List<PersonaOficina> asignacionesActivas = (!isAdmin && persona != null && persona.getId() != null)
                ? personaOficinaRepository.findByPersonaIdAndEstado(persona.getId(), "ACTIVO")
                : List.of();

        boolean filtrarPorCompetenciaOficina = !isAdmin && !asignacionesActivas.isEmpty();
        boolean competenciaMercados = !filtrarPorCompetenciaOficina || tieneCompetenciaMercados(asignacionesActivas, roleCodes);
        boolean competenciaSerenazgo = !filtrarPorCompetenciaOficina || tieneCompetenciaSerenazgo(asignacionesActivas, roleCodes);
        boolean competenciaTributaria = !filtrarPorCompetenciaOficina || tieneCompetenciaTributaria(asignacionesActivas, roleCodes);

        for (Rol rol : rolesWithPermisos) {
            if (rol.getPermisos() != null) {
                for (Permiso permiso : rol.getPermisos()) {
                    String permCode = permiso.getCodigo().trim();
                    if (filtrarPorCompetenciaOficina && !esPermisoCompetenteParaOficina(
                            permCode, competenciaMercados, competenciaSerenazgo, competenciaTributaria)) {
                        continue;
                    }
                    permissionCodes.add(permCode);
                    authorities.add(new SimpleGrantedAuthority(permCode));
                }
            }
        }

        return CustomUserDetails.builder()
                .id(usuario.getId())
                .username(usuario.getUsername())
                .password(usuario.getPasswordHash())
                .enabled(Boolean.TRUE.equals(usuario.getEnabled()))
                .accountNonLocked(!Boolean.TRUE.equals(usuario.getAccountLocked()))
                .personaId(persona != null ? persona.getId() : null)
                .nombreCompleto(persona != null ? persona.getNombreCompleto() : usuario.getUsername())
                .numeroDocumento(persona != null ? persona.getNumeroDocumento() : "")
                .correo(persona != null ? persona.getCorreo() : "")
                .roles(roleCodes)
                .permisos(permissionCodes)
                .authorities(authorities)
                .build();
    }

    private boolean esPermisoCompetenteParaOficina(
            String permCode,
            boolean competenciaMercados,
            boolean competenciaSerenazgo,
            boolean competenciaTributaria
    ) {
        if (permCode.startsWith("MERCADOS_") || permCode.startsWith("PORTAL_COMERCIANTE_")) {
            return competenciaMercados;
        }
        if (permCode.startsWith("SERENAZGO_")) {
            return competenciaSerenazgo;
        }
        if (permCode.startsWith("TRIBUTARIA_") || permCode.startsWith("PORTAL_TRIBUTARIO_")) {
            return competenciaTributaria;
        }
        return true;
    }

    private boolean tieneCompetenciaMercados(List<PersonaOficina> asignaciones, Set<String> roleCodes) {
        if (roleCodes.contains("ROLE_MERCADOS") || roleCodes.contains("ROLE_COMERCIANTE")) {
            return true;
        }
        return asignaciones.stream().anyMatch(po -> {
            Oficina o = po.getOficina();
            if (o == null) return false;
            String texto = ((o.getCodigo() != null ? o.getCodigo() : "") + " " + (o.getNombre() != null ? o.getNombre() : "")).toUpperCase(Locale.ROOT);
            return texto.contains("MKT") || texto.contains("MERCAD") || texto.contains("ABASTO") || texto.contains("COMERCI");
        });
    }

    private boolean tieneCompetenciaSerenazgo(List<PersonaOficina> asignaciones, Set<String> roleCodes) {
        if (roleCodes.contains("ROLE_SERENO") || roleCodes.contains("ROLE_SUPERVISOR_SERENAZGO") || roleCodes.contains("ROLE_OPERADOR_SERENAZGO")) {
            return true;
        }
        return asignaciones.stream().anyMatch(po -> {
            Oficina o = po.getOficina();
            if (o == null) return false;
            String texto = ((o.getCodigo() != null ? o.getCodigo() : "") + " " + (o.getNombre() != null ? o.getNombre() : "")).toUpperCase(Locale.ROOT);
            return texto.contains("SEREN") || texto.contains("SEGURIDAD");
        });
    }

    private boolean tieneCompetenciaTributaria(List<PersonaOficina> asignaciones, Set<String> roleCodes) {
        if (roleCodes.contains("ROLE_CAJERO") || roleCodes.contains("ROLE_TRIBUTACION") || roleCodes.contains("ROLE_ESPECIALISTA_TRIBUTARIO")) {
            return true;
        }
        return asignaciones.stream().anyMatch(po -> {
            Oficina o = po.getOficina();
            if (o == null) return false;
            String texto = ((o.getCodigo() != null ? o.getCodigo() : "") + " " + (o.getNombre() != null ? o.getNombre() : "")).toUpperCase(Locale.ROOT);
            return texto.contains("TRIBUT") || texto.contains("RENTA") || texto.contains("RECAUD") || texto.contains("CAJA") || texto.contains("FISCAL");
        });
    }
}
