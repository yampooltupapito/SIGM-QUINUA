package pe.gob.municipalidad.sigm.modules.mercados.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.common.repository.OficinaRepository;
import pe.gob.municipalidad.sigm.modules.administracion.service.AuditoriaAdministrativaService;
import pe.gob.municipalidad.sigm.modules.mercados.dto.RenovacionRequest;
import pe.gob.municipalidad.sigm.modules.mercados.dto.SolicitudGestionOcupacionResponse;
import pe.gob.municipalidad.sigm.modules.mercados.entity.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoAsignacion;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoSolicitudGestion;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoSolicitudGestion;
import pe.gob.municipalidad.sigm.modules.mercados.repository.*;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
public class RenovacionOcupacionService {

    private final SolicitudGestionOcupacionRepository solicitudRepository;
    private final ProcedimientoGestionOcupacionRepository procedimientoRepository;
    private final AsignacionPuestoRepository asignacionRepository;
    private final ContratoOcupacionRepository contratoRepository;
    private final AutorizacionComercialRepository autorizacionRepository;
    private final OficinaRepository oficinaRepository;
    private final AuditoriaAdministrativaService auditoriaService;

    public RenovacionOcupacionService(
            SolicitudGestionOcupacionRepository solicitudRepository,
            ProcedimientoGestionOcupacionRepository procedimientoRepository,
            AsignacionPuestoRepository asignacionRepository,
            ContratoOcupacionRepository contratoRepository,
            AutorizacionComercialRepository autorizacionRepository,
            OficinaRepository oficinaRepository,
            AuditoriaAdministrativaService auditoriaService) {
        this.solicitudRepository = solicitudRepository;
        this.procedimientoRepository = procedimientoRepository;
        this.asignacionRepository = asignacionRepository;
        this.contratoRepository = contratoRepository;
        this.autorizacionRepository = autorizacionRepository;
        this.oficinaRepository = oficinaRepository;
        this.auditoriaService = auditoriaService;
    }

