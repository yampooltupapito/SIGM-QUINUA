package pe.gob.municipalidad.sigm.modules.mercados.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.modules.administracion.service.AuditoriaAdministrativaService;
import pe.gob.municipalidad.sigm.common.document.entity.Documento;
import pe.gob.municipalidad.sigm.common.document.repository.DocumentoRepository;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.modules.mercados.dto.*;
import pe.gob.municipalidad.sigm.modules.mercados.entity.AsignacionEvaluacion;
import pe.gob.municipalidad.sigm.modules.mercados.entity.AsignacionPuesto;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoAsignacion;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoMovimientoOcupacion;
import pe.gob.municipalidad.sigm.modules.mercados.repository.AsignacionEvaluacionRepository;
import pe.gob.municipalidad.sigm.modules.mercados.repository.AsignacionPuestoRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EvaluacionAsignacionService {

    private final AsignacionPuestoRepository asignacionRepository;
    private final AsignacionEvaluacionRepository evaluacionRepository;
    private final DocumentoRepository documentoRepository;
    private final HistorialOcupacionService historialService;
    private final AuditoriaAdministrativaService auditoriaService;

    public EvaluacionAsignacionService(
            AsignacionPuestoRepository asignacionRepository,
            AsignacionEvaluacionRepository evaluacionRepository,
            DocumentoRepository documentoRepository,
            HistorialOcupacionService historialService,
            AuditoriaAdministrativaService auditoriaService) {
        this.asignacionRepository = asignacionRepository;
        this.evaluacionRepository = evaluacionRepository;
        this.documentoRepository = documentoRepository;
        this.historialService = historialService;
        this.auditoriaService = auditoriaService;
    }

    @Transactional
    public AsignacionDetailResponse evaluarSolicitud(UUID asignacionId, AsignacionEvaluacionRequest request) {
        AsignacionPuesto asignacion = asignacionRepository.findByIdCompleto(asignacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Asignación no encontrada con ID: " + asignacionId));

        if (!asignacion.getEstado().permiteEvaluacion()) {
            throw new BusinessException("La asignación no se encuentra en un estado que permita evaluación técnica. Estado actual: " + asignacion.getEstado());
        }

        String usuario = obtenerUsuarioAutenticado();
        Documento doc = null;
        if (request.getDocumentoSustentoId() != null) {
            doc = documentoRepository.findById(request.getDocumentoSustentoId()).orElse(null);
        }

        EstadoAsignacion estadoAnterior = asignacion.getEstado();
        asignacion.setEstado(EstadoAsignacion.EN_EVALUACION);
        asignacion.setUsuarioEvaluacion(usuario);
        asignacion.setFechaEvaluacion(OffsetDateTime.now());
        asignacion.setObservacionesEvaluacion(request.getMotivo());

        AsignacionEvaluacion evaluacion = AsignacionEvaluacion.builder()
                .asignacion(asignacion)
                .tipoEvaluacion("EVALUACION_TECNICA")
                .resultado(request.getResultado())
                .motivo(request.getMotivo())
                .documentoSustento(doc)
                .usuarioEvaluador(usuario)
                .fechaEvaluacion(OffsetDateTime.now())
                .build();
        evaluacionRepository.save(evaluacion);

        asignacionRepository.save(asignacion);

        historialService.registrarMovimiento(
                asignacion.getPuesto(),
                asignacion.getComerciante(),
                asignacion,
                TipoMovimientoOcupacion.EVALUACION,
                "Evaluación técnica: " + request.getResultado() + " - " + request.getMotivo(),
                doc,
                estadoAnterior.name(),
                EstadoAsignacion.EN_EVALUACION.name(),
                request.getObservaciones()
        );

        auditoriaService.registrar("EVALUAR_ASIGNACION_PUESTO", "mercados_asignaciones", asignacion.getId(), "EXITO",
                "Evaluación registrada con resultado: " + request.getResultado());

        return toDetailResponse(asignacion);
    }

    @Transactional
    public AsignacionDetailResponse formularObservacion(UUID asignacionId, AsignacionObservacionRequest request) {
        AsignacionPuesto asignacion = asignacionRepository.findByIdCompleto(asignacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Asignación no encontrada con ID: " + asignacionId));

        if (!asignacion.getEstado().permiteEvaluacion()) {
            throw new BusinessException("No se pueden formular observaciones a una solicitud en estado: " + asignacion.getEstado());
        }

        String usuario = obtenerUsuarioAutenticado();
        Documento doc = null;
        if (request.getDocumentoSustentoId() != null) {
            doc = documentoRepository.findById(request.getDocumentoSustentoId()).orElse(null);
        }

        EstadoAsignacion estadoAnterior = asignacion.getEstado();
        asignacion.setEstado(EstadoAsignacion.OBSERVADA);
        asignacion.setObservacionesEvaluacion(request.getDetalleObservacion());
        asignacionRepository.save(asignacion);

        AsignacionEvaluacion observacion = AsignacionEvaluacion.builder()
                .asignacion(asignacion)
                .tipoEvaluacion("OBSERVACION_ADMINISTRATIVA")
                .resultado("OBSERVADO")
                .motivo(request.getDetalleObservacion())
                .documentoSustento(doc)
                .usuarioEvaluador(usuario)
                .fechaEvaluacion(OffsetDateTime.now())
                .build();
        evaluacionRepository.save(observacion);

        historialService.registrarMovimiento(
                asignacion.getPuesto(),
                asignacion.getComerciante(),
                asignacion,
                TipoMovimientoOcupacion.CAMBIO_ESTADO,
                "Observación formulada: " + request.getDetalleObservacion(),
                doc,
                estadoAnterior.name(),
                EstadoAsignacion.OBSERVADA.name(),
                null
        );

        auditoriaService.registrar("OBSERVAR_ASIGNACION_PUESTO", "mercados_asignaciones", asignacion.getId(), "EXITO",
                "Observación administrativa formulada para la solicitud " + asignacion.getCodigoAsignacion());

        return toDetailResponse(asignacion);
    }

    @Transactional
    public AsignacionDetailResponse subsanarObservacion(UUID asignacionId, String motivoSubsanacion, Long documentoSustentoId) {
        AsignacionPuesto asignacion = asignacionRepository.findByIdCompleto(asignacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Asignación no encontrada con ID: " + asignacionId));

        if (asignacion.getEstado() != EstadoAsignacion.OBSERVADA) {
            throw new BusinessException("Solo se pueden subsanar solicitudes que se encuentren en estado OBSERVADA.");
        }

        String usuario = obtenerUsuarioAutenticado();
        Documento doc = null;
        if (documentoSustentoId != null) {
            doc = documentoRepository.findById(documentoSustentoId).orElse(null);
        }

        asignacion.setEstado(EstadoAsignacion.EN_EVALUACION);
        asignacionRepository.save(asignacion);

        AsignacionEvaluacion subsanacion = AsignacionEvaluacion.builder()
                .asignacion(asignacion)
                .tipoEvaluacion("SUBSANACION")
                .resultado("EN_REVISION")
                .motivo(motivoSubsanacion != null ? motivoSubsanacion : "Subsanación de observaciones presentada por el administrado")
                .documentoSustento(doc)
                .usuarioEvaluador(usuario)
                .fechaEvaluacion(OffsetDateTime.now())
                .build();
        evaluacionRepository.save(subsanacion);

        historialService.registrarMovimiento(
                asignacion.getPuesto(),
                asignacion.getComerciante(),
                asignacion,
                TipoMovimientoOcupacion.CAMBIO_ESTADO,
                "Subsanación presentada: " + motivoSubsanacion,
                doc,
                EstadoAsignacion.OBSERVADA.name(),
                EstadoAsignacion.EN_EVALUACION.name(),
                null
        );

        auditoriaService.registrar("SUBSANAR_ASIGNACION_PUESTO", "mercados_asignaciones", asignacion.getId(), "EXITO",
                "Subsanación registrada para la solicitud " + asignacion.getCodigoAsignacion());

        return toDetailResponse(asignacion);
    }

    @Transactional
    public AsignacionDetailResponse aprobarSolicitud(UUID asignacionId, AsignacionAprobacionRequest request) {
        AsignacionPuesto asignacion = asignacionRepository.findByIdCompleto(asignacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Asignación no encontrada con ID: " + asignacionId));

        if (!asignacion.getEstado().permiteAprobacion()) {
            throw new BusinessException("La solicitud no se encuentra en estado EN_EVALUACION para poder ser aprobada. Estado actual: " + asignacion.getEstado());
        }

        String usuario = obtenerUsuarioAutenticado();
        Documento doc = null;
        if (request.getDocumentoResolucionId() != null) {
            doc = documentoRepository.findById(request.getDocumentoResolucionId()).orElse(null);
            asignacion.setDocumentoSustento(doc);
        }

        if (request.getFechaInicioVigencia() != null) {
            asignacion.setFechaInicio(request.getFechaInicioVigencia());
        }
        if (request.getFechaFinVigencia() != null) {
            asignacion.setFechaFin(request.getFechaFinVigencia());
        }

        EstadoAsignacion estadoAnterior = asignacion.getEstado();
        asignacion.setEstado(EstadoAsignacion.APROBADA);
        asignacion.setUsuarioAprobacion(usuario);
        asignacion.setFechaAprobacion(OffsetDateTime.now());
        asignacion.setMotivo(request.getSustentoAprobacion());
        asignacionRepository.save(asignacion);

        AsignacionEvaluacion aprobacion = AsignacionEvaluacion.builder()
                .asignacion(asignacion)
                .tipoEvaluacion("APROBACION_ADMINISTRATIVA")
                .resultado("APROBADO")
                .motivo(request.getSustentoAprobacion())
                .documentoSustento(doc)
                .usuarioEvaluador(usuario)
                .fechaEvaluacion(OffsetDateTime.now())
                .build();
        evaluacionRepository.save(aprobacion);

        historialService.registrarMovimiento(
                asignacion.getPuesto(),
                asignacion.getComerciante(),
                asignacion,
                TipoMovimientoOcupacion.APROBACION,
                "Aprobación formal de asignación: " + request.getSustentoAprobacion(),
                doc,
                estadoAnterior.name(),
                EstadoAsignacion.APROBADA.name(),
                null
        );

        auditoriaService.registrar("APROBAR_ASIGNACION_PUESTO", "mercados_asignaciones", asignacion.getId(), "EXITO",
                "Aprobada solicitud de asignación " + asignacion.getCodigoAsignacion() + " por " + usuario);

        return toDetailResponse(asignacion);
    }

    @Transactional
    public AsignacionDetailResponse rechazarSolicitud(UUID asignacionId, AsignacionRechazoRequest request) {
        AsignacionPuesto asignacion = asignacionRepository.findByIdCompleto(asignacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Asignación no encontrada con ID: " + asignacionId));

        if (!asignacion.getEstado().permiteEvaluacion()) {
            throw new BusinessException("No se puede rechazar una asignación en estado: " + asignacion.getEstado());
        }

        String usuario = obtenerUsuarioAutenticado();
        Documento doc = null;
        if (request.getDocumentoSustentoId() != null) {
            doc = documentoRepository.findById(request.getDocumentoSustentoId()).orElse(null);
        }

        EstadoAsignacion estadoAnterior = asignacion.getEstado();
        asignacion.setEstado(EstadoAsignacion.RECHAZADA);
        asignacion.setMotivoRechazo(request.getMotivoRechazo());
        asignacionRepository.save(asignacion);

        AsignacionEvaluacion rechazo = AsignacionEvaluacion.builder()
                .asignacion(asignacion)
                .tipoEvaluacion("RECHAZO_ADMINISTRATIVO")
                .resultado("DENEGADO")
                .motivo(request.getMotivoRechazo())
                .documentoSustento(doc)
                .usuarioEvaluador(usuario)
                .fechaEvaluacion(OffsetDateTime.now())
                .build();
        evaluacionRepository.save(rechazo);

        historialService.registrarMovimiento(
                asignacion.getPuesto(),
                asignacion.getComerciante(),
                asignacion,
                TipoMovimientoOcupacion.RECHAZO,
                "Rechazo de solicitud de asignación: " + request.getMotivoRechazo(),
                doc,
                estadoAnterior.name(),
                EstadoAsignacion.RECHAZADA.name(),
                null
        );

        auditoriaService.registrar("RECHAZAR_ASIGNACION_PUESTO", "mercados_asignaciones", asignacion.getId(), "EXITO",
                "Rechazada solicitud de asignación " + asignacion.getCodigoAsignacion() + " - Motivo: " + request.getMotivoRechazo());

        return toDetailResponse(asignacion);
    }

    private String obtenerUsuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            return auth.getName();
        }
        return "SISTEMA";
    }

    private AsignacionDetailResponse toDetailResponse(AsignacionPuesto a) {
        String titular = "-";
        String doc = "-";
        if (a.getComerciante() != null) {
            if (a.getComerciante().getPersona() != null) {
                titular = a.getComerciante().getPersona().getNombreCompleto();
                doc = a.getComerciante().getPersona().getNumeroDocumento();
            } else if (a.getComerciante().getPersonaJuridica() != null) {
                titular = a.getComerciante().getPersonaJuridica().getRazonSocial();
                doc = a.getComerciante().getPersonaJuridica().getRuc();
            }
        }

        AsignacionDetailResponse detail = AsignacionDetailResponse.builder()
                .id(a.getId())
                .codigoAsignacion(a.getCodigoAsignacion())
                .puestoId(a.getPuesto() != null ? a.getPuesto().getId() : null)
                .puestoCodigo(a.getPuesto() != null ? a.getPuesto().getCodigo() : null)
                .puestoNumero(a.getPuesto() != null ? a.getPuesto().getNumero() : null)
                .puestoEstado(a.getPuesto() != null ? a.getPuesto().getEstado().name() : null)
                .puestoArea(a.getPuesto() != null ? a.getPuesto().getAreaMetrosCuadrados() : null)
                .tipoPuestoNombre(a.getPuesto() != null && a.getPuesto().getTipoPuesto() != null ? a.getPuesto().getTipoPuesto().getNombre() : null)
                .mercadoId(a.getPuesto() != null && a.getPuesto().getMercado() != null ? a.getPuesto().getMercado().getId() : null)
                .mercadoNombre(a.getPuesto() != null && a.getPuesto().getMercado() != null ? a.getPuesto().getMercado().getNombre() : null)
                .mercadoCodigo(a.getPuesto() != null && a.getPuesto().getMercado() != null ? a.getPuesto().getMercado().getCodigo() : null)
                .sectorNombre(a.getPuesto() != null && a.getPuesto().getSector() != null ? a.getPuesto().getSector().getNombre() : null)
                .pabellonNombre(a.getPuesto() != null && a.getPuesto().getPabellon() != null ? a.getPuesto().getPabellon().getNombre() : null)
                .comercianteId(a.getComerciante() != null ? a.getComerciante().getId() : null)
                .comercianteCodigo(a.getComerciante() != null ? a.getComerciante().getCodigoComerciante() : null)
                .comercianteTipo(a.getComerciante() != null && a.getComerciante().getTipoComerciante() != null ? a.getComerciante().getTipoComerciante().name() : null)
                .titularNombre(titular)
                .titularDocumento(doc)
                .comercianteTelefono(a.getComerciante() != null ? a.getComerciante().getTelefonoContacto() : null)
                .comercianteCorreo(a.getComerciante() != null ? a.getComerciante().getCorreoContacto() : null)
                .comercianteDireccion(a.getComerciante() != null ? a.getComerciante().getDireccionContacto() : null)
                .comercianteEstado(a.getComerciante() != null && a.getComerciante().getEstado() != null ? a.getComerciante().getEstado().name() : null)
                .fechaSolicitud(a.getFechaSolicitud())
                .fechaInicio(a.getFechaInicio())
                .fechaFin(a.getFechaFin())
                .estado(a.getEstado())
                .estadoDescripcion(a.getEstado() != null ? a.getEstado().getDescripcion() : null)
                .badgeClase(a.getEstado() != null ? a.getEstado().getBadgeClase() : null)
                .tipoAsignacion(a.getTipoAsignacion())
                .motivo(a.getMotivo())
                .observaciones(a.getObservaciones())
                .usuarioRegistro(a.getUsuarioRegistro())
                .usuarioEvaluacion(a.getUsuarioEvaluacion())
                .fechaEvaluacion(a.getFechaEvaluacion())
                .observacionesEvaluacion(a.getObservacionesEvaluacion())
                .usuarioAprobacion(a.getUsuarioAprobacion())
                .fechaAprobacion(a.getFechaAprobacion())
                .motivoRechazo(a.getMotivoRechazo())
                .motivoSuspension(a.getMotivoSuspension())
                .motivoFinalizacion(a.getMotivoFinalizacion())
                .documentoSustentoId(a.getDocumentoSustento() != null ? a.getDocumentoSustento().getId() : null)
                .documentoSustentoNombre(a.getDocumentoSustento() != null ? a.getDocumentoSustento().getNombreOriginal() : null)
                .expedienteId(a.getExpedienteId())
                .version(a.getVersion())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();

        if (a.getId() != null) {
            List<AsignacionEvaluacionResponse> evalList = evaluacionRepository.findByAsignacionIdOrderByFechaEvaluacionDesc(a.getId()).stream()
                    .map(e -> AsignacionEvaluacionResponse.builder()
                            .id(e.getId())
                            .tipoEvaluacion(e.getTipoEvaluacion())
                            .resultado(e.getResultado())
                            .motivo(e.getMotivo())
                            .documentoSustentoId(e.getDocumentoSustento() != null ? e.getDocumentoSustento().getId() : null)
                            .documentoSustentoNombre(e.getDocumentoSustento() != null ? e.getDocumentoSustento().getNombreOriginal() : null)
                            .usuarioEvaluador(e.getUsuarioEvaluador())
                            .fechaEvaluacion(e.getFechaEvaluacion())
                            .build())
                    .collect(Collectors.toList());
            detail.setEvaluaciones(evalList);
        }

        return detail;
    }
}
