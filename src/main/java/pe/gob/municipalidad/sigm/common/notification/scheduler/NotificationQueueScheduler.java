package pe.gob.municipalidad.sigm.common.notification.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pe.gob.municipalidad.sigm.common.notification.service.CentralNotificationService;
import pe.gob.municipalidad.sigm.common.notification.service.EmailService;

/**
 * Planificador en segundo plano para el procesamiento de la cola persistente de correos
 * y la ejecución de notificaciones institucionales programadas.
 * Zona horaria de referencia: America/Lima.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationQueueScheduler {

    private final EmailService emailService;
    private final CentralNotificationService centralNotificationService;

    /**
     * Procesa la cola de envíos pendientes de correo electrónico cada 60 segundos.
     */
    @Scheduled(fixedDelay = 60000, initialDelay = 15000)
    public void procesarColaEnviosCorreo() {
        try {
            emailService.procesarColaEnvios();
            log.debug("Procesamiento de cola de correos completado");
        } catch (Exception e) {
            log.error("Error en tarea programada de cola de correos: {}", e.getMessage(), e);
        }
    }

    /**
     * Procesa las notificaciones programadas vencidas cada 60 segundos.
     */
    @Scheduled(fixedDelay = 60000, initialDelay = 30000)
    public void procesarNotificacionesProgramadas() {
        try {
            int ejecutadas = centralNotificationService.procesarNotificacionesProgramadas();
            if (ejecutadas > 0) {
                log.debug("Procesamiento de notificaciones programadas completado: {} alertas emitidas", ejecutadas);
            }
        } catch (Exception e) {
            log.error("Error en tarea programada de notificaciones programadas: {}", e.getMessage(), e);
        }
    }
}
