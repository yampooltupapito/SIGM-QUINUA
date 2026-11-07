package pe.gob.municipalidad.sigm.modules.mercados.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.modules.mercados.dto.PortalComercianteAutorizacionResponse;
import pe.gob.municipalidad.sigm.modules.mercados.entity.AutorizacionComercial;
import pe.gob.municipalidad.sigm.modules.mercados.entity.Comerciante;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoAutorizacion;
import pe.gob.municipalidad.sigm.modules.mercados.repository.AutorizacionComercialRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PortalComercianteAutorizacionService {

    private final ComerciantePortalContextService contextService;
    private final AutorizacionComercialRepository autorizacionRepository;
    private final AutorizacionComercialService autorizacionComercialService;

    public PortalComercianteAutorizacionService(
            ComerciantePortalContextService contextService,
            AutorizacionComercialRepository autorizacionRepository,
            AutorizacionComercialService autorizacionComercialService) {
        this.contextService = contextService;
        this.autorizacionRepository = autorizacionRepository;
        this.autorizacionComercialService = autorizacionComercialService;
    }

    @Transactional(readOnly = true)
    public Page<PortalComercianteAutorizacionResponse> listarMisAutorizaciones(EstadoAutorizacion estado, Pageable pageable) {
        Comerciante comerciante = contextService.getComercianteActivo();
        Page<AutorizacionComercial> page = autorizacionRepository.findByComercianteId(comerciante.getId(), pageable);

        List<PortalComercianteAutorizacionResponse> items = page.getContent().stream()
                .filter(a -> estado == null || a.getEstado() == estado)
                .map(this::mapearAutorizacion)
                .collect(Collectors.toList());

        return new PageImpl<>(items, pageable, page.getTotalElements());
    }

    @Transactional(readOnly = true)
    public PortalComercianteAutorizacionResponse obtenerDetalleAutorizacion(UUID autorizacionId) {
        Comerciante comerciante = contextService.getComercianteActivo();
        AutorizacionComercial a = contextService.validarPertenenciaAutorizacion(autorizacionId, comerciante);
        return mapearAutorizacion(a);
    }

    @Transactional(readOnly = true)
    public byte[] descargarAutorizacionPdf(UUID autorizacionId) {
        Comerciante comerciante = contextService.getComercianteActivo();
        contextService.validarPertenenciaAutorizacion(autorizacionId, comerciante);
        return autorizacionComercialService.generarPdf(autorizacionId);
    }

    private PortalComercianteAutorizacionResponse mapearAutorizacion(AutorizacionComercial a) {
        LocalDate hoy = LocalDate.now();
        boolean vigente = a.getEstado() == EstadoAutorizacion.VIGENTE &&
                (a.getFechaFin() == null || !hoy.isAfter(a.getFechaFin()));

        String qrUrl = "/verificar/documento?codigo=" + (a.getCodigoSeguro() != null ? a.getCodigoSeguro() : a.getCodigoAutorizacion());

        String giro = "-";
        if (a.getComerciante() != null && a.getComerciante().getActividades() != null && !a.getComerciante().getActividades().isEmpty()) {
            giro = a.getComerciante().getActividades().iterator().next().getNombre();
        }

        return PortalComercianteAutorizacionResponse.builder()
                .autorizacionId(a.getId())
                .codigoAutorizacion(a.getCodigoAutorizacion())
                .numeroAutorizacion(a.getNumeroAutorizacion())
                .codigoSeguro(a.getCodigoSeguro())
                .tipoAutorizacion(a.getTipoAutorizacion() != null ? a.getTipoAutorizacion() : "PADRON_MUNICIPAL")
                .puestoId(a.getPuesto() != null ? a.getPuesto().getId() : null)
                .puestoCodigo(a.getPuesto() != null ? a.getPuesto().getCodigo() : "-")
                .mercadoNombre(a.getPuesto() != null && a.getPuesto().getMercado() != null ? a.getPuesto().getMercado().getNombre() : "-")
                .fechaEmision(a.getFechaEmision())
                .fechaInicio(a.getFechaInicio())
                .fechaFin(a.getFechaFin())
                .estado(a.getEstado())
                .estadoLabel(a.getEstado() != null ? a.getEstado().name() : "-")
                .giroComercial(giro)
                .vigente(vigente)
                .urlVerificacionQr(qrUrl)
                .build();
    }
}
