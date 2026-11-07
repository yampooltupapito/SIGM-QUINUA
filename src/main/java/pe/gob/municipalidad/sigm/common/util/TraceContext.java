package pe.gob.municipalidad.sigm.common.util;

import org.slf4j.MDC;

import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Contexto de trazabilidad para seguimiento y correlación de solicitudes HTTP.
 * Utiliza SLF4J MDC (Mapped Diagnostic Context) asegurando el aislamiento por hilo.
 */
public final class TraceContext {

    public static final String TRACE_ID_KEY = "traceId";
    public static final String TRACE_ID_HEADER = "X-Trace-Id";
    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    private static final Pattern VALID_TRACE_ID_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]{8,64}$");

    private TraceContext() {
        // Utility class
    }

    public static String getTraceId() {
        String traceId = MDC.get(TRACE_ID_KEY);
        if (traceId == null || traceId.trim().isEmpty()) {
            return generateNewTraceId();
        }
        return traceId;
    }

    public static void setTraceId(String traceId) {
        if (isValid(traceId)) {
            MDC.put(TRACE_ID_KEY, traceId.trim());
        } else {
            MDC.put(TRACE_ID_KEY, generateNewTraceId());
        }
    }

    public static String generateNewTraceId() {
        return UUID.randomUUID().toString();
    }

    public static boolean isValid(String traceId) {
        return traceId != null && VALID_TRACE_ID_PATTERN.matcher(traceId.trim()).matches();
    }

    public static boolean isValidTraceId(String traceId) {
        return isValid(traceId);
    }

    public static void clear() {
        MDC.remove(TRACE_ID_KEY);
    }
}
