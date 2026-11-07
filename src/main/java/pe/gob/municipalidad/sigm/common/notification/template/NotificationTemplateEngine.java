package pe.gob.municipalidad.sigm.common.notification.template;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Motor ligero de renderizado y sanitización de plantillas de correo y notificaciones.
 * Sustituye variables {{clave}} aplicando escape estricto de HTML para prevenir ataques XSS.
 */
@Slf4j
@Component
public class NotificationTemplateEngine {

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{(\\w+)\\}\\}");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Renderiza un texto o HTML sustituyendo variables con escape seguro de HTML.
     */
    public String render(String template, Map<String, Object> variables, boolean escapeHtml) {
        if (template == null || template.isBlank()) {
            return "";
        }

        Map<String, Object> mergedVars = new HashMap<>();
        mergedVars.put("nombreMunicipalidad", "Municipalidad Distrital de Quinua");
        mergedVars.put("fecha", OffsetDateTime.now().format(DATE_FORMATTER));
        mergedVars.put("nombreDestinatario", "Ciudadano(a)");
        mergedVars.put("enlaceAccion", "/login");
        mergedVars.put("codigoSeguimiento", "-");

        if (variables != null) {
            mergedVars.putAll(variables);
        }

        Matcher matcher = VARIABLE_PATTERN.matcher(template);
        StringBuilder sb = new StringBuilder();

        while (matcher.find()) {
            String varName = matcher.group(1);
            Object value = mergedVars.get(varName);
            String replacement = value != null ? value.toString() : "";

            if (escapeHtml && !"enlaceAccion".equals(varName)) {
                // Escapar caracteres peligrosos para HTML
                replacement = HtmlUtils.htmlEscape(replacement);
            } else if ("enlaceAccion".equals(varName)) {
                // Sanitizar URLs para evitar javascript: o data:
                replacement = sanitizarUrl(replacement);
            }

            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    public String sanitizarUrl(String url) {
        if (url == null || url.isBlank()) return "#";
        String trimmed = url.trim();
        String lower = trimmed.toLowerCase();
        if (lower.startsWith("javascript:") || lower.startsWith("data:") || lower.startsWith("vbscript:")) {
            log.warn("[XSS PREVENTED] Intento de inyección de URL maliciosa: {}", url);
            return "#";
        }
        return HtmlUtils.htmlEscape(trimmed);
    }
}