    @Transactional
    public SolicitudGestionOcupacionResponse solicitarRenovacion(RenovacionRequest request, String username) {
        log.info("Iniciando solicitud de renovación para asignación ID: {} por usuario: {}", request.getAsignacionId(), username);

        AsignacionPuesto asignacion = asignacionRepository.findById(request.getAsignacionId())
                .orElseThrow(() -> new ResourceNotFoundException("Asignación no encontrada: " + request.getAsignacionId()));

        if (asignacion.getEstado() == EstadoAsignacion.FINALIZADA || asignacion.getEstado() == EstadoAsignacion.ANULADA) {
            throw new BusinessException("No procede solicitar renovación sobre una asignación finalizada o anulada.");
        }

        if (request.getTipoSolicitud() != TipoSolicitudGestion.RENOVACION_ASIGNACION &&
            request.getTipoSolicitud() != TipoSolicitudGestion.RENOVACION_CONTRATO &&
            request.getTipoSolicitud() != TipoSolicitudGestion.RENOVACION_AUTORIZACION) {
            throw new BusinessException("El tipo de solicitud indicado no corresponde a un trámite de renovación: " + request.getTipoSolicitud());
        }

        ContratoOcupacion contrato = null;
        if (request.getContratoId() != null) {
            contrato = contratoRepository.findById(request.getContratoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Contrato no encontrado: " + request.getContratoId()));
            if (!contrato.getAsignacion().getId().equals(asignacion.getId())) {
                throw new BusinessException("El contrato indicado no corresponde a la asignación de origen.");
            }
        }

        AutorizacionComercial autorizacion = null;
        if (request.getAutorizacionId() != null) {
            autorizacion = autorizacionRepository.findById(request.getAutorizacionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Autorización comercial no encontrada: " + request.getAutorizacionId()));
            if (autorizacion.getAsignacion() != null && !autorizacion.getAsignacion().getId().equals(asignacion.getId())) {
                throw new BusinessException("La autorización comercial no corresponde a la asignación de origen.");
            }
        }

        if (request.getFechaInicioSolicitada() != null && request.getFechaFinSolicitada() != null) {
            if (request.getFechaFinSolicitada().isBefore(request.getFechaInicioSolicitada())) {
                throw new BusinessException("La fecha final solicitada no puede ser anterior a la fecha inicial.");
            }
        }

        // Validar no duplicidad de trámites activos
        Set<EstadoSolicitudGestion> estadosActivos = Set.of(
                EstadoSolicitudGestion.PRESENTADA,
                EstadoSolicitudGestion.EN_REVISION,
                EstadoSolicitudGestion.OBSERVADA,
                EstadoSolicitudGestion.PENDIENTE_SUBSANACION,
                EstadoSolicitudGestion.EN_EVALUACION,
                EstadoSolicitudGestion.APROBADA,
                EstadoSolicitudGestion.PENDIENTE_FORMALIZACION
        );

        if (solicitudRepository.existsByAsignacionOrigenIdAndEstadoIn(asignacion.getId(), estadosActivos)) {
            throw new BusinessException("Ya existe una solicitud de gestión u ocupación en trámite activo para esta asignación.");
        }

        ProcedimientoGestionOcupacion procedimiento = null;
        if (request.getProcedimientoId() != null) {
            procedimiento = procedimientoRepository.findById(request.getProcedimientoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Procedimiento no encontrado: " + request.getProcedimientoId()));
        }

        Oficina oficina = null;
        if (request.getOficinaResponsableId() != null) {
            oficina = oficinaRepository.findById(request.getOficinaResponsableId()).orElse(null);
        } else if (procedimiento != null && procedimiento.getOficinaCompetente() != null) {
            oficina = procedimiento.getOficinaCompetente();
        }

        Long correlativo = solicitudRepository.obtenerSiguienteCorrelativo();
        String codigo = String.format("SOL-MER-%d-%06d", Year.now().getValue(), correlativo);
        String tokenQr = UUID.randomUUID().toString();

        SolicitudGestionOcupacion solicitud = SolicitudGestionOcupacion.builder()
                .codigoSolicitud(codigo)
                .tipoSolicitud(request.getTipoSolicitud())
                .procedimiento(procedimiento)
                .mercado(asignacion.getPuesto().getMercado())
                .puesto(asignacion.getPuesto())
                .asignacionOrigen(asignacion)
                .contratoOrigen(contrato)
                .autorizacionOrigen(autorizacion)
                .comercianteActual(asignacion.getComerciante())
                .comerciantePropuesto(null) // Las renovaciones mantienen al titular
                .fechaPresentacion(LocalDate.now())
                .fechaInicioSolicitada(request.getFechaInicioSolicitada())
                .fechaFinSolicitada(request.getFechaFinSolicitada())
                .motivo(request.getMotivo())
                .estado(EstadoSolicitudGestion.PRESENTADA)
                .expedienteId(request.getExpedienteId())
                .oficinaResponsable(oficina)
                .usuarioRegistro(username)
                .tokenVerificacionQr(tokenQr)
                .build();

        SolicitudGestionOcupacion guardada = solicitudRepository.save(solicitud);

        auditoriaService.registrar("SOLICITAR_RENOVACION_MERCADO", "mercados_solicitudes_gestion", guardada.getId(),
                "EXITO", "Solicitud de renovación " + codigo + " registrada para asignación " + asignacion.getCodigoAsignacion());

        return toResponse(guardada);
    }

    @Transactional(readOnly = true)
    public Page<SolicitudGestionOcupacionResponse> buscarPaginado(
            UUID mercadoId, UUID puestoId, UUID comercianteId, TipoSolicitudGestion tipo,
            EstadoSolicitudGestion estado, LocalDate fechaDesde, LocalDate fechaHasta, String q, Pageable pageable) {

        Specification<SolicitudGestionOcupacion> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Por defecto filtrar tipos de renovación si tipo no está explicitado
            if (tipo != null) {
                predicates.add(cb.equal(root.get("tipoSolicitud"), tipo));
            } else {
                predicates.add(root.get("tipoSolicitud").in(
                        TipoSolicitudGestion.RENOVACION_ASIGNACION,
                        TipoSolicitudGestion.RENOVACION_CONTRATO,
                        TipoSolicitudGestion.RENOVACION_AUTORIZACION
                ));
            }

            if (mercadoId != null) {
                predicates.add(cb.equal(root.get("mercado").get("id"), mercadoId));
            }
            if (puestoId != null) {
                predicates.add(cb.equal(root.get("puesto").get("id"), puestoId));
            }
            if (comercianteId != null) {
                predicates.add(cb.equal(root.get("comercianteActual").get("id"), comercianteId));
            }
            if (estado != null) {
                predicates.add(cb.equal(root.get("estado"), estado));
            }
            if (fechaDesde != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("fechaPresentacion"), fechaDesde));
            }
            if (fechaHasta != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("fechaPresentacion"), fechaHasta));
            }
            if (q != null && !q.trim().isEmpty()) {
                String pattern = "%" + q.trim().toLowerCase() + "%";
                Predicate porCodigo = cb.like(cb.lower(root.get("codigoSolicitud")), pattern);
                Predicate porMotivo = cb.like(cb.lower(root.get("motivo")), pattern);
                predicates.add(cb.or(porCodigo, porMotivo));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return solicitudRepository.findAll(spec, pageable).map(this::toResponse);
    }

