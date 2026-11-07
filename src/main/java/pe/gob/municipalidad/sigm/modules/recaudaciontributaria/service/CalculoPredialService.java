package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request.CalculoPredialCreateRequest;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request.CalculoPredialFilterDto;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request.SimulacionPredialRequest;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response.CalculoPredialDetailResponse;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response.CalculoPredialSummaryDto;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response.HistorialCalculoResponse;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface CalculoPredialService {

    /**
     * Realiza una simulación técnica y explicable del Impuesto Predial sin persistirla como deuda ni cálculo definitivo.
     */
    CalculoPredialDetailResponse simularCalculo(SimulacionPredialRequest request, String usuario);

    /**
     * Ejecuta y persiste la determinación formal de Impuesto Predial en estado PRELIMINAR.
     */
    CalculoPredialDetailResponse registrarCalculo(CalculoPredialCreateRequest request, String usuario);

    /**
     * Obtiene el detalle explicable completo de un cálculo por su identificador UUID.
     */
    CalculoPredialDetailResponse obtenerCalculoPorId(UUID id);

    /**
     * Obtiene un cálculo por su código de determinación unívoco.
     */
    CalculoPredialDetailResponse obtenerCalculoPorCodigo(String codigo);

    /**
     * Lista cálculos y simulaciones con paginación y filtros multicriterio.
     */
    Page<CalculoPredialSummaryDto> listarCalculos(CalculoPredialFilterDto filtro, Pageable pageable);

    /**
     * Lista completa sin paginación para exportaciones institucionales.
     */
    List<CalculoPredialSummaryDto> listarTodosCalculos(CalculoPredialFilterDto filtro);

    /**
     * Eleva un cálculo preliminar o con observaciones a revisión técnica y legal.
     */
    CalculoPredialDetailResponse enviarARevision(UUID id, String motivo, String usuario);

    /**
     * Asienta observaciones técnicas devolviendo el cálculo para ajuste.
     */
    CalculoPredialDetailResponse registrarObservaciones(UUID id, String observaciones, String usuario);

    /**
     * Aprueba formalmente la determinación del impuesto predial.
     */
    CalculoPredialDetailResponse aprobarCalculo(UUID id, String resolucion, String motivo, String usuario);

    /**
     * Anula motivadamente un cálculo tributario predial.
     */
    CalculoPredialDetailResponse anularCalculo(UUID id, String motivo, String usuario);

    /**
     * Genera una nueva versión de recálculo (v(N+1)) a partir de un cálculo previo.
     */
    CalculoPredialDetailResponse recalcularNuevaVersion(UUID calculoOriginalId, String motivo, String usuario);

    /**
     * Consulta el historial de auditoría y transiciones de un cálculo predial.
     */
    List<HistorialCalculoResponse> obtenerHistorial(UUID calculoId);

    /**
     * Métricas estadísticas del motor de cálculo predial para el panel de control.
     */
    Map<String, Object> obtenerEstadisticas();
}
