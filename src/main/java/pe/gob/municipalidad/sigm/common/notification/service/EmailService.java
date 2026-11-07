package pe.gob.municipalidad.sigm.common.notification.service;

import pe.gob.municipalidad.sigm.common.notification.dto.NotificationDtos;
import pe.gob.municipalidad.sigm.common.notification.entity.NotificacionEnvio;

import java.util.List;

public interface EmailService {

    NotificacionEnvio encolarCorreo(NotificationDtos.EmailRequestDto request);

    default NotificacionEnvio encolarEnvio(NotificationDtos.EmailRequestDto request) {
        return encolarCorreo(request);
    }

    void procesarColaEnvios();

    void despacharEnvioDirecto(Long envioId);

    void reintentarEnvio(Long envioId);

    List<NotificationDtos.NotificacionEnvioDto> obtenerHistorialUsuario(String username);
}
