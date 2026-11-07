package pe.gob.municipalidad.sigm.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import pe.gob.municipalidad.sigm.common.constants.ErrorCodes;
import pe.gob.municipalidad.sigm.common.response.ErrorResponse;
import pe.gob.municipalidad.sigm.common.util.DateUtils;
import pe.gob.municipalidad.sigm.common.util.TraceContext;

import java.io.IOException;

/**
 * Manejador para solicitudes denegadas por falta de permisos o roles suficientes (HTTP 403 Forbidden).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        String traceId = TraceContext.getTraceId();
        log.warn("[Trace: {}] Acceso denegado a recurso '{}' para usuario '{}': {}",
                traceId, request.getRequestURI(), request.getRemoteUser(), accessDeniedException.getMessage());

        if (isRestRequest(request)) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");

            ErrorResponse errorResponse = ErrorResponse.builder()
                    .success(false)
                    .message("No cuenta con los privilegios ni permisos requeridos para acceder a esta función municipal.")
                    .errorCode(ErrorCodes.FORBIDDEN_ACCESS)
                    .status(HttpStatus.FORBIDDEN.value())
                    .path(request.getRequestURI())
                    .timestamp(DateUtils.nowOffset())
                    .traceId(traceId)
                    .build();

            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        } else {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType(MediaType.TEXT_HTML_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("""
                    <!DOCTYPE html>
                    <html lang="es">
                    <head>
                        <meta charset="UTF-8">
                        <meta name="viewport" content="width=device-width, initial-scale=1.0">
                        <title>403 - Acceso No Autorizado | SIGM-QUINUA</title>
                        <link rel="stylesheet" href="/css/tailwind.min.css">
                    </head>
                    <body class="bg-[#101827] text-[#e2e8f0] min-h-screen flex items-center justify-center p-6">
                        <div class="max-w-md w-full bg-[#202c3d] border border-slate-700/60 rounded-xl p-8 text-center shadow-2xl">
                            <div class="w-16 h-16 rounded-full bg-rose-600/20 border border-rose-500/30 flex items-center justify-center mx-auto mb-4 text-rose-400">
                                <i data-lucide="shield-alert" class="w-8 h-8"></i>
                            </div>
                            <h1 class="text-3xl font-extrabold text-slate-100">403</h1>
                            <h2 class="text-lg font-bold text-slate-200 mt-2">Acceso No Autorizado</h2>
                            <p class="text-sm text-slate-400 mt-2">
                                No cuenta con los privilegios ni la competencia de unidad orgánica requerida para acceder a este módulo institucional.
                            </p>
                            <div class="mt-6 flex items-center justify-center space-x-3">
                                <a href="/bienvenida" class="inline-flex items-center space-x-2 px-5 py-2.5 bg-blue-600 hover:bg-blue-500 text-white font-medium text-xs rounded-lg transition-colors shadow-lg shadow-blue-900/30">
                                    <i data-lucide="home" class="w-4 h-4"></i>
                                    <span>Ir a Mi Panel Principal</span>
                                </a>
                                <a href="javascript:history.back()" class="inline-flex items-center space-x-2 px-4 py-2.5 bg-slate-800 hover:bg-slate-700 text-slate-300 font-medium text-xs rounded-lg border border-slate-700 transition-colors">
                                    <i data-lucide="arrow-left" class="w-4 h-4"></i>
                                    <span>Regresar</span>
                                </a>
                            </div>
                        </div>
                        <script src="/js/lucide.min.js"></script>
                        <script>if (window.lucide) { lucide.createIcons(); }</script>
                    </body>
                    </html>
                    """);
        }
    }

    private boolean isRestRequest(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        String uri = request.getRequestURI();
        return (accept != null && accept.contains(MediaType.APPLICATION_JSON_VALUE)) || uri.startsWith("/api/");
    }
}
