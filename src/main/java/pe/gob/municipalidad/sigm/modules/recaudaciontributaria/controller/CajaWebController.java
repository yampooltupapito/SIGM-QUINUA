package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.cajapago.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoCajaMunicipal;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoSesionCaja;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.CajaMunicipalService;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.PagoExportService;

import java.util.UUID;

@Slf4j
@Controller
@RequestMapping("/dashboard/tributaria/cajas")
@RequiredArgsConstructor
public class CajaWebController {

    private final CajaMunicipalService cajaService;
    private final PagoExportService exportService;
    private final UsuarioRepository usuarioRepository;

    @GetMapping
    @PreAuthorize("hasAuthority('TRIBUTARIA_CAJA_VER') or hasAnyRole('ADMIN', 'SUPERADMIN', 'CAJERO', 'JEFE_OFICINA')")
    public String listarCajas(@RequestParam(value = "busqueda", required = false) String busqueda,
                             @RequestParam(value = "estado", required = false) EstadoCajaMunicipal estado,
                             @RequestParam(value = "page", defaultValue = "0") int page,
                             @RequestParam(value = "size", defaultValue = "10") int size,
                             Model model) {

        Page<CajaResponse> cajas = cajaService.listarCajas(
                busqueda, estado, PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "codigo"))
        );

        Page<SesionCajaResponse> sesiones = cajaService.listarSesiones(
                null, null, null, null, null, null,
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "fechaApertura"))
        );

        model.addAttribute("cajas", cajas);
        model.addAttribute("sesionesRecientes", sesiones);
        model.addAttribute("busqueda", busqueda);
        model.addAttribute("estado", estado);
        model.addAttribute("estadosCaja", EstadoCajaMunicipal.values());
        model.addAttribute("dashboard", cajaService.obtenerMetricasDashboard());

        model.addAttribute("moduloActivo", "tributaria");
        model.addAttribute("submenuActivo", "cajas");
        return "admin/tributaria/cajas/list";
    }

    @GetMapping("/nueva")
    @PreAuthorize("hasAuthority('TRIBUTARIA_CAJA_ADMINISTRAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA')")
    public String nuevaCajaForm(Model model) {
        model.addAttribute("usuarios", usuarioRepository.findAll());
        model.addAttribute("moduloActivo", "tributaria");
        model.addAttribute("submenuActivo", "cajas");
        return "admin/tributaria/cajas/form";
    }

    @PostMapping
    @PreAuthorize("hasAuthority('TRIBUTARIA_CAJA_ADMINISTRAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA')")
    public String crearCaja(@ModelAttribute @Valid CrearCajaRequest request,
                            BindingResult result,
                            @AuthenticationPrincipal UserDetails userDetails,
                            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", result.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/dashboard/tributaria/cajas/nueva";
        }

        try {
            String username = userDetails != null ? userDetails.getUsername() : "ADMIN";
            CajaResponse response = cajaService.crearCaja(request, username);
            redirectAttributes.addFlashAttribute("success", "Caja Municipal creada exitosamente: " + response.getCodigo());
            return "redirect:/dashboard/tributaria/cajas";
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/dashboard/tributaria/cajas/nueva";
        }
    }

    @PostMapping("/sesion/abrir")
    @PreAuthorize("hasAuthority('TRIBUTARIA_CAJA_ABRIR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'CAJERO')")
    public String abrirSesion(@ModelAttribute @Valid AbrirSesionCajaRequest request,
                              BindingResult result,
                              @AuthenticationPrincipal UserDetails userDetails,
                              RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", result.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/dashboard/tributaria/cajas";
        }

        try {
            String username = userDetails != null ? userDetails.getUsername() : "CAJERO";
            SesionCajaResponse sesion = cajaService.abrirSesion(request, username);
            redirectAttributes.addFlashAttribute("success", "Sesión de caja aperturada con éxito: " + sesion.getCodigoSesion());
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/dashboard/tributaria/cajas";
    }

    @PostMapping("/sesion/{id}/cerrar")
    @PreAuthorize("hasAuthority('TRIBUTARIA_CAJA_CERRAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'CAJERO')")
    public String cerrarSesion(@PathVariable("id") UUID id,
                               @ModelAttribute @Valid CerrarSesionCajaRequest request,
                               BindingResult result,
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", result.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/dashboard/tributaria/cajas";
        }

        try {
            String username = userDetails != null ? userDetails.getUsername() : "CAJERO";
            SesionCajaResponse sesion = cajaService.cerrarSesion(id, request, username);
            redirectAttributes.addFlashAttribute("success", "Sesión " + sesion.getCodigoSesion() + " cerrada correctamente.");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/dashboard/tributaria/cajas";
    }

    @GetMapping("/sesion/{id}/cierre-pdf")
    @PreAuthorize("hasAuthority('TRIBUTARIA_CAJA_REPORTES') or hasAnyRole('ADMIN', 'SUPERADMIN', 'CAJERO', 'JEFE_OFICINA')")
    public ResponseEntity<byte[]> descargarCierrePdf(@PathVariable("id") UUID id) {
        byte[] bytes = exportService.exportarCierreSesionPdf(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"acta_cierre_caja_" + id + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(bytes);
    }
}
