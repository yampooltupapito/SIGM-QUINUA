package pe.gob.municipalidad.sigm.common.util;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TraceContextAndFilterTest {

    @Test
    @DisplayName("TraceContext debe generar identificador cuando no existe")
    void traceContextGeneratesIdWhenEmpty() {
        TraceContext.clear();
        String traceId = TraceContext.getTraceId();

        assertNotNull(traceId);
        assertFalse(traceId.isEmpty());
        TraceContext.clear();
    }

    @Test
    @DisplayName("TraceContext debe validar UUIDs y caracteres alfanuméricos")
    void traceContextValidation() {
        assertTrue(TraceContext.isValidTraceId("abc-123_456"));
        assertTrue(TraceContext.isValidTraceId("12345678-1234-1234-1234-123456789abc"));
        assertFalse(TraceContext.isValidTraceId("invalid trace with spaces"));
        assertFalse(TraceContext.isValidTraceId("<script>alert(1)</script>"));
    }

    @Test
    @DisplayName("TraceIdFilter debe respetar cabecera X-Trace-Id válida y limpiar MDC al finalizar")
    void traceIdFilterUsesIncomingHeaderAndCleansMdc() throws ServletException, IOException {
        TraceIdFilter filter = new TraceIdFilter();
        MockHttpServletRequest request = new MockHttpServletRequest();
        String clientTraceId = "client-trace-987";
        request.addHeader(TraceIdFilter.TRACE_HEADER, clientTraceId);
        MockHttpServletResponse response = new MockHttpServletResponse();

        final String[] capturedInChain = new String[1];

        FilterChain chain = (req, res) -> {
            capturedInChain[0] = MDC.get(TraceIdFilter.MDC_TRACE_ID_KEY);
            assertEquals(clientTraceId, capturedInChain[0]);
        };

        filter.doFilter(request, response, chain);

        // Verifica que la respuesta tenga el encabezado
        assertEquals(clientTraceId, response.getHeader(TraceIdFilter.TRACE_HEADER));
        // Verifica que el MDC fue limpiado en el finally para evitar contaminación de hilos
        assertNull(MDC.get(TraceIdFilter.MDC_TRACE_ID_KEY));
    }

    @Test
    @DisplayName("TraceIdFilter debe generar nuevo traceId si la cabecera es inválida o inexistente")
    void traceIdFilterGeneratesNewIdIfMissingOrInvalid() throws ServletException, IOException {
        TraceIdFilter filter = new TraceIdFilter();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(TraceIdFilter.TRACE_HEADER, "invalid trace header with spaces!");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> {
            String insideMdc = MDC.get(TraceIdFilter.MDC_TRACE_ID_KEY);
            assertNotNull(insideMdc);
            assertNotEquals("invalid trace header with spaces!", insideMdc);
        });

        assertNull(MDC.get(TraceIdFilter.MDC_TRACE_ID_KEY));
    }
}
