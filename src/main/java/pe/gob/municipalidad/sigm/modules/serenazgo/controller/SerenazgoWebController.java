package pe.gob.municipalidad.sigm.modules.serenazgo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.CatalogoSeguridadService;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.DashboardSeguridadService;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.SectorPatrullajeService;

@Controller
@RequestMapping("/dashboard/serenazgo")
@RequiredArgsConstructor
public class SerenazgoWebController {

    private final DashboardSeguridadService dashboardService;
    private final CatalogoSeguridadService catalogoService;
    private final SectorPatrullajeService sectorService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('SERENAZGO_MODULO_ACCEDER', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_SERENO')")
    public String dashboard(Model model) {
        model.addAttribute("resumen", dashboardService.obtenerResumenOperativo());
        model.addAttribute("tiposIncidencia", catalogoService.obtenerTiposIncidenciaActivos());
        model.addAttribute("cargos", catalogoService.obtenerCargosOperativos());
        model.addAttribute("sectores", sectorService.listarSectoresActivos());
        model.addAttribute("turnos", catalogoService.obtenerTurnos());
        return "admin/serenazgo/dashboard";
    }
}
