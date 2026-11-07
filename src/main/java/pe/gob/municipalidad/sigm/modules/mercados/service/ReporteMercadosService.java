package pe.gob.municipalidad.sigm.modules.mercados.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pe.gob.municipalidad.sigm.modules.mercados.dto.ComercianteReporteItem;
import pe.gob.municipalidad.sigm.modules.mercados.dto.ContratoReporteItem;
import pe.gob.municipalidad.sigm.modules.mercados.dto.ReporteMercadoFilter;

import java.util.List;

public interface ReporteMercadosService {
    Page<ComercianteReporteItem> listarComerciantes(ReporteMercadoFilter filtro, Pageable pageable, boolean puedeVerDocumentos);
    List<ComercianteReporteItem> listarTodosComerciantes(ReporteMercadoFilter filtro, boolean puedeVerDocumentos);
    Page<ContratoReporteItem> listarContratos(ReporteMercadoFilter filtro, Pageable pageable);
    List<ContratoReporteItem> listarTodosContratos(ReporteMercadoFilter filtro);
    Page<ContratoReporteItem> listarAutorizaciones(ReporteMercadoFilter filtro, Pageable pageable);
    List<ContratoReporteItem> listarTodasAutorizaciones(ReporteMercadoFilter filtro);
    Page<ContratoReporteItem> listarVencimientos(ReporteMercadoFilter filtro, Pageable pageable);
    List<ContratoReporteItem> listarTodosVencimientos(ReporteMercadoFilter filtro);
}
