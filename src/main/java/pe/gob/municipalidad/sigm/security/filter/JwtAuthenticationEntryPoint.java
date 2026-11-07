package pe.gob.municipalidad.sigm.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import pe.gob.municipalidad.sigm.common.constants.ErrorCodes;
import pe.gob.municipalidad.sigm.common.response.ErrorResponse;
import pe.gob.municipalidad.sigm.common.util.DateUtils;
import pe.gob.municipalidad.sigm.common.util.TraceContext;

import java.io.IOException;

/**
 * Manejador para solicitudes que fallan por falta de autenticación (HTTP 401 Unauthorized).
 * Emite respuestas JSON estructuradas con ErrorResponse y traceId sin exponer información técnica interna.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        String traceId = TraceContext.getTraceId();
        log.warn("[Trace: {}] Acceso no autenticado a '{}': {}", traceId, request.getRequestURI(), authException.getMessage());

        if (isRestRequest(request)) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");

            ErrorResponse errorResponse = ErrorResponse.builder()
                    .success(false)
                    .message("Debe autenticarse con credenciales válidas para acceder a este recurso municipal.")
                    .errorCode(ErrorCodes.UNAUTHORIZED_ACCESS)
                    .status(HttpStatus.UNAUTHORIZED.value())
                    .path(request.getRequestURI())
                    .timestamp(DateUtils.nowOffset())
                    .traceId(traceId)
                    .build();

            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        } else {
            response.sendRedirect("/login");
        }
    }

    private boolean isRestRequest(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        String uri = request.getRequestURI();
        return (accept != null && accept.contains(MediaType.APPLICATION_JSON_VALUE)) || uri.startsWith("/api/");
    }
}
