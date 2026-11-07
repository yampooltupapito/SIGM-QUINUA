package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.portal.PortalTributarioContextoDto;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoVinculacionContribuyente;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.SeccionPortalTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoVinculacionContribuyente;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Servicio central para el aislamiento de datos y control de contexto del Portal Tributario Ciudadano.
 * Garantiza que ninguna consulta exponga información ajena (Prevención estricta de IDOR).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PortalTributarioContextService {

    public static final String SESSION_CONTRIBUYENTE_ACTIVO_ID = "PORTAL_TRIBUTARIO_ACTIVO_CONTRIBUYENTE_ID";

    private final UsuarioRepository usuarioRepository;
    private final ContribuyenteTributarioRepository contribuyenteRepository;
    private final VinculacionUsuarioContribuyenteRepository vinculacionRepository;
    private final HistorialAccesoPortalTributarioRepository historialAccesoRepository;
    private final RelacionContribuyentePredioRepository relacionPredioRepository;
    private final ObligacionTributariaRepository obligacionRepository;
    private final PagoTributarioRepository pagoRepository;
    private final DocumentoPagoTributarioRepository reciboRepository;
    private final ConvenioFraccionamientoTributarioRepository convenioRepository;
    private final NotificacionTributariaRepository notificacionRepository;
    private final HttpServletRequest httpServletRequest;

    /**
     * Obtiene el usuario autenticado desde el SecurityContext.
     */
    public Usuario obtenerUsuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new AccessDeniedException("Debe iniciar sesión para acceder al Portal Tributario Digital.");
        }
        return usuarioRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new AccessDeniedException("Usuario autenticado no encontrado en el sistema."));
    }

    /**
     * Lista todos los contextos de contribuyentes autorizados (vigentes y aprobados) para el usuario.
     */
    @Transactional(readOnly = true)
    public List<PortalTributarioContextoDto> listarContextosDisponibles() {
        Usuario usuario = obtenerUsuarioAutenticado();
        List<VinculacionUsuarioContribuyente> vinculaciones = vinculacionRepository
                .findByUsuarioIdAndActivoTrue(usuario.getId());

        UUID activoId = obtenerIdContribuyenteActivoEnSesion();

        return vinculaciones.stream()
                .filter(VinculacionUsuarioContribuyente::isVigente)
                .map(v -> {
                    ContribuyenteTributario c = v.getContribuyente();
                    boolean esActivo = activoId != null && activoId.equals(c.getId());
                    return PortalTributarioContextoDto.builder()
                            .vinculacionId(v.getId())
                            .contribuyenteId(c.getId())
                            .codigoContribuyente(c.getCodigoContribuyente())
                            .tipoDocumento(c.getTipoDocumento())
                            .numeroDocumento(c.getNumeroDocumento())
                            .nombreRazonSocial(c.getNombreRazonSocial())
                            .tipoVinculacion(v.getTipoVinculacion())
                            .tipoVinculacionEtiqueta(v.getTipoVinculacion().getNombre())
                            .estado(v.getEstado())
                            .vigenciaHasta(v.getFechaVigenciaHasta())
                            .esActivo(esActivo)
                            .esTitular(v.getTipoVinculacion() == TipoVinculacionContribuyente.TITULAR_VERIFICADO)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Obtiene el contribuyente tributario activo en la sesión actual.
     * Si no se ha seleccionado uno expresamente, resuelve el primero autorizado.
     */
    @Transactional(readOnly = true)
    public Optional<ContribuyenteTributario> obtenerContribuyenteActivo() {
        Usuario usuario = obtenerUsuarioAutenticado();
        List<VinculacionUsuarioContribuyente> aprobadas = vinculacionRepository
                .findByUsuarioIdAndEstadoAndActivoTrue(usuario.getId(), EstadoVinculacionContribuyente.APROBADA)
                .stream()
                .filter(VinculacionUsuarioContribuyente::isVigente)
                .collect(Collectors.toList());

        if (aprobadas.isEmpty()) {
            return Optional.empty();
        }

        UUID activoId = obtenerIdContribuyenteActivoEnSesion();
        if (activoId != null) {
            Optional<VinculacionUsuarioContribuyente> coincidencia = aprobadas.stream()
                    .filter(v -> v.getContribuyente().getId().equals(activoId))
                    .findFirst();
            if (coincidencia.isPresent()) {
                return Optional.of(coincidencia.get().getContribuyente());
            }
        }

        // Si no está en sesión o ya no es válido, establecer el primero
        ContribuyenteTributario defaultContrib = aprobadas.get(0).getContribuyente();
        establecerContribuyenteActivo(defaultContrib.getId());
        return Optional.of(defaultContrib);
    }

    /**
     * Cambia de perfil activo entre los contribuyentes representados legalmente por el ciudadano.
     */
    @Transactional(readOnly = true)
    public void cambiarContexto(UUID contribuyenteId) {
        Usuario usuario = obtenerUsuarioAutenticado();
        VinculacionUsuarioContribuyente vinc = vinculacionRepository
                .findByUsuarioIdAndContribuyenteIdAndEstadoAndActivoTrue(usuario.getId(), contribuyenteId, EstadoVinculacionContribuyente.APROBADA)
                .filter(VinculacionUsuarioContribuyente::isVigente)
                .orElseThrow(() -> new AccessDeniedException("No tiene autorización ni representación acreditada para el contribuyente solicitado."));

        establecerContribuyenteActivo(vinc.getContribuyente().getId());
        log.info("Usuario '{}' cambió perfil tributario activo a contribuyente '{}'",
                usuario.getUsername(), vinc.getContribuyente().getCodigoContribuyente());
    }

    /**
     * Control estricto anti-IDOR para Predio.
     */
    @Transactional(readOnly = true)
    public RelacionContribuyentePredio validarAccesoPredio(UUID predioId) {
        ContribuyenteTributario c = obtenerContribuyenteActivo()
                .orElseThrow(() -> new AccessDeniedException("No cuenta con un contribuyente activo habilitado."));

        return relacionPredioRepository.findByContribuyenteIdAndPredioId(c.getId(), predioId)
                .orElseThrow(() -> new AccessDeniedException("Acceso denegado: el predio no está vinculado a su cuenta tributaria."));
    }

    /**
     * Control estricto anti-IDOR para Obligación Tributaria.
     */
    @Transactional(readOnly = true)
    public ObligacionTributaria validarAccesoObligacion(UUID obligacionId) {
        ContribuyenteTributario c = obtenerContribuyenteActivo()
                .orElseThrow(() -> new AccessDeniedException("No cuenta con un contribuyente activo habilitado."));

        ObligacionTributaria obl = obligacionRepository.findById(obligacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Obligación tributaria no encontrada."));

        if (!obl.getContribuyente().getId().equals(c.getId())) {
            log.warn("ALERTA DE SEGURIDAD: Usuario intentó acceder a obligación ajena {} que pertenece a {}",
                    obligacionId, obl.getContribuyente().getId());
            throw new AccessDeniedException("Acceso denegado: la obligación consultada no corresponde a su perfil activo.");
        }
        return obl;
    }

    /**
     * Control estricto anti-IDOR para Pago Tributario.
     */
    @Transactional(readOnly = true)
    public PagoTributario validarAccesoPago(UUID pagoId) {
        ContribuyenteTributario c = obtenerContribuyenteActivo()
                .orElseThrow(() -> new AccessDeniedException("No cuenta con un contribuyente activo habilitado."));

        PagoTributario pago = pagoRepository.findById(pagoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pago tributario no encontrado."));

        if (!pago.getContribuyente().getId().equals(c.getId())) {
            log.warn("ALERTA DE SEGURIDAD: Usuario intentó acceder a pago ajeno {}", pagoId);
            throw new AccessDeniedException("Acceso denegado: la operación de pago no corresponde a su cuenta.");
        }
        return pago;
    }

    /**
     * Control estricto anti-IDOR para Recibo o Documento de Pago.
     */
    @Transactional(readOnly = true)
    public DocumentoPagoTributario validarAccesoRecibo(UUID reciboId) {
        ContribuyenteTributario c = obtenerContribuyenteActivo()
                .orElseThrow(() -> new AccessDeniedException("No cuenta con un contribuyente activo habilitado."));

        DocumentoPagoTributario doc = reciboRepository.findById(reciboId)
                .orElseThrow(() -> new ResourceNotFoundException("Recibo tributario no encontrado."));

        if (!doc.getContribuyente().getId().equals(c.getId())) {
            log.warn("ALERTA DE SEGURIDAD: Usuario intentó descargar recibo ajeno {}", reciboId);
            throw new AccessDeniedException("Acceso denegado: el comprobante no corresponde a su cuenta.");
        }
        return doc;
    }

    /**
     * Control estricto anti-IDOR para Convenio de Fraccionamiento.
     */
    @Transactional(readOnly = true)
    public ConvenioFraccionamientoTributario validarAccesoConvenio(UUID convenioId) {
        ContribuyenteTributario c = obtenerContribuyenteActivo()
                .orElseThrow(() -> new AccessDeniedException("No cuenta con un contribuyente activo habilitado."));

        ConvenioFraccionamientoTributario conv = convenioRepository.findById(convenioId)
                .orElseThrow(() -> new ResourceNotFoundException("Convenio de fraccionamiento no encontrado."));

        if (!conv.getContribuyente().getId().equals(c.getId())) {
            log.warn("ALERTA DE SEGURIDAD: Intento de acceso no autorizado a convenio ajeno {}", convenioId);
            throw new AccessDeniedException("Acceso denegado: el convenio solicitado no corresponde a su cuenta.");
        }
        return conv;
    }

    /**
     * Control estricto anti-IDOR para Notificación Tributaria.
     */
    @Transactional(readOnly = true)
    public NotificacionTributaria validarAccesoNotificacion(UUID notificacionId) {
        ContribuyenteTributario c = obtenerContribuyenteActivo()
                .orElseThrow(() -> new AccessDeniedException("No cuenta con un contribuyente activo habilitado."));

        NotificacionTributaria notif = notificacionRepository.findById(notificacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Notificación tributaria no encontrada."));

        if (!notif.getContribuyente().getId().equals(c.getId())) {
            log.warn("ALERTA DE SEGURIDAD: Intento de acceso a notificación ajena {}", notificacionId);
            throw new AccessDeniedException("Acceso denegado: la notificación requerida no corresponde a su cuenta.");
        }
        return notif;
    }

    /**
     * Registra auditoría ciudadana del acceso a secciones del portal.
     */
    @Transactional
    public void registrarAcceso(SeccionPortalTributario seccion) {
        try {
            Usuario usuario = obtenerUsuarioAutenticado();
            obtenerContribuyenteActivo().ifPresent(c -> {
                String ip = extraerIpCliente(httpServletRequest);
                String userAgent = httpServletRequest != null ? httpServletRequest.getHeader("User-Agent") : "N/D";
                if (userAgent != null && userAgent.length() > 250) {
                    userAgent = userAgent.substring(0, 250);
                }

                HistorialAccesoPortalTributario historial = HistorialAccesoPortalTributario.builder()
                        .usuario(usuario)
                        .contribuyente(c)
                        .seccionAccedida(seccion.name())
                        .ipOrigen(ip)
                        .userAgent(userAgent)
                        .build();
                historialAccesoRepository.save(historial);
            });
        } catch (Exception e) {
            log.debug("No se pudo registrar auditoría de acceso: {}", e.getMessage());
        }
    }

    private UUID obtenerIdContribuyenteActivoEnSesion() {
        if (httpServletRequest == null) return null;
        HttpSession session = httpServletRequest.getSession(false);
        if (session == null) return null;
        Object val = session.getAttribute(SESSION_CONTRIBUYENTE_ACTIVO_ID);
        return val instanceof UUID ? (UUID) val : null;
    }

    private void establecerContribuyenteActivo(UUID contribuyenteId) {
        if (httpServletRequest == null) return;
        HttpSession session = httpServletRequest.getSession(true);
        if (session != null) {
            session.setAttribute(SESSION_CONTRIBUYENTE_ACTIVO_ID, contribuyenteId);
        }
    }

    private String extraerIpCliente(HttpServletRequest request) {
        if (request == null) return "127.0.0.1";
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        } else {
            int idx = ip.indexOf(',');
            if (idx > 0) {
                ip = ip.substring(0, idx).trim();
            }
        }
        return ip != null ? ip : "127.0.0.1";
    }
}

