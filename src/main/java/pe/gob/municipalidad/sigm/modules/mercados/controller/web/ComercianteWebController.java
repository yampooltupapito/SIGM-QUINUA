package pe.gob.municipalidad.sigm.modules.mercados.controller.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pe.gob.municipalidad.sigm.modules.mercados.dto.ComercianteDetailResponse;
import pe.gob.municipalidad.sigm.modules.mercados.dto.ComercianteEstadisticasResponse;
import pe.gob.municipalidad.sigm.modules.mercados.dto.ComercianteFilter;
import pe.gob.municipalidad.sigm.modules.mercados.dto.ComercianteResponse;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoComerciante;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoComerciante;
import pe.gob.municipalidad.sigm.modules.mercados.repository.ActividadComercialRepository;
import pe.gob.municipalidad.sigm.modules.mercados.repository.MercadoMunicipalRepository;
import pe.gob.municipalidad.sigm.modules.mercados.service.ComercianteConsultaService;
import pe.gob.municipalidad.sigm.modules.mercados.service.ComercianteService;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Controlador MVC para las interfaces Thymeleaf de administración de comerciantes municipales.
 */
@Slf4j
@Controller
@RequestMapping("/dashboard/mercados/comerciantes")
public class ComercianteWebController {

    private final ComercianteService comercianteService;
    private final ComercianteConsultaService consultaService;
    private final ActividadComercialRepository actividadRepository;
    private final MercadoMunicipalRepository mercadoRepository;

    public ComercianteWebController(
            ComercianteService comercianteService,
            ComercianteConsultaService consultaService,
            ActividadComercialRepository actividadRepository,
            MercadoMunicipalRepository mercadoRepository) {
        this.comercianteService = comercianteService;
        this.consultaService = consultaService;
        this.actividadRepository = actividadRepository;
        this.mercadoRepository = mercadoRepository;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('MERCADOS_COMERCIANTE_VER') or hasAuthority('MERCADOS_COMERCIANTE_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'OPERADOR', 'JEFE_OFICINA')")
    public String listarComerciantes(
            @RequestParam(required = false) String termino,
            @RequestParam(required = false) TipoComerciante tipoComerciante,
            @RequestParam(required = false) EstadoComerciante estado,
            @RequestParam(required = false) Long actividadId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(required = false) UUID mercadoId,
            @RequestParam(defaultValue = "tabla") String vista,
            @PageableDefault(size = 15, sort = "codigoComerciante") Pageable pageable,
            Model model) {

        ComercianteFilter filter = ComercianteFilter.builder()
                .termino(termino)
                .tipoComerciante(tipoComerciante)
                .estado(estado)
                .actividadId(actividadId)
                .fechaDesde(fechaDesde)
                .fechaHasta(fechaHasta)
                .mercadoId(mercadoId)
                .build();

        Page<ComercianteResponse> pagina = comercianteService.listarComerciantes(filter, pageable);
        ComercianteEstadisticasResponse stats = comercianteService.obtenerEstadisticas();

        model.addAttribute("moduloActivo", "Mercados Municipales");
        model.addAttribute("submenuActivo", "comerciantes");
        model.addAttribute("comerciantes", pagina.getContent());
        model.addAttribute("pagina", pagina);
        model.addAttribute("filter", filter);
        model.addAttribute("vista", vista);
        model.addAttribute("stats", stats);
        model.addAttribute("actividades", actividadRepository.findAll());
        model.addAttribute("mercados", mercadoRepository.findAll());
        model.addAttribute("tiposComerciante", TipoComerciante.values());
        model.addAttribute("estadosComerciante", EstadoComerciante.values());

        return "admin/mercados/comerciantes/listado";
    }

    @GetMapping("/nuevo")
    @PreAuthorize("hasAuthority('MERCADOS_COMERCIANTE_CREAR') or hasAuthority('MERCADOS_COMERCIANTE_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'OPERADOR')")
    public String nuevoComerciante(Model model) {
        model.addAttribute("moduloActivo", "Mercados Municipales");
        model.addAttribute("submenuActivo", "comerciantes");
        model.addAttribute("esEdicion", false);
        model.addAttribute("actividades", actividadRepository.findAll());
        model.addAttribute("tiposComerciante", TipoComerciante.values());
        model.addAttribute("estadosComerciante", EstadoComerciante.values());
        return "admin/mercados/comerciantes/formulario";
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('MERCADOS_COMERCIANTE_VER') or hasAuthority('MERCADOS_COMERCIANTE_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'OPERADOR', 'JEFE_OFICINA')")
    public String detalleComerciante(@PathVariable UUID id, Model model) {
        ComercianteDetailResponse detalle = comercianteService.obtenerDetalleCompleto(id);

        model.addAttribute("moduloActivo", "Mercados Municipales");
        model.addAttribute("submenuActivo", "comerciantes");
        model.addAttribute("comerciante", detalle);
        model.addAttribute("estadosComerciante", EstadoComerciante.values());

        return "admin/mercados/comerciantes/detalle";
    }

    @GetMapping("/{id}/editar")
    @PreAuthorize("hasAuthority('MERCADOS_COMERCIANTE_EDITAR') or hasAuthority('MERCADOS_COMERCIANTE_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public String editarComerciante(@PathVariable UUID id, Model model) {
        ComercianteDetailResponse detalle = comercianteService.obtenerDetalleCompleto(id);

        model.addAttribute("moduloActivo", "Mercados Municipales");
        model.addAttribute("submenuActivo", "comerciantes");
        model.addAttribute("comerciante", detalle);
        model.addAttribute("esEdicion", true);
        model.addAttribute("actividades", actividadRepository.findAll());

        return "admin/mercados/comerciantes/formulario";
    }
}
