package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.OficinaRepository;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.deuda.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.ClasificacionDeudaTributaria;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoExigibilidadObligacion;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoObligacionTributaria;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoGestionDeuda;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TramoAntiguedadDeuda;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.ConceptoTributarioRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.PeriodoTributarioRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.DeudaTributariaService;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.InteresMoratorioService;
import pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Controller
@RequestMapping("/dashboard/tributaria/deudas")
@RequiredArgsConstructor
public class DeudaTributariaWebController {

    private final DeudaTributariaService deudaService;
    private final InteresMoratorioService interesService;
    private final ConceptoTributarioRepository conceptoRepository;
    private final PeriodoTributarioRepository periodoRepository;
    private final UsuarioRepository usuarioRepository;
    private final OficinaRepository oficinaRepository;

    @GetMapping
    @PreAuthorize("hasAuthority('TRIBUTARIA_DEUDAS_VER') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_TRIBUTACION', 'ROLE_CAJERO')")
    public String dashboardDeudas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaCorte,
            Model model) {

        LocalDate corte = fechaCorte != null ? fechaCorte : LocalDate.now();
        DashboardDeudasResponse dashboard = deudaService.obtenerDashboardDeudas(corte);

        model.addAttribute("dashboard", dashboard);
        model.addAttribute("fechaCorte", corte);
        return "admin/tributaria/deudas/dashboard";
    }

    @GetMapping("/cartera")
    @PreAuthorize("hasAuthority('TRIBUTARIA_DEUDAS_VER') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_TRIBUTACION', 'ROLE_CAJERO')")
    public String carteraTributaria(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) UUID contribuyenteId,
            @RequestParam(required = false) UUID predioId,
            @RequestParam(required = false) UUID conceptoId,
            @RequestParam(required = false) UUID periodoId,
            @RequestParam(required = false) EstadoObligacionTributaria estado,
            @RequestParam(required = false) EstadoExigibilidadObligacion exigibilidad,
            @RequestParam(required = false, defaultValue = "true") Boolean soloConSaldo,
            @RequestParam(required = false) TramoAntiguedadDeuda tramo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaCorte,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            Model model) {

        LocalDate corte = fechaCorte != null ? fechaCorte : LocalDate.now();

        Page<CarteraItemDto> cartera = deudaService.listarCartera(
                query, contribuyenteId, predioId, conceptoId, periodoId,
                estado, exigibilidad, soloConSaldo, corte, tramo,
                PageRequest.of(page, size, Sort.by("fechaVencimiento").ascending().and(Sort.by("codigoObligacion"))));

        model.addAttribute("cartera", cartera);
        model.addAttribute("query", query);
        model.addAttribute("conceptoId", conceptoId);
        model.addAttribute("periodoId", periodoId);
        model.addAttribute("estado", estado);
        model.addAttribute("exigibilidad", exigibilidad);
        model.addAttribute("soloConSaldo", soloConSaldo);
        model.addAttribute("tramo", tramo);
        model.addAttribute("fechaCorte", corte);

        model.addAttribute("conceptos", conceptoRepository.findAll());
        model.addAttribute("periodos", periodoRepository.findAll());
        model.addAttribute("tramos", TramoAntiguedadDeuda.values());
        model.addAttribute("estadosExigibilidad", EstadoExigibilidadObligacion.values());
        return "admin/tributaria/deudas/cartera";
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('TRIBUTARIA_DEUDAS_VER_DETALLE') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_TRIBUTACION', 'ROLE_CAJERO')")
    public String verFichaDeuda(
            @PathVariable UUID id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaCorte,
            Model model) {

        LocalDate corte = fechaCorte != null ? fechaCorte : LocalDate.now();
        DeudaDetalleResponse ficha = deudaService.obtenerFichaDeuda(id, corte);

        List<Usuario> usuariosStaff = usuarioRepository.findHabilitadosWithPersona();

        model.addAttribute("ficha", ficha);
        model.addAttribute("fechaCorte", corte);
        model.addAttribute("usuariosStaff", usuariosStaff);
        model.addAttribute("oficinas", oficinaRepository.findAll());
        model.addAttribute("clasificaciones", ClasificacionDeudaTributaria.values());
        model.addAttribute("tiposGestion", TipoGestionDeuda.values());

        model.addAttribute("formClasificar", new ClasificarDeudaRequest());
        model.addAttribute("formAsignar", new AsignarResponsableRequest());
        model.addAttribute("formGestion", new RegistrarGestionDeudaRequest());
        model.addAttribute("formCompromiso", new RegistrarCompromisoDeudaRequest());

        return "admin/tributaria/deudas/detail";
    }

    @PostMapping("/{id}/clasificar")
    @PreAuthorize("hasAuthority('TRIBUTARIA_DEUDAS_CLASIFICAR') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_TRIBUTACION')")
    public String clasificarDeuda(
            @PathVariable UUID id,
            @Valid @ModelAttribute("formClasificar") ClasificarDeudaRequest request,
            BindingResult bindingResult,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Datos inválidos para clasificación de deuda");
            return "redirect:/dashboard/tributaria/deudas/" + id;
        }

        try {
            deudaService.clasificarObligacion(id, request, userDetails != null ? userDetails.getUsername() : "SYSTEM");
            redirectAttributes.addFlashAttribute("successMessage", "Clasificación de deuda actualizada con éxito");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/dashboard/tributaria/deudas/" + id;
    }

    @PostMapping("/{id}/asignar")
    @PreAuthorize("hasAuthority('TRIBUTARIA_DEUDAS_ASIGNAR') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_TRIBUTACION')")
    public String asignarResponsable(
            @PathVariable UUID id,
            @Valid @ModelAttribute("formAsignar") AsignarResponsableRequest request,
            BindingResult bindingResult,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Debe seleccionar un usuario responsable válido");
            return "redirect:/dashboard/tributaria/deudas/" + id;
        }

        try {
            deudaService.asignarResponsable(id, request, userDetails != null ? userDetails.getUsername() : "SYSTEM");
            redirectAttributes.addFlashAttribute("successMessage", "Responsable de seguimiento asignado correctamente");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/dashboard/tributaria/deudas/" + id;
    }

    @PostMapping("/{id}/gestiones")
    @PreAuthorize("hasAuthority('TRIBUTARIA_DEUDAS_REGISTRAR_GESTION') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_TRIBUTACION')")
    public String registrarGestion(
            @PathVariable UUID id,
            @Valid @ModelAttribute("formGestion") RegistrarGestionDeudaRequest request,
            BindingResult bindingResult,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Complete los campos obligatorios de la gestión administrativa");
            return "redirect:/dashboard/tributaria/deudas/" + id;
        }

        try {
            deudaService.registrarGestion(id, request, userDetails != null ? userDetails.getUsername() : "SYSTEM");
            redirectAttributes.addFlashAttribute("successMessage", "Gestión administrativa registrada exitosamente");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/dashboard/tributaria/deudas/" + id;
    }

    @PostMapping("/{id}/compromisos")
    @PreAuthorize("hasAuthority('TRIBUTARIA_DEUDAS_REGISTRAR_COMPROMISO') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_TRIBUTACION', 'ROLE_CAJERO')")
    public String registrarCompromiso(
            @PathVariable UUID id,
            @Valid @ModelAttribute("formCompromiso") RegistrarCompromisoDeudaRequest request,
            BindingResult bindingResult,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Complete la fecha comprometida válida (hoy o futura)");
            return "redirect:/dashboard/tributaria/deudas/" + id;
        }

        try {
            deudaService.registrarCompromiso(id, request, userDetails != null ? userDetails.getUsername() : "SYSTEM");
            redirectAttributes.addFlashAttribute("successMessage", "Compromiso informativo de pago registrado en el expediente");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/dashboard/tributaria/deudas/" + id;
    }

    @GetMapping("/intereses")
    @PreAuthorize("hasAuthority('TRIBUTARIA_DEUDAS_CALCULAR_INTERESES') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_TRIBUTACION', 'ROLE_CAJERO')")
    public String pantallaIntereses(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaCorte,
            Model model) {

        LocalDate corte = fechaCorte != null ? fechaCorte : LocalDate.now();

        model.addAttribute("parametros", interesService.listarParametros());
        model.addAttribute("conceptos", conceptoRepository.findAll());
        model.addAttribute("fechaCorte", corte);
        model.addAttribute("simulacionForm", new SimulacionInteresRequest());
        model.addAttribute("parametroForm", new ParametroInteresRequest());
        return "admin/tributaria/deudas/intereses";
    }

    @PostMapping("/intereses/simular")
    @PreAuthorize("hasAuthority('TRIBUTARIA_DEUDAS_CALCULAR_INTERESES') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_TRIBUTACION', 'ROLE_CAJERO')")
    public String simularInteresPost(
            @ModelAttribute SimulacionInteresRequest request,
            Model model) {

        SimulacionInteresResponse resultado = interesService.simularInteresGenerico(request);

        model.addAttribute("resultadoSimulacion", resultado);
        model.addAttribute("simulacionForm", request);
        model.addAttribute("parametros", interesService.listarParametros());
        model.addAttribute("conceptos", conceptoRepository.findAll());
        model.addAttribute("fechaCorte", request.getFechaCorte() != null ? request.getFechaCorte() : LocalDate.now());
        model.addAttribute("parametroForm", new ParametroInteresRequest());
        return "admin/tributaria/deudas/intereses";
    }

    @PostMapping("/intereses/parametros")
    @PreAuthorize("hasAuthority('TRIBUTARIA_DEUDAS_CONFIGURAR_INTERESES') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public String crearParametroInteres(
            @Valid @ModelAttribute("parametroForm") ParametroInteresRequest request,
            BindingResult bindingResult,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Revise los campos obligatorios del parámetro normativo");
            return "redirect:/dashboard/tributaria/deudas/intereses";
        }

        try {
            interesService.crearParametro(request, userDetails != null ? userDetails.getUsername() : "SYSTEM");
            redirectAttributes.addFlashAttribute("successMessage", "Parámetro normativo de tasa de interés creado correctamente");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/dashboard/tributaria/deudas/intereses";
    }
}
