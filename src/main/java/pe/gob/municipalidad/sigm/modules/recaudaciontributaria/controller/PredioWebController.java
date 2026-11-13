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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.PredioExportService;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.PredioService;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

/**
 * Controlador Web MVC para el Subsistema de Registro y Administración de Predios Municipales.
 */
@Slf4j
@Controller
@RequestMapping("/dashboard/tributaria/predios")
@RequiredArgsConstructor
public class PredioWebController {

    private final PredioService predioService;
    private final PredioExportService exportService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_PREDIOS_VER', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_JEFE_OFICINA')")
    public String listar(@RequestParam(value = "query", required = false) String query,
                         @RequestParam(value = "clasificacion", required = false) ClasificacionPredio clasificacion,
                         @RequestParam(value = "uso", required = false) UsoDeclaradoPredio uso,
                         @RequestParam(value = "sector", required = false) String sector,
                         @RequestParam(value = "zona", required = false) String zona,
                         @RequestParam(value = "estado", required = false) EstadoPredio estado,
                         @RequestParam(value = "contribuyenteId", required = false) UUID contribuyenteId,
                         @RequestParam(value = "page", defaultValue = "0") int page,
                         @RequestParam(value = "size", defaultValue = "10") int size,
                         Model model) {

        log.info("Consultando lista de predios: query={}, clasificacion={}, estado={}", query, clasificacion, estado);

        PredioFilterDto filtro = PredioFilterDto.builder()
                .query(query)
                .clasificacion(clasificacion)
                .uso(uso)
                .sector(sector)
                .zona(zona)
                .estado(estado)
                .contribuyenteId(contribuyenteId)
                .build();

        Page<PredioSummaryDto> predios = predioService.listarPredios(filtro, PageRequest.of(page, size, Sort.by("codigoPredio").ascending()));
        Map<String, Long> estadisticas = predioService.obtenerEstadisticas();

        model.addAttribute("predios", predios);
        model.addAttribute("filtro", filtro);
        model.addAttribute("estadisticas", estadisticas);
        model.addAttribute("clasificaciones", ClasificacionPredio.values());
        model.addAttribute("usos", UsoDeclaradoPredio.values());
        model.addAttribute("estados", EstadoPredio.values());
        model.addAttribute("moduloActivo", "tributaria");
        model.addAttribute("submenuActivo", "predios");

        return "admin/tributaria/predios/list";
    }

    @GetMapping("/nuevo")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_PREDIOS_CREAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_JEFE_OFICINA')")
    public String nuevoPredio(Model model) {
        log.info("Abriendo formulario para nuevo predio municipal");

        if (!model.containsAttribute("request")) {
            model.addAttribute("request", PredioCreateRequest.builder()
                    .clasificacionPredio(ClasificacionPredio.URBANO)
                    .usoDeclarado(UsoDeclaradoPredio.VIVIENDA)
                    .unidadArea("M2")
                    .departamento("AYACUCHO")
                    .provincia("HUAMANGA")
                    .distrito("QUINUA")
                    .ubigeo("050110")
                    .build());
        }

        model.addAttribute("clasificaciones", ClasificacionPredio.values());
        model.addAttribute("usos", UsoDeclaradoPredio.values());
        model.addAttribute("fuentesCoordenadas", FuenteCoordenadas.values());
        model.addAttribute("tiposPredio", TipoPredio.values());
        model.addAttribute("moduloActivo", "tributaria");
        model.addAttribute("submenuActivo", "predios");

        return "admin/tributaria/predios/form";
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_PREDIOS_CREAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_JEFE_OFICINA')")
    public String registrarPredio(@Valid @ModelAttribute("request") PredioCreateRequest request,
                                  BindingResult bindingResult,
                                  RedirectAttributes redirectAttributes,
                                  Model model) {
        log.info("Procesando registro de predio municipal: {}", request.getCodigoCatastral());

        if (bindingResult.hasErrors()) {
            model.addAttribute("clasificaciones", ClasificacionPredio.values());
            model.addAttribute("usos", UsoDeclaradoPredio.values());
            model.addAttribute("fuentesCoordenadas", FuenteCoordenadas.values());
            model.addAttribute("tiposPredio", TipoPredio.values());
            model.addAttribute("moduloActivo", "tributaria");
            model.addAttribute("submenuActivo", "predios");
            return "admin/tributaria/predios/form";
        }

        try {
            PredioDetailResponse predio = predioService.registrarPredio(request);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Predio registrado exitosamente con código interno " + predio.getCodigoPredio());
            return "redirect:/dashboard/tributaria/predios/" + predio.getId();
        } catch (BusinessException | pe.gob.municipalidad.sigm.common.exception.DuplicateResourceException e) {
            log.warn("Error de validación al registrar predio: {}", e.getMessage());
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("clasificaciones", ClasificacionPredio.values());
            model.addAttribute("usos", UsoDeclaradoPredio.values());
            model.addAttribute("fuentesCoordenadas", FuenteCoordenadas.values());
            model.addAttribute("tiposPredio", TipoPredio.values());
            model.addAttribute("moduloActivo", "tributaria");
            model.addAttribute("submenuActivo", "predios");
            return "admin/tributaria/predios/form";
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_PREDIOS_VER', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_JEFE_OFICINA')")
    public String verDetalle(@PathVariable UUID id,
                             @RequestParam(value = "tab", defaultValue = "general") String tab,
                             Model model) {
        log.info("Accediendo a la ficha predial: id={}, tab={}", id, tab);

        PredioDetailResponse predio = predioService.obtenerPredioPorId(id);

        model.addAttribute("predio", predio);
        model.addAttribute("tabActiva", tab);
        model.addAttribute("materiales", MaterialPredominante.values());
        model.addAttribute("estadosConservacion", EstadoConservacion.values());
        model.addAttribute("tiposRelacion", TipoRelacionContribuyente.values());
        model.addAttribute("tiposDocumento", TipoDocumentoPredio.values());
        model.addAttribute("estadosPredio", EstadoPredio.values());
        model.addAttribute("moduloActivo", "tributaria");
        model.addAttribute("submenuActivo", "predios");

        return "admin/tributaria/predios/detail";
    }

    @GetMapping("/{id}/editar")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_PREDIOS_EDITAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_JEFE_OFICINA')")
    public String editarPredio(@PathVariable UUID id, Model model) {
        log.info("Abriendo formulario de edición para predio: {}", id);

        PredioDetailResponse predio = predioService.obtenerPredioPorId(id);

        if (!model.containsAttribute("request")) {
            PredioUpdateRequest request = PredioUpdateRequest.builder()
                    .codigoCatastral(predio.getCodigoCatastral())
                    .tipoPredio(predio.getTipoPredio())
                    .clasificacionPredio(predio.getClasificacionPredio())
                    .departamento(predio.getDepartamento())
                    .provincia(predio.getProvincia())
                    .distrito(predio.getDistrito())
                    .ubigeo(predio.getUbigeo())
                    .comunidadCentroPoblado(predio.getComunidadCentroPoblado())
                    .tipoVia(predio.getTipoVia())
                    .nombreVia(predio.getNombreVia())
                    .numeroMunicipal(predio.getNumeroMunicipal())
                    .manzana(predio.getManzana())
                    .lote(predio.getLote())
                    .interior(predio.getInterior())
                    .sector(predio.getSector())
                    .zona(predio.getZona())
                    .referencia(predio.getReferencia())
                    .direccion(predio.getDireccion())
                    .areaTerreno(predio.getAreaTerreno())
                    .unidadArea(predio.getUnidadArea())
                    .usoDeclarado(predio.getUsoDeclarado())
                    .tieneAgua(predio.isTieneAgua())
                    .tieneDesague(predio.isTieneDesague())
                    .tieneLuz(predio.isTieneLuz())
                    .tieneAlumbradoPublico(predio.isTieneAlumbradoPublico())
                    .tienePistasVeredas(predio.isTienePistasVeredas())
                    .tieneLimpiezaPublica(predio.isTieneLimpiezaPublica())
                    .latitud(predio.getLatitud())
                    .longitud(predio.getLongitud())
                    .precisionMetros(predio.getPrecisionMetros())
                    .fuenteCoordenadas(predio.getFuenteCoordenadas())
                    .build();
            model.addAttribute("request", request);
        }

        model.addAttribute("predioId", id);
        model.addAttribute("codigoPredio", predio.getCodigoPredio());
        model.addAttribute("clasificaciones", ClasificacionPredio.values());
        model.addAttribute("usos", UsoDeclaradoPredio.values());
        model.addAttribute("fuentesCoordenadas", FuenteCoordenadas.values());
        model.addAttribute("tiposPredio", TipoPredio.values());
        model.addAttribute("moduloActivo", "tributaria");
        model.addAttribute("submenuActivo", "predios");

        return "admin/tributaria/predios/edit";
    }

    @PostMapping("/{id}/actualizar")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_PREDIOS_EDITAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_JEFE_OFICINA')")
    public String actualizarPredio(@PathVariable UUID id,
                                   @Valid @ModelAttribute("request") PredioUpdateRequest request,
                                   BindingResult bindingResult,
                                   RedirectAttributes redirectAttributes,
                                   Model model) {
        log.info("Procesando actualización de predio: {}", id);

        if (bindingResult.hasErrors()) {
            cargarContextoEdicionPredio(model, id);
            return "admin/tributaria/predios/edit";
        }

        try {
            predioService.actualizarPredio(id, request);
            redirectAttributes.addFlashAttribute("successMessage", "Predio actualizado exitosamente");
            return "redirect:/dashboard/tributaria/predios/" + id;
        } catch (BusinessException | pe.gob.municipalidad.sigm.common.exception.DuplicateResourceException e) {
            log.warn("Error al actualizar predio {}: {}", id, e.getMessage());
            model.addAttribute("errorMessage", e.getMessage());
            cargarContextoEdicionPredio(model, id);
            return "admin/tributaria/predios/edit";
        }
    }

    /**
     * Datos que la vista de edición necesita para volver a mostrarse tras un error
     * (antes faltaba el código del predio en la cabecera y el título de la página).
     */
    private void cargarContextoEdicionPredio(Model model, UUID id) {
        model.addAttribute("predioId", id);
        model.addAttribute("codigoPredio", predioService.obtenerPredioPorId(id).getCodigoPredio());
        model.addAttribute("clasificaciones", ClasificacionPredio.values());
        model.addAttribute("usos", UsoDeclaradoPredio.values());
        model.addAttribute("fuentesCoordenadas", FuenteCoordenadas.values());
        model.addAttribute("tiposPredio", TipoPredio.values());
        model.addAttribute("moduloActivo", "tributaria");
        model.addAttribute("submenuActivo", "predios");
    }

    @PostMapping("/{id}/estado")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_PREDIOS_CAMBIAR_ESTADO', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_JEFE_OFICINA')")
    public String cambiarEstado(@PathVariable UUID id,
                                @Valid @ModelAttribute CambioEstadoPredioRequest request,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Debe proporcionar un estado válido y motivo justificado.");
            return "redirect:/dashboard/tributaria/predios/" + id;
        }

        try {
            predioService.cambiarEstado(id, request);
            redirectAttributes.addFlashAttribute("successMessage", "Estado administrativo del predio actualizado a " + request.getNuevoEstado());
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/dashboard/tributaria/predios/" + id;
    }

    @PostMapping("/{id}/construcciones")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_PREDIOS_GESTIONAR_CONSTRUCCIONES', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_JEFE_OFICINA')")
    public String agregarConstruccion(@PathVariable UUID id,
                                      @Valid @ModelAttribute ConstruccionPredialRequest request,
                                      BindingResult bindingResult,
                                      RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error en los datos de la construcción. Verifique los campos requeridos.");
            return "redirect:/dashboard/tributaria/predios/" + id + "?tab=construcciones";
        }

        try {
            predioService.agregarConstruccion(id, request);
            redirectAttributes.addFlashAttribute("successMessage", "Construcción o edificación agregada exitosamente");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/dashboard/tributaria/predios/" + id + "?tab=construcciones";
    }

    @PostMapping("/{id}/construcciones/{cid}/eliminar")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_PREDIOS_GESTIONAR_CONSTRUCCIONES', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_JEFE_OFICINA')")
    public String eliminarConstruccion(@PathVariable UUID id,
                                       @PathVariable UUID cid,
                                       RedirectAttributes redirectAttributes) {
        try {
            predioService.eliminarConstruccion(id, cid);
            redirectAttributes.addFlashAttribute("successMessage", "Construcción eliminada exitosamente");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/dashboard/tributaria/predios/" + id + "?tab=construcciones";
    }

    @PostMapping("/{id}/contribuyentes")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_PREDIOS_GESTIONAR_RELACIONES', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_JEFE_OFICINA')")
    public String vincularContribuyente(@PathVariable UUID id,
                                        @Valid @ModelAttribute RelacionContribuyentePredioRequest request,
                                        BindingResult bindingResult,
                                        RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Datos de vinculación incompletos o inválidos.");
            return "redirect:/dashboard/tributaria/predios/" + id + "?tab=contribuyentes";
        }

        try {
            predioService.vincularContribuyente(id, request);
            redirectAttributes.addFlashAttribute("successMessage", "Contribuyente vinculado al predio con éxito");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/dashboard/tributaria/predios/" + id + "?tab=contribuyentes";
    }

    @PostMapping("/{id}/contribuyentes/{rid}/eliminar")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_PREDIOS_GESTIONAR_RELACIONES', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_JEFE_OFICINA')")
    public String desvincularContribuyente(@PathVariable UUID id,
                                          @PathVariable UUID rid,
                                          RedirectAttributes redirectAttributes) {
        try {
            predioService.desvincularContribuyente(id, rid);
            redirectAttributes.addFlashAttribute("successMessage", "Relación de contribuyente finalizada exitosamente");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/dashboard/tributaria/predios/" + id + "?tab=contribuyentes";
    }

    @PostMapping("/{id}/documentos")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_PREDIOS_GESTIONAR_DOCUMENTOS', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_JEFE_OFICINA')")
    public String agregarDocumento(@PathVariable UUID id,
                                   @Valid @ModelAttribute DocumentoPredioRequest request,
                                   BindingResult bindingResult,
                                   RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Datos del documento de sustento inválidos.");
            return "redirect:/dashboard/tributaria/predios/" + id + "?tab=documentos";
        }

        try {
            predioService.agregarDocumento(id, request);
            redirectAttributes.addFlashAttribute("successMessage", "Documento de sustento registrado exitosamente");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/dashboard/tributaria/predios/" + id + "?tab=documentos";
    }

    @PostMapping("/{id}/documentos/{did}/eliminar")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_PREDIOS_GESTIONAR_DOCUMENTOS', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_JEFE_OFICINA')")
    public String eliminarDocumento(@PathVariable UUID id,
                                    @PathVariable UUID did,
                                    RedirectAttributes redirectAttributes) {
        try {
            predioService.eliminarDocumento(id, did);
            redirectAttributes.addFlashAttribute("successMessage", "Documento predial eliminado del registro");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/dashboard/tributaria/predios/" + id + "?tab=documentos";
    }

    @GetMapping("/exportar/excel")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_PREDIOS_EXPORTAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_JEFE_OFICINA')")
    public void exportarExcel(@RequestParam(value = "query", required = false) String query,
                              @RequestParam(value = "clasificacion", required = false) ClasificacionPredio clasificacion,
                              @RequestParam(value = "uso", required = false) UsoDeclaradoPredio uso,
                              @RequestParam(value = "sector", required = false) String sector,
                              @RequestParam(value = "zona", required = false) String zona,
                              @RequestParam(value = "estado", required = false) EstadoPredio estado,
                              HttpServletResponse response) {
        log.info("Generando exportación de predios a Excel");
        try {
            PredioFilterDto filtro = PredioFilterDto.builder()
                    .query(query)
                    .clasificacion(clasificacion)
                    .uso(uso)
                    .sector(sector)
                    .zona(zona)
                    .estado(estado)
                    .build();

            byte[] bytes = exportService.exportarExcel(filtro);
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"padron_predios_quinua_" + timestamp + ".xlsx\"");
            response.getOutputStream().write(bytes);
            response.getOutputStream().flush();
        } catch (Exception e) {
            log.error("Error al exportar predios a Excel", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/exportar/pdf")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_PREDIOS_EXPORTAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_JEFE_OFICINA')")
    public void exportarPdf(@RequestParam(value = "query", required = false) String query,
                            @RequestParam(value = "clasificacion", required = false) ClasificacionPredio clasificacion,
                            @RequestParam(value = "uso", required = false) UsoDeclaradoPredio uso,
                            @RequestParam(value = "sector", required = false) String sector,
                            @RequestParam(value = "zona", required = false) String zona,
                            @RequestParam(value = "estado", required = false) EstadoPredio estado,
                            HttpServletResponse response) {
        log.info("Generando exportación de predios a PDF");
        try {
            PredioFilterDto filtro = PredioFilterDto.builder()
                    .query(query)
                    .clasificacion(clasificacion)
                    .uso(uso)
                    .sector(sector)
                    .zona(zona)
                    .estado(estado)
                    .build();

            byte[] bytes = exportService.exportarPdf(filtro);
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

            response.setContentType(MediaType.APPLICATION_PDF_VALUE);
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"padron_predios_quinua_" + timestamp + ".pdf\"");
            response.getOutputStream().write(bytes);
            response.getOutputStream().flush();
        } catch (Exception e) {
            log.error("Error al exportar predios a PDF", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
