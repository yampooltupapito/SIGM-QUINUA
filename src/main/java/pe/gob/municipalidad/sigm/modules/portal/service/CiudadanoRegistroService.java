package pe.gob.municipalidad.sigm.modules.portal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.exception.ConflictException;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.common.model.Persona;
import pe.gob.municipalidad.sigm.common.model.Rol;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.PersonaRepository;
import pe.gob.municipalidad.sigm.common.repository.RolRepository;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.portal.dto.CiudadanoRegistroDto;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class CiudadanoRegistroService {

    private final UsuarioRepository usuarioRepository;
    private final PersonaRepository personaRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public CiudadanoRegistroDto.CiudadanoRegistroResponse registrarCiudadano(
            CiudadanoRegistroDto.CiudadanoRegistroRequest request,
            String ipOrigen) {

        // 1. Verificación Honeypot antispam
        if (request.getWebsite() != null && !request.getWebsite().trim().isEmpty()) {
            log.warn("Detección de bot en registro ciudadano desde IP: {}", ipOrigen);
            throw new BusinessException("No se pudo completar el registro debido a controles de seguridad.");
        }

        // 2. Validación de coincidencia de contraseñas
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException("La contraseña y su confirmación no coinciden.");
        }

        // 3. Validación de términos y condiciones
        if (!request.isTerminosAceptados()) {
            throw new BusinessException("Debe aceptar los términos y condiciones del servicio para registrarse.");
        }

        String usernameNormalizado = request.getUsername().trim().toLowerCase();
        if (usuarioRepository.existsByUsername(usernameNormalizado)) {
            throw new ConflictException("El nombre de usuario '" + usernameNormalizado + "' ya se encuentra registrado. Elija otro.");
        }

        String tipoDoc = request.getTipoDocumento().trim().toUpperCase();
        String numDoc = request.getNumeroDocumento().trim();

        // 4. Validar o registrar la Persona
        Persona persona = personaRepository.findByTipoDocumentoAndNumeroDocumento(tipoDoc, numDoc)
                .orElse(null);

        if (persona != null) {
            if (usuarioRepository.existsByPersonaId(persona.getId())) {
                throw new ConflictException("Ya existe una cuenta ciudadana activa para el documento " + tipoDoc + " " + numDoc + ".");
            }
            // Actualizar datos de contacto si ya existía como administrado
            persona.setNombres(request.getNombres().trim());
            persona.setApellidoPaterno(request.getApellidoPaterno().trim());
            if (request.getApellidoMaterno() != null) {
                persona.setApellidoMaterno(request.getApellidoMaterno().trim());
            }
            persona.setCorreo(request.getCorreo().trim().toLowerCase());
            if (request.getTelefono() != null) {
                persona.setTelefono(request.getTelefono().trim());
            }
            if (request.getDireccion() != null) {
                persona.setDireccion(request.getDireccion().trim());
            }
            persona = personaRepository.save(persona);
        } else {
            persona = Persona.builder()
                    .tipoDocumento(tipoDoc)
                    .numeroDocumento(numDoc)
                    .nombres(request.getNombres().trim())
                    .apellidoPaterno(request.getApellidoPaterno().trim())
                    .apellidoMaterno(request.getApellidoMaterno() != null ? request.getApellidoMaterno().trim() : null)
                    .correo(request.getCorreo().trim().toLowerCase())
                    .telefono(request.getTelefono() != null ? request.getTelefono().trim() : null)
                    .direccion(request.getDireccion() != null ? request.getDireccion().trim() : null)
                    .estado("ACTIVO")
                    .build();
            persona = personaRepository.save(persona);
        }

        // 5. Asignación estricta y exclusiva del rol ROLE_CIUDADANO (Prevención de escalamiento de privilegios)
        Rol rolCiudadano = rolRepository.findByCodigo("ROLE_CIUDADANO")
                .orElseThrow(() -> new ResourceNotFoundException("El rol institucional 'ROLE_CIUDADANO' no se encuentra configurado en el sistema."));

        Set<Rol> roles = new HashSet<>();
        roles.add(rolCiudadano);

        // 6. Cifrado seguro de contraseña con BCrypt (costo 12)
        String passwordCifrada = passwordEncoder.encode(request.getPassword());

        Usuario nuevoUsuario = Usuario.builder()
                .persona(persona)
                .username(usernameNormalizado)
                .passwordHash(passwordCifrada)
                .enabled(true)
                .accountLocked(false)
                .failedAttempts(0)
                .roles(roles)
                .build();

        usuarioRepository.save(nuevoUsuario);
        log.info("Registro ciudadano exitoso para username: '{}', DNI: '{}' desde IP: {}", usernameNormalizado, numDoc, ipOrigen);

        return CiudadanoRegistroDto.CiudadanoRegistroResponse.builder()
                .success(true)
                .message("Su cuenta ciudadana ha sido creada exitosamente. Ya puede iniciar sesión en la plataforma municipal.")
                .username(usernameNormalizado)
                .numeroDocumento(numDoc)
                .nombreCompleto(persona.getNombreCompleto())
                .build();
    }
}
