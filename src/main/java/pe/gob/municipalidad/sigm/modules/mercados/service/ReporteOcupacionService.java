package pe.gob.municipalidad.sigm.modules.mercados.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pe.gob.municipalidad.sigm.modules.mercados.dto.IndicadorOcupacionResponse;
import pe.gob.municipalidad.sigm.modules.mercados.dto.PuestoReporteItem;
import pe.gob.municipalidad.sigm.modules.mercados.dto.ReporteMercadoFilter;

import java.util.List;

public interface ReporteOcupacionService {
    IndicadorOcupacionResponse obtenerIndicadorOcupacion(ReporteMercadoFilter filtro);
    Page<PuestoReporteItem> listarPuestos(ReporteMercadoFilter filtro, Pageable pageable);
    List<PuestoReporteItem> listarTodosPuestos(ReporteMercadoFilter filtro);
}
