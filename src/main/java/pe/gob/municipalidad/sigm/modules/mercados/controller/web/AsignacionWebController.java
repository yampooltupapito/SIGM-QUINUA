package pe.gob.municipalidad.sigm.modules.mercados.controller.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pe.gob.municipalidad.sigm.modules.mercados.dto.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoAsignacion;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoAsignacion;
import pe.gob.municipalidad.sigm.modules.mercados.repository.MercadoMunicipalRepository;
import pe.gob.municipalidad.sigm.modules.mercados.service.AsignacionPuestoService;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Controlador MVC para las interfaces Thymeleaf de asignación y control de ocupación de puestos.
 */
@Slf4j
@Controller
@RequestMapping("/dashboard/mercados/asignaciones")
public class AsignacionWebController {

    private final AsignacionPuestoService asignacionService;
    private final MercadoMunicipalRepository mercadoRepository;

    public AsignacionWebController(
            AsignacionPuestoService asignacionService,
            MercadoMunicipalRepository mercadoRepository) {
        this.asignacionService = asignacionService;
        this.mercadoRepository = mercadoRepository;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('MERCADOS_ASIGNACION_VER') or hasAuthority('MERCADOS_ASIGNACION_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'OPERADOR', 'JEFE_OFICINA')")
    public String listarAsignaciones(
            @RequestParam(required = false) String termino,
            @RequestParam(required = false) UUID mercadoId,
            @RequestParam(required = false) UUID sectorId,
            @RequestParam(required = false) UUID pabellonId,
            @RequestParam(required = false) UUID comercianteId,
            @RequestParam(required = false) EstadoAsignacion estado,
            @RequestParam(required = false) TipoAsignacion tipoAsignacion,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicioDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicioHasta,
            @PageableDefault(size = 15, sort = "fechaSolicitud", direction = Sort.Direction.DESC) Pageable pageable,
            Model model) {

        AsignacionFilter filter = AsignacionFilter.builder()
                .termino(termino)
                .mercadoId(mercadoId)
                .sectorId(sectorId)
                .pabellonId(pabellonId)
                .comercianteId(comercianteId)
                .estado(estado)
                .tipoAsignacion(tipoAsignacion)
                .fechaInicioDesde(fechaInicioDesde)
                .fechaInicioHasta(fechaInicioHasta)
                .build();

        Page<AsignacionResponse> asignaciones = asignacionService.listarConFiltros(filter, pageable);
        AsignacionEstadisticasResponse stats = asignacionService.obtenerEstadisticas();

        model.addAttribute("asignaciones", asignaciones);
        model.addAttribute("stats", stats);
        model.addAttribute("mercados", mercadoRepository.findAll());
        model.addAttribute("estados", EstadoAsignacion.values());
        model.addAttribute("tiposAsignacion", TipoAsignacion.values());
        model.addAttribute("termino", termino);
        model.addAttribute("mercadoIdSeleccionado", mercadoId);
        model.addAttribute("sectorIdSeleccionado", sectorId);
        model.addAttribute("pabellonIdSeleccionado", pabellonId);
        model.addAttribute("comercianteIdSeleccionado", comercianteId);
        model.addAttribute("estadoSeleccionado", estado);
        model.addAttribute("tipoSeleccionado", tipoAsignacion);
        model.addAttribute("fechaDesde", fechaInicioDesde);
        model.addAttribute("fechaHasta", fechaInicioHasta);

        return "admin/mercados/asignaciones/listado";
    }

    @GetMapping("/nueva")
    @PreAuthorize("hasAuthority('MERCADOS_ASIGNACION_CREAR') or hasAuthority('MERCADOS_ASIGNACION_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public String formularioNueva(Model model) {
        model.addAttribute("mercados", mercadoRepository.findAll());
        model.addAttribute("tiposAsignacion", TipoAsignacion.values());
        model.addAttribute("esEdicion", false);
        model.addAttribute("asignacion", new AsignacionDetailResponse());
        return "admin/mercados/asignaciones/formulario";
    }

    @GetMapping("/{id}/editar")
    @PreAuthorize("hasAuthority('MERCADOS_ASIGNACION_CREAR') or hasAuthority('MERCADOS_ASIGNACION_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public String formularioEditar(@PathVariable UUID id, Model model) {
        AsignacionDetailResponse asignacion = asignacionService.obtenerPorIdCompleto(id);
        model.addAttribute("asignacion", asignacion);
        model.addAttribute("mercados", mercadoRepository.findAll());
        model.addAttribute("tiposAsignacion", TipoAsignacion.values());
        model.addAttribute("esEdicion", true);
        return "admin/mercados/asignaciones/formulario";
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('MERCADOS_ASIGNACION_VER') or hasAuthority('MERCADOS_ASIGNACION_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'OPERADOR', 'JEFE_OFICINA')")
    public String verDetalle(@PathVariable UUID id, Model model) {
        AsignacionDetailResponse asignacion = asignacionService.obtenerPorIdCompleto(id);
        model.addAttribute("asignacion", asignacion);
        return "admin/mercados/asignaciones/detalle";
    }
}
