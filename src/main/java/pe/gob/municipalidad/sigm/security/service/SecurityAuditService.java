package pe.gob.municipalidad.sigm.security.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.util.TraceContext;
import pe.gob.municipalidad.sigm.security.model.AuditoriaSeguridad;
import pe.gob.municipalidad.sigm.security.repository.AuditoriaSeguridadRepository;

import java.time.OffsetDateTime;

/**
 * Servicio de auditoría de seguridad para trazabilidad de eventos de autenticación y autorización.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SecurityAuditService {

    private final AuditoriaSeguridadRepository auditoriaSeguridadRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordEvent(String usuario, String tipoEvento, String resultado, String descripcion, HttpServletRequest request) {
        try {
            String ip = "0.0.0.0";
            String userAgent = "Unknown";

            if (request != null) {
                String xf = request.getHeader("X-Forwarded-For");
                ip = (xf != null && !xf.isEmpty()) ? xf.split(",")[0].trim() : request.getRemoteAddr();
                String ua = request.getHeader("User-Agent");
                userAgent = (ua != null && ua.length() > 250) ? ua.substring(0, 250) : (ua != null ? ua : "Unknown");
            }

            AuditoriaSeguridad evento = AuditoriaSeguridad.builder()
                    .usuario(usuario)
                    .tipoEvento(tipoEvento)
                    .resultado(resultado)
                    .descripcion(descripcion)
                    .ipOrigen(ip)
                    .userAgent(userAgent)
                    .traceId(TraceContext.getTraceId())
                    .createdAt(OffsetDateTime.now())
                    .build();

            auditoriaSeguridadRepository.save(evento);
            log.info("[Seguridad] Evento registrado: {} | Usuario: {} | Resultado: {} | IP: {} | Trace: {}",
                    tipoEvento, usuario, resultado, ip, TraceContext.getTraceId());
        } catch (Exception e) {
            log.error("Error al registrar auditoría de seguridad: {}", e.getMessage());
        }
    }
}
