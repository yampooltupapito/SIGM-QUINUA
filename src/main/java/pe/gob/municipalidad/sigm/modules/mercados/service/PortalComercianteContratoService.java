package pe.gob.municipalidad.sigm.modules.mercados.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.modules.mercados.dto.PortalComercianteContratoResponse;
import pe.gob.municipalidad.sigm.modules.mercados.entity.Comerciante;
import pe.gob.municipalidad.sigm.modules.mercados.entity.ContratoOcupacion;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoContrato;
import pe.gob.municipalidad.sigm.modules.mercados.repository.ContratoOcupacionRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PortalComercianteContratoService {

    private final ComerciantePortalContextService contextService;
    private final ContratoOcupacionRepository contratoRepository;
    private final ContratoOcupacionService contratoOcupacionService;

    public PortalComercianteContratoService(
            ComerciantePortalContextService contextService,
            ContratoOcupacionRepository contratoRepository,
            ContratoOcupacionService contratoOcupacionService) {
        this.contextService = contextService;
        this.contratoRepository = contratoRepository;
        this.contratoOcupacionService = contratoOcupacionService;
    }

    @Transactional(readOnly = true)
    public Page<PortalComercianteContratoResponse> listarMisContratos(EstadoContrato estado, Pageable pageable) {
        Comerciante comerciante = contextService.getComercianteActivo();
        Page<ContratoOcupacion> page = contratoRepository.findByComercianteId(comerciante.getId(), pageable);

        List<PortalComercianteContratoResponse> items = page.getContent().stream()
                // No mostrar borradores internos que todavía no sean comunicables
                .filter(c -> c.getEstado() != EstadoContrato.BORRADOR)
                .filter(c -> estado == null || c.getEstado() == estado)
                .map(this::mapearContrato)
                .collect(Collectors.toList());

        return new PageImpl<>(items, pageable, page.getTotalElements());
    }

    @Transactional(readOnly = true)
    public PortalComercianteContratoResponse obtenerDetalleContrato(UUID contratoId) {
        Comerciante comerciante = contextService.getComercianteActivo();
        ContratoOcupacion c = contextService.validarPertenenciaContrato(contratoId, comerciante);
        return mapearContrato(c);
    }

    @Transactional(readOnly = true)
    public byte[] descargarContratoPdf(UUID contratoId) {
        Comerciante comerciante = contextService.getComercianteActivo();
        contextService.validarPertenenciaContrato(contratoId, comerciante);
        return contratoOcupacionService.generarPdf(contratoId);
    }

    private PortalComercianteContratoResponse mapearContrato(ContratoOcupacion c) {
        return PortalComercianteContratoResponse.builder()
                .contratoId(c.getId())
                .codigoContrato(c.getCodigoContrato())
                .codigoSeguro(c.getCodigoSeguro())
                .puestoId(c.getPuesto() != null ? c.getPuesto().getId() : null)
                .puestoCodigo(c.getPuesto() != null ? c.getPuesto().getCodigo() : "-")
                .mercadoNombre(c.getPuesto() != null && c.getPuesto().getMercado() != null ? c.getPuesto().getMercado().getNombre() : "-")
                .tipoContratoCodigo(c.getTipoContrato() != null ? c.getTipoContrato().getNombre() : c.getTipoContratoCodigo())
                .fechaElaboracion(c.getFechaElaboracion())
                .fechaInicio(c.getFechaInicio())
                .fechaFin(c.getFechaFin())
                .estado(c.getEstado())
                .estadoLabel(c.getEstado() != null ? c.getEstado().name() : "-")
                .montoCanonMensual(null)
                .moneda("PEN")
                .formalizado(c.getFechaFormalizacion() != null)
                .documentoId(c.getDocumento() != null ? c.getDocumento().getId() : null)
                .build();
    }
}
