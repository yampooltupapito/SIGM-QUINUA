package pe.gob.municipalidad.sigm.modules.serenazgo.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.gob.municipalidad.sigm.common.repository.OficinaRepository;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoSectorPatrullaje;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.PrioridadOperativaZona;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.SectorPatrullajeService;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.TerritorioSeguridadService;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.ZonaPatrullajeService;

import java.security.Principal;
import java.util.UUID;

@Controller
@RequestMapping("/dashboard/serenazgo")
@RequiredArgsConstructor
@Slf4j
public class SectoresZonasWebController {

    private final SectorPatrullajeService sectorService;
    private final ZonaPatrullajeService zonaService;
    private final TerritorioSeguridadService territorioService;
    private final OficinaRepository oficinaRepository;

    @GetMapping("/sectores")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_SECTORES_VER', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public String index(
            @ModelAttribute("sectorFilter") SectorFilter sectorFilter,
            @ModelAttribute("zonaFilter") ZonaFilter zonaFilter,
            @RequestParam(defaultValue = "0") int pageSectores,
            @RequestParam(defaultValue = "0") int pageZonas,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "sectores") String activeTab,
            Model model) {

        Pageable pageableSectores = PageRequest.of(pageSectores, size);
        Pageable pageableZonas = PageRequest.of(pageZonas, size);

        Page<SectorResponse> sectores = sectorService.listarSectores(sectorFilter, pageableSectores);
        Page<ZonaPatrullajeResponse> zonas = zonaService.listarZonas(zonaFilter, pageableZonas);
        DashboardTerritorialResponse dashboard = territorioService.obtenerDashboardTerritorial();

        model.addAttribute("sectores", sectores);
        model.addAttribute("zonas", zonas);
        model.addAttribute("dashboard", dashboard);
        model.addAttribute("activeTab", activeTab);
        model.addAttribute("estados", EstadoSectorPatrullaje.values());
        model.addAttribute("prioridades", PrioridadOperativaZona.values());
        model.addAttribute("oficinas", oficinaRepository.findAll());
        model.addAttribute("listaSectoresActivos", sectorService.listarSectoresActivos());

        return "admin/serenazgo/territorio/index";
    }

    // =========================================================================
    // Sectores
    // =========================================================================

    @GetMapping("/sectores/nuevo")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_SECTORES_CREAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public String nuevoSectorForm(Model model) {
        if (!model.containsAttribute("sectorForm")) {
            model.addAttribute("sectorForm", new SectorCreateRequest());
        }
        model.addAttribute("oficinas", oficinaRepository.findAll());
        model.addAttribute("estados", EstadoSectorPatrullaje.values());
        return "admin/serenazgo/territorio/sector-form";
    }

    @PostMapping("/sectores")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_SECTORES_CREAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public String registrarSector(
            @Valid @ModelAttribute("sectorForm") SectorCreateRequest request,
            BindingResult bindingResult,
            Principal principal,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("oficinas", oficinaRepository.findAll());
            model.addAttribute("estados", EstadoSectorPatrullaje.values());
            return "admin/serenazgo/territorio/sector-form";
        }

        try {
            String username = principal != null ? principal.getName() : "admin";
            SectorResponse nuevo = sectorService.crearSector(request, username);
            redirectAttributes.addFlashAttribute("successMessage", "Sector " + nuevo.getCodigo() + " registrado exitosamente.");
            return "redirect:/dashboard/serenazgo/sectores/" + nuevo.getId();
        } catch (Exception e) {
            log.error("Error al registrar sector: {}", e.getMessage());
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("oficinas", oficinaRepository.findAll());
            model.addAttribute("estados", EstadoSectorPatrullaje.values());
            return "admin/serenazgo/territorio/sector-form";
        }
    }

    @GetMapping("/sectores/{id}")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_SECTORES_VER', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public String detalleSector(@PathVariable UUID id, Model model) {
        SectorDetailResponse sector = sectorService.obtenerDetallePorId(id);
        model.addAttribute("sector", sector);
        model.addAttribute("estados", EstadoSectorPatrullaje.values());
        model.addAttribute("prioridades", PrioridadOperativaZona.values());
        return "admin/serenazgo/territorio/sector-detalle";
    }

    @GetMapping("/sectores/{id}/editar")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_SECTORES_EDITAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public String editarSectorForm(@PathVariable UUID id, Model model) {
        SectorDetailResponse s = sectorService.obtenerDetallePorId(id);
        SectorUpdateRequest form = SectorUpdateRequest.builder()
                .nombre(s.getNombre())
                .descripcion(s.getDescripcion())
                .colorHex(s.getColorHex())
                .oficinaResponsableId(s.getOficinaResponsableId())
                .fechaInicioVigencia(s.getFechaInicioVigencia())
                .fechaFinVigencia(s.getFechaFinVigencia())
                .estado(s.getEstado())
                .observaciones(s.getObservaciones())
                .version(s.getVersion())
                .build();

        model.addAttribute("sectorId", id);
        model.addAttribute("sectorCodigo", s.getCodigo());
        model.addAttribute("sectorForm", form);
        model.addAttribute("oficinas", oficinaRepository.findAll());
        model.addAttribute("estados", EstadoSectorPatrullaje.values());
        return "admin/serenazgo/territorio/sector-form";
    }

    @PostMapping("/sectores/{id}/editar")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_SECTORES_EDITAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public String actualizarSector(
            @PathVariable UUID id,
            @Valid @ModelAttribute("sectorForm") SectorUpdateRequest request,
            BindingResult bindingResult,
            Principal principal,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("sectorId", id);
            model.addAttribute("oficinas", oficinaRepository.findAll());
            model.addAttribute("estados", EstadoSectorPatrullaje.values());
            return "admin/serenazgo/territorio/sector-form";
        }

        try {
            String username = principal != null ? principal.getName() : "admin";
            SectorResponse act = sectorService.actualizarSector(id, request, username);
            redirectAttributes.addFlashAttribute("successMessage", "Sector " + act.getCodigo() + " actualizado correctamente.");
            return "redirect:/dashboard/serenazgo/sectores/" + id;
        } catch (Exception e) {
            log.error("Error al actualizar sector: {}", e.getMessage());
            model.addAttribute("sectorId", id);
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("oficinas", oficinaRepository.findAll());
            model.addAttribute("estados", EstadoSectorPatrullaje.values());
            return "admin/serenazgo/territorio/sector-form";
        }
    }

    @GetMapping("/sectores/{id}/ficha-pdf")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_SECTORES_VER', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public ResponseEntity<ByteArrayResource> descargarFichaSectorPdf(@PathVariable UUID id) {
        byte[] pdfBytes = territorioService.generarFichaSectorPdf(id);
        ByteArrayResource resource = new ByteArrayResource(pdfBytes);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"ficha_sector_" + id + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdfBytes.length)
                .body(resource);
    }

    // =========================================================================
    // Zonas
    // =========================================================================

    @GetMapping("/zonas/nueva")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_ZONAS_CREAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public String nuevaZonaForm(@RequestParam(required = false) UUID sectorId, Model model) {
        if (!model.containsAttribute("zonaForm")) {
            ZonaCreateRequest form = new ZonaCreateRequest();
            if (sectorId != null) {
                form.setSectorId(sectorId);
            }
            model.addAttribute("zonaForm", form);
        }
        model.addAttribute("sectores", sectorService.listarSectoresActivos());
        model.addAttribute("prioridades", PrioridadOperativaZona.values());
        model.addAttribute("estados", EstadoSectorPatrullaje.values());
        return "admin/serenazgo/territorio/zona-form";
    }

    @PostMapping("/zonas")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_ZONAS_CREAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public String registrarZona(
            @Valid @ModelAttribute("zonaForm") ZonaCreateRequest request,
            BindingResult bindingResult,
            Principal principal,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("sectores", sectorService.listarSectoresActivos());
            model.addAttribute("prioridades", PrioridadOperativaZona.values());
            model.addAttribute("estados", EstadoSectorPatrullaje.values());
            return "admin/serenazgo/territorio/zona-form";
        }

        try {
            String username = principal != null ? principal.getName() : "admin";
            ZonaPatrullajeResponse nueva = zonaService.crearZona(request, username);
            redirectAttributes.addFlashAttribute("successMessage", "Zona " + nueva.getCodigo() + " registrada exitosamente.");
            return "redirect:/dashboard/serenazgo/zonas/" + nueva.getId();
        } catch (Exception e) {
            log.error("Error al registrar zona: {}", e.getMessage());
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("sectores", sectorService.listarSectoresActivos());
            model.addAttribute("prioridades", PrioridadOperativaZona.values());
            model.addAttribute("estados", EstadoSectorPatrullaje.values());
            return "admin/serenazgo/territorio/zona-form";
        }
    }

    @GetMapping("/zonas/{id}")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_ZONAS_VER', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public String detalleZona(@PathVariable UUID id, Model model) {
        ZonaDetailResponse zona = zonaService.obtenerDetallePorId(id);
        model.addAttribute("zona", zona);
        model.addAttribute("estados", EstadoSectorPatrullaje.values());
        model.addAttribute("prioridades", PrioridadOperativaZona.values());
        model.addAttribute("sectoresDisponibles", sectorService.listarSectoresActivos());
        return "admin/serenazgo/territorio/zona-detalle";
    }

    @GetMapping("/zonas/{id}/editar")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_ZONAS_EDITAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public String editarZonaForm(@PathVariable UUID id, Model model) {
        ZonaDetailResponse z = zonaService.obtenerDetallePorId(id);
        ZonaUpdateRequest form = ZonaUpdateRequest.builder()
                .nombre(z.getNombre())
                .descripcion(z.getDescripcion())
                .prioridadOperativa(z.getPrioridadOperativa())
                .fechaInicioVigencia(z.getFechaInicioVigencia())
                .fechaFinVigencia(z.getFechaFinVigencia())
                .estado(z.getEstado())
                .observaciones(z.getObservaciones())
                .version(z.getVersion())
                .build();

        model.addAttribute("zonaId", id);
        model.addAttribute("zonaCodigo", z.getCodigo());
        model.addAttribute("sectorNombre", z.getSectorNombre());
        model.addAttribute("zonaForm", form);
        model.addAttribute("prioridades", PrioridadOperativaZona.values());
        model.addAttribute("estados", EstadoSectorPatrullaje.values());
        return "admin/serenazgo/territorio/zona-form";
    }

    @PostMapping("/zonas/{id}/editar")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_ZONAS_EDITAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public String actualizarZona(
            @PathVariable UUID id,
            @Valid @ModelAttribute("zonaForm") ZonaUpdateRequest request,
            BindingResult bindingResult,
            Principal principal,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("zonaId", id);
            model.addAttribute("prioridades", PrioridadOperativaZona.values());
            model.addAttribute("estados", EstadoSectorPatrullaje.values());
            return "admin/serenazgo/territorio/zona-form";
        }

        try {
            String username = principal != null ? principal.getName() : "admin";
            ZonaPatrullajeResponse act = zonaService.actualizarZona(id, request, username);
            redirectAttributes.addFlashAttribute("successMessage", "Zona " + act.getCodigo() + " actualizada exitosamente.");
            return "redirect:/dashboard/serenazgo/zonas/" + id;
        } catch (Exception e) {
            log.error("Error al actualizar zona: {}", e.getMessage());
            model.addAttribute("zonaId", id);
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("prioridades", PrioridadOperativaZona.values());
            model.addAttribute("estados", EstadoSectorPatrullaje.values());
            return "admin/serenazgo/territorio/zona-form";
        }
    }
}
