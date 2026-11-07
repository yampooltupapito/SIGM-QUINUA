package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response.*;

import java.util.Map;
import java.util.UUID;

/**
 * Servicio de negocio para la administración y control del Padrón de Contribuyentes Municipales.
 */
public interface ContribuyenteService {

    ContribuyenteDetailResponse registrarContribuyente(ContribuyenteCreateRequest request, String usuario);

    ContribuyenteDetailResponse actualizarContribuyente(UUID id, ContribuyenteUpdateRequest request, String usuario);

    ContribuyenteDetailResponse cambiarEstado(UUID id, CambioEstadoContribuyenteRequest request, String usuario);

    ContribuyenteDetailResponse obtenerDetalle(UUID id);

    Page<ContribuyenteSummaryDto> listarConFiltros(ContribuyenteFilterDto filtro, Pageable pageable);

    DuplicateCheckResponse verificarDuplicados(String tipoDocumento, String numeroDocumento, String nombreRazonSocial, UUID excludeId);

    RepresentanteResponse agregarRepresentante(UUID contribuyenteId, RepresentanteContribuyenteRequest request, String usuario);

    void eliminarRepresentante(UUID contribuyenteId, UUID representanteId, String usuario);

    DomicilioResponse agregarDomicilio(UUID contribuyenteId, DomicilioContribuyenteRequest request, String usuario);

    void eliminarDomicilio(UUID contribuyenteId, UUID domicilioId, String usuario);

    ContactoResponse agregarContacto(UUID contribuyenteId, ContactoContribuyenteRequest request, String usuario);

    void eliminarContacto(UUID contribuyenteId, UUID contactoId, String usuario);

    Map<String, Object> obtenerEstadisticas();
}
