package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.repository.OficinaRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.ContribuyenteExportService;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.ContribuyenteService;

import java.security.Principal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

/**
 * Controlador Web MVC para la gestión administrativa del Padrón de Contribuyentes Municipales.
 */
@Slf4j
@Controller
@RequestMapping("/dashboard/tributaria/contribuyentes")
@RequiredArgsConstructor
public class ContribuyenteWebController {

    private final ContribuyenteService contribuyenteService;
    private final ContribuyenteExportService exportService;
    private final pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.PredioService predioService;
    private final OficinaRepository oficinaRepository;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_CONTRIBUYENTES_VER', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_JEFE_OFICINA')")
    public String listar(@RequestParam(value = "query", required = false) String query,
                         @RequestParam(value = "tipo", required = false) TipoContribuyente tipo,
                         @RequestParam(value = "estado", required = false) EstadoContribuyente estado,
                         @RequestParam(value = "fechaDesde", required = false) LocalDate fechaDesde,
                         @RequestParam(value = "fechaHasta", required = false) LocalDate fechaHasta,
                         @RequestParam(value = "oficinaId", required = false) Long oficinaId,
                         @RequestParam(value = "page", defaultValue = "0") int page,
                         @RequestParam(value = "size", defaultValue = "10") int size,
                         Model model) {

        log.info("Accediendo a lista de contribuyentes: query={}, tipo={}, estado={}", query, tipo, estado);

        ContribuyenteFilterDto filtro = ContribuyenteFilterDto.builder()
                .query(query)
                .tipo(tipo)
                .estado(estado)
                .fechaDesde(fechaDesde)
                .fechaHasta(fechaHasta)
                .oficinaId(oficinaId)
                .build();

        Page<ContribuyenteSummaryDto> contribuyentes = contribuyenteService.listarConFiltros(
                filtro,
                PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "codigoContribuyente"))
        );

        Map<String, Object> stats = contribuyenteService.obtenerEstadisticas();

        model.addAttribute("contribuyentes", contribuyentes);
        model.addAttribute("filtro", filtro);
        model.addAttribute("stats", stats);
        model.addAttribute("tiposContribuyente", TipoContribuyente.values());
        model.addAttribute("estadosContribuyente", EstadoContribuyente.values());
        model.addAttribute("oficinas", oficinaRepository.findAll());
        model.addAttribute("moduloActivo", "tributaria");
        model.addAttribute("submenuActivo", "contribuyentes");

        return "admin/tributaria/contribuyentes/list";
    }

    @GetMapping("/nuevo")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_CONTRIBUYENTES_CREAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public String nuevo(Model model) {
        if (!model.containsAttribute("request")) {
            model.addAttribute("request", ContribuyenteCreateRequest.builder()
                    .tipoContribuyente(TipoContribuyente.PERSONA_NATURAL)
                    .tipoDocumento("DNI")
                    .medioContactoPreferido(MedioContactoPreferido.CORREO_ELECTRONICO)
                    .build());
        }

        model.addAttribute("tiposContribuyente", TipoContribuyente.values());
        model.addAttribute("mediosContacto", MedioContactoPreferido.values());
        model.addAttribute("oficinas", oficinaRepository.findAll());
        model.addAttribute("moduloActivo", "tributaria");
        model.addAttribute("submenuActivo", "contribuyentes");
        return "admin/tributaria/contribuyentes/form";
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_CONTRIBUYENTES_CREAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public String registrar(@Valid @ModelAttribute("request") ContribuyenteCreateRequest request,
                            BindingResult bindingResult,
                            Principal principal,
                            RedirectAttributes redirectAttributes,
                            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("tiposContribuyente", TipoContribuyente.values());
            model.addAttribute("mediosContacto", MedioContactoPreferido.values());
            model.addAttribute("oficinas", oficinaRepository.findAll());
            model.addAttribute("moduloActivo", "tributaria");
            model.addAttribute("submenuActivo", "contribuyentes");
            return "admin/tributaria/contribuyentes/form";
        }

        try {
            String username = principal != null ? principal.getName() : "SISTEMA";
            ContribuyenteDetailResponse res = contribuyenteService.registrarContribuyente(request, username);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Contribuyente registrado con éxito. Código asignado: " + res.getCodigoContribuyente());
            return "redirect:/dashboard/tributaria/contribuyentes/" + res.getId();
        } catch (BusinessException e) {
            log.warn("Error de validación al registrar contribuyente: {}", e.getMessage());
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("tiposContribuyente", TipoContribuyente.values());
            model.addAttribute("mediosContacto", MedioContactoPreferido.values());
            model.addAttribute("oficinas", oficinaRepository.findAll());
            model.addAttribute("moduloActivo", "tributaria");
            model.addAttribute("submenuActivo", "contribuyentes");
            return "admin/tributaria/contribuyentes/form";
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_CONTRIBUYENTES_VER', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_JEFE_OFICINA')")
    public String verDetalle(@PathVariable("id") UUID id, Model model) {
        log.info("Visualizando detalle de contribuyente con ID: {}", id);
        ContribuyenteDetailResponse contribuyente = contribuyenteService.obtenerDetalle(id);

        model.addAttribute("contribuyente", contribuyente);
        model.addAttribute("prediosVinculados", predioService.listarPrediosPorContribuyente(id));
        model.addAttribute("estadosContribuyente", EstadoContribuyente.values());
        model.addAttribute("tiposRepresentacion", TipoRepresentacionLegal.values());
        model.addAttribute("tiposDomicilio", TipoDomicilioContribuyente.values());
        model.addAttribute("tiposContacto", TipoContactoContribuyente.values());

        // Modelos vacíos para los modales
        model.addAttribute("estadoRequest", new CambioEstadoContribuyenteRequest());
        model.addAttribute("repRequest", new RepresentanteContribuyenteRequest());
        model.addAttribute("domRequest", new DomicilioContribuyenteRequest());
        model.addAttribute("contRequest", new ContactoContribuyenteRequest());

        model.addAttribute("moduloActivo", "tributaria");
        model.addAttribute("submenuActivo", "contribuyentes");
        return "admin/tributaria/contribuyentes/detail";
    }

    @GetMapping("/{id}/editar")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_CONTRIBUYENTES_EDITAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public String editar(@PathVariable("id") UUID id, Model model) {
        ContribuyenteDetailResponse detalle = contribuyenteService.obtenerDetalle(id);

        ContribuyenteUpdateRequest editRequest = ContribuyenteUpdateRequest.builder()
                .tipoDocumento(detalle.getTipoDocumento())
                .numeroDocumento(detalle.getNumeroDocumento())
                .nombres(detalle.getNombres())
                .primerApellido(detalle.getPrimerApellido())
                .segundoApellido(detalle.getSegundoApellido())
                .nombreRazonSocial(detalle.getNombreRazonSocial())
                .nombreComercial(detalle.getNombreComercial())
                .representanteLegalNombre(detalle.getRepresentanteLegalNombre())
                .representanteLegalDocumento(detalle.getRepresentanteLegalDocumento())
                .domicilioFiscal(detalle.getDomicilioFiscal())
                .referenciaDomicilio(detalle.getReferenciaDomicilio())
                .telefono(detalle.getTelefono())
                .telefonoAlternativo(detalle.getTelefonoAlternativo())
                .correo(detalle.getCorreo())
                .medioContactoPreferido(detalle.getMedioContactoPreferido())
                .observaciones(detalle.getObservaciones())
                .build();

        model.addAttribute("contribuyenteId", id);
        model.addAttribute("tipoContribuyente", detalle.getTipoContribuyente());
        model.addAttribute("codigoContribuyente", detalle.getCodigoContribuyente());
        model.addAttribute("editRequest", editRequest);
        model.addAttribute("mediosContacto", MedioContactoPreferido.values());
        model.addAttribute("moduloActivo", "tributaria");
        model.addAttribute("submenuActivo", "contribuyentes");
        return "admin/tributaria/contribuyentes/edit";
    }

    @PostMapping("/{id}/actualizar")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_CONTRIBUYENTES_EDITAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public String actualizar(@PathVariable("id") UUID id,
                             @Valid @ModelAttribute("editRequest") ContribuyenteUpdateRequest request,
                             BindingResult bindingResult,
                             Principal principal,
                             RedirectAttributes redirectAttributes,
                             Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("contribuyenteId", id);
            model.addAttribute("mediosContacto", MedioContactoPreferido.values());
            model.addAttribute("moduloActivo", "tributaria");
            model.addAttribute("submenuActivo", "contribuyentes");
            return "admin/tributaria/contribuyentes/edit";
        }

        try {
            String username = principal != null ? principal.getName() : "SISTEMA";
            contribuyenteService.actualizarContribuyente(id, request, username);
            redirectAttributes.addFlashAttribute("successMessage", "Datos del contribuyente actualizados correctamente.");
            return "redirect:/dashboard/tributaria/contribuyentes/" + id;
        } catch (BusinessException e) {
            log.warn("Error actualizando contribuyente: {}", e.getMessage());
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("contribuyenteId", id);
            model.addAttribute("mediosContacto", MedioContactoPreferido.values());
            model.addAttribute("moduloActivo", "tributaria");
            model.addAttribute("submenuActivo", "contribuyentes");
            return "admin/tributaria/contribuyentes/edit";
        }
    }

    @PostMapping("/{id}/estado")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_CONTRIBUYENTES_CAMBIAR_ESTADO', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public String cambiarEstado(@PathVariable("id") UUID id,
                                @Valid @ModelAttribute("estadoRequest") CambioEstadoContribuyenteRequest request,
                                BindingResult bindingResult,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Debe indicar el nuevo estado y el motivo sustentatorio.");
            return "redirect:/dashboard/tributaria/contribuyentes/" + id;
        }

        try {
            String username = principal != null ? principal.getName() : "SISTEMA";
            contribuyenteService.cambiarEstado(id, request, username);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Estado actualizado satisfactoriamente a: " + request.getNuevoEstado().getDescripcion());
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/dashboard/tributaria/contribuyentes/" + id;
    }

    @PostMapping("/{id}/representantes")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_CONTRIBUYENTES_GESTIONAR_REPRESENTANTES', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public String agregarRepresentante(@PathVariable("id") UUID id,
                                       @Valid @ModelAttribute("repRequest") RepresentanteContribuyenteRequest request,
                                       BindingResult bindingResult,
                                       Principal principal,
                                       RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Datos incompletos para el registro del representante legal.");
            return "redirect:/dashboard/tributaria/contribuyentes/" + id;
        }

        try {
            String username = principal != null ? principal.getName() : "SISTEMA";
            contribuyenteService.agregarRepresentante(id, request, username);
            redirectAttributes.addFlashAttribute("successMessage", "Representante legal incorporado con éxito.");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/dashboard/tributaria/contribuyentes/" + id;
    }

    @PostMapping("/{id}/representantes/{representanteId}/eliminar")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_CONTRIBUYENTES_GESTIONAR_REPRESENTANTES', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public String eliminarRepresentante(@PathVariable("id") UUID id,
                                        @PathVariable("representanteId") UUID repId,
                                        Principal principal,
                                        RedirectAttributes redirectAttributes) {
        try {
            String username = principal != null ? principal.getName() : "SISTEMA";
            contribuyenteService.eliminarRepresentante(id, repId, username);
            redirectAttributes.addFlashAttribute("successMessage", "Representación legal revocada correctamente.");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/dashboard/tributaria/contribuyentes/" + id;
    }

    @PostMapping("/{id}/domicilios")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_CONTRIBUYENTES_GESTIONAR_DOMICILIOS', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public String agregarDomicilio(@PathVariable("id") UUID id,
                                   @Valid @ModelAttribute("domRequest") DomicilioContribuyenteRequest request,
                                   BindingResult bindingResult,
                                   Principal principal,
                                   RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Datos incompletos para el registro del domicilio.");
            return "redirect:/dashboard/tributaria/contribuyentes/" + id;
        }

        try {
            String username = principal != null ? principal.getName() : "SISTEMA";
            contribuyenteService.agregarDomicilio(id, request, username);
            redirectAttributes.addFlashAttribute("successMessage", "Domicilio registrado con éxito.");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/dashboard/tributaria/contribuyentes/" + id;
    }

    @PostMapping("/{id}/domicilios/{domicilioId}/eliminar")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_CONTRIBUYENTES_GESTIONAR_DOMICILIOS', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public String eliminarDomicilio(@PathVariable("id") UUID id,
                                    @PathVariable("domicilioId") UUID domId,
                                    Principal principal,
                                    RedirectAttributes redirectAttributes) {
        try {
            String username = principal != null ? principal.getName() : "SISTEMA";
            contribuyenteService.eliminarDomicilio(id, domId, username);
            redirectAttributes.addFlashAttribute("successMessage", "Domicilio dado de baja históricamente.");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/dashboard/tributaria/contribuyentes/" + id;
    }

    @PostMapping("/{id}/contactos")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_CONTRIBUYENTES_GESTIONAR_CONTACTOS', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public String agregarContacto(@PathVariable("id") UUID id,
                                  @Valid @ModelAttribute("contRequest") ContactoContribuyenteRequest request,
                                  BindingResult bindingResult,
                                  Principal principal,
                                  RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Datos incompletos para registrar el medio de contacto.");
            return "redirect:/dashboard/tributaria/contribuyentes/" + id;
        }

        try {
            String username = principal != null ? principal.getName() : "SISTEMA";
            contribuyenteService.agregarContacto(id, request, username);
            redirectAttributes.addFlashAttribute("successMessage", "Canal de contacto registrado con éxito.");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/dashboard/tributaria/contribuyentes/" + id;
    }

    @PostMapping("/{id}/contactos/{contactoId}/eliminar")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_CONTRIBUYENTES_GESTIONAR_CONTACTOS', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public String eliminarContacto(@PathVariable("id") UUID id,
                                   @PathVariable("contactoId") UUID contId,
                                   Principal principal,
                                   RedirectAttributes redirectAttributes) {
        try {
            String username = principal != null ? principal.getName() : "SISTEMA";
            contribuyenteService.eliminarContacto(id, contId, username);
            redirectAttributes.addFlashAttribute("successMessage", "Medio de contacto desactivado.");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/dashboard/tributaria/contribuyentes/" + id;
    }

    @GetMapping("/exportar/excel")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_CONTRIBUYENTES_EXPORTAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public void exportarExcel(@RequestParam(value = "query", required = false) String query,
                              @RequestParam(value = "tipo", required = false) TipoContribuyente tipo,
                              @RequestParam(value = "estado", required = false) EstadoContribuyente estado,
                              Authentication authentication,
                              HttpServletResponse response) {

        log.info("Exportando padrón de contribuyentes a Excel");
        ContribuyenteFilterDto filtro = ContribuyenteFilterDto.builder()
                .query(query)
                .tipo(tipo)
                .estado(estado)
                .build();

        boolean puedeVerDocumentos = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SUPERADMIN"));

        byte[] bytes = exportService.exportarExcel(filtro, puedeVerDocumentos);

        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            String filename = "Padron_Contribuyentes_Quinua_" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + ".xlsx";
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");
            response.getOutputStream().write(bytes);
            response.flushBuffer();
        } catch (Exception e) {
            log.error("Error transmitiendo archivo Excel: {}", e.getMessage(), e);
        }
    }

    @GetMapping("/exportar/pdf")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_CONTRIBUYENTES_EXPORTAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public void exportarPdf(@RequestParam(value = "query", required = false) String query,
                            @RequestParam(value = "tipo", required = false) TipoContribuyente tipo,
                            @RequestParam(value = "estado", required = false) EstadoContribuyente estado,
                            Authentication authentication,
                            HttpServletResponse response) {

        log.info("Exportando padrón de contribuyentes a PDF");
        ContribuyenteFilterDto filtro = ContribuyenteFilterDto.builder()
                .query(query)
                .tipo(tipo)
                .estado(estado)
                .build();

        boolean puedeVerDocumentos = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SUPERADMIN"));

        byte[] bytes = exportService.exportarPdf(filtro, puedeVerDocumentos);

        try {
            response.setContentType(MediaType.APPLICATION_PDF_VALUE);
            String filename = "Padron_Contribuyentes_Quinua_" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + ".pdf";
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");
            response.getOutputStream().write(bytes);
            response.flushBuffer();
        } catch (Exception e) {
            log.error("Error transmitiendo archivo PDF: {}", e.getMessage(), e);
        }
    }
}
