package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response.*;

public interface RecaudacionTributariaService {

    DashboardTributarioResponse obtenerDashboard();

    Page<ContribuyenteSummaryDto> listarContribuyentes(String q, Pageable pageable);

    Page<PredioSummaryDto> listarPredios(String q, Pageable pageable);

    Page<ConceptoSummaryDto> listarConceptos(Pageable pageable);

    Page<PeriodoSummaryDto> listarPeriodos(Pageable pageable);

    Page<NormaSummaryDto> listarNormas(Pageable pageable);
}
