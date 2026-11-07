package pe.gob.municipalidad.sigm.modules.mercados.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.modules.mercados.dto.PortalComercianteDashboardResponse;
import pe.gob.municipalidad.sigm.modules.mercados.entity.Comerciante;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoAsignacion;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoAutorizacion;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoContrato;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoObligacion;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoSolicitudGestion;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoVinculoComerciante;
import pe.gob.municipalidad.sigm.modules.mercados.repository.*;
import pe.gob.municipalidad.sigm.modules.notificaciones.service.NotificacionService;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class PortalComercianteDashboardService {

    private final ComerciantePortalContextService contextService;
    private final AsignacionPuestoRepository asignacionRepository;
    private final ContratoOcupacionRepository contratoRepository;
    private final AutorizacionComercialRepository autorizacionRepository;
    private final ObligacionMercadoRepository obligacionRepository;
    private final PagoMercadoRepository pagoRepository;
    private final SolicitudGestionOcupacionRepository solicitudRepository;
    private final NotificacionService notificacionService;

    public PortalComercianteDashboardService(
            ComerciantePortalContextService contextService,
            AsignacionPuestoRepository asignacionRepository,
            ContratoOcupacionRepository contratoRepository,
            AutorizacionComercialRepository autorizacionRepository,
            ObligacionMercadoRepository obligacionRepository,
            PagoMercadoRepository pagoRepository,
            SolicitudGestionOcupacionRepository solicitudRepository,
            NotificacionService notificacionService) {
        this.contextService = contextService;
        this.asignacionRepository = asignacionRepository;
        this.contratoRepository = contratoRepository;
        this.autorizacionRepository = autorizacionRepository;
        this.obligacionRepository = obligacionRepository;
        this.pagoRepository = pagoRepository;
        this.solicitudRepository = solicitudRepository;
        this.notificacionService = notificacionService;
    }

    @Transactional(readOnly = true)
    public PortalComercianteDashboardResponse obtenerDashboard() {
        return obtenerResumenDashboard();
    }

    @Transactional(readOnly = true)
    public PortalComercianteDashboardResponse obtenerResumenDashboard() {
        Usuario usuario = contextService.getUsuarioAutenticado();
        Comerciante comerciante = contextService.getComercianteActivo();
        UUID cId = comerciante.getId();

        int asignacionesVigentes = (int) asignacionRepository.countByComercianteIdAndEstado(cId, EstadoAsignacion.VIGENTE);
        int puestosActivos = asignacionesVigentes;
        int contratosVigentes = (int) contratoRepository.countByComercianteIdAndEstado(cId, EstadoContrato.VIGENTE);
        int autorizacionesVigentes = (int) autorizacionRepository.countByComercianteIdAndEstadoIn(
                cId, List.of(EstadoAutorizacion.VIGENTE, EstadoAutorizacion.EMITIDA));

        BigDecimal deudaPendiente = obligacionRepository.sumarDeudaTotalComerciante(cId);
        if (deudaPendiente == null) deudaPendiente = BigDecimal.ZERO;

        int obligacionesPendientesCount = (int) obligacionRepository.countByComercianteIdAndEstadoIn(
                cId, List.of(EstadoObligacion.PENDIENTE, EstadoObligacion.PARCIALMENTE_PAGADA, EstadoObligacion.VENCIDA));

        long pagosCount = pagoRepository.countByComercianteId(cId);
        BigDecimal totalPagos = pagoRepository.findByComercianteIdOrderByFechaPagoDesc(cId).stream()
                .map(p -> p.getMontoRecibido() != null ? p.getMontoRecibido() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int solicitudesEnTramite = (int) solicitudRepository.countByComercianteActualIdAndEstadoIn(
                cId, List.of(EstadoSolicitudGestion.PRESENTADA, EstadoSolicitudGestion.EN_EVALUACION,
                        EstadoSolicitudGestion.OBSERVADA, EstadoSolicitudGestion.PENDIENTE_FORMALIZACION));

        long notifNoLeidas = notificacionService.contarNoLeidas(usuario.getUsername());

        return PortalComercianteDashboardResponse.builder()
                .comercianteId(cId)
                .codigoComerciante(comerciante.getCodigoComerciante())
                .nombreTitular(comerciante.getNombreOTitular())
                .tipoComerciante(comerciante.getTipoComerciante())
                .tipoVinculo(TipoVinculoComerciante.TITULAR)
                .puestosActivos(puestosActivos)
                .asignacionesVigentes(asignacionesVigentes)
                .contratosVigentes(contratosVigentes)
                .autorizacionesVigentes(autorizacionesVigentes)
                .deudaPendienteTotal(deudaPendiente)
                .obligacionesPendientesCount(obligacionesPendientesCount)
                .totalPagosRealizados(totalPagos)
                .pagosRealizadosCount((int) pagosCount)
                .solicitudesEnTramite(solicitudesEnTramite)
                .notificacionesNoLeidas(notifNoLeidas)
                .build();
    }
}
