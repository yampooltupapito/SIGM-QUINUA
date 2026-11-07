package pe.gob.municipalidad.sigm.common.util;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro HTTP de correlación y trazabilidad para SIGM-QUINUA.
 * Extrae o genera un traceId por cada solicitud, lo registra en MDC para los logs,
 * lo adjunta en los encabezados de respuesta y garantiza la limpieza de MDC al finalizar.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdFilter extends OncePerRequestFilter {

    public static final String TRACE_HEADER = TraceContext.TRACE_ID_HEADER;
    public static final String MDC_TRACE_ID_KEY = TraceContext.TRACE_ID_KEY;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String incomingTraceId = request.getHeader(TraceContext.TRACE_ID_HEADER);
        if (incomingTraceId == null || incomingTraceId.trim().isEmpty()) {
            incomingTraceId = request.getHeader(TraceContext.CORRELATION_ID_HEADER);
        }

        String effectiveTraceId = TraceContext.isValid(incomingTraceId)
                ? incomingTraceId.trim()
                : TraceContext.generateNewTraceId();

        TraceContext.setTraceId(effectiveTraceId);
        response.setHeader(TraceContext.TRACE_ID_HEADER, effectiveTraceId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            TraceContext.clear();
        }
    }
}
