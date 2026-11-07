package pe.gob.municipalidad.sigm.common.notification.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Evento interno de dominio que representa una alerta de seguridad o evento crítico
 * que requiere notificación inmediata al usuario afectado.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificacionSeguridadEvent {

    private Long usuarioId;
    private String titulo;
    private String descripcion;
    private String tipoEvento; // PASSWORD_CHANGED, EMAIL_CHANGED, ACCOUNT_LOCKED, SESSION_REVOKED, PERMISSION_CHANGED
    private String ipOrigen;
    private String userAgent;
}
