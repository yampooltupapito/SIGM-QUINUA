package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.portal.ReciboCiudadanoDto;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.AplicacionPagoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ContribuyenteTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.DocumentoPagoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoDocumentoPagoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.AplicacionPagoTributarioRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.DocumentoPagoTributarioRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Consulta de recibos oficiales y descarga segura de comprobantes PDF para el ciudadano.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConsultaDocumentosCiudadanoService {

    private final PortalTributarioContextService contextService;
    private final DocumentoPagoTributarioRepository reciboRepository;
    private final AplicacionPagoTributarioRepository aplicacionPagoRepository;
    private final PdfDocumentoTributarioService pdfDocumentoTributarioService;

    @Transactional(readOnly = true)
    public List<ReciboCiudadanoDto> listarMisRecibos() {
        Optional<ContribuyenteTributario> contribOpt = contextService.obtenerContribuyenteActivo();
        if (contribOpt.isEmpty()) {
            return Collections.emptyList();
        }

        List<DocumentoPagoTributario> docs = reciboRepository
                .findByContribuyenteIdOrderByFechaEmisionDesc(contribOpt.get().getId());

        return docs.stream()
                .map(this::convertirADto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public byte[] descargarReciboPdf(UUID reciboId) {
        // Valida acceso anti-IDOR
        DocumentoPagoTributario doc = contextService.validarAccesoRecibo(reciboId);

        List<AplicacionPagoTributario> aplicaciones = Collections.emptyList();
        if (doc.getPagoTributario() != null) {
            aplicaciones = aplicacionPagoRepository.findByPagoId(doc.getPagoTributario().getId());
        }

        String urlVerificacion = doc.getUrlVerificacion();
        if (urlVerificacion == null || urlVerificacion.isEmpty()) {
            urlVerificacion = "/verificar/tributaria/" + doc.getCodigoVerificacion();
        }

        return pdfDocumentoTributarioService.generarPdfDocumento(doc, aplicaciones, urlVerificacion);
    }

    private ReciboCiudadanoDto convertirADto(DocumentoPagoTributario d) {
        String codSerie = d.getSerie() != null ? d.getSerie().getCodigoSerie() : "";
        String numCorrelativo = d.getNumeroCorrelativo() != null ? String.valueOf(d.getNumeroCorrelativo()) : "";

        return ReciboCiudadanoDto.builder()
                .id(d.getId())
                .codigoDocumento(d.getCodigoDocumento())
                .serie(codSerie)
                .numero(numCorrelativo)
                .numeroCompleto(d.getCodigoDocumento())
                .tipoDocumentoNombre(d.getTipoDocumento() != null ? d.getTipoDocumento().getNombre() : "Recibo de Pago")
                .fechaEmision(d.getFechaEmision())
                .importeTotal(d.getImporteTotal())
                .estado(d.getEstado() != null ? d.getEstado().name() : "EMITIDO")
                .codigoVerificacion(d.getCodigoVerificacion())
                .hashDocumento(d.getHashDocumento())
                .urlVerificacion(d.getUrlVerificacion())
                .anulado(d.getEstado() == EstadoDocumentoPagoTributario.ANULADO)
                .build();
    }
}

