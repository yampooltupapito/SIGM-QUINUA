package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ObligacionTributaria;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public interface ObligacionTributariaService {

    Page<ObligacionSummaryDto> listarObligaciones(ObligacionFilterDto filter, Pageable pageable);

    ObligacionDetailResponse obtenerPorId(UUID id);

    ObligacionDetailResponse generarDesdeDeterminacion(GeneracionObligacionRequest request, String usuario);

    PrevisualizacionLoteDto previsualizarLote(GeneracionLoteRequest request);

    LoteGeneracionResponse generarLote(GeneracionLoteRequest request, String usuario);

    ObligacionDetailResponse validarObligacion(UUID id, String resolucionSustento, String usuario);

    ObligacionDetailResponse suspenderObligacion(UUID id, AccionObligacionRequest request, String usuario);

    ObligacionDetailResponse rectificarObligacion(UUID id, AccionObligacionRequest request, String usuario);

    ObligacionDetailResponse anularObligacion(UUID id, AccionObligacionRequest request, String usuario);

    Map<String, Object> obtenerMetricasGenerales();
}
