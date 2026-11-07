package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.portal.AprobarRechazarVinculacionRequest;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.portal.VinculacionResponse;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoVinculacionContribuyente;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.VinculacionContribuyenteService;
import pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails;

import java.util.UUID;

/**
 * Controlador Administrativo para la calificación y auditoría de vinculaciones ciudadanas.
 */
@Slf4j
@Controller
@RequestMapping("/dashboard/tributaria/portal/vinculaciones")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('TRIBUTARIA_PORTAL_VALIDAR_VINCULACION') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_TRIBUTACION')")
public class VinculacionTributariaAdminController {

    private final VinculacionContribuyenteService vinculacionService;
    private final UsuarioRepository usuarioRepository;

    @GetMapping
    public String bandejaVinculaciones(
            @RequestParam(value = "estado", required = false) EstadoVinculacionContribuyente estado,
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "15") int size,
            Model model) {

        Page<VinculacionResponse> vinculaciones = vinculacionService.listarBandejaAdministrativa(
                estado, q, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "fechaSolicitud")));

        model.addAttribute("vinculaciones", vinculaciones);
        model.addAttribute("estadoSeleccionado", estado);
        model.addAttribute("estados", EstadoVinculacionContribuyente.values());
        model.addAttribute("q", q);
        model.addAttribute("moduloActivo", "tributaria");
        model.addAttribute("submenuActivo", "vinculaciones");
        return "admin/tributaria/portal/vinculaciones-admin";
    }

    @PostMapping("/{id}/aprobar")
    public String aprobarVinculacion(
            @PathVariable("id") UUID id,
            @ModelAttribute AprobarRechazarVinculacionRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            Usuario operador = obtenerOperador(userDetails);
            VinculacionResponse resp = vinculacionService.aprobarVinculacion(id, request, operador);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Vinculación de " + resp.getContribuyenteNombre() + " con usuario " + resp.getUsername() + " fue APROBADA satisfactoriamente.");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/dashboard/tributaria/portal/vinculaciones";
    }

    @PostMapping("/{id}/rechazar")
    public String rechazarVinculacion(
            @PathVariable("id") UUID id,
            @Valid @ModelAttribute AprobarRechazarVinculacionRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            Usuario operador = obtenerOperador(userDetails);
            VinculacionResponse resp = vinculacionService.rechazarVinculacion(id, request, operador);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Vinculación con usuario " + resp.getUsername() + " fue RECHAZADA.");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/dashboard/tributaria/portal/vinculaciones";
    }

    @PostMapping("/{id}/revocar")
    @PreAuthorize("hasAuthority('TRIBUTARIA_PORTAL_REVOCAR_VINCULACION') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_TRIBUTACION')")
    public String revocarVinculacion(
            @PathVariable("id") UUID id,
            @Valid @ModelAttribute AprobarRechazarVinculacionRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            Usuario operador = obtenerOperador(userDetails);
            VinculacionResponse resp = vinculacionService.revocarVinculacion(id, request, operador);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Vinculación con usuario " + resp.getUsername() + " fue REVOCADA.");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/dashboard/tributaria/portal/vinculaciones";
    }

    private Usuario obtenerOperador(CustomUserDetails userDetails) {
        if (userDetails != null && userDetails.getUsername() != null) {
            return usuarioRepository.findByUsername(userDetails.getUsername()).orElse(null);
        }
        return null;
    }
}

