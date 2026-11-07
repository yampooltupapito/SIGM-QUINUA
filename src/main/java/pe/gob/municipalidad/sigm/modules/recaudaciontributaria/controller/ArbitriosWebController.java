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
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request.ArbitrioFilterDto;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request.CalculoArbitrioCreateRequest;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request.CambioEstadoArbitrioRequest;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request.SimulacionArbitrioRequest;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response.ArbitrioDetailResponse;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response.ArbitrioSummaryDto;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response.ConfiguracionArbitrioSummaryDto;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.PredioTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.RelacionContribuyentePredio;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoCalculoArbitrio;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoRelacionContribuyentePredio;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoBeneficioArbitrio;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoCalculoArbitrio;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.ContribuyenteTributarioRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.PeriodoTributarioRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.PredioTributarioRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.RelacionContribuyentePredioRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.ArbitrioExportService;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.ArbitrioMunicipalService;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Controller
@RequestMapping("/dashboard/tributaria/arbitrios")
@RequiredArgsConstructor
public class ArbitriosWebController {

    private final ArbitrioMunicipalService arbitrioService;
    private final ArbitrioExportService exportService;
    private final ContribuyenteTributarioRepository contribuyenteRepository;
    private final PredioTributarioRepository predioRepository;
    private final RelacionContribuyentePredioRepository relacionRepository;
    private final PeriodoTributarioRepository periodoTributarioRepository;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_ARBITRIOS_VER', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_JEFE_OFICINA', 'ROLE_ESPECIALISTA_TRIBUTARIO')")
    public String listarCalculos(@ModelAttribute ArbitrioFilterDto filtro,
                                 @RequestParam(value = "page", defaultValue = "0") int page,
                                 @RequestParam(value = "size", defaultValue = "10") int size,
                                 Model model) {
        log.info("Listando determinaciones de arbitrios: periodo={}, estado={}", filtro.getEjercicioFiscal(), filtro.getEstado());
        Page<ArbitrioSummaryDto> calculos = arbitrioService.listarCalculos(
                filtro,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "fechaCalculo"))
        );

        Map<String, Object> stats = arbitrioService.obtenerEstadisticas();
        List<ConfiguracionArbitrioSummaryDto> configuraciones = arbitrioService.listarConfiguracionesActivas();

        model.addAttribute("calculos", calculos);
        model.addAttribute("filtro", filtro);
        model.addAttribute("stats", stats);
        model.addAttribute("configuraciones", configuraciones);
        model.addAttribute("estados", EstadoCalculoArbitrio.values());
        model.addAttribute("tiposCalculo", TipoCalculoArbitrio.values());
        model.addAttribute("servicios", arbitrioService.listarServicios());
        model.addAttribute("periodos", periodoTributarioRepository.findAllByOrderByAnioDesc());
        model.addAttribute("moduloActivo", "tributaria");
        model.addAttribute("submenuActivo", "arbitrios");
        return "admin/tributaria/arbitrios/list";
    }

    @GetMapping("/simular")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_ARBITRIOS_SIMULAR', 'TRIBUTARIA_ARBITRIOS_VER', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_JEFE_OFICINA', 'ROLE_ESPECIALISTA_TRIBUTARIO')")
    public String formularioSimular(Model model) {
        model.addAttribute("request", SimulacionArbitrioRequest.builder()
                .fechaReferencia(LocalDate.now())
                .beneficio(TipoBeneficioArbitrio.NINGUNO)
                .porcentajeCondomino(new BigDecimal("100.00"))
                .build());
        cargarDatosFormulario(model);
        model.addAttribute("esNuevoCalculo", false);
        return "admin/tributaria/arbitrios/simular";
    }

    @PostMapping("/simular")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_ARBITRIOS_SIMULAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_JEFE_OFICINA', 'ROLE_ESPECIALISTA_TRIBUTARIO')")
    public String ejecutarSimulacion(@Valid @ModelAttribute("request") SimulacionArbitrioRequest request,
                                     BindingResult bindingResult,
                                     Principal principal,
                                     Model model) {
        if (bindingResult.hasErrors()) {
            cargarDatosFormulario(model);
            model.addAttribute("esNuevoCalculo", false);
            return "admin/tributaria/arbitrios/simular";
        }

        try {
            String username = principal != null ? principal.getName() : "SISTEMA";
            ArbitrioDetailResponse resultado = arbitrioService.simularCalculo(request, username);
            model.addAttribute("resultado", resultado);
            model.addAttribute("esSimulacion", true);
            cargarDatosFormulario(model);
            model.addAttribute("esNuevoCalculo", false);
            return "admin/tributaria/arbitrios/simular";
        } catch (BusinessException e) {
            log.warn("Error de negocio en simulación de arbitrios: {}", e.getMessage());
            model.addAttribute("errorNegocio", e.getMessage());
            cargarDatosFormulario(model);
            model.addAttribute("esNuevoCalculo", false);
            return "admin/tributaria/arbitrios/simular";
        } catch (Exception e) {
            log.error("Error inesperado en simulación de arbitrios", e);
            model.addAttribute("errorNegocio", "Error inesperado al ejecutar la simulación: " + e.getMessage());
            cargarDatosFormulario(model);
            model.addAttribute("esNuevoCalculo", false);
            return "admin/tributaria/arbitrios/simular";
        }
    }

    @GetMapping("/nuevo")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_ARBITRIOS_CALCULAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_JEFE_OFICINA', 'ROLE_ESPECIALISTA_TRIBUTARIO')")
    public String formularioNuevoCalculo(Model model) {
        model.addAttribute("request", CalculoArbitrioCreateRequest.builder()
                .fechaReferencia(LocalDate.now())
                .beneficio(TipoBeneficioArbitrio.NINGUNO)
                .porcentajeCondomino(new BigDecimal("100.00"))
                .build());
        cargarDatosFormulario(model);
        model.addAttribute("esNuevoCalculo", true);
        return "admin/tributaria/arbitrios/simular";
    }

    @PostMapping("/nuevo")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_ARBITRIOS_CALCULAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_JEFE_OFICINA', 'ROLE_ESPECIALISTA_TRIBUTARIO')")
    public String crearCalculoPreliminar(@Valid @ModelAttribute("request") CalculoArbitrioCreateRequest request,
                                         BindingResult bindingResult,
                                         Principal principal,
                                         RedirectAttributes redirectAttributes,
                                         Model model) {
        if (bindingResult.hasErrors()) {
            cargarDatosFormulario(model);
            model.addAttribute("esNuevoCalculo", true);
            return "admin/tributaria/arbitrios/simular";
        }

        try {
            String username = principal != null ? principal.getName() : "SISTEMA";
            ArbitrioDetailResponse nuevo = arbitrioService.crearCalculoPreliminar(request, username);
            redirectAttributes.addFlashAttribute("mensajeExito",
                    "Determinación preliminar de arbitrios generada exitosamente con código " + nuevo.getCodigoCalculo() + " (Estado: " + nuevo.getEstado().getDescripcion() + ")");
            return "redirect:/dashboard/tributaria/arbitrios/calculos/" + nuevo.getId();
        } catch (BusinessException e) {
            log.warn("Error de negocio al generar determinación preliminar: {}", e.getMessage());
            model.addAttribute("errorNegocio", e.getMessage());
            cargarDatosFormulario(model);
            model.addAttribute("esNuevoCalculo", true);
            return "admin/tributaria/arbitrios/simular";
        } catch (Exception e) {
            log.error("Error inesperado al generar determinación preliminar", e);
            model.addAttribute("errorNegocio", "Error inesperado al generar determinación: " + e.getMessage());
            cargarDatosFormulario(model);
            model.addAttribute("esNuevoCalculo", true);
            return "admin/tributaria/arbitrios/simular";
        }
    }

    @GetMapping("/calculos/{id}")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_ARBITRIOS_VER', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_JEFE_OFICINA', 'ROLE_ESPECIALISTA_TRIBUTARIO')")
    public String verDetalleCalculo(@PathVariable UUID id, Model model) {
        ArbitrioDetailResponse calculo = arbitrioService.obtenerCalculoPorId(id);
        model.addAttribute("calculo", calculo);
        model.addAttribute("moduloActivo", "tributaria");
        model.addAttribute("submenuActivo", "arbitrios");
        return "admin/tributaria/arbitrios/detail";
    }

    @PostMapping("/calculos/{id}/revision")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_ARBITRIOS_REVISAR_CALCULO', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_JEFE_OFICINA')")
    public String enviarARevision(@PathVariable UUID id,
                                  Principal principal,
                                  RedirectAttributes redirectAttributes) {
        String username = principal != null ? principal.getName() : "SISTEMA";
        arbitrioService.cambiarEstado(id, CambioEstadoArbitrioRequest.builder()
                .nuevoEstado(EstadoCalculoArbitrio.EN_REVISION)
                .motivo("Enviado a revisión técnica")
                .build(), username);
        redirectAttributes.addFlashAttribute("mensajeExito", "La determinación ha pasado a estado EN REVISIÓN");
        return "redirect:/dashboard/tributaria/arbitrios/calculos/" + id;
    }

    @PostMapping("/calculos/{id}/observar")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_ARBITRIOS_REVISAR_CALCULO', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_JEFE_OFICINA')")
    public String observarCalculo(@PathVariable UUID id,
                                  @RequestParam("motivo") String motivo,
                                  Principal principal,
                                  RedirectAttributes redirectAttributes) {
        String username = principal != null ? principal.getName() : "SISTEMA";
        arbitrioService.cambiarEstado(id, CambioEstadoArbitrioRequest.builder()
                .nuevoEstado(EstadoCalculoArbitrio.OBSERVADO)
                .motivo(motivo)
                .build(), username);
        redirectAttributes.addFlashAttribute("mensajeExito", "La determinación ha sido OBSERVADA debidamente");
        return "redirect:/dashboard/tributaria/arbitrios/calculos/" + id;
    }

    @PostMapping("/calculos/{id}/aprobar")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_ARBITRIOS_APROBAR_CALCULO', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_JEFE_OFICINA')")
    public String aprobarCalculo(@PathVariable UUID id,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) {
        String username = principal != null ? principal.getName() : "SISTEMA";
        arbitrioService.aprobarCalculo(id, username);
        redirectAttributes.addFlashAttribute("mensajeExito", "Determinación de arbitrios APROBADA satisfactoriamente. Consérvese en archivo tributario.");
        return "redirect:/dashboard/tributaria/arbitrios/calculos/" + id;
    }

    @PostMapping("/calculos/{id}/anular")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_ARBITRIOS_ANULAR_CALCULO', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_JEFE_OFICINA')")
    public String anularCalculo(@PathVariable UUID id,
                                @RequestParam("motivo") String motivo,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {
        String username = principal != null ? principal.getName() : "SISTEMA";
        arbitrioService.anularCalculo(id, motivo, username);
        redirectAttributes.addFlashAttribute("mensajeExito", "La determinación de arbitrios ha sido ANULADA debidamente");
        return "redirect:/dashboard/tributaria/arbitrios/calculos/" + id;
    }

    @PostMapping("/calculos/{id}/recalcular")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_ARBITRIOS_CALCULAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_JEFE_OFICINA')")
    public String recalcular(@PathVariable UUID id,
                             @RequestParam("motivo") String motivo,
                             Principal principal,
                             RedirectAttributes redirectAttributes) {
        String username = principal != null ? principal.getName() : "SISTEMA";
        ArbitrioDetailResponse nuevaVersion = arbitrioService.recalcular(id, motivo, username);
        redirectAttributes.addFlashAttribute("mensajeExito",
                "Nueva versión generada: " + nuevaVersion.getCodigoCalculo() + " (v" + nuevaVersion.getVersionCalculo() + ")");
        return "redirect:/dashboard/tributaria/arbitrios/calculos/" + nuevaVersion.getId();
    }

    @GetMapping("/exportar/excel")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_ARBITRIOS_EXPORTAR', 'TRIBUTARIA_ARBITRIOS_VER', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public ResponseEntity<byte[]> exportarExcel(@ModelAttribute ArbitrioFilterDto filtro) {
        byte[] bytes = exportService.exportarExcel(filtro);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"arbitrios_quinua.xlsx\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }

    @GetMapping("/exportar/pdf")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_ARBITRIOS_EXPORTAR', 'TRIBUTARIA_ARBITRIOS_VER', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public ResponseEntity<byte[]> exportarPdf(@ModelAttribute ArbitrioFilterDto filtro) {
        byte[] bytes = exportService.exportarPdf(filtro);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"reporte_arbitrios_quinua.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(bytes);
    }

    @GetMapping("/calculos/{id}/liquidacion-pdf")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_ARBITRIOS_EXPORTAR', 'TRIBUTARIA_ARBITRIOS_VER', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public ResponseEntity<byte[]> exportarHojaLiquidacionPdf(@PathVariable UUID id) {
        byte[] bytes = exportService.exportarHojaLiquidacionPdf(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"hoja_liquidacion_arbitrios_" + id + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(bytes);
    }

    @GetMapping("/contribuyentes/{contribuyenteId}/predios")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> obtenerPrediosPorContribuyente(@PathVariable UUID contribuyenteId) {
        List<RelacionContribuyentePredio> relaciones = relacionRepository.findByContribuyenteIdAndEstado(
                contribuyenteId, EstadoRelacionContribuyentePredio.ACTIVO);

        List<Map<String, Object>> resultado = new ArrayList<>();
        for (RelacionContribuyentePredio rel : relaciones) {
            PredioTributario predio = rel.getPredio();
            Map<String, Object> map = new HashMap<>();
            map.put("id", predio.getId());
            map.put("codigoPredio", predio.getCodigoPredio());
            map.put("direccion", predio.getDireccionCompleta() != null ? predio.getDireccionCompleta() : predio.getDireccion());
            map.put("porcentajeCondomino", rel.getPorcentajeParticipacion() != null ? rel.getPorcentajeParticipacion() : new BigDecimal("100.00"));
            resultado.add(map);
        }
        return ResponseEntity.ok(resultado);
    }

    private void cargarDatosFormulario(Model model) {
        model.addAttribute("contribuyentes", contribuyenteRepository.findAll(Sort.by("nombreRazonSocial").ascending()));
        model.addAttribute("predios", predioRepository.findAll(Sort.by("codigoPredio").ascending()));
        model.addAttribute("periodos", periodoTributarioRepository.findAllByOrderByAnioDesc());
        model.addAttribute("beneficios", TipoBeneficioArbitrio.values());
        model.addAttribute("moduloActivo", "tributaria");
        model.addAttribute("submenuActivo", "arbitrios");
    }
}
