package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.cajapago.ConciliacionCajaResponse;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoConciliacionCaja;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.CajaMunicipalService;

import java.time.OffsetDateTime;
import java.util.UUID;

@Slf4j
@Controller
@RequestMapping("/dashboard/tributaria/conciliaciones")
@RequiredArgsConstructor
public class ConciliacionWebController {

    private final CajaMunicipalService cajaService;

    @GetMapping
    @PreAuthorize("hasAuthority('TRIBUTARIA_CONCILIACION_VER') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA', 'TESORERO')")
    public String listarConciliaciones(@RequestParam(value = "busqueda", required = false) String busqueda,
                                       @RequestParam(value = "estado", required = false) EstadoConciliacionCaja estado,
                                       @RequestParam(value = "desde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime desde,
                                       @RequestParam(value = "hasta", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime hasta,
                                       @RequestParam(value = "page", defaultValue = "0") int page,
                                       @RequestParam(value = "size", defaultValue = "10") int size,
                                       Model model) {

        Page<ConciliacionCajaResponse> conciliaciones = cajaService.listarConciliaciones(
                busqueda, null, estado, desde, hasta,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "fechaConciliacion"))
        );

        model.addAttribute("conciliaciones", conciliaciones);
        model.addAttribute("busqueda", busqueda);
        model.addAttribute("estado", estado);
        model.addAttribute("estadosConciliacion", EstadoConciliacionCaja.values());
        model.addAttribute("dashboard", cajaService.obtenerMetricasDashboard());

        model.addAttribute("moduloActivo", "tributaria");
        model.addAttribute("submenuActivo", "conciliaciones");
        return "admin/tributaria/conciliaciones/list";
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('TRIBUTARIA_CONCILIACION_VER') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA')")
    public String detalleConciliacion(@PathVariable("id") UUID id, Model model) {
        ConciliacionCajaResponse conc = cajaService.obtenerConciliacionPorId(id);
        model.addAttribute("conciliacion", conc);
        model.addAttribute("moduloActivo", "tributaria");
        model.addAttribute("submenuActivo", "conciliaciones");
        return "admin/tributaria/conciliaciones/detail";
    }

    @PostMapping("/{id}/aprobar")
    @PreAuthorize("hasAuthority('TRIBUTARIA_CONCILIACION_APROBAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA')")
    public String aprobarConciliacion(@PathVariable("id") UUID id,
                                      @RequestParam(value = "observaciones", required = false) String observaciones,
                                      @AuthenticationPrincipal UserDetails userDetails,
                                      RedirectAttributes redirectAttributes) {
        try {
            String username = userDetails != null ? userDetails.getUsername() : "SUPERVISOR";
            ConciliacionCajaResponse res = cajaService.aprobarConciliacion(id, observaciones, username);
            redirectAttributes.addFlashAttribute("success", "Conciliación " + res.getCodigoConciliacion() + " aprobada por tesorería.");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/dashboard/tributaria/conciliaciones/" + id;
    }
}
