package pe.gob.municipalidad.sigm.modules.mercados.controller.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pe.gob.municipalidad.sigm.modules.mercados.dto.*;
import pe.gob.municipalidad.sigm.modules.mercados.entity.MercadoMunicipal;
import pe.gob.municipalidad.sigm.modules.mercados.enums.PeriodoConsulta;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoReporteMercado;
import pe.gob.municipalidad.sigm.modules.mercados.repository.MercadoMunicipalRepository;
import pe.gob.municipalidad.sigm.modules.mercados.repository.SectorMercadoRepository;
import pe.gob.municipalidad.sigm.modules.mercados.service.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@Controller
@RequestMapping("/dashboard/mercados/reportes")
public class ReporteMercadosWebController {

    private final IndicadoresMercadosService indicadoresService;
    private final ReporteOcupacionService reporteOcupacionService;
    private final ReporteMercadosService reporteMercadosService;
    private final ReporteRecaudacionService reporteRecaudacionService;
    private final ReporteOperativoService reporteOperativoService;
    private final HistorialExportacionService historialExportacionService;
    private final MercadoMunicipalRepository mercadoRepository;
    private final SectorMercadoRepository sectorRepository;

    public ReporteMercadosWebController(
            IndicadoresMercadosService indicadoresService,
            ReporteOcupacionService reporteOcupacionService,
            ReporteMercadosService reporteMercadosService,
            ReporteRecaudacionService reporteRecaudacionService,
            ReporteOperativoService reporteOperativoService,
            HistorialExportacionService historialExportacionService,
            MercadoMunicipalRepository mercadoRepository,
            SectorMercadoRepository sectorRepository) {
        this.indicadoresService = indicadoresService;
        this.reporteOcupacionService = reporteOcupacionService;
        this.reporteMercadosService = reporteMercadosService;
        this.reporteRecaudacionService = reporteRecaudacionService;
        this.reporteOperativoService = reporteOperativoService;
        this.historialExportacionService = historialExportacionService;
        this.mercadoRepository = mercadoRepository;
        this.sectorRepository = sectorRepository;
    }

    private void agregarMercadosYPeriodos(Model model) {
        List<MercadoMunicipal> mercados = mercadoRepository.findByEstado(pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoMercado.ACTIVO);
        model.addAttribute("mercados", mercados);
        model.addAttribute("periodos", PeriodoConsulta.values());
        model.addAttribute("tiposReporte", TipoReporteMercado.values());
    }

