package pe.gob.municipalidad.sigm.modules.administracion.perfil.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pe.gob.municipalidad.sigm.common.exception.DuplicateResourceException;
import pe.gob.municipalidad.sigm.common.exception.InvalidRequestException;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.common.model.Persona;
import pe.gob.municipalidad.sigm.common.model.PersonaOficina;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.PersonaOficinaRepository;
import pe.gob.municipalidad.sigm.common.repository.PersonaRepository;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.common.util.StringUtils;
import pe.gob.municipalidad.sigm.modules.administracion.dto.AuditoriaDtos.AuditoriaAdministrativaResponse;
import pe.gob.municipalidad.sigm.modules.administracion.entity.AuditoriaAdministrativa;
import pe.gob.municipalidad.sigm.modules.administracion.perfil.dto.PerfilDtos;
import pe.gob.municipalidad.sigm.modules.administracion.perfil.entity.UsuarioPreferencia;
import pe.gob.municipalidad.sigm.modules.administracion.perfil.repository.UsuarioPreferenciaRepository;
import pe.gob.municipalidad.sigm.modules.administracion.repository.AuditoriaAdministrativaRepository;
import pe.gob.municipalidad.sigm.modules.administracion.service.AuditoriaAdministrativaService;
import pe.gob.municipalidad.sigm.security.model.RefreshToken;
import pe.gob.municipalidad.sigm.security.repository.RefreshTokenRepository;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PerfilService {

    private final UsuarioRepository usuarioRepository;
    private final PersonaRepository personaRepository;
    private final PersonaOficinaRepository personaOficinaRepository;
    private final UsuarioPreferenciaRepository preferenciaRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuditoriaAdministrativaRepository auditoriaRepository;
    private final AuditoriaAdministrativaService auditoriaService;
    private final PasswordEncoder passwordEncoder;

    private static final String UPLOAD_DIR = "uploads/avatars";
    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024; // 2 MB
    private static final Pattern SAFE_FILENAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_\\-]+\\.(jpg|jpeg|png|webp)$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&._\\-#]).{8,}$");

    // ==========================================
    // 1. CONSULTA DE PERFIL PERSONAL
    // ==========================================

    @Transactional(readOnly = true)
    public PerfilDtos.PerfilResponse obtenerPerfilUsuario(String username) {
        Usuario usuario = buscarUsuarioPorUsername(username);
        Persona persona = usuario.getPersona();

        // Enmascaramiento seguro del número de documento
        String docEnmascarado = "DNI".equalsIgnoreCase(persona.getTipoDocumento())
                ? StringUtils.maskDni(persona.getNumeroDocumento())
                : StringUtils.maskRuc(persona.getNumeroDocumento());

        // Asignación principal
        Optional<PersonaOficina> principalOpt = personaOficinaRepository.findByPersonaIdAndPrincipalTrue(persona.getId());
        PerfilDtos.OficinaInfoDto oficinaDto = null;
        PerfilDtos.CargoInfoDto cargoDto = null;

        if (principalOpt.isPresent()) {
            PersonaOficina po = principalOpt.get();
            if (po.getOficina() != null) {
                oficinaDto = PerfilDtos.OficinaInfoDto.builder()
                        .id(po.getOficina().getId())
                        .codigo(po.getOficina().getCodigo())
                        .nombre(po.getOficina().getNombre())
                        .sigla(po.getOficina().getSigla())
                        .build();
            }
            if (po.getCargo() != null) {
                cargoDto = PerfilDtos.CargoInfoDto.builder()
                        .id(po.getCargo().getId())
                        .codigo(po.getCargo().getCodigo())
                        .nombre(po.getCargo().getNombre())
                        .nivelJerarquico(po.getCargo().getNivelJerarquico())
                        .build();
            }
        }

        // Roles institucionales
        List<PerfilDtos.RolInfoDto> rolesDto = usuario.getRoles().stream()
                .map(r -> PerfilDtos.RolInfoDto.builder()
                        .id(r.getId())
                        .codigo(r.getCodigo())
                        .nombre(r.getNombre())
                        .descripcion(r.getDescripcion())
                        .build())
                .collect(Collectors.toList());

        // Tipos de actores asociados
        List<PersonaOficina> todasAsignaciones = personaOficinaRepository.findByPersonaId(persona.getId());
        List<String> tiposActores = todasAsignaciones.stream()
                .map(PersonaOficina::getTipoActor)
                .filter(Objects::nonNull)
                .map(t -> t.getNombre())
                .distinct()
                .collect(Collectors.toList());

        if (tiposActores.isEmpty()) {
            tiposActores = List.of("Ciudadano");
        }

        // Preferencias
        PerfilDtos.UsuarioPreferenciaDto prefDto = obtenerPreferenciasDto(usuario.getId());

        return PerfilDtos.PerfilResponse.builder()
                .usuarioId(usuario.getId())
                .personaId(persona.getId())
                .username(usuario.getUsername())
                .nombres(persona.getNombres())
                .apellidoPaterno(persona.getApellidoPaterno())
                .apellidoMaterno(persona.getApellidoMaterno())
                .nombreCompleto(persona.getNombreCompleto())
                .tipoDocumento(persona.getTipoDocumento())
                .numeroDocumento(persona.getNumeroDocumento())
                .numeroDocumentoEnmascarado(docEnmascarado)
                .correo(persona.getCorreo())
                .telefono(persona.getTelefono())
                .direccion(persona.getDireccion())
                .fotoUrl(persona.getFotoUrl())
                .enabled(usuario.getEnabled())
                .accountLocked(usuario.getAccountLocked())
                .fechaRegistro(usuario.getCreatedAt())
                .roles(rolesDto)
                .oficinaPrincipal(oficinaDto)
                .cargoPrincipal(cargoDto)
                .tiposActores(tiposActores)
                .preferencias(prefDto)
                .build();
    }

    // ==========================================
    // 2. EDICIÓN DE DATOS PERSONALES PERMITIDOS
    // ==========================================

    @Transactional
    public PerfilDtos.PerfilResponse actualizarDatosPersonales(String username, PerfilDtos.ActualizarDatosPersonalesRequest request) {
        Usuario usuario = buscarUsuarioPorUsername(username);
        Persona persona = usuario.getPersona();

        String nuevoCorreo = request.getCorreo() != null ? request.getCorreo().trim() : null;

        if (nuevoCorreo != null && !nuevoCorreo.equalsIgnoreCase(persona.getCorreo())) {
            boolean correoOcupado = personaRepository.existsByCorreoAndIdNot(nuevoCorreo, persona.getId());
            if (correoOcupado) {
                throw new DuplicateResourceException("El correo electrónico ya está registrado por otro administrado o usuario.");
            }
            persona.setCorreo(nuevoCorreo);
        }

        if (request.getTelefono() != null) {
            persona.setTelefono(request.getTelefono().trim());
        }

        if (request.getDireccion() != null) {
            persona.setDireccion(request.getDireccion().trim());
        }

        personaRepository.save(persona);

        auditoriaService.registrar(
                "ACTUALIZAR_DATOS_PERSONALES",
                "Persona",
                persona.getId(),
                "EXITO",
                "Actualización de datos personales del usuario: " + username
        );

        return obtenerPerfilUsuario(username);
    }

    // ==========================================
    // 3. CAMBIO DE CONTRASEÑA
    // ==========================================

    @Transactional
    public void cambiarPassword(String username, PerfilDtos.CambiarPasswordRequest request) {
        Usuario usuario = buscarUsuarioPorUsername(username);

        // 1. Validar contraseña actual con BCrypt
        if (!passwordEncoder.matches(request.getPasswordActual(), usuario.getPasswordHash())) {
            auditoriaService.registrar(
                    "INTENTO_CAMBIO_PASSWORD_FALLIDO",
                    "Usuario",
                    usuario.getId(),
                    "FALLO",
                    "Contraseña actual incorrecta para usuario: " + username
            );
            throw new InvalidRequestException("La contraseña actual proporcionada es incorrecta.");
        }

        // 2. Validar coincidencia de nueva contraseña
        if (!request.getPasswordNuevo().equals(request.getConfirmacionPasswordNuevo())) {
            throw new InvalidRequestException("La nueva contraseña y su confirmación no coinciden.");
        }

        // 3. Validar complejidad
        if (!PASSWORD_PATTERN.matcher(request.getPasswordNuevo()).matches()) {
            throw new InvalidRequestException("La contraseña debe tener al menos 8 caracteres, incluyendo mayúsculas, minúsculas, números y caracteres especiales.");
        }

        // 4. Validar que no sea idéntica a la actual
        if (passwordEncoder.matches(request.getPasswordNuevo(), usuario.getPasswordHash())) {
            throw new InvalidRequestException("La nueva contraseña debe ser diferente a la contraseña actual.");
        }

        // 5. Cifrado y actualización
        usuario.setPasswordHash(passwordEncoder.encode(request.getPasswordNuevo()));
        usuarioRepository.save(usuario);

        // 6. Revocación de sesiones activas anteriores por política de seguridad
        refreshTokenRepository.revokeAllByUsuarioId(usuario.getId(), OffsetDateTime.now());

        auditoriaService.registrar(
                "CAMBIO_PASSWORD",
                "Usuario",
                usuario.getId(),
                "EXITO",
                "Cambio exitoso de contraseña para usuario: " + username + ". Sesiones anteriores revocadas."
        );
    }

    // ==========================================
    // 4. CONSULTA DE ROLES Y PERMISOS EFECTIVOS
    // ==========================================

    @Transactional(readOnly = true)
    public PerfilDtos.PermisosConsolidadosDto obtenerPermisosUsuario(String username) {
        Usuario usuario = buscarUsuarioPorUsername(username);

        List<PerfilDtos.RolInfoDto> rolesDto = usuario.getRoles().stream()
                .map(r -> PerfilDtos.RolInfoDto.builder()
                        .id(r.getId())
                        .codigo(r.getCodigo())
                        .nombre(r.getNombre())
                        .descripcion(r.getDescripcion())
                        .build())
                .collect(Collectors.toList());

        List<PerfilDtos.PermisoDetalleDto> permisosDto = new ArrayList<>();
        List<String> modulos = new ArrayList<>();

        usuario.getRoles().forEach(r -> {
            r.getPermisos().forEach(p -> {
                String modCodigo = p.getModulo() != null ? p.getModulo().getCodigo() : "GENERAL";
                permisosDto.add(PerfilDtos.PermisoDetalleDto.builder()
                        .id(p.getId())
                        .modulo(modCodigo)
                        .accion(p.getAccion())
                        .codigo(p.getCodigo())
                        .descripcion(p.getDescripcion())
                        .build());
                if (!modulos.contains(modCodigo)) {
                    modulos.add(modCodigo);
                }
            });
        });

        return PerfilDtos.PermisosConsolidadosDto.builder()
                .roles(rolesDto)
                .modulosAutorizados(modulos)
                .permisosEfectivos(permisosDto)
                .build();
    }

    // ==========================================
    // 5. OFICINAS Y CARGOS ASIGNADOS
    // ==========================================

    @Transactional(readOnly = true)
    public List<PerfilDtos.AsignacionOficinaDto> obtenerAsignacionesUsuario(String username) {
        Usuario usuario = buscarUsuarioPorUsername(username);
        List<PersonaOficina> asignaciones = personaOficinaRepository.findByPersonaId(usuario.getPersona().getId());

        return asignaciones.stream()
                .map(a -> PerfilDtos.AsignacionOficinaDto.builder()
                        .id(a.getId())
                        .oficinaId(a.getOficina() != null ? a.getOficina().getId() : null)
                        .oficinaNombre(a.getOficina() != null ? a.getOficina().getNombre() : "Sin Oficina")
                        .oficinaCodigo(a.getOficina() != null ? a.getOficina().getCodigo() : "-")
                        .cargoId(a.getCargo() != null ? a.getCargo().getId() : null)
                        .cargoNombre(a.getCargo() != null ? a.getCargo().getNombre() : "Sin Cargo Asignado")
                        .tipoActorId(a.getTipoActor() != null ? a.getTipoActor().getId() : null)
                        .tipoActorNombre(a.getTipoActor() != null ? a.getTipoActor().getNombre() : "-")
                        .fechaInicio(a.getFechaInicio())
                        .fechaFin(a.getFechaFin())
                        .principal(a.getPrincipal())
                        .estado(a.getEstado())
                        .build())
                .collect(Collectors.toList());
    }

    // ==========================================
    // 6. PREFERENCIAS PERSONALES
    // ==========================================

    @Transactional(readOnly = true)
    public PerfilDtos.UsuarioPreferenciaDto obtenerPreferencias(String username) {
        Usuario usuario = buscarUsuarioPorUsername(username);
        return obtenerPreferenciasDto(usuario.getId());
    }

    @Transactional
    public PerfilDtos.UsuarioPreferenciaDto guardarPreferencias(String username, PerfilDtos.UsuarioPreferenciaDto request) {
        Usuario usuario = buscarUsuarioPorUsername(username);

        UsuarioPreferencia pref = preferenciaRepository.findByUsuarioId(usuario.getId())
                .orElse(UsuarioPreferencia.builder()
                        .usuario(usuario)
                        .tema("OSCURO")
                        .idioma("es")
                        .notifEmail(true)
                        .notifSistema(true)
                        .pageSize(10)
                        .sidebarColapsado(false)
                        .build());

        if (request.getTema() != null) pref.setTema(request.getTema().toUpperCase());
        if (request.getIdioma() != null) pref.setIdioma(request.getIdioma().toLowerCase());
        if (request.getNotifEmail() != null) pref.setNotifEmail(request.getNotifEmail());
        if (request.getNotifSistema() != null) pref.setNotifSistema(request.getNotifSistema());
        if (request.getPageSize() != null) pref.setPageSize(Math.max(5, Math.min(100, request.getPageSize())));
        if (request.getSidebarColapsado() != null) pref.setSidebarColapsado(request.getSidebarColapsado());

        UsuarioPreferencia guardada = preferenciaRepository.save(pref);

        auditoriaService.registrar(
                "ACTUALIZAR_PREFERENCIAS",
                "UsuarioPreferencia",
                guardada.getId(),
                "EXITO",
                "Actualización de preferencias de usuario: " + username
        );

        return toPreferenciaDto(guardada);
    }

    private PerfilDtos.UsuarioPreferenciaDto obtenerPreferenciasDto(Long usuarioId) {
        return preferenciaRepository.findByUsuarioId(usuarioId)
                .map(this::toPreferenciaDto)
                .orElse(PerfilDtos.UsuarioPreferenciaDto.builder()
                        .tema("OSCURO")
                        .idioma("es")
                        .notifEmail(true)
                        .notifSistema(true)
                        .pageSize(10)
                        .sidebarColapsado(false)
                        .build());
    }

    private PerfilDtos.UsuarioPreferenciaDto toPreferenciaDto(UsuarioPreferencia p) {
        return PerfilDtos.UsuarioPreferenciaDto.builder()
                .tema(p.getTema())
                .idioma(p.getIdioma())
                .notifEmail(p.getNotifEmail())
                .notifSistema(p.getNotifSistema())
                .pageSize(p.getPageSize())
                .sidebarColapsado(p.getSidebarColapsado())
                .build();
    }

    // ==========================================
    // 7. GESTIÓN SEGURA DE FOTOGRAFÍA DE PERFIL
    // ==========================================

    @Transactional
    public String guardarFotografia(String username, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidRequestException("El archivo de fotografía no puede estar vacío.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new InvalidRequestException("El tamaño máximo permitido para la fotografía es de 2 MB.");
        }

        // Validación de extensión y tipo MIME
        String originalFilename = file.getOriginalFilename();
        String extension = extraerExtension(originalFilename);
        if (!List.of("jpg", "jpeg", "png", "webp").contains(extension.toLowerCase())) {
            throw new InvalidRequestException("Formato no permitido. Solo se admiten imágenes JPG, PNG y WebP.");
        }

        // Validación estricta de Magic Bytes en contenido binario
        try (InputStream is = file.getInputStream()) {
            byte[] header = new byte[12];
            int read = is.read(header);
            if (!esImagenValida(header, read, extension)) {
                throw new InvalidRequestException("El contenido del archivo no coincide con una imagen válida permitida.");
            }
        } catch (IOException e) {
            throw new InvalidRequestException("No se pudo leer el archivo subido: " + e.getMessage());
        }

        Usuario usuario = buscarUsuarioPorUsername(username);
        Persona persona = usuario.getPersona();

        // Eliminar avatar anterior físico si existía
        eliminarArchivoFisicoSiExiste(persona.getFotoUrl());

        // Generar nombre interno aleatorio y no predecible (previene path traversal y colisiones)
        String nuevoNombre = "avatar_" + usuario.getId() + "_" + UUID.randomUUID().toString().replace("-", "") + "." + extension.toLowerCase();

        try {
            Path targetDir = Paths.get(UPLOAD_DIR);
            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
            }
            Path targetPath = targetDir.resolve(nuevoNombre).normalize();
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("Error al guardar fotografía de perfil para usuario {}: {}", username, e.getMessage());
            throw new InvalidRequestException("Error al almacenar el archivo en el servidor.");
        }

        String fotoUrl = "/api/perfil/fotografia/" + nuevoNombre;
        persona.setFotoUrl(fotoUrl);
        personaRepository.save(persona);

        auditoriaService.registrar(
                "ACTUALIZAR_FOTOGRAFIA",
                "Persona",
                persona.getId(),
                "EXITO",
                "Fotografía de perfil actualizada para usuario: " + username
        );

        return fotoUrl;
    }

    @Transactional
    public void eliminarFotografia(String username) {
        Usuario usuario = buscarUsuarioPorUsername(username);
        Persona persona = usuario.getPersona();

        if (persona.getFotoUrl() != null) {
            eliminarArchivoFisicoSiExiste(persona.getFotoUrl());
            persona.setFotoUrl(null);
            personaRepository.save(persona);

            auditoriaService.registrar(
                    "ELIMINAR_FOTOGRAFIA",
                    "Persona",
                    persona.getId(),
                    "EXITO",
                    "Fotografía de perfil eliminada para usuario: " + username
            );
        }
    }

    @Transactional(readOnly = true)
    public Resource obtenerRecursoFotografia(String filename) {
        if (filename == null || !SAFE_FILENAME_PATTERN.matcher(filename).matches() || filename.contains("..")) {
            throw new ResourceNotFoundException("Archivo de fotografía inválido.");
        }

        try {
            Path filePath = Paths.get(UPLOAD_DIR).resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new ResourceNotFoundException("Fotografía no encontrada en el almacenamiento.");
            }
        } catch (MalformedURLException e) {
            throw new ResourceNotFoundException("Ruta de archivo no válida.");
        }
    }

    private boolean esImagenValida(byte[] header, int readBytes, String extension) {
        if (readBytes < 4) return false;

        // JPEG: FF D8 FF
        if ((header[0] & 0xFF) == 0xFF && (header[1] & 0xFF) == 0xD8 && (header[2] & 0xFF) == 0xFF) {
            return true;
        }

        // PNG: 89 50 4E 47
        if ((header[0] & 0xFF) == 0x89 && header[1] == 0x50 && header[2] == 0x4E && header[3] == 0x47) {
            return true;
        }

        // WebP: RIFF ... WEBP
        if (readBytes >= 12 && header[0] == 'R' && header[1] == 'I' && header[2] == 'F' && header[3] == 'F'
                && header[8] == 'W' && header[9] == 'E' && header[10] == 'B' && header[11] == 'P') {
            return true;
        }

        return false;
    }

    private String extraerExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    private void eliminarArchivoFisicoSiExiste(String fotoUrl) {
        if (fotoUrl != null && fotoUrl.startsWith("/api/perfil/fotografia/")) {
            String filename = fotoUrl.substring("/api/perfil/fotografia/".length());
            if (SAFE_FILENAME_PATTERN.matcher(filename).matches()) {
                try {
                    Path path = Paths.get(UPLOAD_DIR).resolve(filename).normalize();
                    Files.deleteIfExists(path);
                } catch (IOException e) {
                    log.warn("No se pudo eliminar archivo físico de avatar {}: {}", filename, e.getMessage());
                }
            }
        }
    }

    // ==========================================
    // 8. SESIONES ACTIVAS
    // ==========================================

    @Transactional(readOnly = true)
    public List<PerfilDtos.SesionActivaDto> obtenerSesionesActivas(String username) {
        Usuario usuario = buscarUsuarioPorUsername(username);
        List<RefreshToken> tokens = refreshTokenRepository.findActiveByUsuarioId(usuario.getId(), OffsetDateTime.now());

        return tokens.stream()
                .map(t -> PerfilDtos.SesionActivaDto.builder()
                        .id(t.getId())
                        .tokenId(t.getTokenId())
                        .ip(t.getCreatedByIp() != null ? t.getCreatedByIp() : "127.0.0.1")
                        .userAgent(t.getUserAgent())
                        .dispositivoAproximado(identificarDispositivo(t.getUserAgent()))
                        .fechaInicio(t.getCreatedAt())
                        .fechaExpiracion(t.getExpiresAt())
                        .activa(t.isActive())
                        .esActual(false)
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public void revocarOtrasSesiones(String username, Long sesionActualId) {
        Usuario usuario = buscarUsuarioPorUsername(username);

        if (sesionActualId != null) {
            refreshTokenRepository.revokeAllByUsuarioIdExcept(usuario.getId(), sesionActualId, OffsetDateTime.now());
        } else {
            refreshTokenRepository.revokeAllByUsuarioId(usuario.getId(), OffsetDateTime.now());
        }

        auditoriaService.registrar(
                "REVOCAR_OTRAS_SESIONES",
                "RefreshToken",
                usuario.getId(),
                "EXITO",
                "Otras sesiones activas revocadas para el usuario: " + username
        );
    }

    @Transactional
    public void revocarSesion(String username, Long sesionId) {
        Usuario usuario = buscarUsuarioPorUsername(username);
        RefreshToken token = refreshTokenRepository.findById(sesionId)
                .orElseThrow(() -> new ResourceNotFoundException("Sesión no encontrada con ID: " + sesionId));

        if (!token.getUsuario().getId().equals(usuario.getId())) {
            throw new ResourceNotFoundException("Sesión no encontrada o no pertenece al usuario autenticado.");
        }

        token.setRevoked(true);
        token.setRevokedAt(OffsetDateTime.now());
        refreshTokenRepository.save(token);

        auditoriaService.registrar(
                "REVOCAR_SESION",
                "RefreshToken",
                sesionId,
                "EXITO",
                "Sesión " + sesionId + " revocada por el usuario: " + username
        );
    }

    private String identificarDispositivo(String ua) {
        if (ua == null || ua.isBlank()) return "Dispositivo Desconocido";
        String s = ua.toLowerCase();
        if (s.contains("windows")) return "Windows PC";
        if (s.contains("android")) return "Dispositivo Android";
        if (s.contains("iphone") || s.contains("ipad") || s.contains("ios")) return "Apple iOS";
        if (s.contains("macintosh") || s.contains("mac os")) return "Mac OS";
        if (s.contains("linux")) return "Linux PC";
        return "Navegador Web";
    }

    // ==========================================
    // 9. ACTIVIDAD DE SEGURIDAD RECIENTE
    // ==========================================

    @Transactional(readOnly = true)
    public Page<AuditoriaAdministrativaResponse> obtenerActividadSeguridad(String username, Pageable pageable) {
        Page<AuditoriaAdministrativa> logs = auditoriaRepository.findByUsuarioResponsableOrderByCreatedAtDesc(username, pageable);
        return logs.map(a -> AuditoriaAdministrativaResponse.builder()
                .id(a.getId())
                .usuarioResponsable(a.getUsuarioResponsable())
                .accion(a.getAccion())
                .entidadAfectada(a.getEntidadAfectada())
                .entidadId(a.getEntidadId())
                .resultado(a.getResultado())
                .detalle(a.getDetalle())
                .ipOrigen(a.getIpOrigen())
                .userAgent(a.getUserAgent())
                .traceId(a.getTraceId())
                .createdAt(a.getCreatedAt())
                .build());
    }

    // ==========================================
    // HELPERS
    // ==========================================

    private Usuario buscarUsuarioPorUsername(String username) {
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));
    }
}
