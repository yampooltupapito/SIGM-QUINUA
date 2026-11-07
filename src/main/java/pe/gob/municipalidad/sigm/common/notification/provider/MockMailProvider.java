package pe.gob.municipalidad.sigm.common.notification.provider;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Proveedor simulado de correo electrónico para entornos de prueba y desarrollo local.
 * Registra los despachos de correo en memoria y logs sin contactar servicios SMTP externos.
 */
@Slf4j
@Component("mockMailProvider")
public class MockMailProvider implements MailProvider {

    @Getter
    @Setter
    private boolean simularFallo = false;

    private final List<MockEmailItem> correosEnviados = Collections.synchronizedList(new ArrayList<>());

    @Override
    public SendResult send(String to, String toName, String subject, String htmlBody, String textBody) {
        if (simularFallo) {
            log.warn("[MOCK MAIL] Simulación de error de proveedor de correo activada para destinatario: {}", to);
            return SendResult.error("Fallo simulado de conexión con el proveedor SMTP/Mail de prueba", getProviderName());
        }

        String messageId = "mock-" + UUID.randomUUID();
        correosEnviados.add(MockEmailItem.builder()
                .destinatario(to)
                .destinatarioNombre(toName)
                .asunto(subject)
                .cuerpoHtml(htmlBody)
                .cuerpoTexto(textBody)
                .fechaEnvio(OffsetDateTime.now())
                .messageId(messageId)
                .build());

        log.info("[MOCK MAIL] Despacho simulado de correo exitoso. MessageId: {}, Destinatario: <{}> {}, Asunto: '{}'",
                messageId, to, toName != null ? toName : "", subject);
        return SendResult.ok(messageId, getProviderName());
    }

    @Override
    public String getProviderName() {
        return "MOCK";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    public List<MockEmailItem> getCorreosEnviados() {
        return new ArrayList<>(correosEnviados);
    }

    public void limpiarBandejaSimulada() {
        correosEnviados.clear();
        simularFallo = false;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class MockEmailItem {
        private String destinatario;
        private String destinatarioNombre;
        private String asunto;
        private String cuerpoHtml;
        private String cuerpoTexto;
        private OffsetDateTime fechaEnvio;
        private String messageId;
    }
}