    public SolicitudGestionOcupacionResponse toResponse(SolicitudGestionOcupacion s) {
        return SolicitudGestionOcupacionResponse.builder()
                .id(s.getId())
                .codigoSolicitud(s.getCodigoSolicitud())
                .tipoSolicitud(s.getTipoSolicitud())
                .tipoSolicitudLabel(s.getTipoSolicitud().getLabel())
                .procedimientoId(s.getProcedimiento() != null ? s.getProcedimiento().getId() : null)
                .procedimientoNombre(s.getProcedimiento() != null ? s.getProcedimiento().getNombre() : null)
                .mercadoId(s.getMercado().getId())
                .mercadoNombre(s.getMercado().getNombre())
                .puestoId(s.getPuesto().getId())
                .puestoNumero(s.getPuesto().getNumero())
                .asignacionOrigenId(s.getAsignacionOrigen().getId())
                .asignacionOrigenCodigo(s.getAsignacionOrigen().getCodigoAsignacion())
                .contratoOrigenId(s.getContratoOrigen() != null ? s.getContratoOrigen().getId() : null)
                .contratoOrigenCodigo(s.getContratoOrigen() != null ? s.getContratoOrigen().getCodigoContrato() : null)
                .autorizacionOrigenId(s.getAutorizacionOrigen() != null ? s.getAutorizacionOrigen().getId() : null)
                .autorizacionOrigenCodigo(s.getAutorizacionOrigen() != null ? s.getAutorizacionOrigen().getCodigoAutorizacion() : null)
                .comercianteActualId(s.getComercianteActual().getId())
                .comercianteActualNombre(s.getComercianteActual().getPersona().getNombreCompleto())
                .comercianteActualDocumento(s.getComercianteActual().getPersona().getNumeroDocumento())
                .comerciantePropuestoId(s.getComerciantePropuesto() != null ? s.getComerciantePropuesto().getId() : null)
                .comerciantePropuestoNombre(s.getComerciantePropuesto() != null ? s.getComerciantePropuesto().getPersona().getNombreCompleto() : null)
                .comerciantePropuestoDocumento(s.getComerciantePropuesto() != null ? s.getComerciantePropuesto().getPersona().getNumeroDocumento() : null)
                .fechaPresentacion(s.getFechaPresentacion())
                .fechaInicioSolicitada(s.getFechaInicioSolicitada())
                .fechaFinSolicitada(s.getFechaFinSolicitada())
                .motivo(s.getMotivo())
                .estado(s.getEstado())
                .estadoLabel(s.getEstado().getLabel())
                .estadoBadgeClass(s.getEstado().getBadgeClass())
                .expedienteId(s.getExpedienteId())
                .oficinaResponsableId(s.getOficinaResponsable() != null ? s.getOficinaResponsable().getId() : null)
                .oficinaResponsableNombre(s.getOficinaResponsable() != null ? s.getOficinaResponsable().getNombre() : null)
                .usuarioRegistro(s.getUsuarioRegistro())
                .usuarioEvaluacion(s.getUsuarioEvaluacion())
                .fechaEvaluacion(s.getFechaEvaluacion())
                .informeEvaluacion(s.getInformeEvaluacion())
                .usuarioDecision(s.getUsuarioDecision())
                .fechaDecision(s.getFechaDecision())
                .motivoDecision(s.getMotivoDecision())
                .usuarioFormalizacion(s.getUsuarioFormalizacion())
                .fechaFormalizacion(s.getFechaFormalizacion())
                .asignacionNuevaId(s.getAsignacionNueva() != null ? s.getAsignacionNueva().getId() : null)
                .asignacionNuevaCodigo(s.getAsignacionNueva() != null ? s.getAsignacionNueva().getCodigoAsignacion() : null)
                .contratoNuevoId(s.getContratoNuevo() != null ? s.getContratoNuevo().getId() : null)
                .autorizacionNuevaId(s.getAutorizacionNueva() != null ? s.getAutorizacionNueva().getId() : null)
                .tokenVerificacionQr(s.getTokenVerificacionQr())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }
}
