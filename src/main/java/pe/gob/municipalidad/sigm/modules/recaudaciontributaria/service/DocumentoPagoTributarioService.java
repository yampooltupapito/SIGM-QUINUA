package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.documentopago.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.SerieDocumentoPagoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.TipoDocumentoPagoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoDocumentoPagoTributario;
import pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface DocumentoPagoTributarioService {

    DocumentoPagoResponse emitirDocumento(EmitirDocumentoRequest request, CustomUserDetails user, String ip, String userAgent);

    DocumentoPagoResponse obtenerPorId(UUID id);

    DocumentoPagoResponse obtenerPorCodigo(String codigoDocumento);

    byte[] obtenerContenidoPdf(UUID id, CustomUserDetails user, String ip, String userAgent);

    byte[] obtenerQrPng(UUID id);

    DocumentoPagoResponse registrarReimpresion(UUID id, CustomUserDetails user, String ip, String userAgent);

    DocumentoPagoResponse anularDocumento(UUID id, AnularDocumentoRequest request, CustomUserDetails user, String ip, String userAgent);

    DocumentoPagoResponse sustituirDocumento(UUID id, SustituirDocumentoRequest request, CustomUserDetails user, String ip, String userAgent);

    VerificacionPublicaDocumentoResponse verificarPublicamente(String codigoVerificacionOQr, String ip, String userAgent);

    Page<DocumentoPagoResponse> listarDocumentos(
            String query, UUID tipoDocId, UUID serieId, UUID contribuyenteId, UUID pagoId,
            EstadoDocumentoPagoTributario estado, LocalDate desde, LocalDate hasta, Pageable pageable);

    List<HistorialDocumentoResponse> listarHistorial(UUID documentoId);

    DashboardDocumentalResponse obtenerMetricas();

    List<TipoDocumentoPagoTributario> listarTiposDocumento();

    List<SerieDocumentoPagoTributario> listarSeriesPorTipo(UUID tipoId);
}