    @GetMapping
    @PreAuthorize("hasAuthority('MERCADOS_REPORTES_VER') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public String index(Model model, Authentication authentication) {
        agregarMercadosYPeriodos(model);

        ReporteMercadoFilter filtro = ReporteMercadoFilter.builder().periodo(PeriodoConsulta.MES_ACTUAL).build();
        DashboardMercadosResponse kpis = indicadoresService.obtenerDashboard(filtro);
        model.addAttribute("kpis", kpis);

        String username = authentication != null ? authentication.getName() : "sistema";
        boolean esAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SUPERADMIN"));
        Page<HistorialExportacionResponse> ultimasExportaciones = historialExportacionService.listarHistorial(
                PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "fechaGeneracion")), username, esAdmin);
        model.addAttribute("ultimasExportaciones", ultimasExportaciones.getContent());

        return "admin/mercados/reportes/index";
    }

    @GetMapping("/ejecutivo")
    @PreAuthorize("hasAuthority('MERCADOS_REPORTES_VER') or hasAuthority('MERCADOS_REPORTES_EJECUTIVO') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public String dashboardEjecutivo(
            ReporteMercadoFilter filtro,
            Model model) {
        agregarMercadosYPeriodos(model);
        if (filtro.getPeriodo() == null) {
            filtro.setPeriodo(PeriodoConsulta.MES_ACTUAL);
        }
        DashboardMercadosResponse data = indicadoresService.obtenerDashboard(filtro);
        model.addAttribute("data", data);
        model.addAttribute("filtro", filtro);
        return "admin/mercados/reportes/ejecutivo";
    }

    @GetMapping("/ocupacion")
    @PreAuthorize("hasAuthority('MERCADOS_REPORTES_VER') or hasAuthority('MERCADOS_REPORTES_OCUPACION') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public String reporteOcupacion(
            ReporteMercadoFilter filtro,
            Model model) {
        agregarMercadosYPeriodos(model);
        IndicadorOcupacionResponse ocupacion = reporteOcupacionService.obtenerIndicadorOcupacion(filtro);
        model.addAttribute("ocupacion", ocupacion);
        model.addAttribute("filtro", filtro);
        return "admin/mercados/reportes/ocupacion";
    }

    @GetMapping("/puestos")
    @PreAuthorize("hasAuthority('MERCADOS_REPORTES_VER') or hasAuthority('MERCADOS_REPORTES_OCUPACION') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public String reportePuestos(
            ReporteMercadoFilter filtro,
            @PageableDefault(size = 15) Pageable pageable,
            Model model) {
        agregarMercadosYPeriodos(model);
        Page<PuestoReporteItem> pagina = reporteOcupacionService.listarPuestos(filtro, pageable);
        model.addAttribute("pagina", pagina);
        model.addAttribute("filtro", filtro);
        return "admin/mercados/reportes/puestos";
    }

    @GetMapping("/comerciantes")
    @PreAuthorize("hasAuthority('MERCADOS_REPORTES_VER') or hasAuthority('MERCADOS_REPORTES_COMERCIANTES') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public String reporteComerciantes(
            ReporteMercadoFilter filtro,
            @PageableDefault(size = 15) Pageable pageable,
            Model model,
            Authentication authentication) {
        agregarMercadosYPeriodos(model);
        boolean puedeVerDoc = tienePermisoDocumentos(authentication);
        Page<ComercianteReporteItem> pagina = reporteMercadosService.listarComerciantes(filtro, pageable, puedeVerDoc);
        model.addAttribute("pagina", pagina);
        model.addAttribute("filtro", filtro);
        model.addAttribute("puedeVerDoc", puedeVerDoc);
        return "admin/mercados/reportes/comerciantes";
    }

    @GetMapping("/contratos")
    @PreAuthorize("hasAuthority('MERCADOS_REPORTES_VER') or hasAuthority('MERCADOS_REPORTES_CONTRATOS') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public String reporteContratos(
            ReporteMercadoFilter filtro,
            @PageableDefault(size = 15) Pageable pageable,
            Model model) {
        agregarMercadosYPeriodos(model);
        Page<ContratoReporteItem> pagina = reporteMercadosService.listarContratos(filtro, pageable);
        model.addAttribute("pagina", pagina);
        model.addAttribute("filtro", filtro);
        return "admin/mercados/reportes/contratos";
    }

    @GetMapping("/autorizaciones")
    @PreAuthorize("hasAuthority('MERCADOS_REPORTES_VER') or hasAuthority('MERCADOS_REPORTES_CONTRATOS') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public String reporteAutorizaciones(
            ReporteMercadoFilter filtro,
            @PageableDefault(size = 15) Pageable pageable,
            Model model) {
        agregarMercadosYPeriodos(model);
        Page<ContratoReporteItem> pagina = reporteMercadosService.listarAutorizaciones(filtro, pageable);
        model.addAttribute("pagina", pagina);
        model.addAttribute("filtro", filtro);
        return "admin/mercados/reportes/autorizaciones";
    }

    @GetMapping("/vencimientos")
    @PreAuthorize("hasAuthority('MERCADOS_REPORTES_VER') or hasAuthority('MERCADOS_REPORTES_CONTRATOS') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public String reporteVencimientos(
            ReporteMercadoFilter filtro,
            @PageableDefault(size = 15) Pageable pageable,
            Model model) {
        agregarMercadosYPeriodos(model);
        Page<ContratoReporteItem> pagina = reporteMercadosService.listarVencimientos(filtro, pageable);
        model.addAttribute("pagina", pagina);
        model.addAttribute("filtro", filtro);
        return "admin/mercados/reportes/vencimientos";
    }

    @GetMapping("/recaudacion")
    @PreAuthorize("hasAuthority('MERCADOS_REPORTES_VER') or hasAuthority('MERCADOS_REPORTES_FINANZAS') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public String reporteRecaudacion(
            ReporteMercadoFilter filtro,
            @PageableDefault(size = 15) Pageable pageable,
            Model model) {
        agregarMercadosYPeriodos(model);
        if (filtro.getPeriodo() == null) {
            filtro.setPeriodo(PeriodoConsulta.MES_ACTUAL);
        }
        Page<RecaudacionReporteItem> pagina = reporteRecaudacionService.listarRecaudacion(filtro, pageable);
        model.addAttribute("pagina", pagina);
        model.addAttribute("filtro", filtro);
        return "admin/mercados/reportes/recaudacion";
    }

    @GetMapping("/deudas")
    @PreAuthorize("hasAuthority('MERCADOS_REPORTES_VER') or hasAuthority('MERCADOS_REPORTES_FINANZAS') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public String reporteDeudas(
            ReporteMercadoFilter filtro,
            @PageableDefault(size = 15) Pageable pageable,
            Model model) {
        agregarMercadosYPeriodos(model);
        Page<DeudaReporteItem> pagina = reporteRecaudacionService.listarDeudas(filtro, pageable);
        var resumen = reporteRecaudacionService.obtenerResumenDeudas(filtro);
        model.addAttribute("pagina", pagina);
        model.addAttribute("resumen", resumen);
        model.addAttribute("filtro", filtro);
        return "admin/mercados/reportes/deudas";
    }

    @GetMapping("/inspecciones")
    @PreAuthorize("hasAuthority('MERCADOS_REPORTES_VER') or hasAuthority('MERCADOS_REPORTES_INSPECCIONES') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public String reporteInspecciones(
            ReporteMercadoFilter filtro,
            @PageableDefault(size = 15) Pageable pageable,
            Model model) {
        agregarMercadosYPeriodos(model);
        Page<InspeccionReporteItem> pagina = reporteOperativoService.listarInspecciones(filtro, pageable);
        model.addAttribute("pagina", pagina);
        model.addAttribute("filtro", filtro);
        return "admin/mercados/reportes/inspecciones";
    }

    @GetMapping("/incidencias")
    @PreAuthorize("hasAuthority('MERCADOS_REPORTES_VER') or hasAuthority('MERCADOS_REPORTES_INCIDENCIAS') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public String reporteIncidencias(
            ReporteMercadoFilter filtro,
            @PageableDefault(size = 15) Pageable pageable,
            Model model) {
        agregarMercadosYPeriodos(model);
        Page<IncidenciaReporteItem> pagina = reporteOperativoService.listarIncidencias(filtro, pageable);
        model.addAttribute("pagina", pagina);
        model.addAttribute("filtro", filtro);
        return "admin/mercados/reportes/incidencias";
    }

    @GetMapping("/renovaciones")
    @PreAuthorize("hasAuthority('MERCADOS_REPORTES_VER') or hasAuthority('MERCADOS_REPORTES_CONTRATOS') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public String reporteRenovaciones(
            ReporteMercadoFilter filtro,
            @PageableDefault(size = 15) Pageable pageable,
            Model model) {
        agregarMercadosYPeriodos(model);
        Page<RenovacionReporteItem> pagina = reporteOperativoService.listarRenovaciones(filtro, pageable);
        model.addAttribute("pagina", pagina);
        model.addAttribute("filtro", filtro);
        return "admin/mercados/reportes/renovaciones";
    }

    @GetMapping("/exportaciones")
    @PreAuthorize("hasAuthority('MERCADOS_REPORTES_HISTORIAL_VER') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public String historialExportaciones(
            @PageableDefault(size = 20, sort = "fechaGeneracion", direction = Sort.Direction.DESC) Pageable pageable,
            Model model,
            Authentication authentication) {
        agregarMercadosYPeriodos(model);
        String username = authentication != null ? authentication.getName() : "sistema";
        boolean esAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SUPERADMIN"));

        Page<HistorialExportacionResponse> pagina = historialExportacionService.listarHistorial(pageable, username, esAdmin);
        model.addAttribute("pagina", pagina);
        return "admin/mercados/reportes/exportaciones";
    }

    private boolean tienePermisoDocumentos(Authentication authentication) {
        if (authentication == null) return false;
        return authentication.getAuthorities().stream().anyMatch(a ->
                a.getAuthority().equals("ROLE_ADMIN") ||
                a.getAuthority().equals("ROLE_SUPERADMIN") ||
                a.getAuthority().equals("MERCADOS_COMERCIANTE_VER") ||
                a.getAuthority().equals("MERCADOS_REPORTES_COMERCIANTES"));
    }
}
