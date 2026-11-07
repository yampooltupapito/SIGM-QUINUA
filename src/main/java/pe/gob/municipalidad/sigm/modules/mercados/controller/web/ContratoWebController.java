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
import pe.gob.municipalidad.sigm.modules.mercados.dto.ContratoDetailResponse;
import pe.gob.municipalidad.sigm.modules.mercados.dto.ContratoResponse;
import pe.gob.municipalidad.sigm.modules.mercados.entity.AsignacionPuesto;
import pe.gob.municipalidad.sigm.modules.mercados.entity.MercadoMunicipal;
import pe.gob.municipalidad.sigm.modules.mercados.entity.TipoContrato;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoAsignacion;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoContrato;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoMercado;
import pe.gob.municipalidad.sigm.modules.mercados.repository.AsignacionPuestoRepository;
import pe.gob.municipalidad.sigm.modules.mercados.repository.MercadoMunicipalRepository;
import pe.gob.municipalidad.sigm.modules.mercados.repository.TipoContratoRepository;
import pe.gob.municipalidad.sigm.modules.mercados.service.ContratoOcupacionService;
import pe.gob.municipalidad.sigm.modules.mercados.service.VigenciaOcupacionService;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/dashboard/mercados/contratos")
public class ContratoWebController {

    private final ContratoOcupacionService contratoService;
    private final VigenciaOcupacionService vigenciaService;
    private final MercadoMunicipalRepository mercadoRepository;
    private final TipoContratoRepository tipoContratoRepository;
    private final AsignacionPuestoRepository asignacionRepository;
    private final OficinaRepository oficinaRepository;

    public ContratoWebController(
            ContratoOcupacionService contratoService,
            VigenciaOcupacionService vigenciaService,
            MercadoMunicipalRepository mercadoRepository,
            TipoContratoRepository tipoContratoRepository,
            AsignacionPuestoRepository asignacionRepository,
            OficinaRepository oficinaRepository) {
        this.contratoService = contratoService;
        this.vigenciaService = vigenciaService;
        this.mercadoRepository = mercadoRepository;
        this.tipoContratoRepository = tipoContratoRepository;
        this.asignacionRepository = asignacionRepository;
        this.oficinaRepository = oficinaRepository;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('MERCADOS_CONTRATO_VER') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public String listar(
            @RequestParam(required = false) String termino,
            @RequestParam(required = false) UUID mercadoId,
            @RequestParam(required = false) EstadoContrato estado,
            @RequestParam(required = false) String tipoContratoCodigo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        Page<ContratoResponse> pagina = contratoService.buscarConFiltros(
                termino, mercadoId, estado, tipoContratoCodigo, fechaDesde, fechaHasta,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "fechaInicio")));

        List<MercadoMunicipal> mercados = mercadoRepository.findByEstado(EstadoMercado.ACTIVO);
        List<TipoContrato> tiposContrato = tipoContratoRepository.findByEstadoOrderByNombreAsc("ACTIVO");

        model.addAttribute("contratos", pagina);
        model.addAttribute("mercados", mercados);
        model.addAttribute("tiposContrato", tiposContrato);
        model.addAttribute("estados", EstadoContrato.values());
        model.addAttribute("stats", vigenciaService.obtenerEstadisticas());

        model.addAttribute("termino", termino);
        model.addAttribute("mercadoIdSeleccionado", mercadoId);
        model.addAttribute("estadoSeleccionado", estado);
        model.addAttribute("tipoSeleccionado", tipoContratoCodigo);
        model.addAttribute("fechaDesde", fechaDesde);
        model.addAttribute("fechaHasta", fechaHasta);

        model.addAttribute("moduloActivo", "mercados");
        model.addAttribute("submenuActivo", "contratos");

        return "admin/mercados/contratos/listado";
    }

    @GetMapping("/nuevo")
    @PreAuthorize("hasAuthority('MERCADOS_CONTRATO_CREAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public String nuevo(
            @RequestParam(required = false) UUID asignacionId,
            Model model) {

        List<AsignacionPuesto> asignacionesHabilitadas = new java.util.ArrayList<>(
                asignacionRepository.findByEstadoInConRelaciones(List.of(EstadoAsignacion.APROBADA, EstadoAsignacion.VIGENTE))
        );
        if (asignacionId != null && asignacionesHabilitadas.stream().noneMatch(a -> a.getId().equals(asignacionId))) {
            asignacionRepository.findByIdCompleto(asignacionId).ifPresent(asignacionesHabilitadas::add);
        }

        List<TipoContrato> tiposContrato = tipoContratoRepository.findByEstadoOrderByNombreAsc("ACTIVO");
        List<Oficina> oficinas = oficinaRepository.findAll();

        model.addAttribute("asignaciones", asignacionesHabilitadas);
        model.addAttribute("tiposContrato", tiposContrato);
        model.addAttribute("oficinas", oficinas);
        model.addAttribute("asignacionPreseleccionada", asignacionId);

        model.addAttribute("moduloActivo", "mercados");
        model.addAttribute("submenuActivo", "contratos");

        return "admin/mercados/contratos/formulario";
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('MERCADOS_CONTRATO_VER') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public String detalle(@PathVariable UUID id, Model model) {
        ContratoDetailResponse detalle = contratoService.obtenerDetalle(id);
        model.addAttribute("detalle", detalle);
        model.addAttribute("moduloActivo", "mercados");
        model.addAttribute("submenuActivo", "contratos");
        return "admin/mercados/contratos/detalle";
    }

    @GetMapping("/{id}/editar")
    @PreAuthorize("hasAuthority('MERCADOS_CONTRATO_EDITAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public String editar(@PathVariable UUID id, Model model) {
        ContratoDetailResponse detalle = contratoService.obtenerDetalle(id);
        List<TipoContrato> tiposContrato = tipoContratoRepository.findByEstadoOrderByNombreAsc("ACTIVO");
        List<Oficina> oficinas = oficinaRepository.findAll();

        model.addAttribute("contrato", detalle);
        model.addAttribute("tiposContrato", tiposContrato);
        model.addAttribute("oficinas", oficinas);

        model.addAttribute("moduloActivo", "mercados");
        model.addAttribute("submenuActivo", "contratos");
        return "admin/mercados/contratos/formulario";
    }
}
