package pe.gob.municipalidad.sigm.modules.serenazgo.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.gob.municipalidad.sigm.common.repository.OficinaRepository;
import pe.gob.municipalidad.sigm.common.repository.PersonaRepository;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoPersonalSerenazgo;
import pe.gob.municipalidad.sigm.modules.serenazgo.repository.CargoOperativoSerenazgoRepository;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.PersonalSerenazgoService;

import java.util.UUID;

@Slf4j
@Controller
@RequestMapping("/dashboard/serenazgo/personal")
@RequiredArgsConstructor
public class PersonalSerenazgoWebController {

    private final PersonalSerenazgoService personalService;
    private final CargoOperativoSerenazgoRepository cargoRepository;
    private final OficinaRepository oficinaRepository;
    private final PersonaRepository personaRepository;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('SERENAZGO_PERSONAL_VER', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_JEFE_OFICINA')")
    public String listado(
            @ModelAttribute("filtro") PersonalSerenazgoFilter filter,
            @PageableDefault(size = 10) Pageable pageable,
            Model model) {
        Page<PersonalSerenazgoResponse> pagina = personalService.listar(filter, pageable);
        EstadisticasPersonalResponse stats = personalService.obtenerEstadisticas();

        model.addAttribute("moduloActivo", "serenazgo");
        model.addAttribute("submenuActivo", "personal");
        model.addAttribute("pagina", pagina);
        model.addAttribute("estadisticas", stats);
        model.addAttribute("cargos", cargoRepository.findByActivoTrueOrderByNivelJerarquicoAsc());
        model.addAttribute("estados", EstadoPersonalSerenazgo.values());
        model.addAttribute("oficinas", oficinaRepository.findAll());
        return "admin/serenazgo/personal/listado";
    }

    @GetMapping("/nuevo")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_PERSONAL_CREAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public String nuevo(Model model) {
        model.addAttribute("moduloActivo", "serenazgo");
        model.addAttribute("submenuActivo", "personal");
        model.addAttribute("form", new PersonalSerenazgoCreateRequest());
        model.addAttribute("cargos", cargoRepository.findByActivoTrueOrderByNivelJerarquicoAsc());
        model.addAttribute("oficinas", oficinaRepository.findAll());
        model.addAttribute("personas", personaRepository.findAll());
        return "admin/serenazgo/personal/formulario";
    }

    @PostMapping("/guardar")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_PERSONAL_CREAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public String guardar(
            @Valid @ModelAttribute("form") PersonalSerenazgoCreateRequest form,
            BindingResult bindingResult,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes,
            Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("moduloActivo", "serenazgo");
            model.addAttribute("submenuActivo", "personal");
            model.addAttribute("cargos", cargoRepository.findByActivoTrueOrderByNivelJerarquicoAsc());
            model.addAttribute("oficinas", oficinaRepository.findAll());
            model.addAttribute("personas", personaRepository.findAll());
            return "admin/serenazgo/personal/formulario";
        }

        try {
            String username = userDetails != null ? userDetails.getUsername() : "admin";
            PersonalSerenazgoResponse res = personalService.registrar(form, username);
            redirectAttributes.addFlashAttribute("mensajeExito", "Efectivo registrado con éxito: " + res.getCodigoInterno());
            return "redirect:/dashboard/serenazgo/personal/" + res.getId();
        } catch (Exception e) {
            log.error("Error al guardar personal de serenazgo: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("mensajeError", "Error al registrar: " + e.getMessage());
            return "redirect:/dashboard/serenazgo/personal/nuevo";
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_PERSONAL_VER', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_JEFE_OFICINA', 'ROLE_SERENO')")
    public String detalle(@PathVariable("id") UUID id, Model model) {
        PersonalSerenazgoDetailResponse detalle = personalService.obtenerDetalle(id);
        model.addAttribute("moduloActivo", "serenazgo");
        model.addAttribute("submenuActivo", "personal");
        model.addAttribute("sereno", detalle);
        model.addAttribute("cargos", cargoRepository.findByActivoTrueOrderByNivelJerarquicoAsc());
        model.addAttribute("estados", EstadoPersonalSerenazgo.values());
        return "admin/serenazgo/personal/detalle";
    }

    @GetMapping("/{id}/editar")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_PERSONAL_EDITAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public String editar(@PathVariable("id") UUID id, Model model) {
        PersonalSerenazgoDetailResponse detalle = personalService.obtenerDetalle(id);
        PersonalSerenazgoUpdateRequest form = PersonalSerenazgoUpdateRequest.builder()
                .oficinaId(detalle.getOficinaId())
                .usuarioId(detalle.getUsuarioId())
                .tipoDocumentoLaboral(detalle.getTipoDocumentoLaboral())
                .fechaIngreso(detalle.getFechaIngreso())
                .fechaSalida(detalle.getFechaSalida())
                .observaciones(detalle.getObservaciones())
                .build();

        model.addAttribute("moduloActivo", "serenazgo");
        model.addAttribute("submenuActivo", "personal");
        model.addAttribute("sereno", detalle);
        model.addAttribute("form", form);
        model.addAttribute("oficinas", oficinaRepository.findAll());
        return "admin/serenazgo/personal/editar";
    }

    @PostMapping("/{id}/actualizar")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_PERSONAL_EDITAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public String actualizar(
            @PathVariable("id") UUID id,
            @Valid @ModelAttribute("form") PersonalSerenazgoUpdateRequest form,
            BindingResult bindingResult,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("mensajeError", "Datos del formulario no válidos.");
            return "redirect:/dashboard/serenazgo/personal/" + id + "/editar";
        }

        try {
            String username = userDetails != null ? userDetails.getUsername() : "admin";
            personalService.actualizar(id, form, username);
            redirectAttributes.addFlashAttribute("mensajeExito", "Ficha laboral actualizada satisfactoriamente.");
            return "redirect:/dashboard/serenazgo/personal/" + id;
        } catch (Exception e) {
            log.error("Error al actualizar personal {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("mensajeError", "Error al actualizar: " + e.getMessage());
            return "redirect:/dashboard/serenazgo/personal/" + id + "/editar";
        }
    }

    @PostMapping("/{id}/cambiar-estado")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_PERSONAL_ESTADO_CAMBIAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public String cambiarEstado(
            @PathVariable("id") UUID id,
            @Valid @ModelAttribute CambioEstadoPersonalRequest request,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            String username = userDetails != null ? userDetails.getUsername() : "admin";
            personalService.cambiarEstado(id, request, username);
            redirectAttributes.addFlashAttribute("mensajeExito", "Estado actualizado a: " + request.getNuevoEstado().getDescripcion());
        } catch (Exception e) {
            log.error("Error al cambiar estado de personal {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("mensajeError", "No se pudo cambiar el estado: " + e.getMessage());
        }
        return "redirect:/dashboard/serenazgo/personal/" + id;
    }

    @PostMapping("/{id}/cambiar-cargo")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_PERSONAL_CARGO_CAMBIAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public String cambiarCargo(
            @PathVariable("id") UUID id,
            @Valid @ModelAttribute CambioCargoPersonalRequest request,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            String username = userDetails != null ? userDetails.getUsername() : "admin";
            personalService.cambiarCargo(id, request, username);
            redirectAttributes.addFlashAttribute("mensajeExito", "Cargo operativo reasignado exitosamente.");
        } catch (Exception e) {
            log.error("Error al cambiar cargo de personal {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("mensajeError", "No se pudo cambiar el cargo: " + e.getMessage());
        }
        return "redirect:/dashboard/serenazgo/personal/" + id;
    }

    @GetMapping("/{id}/historial")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_PERSONAL_HISTORIAL_VER', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_JEFE_OFICINA')")
    public String verHistorial(@PathVariable("id") UUID id, Model model) {
        PersonalSerenazgoDetailResponse detalle = personalService.obtenerDetalle(id);
        model.addAttribute("moduloActivo", "serenazgo");
        model.addAttribute("submenuActivo", "personal");
        model.addAttribute("sereno", detalle);
        return "admin/serenazgo/personal/historial";
    }

    @GetMapping("/{id}/documentos")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_PERSONAL_DOCUMENTOS_VER', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_JEFE_OFICINA')")
    public String verDocumentos(@PathVariable("id") UUID id, Model model) {
        PersonalSerenazgoDetailResponse detalle = personalService.obtenerDetalle(id);
        model.addAttribute("moduloActivo", "serenazgo");
        model.addAttribute("submenuActivo", "personal");
        model.addAttribute("sereno", detalle);
        return "admin/serenazgo/personal/documentos";
    }

    @GetMapping("/{id}/ficha.pdf")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_PERSONAL_FICHA_EXPORTAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_JEFE_OFICINA')")
    public ResponseEntity<byte[]> descargarFichaPdf(@PathVariable("id") UUID id) {
        byte[] pdf = personalService.generarFichaPdf(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"ficha_sereno_" + id + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
