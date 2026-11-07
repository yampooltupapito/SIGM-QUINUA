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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.ConceptoExportService;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.ConceptoTributarioService;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Controller
@RequestMapping("/dashboard/tributaria/conceptos")
@RequiredArgsConstructor
public class ConceptoWebController {

    private final ConceptoTributarioService conceptoService;
    private final ConceptoExportService exportService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_CONCEPTOS_VER', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_JEFE_OFICINA')")
    public String listarConceptos(@ModelAttribute ConceptoFilterDto filtro,
                                  @RequestParam(value = "page", defaultValue = "0") int page,
                                  @RequestParam(value = "size", defaultValue = "10") int size,
                                  Model model) {
        log.info("Listando conceptos tributarios: query={}, tipo={}, estado={}", filtro.getQuery(), filtro.getTipoConcepto(), filtro.getEstado());
        Page<ConceptoSummaryDto> conceptos = conceptoService.listarConceptos(
                filtro,
                PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "codigo"))
        );

        Map<String, Object> stats = conceptoService.obtenerEstadisticas();

        model.addAttribute("conceptos", conceptos);
        model.addAttribute("filtro", filtro);
        model.addAttribute("stats", stats);
        model.addAttribute("tiposConcepto", TipoConceptoTributario.values());
        model.addAttribute("naturalezas", NaturalezaTributaria.values());
        model.addAttribute("periodicidades", PeriodicidadConcepto.values());
        model.addAttribute("estados", EstadoConceptoTributario.values());
        model.addAttribute("moduloActivo", "tributaria");
        model.addAttribute("submenuActivo", "conceptos");
        return "admin/tributaria/conceptos/list";
    }

    @GetMapping("/nuevo")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_CONCEPTOS_CREAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public String formularioNuevoConcepto(Model model) {
        model.addAttribute("form", new ConceptoCreateRequest());
        model.addAttribute("tiposConcepto", TipoConceptoTributario.values());
        model.addAttribute("naturalezas", NaturalezaTributaria.values());
        model.addAttribute("periodicidades", PeriodicidadConcepto.values());
        model.addAttribute("normas", conceptoService.listarNormas());
        model.addAttribute("moduloActivo", "tributaria");
        model.addAttribute("submenuActivo", "conceptos");
        return "admin/tributaria/conceptos/form";
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_CONCEPTOS_CREAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public String registrarConcepto(@Valid @ModelAttribute("form") ConceptoCreateRequest form,
                                    BindingResult bindingResult,
                                    Principal principal,
                                    RedirectAttributes redirectAttributes,
                                    Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("tiposConcepto", TipoConceptoTributario.values());
            model.addAttribute("naturalezas", NaturalezaTributaria.values());
            model.addAttribute("periodicidades", PeriodicidadConcepto.values());
            model.addAttribute("normas", conceptoService.listarNormas());
            model.addAttribute("moduloActivo", "tributaria");
            model.addAttribute("submenuActivo", "conceptos");
            return "admin/tributaria/conceptos/form";
        }

        try {
            String usuario = principal != null ? principal.getName() : "SISTEMA";
            ConceptoDetailResponse resp = conceptoService.registrarConcepto(form, usuario);
            redirectAttributes.addFlashAttribute("mensajeExito", "Concepto tributario registrado con éxito: " + resp.getCodigo());
            return "redirect:/dashboard/tributaria/conceptos/" + resp.getId();
        } catch (BusinessException e) {
            model.addAttribute("mensajeError", e.getMessage());
            model.addAttribute("tiposConcepto", TipoConceptoTributario.values());
            model.addAttribute("naturalezas", NaturalezaTributaria.values());
            model.addAttribute("periodicidades", PeriodicidadConcepto.values());
            model.addAttribute("normas", conceptoService.listarNormas());
            model.addAttribute("moduloActivo", "tributaria");
            model.addAttribute("submenuActivo", "conceptos");
            return "admin/tributaria/conceptos/form";
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_CONCEPTOS_VER', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_JEFE_OFICINA')")
    public String verDetalleConcepto(@PathVariable("id") UUID id,
                                     @RequestParam(value = "tab", defaultValue = "general") String tab,
                                     @RequestParam(value = "versionId", required = false) UUID versionId,
                                     Model model) {
        ConceptoDetailResponse concepto = conceptoService.obtenerConceptoPorId(id);

        ConfiguracionDetailResponse configuracionSeleccionada = null;
        if (versionId != null) {
            configuracionSeleccionada = conceptoService.obtenerConfiguracionPorId(versionId);
        } else if (concepto.getConfiguracionActiva() != null) {
            configuracionSeleccionada = concepto.getConfiguracionActiva();
        } else if (!concepto.getConfiguraciones().isEmpty()) {
            configuracionSeleccionada = conceptoService.obtenerConfiguracionPorId(concepto.getConfiguraciones().get(0).getId());
        }

        List<CalendarioCuotaResponse> calendario = List.of();
        if (configuracionSeleccionada != null) {
            calendario = conceptoService.listarCalendarioPorConceptoYPeriodo(id, configuracionSeleccionada.getPeriodoId());
        }

        model.addAttribute("concepto", concepto);
        model.addAttribute("conf", configuracionSeleccionada);
        model.addAttribute("calendario", calendario);
        model.addAttribute("tabActivo", tab);
        model.addAttribute("periodos", conceptoService.listarPeriodos());
        model.addAttribute("normas", conceptoService.listarNormas());
        model.addAttribute("tiposDato", TipoDatoParametro.values());
        model.addAttribute("unidades", UnidadMedidaParametro.values());
        model.addAttribute("moduloActivo", "tributaria");
        model.addAttribute("submenuActivo", "conceptos");
        return "admin/tributaria/conceptos/detail";
    }

    @GetMapping("/{id}/editar")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_CONCEPTOS_EDITAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public String formularioEditarConcepto(@PathVariable("id") UUID id, Model model) {
        ConceptoDetailResponse concepto = conceptoService.obtenerConceptoPorId(id);

        ConceptoUpdateRequest form = ConceptoUpdateRequest.builder()
                .nombre(concepto.getNombre())
                .descripcion(concepto.getDescripcion())
                .tipoConcepto(concepto.getTipoConcepto())
                .naturalezaTributaria(concepto.getNaturalezaTributaria())
                .periodicidad(concepto.getPeriodicidad())
                .aplicaAPredio(concepto.getAplicaAPredio())
                .cuentaPresupuestal(concepto.getCuentaPresupuestal())
                .fechaInicioVigencia(concepto.getFechaInicioVigencia())
                .fechaFinVigencia(concepto.getFechaFinVigencia())
                .normaLegalId(concepto.getNormaLegalId())
                .oficinaResponsableId(concepto.getOficinaResponsableId())
                .estado(concepto.getEstado())
                .build();

        model.addAttribute("concepto", concepto);
        model.addAttribute("form", form);
        model.addAttribute("tiposConcepto", TipoConceptoTributario.values());
        model.addAttribute("naturalezas", NaturalezaTributaria.values());
        model.addAttribute("periodicidades", PeriodicidadConcepto.values());
        model.addAttribute("estados", EstadoConceptoTributario.values());
        model.addAttribute("normas", conceptoService.listarNormas());
        model.addAttribute("moduloActivo", "tributaria");
        model.addAttribute("submenuActivo", "conceptos");
        return "admin/tributaria/conceptos/edit";
    }

    @PostMapping("/{id}/actualizar")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_CONCEPTOS_EDITAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public String actualizarConcepto(@PathVariable("id") UUID id,
                                     @Valid @ModelAttribute("form") ConceptoUpdateRequest form,
                                     BindingResult bindingResult,
                                     Principal principal,
                                     RedirectAttributes redirectAttributes,
                                     Model model) {
        if (bindingResult.hasErrors()) {
            ConceptoDetailResponse concepto = conceptoService.obtenerConceptoPorId(id);
            model.addAttribute("concepto", concepto);
            model.addAttribute("tiposConcepto", TipoConceptoTributario.values());
            model.addAttribute("naturalezas", NaturalezaTributaria.values());
            model.addAttribute("periodicidades", PeriodicidadConcepto.values());
            model.addAttribute("estados", EstadoConceptoTributario.values());
            model.addAttribute("normas", conceptoService.listarNormas());
            model.addAttribute("moduloActivo", "tributaria");
            model.addAttribute("submenuActivo", "conceptos");
            return "admin/tributaria/conceptos/edit";
        }

        try {
            String usuario = principal != null ? principal.getName() : "SISTEMA";
            conceptoService.actualizarConcepto(id, form, usuario);
            redirectAttributes.addFlashAttribute("mensajeExito", "Concepto tributario actualizado con éxito.");
            return "redirect:/dashboard/tributaria/conceptos/" + id;
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
            return "redirect:/dashboard/tributaria/conceptos/" + id + "/editar";
        }
    }

    @PostMapping("/{id}/configuraciones")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_CONCEPTOS_CONFIGURAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public String crearConfiguracion(@PathVariable("id") UUID conceptoId,
                                     @Valid @ModelAttribute ConfiguracionCreateRequest request,
                                     Principal principal,
                                     RedirectAttributes redirectAttributes) {
        try {
            request.setConceptoId(conceptoId);
            String usuario = principal != null ? principal.getName() : "SISTEMA";
            ConfiguracionDetailResponse conf = conceptoService.crearConfiguracion(request, usuario);
            redirectAttributes.addFlashAttribute("mensajeExito", "Borrador de configuración v" + conf.getNumeroVersion() + " creado con éxito.");
            return "redirect:/dashboard/tributaria/conceptos/" + conceptoId + "?tab=configuraciones&versionId=" + conf.getId();
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
            return "redirect:/dashboard/tributaria/conceptos/" + conceptoId + "?tab=configuraciones";
        }
    }

    @PostMapping("/configuraciones/{cid}/parametros")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_CONCEPTOS_CONFIGURAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public String agregarParametro(@PathVariable("cid") UUID cid,
                                   @RequestParam("conceptoId") UUID conceptoId,
                                   @Valid @ModelAttribute ParametroCreateRequest request,
                                   Principal principal,
                                   RedirectAttributes redirectAttributes) {
        try {
            String usuario = principal != null ? principal.getName() : "SISTEMA";
            conceptoService.agregarParametro(cid, request, usuario);
            redirectAttributes.addFlashAttribute("mensajeExito", "Parámetro '" + request.getCodigo() + "' agregado con éxito.");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/dashboard/tributaria/conceptos/" + conceptoId + "?tab=parametros&versionId=" + cid;
    }

    @PostMapping("/configuraciones/{cid}/parametros/{pid}/eliminar")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_CONCEPTOS_CONFIGURAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public String eliminarParametro(@PathVariable("cid") UUID cid,
                                    @PathVariable("pid") UUID pid,
                                    @RequestParam("conceptoId") UUID conceptoId,
                                    Principal principal,
                                    RedirectAttributes redirectAttributes) {
        try {
            String usuario = principal != null ? principal.getName() : "SISTEMA";
            conceptoService.eliminarParametro(cid, pid, usuario);
            redirectAttributes.addFlashAttribute("mensajeExito", "Parámetro eliminado con éxito.");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/dashboard/tributaria/conceptos/" + conceptoId + "?tab=parametros&versionId=" + cid;
    }

    @PostMapping("/configuraciones/{cid}/revision")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_CONCEPTOS_REVISAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public String enviarARevision(@PathVariable("cid") UUID cid,
                                  @RequestParam("conceptoId") UUID conceptoId,
                                  @RequestParam(value = "motivo", required = false) String motivo,
                                  Principal principal,
                                  RedirectAttributes redirectAttributes) {
        try {
            String usuario = principal != null ? principal.getName() : "SISTEMA";
            conceptoService.enviarARevision(cid, motivo, usuario);
            redirectAttributes.addFlashAttribute("mensajeExito", "Configuración enviada a revisión técnica/legal.");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/dashboard/tributaria/conceptos/" + conceptoId + "?tab=configuraciones&versionId=" + cid;
    }

    @PostMapping("/configuraciones/{cid}/aprobar")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_CONCEPTOS_APROBAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public String aprobarConfiguracion(@PathVariable("cid") UUID cid,
                                       @RequestParam("conceptoId") UUID conceptoId,
                                       @RequestParam("resolucionAprobacion") String resolucion,
                                       @RequestParam(value = "motivo", required = false) String motivo,
                                       Principal principal,
                                       RedirectAttributes redirectAttributes) {
        try {
            String usuario = principal != null ? principal.getName() : "SISTEMA";
            conceptoService.aprobarConfiguracion(cid, resolucion, motivo, usuario);
            redirectAttributes.addFlashAttribute("mensajeExito", "Configuración aprobada formalmente según: " + resolucion);
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/dashboard/tributaria/conceptos/" + conceptoId + "?tab=configuraciones&versionId=" + cid;
    }

    @PostMapping("/configuraciones/{cid}/activar")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_CONCEPTOS_ACTIVAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public String activarConfiguracion(@PathVariable("cid") UUID cid,
                                       @RequestParam("conceptoId") UUID conceptoId,
                                       @RequestParam(value = "motivo", required = false) String motivo,
                                       Principal principal,
                                       RedirectAttributes redirectAttributes) {
        try {
            String usuario = principal != null ? principal.getName() : "SISTEMA";
            conceptoService.activarConfiguracion(cid, motivo, usuario);
            redirectAttributes.addFlashAttribute("mensajeExito", "Configuración tributaria ACTIVADA y puesta en vigencia para determinación.");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/dashboard/tributaria/conceptos/" + conceptoId + "?tab=configuraciones&versionId=" + cid;
    }

    @PostMapping("/configuraciones/{cid}/desactivar")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_CONCEPTOS_DESACTIVAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public String desactivarConfiguracion(@PathVariable("cid") UUID cid,
                                          @RequestParam("conceptoId") UUID conceptoId,
                                          @RequestParam("motivo") String motivo,
                                          Principal principal,
                                          RedirectAttributes redirectAttributes) {
        try {
            String usuario = principal != null ? principal.getName() : "SISTEMA";
            conceptoService.desactivarConfiguracion(cid, motivo, usuario);
            redirectAttributes.addFlashAttribute("mensajeExito", "Configuración tributaria desactivada.");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/dashboard/tributaria/conceptos/" + conceptoId + "?tab=configuraciones&versionId=" + cid;
    }

    @PostMapping("/configuraciones/{cid}/nueva-version")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_CONCEPTOS_CONFIGURAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public String generarNuevaVersion(@PathVariable("cid") UUID cid,
                                      @RequestParam("conceptoId") UUID conceptoId,
                                      @RequestParam(value = "motivo", required = false) String motivo,
                                      Principal principal,
                                      RedirectAttributes redirectAttributes) {
        try {
            String usuario = principal != null ? principal.getName() : "SISTEMA";
            ConfiguracionDetailResponse nueva = conceptoService.crearNuevaVersion(cid, motivo, usuario);
            redirectAttributes.addFlashAttribute("mensajeExito", "Nueva versión v" + nueva.getNumeroVersion() + " generada en borrador para edición.");
            return "redirect:/dashboard/tributaria/conceptos/" + conceptoId + "?tab=parametros&versionId=" + nueva.getId();
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
            return "redirect:/dashboard/tributaria/conceptos/" + conceptoId + "?tab=configuraciones&versionId=" + cid;
        }
    }

    @GetMapping("/configuraciones/comparar")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_CONCEPTOS_VER_HISTORIAL', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public String compararVersiones(@RequestParam("versionBaseId") UUID baseId,
                                    @RequestParam("versionComparadaId") UUID compId,
                                    Model model) {
        ComparacionVersionesResponse comp = conceptoService.compararVersiones(baseId, compId);
        model.addAttribute("comp", comp);
        model.addAttribute("moduloActivo", "tributaria");
        model.addAttribute("submenuActivo", "conceptos");
        return "admin/tributaria/conceptos/comparar";
    }

    @PostMapping("/{id}/calendario")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_CONCEPTOS_CONFIGURAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public String agregarCuotaCalendario(@PathVariable("id") UUID conceptoId,
                                         @RequestParam("periodoId") UUID periodoId,
                                         @Valid @ModelAttribute CalendarioCuotaRequest request,
                                         Principal principal,
                                         RedirectAttributes redirectAttributes) {
        try {
            String usuario = principal != null ? principal.getName() : "SISTEMA";
            conceptoService.agregarCuotaCalendario(conceptoId, periodoId, request, usuario);
            redirectAttributes.addFlashAttribute("mensajeExito", "Cuota N° " + request.getNumeroCuota() + " programada con éxito en el calendario.");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/dashboard/tributaria/conceptos/" + conceptoId + "?tab=calendario";
    }

    @PostMapping("/normas")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_CONCEPTOS_GESTIONAR_NORMAS', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public String registrarNorma(@Valid @ModelAttribute NormaCreateRequest request,
                                 @RequestParam(value = "redirectConceptoId", required = false) UUID redirectConceptoId,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) {
        try {
            String usuario = principal != null ? principal.getName() : "SISTEMA";
            NormaDetailResponse norma = conceptoService.registrarNorma(request, usuario);
            redirectAttributes.addFlashAttribute("mensajeExito", "Norma legal registrada con éxito: " + norma.getNumeroNorma());
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }

        if (redirectConceptoId != null) {
            return "redirect:/dashboard/tributaria/conceptos/" + redirectConceptoId + "?tab=normas";
        }
        return "redirect:/dashboard/tributaria/conceptos";
    }

    @GetMapping("/exportar/excel")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_CONCEPTOS_EXPORTAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public ResponseEntity<byte[]> exportarExcel(@ModelAttribute ConceptoFilterDto filtro) {
        byte[] bytes = exportService.exportarExcel(filtro);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"catalogo_conceptos_tributarios_quinua.xlsx\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }

    @GetMapping("/exportar/pdf")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_CONCEPTOS_EXPORTAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public ResponseEntity<byte[]> exportarPdf(@ModelAttribute ConceptoFilterDto filtro) {
        byte[] bytes = exportService.exportarPdf(filtro);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"catalogo_conceptos_tributarios_quinua.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(bytes);
    }

    @GetMapping("/{id}/exportar/pdf")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_CONCEPTOS_EXPORTAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public ResponseEntity<byte[]> exportarFichaConceptoPdf(@PathVariable("id") UUID id) {
        byte[] bytes = exportService.exportarFichaConceptoPdf(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"ficha_concepto_" + id + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(bytes);
    }
}
