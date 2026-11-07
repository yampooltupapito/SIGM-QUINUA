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
import pe.gob.municipalidad.sigm.modules.mercados.dto.CambioTitularRequest;
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
public class CambioTitularService {

    private final SolicitudGestionOcupacionRepository solicitudRepository;
    private final ProcedimientoGestionOcupacionRepository procedimientoRepository;
    private final PuestoComercialRepository puestoRepository;
    private final AsignacionPuestoRepository asignacionRepository;
    private final ComercianteRepository comercianteRepository;
    private final OficinaRepository oficinaRepository;
    private final AuditoriaAdministrativaService auditoriaService;
    private final RenovacionOcupacionService renovacionService;

    public CambioTitularService(
            SolicitudGestionOcupacionRepository solicitudRepository,
            ProcedimientoGestionOcupacionRepository procedimientoRepository,
            PuestoComercialRepository puestoRepository,
            AsignacionPuestoRepository asignacionRepository,
            ComercianteRepository comercianteRepository,
            OficinaRepository oficinaRepository,
            AuditoriaAdministrativaService auditoriaService,
            RenovacionOcupacionService renovacionService) {
        this.solicitudRepository = solicitudRepository;
        this.procedimientoRepository = procedimientoRepository;
        this.puestoRepository = puestoRepository;
        this.asignacionRepository = asignacionRepository;
        this.comercianteRepository = comercianteRepository;
        this.oficinaRepository = oficinaRepository;
        this.auditoriaService = auditoriaService;
        this.renovacionService = renovacionService;
    }

    @Transactional
    public SolicitudGestionOcupacionResponse solicitarCambioTitular(CambioTitularRequest request, String username) {
        log.info("Iniciando solicitud de cambio de titular para puesto ID: {} de actual: {} a propuesto: {}",
                request.getPuestoId(), request.getComercianteActualId(), request.getComerciantePropuestoId());

        PuestoComercial puesto = puestoRepository.findById(request.getPuestoId())
                .orElseThrow(() -> new ResourceNotFoundException("Puesto comercial no encontrado: " + request.getPuestoId()));

        AsignacionPuesto asignacion = asignacionRepository.findById(request.getAsignacionId())
                .orElseThrow(() -> new ResourceNotFoundException("Asignación no encontrada: " + request.getAsignacionId()));

        if (!asignacion.getPuesto().getId().equals(puesto.getId())) {
            throw new BusinessException("La asignación no corresponde al puesto comercial indicado: " + puesto.getNumero());
        }

        if (asignacion.getEstado() == EstadoAsignacion.FINALIZADA || asignacion.getEstado() == EstadoAsignacion.ANULADA) {
            throw new BusinessException("No procede solicitar cambio de titular sobre una asignación finalizada o anulada.");
        }

        Comerciante comercianteActual = comercianteRepository.findById(request.getComercianteActualId())
                .orElseThrow(() -> new ResourceNotFoundException("Comerciante actual no encontrado: " + request.getComercianteActualId()));

        if (!asignacion.getComerciante().getId().equals(comercianteActual.getId())) {
            throw new BusinessException("El comerciante indicado no coincide con el titular vigente de la asignación.");
        }

        Comerciante comerciantePropuesto = comercianteRepository.findById(request.getComerciantePropuestoId())
                .orElseThrow(() -> new ResourceNotFoundException("Comerciante propuesto no encontrado: " + request.getComerciantePropuestoId()));

        if (comercianteActual.getId().equals(comerciantePropuesto.getId())) {
            throw new BusinessException("El comerciante propuesto debe ser distinto al titular actual.");
        }

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
            throw new BusinessException("Ya existe una solicitud en trámite activo para esta asignación de puesto.");
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
                .tipoSolicitud(TipoSolicitudGestion.CAMBIO_TITULAR)
                .procedimiento(procedimiento)
                .mercado(puesto.getMercado())
                .puesto(puesto)
                .asignacionOrigen(asignacion)
                .comercianteActual(comercianteActual)
                .comerciantePropuesto(comerciantePropuesto)
                .fechaPresentacion(LocalDate.now())
                .fechaInicioSolicitada(request.getFechaInicioSolicitada())
                .motivo(request.getMotivo())
                .estado(EstadoSolicitudGestion.PRESENTADA)
                .expedienteId(request.getExpedienteId())
                .oficinaResponsable(oficina)
                .usuarioRegistro(username)
                .tokenVerificacionQr(tokenQr)
                .build();

        SolicitudGestionOcupacion guardada = solicitudRepository.save(solicitud);

        auditoriaService.registrar("SOLICITAR_CAMBIO_TITULAR", "mercados_solicitudes_gestion", guardada.getId(),
                "EXITO", "Solicitud de cambio de titular " + codigo + " para puesto " + puesto.getNumero() +
                        " (De " + comercianteActual.getPersona().getNumeroDocumento() + " a " + comerciantePropuesto.getPersona().getNumeroDocumento() + ")");

        return renovacionService.toResponse(guardada);
    }

    @Transactional(readOnly = true)
    public Page<SolicitudGestionOcupacionResponse> buscarPaginado(
            UUID mercadoId, UUID puestoId, UUID comercianteId, EstadoSolicitudGestion estado,
            LocalDate fechaDesde, LocalDate fechaHasta, String q, Pageable pageable) {

        Specification<SolicitudGestionOcupacion> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("tipoSolicitud"), TipoSolicitudGestion.CAMBIO_TITULAR));

            if (mercadoId != null) {
                predicates.add(cb.equal(root.get("mercado").get("id"), mercadoId));
            }
            if (puestoId != null) {
                predicates.add(cb.equal(root.get("puesto").get("id"), puestoId));
            }
            if (comercianteId != null) {
                predicates.add(cb.or(
                        cb.equal(root.get("comercianteActual").get("id"), comercianteId),
                        cb.equal(root.get("comerciantePropuesto").get("id"), comercianteId)
                ));
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

        return solicitudRepository.findAll(spec, pageable).map(renovacionService::toResponse);
    }
}
