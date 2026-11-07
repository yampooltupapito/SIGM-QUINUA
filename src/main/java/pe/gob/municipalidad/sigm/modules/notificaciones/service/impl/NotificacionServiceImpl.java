package pe.gob.municipalidad.sigm.modules.notificaciones.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.notificaciones.dto.NotificacionDto;
import pe.gob.municipalidad.sigm.modules.notificaciones.model.Notificacion;
import pe.gob.municipalidad.sigm.modules.notificaciones.repository.NotificacionRepository;
import pe.gob.municipalidad.sigm.modules.notificaciones.service.NotificacionService;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificacionServiceImpl implements NotificacionService {

    private final NotificacionRepository notificacionRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public List<NotificacionDto> obtenerNotificacionesUsuario(String username) {
        Usuario usuario = usuarioRepository.findByUsername(username).orElse(null);
        if (usuario == null) {
            return Collections.emptyList();
        }
        return notificacionRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuario.getId())
                .stream()
                .map(NotificacionDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificacionDto> obtenerNotificacionesRecientes(String username) {
        Usuario usuario = usuarioRepository.findByUsername(username).orElse(null);
        if (usuario == null) {
            return Collections.emptyList();
        }
        return notificacionRepository.findTop5ByUsuarioIdOrderByFechaCreacionDesc(usuario.getId())
                .stream()
                .map(NotificacionDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long contarNoLeidas(String username) {
        Usuario usuario = usuarioRepository.findByUsername(username).orElse(null);
        if (usuario == null) {
            return 0L;
        }
        return notificacionRepository.countByUsuarioIdAndLeidaFalse(usuario.getId());
    }

    @Override
    @Transactional
    public void marcarComoLeida(Long notificacionId, String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));

        Notificacion notificacion = notificacionRepository.findById(notificacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Notificación no encontrada: " + notificacionId));

        if (!notificacion.getUsuario().getId().equals(usuario.getId())) {
            log.warn("Intento no autorizado de marcar notificación {} por usuario {}", notificacionId, username);
            throw new org.springframework.security.access.AccessDeniedException("Acceso denegado: La notificación no pertenece al usuario autenticado.");
        }

        notificacion.setLeida(true);
        notificacion.setFechaLectura(OffsetDateTime.now());
        notificacionRepository.save(notificacion);
    }

    @Override
    @Transactional
    public void marcarTodasComoLeidas(String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));

        notificacionRepository.marcarTodasComoLeidas(usuario.getId(), OffsetDateTime.now());
    }

    @Override
    @Transactional
    public void crearNotificacion(String username, String titulo, String mensaje, String tipo, String urlDestino, String origen) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));

        Notificacion notificacion = Notificacion.builder()
                .usuario(usuario)
                .titulo(titulo)
                .mensaje(mensaje)
                .tipo(tipo != null ? tipo : "INFO")
                .leida(false)
                .fechaCreacion(OffsetDateTime.now())
                .urlDestino(urlDestino)
                .origen(origen != null ? origen : "SISTEMA")
                .build();

        notificacionRepository.save(notificacion);
    }
}
