package pe.gob.municipalidad.sigm.modules.mercados.controller.web;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pe.gob.municipalidad.sigm.modules.mercados.dto.InspeccionDetailResponse;
import pe.gob.municipalidad.sigm.modules.mercados.dto.InspeccionResponse;
import pe.gob.municipalidad.sigm.modules.mercados.entity.MercadoMunicipal;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoInspeccion;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoInspeccionMercado;
import pe.gob.municipalidad.sigm.modules.mercados.repository.MercadoMunicipalRepository;
import pe.gob.municipalidad.sigm.modules.mercados.service.InspeccionMercadoService;
import pe.gob.municipalidad.sigm.modules.mercados.service.PlantillaInspeccionService;
import pe.gob.municipalidad.sigm.modules.mercados.service.SupervisionMercadoReporteService;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/dashboard/mercados/inspecciones")
public class MercadoInspeccionWebController {

    private final InspeccionMercadoService inspeccionService;
    private final PlantillaInspeccionService plantillaService;
    private final SupervisionMercadoReporteService reporteService;
    private final MercadoMunicipalRepository mercadoRepository;

    public MercadoInspeccionWebController(InspeccionMercadoService inspeccionService,
                                         PlantillaInspeccionService plantillaService,
                                         SupervisionMercadoReporteService reporteService,
                                         MercadoMunicipalRepository mercadoRepository) {
        this.inspeccionService = inspeccionService;
        this.plantillaService = plantillaService;
        this.reporteService = reporteService;
        this.mercadoRepository = mercadoRepository;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('MERCADOS_INSPECCION_VER') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public String listarInspecciones(
            @RequestParam(required = false) UUID mercadoId,
            @RequestParam(required = false) TipoInspeccionMercado tipo,
            @RequestParam(required = false) EstadoInspeccion estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            Model model) {

        Page<InspeccionResponse> inspeccionesPage = inspeccionService.buscarPaginado(
                mercadoId, null, null, null, null, tipo, estado, null, fechaDesde, fechaHasta, q, PageRequest.of(page, size));

        List<MercadoMunicipal> mercados = mercadoRepository.findAll();

        model.addAttribute("inspecciones", inspeccionesPage);
        model.addAttribute("mercados", mercados);
        model.addAttribute("tiposInspeccion", TipoInspeccionMercado.values());
        model.addAttribute("estadosInspeccion", EstadoInspeccion.values());
        model.addAttribute("mercadoId", mercadoId);
        model.addAttribute("tipoSeleccionado", tipo);
        model.addAttribute("estadoSeleccionado", estado);
        model.addAttribute("fechaDesde", fechaDesde);
        model.addAttribute("fechaHasta", fechaHasta);
        model.addAttribute("q", q);

        return "admin/mercados/inspecciones/listado";
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasAuthority('MERCADOS_INSPECCION_VER') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public String dashboardSupervision(Model model) {
        model.addAttribute("estadisticas", reporteService.obtenerEstadisticas());
        model.addAttribute("mercados", mercadoRepository.findAll());
        return "admin/mercados/inspecciones/dashboard";
    }

    @GetMapping("/calendario")
    @PreAuthorize("hasAuthority('MERCADOS_INSPECCION_VER') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public String calendarioInspecciones(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            Model model) {
        LocalDate fDesde = desde != null ? desde : LocalDate.now().minusDays(15);
        LocalDate fHasta = hasta != null ? hasta : LocalDate.now().plusDays(45);

        List<InspeccionResponse> calendario = inspeccionService.listarCalendario(fDesde, fHasta);
        model.addAttribute("inspecciones", calendario);
        model.addAttribute("desde", fDesde);
        model.addAttribute("hasta", fHasta);
        return "admin/mercados/inspecciones/calendario";
    }

    @GetMapping("/programar")
    @PreAuthorize("hasAuthority('MERCADOS_INSPECCION_CREAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public String formularioProgramar(Model model) {
        model.addAttribute("mercados", mercadoRepository.findAll());
        model.addAttribute("tiposInspeccion", TipoInspeccionMercado.values());
        model.addAttribute("plantillas", plantillaService.listarActivas());
        return "admin/mercados/inspecciones/formulario";
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('MERCADOS_INSPECCION_VER') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public String verDetalle(@PathVariable UUID id, Model model) {
        InspeccionDetailResponse detalle = inspeccionService.obtenerDetalle(id);
        model.addAttribute("detalle", detalle);
        model.addAttribute("inspeccion", detalle.getInspeccion());
        return "admin/mercados/inspecciones/detalle";
    }

    @GetMapping("/seguimiento")
    @PreAuthorize("hasAuthority('MERCADOS_INSPECCION_VER') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public String seguimientoMedidas(Model model) {
        model.addAttribute("mercados", mercadoRepository.findAll());
        return "admin/mercados/inspecciones/seguimiento";
    }

    @GetMapping("/reportes")
    @PreAuthorize("hasAuthority('MERCADOS_INSPECCION_REPORTES') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public String consolaReportes(Model model) {
        model.addAttribute("mercados", mercadoRepository.findAll());
        model.addAttribute("estadisticas", reporteService.obtenerEstadisticas());
        return "admin/mercados/inspecciones/reportes";
    }
}
