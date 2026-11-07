package pe.gob.municipalidad.sigm.modules.serenazgo.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.estadisticas.DashboardEstadisticoResponse;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.estadisticas.FiltroEstadisticoRequest;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.estadisticas.GenerarReporteRequest;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.estadisticas.ReporteHistorialDto;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.ReporteEstadisticoSerenazgo;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoSectorPatrullaje;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.FormatoReporteEstadistico;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoReporteEstadistico;
import pe.gob.municipalidad.sigm.modules.serenazgo.repository.SectorPatrullajeRepository;
import pe.gob.municipalidad.sigm.modules.serenazgo.repository.TipoIncidenciaSeguridadRepository;
import pe.gob.municipalidad.sigm.modules.serenazgo.repository.ZonaPatrullajeRepository;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.EstadisticasSerenazgoService;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.ReportesEstadisticosSerenazgoService;
import pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails;

import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@Controller
@RequestMapping("/dashboard/serenazgo")
@RequiredArgsConstructor
public class EstadisticasWebController {

    private final EstadisticasSerenazgoService estadisticasService;
    private final ReportesEstadisticosSerenazgoService reportesService;
    private final SectorPatrullajeRepository sectorRepository;
    private final ZonaPatrullajeRepository zonaRepository;
    private final TipoIncidenciaSeguridadRepository tipoIncidenciaRepository;

    @GetMapping("/estadisticas")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'SUPERVISOR_SERENAZGO') or hasAuthority('SERENAZGO_ESTADISTICAS_VER')")
    public String dashboardEstadistico(
            @ModelAttribute("filtro") FiltroEstadisticoRequest filtro,
            @AuthenticationPrincipal CustomUserDetails user,
            Model model) {

        DashboardEstadisticoResponse datos = estadisticasService.obtenerDashboardEstadistico(filtro, user);

        model.addAttribute("datos", datos);
        model.addAttribute("filtro", filtro);
        model.addAttribute("sectores", sectorRepository.findByEstado(EstadoSectorPatrullaje.ACTIVO));
        model.addAttribute("zonas", zonaRepository.findAll());
        model.addAttribute("tiposIncidencia", tipoIncidenciaRepository.findAll());
        model.addAttribute("moduloActivo", "serenazgo");
        model.addAttribute("submenuActivo", "estadisticas");

        return "admin/serenazgo/estadisticas/index";
    }

    @GetMapping("/reportes")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'SUPERVISOR_SERENAZGO') or hasAuthority('SERENAZGO_REPORTES_VER_HISTORIAL')")
    public String centroReportes(
            @RequestParam(required = false) TipoReporteEstadistico tipo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        Pageable pageable = PageRequest.of(page, size);
        Page<ReporteHistorialDto> reportes = reportesService.listarHistorialReportes(tipo, desde, hasta, pageable);

        model.addAttribute("reportes", reportes);
        model.addAttribute("tiposReporte", TipoReporteEstadistico.values());
        model.addAttribute("formatos", FormatoReporteEstadistico.values());
        model.addAttribute("sectores", sectorRepository.findByEstado(EstadoSectorPatrullaje.ACTIVO));
        model.addAttribute("nuevoReporte", new GenerarReporteRequest());
        model.addAttribute("tipoSeleccionado", tipo);
        model.addAttribute("desde", desde);
        model.addAttribute("hasta", hasta);
        model.addAttribute("moduloActivo", "serenazgo");
        model.addAttribute("submenuActivo", "reportes");

        return "admin/serenazgo/estadisticas/reportes";
    }

    @PostMapping("/reportes/generar")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'SUPERVISOR_SERENAZGO') or hasAuthority('SERENAZGO_REPORTES_GENERAR')")
    public String procesarGeneracionReporte(
            @Valid @ModelAttribute("nuevoReporte") GenerarReporteRequest req,
            BindingResult bindingResult,
            @AuthenticationPrincipal CustomUserDetails user,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("mensajeError", "Parámetros inválidos para generar el reporte");
            return "redirect:/dashboard/serenazgo/reportes";
        }

        try {
            String ip = request.getRemoteAddr();
            ReporteHistorialDto dto = reportesService.generarReporte(req, user, ip);
            redirectAttributes.addFlashAttribute("mensajeExito",
                    "Reporte " + dto.getCodigo() + " generado satisfactoriamente en formato " + dto.getFormato());
        } catch (Exception e) {
            log.error("Error al generar reporte: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("mensajeError", "Error al generar reporte: " + e.getMessage());
        }

        return "redirect:/dashboard/serenazgo/reportes";
    }

    @GetMapping("/reportes/{id}/descargar")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'SUPERVISOR_SERENAZGO') or hasAuthority('SERENAZGO_REPORTES_DESCARGAR')")
    public ResponseEntity<Resource> descargarReporteWeb(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails user) {

        Resource res = reportesService.descargarReporte(id, user);
        ReporteEstadisticoSerenazgo entidad = reportesService.obtenerPorId(id);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(entidad.getMimeType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + entidad.getNombreArchivo() + "\"")
                .body(res);
    }
}
