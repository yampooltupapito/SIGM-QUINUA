package pe.gob.municipalidad.sigm.common.document.audit;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import pe.gob.municipalidad.sigm.common.document.entity.AccionAuditoriaDocumento;
import pe.gob.municipalidad.sigm.common.document.entity.Documento;
import pe.gob.municipalidad.sigm.common.document.entity.DocumentoAcceso;
import pe.gob.municipalidad.sigm.common.document.repository.DocumentoAccesoRepository;
import pe.gob.municipalidad.sigm.common.model.Usuario;

/**
 * Servicio de auditoría para trazabilidad de operaciones documentales.
 */
@Service
public class DocumentoAuditService {

    private static final Logger log = LoggerFactory.getLogger(DocumentoAuditService.class);

    private final DocumentoAccesoRepository accesoRepository;

    public DocumentoAuditService(DocumentoAccesoRepository accesoRepository) {
        this.accesoRepository = accesoRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarAcceso(Documento documento, Usuario usuario, AccionAuditoriaDocumento accion, String resultado) {
        try {
            String ip = "127.0.0.1";
            String userAgent = "Desconocido";

            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String xfHeader = request.getHeader("X-Forwarded-For");
                if (xfHeader != null && !xfHeader.isBlank()) {
                    ip = xfHeader.split(",")[0].trim();
                } else {
                    ip = request.getRemoteAddr();
                }
                userAgent = request.getHeader("User-Agent");
                if (userAgent != null && userAgent.length() > 255) {
                    userAgent = userAgent.substring(0, 255);
                }
            }

            DocumentoAcceso acceso = DocumentoAcceso.builder()
                    .documento(documento)
                    .usuario(usuario)
                    .accion(accion)
                    .direccionIp(ip)
                    .userAgent(userAgent)
                    .resultado(resultado != null ? resultado : "EXITOSO")
                    .build();

            accesoRepository.save(acceso);
            log.debug("Auditoría documental registrada: accion={}, docId={}, usuario={}", accion, documento.getId(), usuario != null ? usuario.getUsername() : "ANONIMO");
        } catch (Exception e) {
            log.warn("No se pudo registrar la traza de auditoría documental: {}", e.getMessage());
        }
    }
}
