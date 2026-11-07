package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.NormaTributariaConfigurada;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.PeriodoTributario;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ConceptoTributarioService {

    ConceptoDetailResponse registrarConcepto(ConceptoCreateRequest request, String usuario);

    ConceptoDetailResponse obtenerConceptoPorId(UUID id);

    ConceptoDetailResponse obtenerConceptoPorCodigo(String codigo);

    ConceptoDetailResponse actualizarConcepto(UUID id, ConceptoUpdateRequest request, String usuario);

    Page<ConceptoSummaryDto> listarConceptos(ConceptoFilterDto filtro, Pageable pageable);

    List<ConceptoSummaryDto> listarTodosConceptos(ConceptoFilterDto filtro);

    ConfiguracionDetailResponse crearConfiguracion(ConfiguracionCreateRequest request, String usuario);

    ConfiguracionDetailResponse obtenerConfiguracionPorId(UUID configuracionId);

    ConfiguracionDetailResponse agregarParametro(UUID configuracionId, ParametroCreateRequest request, String usuario);

    ConfiguracionDetailResponse eliminarParametro(UUID configuracionId, UUID parametroId, String usuario);

    ConfiguracionDetailResponse enviarARevision(UUID configuracionId, String motivo, String usuario);

    ConfiguracionDetailResponse aprobarConfiguracion(UUID configuracionId, String resolucion, String motivo, String usuario);

    ConfiguracionDetailResponse activarConfiguracion(UUID configuracionId, String motivo, String usuario);

    ConfiguracionDetailResponse desactivarConfiguracion(UUID configuracionId, String motivo, String usuario);

    ConfiguracionDetailResponse crearNuevaVersion(UUID configuracionId, String motivo, String usuario);

    ComparacionVersionesResponse compararVersiones(UUID versionBaseId, UUID versionComparadaId);

    List<ConfiguracionSummaryDto> listarConfiguracionesPorConcepto(UUID conceptoId);

    NormaDetailResponse registrarNorma(NormaCreateRequest request, String usuario);

    List<NormaSummaryDto> listarNormas();

    List<PeriodoSummaryDto> listarPeriodos();

    CalendarioCuotaResponse agregarCuotaCalendario(UUID conceptoId, UUID periodoId, CalendarioCuotaRequest request, String usuario);

    List<CalendarioCuotaResponse> listarCalendarioPorConceptoYPeriodo(UUID conceptoId, UUID periodoId);

    Map<String, Object> obtenerEstadisticas();
}
