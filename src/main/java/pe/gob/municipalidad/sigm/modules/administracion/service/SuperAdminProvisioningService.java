package pe.gob.municipalidad.sigm.modules.administracion.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.exception.ConflictException;
import pe.gob.municipalidad.sigm.common.model.Persona;
import pe.gob.municipalidad.sigm.common.model.Rol;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.PersonaRepository;
import pe.gob.municipalidad.sigm.common.repository.RolRepository;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.administracion.dto.UsuarioDtos.UsuarioResponse;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Servicio seguro para el aprovisionamiento inicial y controlado del primer Superadministrador
 * institucional de SIGM-QUINUA.
 * Ejecutable fuera de endpoints públicos, con verificación de clave de inicialización y sin
 * contraseñas fijas en código fuente.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SuperAdminProvisioningService {

    private final UsuarioRepository usuarioRepository;
    private final PersonaRepository personaRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditoriaAdministrativaService auditoriaService;

    @Value("${sigm.security.provisioning-secret:sigm-quinua-init-sec-2026-ayacucho}")
    private String provisioningSecret;

    @Transactional
    public UsuarioResponse provisionarPrimerSuperAdmin(
            String secretToken,
            String username,
            String password,
            String tipoDocumento,
            String numeroDocumento,
            String nombres,
            String apellidoPaterno,
            String apellidoMaterno,
            String correo
    ) {
        if (secretToken == null || !provisioningSecret.equals(secretToken.trim())) {
            throw new BusinessException("ERR_PROVISIONING_TOKEN", "Clave secreta de aprovisionamiento inválida.");
        }

        // Verificar si ya existe algún usuario con ROLE_SUPERADMIN
        boolean yaExisteSuperAdmin = usuarioRepository.findAll().stream()
                .anyMatch(u -> u.getRoles() != null && u.getRoles().stream()
                        .anyMatch(r -> "ROLE_SUPERADMIN".equals(r.getCodigo())));

        if (yaExisteSuperAdmin) {
            throw new BusinessException("ERR_SUPERADMIN_EXISTS",
                    "El sistema ya cuenta con un Superadministrador aprovisionado. No está permitida la auto-creación adicional.");
        }

        String usernameNorm = username.trim().toLowerCase();
        if (usuarioRepository.existsByUsername(usernameNorm)) {
            throw new ConflictException("Ya existe un usuario registrado con el username: " + usernameNorm);
        }

        // Obtener o crear persona
        Optional<Persona> optPersona = personaRepository.findByTipoDocumentoAndNumeroDocumento(
                tipoDocumento.toUpperCase(), numeroDocumento.trim());

        Persona persona = optPersona.orElseGet(() -> {
            Persona p = Persona.builder()
                    .tipoDocumento(tipoDocumento.toUpperCase())
                    .numeroDocumento(numeroDocumento.trim())
                    .nombres(nombres.trim())
                    .apellidoPaterno(apellidoPaterno.trim())
                    .apellidoMaterno(apellidoMaterno != null ? apellidoMaterno.trim() : null)
                    .correo(correo.trim())
                    .estado("ACTIVO")
                    .build();
            return personaRepository.save(p);
        });

        // Buscar rol ROLE_SUPERADMIN
        Rol rolSuperAdmin = rolRepository.findByCodigo("ROLE_SUPERADMIN")
                .orElseThrow(() -> new BusinessException("ERR_ROL_NOT_FOUND", "El rol ROLE_SUPERADMIN no existe en la base de datos."));

        Usuario superAdmin = Usuario.builder()
                .username(usernameNorm)
                .passwordHash(passwordEncoder.encode(password))
                .persona(persona)
                .enabled(true)
                .accountLocked(false)
                .failedAttempts(0)
                .roles(new HashSet<>(Set.of(rolSuperAdmin)))
                .build();

        Usuario guardado = usuarioRepository.save(superAdmin);

        auditoriaService.registrar("PROVISION_SUPERADMIN", "USUARIO", guardado.getId(), "EXITO",
                "Aprovisionamiento inicial de Superadministrador exitoso para el usuario: " + guardado.getUsername());

        log.info("Superadministrador '{}' aprovisionado exitosamente en SIGM-QUINUA.", guardado.getUsername());

        return UsuarioResponse.builder()
                .id(guardado.getId())
                .username(guardado.getUsername())
                .enabled(guardado.getEnabled())
                .accountLocked(guardado.getAccountLocked())
                .failedAttempts(guardado.getFailedAttempts())
                .personaId(persona.getId())
                .personaNombre(persona.getNombreCompleto())
                .tipoDocumento(persona.getTipoDocumento())
                .numeroDocumento(persona.getNumeroDocumento())
                .correo(persona.getCorreo())
                .roles(guardado.getRoles().stream().map(Rol::getCodigo).collect(Collectors.toSet()))
                .createdAt(guardado.getCreatedAt())
                .updatedAt(guardado.getUpdatedAt())
                .build();
    }
}
