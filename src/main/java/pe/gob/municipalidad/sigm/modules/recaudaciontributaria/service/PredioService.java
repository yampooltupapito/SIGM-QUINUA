package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Servicio de negocio para la administración, catastro y control de predios municipales.
 */
public interface PredioService {

    PredioDetailResponse registrarPredio(PredioCreateRequest request);

    PredioDetailResponse registrarPredio(PredioCreateRequest request, String usuario);

    PredioDetailResponse actualizarPredio(UUID id, PredioUpdateRequest request);

    PredioDetailResponse actualizarPredio(UUID id, PredioUpdateRequest request, String usuario);

    PredioDetailResponse cambiarEstado(UUID id, CambioEstadoPredioRequest request);

    PredioDetailResponse cambiarEstado(UUID id, CambioEstadoPredioRequest request, String usuario);

    PredioDetailResponse obtenerDetalle(UUID id);

    PredioDetailResponse obtenerPredioPorId(UUID id);

    Page<PredioSummaryDto> listarConFiltros(PredioFilterDto filtro, Pageable pageable);

    Page<PredioSummaryDto> listarPredios(PredioFilterDto filtro, Pageable pageable);

    ConstruccionResponse agregarConstruccion(UUID predioId, ConstruccionPredialRequest request);

    ConstruccionResponse agregarConstruccion(UUID predioId, ConstruccionPredialRequest request, String usuario);

    void eliminarConstruccion(UUID predioId, UUID construccionId);

    void eliminarConstruccion(UUID predioId, UUID construccionId, String usuario);

    RelacionContribuyenteResponse asociarContribuyente(UUID predioId, RelacionContribuyentePredioRequest request);

    RelacionContribuyenteResponse asociarContribuyente(UUID predioId, RelacionContribuyentePredioRequest request, String usuario);

    RelacionContribuyenteResponse vincularContribuyente(UUID predioId, RelacionContribuyentePredioRequest request);

    RelacionContribuyenteResponse vincularContribuyente(UUID predioId, RelacionContribuyentePredioRequest request, String usuario);

    void desvincularContribuyente(UUID predioId, UUID relacionId);

    void desvincularContribuyente(UUID predioId, UUID relacionId, String usuario);

    DocumentoPredioResponse agregarDocumento(UUID predioId, DocumentoPredioRequest request);

    DocumentoPredioResponse agregarDocumento(UUID predioId, DocumentoPredioRequest request, String usuario);

    void eliminarDocumento(UUID predioId, UUID documentoId);

    void eliminarDocumento(UUID predioId, UUID documentoId, String usuario);

    Map<String, Long> obtenerEstadisticas();

    List<RelacionContribuyenteResponse> listarPrediosPorContribuyente(UUID contribuyenteId);
}
