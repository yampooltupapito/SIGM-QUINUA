package pe.gob.municipalidad.sigm.modules.mercados.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pe.gob.municipalidad.sigm.modules.mercados.dto.IncidenciaReporteItem;
import pe.gob.municipalidad.sigm.modules.mercados.dto.InspeccionReporteItem;
import pe.gob.municipalidad.sigm.modules.mercados.dto.RenovacionReporteItem;
import pe.gob.municipalidad.sigm.modules.mercados.dto.ReporteMercadoFilter;

import java.util.List;

public interface ReporteOperativoService {
    Page<InspeccionReporteItem> listarInspecciones(ReporteMercadoFilter filtro, Pageable pageable);
    List<InspeccionReporteItem> listarTodasInspecciones(ReporteMercadoFilter filtro);
    Page<IncidenciaReporteItem> listarIncidencias(ReporteMercadoFilter filtro, Pageable pageable);
    List<IncidenciaReporteItem> listarTodasIncidencias(ReporteMercadoFilter filtro);
    Page<RenovacionReporteItem> listarRenovaciones(ReporteMercadoFilter filtro, Pageable pageable);
    List<RenovacionReporteItem> listarTodasRenovaciones(ReporteMercadoFilter filtro);
}
