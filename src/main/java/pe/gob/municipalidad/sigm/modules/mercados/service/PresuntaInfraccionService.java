package pe.gob.municipalidad.sigm.modules.mercados.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.administracion.service.AuditoriaAdministrativaService;
import pe.gob.municipalidad.sigm.modules.mercados.dto.*;
import pe.gob.municipalidad.sigm.modules.mercados.entity.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoEvaluacionInfraccion;
import pe.gob.municipalidad.sigm.modules.mercados.repository.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.Year;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PresuntaInfraccionService {

    private final PresuntaInfraccionMercadoRepository infraccionRepository;
    private final TipificacionInfraccionRepository tipificacionRepository;
    private final IncidenciaMercadoRepository incidenciaRepository;
    private final InspeccionMercadoRepository inspeccionRepository;
    private final HallazgoInspeccionRepository hallazgoRepository;
    private final MercadoMunicipalRepository mercadoRepository;
    private final SectorMercadoRepository sectorRepository;
    private final PabellonMercadoRepository pabellonRepository;
    private final PuestoComercialRepository puestoRepository;
    private final ComercianteRepository comercianteRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuditoriaAdministrativaService auditoriaService;

    public PresuntaInfraccionService(
            PresuntaInfraccionMercadoRepository infraccionRepository,
            TipificacionInfraccionRepository tipificacionRepository,
            IncidenciaMercadoRepository incidenciaRepository,
            InspeccionMercadoRepository inspeccionRepository,
            HallazgoInspeccionRepository hallazgoRepository,
            MercadoMunicipalRepository mercadoRepository,
            SectorMercadoRepository sectorRepository,
            PabellonMercadoRepository pabellonRepository,
            PuestoComercialRepository puestoRepository,
            ComercianteRepository comercianteRepository,
            UsuarioRepository usuarioRepository,
            AuditoriaAdministrativaService auditoriaService) {
        this.infraccionRepository = infraccionRepository;
        this.tipificacionRepository = tipificacionRepository;
        this.incidenciaRepository = incidenciaRepository;
        this.inspeccionRepository = inspeccionRepository;
        this.hallazgoRepository = hallazgoRepository;
        this.mercadoRepository = mercadoRepository;
        this.sectorRepository = sectorRepository;
        this.pabellonRepository = pabellonRepository;
        this.puestoRepository = puestoRepository;
        this.comercianteRepository = comercianteRepository;
        this.usuarioRepository = usuarioRepository;
        this.auditoriaService = auditoriaService;
    }

    @Transactional
    public PresuntaInfraccionResponse registrar(PresuntaInfraccionCreateRequest request, String username) {
        log.info("Registrando presunta infracción en mercado ID: {} por usuario: {}", request.getMercadoId(), username);

        MercadoMunicipal mercado = mercadoRepository.findById(request.getMercadoId())
                .orElseThrow(() -> new ResourceNotFoundException("Mercado municipal no encontrado: " + request.getMercadoId()));

        SectorMercado sector = null;
        if (request.getSectorId() != null) {
            sector = sectorRepository.findById(request.getSectorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sector no encontrado: " + request.getSectorId()));
            if (!sector.getMercado().getId().equals(mercado.getId())) {
                throw new BusinessException("El sector no pertenece al mercado indicado: " + mercado.getNombre());
            }
        }

        PabellonMercado pabellon = null;
        if (request.getPabellonId() != null) {
            pabellon = pabellonRepository.findById(request.getPabellonId())
                    .orElseThrow(() -> new ResourceNotFoundException("Pabellón no encontrado: " + request.getPabellonId()));
            if (!pabellon.getMercado().getId().equals(mercado.getId())) {
                throw new BusinessException("El pabellón no pertenece al mercado indicado: " + mercado.getNombre());
            }
            if (sector != null && pabellon.getSector() != null && !pabellon.getSector().getId().equals(sector.getId())) {
                throw new BusinessException("El pabellón no pertenece al sector indicado: " + sector.getNombre());
            }
        }

        PuestoComercial puesto = null;
        if (request.getPuestoId() != null) {
            puesto = puestoRepository.findById(request.getPuestoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Puesto no encontrado: " + request.getPuestoId()));
            if (!puesto.getMercado().getId().equals(mercado.getId())) {
                throw new BusinessException("El puesto no pertenece al mercado indicado: " + mercado.getNombre());
            }
            if (sector != null && puesto.getSector() != null && !puesto.getSector().getId().equals(sector.getId())) {
                throw new BusinessException("El puesto no pertenece al sector indicado: " + sector.getNombre());
            }
            if (pabellon != null && puesto.getPabellon() != null && !puesto.getPabellon().getId().equals(pabellon.getId())) {
                throw new BusinessException("El puesto no pertenece al pabellón indicado: " + pabellon.getNombre());
            }
        }

        Comerciante comerciante = null;
        if (request.getComercianteId() != null) {
            comerciante = comercianteRepository.findById(request.getComercianteId())
                    .orElseThrow(() -> new ResourceNotFoundException("Comerciante no encontrado: " + request.getComercianteId()));
        }

        TipificacionInfraccion tipificacion = null;
        if (request.getTipificacionId() != null) {
            tipificacion = tipificacionRepository.findById(request.getTipificacionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Tipificación normativa no encontrada: " + request.getTipificacionId()));
        }

        IncidenciaMercado incidencia = null;
        if (request.getIncidenciaId() != null) {
            incidencia = incidenciaRepository.findById(request.getIncidenciaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Incidencia no encontrada: " + request.getIncidenciaId()));
        }

        InspeccionMercado inspeccion = null;
        if (request.getInspeccionId() != null) {
            inspeccion = inspeccionRepository.findById(request.getInspeccionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Inspección no encontrada: " + request.getInspeccionId()));
        }

        HallazgoInspeccionMercado hallazgo = null;
        if (request.getHallazgoId() != null) {
            hallazgo = hallazgoRepository.findById(request.getHallazgoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Hallazgo no encontrado: " + request.getHallazgoId()));
        }

        Long correlativo = infraccionRepository.obtenerSiguienteCorrelativo();
        int anio = Year.now().getValue();
        String codigo = String.format("INF-MER-%d-%06d", anio, correlativo);

        PresuntaInfraccionMercado entidad = PresuntaInfraccionMercado.builder()
                .codigo(codigo)
                .mercado(mercado)
                .sector(sector)
                .pabellon(pabellon)
                .puesto(puesto)
                .comerciante(comerciante)
                .tipificacion(tipificacion)
                .incidencia(incidencia)
                .inspeccion(inspeccion)
                .hallazgo(hallazgo)
                .descripcionHechos(request.getDescripcionHechos().trim())
                .fechaDeteccion(request.getFechaDeteccion() != null ? request.getFechaDeteccion() : LocalDate.now())
                .estadoEvaluacion(EstadoEvaluacionInfraccion.REGISTRADA)
                .expedienteId(request.getExpedienteId())
                .expedienteNumero(request.getExpedienteNumero())
                .tieneDescargo(false)
                .build();

        PresuntaInfraccionMercado guardada = infraccionRepository.save(entidad);

        auditoriaService.registrar("REGISTRAR_PRESUNTA_INFRACCION", "mercados_presuntas_infracciones", guardada.getId(),
                "EXITO", "Presunta infracción registrada: " + guardada.getCodigo());

        return toResponse(guardada);
    }

    @Transactional
    public PresuntaInfraccionResponse evaluar(UUID id, EvaluacionInfraccionRequest request, String username) {
        PresuntaInfraccionMercado entidad = infraccionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Presunta infracción no encontrada: " + id));

        Usuario evaluador = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario evaluador no encontrado: " + username));

        if (entidad.getEstadoEvaluacion() == EstadoEvaluacionInfraccion.CERRADA) {
            throw new BusinessException("No se puede reevaluar una presunta infracción que ya ha sido cerrada formalmente.");
        }

        entidad.setEstadoEvaluacion(request.getNuevoEstado());
        entidad.setResponsableEvaluacion(evaluador);
        entidad.setInformeEvaluacion(request.getInformeEvaluacion().trim());
        entidad.setFechaEvaluacion(OffsetDateTime.now());

        if (request.getNuevoEstado() == EstadoEvaluacionInfraccion.DESCARTADA) {
            entidad.setMotivoDescarte(request.getMotivoDescarte() != null ? request.getMotivoDescarte().trim() : request.getInformeEvaluacion().trim());
        }

        if (request.getExpedienteId() != null) {
            entidad.setExpedienteId(request.getExpedienteId());
        }
        if (request.getExpedienteNumero() != null) {
            entidad.setExpedienteNumero(request.getExpedienteNumero().trim());
        }

        PresuntaInfraccionMercado guardada = infraccionRepository.save(entidad);

        auditoriaService.registrar("EVALUAR_PRESUNTA_INFRACCION", "mercados_presuntas_infracciones", guardada.getId(),
                "EXITO", "Evaluación registrada en " + guardada.getCodigo() + ". Nuevo estado: " + request.getNuevoEstado());

        return toResponse(guardada);
    }

    @Transactional
    public PresuntaInfraccionResponse registrarDescargo(UUID id, DescargoInfraccionRequest request, String username) {
        PresuntaInfraccionMercado entidad = infraccionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Presunta infracción no encontrada: " + id));

        entidad.setTieneDescargo(true);
        entidad.setFechaDescargo(request.getFechaDescargo() != null ? request.getFechaDescargo() : OffsetDateTime.now());
        entidad.setDocumentoDescargoRef(request.getDocumentoDescargoRef().trim());
        entidad.setResumenDescargo(request.getResumenDescargo().trim());

        PresuntaInfraccionMercado guardada = infraccionRepository.save(entidad);

        auditoriaService.registrar("REGISTRAR_DESCARGO_INFRACCION", "mercados_presuntas_infracciones", guardada.getId(),
                "EXITO", "Descargo registrado en " + guardada.getCodigo() + " con ref: " + request.getDocumentoDescargoRef());

        return toResponse(guardada);
    }

    @Transactional
    public PresuntaInfraccionResponse derivar(UUID id, DerivacionInfraccionRequest request, String username) {
        PresuntaInfraccionMercado entidad = infraccionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Presunta infracción no encontrada: " + id));

        if (entidad.getEstadoEvaluacion() == EstadoEvaluacionInfraccion.CERRADA
                || entidad.getEstadoEvaluacion() == EstadoEvaluacionInfraccion.DESCARTADA) {
            throw new BusinessException("No se puede derivar una presunta infracción que está cerrada o descartada.");
        }

        entidad.setEstadoEvaluacion(EstadoEvaluacionInfraccion.DERIVADA_A_PROCEDIMIENTO);
        if (request.getExpedienteId() != null && !request.getExpedienteId().trim().isEmpty()) {
            entidad.setExpedienteNumero(request.getExpedienteId().trim());
        }

        PresuntaInfraccionMercado guardada = infraccionRepository.save(entidad);

        auditoriaService.registrar("DERIVAR_PRESUNTA_INFRACCION", "mercados_presuntas_infracciones", guardada.getId(),
                "EXITO", "Presunta infracción " + guardada.getCodigo() + " derivada a procedimiento. Motivo: " + request.getMotivo());

        return toResponse(guardada);
    }

    @Transactional
    public PresuntaInfraccionResponse registrarDecision(UUID id, DecisionInfraccionRequest request, String username) {
        PresuntaInfraccionMercado entidad = infraccionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Presunta infracción no encontrada: " + id));

        entidad.setResolucionDecisionRef(request.getResolucionDecisionRef().trim());
        entidad.setFechaDecision(request.getFechaDecision());
        entidad.setSentidoDecision(request.getSentidoDecision());
        entidad.setEstadoEvaluacion(EstadoEvaluacionInfraccion.CERRADA);

        PresuntaInfraccionMercado guardada = infraccionRepository.save(entidad);

        auditoriaService.registrar("REGISTRAR_DECISION_INFRACCION", "mercados_presuntas_infracciones", guardada.getId(),
                "EXITO", "Decisión registrada en " + guardada.getCodigo() + ": " + request.getSentidoDecision() + " ref: " + request.getResolucionDecisionRef());

        return toResponse(guardada);
    }

    @Transactional(readOnly = true)
    public PresuntaInfraccionResponse obtenerPorId(UUID id) {
        PresuntaInfraccionMercado p = infraccionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Presunta infracción no encontrada: " + id));
        return toResponse(p);
    }

    @Transactional(readOnly = true)
    public Page<PresuntaInfraccionResponse> buscarPaginado(
            UUID mercadoId, UUID comercianteId, UUID puestoId, UUID tipificacionId,
            EstadoEvaluacionInfraccion estado, LocalDate fechaDesde, LocalDate fechaHasta,
            String q, Pageable pageable) {

        String qNorm = (q != null && !q.trim().isEmpty()) ? "%" + q.trim().toLowerCase() + "%" : null;
        return infraccionRepository.buscarPaginado(mercadoId, comercianteId, puestoId, tipificacionId,
                estado, fechaDesde, fechaHasta, qNorm, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<PresuntaInfraccionResponse> listarPorIncidencia(UUID incidenciaId) {
        return infraccionRepository.findByIncidenciaId(incidenciaId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PresuntaInfraccionResponse> listarPorInspeccion(UUID inspeccionId) {
        return infraccionRepository.findByInspeccionId(inspeccionId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PresuntaInfraccionResponse> listarPorComerciante(UUID comercianteId) {
        return infraccionRepository.findByComercianteIdOrderByFechaDeteccionDesc(comercianteId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PresuntaInfraccionResponse> listarPorPuesto(UUID puestoId) {
        return infraccionRepository.findByPuestoIdOrderByFechaDeteccionDesc(puestoId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public PresuntaInfraccionResponse toResponse(PresuntaInfraccionMercado p) {
        return PresuntaInfraccionResponse.builder()
                .id(p.getId())
                .codigo(p.getCodigo())
                .incidenciaId(p.getIncidencia() != null ? p.getIncidencia().getId() : null)
                .incidenciaCodigo(p.getIncidencia() != null ? p.getIncidencia().getCodigoIncidencia() : null)
                .inspeccionId(p.getInspeccion() != null ? p.getInspeccion().getId() : null)
                .inspeccionCodigo(p.getInspeccion() != null ? p.getInspeccion().getCodigoInspeccion() : null)
                .hallazgoId(p.getHallazgo() != null ? p.getHallazgo().getId() : null)
                .hallazgoCodigo(p.getHallazgo() != null ? p.getHallazgo().getCodigo() : null)
                .mercadoId(p.getMercado().getId())
                .mercadoNombre(p.getMercado().getNombre())
                .sectorId(p.getSector() != null ? p.getSector().getId() : null)
                .sectorNombre(p.getSector() != null ? p.getSector().getNombre() : null)
                .pabellonId(p.getPabellon() != null ? p.getPabellon().getId() : null)
                .pabellonNombre(p.getPabellon() != null ? p.getPabellon().getNombre() : null)
                .puestoId(p.getPuesto() != null ? p.getPuesto().getId() : null)
                .puestoNumero(p.getPuesto() != null ? p.getPuesto().getNumero() : null)
                .comercianteId(p.getComerciante() != null ? p.getComerciante().getId() : null)
                .comercianteNombre(p.getComerciante() != null && p.getComerciante().getPersona() != null ? p.getComerciante().getPersona().getNombreCompleto() : null)
                .comercianteDocumento(p.getComerciante() != null && p.getComerciante().getPersona() != null ? p.getComerciante().getPersona().getNumeroDocumento() : null)
                .tipificacionId(p.getTipificacion() != null ? p.getTipificacion().getId() : null)
                .tipificacionCodigo(p.getTipificacion() != null ? p.getTipificacion().getCodigo() : null)
                .tipificacionDescripcion(p.getTipificacion() != null ? p.getTipificacion().getDescripcion() : null)
                .normaReferencia(p.getTipificacion() != null ? p.getTipificacion().getNormaReferencia() : null)
                .articulo(p.getTipificacion() != null ? p.getTipificacion().getArticulo() : null)
                .autoridadCompetente(p.getTipificacion() != null ? p.getTipificacion().getAutoridadCompetente() : null)
                .descripcionHechos(p.getDescripcionHechos())
                .fechaDeteccion(p.getFechaDeteccion())
                .estadoEvaluacion(p.getEstadoEvaluacion())
                .responsableEvaluacionId(p.getResponsableEvaluacion() != null ? p.getResponsableEvaluacion().getId() : null)
                .responsableEvaluacionNombre(p.getResponsableEvaluacion() != null ? (p.getResponsableEvaluacion().getPersona() != null ? p.getResponsableEvaluacion().getPersona().getNombreCompleto() : p.getResponsableEvaluacion().getUsername()) : null)
                .informeEvaluacion(p.getInformeEvaluacion())
                .fechaEvaluacion(p.getFechaEvaluacion())
                .motivoDescarte(p.getMotivoDescarte())
                .expedienteId(p.getExpedienteId())
                .expedienteNumero(p.getExpedienteNumero())
                .tieneDescargo(p.getTieneDescargo())
                .fechaDescargo(p.getFechaDescargo())
                .documentoDescargoRef(p.getDocumentoDescargoRef())
                .resumenDescargo(p.getResumenDescargo())
                .resolucionDecisionRef(p.getResolucionDecisionRef())
                .fechaDecision(p.getFechaDecision())
                .sentidoDecision(p.getSentidoDecision())
                .createdAt(p.getCreatedAt())
                .build();
    }
}
