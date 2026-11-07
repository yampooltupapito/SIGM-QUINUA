package pe.gob.municipalidad.sigm.modules.mercados.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.document.entity.Documento;
import pe.gob.municipalidad.sigm.common.document.repository.DocumentoRepository;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.modules.administracion.service.AuditoriaAdministrativaService;
import pe.gob.municipalidad.sigm.modules.mercados.dto.*;
import pe.gob.municipalidad.sigm.modules.mercados.entity.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoObservacionGestion;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoSolicitudGestion;
import pe.gob.municipalidad.sigm.modules.mercados.enums.ResultadoSubsanacionGestion;
import pe.gob.municipalidad.sigm.modules.mercados.enums.SentidoEvaluacionGestion;
import pe.gob.municipalidad.sigm.modules.mercados.repository.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EvaluacionGestionOcupacionService {

    private final SolicitudGestionOcupacionRepository solicitudRepository;
    private final ObservacionGestionOcupacionRepository observacionRepository;
    private final SubsanacionGestionOcupacionRepository subsanacionRepository;
    private final EvaluacionGestionOcupacionRepository evaluacionRepository;
    private final DocumentoRepository documentoRepository;
    private final AuditoriaAdministrativaService auditoriaService;
    private final RenovacionOcupacionService renovacionService;
    private final HistorialTitularidadService historialTitularidadService;

    public EvaluacionGestionOcupacionService(
            SolicitudGestionOcupacionRepository solicitudRepository,
            ObservacionGestionOcupacionRepository observacionRepository,
            SubsanacionGestionOcupacionRepository subsanacionRepository,
            EvaluacionGestionOcupacionRepository evaluacionRepository,
            DocumentoRepository documentoRepository,
            AuditoriaAdministrativaService auditoriaService,
            RenovacionOcupacionService renovacionService,
            HistorialTitularidadService historialTitularidadService) {
        this.solicitudRepository = solicitudRepository;
        this.observacionRepository = observacionRepository;
        this.subsanacionRepository = subsanacionRepository;
        this.evaluacionRepository = evaluacionRepository;
        this.documentoRepository = documentoRepository;
        this.auditoriaService = auditoriaService;
        this.renovacionService = renovacionService;
        this.historialTitularidadService = historialTitularidadService;
    }

    @Transactional
    public SolicitudGestionOcupacionResponse presentarSolicitud(UUID solicitudId, String username) {
        SolicitudGestionOcupacion s = obtenerSolicitudEntidad(solicitudId);
        if (s.getEstado() != EstadoSolicitudGestion.BORRADOR) {
            throw new BusinessException("Solo las solicitudes en estado BORRADOR pueden ser presentadas.");
        }
        s.setEstado(EstadoSolicitudGestion.PRESENTADA);
        SolicitudGestionOcupacion guardada = solicitudRepository.save(s);
        auditoriaService.registrar("PRESENTAR_SOLICITUD_GESTION", "mercados_solicitudes_gestion", s.getId(), "EXITO",
                "Solicitud " + s.getCodigoSolicitud() + " presentada formalmente");
        return renovacionService.toResponse(guardada);
    }

    @Transactional
    public ObservacionGestionResponse registrarObservacion(UUID solicitudId, ObservacionGestionRequest request, String username) {
        SolicitudGestionOcupacion s = obtenerSolicitudEntidad(solicitudId);
        if (s.getEstado() == EstadoSolicitudGestion.FORMALIZADA ||
            s.getEstado() == EstadoSolicitudGestion.FINALIZADA ||
            s.getEstado() == EstadoSolicitudGestion.RECHAZADA ||
            s.getEstado() == EstadoSolicitudGestion.ANULADA) {
            throw new BusinessException("No procede registrar observaciones sobre una solicitud en estado: " + s.getEstado());
        }

        Documento doc = null;
        if (request.getDocumentoId() != null) {
            doc = documentoRepository.findById(request.getDocumentoId()).orElse(null);
        }

        ObservacionGestionOcupacion obs = ObservacionGestionOcupacion.builder()
                .solicitud(s)
                .descripcion(request.getDescripcion())
                .requisitoRelacionado(request.getRequisitoRelacionado())
                .fechaObservacion(OffsetDateTime.now())
                .usuarioObservador(username)
                .estado(EstadoObservacionGestion.PENDIENTE)
                .documentoSustento(doc)
                .build();

        ObservacionGestionOcupacion guardada = observacionRepository.save(obs);

        s.setEstado(EstadoSolicitudGestion.OBSERVADA);
        solicitudRepository.save(s);

        auditoriaService.registrar("OBSERVAR_SOLICITUD_GESTION", "mercados_solicitudes_observaciones", guardada.getId(),
                "EXITO", "Observación registrada para solicitud " + s.getCodigoSolicitud() + ": " + request.getDescripcion());

        return toObservacionResponse(guardada);
    }

    @Transactional
    public SubsanacionGestionResponse registrarSubsanacion(UUID solicitudId, SubsanacionGestionRequest request, String username) {
        SolicitudGestionOcupacion s = obtenerSolicitudEntidad(solicitudId);
        ObservacionGestionOcupacion obs = observacionRepository.findById(request.getObservacionId())
                .orElseThrow(() -> new ResourceNotFoundException("Observación no encontrada: " + request.getObservacionId()));

        if (!obs.getSolicitud().getId().equals(s.getId())) {
            throw new BusinessException("La observación no pertenece a la solicitud indicada.");
        }

        Documento doc = null;
        if (request.getDocumentoId() != null) {
            doc = documentoRepository.findById(request.getDocumentoId()).orElse(null);
        }

        SubsanacionGestionOcupacion sub = SubsanacionGestionOcupacion.builder()
                .observacion(obs)
                .solicitud(s)
                .descripcion(request.getDescripcion())
                .fechaSubsanacion(OffsetDateTime.now())
                .usuarioSubsanador(username)
                .documento(doc)
                .resultadoRevision(ResultadoSubsanacionGestion.PENDIENTE)
                .build();

        SubsanacionGestionOcupacion guardada = subsanacionRepository.save(sub);

        obs.setEstado(EstadoObservacionGestion.SUBSANADA);
        observacionRepository.save(obs);

        // Si ya no quedan observaciones pendientes, actualizar a EN_EVALUACION
        long pendientes = observacionRepository.countBySolicitudIdAndEstado(s.getId(), EstadoObservacionGestion.PENDIENTE);
        if (pendientes == 0) {
            s.setEstado(EstadoSolicitudGestion.EN_EVALUACION);
            solicitudRepository.save(s);
        }

        auditoriaService.registrar("SUBSANAR_SOLICITUD_GESTION", "mercados_solicitudes_subsanaciones", guardada.getId(),
                "EXITO", "Subsanación registrada para observación ID " + obs.getId() + " en solicitud " + s.getCodigoSolicitud());

        return toSubsanacionResponse(guardada);
    }

    @Transactional
    public SolicitudGestionOcupacionResponse evaluarSolicitud(UUID solicitudId, EvaluacionGestionRequest request, String username) {
        SolicitudGestionOcupacion s = obtenerSolicitudEntidad(solicitudId);
        if (s.getEstado() == EstadoSolicitudGestion.FORMALIZADA ||
            s.getEstado() == EstadoSolicitudGestion.FINALIZADA ||
            s.getEstado() == EstadoSolicitudGestion.RECHAZADA) {
            throw new BusinessException("La solicitud ya se encuentra concluida en estado: " + s.getEstado());
        }

        Documento doc = null;
        if (request.getDocumentoSustentoId() != null) {
            doc = documentoRepository.findById(request.getDocumentoSustentoId()).orElse(null);
        }

        EvaluacionGestionOcupacion eval = EvaluacionGestionOcupacion.builder()
                .solicitud(s)
                .fechaEvaluacion(OffsetDateTime.now())
                .usuarioEvaluador(username)
                .sentidoEvaluacion(request.getSentidoEvaluacion())
                .informeTecnico(request.getInformeTecnico())
                .observaciones(request.getObservaciones())
                .documentoSustento(doc)
                .build();

        evaluacionRepository.save(eval);

        s.setUsuarioEvaluacion(username);
        s.setFechaEvaluacion(OffsetDateTime.now());
        s.setInformeEvaluacion(request.getInformeTecnico());

        if (request.getSentidoEvaluacion() == SentidoEvaluacionGestion.OBSERVADA) {
            s.setEstado(EstadoSolicitudGestion.OBSERVADA);
        } else {
            s.setEstado(EstadoSolicitudGestion.EN_EVALUACION);
        }

        SolicitudGestionOcupacion guardada = solicitudRepository.save(s);

        auditoriaService.registrar("EVALUAR_SOLICITUD_GESTION", "mercados_solicitudes_gestion", s.getId(), "EXITO",
                "Evaluación registrada con sentido " + request.getSentidoEvaluacion() + " para solicitud " + s.getCodigoSolicitud());

        return renovacionService.toResponse(guardada);
    }

    @Transactional
    public SolicitudGestionOcupacionResponse aprobarSolicitud(UUID solicitudId, AprobacionGestionRequest request, String username) {
        SolicitudGestionOcupacion s = obtenerSolicitudEntidad(solicitudId);
        if (s.getEstado() == EstadoSolicitudGestion.FORMALIZADA || s.getEstado() == EstadoSolicitudGestion.FINALIZADA) {
            throw new BusinessException("La solicitud ya ha sido formalizada previamente.");
        }
        if (s.getEstado() == EstadoSolicitudGestion.OBSERVADA || s.getEstado() == EstadoSolicitudGestion.PENDIENTE_SUBSANACION) {
            throw new BusinessException("No procede aprobar una solicitud con observaciones pendientes de subsanar.");
        }

        Documento doc = null;
        if (request.getDocumentoDecisionId() != null) {
            doc = documentoRepository.findById(request.getDocumentoDecisionId()).orElse(null);
        }

        s.setEstado(EstadoSolicitudGestion.APROBADA);
        s.setUsuarioDecision(username);
        s.setFechaDecision(OffsetDateTime.now());
        s.setMotivoDecision(request.getMotivo());
        s.setDocumentoDecision(doc);

        SolicitudGestionOcupacion guardada = solicitudRepository.save(s);

        auditoriaService.registrar("APROBAR_SOLICITUD_GESTION", "mercados_solicitudes_gestion", s.getId(), "EXITO",
                "Solicitud de ocupación " + s.getCodigoSolicitud() + " APROBADA (Pendiente de formalización)");

        return renovacionService.toResponse(guardada);
    }

    @Transactional
    public SolicitudGestionOcupacionResponse rechazarSolicitud(UUID solicitudId, RechazoGestionRequest request, String username) {
        SolicitudGestionOcupacion s = obtenerSolicitudEntidad(solicitudId);
        if (s.getEstado() == EstadoSolicitudGestion.FORMALIZADA || s.getEstado() == EstadoSolicitudGestion.FINALIZADA) {
            throw new BusinessException("No procede rechazar una solicitud que ya fue formalizada.");
        }

        Documento doc = null;
        if (request.getDocumentoDecisionId() != null) {
            doc = documentoRepository.findById(request.getDocumentoDecisionId()).orElse(null);
        }

        s.setEstado(EstadoSolicitudGestion.RECHAZADA);
        s.setUsuarioDecision(username);
        s.setFechaDecision(OffsetDateTime.now());
        s.setMotivoDecision(request.getMotivo());
        s.setDocumentoDecision(doc);

        SolicitudGestionOcupacion guardada = solicitudRepository.save(s);

        auditoriaService.registrar("RECHAZAR_SOLICITUD_GESTION", "mercados_solicitudes_gestion", s.getId(), "EXITO",
                "Solicitud de ocupación " + s.getCodigoSolicitud() + " RECHAZADA. Motivo: " + request.getMotivo());

        return renovacionService.toResponse(guardada);
    }

    @Transactional(readOnly = true)
    public SolicitudGestionOcupacionDetailResponse obtenerDetalle(UUID solicitudId) {
        SolicitudGestionOcupacion s = obtenerSolicitudEntidad(solicitudId);
        SolicitudGestionOcupacionResponse base = renovacionService.toResponse(s);

        List<ObservacionGestionResponse> obsList = observacionRepository.findBySolicitudIdOrderByFechaObservacionAsc(s.getId()).stream()
                .map(this::toObservacionResponse)
                .collect(Collectors.toList());

        List<EvaluacionGestionResponse> evalList = evaluacionRepository.findBySolicitudIdOrderByFechaEvaluacionDesc(s.getId()).stream()
                .map(this::toEvaluacionResponse)
                .collect(Collectors.toList());

        List<HistorialTitularidadResponse> histList = historialTitularidadService.listarPorPuesto(s.getPuesto().getId());

        SolicitudGestionOcupacionDetailResponse detail = new SolicitudGestionOcupacionDetailResponse();
        // Copiar propiedades de base
        detail.setId(base.getId());
        detail.setCodigoSolicitud(base.getCodigoSolicitud());
        detail.setTipoSolicitud(base.getTipoSolicitud());
        detail.setTipoSolicitudLabel(base.getTipoSolicitudLabel());
        detail.setProcedimientoId(base.getProcedimientoId());
        detail.setProcedimientoNombre(base.getProcedimientoNombre());
        detail.setMercadoId(base.getMercadoId());
        detail.setMercadoNombre(base.getMercadoNombre());
        detail.setPuestoId(base.getPuestoId());
        detail.setPuestoNumero(base.getPuestoNumero());
        detail.setAsignacionOrigenId(base.getAsignacionOrigenId());
        detail.setAsignacionOrigenCodigo(base.getAsignacionOrigenCodigo());
        detail.setContratoOrigenId(base.getContratoOrigenId());
        detail.setContratoOrigenCodigo(base.getContratoOrigenCodigo());
        detail.setAutorizacionOrigenId(base.getAutorizacionOrigenId());
        detail.setAutorizacionOrigenCodigo(base.getAutorizacionOrigenCodigo());
        detail.setComercianteActualId(base.getComercianteActualId());
        detail.setComercianteActualNombre(base.getComercianteActualNombre());
        detail.setComercianteActualDocumento(base.getComercianteActualDocumento());
        detail.setComerciantePropuestoId(base.getComerciantePropuestoId());
        detail.setComerciantePropuestoNombre(base.getComerciantePropuestoNombre());
        detail.setComerciantePropuestoDocumento(base.getComerciantePropuestoDocumento());
        detail.setFechaPresentacion(base.getFechaPresentacion());
        detail.setFechaInicioSolicitada(base.getFechaInicioSolicitada());
        detail.setFechaFinSolicitada(base.getFechaFinSolicitada());
        detail.setMotivo(base.getMotivo());
        detail.setEstado(base.getEstado());
        detail.setEstadoLabel(base.getEstadoLabel());
        detail.setEstadoBadgeClass(base.getEstadoBadgeClass());
        detail.setExpedienteId(base.getExpedienteId());
        detail.setOficinaResponsableId(base.getOficinaResponsableId());
        detail.setOficinaResponsableNombre(base.getOficinaResponsableNombre());
        detail.setUsuarioRegistro(base.getUsuarioRegistro());
        detail.setUsuarioEvaluacion(base.getUsuarioEvaluacion());
        detail.setFechaEvaluacion(base.getFechaEvaluacion());
        detail.setInformeEvaluacion(base.getInformeEvaluacion());
        detail.setUsuarioDecision(base.getUsuarioDecision());
        detail.setFechaDecision(base.getFechaDecision());
        detail.setMotivoDecision(base.getMotivoDecision());
        detail.setUsuarioFormalizacion(base.getUsuarioFormalizacion());
        detail.setFechaFormalizacion(base.getFechaFormalizacion());
        detail.setAsignacionNuevaId(base.getAsignacionNuevaId());
        detail.setAsignacionNuevaCodigo(base.getAsignacionNuevaCodigo());
        detail.setContratoNuevoId(base.getContratoNuevoId());
        detail.setAutorizacionNuevaId(base.getAutorizacionNuevaId());
        detail.setTokenVerificacionQr(base.getTokenVerificacionQr());
        detail.setCreatedAt(base.getCreatedAt());
        detail.setUpdatedAt(base.getUpdatedAt());

        detail.setObservaciones(obsList);
        detail.setEvaluaciones(evalList);
        detail.setHistorialPuesto(histList);

        return detail;
    }

    private SolicitudGestionOcupacion obtenerSolicitudEntidad(UUID id) {
        return solicitudRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud de gestión de ocupación no encontrada: " + id));
    }

    private ObservacionGestionResponse toObservacionResponse(ObservacionGestionOcupacion o) {
        List<SubsanacionGestionResponse> subs = subsanacionRepository.findByObservacionIdOrderByFechaSubsanacionAsc(o.getId()).stream()
                .map(this::toSubsanacionResponse)
                .collect(Collectors.toList());

        return ObservacionGestionResponse.builder()
                .id(o.getId())
                .descripcion(o.getDescripcion())
                .requisitoRelacionado(o.getRequisitoRelacionado())
                .fechaObservacion(o.getFechaObservacion())
                .usuarioObservador(o.getUsuarioObservador())
                .estado(o.getEstado())
                .estadoLabel(o.getEstado().getLabel())
                .documentoId(o.getDocumentoSustento() != null ? o.getDocumentoSustento().getId() : null)
                .subsanaciones(subs)
                .build();
    }

    private SubsanacionGestionResponse toSubsanacionResponse(SubsanacionGestionOcupacion s) {
        return SubsanacionGestionResponse.builder()
                .id(s.getId())
                .observacionId(s.getObservacion().getId())
                .descripcion(s.getDescripcion())
                .fechaSubsanacion(s.getFechaSubsanacion())
                .usuarioSubsanador(s.getUsuarioSubsanador())
                .documentoId(s.getDocumento() != null ? s.getDocumento().getId() : null)
                .resultadoRevision(s.getResultadoRevision())
                .resultadoRevisionLabel(s.getResultadoRevision().getLabel())
                .build();
    }

    private EvaluacionGestionResponse toEvaluacionResponse(EvaluacionGestionOcupacion e) {
        return EvaluacionGestionResponse.builder()
                .id(e.getId())
                .fechaEvaluacion(e.getFechaEvaluacion())
                .usuarioEvaluador(e.getUsuarioEvaluador())
                .sentidoEvaluacion(e.getSentidoEvaluacion())
                .sentidoEvaluacionLabel(e.getSentidoEvaluacion().getLabel())
                .informeTecnico(e.getInformeTecnico())
                .observaciones(e.getObservaciones())
                .documentoSustentoId(e.getDocumentoSustento() != null ? e.getDocumentoSustento().getId() : null)
                .build();
    }
}
