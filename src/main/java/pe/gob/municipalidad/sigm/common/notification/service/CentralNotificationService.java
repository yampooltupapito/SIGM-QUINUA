package pe.gob.municipalidad.sigm.common.notification.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pe.gob.municipalidad.sigm.common.notification.dto.NotificationDtos.EnviarNotificacionOficinaRequest;
import pe.gob.municipalidad.sigm.common.notification.dto.NotificationDtos.EnviarNotificacionRequest;
import pe.gob.municipalidad.sigm.common.notification.dto.NotificationDtos.NotificacionItemDto;
import pe.gob.municipalidad.sigm.common.notification.dto.NotificationDtos.NotificacionPreferenciasDto;
import pe.gob.municipalidad.sigm.common.notification.dto.NotificationDtos.NotificacionProgramadaDto;
import pe.gob.municipalidad.sigm.common.notification.dto.NotificationDtos.ProgramarNotificacionRequest;

/**
 * Servicio centralizado de notificaciones institucionales, alertas y comunicaciones de SIGM-QUINUA.
 */
public interface CentralNotificationService {

    Page<NotificacionItemDto> buscarNotificacionesFiltradas(String username, Boolean leida, String tipo, String termino, Pageable pageable);

    NotificacionItemDto obtenerNotificacionPorId(Long id, String username);

    long contarNoLeidas(String username);

    void marcarComoLeida(Long notificacionId, String username);

    void marcarTodasComoLeidas(String username);

    NotificacionItemDto enviarNotificacion(EnviarNotificacionRequest request, String creadoPor);

    int enviarNotificacionOficina(EnviarNotificacionOficinaRequest request, String creadoPor);

    NotificacionProgramadaDto programarNotificacion(ProgramarNotificacionRequest request, String creadoPor);

    void cancelarProgramacion(Long programacionId, String usuario);

    int procesarNotificacionesProgramadas();

    NotificacionPreferenciasDto obtenerOInicializarPreferencias(String username);

    NotificacionPreferenciasDto actualizarPreferencias(String username, NotificacionPreferenciasDto dto);

    Page<NotificacionProgramadaDto> listarProgramadas(Pageable pageable);
}
