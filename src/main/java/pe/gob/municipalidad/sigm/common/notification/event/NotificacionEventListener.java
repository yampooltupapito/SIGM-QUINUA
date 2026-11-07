package pe.gob.municipalidad.sigm.common.notification.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import pe.gob.municipalidad.sigm.common.notification.dto.NotificationDtos.EnviarNotificacionRequest;
import pe.gob.municipalidad.sigm.common.notification.service.CentralNotificationService;

/**
 * Receptor desacoplado de eventos del sistema que dispara notificaciones internas
 * y correos transaccionales automáticamente.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificacionEventListener {

    private final CentralNotificationService centralNotificationService;

    @Async
    @EventListener
    public void manejarEventoSeguridad(NotificacionSeguridadEvent evento) {
        if (evento == null || evento.getUsuarioId() == null) {
            return;
        }

        try {
            log.info("Procesando evento de seguridad '{}' para usuario ID {}", evento.getTipoEvento(), evento.getUsuarioId());

            EnviarNotificacionRequest req = EnviarNotificacionRequest.builder()
                    .usuarioId(evento.getUsuarioId())
                    .titulo(evento.getTitulo() != null ? evento.getTitulo() : "Alerta de Seguridad Institucional")
                    .mensaje(evento.getDescripcion() != null ? evento.getDescripcion() : "Se ha registrado un evento de seguridad en su cuenta.")
                    .tipo("SEGURIDAD")
                    .urlDestino("/perfil?tab=seguridad")
                    .enviarCorreo(true)
                    .build();

            centralNotificationService.enviarNotificacion(req, "SEGURIDAD:" + (evento.getTipoEvento() != null ? evento.getTipoEvento() : "EVENTO"));
        } catch (Exception e) {
            log.error("Fallo al procesar evento de seguridad para usuario {}: {}", evento.getUsuarioId(), e.getMessage(), e);
        }
    }
}
