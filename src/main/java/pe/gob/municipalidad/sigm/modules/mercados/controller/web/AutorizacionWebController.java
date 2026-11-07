package pe.gob.municipalidad.sigm.modules.mercados.controller.web;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.common.repository.OficinaRepository;
import pe.gob.municipalidad.sigm.modules.mercados.dto.AutorizacionDetailResponse;
import pe.gob.municipalidad.sigm.modules.mercados.dto.AutorizacionResponse;
import pe.gob.municipalidad.sigm.modules.mercados.entity.AsignacionPuesto;
import pe.gob.municipalidad.sigm.modules.mercados.entity.Comerciante;
import pe.gob.municipalidad.sigm.modules.mercados.entity.MercadoMunicipal;
import pe.gob.municipalidad.sigm.modules.mercados.entity.TipoAutorizacion;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoAsignacion;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoAutorizacion;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoMercado;
import pe.gob.municipalidad.sigm.modules.mercados.repository.AsignacionPuestoRepository;
import pe.gob.municipalidad.sigm.modules.mercados.repository.ComercianteRepository;
import pe.gob.municipalidad.sigm.modules.mercados.repository.MercadoMunicipalRepository;
import pe.gob.municipalidad.sigm.modules.mercados.repository.TipoAutorizacionRepository;
import pe.gob.municipalidad.sigm.modules.mercados.service.AutorizacionComercialService;
import pe.gob.municipalidad.sigm.modules.mercados.service.VigenciaOcupacionService;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/dashboard/mercados/autorizaciones")
public class AutorizacionWebController {

    private final AutorizacionComercialService autorizacionService;
    private final VigenciaOcupacionService vigenciaService;
    private final MercadoMunicipalRepository mercadoRepository;
    private final TipoAutorizacionRepository tipoAutorizacionRepository;
    private final AsignacionPuestoRepository asignacionRepository;
    private final ComercianteRepository comercianteRepository;
    private final OficinaRepository oficinaRepository;

    public AutorizacionWebController(
            AutorizacionComercialService autorizacionService,
            VigenciaOcupacionService vigenciaService,
            MercadoMunicipalRepository mercadoRepository,
            TipoAutorizacionRepository tipoAutorizacionRepository,
            AsignacionPuestoRepository asignacionRepository,
            ComercianteRepository comercianteRepository,
            OficinaRepository oficinaRepository) {
        this.autorizacionService = autorizacionService;
        this.vigenciaService = vigenciaService;
        this.mercadoRepository = mercadoRepository;
        this.tipoAutorizacionRepository = tipoAutorizacionRepository;
        this.asignacionRepository = asignacionRepository;
        this.comercianteRepository = comercianteRepository;
        this.oficinaRepository = oficinaRepository;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('MERCADOS_AUTORIZACION_VER') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public String listar(
            @RequestParam(required = false) String termino,
            @RequestParam(required = false) UUID mercadoId,
            @RequestParam(required = false) EstadoAutorizacion estado,
            @RequestParam(required = false) String tipoAutorizacion,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        Page<AutorizacionResponse> pagina = autorizacionService.buscarConFiltros(
                termino, mercadoId, estado, tipoAutorizacion, fechaDesde, fechaHasta,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "fechaSolicitud")));

        List<MercadoMunicipal> mercados = mercadoRepository.findByEstado(EstadoMercado.ACTIVO);
        List<TipoAutorizacion> tiposAutorizacion = tipoAutorizacionRepository.findByEstadoOrderByNombreAsc("ACTIVO");

        model.addAttribute("autorizaciones", pagina);
        model.addAttribute("mercados", mercados);
        model.addAttribute("tiposAutorizacion", tiposAutorizacion);
        model.addAttribute("estados", EstadoAutorizacion.values());
        model.addAttribute("stats", vigenciaService.obtenerEstadisticas());

        model.addAttribute("termino", termino);
        model.addAttribute("mercadoIdSeleccionado", mercadoId);
        model.addAttribute("estadoSeleccionado", estado);
        model.addAttribute("tipoSeleccionado", tipoAutorizacion);
        model.addAttribute("fechaDesde", fechaDesde);
        model.addAttribute("fechaHasta", fechaHasta);

        model.addAttribute("moduloActivo", "mercados");
        model.addAttribute("submenuActivo", "autorizaciones");

        return "admin/mercados/autorizaciones/listado";
    }

    @GetMapping("/nueva")
    @PreAuthorize("hasAuthority('MERCADOS_AUTORIZACION_CREAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public String nueva(
            @RequestParam(required = false) UUID asignacionId,
            @RequestParam(required = false) UUID comercianteId,
            Model model) {

        List<AsignacionPuesto> asignacionesHabilitadas = new java.util.ArrayList<>(
                asignacionRepository.findByEstadoInConRelaciones(List.of(EstadoAsignacion.APROBADA, EstadoAsignacion.VIGENTE))
        );
        if (asignacionId != null && asignacionesHabilitadas.stream().noneMatch(a -> a.getId().equals(asignacionId))) {
            asignacionRepository.findByIdCompleto(asignacionId).ifPresent(asignacionesHabilitadas::add);
        }

        List<Comerciante> comerciantes = new java.util.ArrayList<>(comercianteRepository.findAllConRelaciones());
        if (comercianteId != null && comerciantes.stream().noneMatch(c -> c.getId().equals(comercianteId))) {
            comercianteRepository.findByIdConRelaciones(comercianteId).ifPresent(comerciantes::add);
        }
        List<TipoAutorizacion> tiposAutorizacion = tipoAutorizacionRepository.findByEstadoOrderByNombreAsc("ACTIVO");
        List<Oficina> oficinas = oficinaRepository.findAll();

        model.addAttribute("asignaciones", asignacionesHabilitadas);
        model.addAttribute("comerciantes", comerciantes);
        model.addAttribute("tiposAutorizacion", tiposAutorizacion);
        model.addAttribute("oficinas", oficinas);
        model.addAttribute("asignacionPreseleccionada", asignacionId);
        model.addAttribute("comerciantePreseleccionado", comercianteId);

        model.addAttribute("moduloActivo", "mercados");
        model.addAttribute("submenuActivo", "autorizaciones");

        return "admin/mercados/autorizaciones/formulario";
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('MERCADOS_AUTORIZACION_VER') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public String detalle(@PathVariable UUID id, Model model) {
        AutorizacionDetailResponse detalle = autorizacionService.obtenerDetalle(id);
        model.addAttribute("detalle", detalle);
        model.addAttribute("moduloActivo", "mercados");
        model.addAttribute("submenuActivo", "autorizaciones");
        return "admin/mercados/autorizaciones/detalle";
    }

    @GetMapping("/{id}/editar")
    @PreAuthorize("hasAuthority('MERCADOS_AUTORIZACION_CREAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public String editar(@PathVariable UUID id, Model model) {
        AutorizacionDetailResponse detalle = autorizacionService.obtenerDetalle(id);
        List<TipoAutorizacion> tiposAutorizacion = tipoAutorizacionRepository.findByEstadoOrderByNombreAsc("ACTIVO");
        List<Oficina> oficinas = oficinaRepository.findAll();

        model.addAttribute("autorizacion", detalle);
        model.addAttribute("tiposAutorizacion", tiposAutorizacion);
        model.addAttribute("oficinas", oficinas);

        model.addAttribute("moduloActivo", "mercados");
        model.addAttribute("submenuActivo", "autorizaciones");
        return "admin/mercados/autorizaciones/formulario";
    }
}
