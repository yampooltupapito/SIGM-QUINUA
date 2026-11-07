package pe.gob.municipalidad.sigm.common.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.gob.municipalidad.sigm.common.notification.provider.MockMailProvider;
import pe.gob.municipalidad.sigm.common.notification.provider.SendResult;
import pe.gob.municipalidad.sigm.common.notification.template.NotificationTemplateEngine;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TemplateEngineAndMailUnitTest {

    private NotificationTemplateEngine templateEngine;
    private MockMailProvider mockMailProvider;

    @BeforeEach
    void setUp() {
        templateEngine = new NotificationTemplateEngine();
        mockMailProvider = new MockMailProvider();
        mockMailProvider.limpiarBandejaSimulada();
    }

    @Test
    @DisplayName("10 & 20. Renderizado de plantillas y protección estricta contra XSS (escaping)")
    void testRenderizadoPlantillaYEscapingXSS() {
        String plantillaHtml = "Hola {{nombreDestinatario}}, su trámite {{codigoSeguimiento}} fue observado con: {{mensaje}}";
        Map<String, Object> variables = new HashMap<>();
        variables.put("nombreDestinatario", "Juan Pérez");
        variables.put("codigoSeguimiento", "EXP-2026-0042");
        variables.put("mensaje", "<script>alert('xss');</script> & falta firma");

        String renderizado = templateEngine.render(plantillaHtml, variables, true);

        assertThat(renderizado).contains("Hola Juan");
        assertThat(renderizado).contains("EXP-2026-0042");
        // Debe haber escapado el script malicioso y los caracteres especiales
        assertThat(renderizado).doesNotContain("<script>");
        assertThat(renderizado).contains("&lt;script&gt;");
        assertThat(renderizado).contains("&amp; falta firma");
    }

    @Test
    @DisplayName("20. Sanitización de enlaces potencialmente peligrosos (javascript: o vbscript:)")
    void testSanitizacionEnlacesPeligrosos() {
        String urlInsegura = "javascript:alert(document.cookie)";
        String sanitizado = templateEngine.sanitizarUrl(urlInsegura);
        assertThat(sanitizado).isEqualTo("#");

        String urlSegura = "https://muniquinua.gob.pe/tramites/123";
        assertThat(templateEngine.sanitizarUrl(urlSegura)).isEqualTo(urlSegura);
    }

    @Test
    @DisplayName("11. Envío de correo exitoso con proveedor simulado MockMailProvider")
    void testEnvioExitosoMockMailProvider() {
        SendResult result = mockMailProvider.send(
                "ciudadano@gmail.com",
                "Juan Pérez",
                "Bienvenida a SIGM-QUINUA",
                "<p>Bienvenido</p>",
                "Bienvenido"
        );

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getMessageId()).isNotBlank();
        assertThat(mockMailProvider.getCorreosEnviados()).hasSize(1);
        assertThat(mockMailProvider.getCorreosEnviados().get(0).getDestinatario()).isEqualTo("ciudadano@gmail.com");
    }

    @Test
    @DisplayName("12. Error y simulación de fallo del proveedor de correo")
    void testErrorSimuladoMockMailProvider() {
        mockMailProvider.setSimularFallo(true);

        SendResult result = mockMailProvider.send(
                "ciudadano@gmail.com",
                "Juan Pérez",
                "Fallo",
                "<p>Error</p>",
                "Error"
        );

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorMessage()).contains("Fallo simulado");
    }
}
