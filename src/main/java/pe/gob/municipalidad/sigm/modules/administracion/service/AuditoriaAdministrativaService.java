package pe.gob.municipalidad.sigm.modules.administracion.service;

import jakarta.persistence.criteria.Predicate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import pe.gob.municipalidad.sigm.modules.administracion.dto.AuditoriaDtos.AuditoriaAdministrativaResponse;
import pe.gob.municipalidad.sigm.modules.administracion.entity.AuditoriaAdministrativa;
import pe.gob.municipalidad.sigm.modules.administracion.repository.AuditoriaAdministrativaRepository;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Servicio encargado de la persistencia aislada de pistas de auditoría administrativa.
 * Ejecuta los registros en transacciones independientes (REQUIRES_NEW) para no interferir
 * con rollbacks del negocio y garantizar la trazabilidad permanente.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditoriaAdministrativaService {

    private final AuditoriaAdministrativaRepository auditoriaRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(String accion, String entidadAfectada, Object entidadId, String resultado, String detalle) {
        try {
            Long idLong = null;
            String idStr = null;
            if (entidadId instanceof Number) {
                idLong = ((Number) entidadId).longValue();
            } else if (entidadId != null) {
                idStr = entidadId.toString();
                try {
                    idLong = Long.parseLong(idStr);
                } catch (NumberFormatException ignored) {}
            }
            String fullDetalle = (idLong == null && idStr != null)
                    ? "[ID: " + idStr + "] " + (detalle != null ? detalle : "")
                    : detalle;

            String usuario = "SISTEMA";
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
                usuario = auth.getName();
            }

            String ipOrigen = null;
            String userAgent = null;
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                ipOrigen = request.getHeader("X-Forwarded-For");
                if (ipOrigen == null || ipOrigen.isBlank()) {
                    ipOrigen = request.getRemoteAddr();
                } else if (ipOrigen.contains(",")) {
                    ipOrigen = ipOrigen.split(",")[0].trim();
                }
                userAgent = request.getHeader("User-Agent");
                if (userAgent != null && userAgent.length() > 255) {
                    userAgent = userAgent.substring(0, 255);
                }
            }

            String traceId = MDC.get("traceId");
            if (traceId == null || traceId.isBlank()) {
                traceId = UUID.randomUUID().toString();
            }

            AuditoriaAdministrativa logAdmin = AuditoriaAdministrativa.builder()
                    .usuarioResponsable(usuario)
                    .accion(accion)
                    .entidadAfectada(entidadAfectada)
                    .entidadId(idLong)
                    .resultado(resultado != null ? resultado : "EXITO")
                    .detalle(fullDetalle)
                    .ipOrigen(ipOrigen)
                    .userAgent(userAgent)
                    .traceId(traceId)
                    .createdAt(OffsetDateTime.now())
                    .build();

            auditoriaRepository.save(logAdmin);
        } catch (Exception e) {
            log.error("Fallo al registrar auditoría administrativa para entidad {}: {}", entidadAfectada, e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public Page<AuditoriaAdministrativaResponse> buscarAuditoria(String usuario, String entidad, String accion, Pageable pageable) {
        Specification<AuditoriaAdministrativa> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (usuario != null && !usuario.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("usuarioResponsable")), "%" + usuario.trim().toLowerCase() + "%"));
            }
            if (entidad != null && !entidad.isBlank()) {
                predicates.add(cb.equal(root.get("entidadAfectada"), entidad.trim()));
            }
            if (accion != null && !accion.isBlank()) {
                predicates.add(cb.equal(root.get("accion"), accion.trim()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return auditoriaRepository.findAll(spec, pageable).map(this::toResponse);
    }

    private AuditoriaAdministrativaResponse toResponse(AuditoriaAdministrativa a) {
        return AuditoriaAdministrativaResponse.builder()
                .id(a.getId())
                .usuarioResponsable(a.getUsuarioResponsable())
                .accion(a.getAccion())
                .entidadAfectada(a.getEntidadAfectada())
                .entidadId(a.getEntidadId())
                .resultado(a.getResultado())
                .detalle(a.getDetalle())
                .ipOrigen(a.getIpOrigen())
                .userAgent(a.getUserAgent())
                .traceId(a.getTraceId())
                .createdAt(a.getCreatedAt())
                .build();
    }
}
