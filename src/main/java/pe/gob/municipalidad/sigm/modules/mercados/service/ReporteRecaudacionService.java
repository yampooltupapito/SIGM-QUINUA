package pe.gob.municipalidad.sigm.modules.mercados.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pe.gob.municipalidad.sigm.modules.mercados.dto.DeudaReporteItem;
import pe.gob.municipalidad.sigm.modules.mercados.dto.RecaudacionReporteItem;
import pe.gob.municipalidad.sigm.modules.mercados.dto.ReporteMercadoFilter;

import java.util.List;
import java.util.Map;

public interface ReporteRecaudacionService {
    Page<RecaudacionReporteItem> listarRecaudacion(ReporteMercadoFilter filtro, Pageable pageable);
    List<RecaudacionReporteItem> listarTodaRecaudacion(ReporteMercadoFilter filtro);
    Page<DeudaReporteItem> listarDeudas(ReporteMercadoFilter filtro, Pageable pageable);
    List<DeudaReporteItem> listarTodasDeudas(ReporteMercadoFilter filtro);
    Map<String, Object> obtenerResumenDeudas(ReporteMercadoFilter filtro);
}
