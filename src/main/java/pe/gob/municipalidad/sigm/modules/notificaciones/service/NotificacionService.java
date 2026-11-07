package pe.gob.municipalidad.sigm.modules.notificaciones.service;

import pe.gob.municipalidad.sigm.modules.notificaciones.dto.NotificacionDto;

import java.util.List;

public interface NotificacionService {

    List<NotificacionDto> obtenerNotificacionesUsuario(String username);

    List<NotificacionDto> obtenerNotificacionesRecientes(String username);

    long contarNoLeidas(String username);

    void marcarComoLeida(Long notificacionId, String username);

    void marcarTodasComoLeidas(String username);

    void crearNotificacion(String username, String titulo, String mensaje, String tipo, String urlDestino, String origen);
}
