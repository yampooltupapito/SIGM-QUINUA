package pe.gob.municipalidad.sigm.common.notification.provider;

/**
 * Interfaz que desacopla la aplicación de los proveedores de correo electrónico (SMTP, Resend, Mock).
 */
public interface MailProvider {

    SendResult send(String to, String toName, String subject, String htmlBody, String textBody);

    String getProviderName();

    boolean isAvailable();
}
