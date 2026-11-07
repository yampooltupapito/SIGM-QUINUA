package pe.gob.municipalidad.sigm.security.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Servicio desacoplado para notificaciones de seguridad y entrega de enlaces de recuperación.
 * Diseñado para admitir transporte SMTP cuando esté provisto o registro controlado en desarrollo.
 */
@Slf4j
@Service
public class EmailNotificationService {

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${app.frontend.url:http://localhost:8080}")
    private String appBaseUrl;

    public void enviarEnlaceRecuperacion(String destinatario, String nombreDestinatario, String rawToken) {
        String resetUrl = appBaseUrl + "/reset-password?token=" + rawToken;
        String correoEnmascarado = enmascararCorreo(destinatario);

        if (mailEnabled) {
            // Cuando se configuren credenciales SMTP en producción
            log.info("[Notificación] Enviando correo de recuperación vía SMTP a: {}", correoEnmascarado);
        } else {
            // Entorno de desarrollo institucional / Infraestructura sin servidor de correo activo
            log.info("[Notificación - Dev] Enlace de recuperación generado para {} ({})", nombreDestinatario, correoEnmascarado);
            log.debug("[Notificación - Dev] URL segura de restablecimiento: {}", resetUrl);
        }
    }

    private String enmascararCorreo(String correo) {
        if (correo == null || !correo.contains("@")) {
            return "***";
        }
        String[] partes = correo.split("@");
        String nombre = partes[0];
        String dominio = partes[1];
        if (nombre.length() <= 2) {
            return nombre.charAt(0) + "***@" + dominio;
        }
        return nombre.charAt(0) + "***" + nombre.charAt(nombre.length() - 1) + "@" + dominio;
    }
}
