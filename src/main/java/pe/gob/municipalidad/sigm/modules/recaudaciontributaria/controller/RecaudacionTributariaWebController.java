package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.RecaudacionTributariaService;

@Slf4j
@Controller
@RequestMapping("/dashboard/tributaria")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('TRIBUTARIA_MODULO_VER', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_CAJERO', 'ROLE_JEFE_OFICINA')")
public class RecaudacionTributariaWebController {

    private final RecaudacionTributariaService tributariaService;

    @GetMapping
    public String dashboard(Model model) {
        log.info("Accediendo al panel principal de Recaudación Tributaria");
        DashboardTributarioResponse dashboard = tributariaService.obtenerDashboard();
        model.addAttribute("dashboard", dashboard);
        model.addAttribute("moduloActivo", "tributaria");
        model.addAttribute("submenuActivo", "dashboard");
        return "admin/tributaria/dashboard";
    }

    // Las rutas de /dashboard/tributaria/contribuyentes/** son gestionadas por ContribuyenteWebController

    // Las rutas de /dashboard/tributaria/predios/** son gestionadas por PredioWebController
    @GetMapping("/predial")
    public String redirectPredial() {
        return "redirect:/dashboard/tributaria/impuesto-predial";
    }

    // Las rutas de /dashboard/tributaria/conceptos/** son gestionadas por ConceptoWebController

    @GetMapping("/periodos")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_CONCEPTOS_VER', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_JEFE_OFICINA')")
    public String periodos(@RequestParam(value = "page", defaultValue = "0") int page,
                           @RequestParam(value = "size", defaultValue = "10") int size,
                           Model model) {
        log.info("Accediendo a períodos fiscales tributarios");
        Page<PeriodoSummaryDto> periodos = tributariaService.listarPeriodos(
                PageRequest.of(page, size)
        );
        model.addAttribute("periodos", periodos);
        model.addAttribute("moduloActivo", "tributaria");
        model.addAttribute("submenuActivo", "periodos");
        return "admin/tributaria/periodos/list";
    }

    @GetMapping("/normas")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_CONCEPTOS_VER', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_JEFE_OFICINA')")
    public String normas(@RequestParam(value = "page", defaultValue = "0") int page,
                         @RequestParam(value = "size", defaultValue = "10") int size,
                         Model model) {
        log.info("Accediendo a normas tributarias institucionales");
        Page<NormaSummaryDto> normas = tributariaService.listarNormas(
                PageRequest.of(page, size)
        );
        model.addAttribute("normas", normas);
        model.addAttribute("moduloActivo", "tributaria");
        model.addAttribute("submenuActivo", "normas");
        return "admin/tributaria/normas/list";
    }

    // Las rutas de /dashboard/tributaria/reportes/** son gestionadas por CentroReportesTributariosWebController (Prompt 64)
}
