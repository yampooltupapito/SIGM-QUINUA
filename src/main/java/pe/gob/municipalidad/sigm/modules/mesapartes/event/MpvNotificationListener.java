package pe.gob.municipalidad.sigm.modules.mesapartes.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.notification.dto.NotificationDtos.EnviarNotificacionOficinaRequest;
import pe.gob.municipalidad.sigm.common.notification.dto.NotificationDtos.EnviarNotificacionRequest;
import pe.gob.municipalidad.sigm.common.notification.service.CentralNotificationService;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;

import java.util.Optional;

/**
 * Listener de eventos de dominio de Mesa de Partes Virtual.
 * Se ejecuta tras la confirmación de la transacción (AFTER_COMMIT) para garantizar
 * que nunca se generen notificaciones sobre operaciones fallidas o revertidas.
 */
@Component
public class MpvNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(MpvNotificationListener.class);

    private final CentralNotificationService notificationService;
    private final UsuarioRepository usuarioRepository;

    public MpvNotificationListener(CentralNotificationService notificationService,
                                   UsuarioRepository usuarioRepository) {
        this.notificationService = notificationService;
        this.usuarioRepository = usuarioRepository;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onExpedientePresentado(ExpedientePresentadoEvent event) {
        try {
            log.info("Notificando presentación de expediente: {}", event.getNumeroExpediente());

            // Notificar al solicitante si cuenta con usuario en el sistema
            obtenerUsuarioId(event.getSolicitanteId()).ifPresent(usuarioId -> {
                notificationService.enviarNotificacion(
                        EnviarNotificacionRequest.builder()
                                .usuarioId(usuarioId)
                                .titulo("Expediente Presentado: " + event.getNumeroExpediente())
                                .mensaje("Su expediente '" + event.getAsunto() + "' ha sido registrado virtualmente con código " + event.getNumeroExpediente() + ". Se encuentra en espera de recepción.")
                                .tipo("TRAMITE")
                                .referenciaId(event.getNumeroExpediente())
                                .urlDestino("/portal/tramites/" + event.getExpedienteId())
                                .enviarCorreo(true)
                                .build(),
                        "SISTEMA_MPV"
                );
            });

            // Notificar a la oficina responsable
            if (event.getOficinaId() != null) {
                notificationService.enviarNotificacionOficina(
                        EnviarNotificacionOficinaRequest.builder()
                                .oficinaId(event.getOficinaId())
                                .titulo("Nuevo Expediente Presentado: " + event.getNumeroExpediente())
                                .mensaje("Se ha presentado el expediente '" + event.getAsunto() + "' asignado a su despacho.")
                                .tipo("TRAMITE")
                                .urlDestino("/admin/mesa-partes/expedientes/" + event.getExpedienteId())
                                .canal("INTERNO")
                                .build(),
                        "SISTEMA_MPV"
                );
            }
        } catch (Exception e) {
            log.error("Error al procesar notificación de presentación para expediente {}: {}", event.getNumeroExpediente(), e.getMessage());
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onExpedienteRecibido(ExpedienteRecibidoEvent event) {
        try {
            log.info("Notificando recepción de expediente: {}", event.getNumeroExpediente());
            obtenerUsuarioId(event.getSolicitanteId()).ifPresent(usuarioId -> {
                notificationService.enviarNotificacion(
                        EnviarNotificacionRequest.builder()
                                .usuarioId(usuarioId)
                                .titulo("Expediente Recibido: " + event.getNumeroExpediente())
                                .mensaje("Su expediente " + event.getNumeroExpediente() + " ha sido recibido formalmente por la Mesa de Partes.")
                                .tipo("TRAMITE")
                                .referenciaId(event.getNumeroExpediente())
                                .urlDestino("/portal/tramites/" + event.getExpedienteId())
                                .enviarCorreo(true)
                                .build(),
                        "SISTEMA_MPV"
                );
            });
        } catch (Exception e) {
            log.error("Error al notificar recepción de expediente {}: {}", event.getNumeroExpediente(), e.getMessage());
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onExpedienteObservado(ExpedienteObservadoEvent event) {
        try {
            log.info("Notificando observación de expediente: {}", event.getNumeroExpediente());
            obtenerUsuarioId(event.getSolicitanteId()).ifPresent(usuarioId -> {
                notificationService.enviarNotificacion(
                        EnviarNotificacionRequest.builder()
                                .usuarioId(usuarioId)
                                .titulo("Expediente Observado: " + event.getNumeroExpediente())
                                .mensaje("Su expediente presenta observaciones que requieren subsanación: " + event.getDetalle())
                                .tipo("OBSERVACION")
                                .referenciaId(event.getNumeroExpediente())
                                .urlDestino("/portal/tramites/" + event.getExpedienteId())
                                .enviarCorreo(true)
                                .build(),
                        "SISTEMA_MPV"
                );
            });
        } catch (Exception e) {
            log.error("Error al notificar observación de expediente {}: {}", event.getNumeroExpediente(), e.getMessage());
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onExpedienteDerivado(ExpedienteDerivadoEvent event) {
        try {
            log.info("Notificando derivación de expediente: {} a oficina {}", event.getNumeroExpediente(), event.getOficinaDestinoId());
            if (event.getOficinaDestinoId() != null) {
                notificationService.enviarNotificacionOficina(
                        EnviarNotificacionOficinaRequest.builder()
                                .oficinaId(event.getOficinaDestinoId())
                                .titulo("Expediente Derivado: " + event.getNumeroExpediente())
                                .mensaje("Ha recibido el expediente '" + event.getAsunto() + "'. Motivo: " + event.getDetalle())
                                .tipo("TRAMITE")
                                .urlDestino("/admin/mesa-partes/expedientes/" + event.getExpedienteId())
                                .canal("INTERNO")
                                .build(),
                        "SISTEMA_MPV"
                );
            }
        } catch (Exception e) {
            log.error("Error al notificar derivación de expediente {}: {}", event.getNumeroExpediente(), e.getMessage());
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onExpedienteResuelto(ExpedienteResueltoEvent event) {
        try {
            log.info("Notificando resolución de expediente: {}", event.getNumeroExpediente());
            obtenerUsuarioId(event.getSolicitanteId()).ifPresent(usuarioId -> {
                notificationService.enviarNotificacion(
                        EnviarNotificacionRequest.builder()
                                .usuarioId(usuarioId)
                                .titulo("Expediente Resuelto: " + event.getNumeroExpediente())
                                .mensaje("Su expediente " + event.getNumeroExpediente() + " ha sido resuelto formalmente.")
                                .tipo("APROBACION")
                                .referenciaId(event.getNumeroExpediente())
                                .urlDestino("/portal/tramites/" + event.getExpedienteId())
                                .enviarCorreo(true)
                                .build(),
                        "SISTEMA_MPV"
                );
            });
        } catch (Exception e) {
            log.error("Error al notificar resolución de expediente {}: {}", event.getNumeroExpediente(), e.getMessage());
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onExpedienteArchivado(ExpedienteArchivadoEvent event) {
        try {
            log.info("Notificando archivo de expediente: {}", event.getNumeroExpediente());
            obtenerUsuarioId(event.getSolicitanteId()).ifPresent(usuarioId -> {
                notificationService.enviarNotificacion(
                        EnviarNotificacionRequest.builder()
                                .usuarioId(usuarioId)
                                .titulo("Expediente Concluido y Archivado: " + event.getNumeroExpediente())
                                .mensaje("El expediente " + event.getNumeroExpediente() + " ha sido archivado.")
                                .tipo("INFO")
                                .referenciaId(event.getNumeroExpediente())
                                .urlDestino("/portal/tramites/" + event.getExpedienteId())
                                .enviarCorreo(false)
                                .build(),
                        "SISTEMA_MPV"
                );
            });
        } catch (Exception e) {
            log.error("Error al notificar archivo de expediente {}: {}", event.getNumeroExpediente(), e.getMessage());
        }
    }

    private Optional<Long> obtenerUsuarioId(Long solicitantePersonaId) {
        if (solicitantePersonaId == null) {
            return Optional.empty();
        }
        return usuarioRepository.findByPersonaId(solicitantePersonaId).map(Usuario::getId);
    }
}
