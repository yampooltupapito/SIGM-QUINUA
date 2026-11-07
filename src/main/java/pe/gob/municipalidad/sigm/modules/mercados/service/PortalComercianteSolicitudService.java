package pe.gob.municipalidad.sigm.modules.mercados.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.modules.mercados.dto.PortalComercianteRenovacionRequest;
import pe.gob.municipalidad.sigm.modules.mercados.dto.PortalComercianteSolicitudResponse;
import pe.gob.municipalidad.sigm.modules.mercados.dto.RenovacionRequest;
import pe.gob.municipalidad.sigm.modules.mercados.dto.SolicitudGestionOcupacionResponse;
import pe.gob.municipalidad.sigm.modules.mercados.entity.AsignacionPuesto;
import pe.gob.municipalidad.sigm.modules.mercados.entity.Comerciante;
import pe.gob.municipalidad.sigm.modules.mercados.entity.SolicitudGestionOcupacion;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoSolicitudGestion;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoSolicitudGestion;
import pe.gob.municipalidad.sigm.modules.mercados.repository.AsignacionPuestoRepository;
import pe.gob.municipalidad.sigm.modules.mercados.repository.SolicitudGestionOcupacionRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PortalComercianteSolicitudService {

    private final ComerciantePortalContextService contextService;
    private final SolicitudGestionOcupacionRepository solicitudRepository;
    private final AsignacionPuestoRepository asignacionRepository;
    private final RenovacionOcupacionService renovacionOcupacionService;

    public PortalComercianteSolicitudService(
            ComerciantePortalContextService contextService,
            SolicitudGestionOcupacionRepository solicitudRepository,
            AsignacionPuestoRepository asignacionRepository,
            RenovacionOcupacionService renovacionOcupacionService) {
        this.contextService = contextService;
        this.solicitudRepository = solicitudRepository;
        this.asignacionRepository = asignacionRepository;
        this.renovacionOcupacionService = renovacionOcupacionService;
    }

    @Transactional(readOnly = true)
    public Page<PortalComercianteSolicitudResponse> listarMisSolicitudes(EstadoSolicitudGestion estado, Pageable pageable) {
        Comerciante comerciante = contextService.getComercianteActivo();
        Page<SolicitudGestionOcupacion> page = solicitudRepository.findByComercianteActualId(comerciante.getId(), pageable);

        List<PortalComercianteSolicitudResponse> items = page.getContent().stream()
                .filter(s -> estado == null || s.getEstado() == estado)
                .map(this::mapearSolicitud)
                .collect(Collectors.toList());

        return new PageImpl<>(items, pageable, page.getTotalElements());
    }

    @Transactional(readOnly = true)
    public PortalComercianteSolicitudResponse obtenerDetalleSolicitud(UUID solicitudId) {
        Comerciante comerciante = contextService.getComercianteActivo();
        SolicitudGestionOcupacion s = contextService.validarPertenenciaSolicitud(solicitudId, comerciante);
        return mapearSolicitud(s);
    }

    @Transactional
    public SolicitudGestionOcupacionResponse presentarSolicitudRenovacion(PortalComercianteRenovacionRequest request) {
        Usuario usuario = contextService.getUsuarioAutenticado();
        Comerciante comerciante = contextService.getComercianteActivo();

        // Validar que la asignación corresponda al comerciante activo
        AsignacionPuesto asignacion = asignacionRepository.findById(request.getAsignacionId())
                .orElseThrow(() -> new BusinessException("Asignación no encontrada: " + request.getAsignacionId()));

        if (!asignacion.getComerciante().getId().equals(comerciante.getId())) {
            throw new BusinessException("No puede presentar renovación sobre una asignación que no le pertenece.");
        }

        LocalDate fechaInicio = LocalDate.now();
        int meses = request.getPlazoMesesSolicitado() != null ? request.getPlazoMesesSolicitado() : 12;
        LocalDate fechaFin = fechaInicio.plusMonths(meses);

        RenovacionRequest renovacionReq = RenovacionRequest.builder()
                .asignacionId(asignacion.getId())
                .procedimientoId(request.getProcedimientoId())
                .tipoSolicitud(TipoSolicitudGestion.RENOVACION_ASIGNACION)
                .fechaInicioSolicitada(fechaInicio)
                .fechaFinSolicitada(fechaFin)
                .motivo(request.getJustificacion() != null ? request.getJustificacion() : "Solicitud de renovación presentada por portal de autogestión de comerciantes.")
                .build();

        return renovacionOcupacionService.solicitarRenovacion(renovacionReq, usuario.getUsername());
    }

    private PortalComercianteSolicitudResponse mapearSolicitud(SolicitudGestionOcupacion s) {
        boolean observada = s.getEstado() == EstadoSolicitudGestion.OBSERVADA || s.getEstado() == EstadoSolicitudGestion.PENDIENTE_SUBSANACION;

        return PortalComercianteSolicitudResponse.builder()
                .solicitudId(s.getId())
                .codigoSolicitud(s.getCodigoSolicitud())
                .tipoSolicitud(s.getTipoSolicitud())
                .tipoSolicitudLabel(s.getTipoSolicitud() != null ? s.getTipoSolicitud().name() : "-")
                .procedimientoNombre(s.getProcedimiento() != null ? s.getProcedimiento().getNombre() : "GESTIÓN DE OCUPACIÓN")
                .mercadoNombre(s.getPuesto() != null && s.getPuesto().getMercado() != null ? s.getPuesto().getMercado().getNombre() : "-")
                .puestoCodigo(s.getPuesto() != null ? s.getPuesto().getCodigo() : "-")
                .fechaPresentacion(s.getFechaPresentacion())
                .estado(s.getEstado())
                .estadoLabel(s.getEstado() != null ? s.getEstado().name() : "-")
                .tokenVerificacionQr(s.getTokenVerificacionQr())
                .tieneObservacionesPendientes(observada)
                .cantidadObservaciones(observada ? 1 : 0)
                .motivoObservacionReciente(observada ? "Tiene observaciones que requieren subsanación administrativa." : null)
                .build();
    }
}
