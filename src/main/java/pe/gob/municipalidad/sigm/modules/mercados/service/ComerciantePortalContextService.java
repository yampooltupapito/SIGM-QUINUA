package pe.gob.municipalidad.sigm.modules.mercados.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.mercados.dto.PortalComercianteContextoResponse;
import pe.gob.municipalidad.sigm.modules.mercados.entity.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoVinculoComerciante;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoVinculoComerciante;
import pe.gob.municipalidad.sigm.modules.mercados.repository.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Servicio encargado de resolver y validar de forma estricta la identidad del comerciante
 * en el backend, sin confiar en identificadores manipulables enviados desde el cliente.
 */
@Slf4j
@Service
public class ComerciantePortalContextService {

    public static final String SESSION_COMERCIANTE_ID = "PORTAL_ACTIVO_COMERCIANTE_ID";

    private final UsuarioRepository usuarioRepository;
    private final ComercianteRepository comercianteRepository;
    private final VinculoUsuarioComercianteRepository vinculoRepository;
    private final AsignacionPuestoRepository asignacionRepository;
    private final ContratoOcupacionRepository contratoRepository;
    private final AutorizacionComercialRepository autorizacionRepository;
    private final ObligacionMercadoRepository obligacionRepository;
    private final PagoMercadoRepository pagoRepository;
    private final SolicitudGestionOcupacionRepository solicitudRepository;
    private final InspeccionMercadoRepository inspeccionRepository;
    private final IncidenciaMercadoRepository incidenciaRepository;
    private final HttpServletRequest httpServletRequest;

    public ComerciantePortalContextService(
            UsuarioRepository usuarioRepository,
            ComercianteRepository comercianteRepository,
            VinculoUsuarioComercianteRepository vinculoRepository,
            AsignacionPuestoRepository asignacionRepository,
            ContratoOcupacionRepository contratoRepository,
            AutorizacionComercialRepository autorizacionRepository,
            ObligacionMercadoRepository obligacionRepository,
            PagoMercadoRepository pagoRepository,
            SolicitudGestionOcupacionRepository solicitudRepository,
            InspeccionMercadoRepository inspeccionRepository,
            IncidenciaMercadoRepository incidenciaRepository,
            HttpServletRequest httpServletRequest) {
        this.usuarioRepository = usuarioRepository;
        this.comercianteRepository = comercianteRepository;
        this.vinculoRepository = vinculoRepository;
        this.asignacionRepository = asignacionRepository;
        this.contratoRepository = contratoRepository;
        this.autorizacionRepository = autorizacionRepository;
        this.obligacionRepository = obligacionRepository;
        this.pagoRepository = pagoRepository;
        this.solicitudRepository = solicitudRepository;
        this.inspeccionRepository = inspeccionRepository;
        this.incidenciaRepository = incidenciaRepository;
        this.httpServletRequest = httpServletRequest;
    }

