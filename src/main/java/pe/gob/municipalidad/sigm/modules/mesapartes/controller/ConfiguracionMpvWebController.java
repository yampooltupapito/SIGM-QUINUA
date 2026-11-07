package pe.gob.municipalidad.sigm.modules.mesapartes.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.common.repository.OficinaRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.ConfiguracionMpvDtos.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoCatalogoTramite;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.TipoCampoFormulario;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.TipoDocumentalPlantilla;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.UnidadPlazo;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.*;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/dashboard/mesa-partes/configuracion")
@PreAuthorize("hasAuthority('MESAPARTES_CONFIG_VER') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA')")
public class ConfiguracionMpvWebController {

    private final CatalogoTramitesService catalogoService;
    private final RequisitosTramiteService requisitosService;
    private final AdministracionFormulariosService formulariosService;
    private final ConfiguracionPlazosService plazosService;
    private final CalendarioAdministrativoService calendarioService;
    private final PlantillasDocumentalesService plantillasService;
    private final ConfiguracionNumeracionService numeracionService;
    private final ConfiguracionHistorialService historialService;
    private final OficinaRepository oficinaRepository;

    public ConfiguracionMpvWebController(
            CatalogoTramitesService catalogoService,
            RequisitosTramiteService requisitosService,
            AdministracionFormulariosService formulariosService,
            ConfiguracionPlazosService plazosService,
            CalendarioAdministrativoService calendarioService,
            PlantillasDocumentalesService plantillasService,
            ConfiguracionNumeracionService numeracionService,
            ConfiguracionHistorialService historialService,
            OficinaRepository oficinaRepository
    ) {
        this.catalogoService = catalogoService;
        this.requisitosService = requisitosService;
        this.formulariosService = formulariosService;
        this.plazosService = plazosService;
        this.calendarioService = calendarioService;
        this.plantillasService = plantillasService;
        this.numeracionService = numeracionService;
        this.historialService = historialService;
        this.oficinaRepository = oficinaRepository;
    }

    @GetMapping
    public String index() {
        return "redirect:/dashboard/mesa-partes/configuracion/tramites";
    }

    @GetMapping("/tramites")
    public String tramites(
            @RequestParam(required = false) EstadoCatalogoTramite estado,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            Model model
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("nombre").ascending());
        Page<TramiteAdminResponse> tramitesPage = catalogoService.listarTramites(estado, q, pageable);
        List<Oficina> oficinas = oficinaRepository.findByEstado("ACTIVO");

        model.addAttribute("tramitesPage", tramitesPage);
        model.addAttribute("oficinas", oficinas);
        model.addAttribute("estadosCatalogo", EstadoCatalogoTramite.values());
        model.addAttribute("unidadesPlazo", UnidadPlazo.values());
        model.addAttribute("filtroEstado", estado);
        model.addAttribute("filtroQ", q);
        model.addAttribute("moduloActivo", "mesa-partes");
        model.addAttribute("submenuActivo", "configuracion");
        model.addAttribute("pestanaActiva", "tramites");

        return "admin/mesa-partes/configuracion/tramites";
    }

    @GetMapping("/requisitos")
    public String requisitos(
            @RequestParam(required = false) Long tramiteId,
            Model model
    ) {
        var tramites = catalogoService.listarTramites(null, null, PageRequest.of(0, 100, Sort.by("nombre").ascending())).getContent();
        Long seleccionado = tramiteId;
        if (seleccionado == null && !tramites.isEmpty()) {
            seleccionado = tramites.get(0).getId();
        }

        List<RequisitoAdminResponse> requisitos = (seleccionado != null) ?
                requisitosService.listarPorTramite(seleccionado) : List.of();

        model.addAttribute("tramites", tramites);
        model.addAttribute("tramiteSeleccionado", seleccionado);
        model.addAttribute("requisitos", requisitos);
        model.addAttribute("moduloActivo", "mesa-partes");
        model.addAttribute("submenuActivo", "configuracion");
        model.addAttribute("pestanaActiva", "requisitos");

        return "admin/mesa-partes/configuracion/requisitos";
    }

    @GetMapping("/formularios")
    public String formularios(
            @RequestParam(required = false) Long tramiteId,
            Model model
    ) {
        var tramites = catalogoService.listarTramites(null, null, PageRequest.of(0, 100, Sort.by("nombre").ascending())).getContent();
        Long seleccionado = tramiteId;
        if (seleccionado == null && !tramites.isEmpty()) {
            seleccionado = tramites.get(0).getId();
        }

        FormularioAdminResponse formAdmin = null;
        if (seleccionado != null) {
            try {
                formAdmin = formulariosService.obtenerPorTramiteId(seleccionado);
            } catch (Exception ignored) {}
        }

        model.addAttribute("tramites", tramites);
        model.addAttribute("tramiteSeleccionado", seleccionado);
        model.addAttribute("formulario", formAdmin);
        model.addAttribute("tiposCampo", TipoCampoFormulario.values());
        model.addAttribute("moduloActivo", "mesa-partes");
        model.addAttribute("submenuActivo", "configuracion");
        model.addAttribute("pestanaActiva", "formularios");

        return "admin/mesa-partes/configuracion/formularios";
    }

    @GetMapping("/plazos")
    public String plazos(Model model) {
        var plazos = plazosService.listarPlazosPorTramite();
        model.addAttribute("plazos", plazos);
        model.addAttribute("unidadesPlazo", UnidadPlazo.values());
        model.addAttribute("moduloActivo", "mesa-partes");
        model.addAttribute("submenuActivo", "configuracion");
        model.addAttribute("pestanaActiva", "plazos");
        return "admin/mesa-partes/configuracion/plazos";
    }

    @GetMapping("/calendario")
    public String calendario(
            @RequestParam(required = false) Integer anio,
            Model model
    ) {
        int anioReq = (anio != null && anio > 1900) ? anio : LocalDate.now().getYear();
        var feriados = calendarioService.listarPorAnio(anioReq);

        model.addAttribute("anio", anioReq);
        model.addAttribute("feriados", feriados);
        model.addAttribute("moduloActivo", "mesa-partes");
        model.addAttribute("submenuActivo", "configuracion");
        model.addAttribute("pestanaActiva", "calendario");
        return "admin/mesa-partes/configuracion/calendario";
    }

    @GetMapping("/numeracion")
    public String numeracion(Model model) {
        var correlativos = numeracionService.listarCorrelativos();
        model.addAttribute("correlativos", correlativos);
        model.addAttribute("moduloActivo", "mesa-partes");
        model.addAttribute("submenuActivo", "configuracion");
        model.addAttribute("pestanaActiva", "numeracion");
        return "admin/mesa-partes/configuracion/numeracion";
    }

    @GetMapping("/plantillas")
    public String plantillas(Model model) {
        var plantillas = plantillasService.listarPlantillas();
        model.addAttribute("plantillas", plantillas);
        model.addAttribute("tiposDocumentales", TipoDocumentalPlantilla.values());
        model.addAttribute("moduloActivo", "mesa-partes");
        model.addAttribute("submenuActivo", "configuracion");
        model.addAttribute("pestanaActiva", "plantillas");
        return "admin/mesa-partes/configuracion/plantillas";
    }

    @GetMapping("/historial")
    public String historial(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaCambio").descending());
        var historialPage = historialService.listarHistorial(null, pageable);

        model.addAttribute("historialPage", historialPage);
        model.addAttribute("moduloActivo", "mesa-partes");
        model.addAttribute("submenuActivo", "configuracion");
        model.addAttribute("pestanaActiva", "historial");
        return "admin/mesa-partes/configuracion/historial";
    }
}
