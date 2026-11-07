package pe.gob.municipalidad.sigm.modules.portal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.modules.portal.dto.PortalContactoDtos;
import pe.gob.municipalidad.sigm.modules.portal.entity.PortalContacto;
import pe.gob.municipalidad.sigm.modules.portal.repository.PortalContactoRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PortalContactoService {

    private final PortalContactoRepository contactoRepository;

    @Transactional
    public PortalContactoDtos.ContactoResponse registrarContactoCiudadano(
            PortalContactoDtos.ContactoPublicRequest request,
            String ipOrigen) {

        // 1. Verificación Honeypot (campo trampa contra bots)
        if (request.getWebsite() != null && !request.getWebsite().trim().isEmpty()) {
            log.warn("Detección de bot antispam en formulario de contacto desde IP: {}", ipOrigen);
            throw new BusinessException("No se pudo procesar la solicitud debido a validación de seguridad antispam.");
        }

        // 2. Control de Tasa de Envío (Rate Limiting: Máximo 5 consultas por hora por correo o IP)
        LocalDateTime unaHoraAtras = LocalDateTime.now().minusHours(1);
        long porCorreo = contactoRepository.countByRemitenteCorreoAndCreatedAtAfter(request.getRemitenteCorreo().trim().toLowerCase(), unaHoraAtras);
        if (porCorreo >= 5) {
            log.warn("Límite de solicitudes superado para correo: {}", request.getRemitenteCorreo());
            throw new BusinessException("Ha superado el límite de mensajes permitidos por hora. Por favor espere unos momentos antes de enviar otra consulta.");
        }

        if (ipOrigen != null && !ipOrigen.isEmpty()) {
            long porIp = contactoRepository.countByIpOrigenAndCreatedAtAfter(ipOrigen, unaHoraAtras);
            if (porIp >= 15) {
                log.warn("Límite de solicitudes superado para IP: {}", ipOrigen);
                throw new BusinessException("Se ha detectado un volumen inusualmente alto de solicitudes desde su red. Inténtelo más tarde.");
            }
        }

        // 3. Generar código de seguimiento institucional único: CNS-YYYY-XXXXX
        String codigo = generarCodigoContacto();

        PortalContacto contacto = PortalContacto.builder()
                .codigo(codigo)
                .remitenteNombre(request.getRemitenteNombre().trim())
                .remitenteCorreo(request.getRemitenteCorreo().trim().toLowerCase())
                .remitenteTelefono(request.getRemitenteTelefono() != null ? request.getRemitenteTelefono().trim() : null)
                .asunto(request.getAsunto().trim())
                .mensaje(request.getMensaje().trim())
                .estado("PENDIENTE")
                .ipOrigen(ipOrigen)
                .build();

        PortalContacto guardado = contactoRepository.save(contacto);
        log.info("Nuevo mensaje ciudadano registrado con código: '{}' desde IP: {}", guardado.getCodigo(), ipOrigen);

        return toResponse(guardado);
    }

    @Transactional(readOnly = true)
    public Page<PortalContactoDtos.ContactoResponse> listarContactosAdmin(String estado, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, Math.min(50, size)));
        if (estado != null && !estado.trim().isEmpty() && !"TODOS".equalsIgnoreCase(estado)) {
            return contactoRepository.findByEstadoOrderByCreatedAtDesc(estado.trim().toUpperCase(), pageable)
                    .map(this::toResponse);
        }
        return contactoRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public PortalContactoDtos.ContactoResponse obtenerDetalleContacto(Long id) {
        PortalContacto contacto = contactoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consulta ciudadana no encontrada con ID: " + id));
        return toResponse(contacto);
    }

    @Transactional
    public PortalContactoDtos.ContactoResponse atenderContacto(Long id, PortalContactoDtos.ContactoAtencionRequest request, String usuarioResponsable) {
        PortalContacto contacto = contactoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consulta ciudadana no encontrada con ID: " + id));

        contacto.setRespuestaInterna(request.getRespuestaInterna().trim());
        contacto.setEstado(request.getEstado() != null ? request.getEstado().toUpperCase() : "ATENDIDO");
        contacto.setAtendidoPor(usuarioResponsable);
        contacto.setAtendidoAt(LocalDateTime.now());

        PortalContacto actualizado = contactoRepository.save(contacto);
        log.info("Consulta ciudadana '{}' atendida por: {}", actualizado.getCodigo(), usuarioResponsable);
        return toResponse(actualizado);
    }

    @Transactional(readOnly = true)
    public long contarPendientes() {
        return contactoRepository.countByEstado("PENDIENTE");
    }

    private synchronized String generarCodigoContacto() {
        int anio = LocalDate.now().getYear();
        long total = contactoRepository.count() + 1;
        String codigo = String.format("CNS-%d-%05d", anio, total);
        while (contactoRepository.findByCodigo(codigo).isPresent()) {
            total++;
            codigo = String.format("CNS-%d-%05d", anio, total);
        }
        return codigo;
    }

    private PortalContactoDtos.ContactoResponse toResponse(PortalContacto c) {
        return PortalContactoDtos.ContactoResponse.builder()
                .id(c.getId())
                .codigo(c.getCodigo())
                .remitenteNombre(c.getRemitenteNombre())
                .remitenteCorreo(c.getRemitenteCorreo())
                .remitenteTelefono(c.getRemitenteTelefono())
                .asunto(c.getAsunto())
                .mensaje(c.getMensaje())
                .estado(c.getEstado())
                .respuestaInterna(c.getRespuestaInterna())
                .atendidoPor(c.getAtendidoPor())
                .atendidoAt(c.getAtendidoAt())
                .createdAt(c.getCreatedAt())
                .build();
    }
}