    /**
     * Obtiene el Usuario autenticado en el contexto de seguridad actual.
     */
    @Transactional(readOnly = true)
    public Usuario getUsuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new AccessDeniedException("Debe iniciar sesión para acceder al portal de comerciantes.");
        }
        return usuarioRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new AccessDeniedException("Usuario autenticado no encontrado en el sistema."));
    }

    /**
     * Resuelve el Comerciante activo autorizado para el usuario autenticado.
     */
    @Transactional
    public Comerciante getComercianteActivo() {
        Usuario usuario = getUsuarioAutenticado();
        List<VinculoUsuarioComerciante> vinculos = obtenerVinculosValidos(usuario);

        if (vinculos.isEmpty()) {
            throw new AccessDeniedException("No cuenta con un registro comercial verificado vinculado a su cuenta ciudadana.");
        }

        UUID comercianteIdSeleccionado = resolverComercianteIdSeleccionado();
        if (comercianteIdSeleccionado != null) {
            for (VinculoUsuarioComerciante v : vinculos) {
                if (v.getComerciante().getId().equals(comercianteIdSeleccionado)) {
                    return v.getComerciante();
                }
            }
        }

        // Por defecto el primer vínculo válido
        return vinculos.get(0).getComerciante();
    }

    /**
     * Obtiene todos los vínculos vigentes y verificados para el usuario.
     * Si no tiene registro en la tabla de vínculos pero tiene relación directa de titularidad,
     * auto-aprovisiona el vínculo como TITULAR VERIFICADO.
     */
    @Transactional
    public List<VinculoUsuarioComerciante> obtenerVinculosValidos(Usuario usuario) {
        List<VinculoUsuarioComerciante> vinculos = vinculoRepository.findByUsuarioIdAndEstado(
                usuario.getId(), EstadoVinculoComerciante.VERIFICADO);

        LocalDate hoy = LocalDate.now();
        List<VinculoUsuarioComerciante> vigentes = vinculos.stream()
                .filter(v -> (v.getFechaInicio() == null || !hoy.isBefore(v.getFechaInicio()))
                        && (v.getFechaFin() == null || !hoy.isAfter(v.getFechaFin())))
                .collect(Collectors.toList());

        if (vigentes.isEmpty()) {
            // Comprobar si existe comerciante titular directo asociado al usuario o su persona
            Optional<Comerciante> titularDirecto = comercianteRepository.findByUsuarioId(usuario.getId());
            if (titularDirecto.isEmpty() && usuario.getPersona() != null) {
                titularDirecto = comercianteRepository.findByPersonaId(usuario.getPersona().getId());
            }

            if (titularDirecto.isPresent()) {
                Comerciante c = titularDirecto.get();
                VinculoUsuarioComerciante nuevoVinculo = VinculoUsuarioComerciante.builder()
                        .usuario(usuario)
                        .comerciante(c)
                        .tipoVinculo(TipoVinculoComerciante.TITULAR)
                        .estado(EstadoVinculoComerciante.VERIFICADO)
                        .fechaInicio(LocalDate.now())
                        .fechaVerificacion(OffsetDateTime.now())
                        .verificadoPor(usuario)
                        .facultadesRepresentacion("Titularidad originaria del puesto en padrón municipal")
                        .build();
                nuevoVinculo = vinculoRepository.save(nuevoVinculo);
                vigentes = List.of(nuevoVinculo);
            }
        }

        return vigentes;
    }

    /**
     * Obtiene el listado de contextos comerciales disponibles para el usuario (si representa varios comercios).
     */
    @Transactional(readOnly = true)
    public List<PortalComercianteContextoResponse> listarContextosDisponibles() {
        Usuario usuario = getUsuarioAutenticado();
        List<VinculoUsuarioComerciante> vinculos = vinculoRepository.findByUsuarioIdAndEstado(
                usuario.getId(), EstadoVinculoComerciante.VERIFICADO);

        UUID activoId = resolverComercianteIdSeleccionado();
        if (activoId == null && !vinculos.isEmpty()) {
            activoId = vinculos.get(0).getComerciante().getId();
        }

        UUID finalActivoId = activoId;
        return vinculos.stream().map(v -> {
            Comerciante c = v.getComerciante();
            return PortalComercianteContextoResponse.builder()
                    .comercianteId(c.getId())
                    .codigoComerciante(c.getCodigoComerciante())
                    .nombreComercialOTitular(c.getNombreOTitular())
                    .tipoComerciante(c.getTipoComerciante())
                    .tipoVinculo(v.getTipoVinculo())
                    .activo(c.getId().equals(finalActivoId))
                    .build();
        }).collect(Collectors.toList());
    }

    /**
     * Permite al usuario cambiar voluntariamente el contexto comercial activo cuando posee múltiples representaciones.
     */
    @Transactional(readOnly = true)
    public void cambiarContextoActivo(UUID comercianteId) {
        Usuario usuario = getUsuarioAutenticado();
        boolean permitido = vinculoRepository.findByUsuarioIdAndEstado(usuario.getId(), EstadoVinculoComerciante.VERIFICADO)
                .stream().anyMatch(v -> v.getComerciante().getId().equals(comercianteId));

        if (!permitido) {
            throw new AccessDeniedException("No tiene autorización para operar en representación del comercio seleccionado.");
        }

        try {
            HttpSession session = httpServletRequest.getSession(true);
            session.setAttribute(SESSION_COMERCIANTE_ID, comercianteId);
        } catch (Exception e) {
            log.debug("No se pudo persistir contexto en sesión HTTP: {}", e.getMessage());
        }
    }

    private UUID resolverComercianteIdSeleccionado() {
        try {
            // Primero intentar de cabecera HTTP
            String headerId = httpServletRequest.getHeader("X-Comerciante-Id");
            if (headerId != null && !headerId.isBlank()) {
                return UUID.fromString(headerId.trim());
            }

            // Segundo intentar de sesión
            HttpSession session = httpServletRequest.getSession(false);
            if (session != null) {
                Object attr = session.getAttribute(SESSION_COMERCIANTE_ID);
                if (attr instanceof UUID) return (UUID) attr;
                if (attr instanceof String) return UUID.fromString((String) attr);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    // =========================================================================
    // VALIDACIONES DE PERTENENCIA ESTRICTA (PREVENCIÓN DE IDOR)
    // =========================================================================

    public void validarPertenenciaPuesto(UUID puestoId, Comerciante comerciante) {
        boolean pertenece = asignacionRepository.findByComercianteIdOrderByFechaInicioDesc(comerciante.getId())
                .stream().anyMatch(a -> a.getPuesto().getId().equals(puestoId));
        if (!pertenece) {
            throw new AccessDeniedException("El puesto solicitado no pertenece al comerciante en sesión.");
        }
    }

    public ContratoOcupacion validarPertenenciaContrato(UUID contratoId, Comerciante comerciante) {
        ContratoOcupacion contrato = contratoRepository.findById(contratoId)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato no encontrado: " + contratoId));
        if (!contrato.getComerciante().getId().equals(comerciante.getId())) {
            throw new AccessDeniedException("El contrato solicitado no corresponde al comerciante en sesión.");
        }
        return contrato;
    }

    public AutorizacionComercial validarPertenenciaAutorizacion(UUID autorizacionId, Comerciante comerciante) {
        AutorizacionComercial autorizacion = autorizacionRepository.findById(autorizacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Autorización no encontrada: " + autorizacionId));
        if (!autorizacion.getComerciante().getId().equals(comerciante.getId())) {
            throw new AccessDeniedException("La autorización solicitada no corresponde al comerciante en sesión.");
        }
        return autorizacion;
    }

    public ObligacionMercado validarPertenenciaObligacion(UUID obligacionId, Comerciante comerciante) {
        ObligacionMercado obligacion = obligacionRepository.findById(obligacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Obligación no encontrada: " + obligacionId));
        if (!obligacion.getComerciante().getId().equals(comerciante.getId())) {
            throw new AccessDeniedException("La obligación solicitada no corresponde al comerciante en sesión.");
        }
        return obligacion;
    }

    public PagoMercado validarPertenenciaPago(UUID pagoId, Comerciante comerciante) {
        PagoMercado pago = pagoRepository.findById(pagoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado: " + pagoId));
        if (!pago.getComerciante().getId().equals(comerciante.getId())) {
            throw new AccessDeniedException("El comprobante de pago no corresponde al comerciante en sesión.");
        }
        return pago;
    }

    public SolicitudGestionOcupacion validarPertenenciaSolicitud(UUID solicitudId, Comerciante comerciante) {
        SolicitudGestionOcupacion sol = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada: " + solicitudId));
        boolean esTitular = sol.getComercianteActual().getId().equals(comerciante.getId());
        boolean esPropuesto = sol.getComerciantePropuesto() != null && sol.getComerciantePropuesto().getId().equals(comerciante.getId());
        if (!esTitular && !esPropuesto) {
            throw new AccessDeniedException("El expediente solicitado no corresponde al comerciante en sesión.");
        }
        return sol;
    }

    public IncidenciaMercado validarPertenenciaIncidencia(UUID incidenciaId, Comerciante comerciante) {
        IncidenciaMercado inc = incidenciaRepository.findById(incidenciaId)
                .orElseThrow(() -> new ResourceNotFoundException("Incidencia no encontrada: " + incidenciaId));
        if (inc.getComerciante() == null || !inc.getComerciante().getId().equals(comerciante.getId())) {
            throw new AccessDeniedException("La incidencia solicitada no corresponde al comerciante en sesión.");
        }
        return inc;
    }

    public InspeccionMercado validarPertenenciaInspeccion(UUID inspeccionId, Comerciante comerciante) {
        InspeccionMercado insp = inspeccionRepository.findById(inspeccionId)
                .orElseThrow(() -> new ResourceNotFoundException("Inspección no encontrada: " + inspeccionId));
        if (insp.getComerciante() == null || !insp.getComerciante().getId().equals(comerciante.getId())) {
            throw new AccessDeniedException("La inspección solicitada no corresponde al comerciante en sesión.");
        }
        return insp;
    }
}
