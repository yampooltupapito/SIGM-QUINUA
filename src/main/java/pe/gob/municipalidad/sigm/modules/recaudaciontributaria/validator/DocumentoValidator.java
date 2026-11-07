package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.DocumentoPagoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.PagoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.SerieDocumentoPagoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.TipoDocumentoPagoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoDocumentoPagoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoPagoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.DocumentoPagoTributarioRepository;

@Component
@RequiredArgsConstructor
public class DocumentoValidator {

    private final DocumentoPagoTributarioRepository documentoRepository;

    public void validarPagoParaEmision(PagoTributario pago, TipoDocumentoPagoTributario tipoDocumento) {
        if (pago == null) {
            throw new BusinessException("El pago especificado no existe");
        }

        if (pago.getEstado() != EstadoPagoTributario.CONFIRMADO && pago.getEstado() != EstadoPagoTributario.PROCESADO) {
            throw new BusinessException("Solo se pueden emitir documentos oficiales sobre pagos en estado CONFIRMADO. " +
                    "El pago " + pago.getCodigoPago() + " se encuentra en estado: " + pago.getEstado());
        }

        if (pago.getFechaReversion() != null || pago.getEstado() == EstadoPagoTributario.REVERTIDO) {
            throw new BusinessException("No se puede emitir documento sobre un pago revertido");
        }

        boolean yaEmitido = documentoRepository.existsByPagoTributarioIdAndTipoDocumentoIdAndEstado(
                pago.getId(), tipoDocumento.getId(), EstadoDocumentoPagoTributario.EMITIDO);

        if (yaEmitido) {
            throw new BusinessException("Ya existe un documento activo (" + tipoDocumento.getNombre() +
                    ") emitido para el pago " + pago.getCodigoPago() + ". Para volver a imprimir, utilice la opción de Reimpresión.");
        }
    }

    public void validarSerieParaEmision(SerieDocumentoPagoTributario serie, TipoDocumentoPagoTributario tipo) {
        if (serie == null) {
            throw new BusinessException("La serie documental especificada no existe");
        }

        if (!"ACTIVA".equalsIgnoreCase(serie.getEstado())) {
            throw new BusinessException("La serie documental " + serie.getCodigoSerie() + " no está ACTIVA");
        }

        if (!serie.getTipoDocumento().getId().equals(tipo.getId())) {
            throw new BusinessException("La serie " + serie.getCodigoSerie() + " no corresponde al tipo documental " + tipo.getNombre());
        }
    }

    public void validarParaAnulacion(DocumentoPagoTributario doc, String motivo) {
        if (doc == null) {
            throw new BusinessException("El documento a anular no existe");
        }

        if (doc.getEstado() != EstadoDocumentoPagoTributario.EMITIDO) {
            throw new BusinessException("Solo se pueden anular documentos en estado EMITIDO. Estado actual: " + doc.getEstado());
        }

        if (motivo == null || motivo.trim().length() < 10) {
            throw new BusinessException("El motivo de anulación es obligatorio y debe tener al menos 10 caracteres");
        }
    }

    public void validarParaSustitucion(DocumentoPagoTributario docOriginal, String motivo) {
        if (docOriginal == null) {
            throw new BusinessException("El documento a sustituir no existe");
        }

        if (docOriginal.getEstado() != EstadoDocumentoPagoTributario.EMITIDO) {
            throw new BusinessException("Solo se pueden sustituir documentos en estado EMITIDO. Estado actual: " + docOriginal.getEstado());
        }

        if (motivo == null || motivo.trim().length() < 10) {
            throw new BusinessException("El motivo de sustitución es obligatorio y debe tener al menos 10 caracteres");
        }

        PagoTributario pago = docOriginal.getPagoTributario();
        if (pago.getEstado() == EstadoPagoTributario.REVERTIDO || pago.getEstado() == EstadoPagoTributario.ANULADO) {
            throw new BusinessException("No se puede emitir un sustitutorio porque el pago de origen fue anulado o revertido");
        }
    }
}
