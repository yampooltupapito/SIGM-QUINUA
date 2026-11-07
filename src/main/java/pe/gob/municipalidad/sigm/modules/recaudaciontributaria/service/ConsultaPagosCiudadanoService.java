package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.portal.PagoCiudadanoDto;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ContribuyenteTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.DocumentoPagoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.PagoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.DocumentoPagoTributarioRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.PagoTributarioRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Consulta de pagos confirmados para el ciudadano autenticado.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConsultaPagosCiudadanoService {

    private final PortalTributarioContextService contextService;
    private final PagoTributarioRepository pagoRepository;
    private final DocumentoPagoTributarioRepository reciboRepository;

    @Transactional(readOnly = true)
    public Page<PagoCiudadanoDto> listarMisPagos(Pageable pageable) {
        Optional<ContribuyenteTributario> contribOpt = contextService.obtenerContribuyenteActivo();
        if (contribOpt.isEmpty()) {
            return Page.empty(pageable);
        }

        Page<PagoTributario> page = pagoRepository
                .findByContribuyenteId(contribOpt.get().getId(), pageable);

        return page.map(this::convertirADto);
    }

    @Transactional(readOnly = true)
    public PagoCiudadanoDto obtenerDetallePago(UUID pagoId) {
        PagoTributario pago = contextService.validarAccesoPago(pagoId);
        return convertirADto(pago);
    }

    private PagoCiudadanoDto convertirADto(PagoTributario p) {
        String numRecibo = null;
        UUID reciboId = null;

        List<DocumentoPagoTributario> docs = reciboRepository.findByPagoTributarioId(p.getId());
        if (!docs.isEmpty()) {
            DocumentoPagoTributario doc = docs.get(0);
            numRecibo = doc.getCodigoDocumento();
            reciboId = doc.getId();
        }

        return PagoCiudadanoDto.builder()
                .id(p.getId())
                .codigoPago(p.getCodigoPago())
                .fechaPago(p.getFechaHoraPago())
                .montoTotal(p.getImporte())
                .medioPago(p.getMedioPago() != null ? p.getMedioPago().getDescripcion() : "VENTANILLA")
                .estado(p.getEstado() != null ? p.getEstado().name() : "CONFIRMADO")
                .reciboNumero(numRecibo)
                .reciboId(reciboId)
                .observaciones(p.getObservaciones())
                .build();
    }
}

