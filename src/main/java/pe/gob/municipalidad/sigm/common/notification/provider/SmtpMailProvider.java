package pe.gob.municipalidad.sigm.common.notification.provider;

import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Proveedor de correo SMTP estándar que utiliza JavaMailSender.
 * Se activa para despachos reales cuando MAIL_ENABLED=true y las credenciales están configuradas.
 */
@Slf4j
@Component("smtpMailProvider")
public class SmtpMailProvider implements MailProvider {

    private final JavaMailSender mailSender;

    @Value("${sigm.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${sigm.mail.from:no-reply@quinua.gob.pe}")
    private String fromAddress;

    @Value("${sigm.mail.from-name:Municipalidad Distrital de Quinua}")
    private String fromPersonalName;

    public SmtpMailProvider(@Autowired(required = false) JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public SendResult send(String to, String toName, String subject, String htmlBody, String textBody) {
        if (!mailEnabled || mailSender == null) {
            String simulatedId = "smtp-simulated-" + UUID.randomUUID();
            log.info("[SMTP SIMULADO] Correo no enviado por red (MAIL_ENABLED=false o JavaMailSender inactivo). Destinatario: {}, Asunto: '{}'", to, subject);
            return SendResult.ok(simulatedId, "SMTP_SIMULATED");
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());

            helper.setFrom(new InternetAddress(fromAddress, fromPersonalName, StandardCharsets.UTF_8.name()));
            if (toName != null && !toName.isBlank()) {
                helper.setTo(new InternetAddress(to, toName, StandardCharsets.UTF_8.name()));
            } else {
                helper.setTo(to);
            }

            helper.setSubject(subject);
            if (htmlBody != null && !htmlBody.isBlank()) {
                helper.setText(textBody != null ? textBody : "", htmlBody);
            } else {
                helper.setText(textBody != null ? textBody : "", false);
            }

            mailSender.send(message);
            String messageId = message.getMessageID() != null ? message.getMessageID() : UUID.randomUUID().toString();
            log.info("[SMTP REAL] Correo enviado satisfactoriamente. Id: {}, Destinatario: {}", messageId, to);
            return SendResult.ok(messageId, getProviderName());

        } catch (Exception e) {
            log.error("[SMTP ERROR] Error al enviar correo a {}: {}", to, e.getMessage());
            return SendResult.failure(e.getMessage(), getProviderName());
        }
    }

    @Override
    public String getProviderName() {
        return "SMTP";
    }

    @Override
    public boolean isAvailable() {
        return mailSender != null;
    }
}
