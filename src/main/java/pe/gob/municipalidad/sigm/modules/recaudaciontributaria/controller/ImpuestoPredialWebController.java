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
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request.CalculoPredialCreateRequest;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request.CalculoPredialFilterDto;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request.CambioEstadoCalculoRequest;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request.SimulacionPredialRequest;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response.CalculoPredialDetailResponse;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response.CalculoPredialSummaryDto;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response.HistorialCalculoResponse;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoCalculoPredial;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoBeneficioPredial;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoCalculoPredial;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.ContribuyenteTributarioRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.PeriodoTributarioRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.CalculoPredialService;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.ImpuestoPredialExportService;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Controller
@RequestMapping("/dashboard/tributaria/impuesto-predial")
@RequiredArgsConstructor
public class ImpuestoPredialWebController {

    private final CalculoPredialService calculoService;
    private final ImpuestoPredialExportService exportService;
    private final ContribuyenteTributarioRepository contribuyenteRepository;
    private final PeriodoTributarioRepository periodoTributarioRepository;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_PREDIAL_VER', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_JEFE_OFICINA', 'ROLE_ESPECIALISTA_TRIBUTARIO')")
    public String listarCalculos(@ModelAttribute CalculoPredialFilterDto filtro,
                                 @RequestParam(value = "page", defaultValue = "0") int page,
                                 @RequestParam(value = "size", defaultValue = "10") int size,
                                 Model model) {
        log.info("Listando determinaciones prediales: ejercicio={}, estado={}", filtro.getEjercicioFiscal(), filtro.getEstado());
        Page<CalculoPredialSummaryDto> calculos = calculoService.listarCalculos(
                filtro,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "fechaCalculo"))
        );

        Map<String, Object> stats = calculoService.obtenerEstadisticas();

        model.addAttribute("calculos", calculos);
        model.addAttribute("filtro", filtro);
        model.addAttribute("stats", stats);
        model.addAttribute("estados", EstadoCalculoPredial.values());
        model.addAttribute("tiposCalculo", TipoCalculoPredial.values());
        model.addAttribute("moduloActivo", "tributaria");
        model.addAttribute("submenuActivo", "impuesto-predial");
        return "admin/tributaria/predial/list";
    }

    @GetMapping("/simular")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_PREDIAL_SIMULAR', 'TRIBUTARIA_PREDIAL_VER', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_JEFE_OFICINA', 'ROLE_ESPECIALISTA_TRIBUTARIO')")
    public String formularioSimular(Model model) {
        model.addAttribute("request", SimulacionPredialRequest.builder()
                .fechaReferencia(LocalDate.now())
                .beneficio(TipoBeneficioPredial.NINGUNO)
                .build());
        model.addAttribute("contribuyentes", contribuyenteRepository.findAll(Sort.by("nombreRazonSocial").ascending()));
        model.addAttribute("periodos", periodoTributarioRepository.findAllByOrderByAnioDesc());
        model.addAttribute("beneficios", TipoBeneficioPredial.values());
        model.addAttribute("moduloActivo", "tributaria");
        model.addAttribute("submenuActivo", "impuesto-predial");
        model.addAttribute("esNuevoCalculo", false);
        return "admin/tributaria/predial/simular";
    }

    @PostMapping("/simular")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_PREDIAL_SIMULAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_JEFE_OFICINA', 'ROLE_ESPECIALISTA_TRIBUTARIO')")
    public String ejecutarSimulacion(@Valid @ModelAttribute("request") SimulacionPredialRequest request,
                                     BindingResult bindingResult,
                                     Principal principal,
                                     Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("contribuyentes", contribuyenteRepository.findAll(Sort.by("nombreRazonSocial").ascending()));
            model.addAttribute("periodos", periodoTributarioRepository.findAllByOrderByAnioDesc());
            model.addAttribute("beneficios", TipoBeneficioPredial.values());
            model.addAttribute("moduloActivo", "tributaria");
            model.addAttribute("submenuActivo", "impuesto-predial");
            model.addAttribute("esNuevoCalculo", false);
            return "admin/tributaria/predial/simular";
        }

        try {
            String username = principal != null ? principal.getName() : "SISTEMA";
            CalculoPredialDetailResponse resultado = calculoService.simularCalculo(request, username);
            model.addAttribute("resultado", resultado);
            model.addAttribute("request", request);
            model.addAttribute("contribuyentes", contribuyenteRepository.findAll(Sort.by("nombreRazonSocial").ascending()));
            model.addAttribute("periodos", periodoTributarioRepository.findAllByOrderByAnioDesc());
            model.addAttribute("beneficios", TipoBeneficioPredial.values());
            model.addAttribute("moduloActivo", "tributaria");
            model.addAttribute("submenuActivo", "impuesto-predial");
            model.addAttribute("simulacionExitosa", true);
            model.addAttribute("esNuevoCalculo", false);
            return "admin/tributaria/predial/simular";
        } catch (BusinessException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("contribuyentes", contribuyenteRepository.findAll(Sort.by("nombreRazonSocial").ascending()));
            model.addAttribute("periodos", periodoTributarioRepository.findAllByOrderByAnioDesc());
            model.addAttribute("beneficios", TipoBeneficioPredial.values());
            model.addAttribute("moduloActivo", "tributaria");
            model.addAttribute("submenuActivo", "impuesto-predial");
            model.addAttribute("esNuevoCalculo", false);
            return "admin/tributaria/predial/simular";
        }
    }

    @GetMapping("/nuevo")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_PREDIAL_CALCULAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_JEFE_OFICINA', 'ROLE_ESPECIALISTA_TRIBUTARIO')")
    public String formularioNuevo(Model model) {
        model.addAttribute("request", CalculoPredialCreateRequest.builder()
                .fechaReferencia(LocalDate.now())
                .beneficio(TipoBeneficioPredial.NINGUNO)
                .build());
        model.addAttribute("contribuyentes", contribuyenteRepository.findAll(Sort.by("nombreRazonSocial").ascending()));
        model.addAttribute("periodos", periodoTributarioRepository.findAllByOrderByAnioDesc());
        model.addAttribute("beneficios", TipoBeneficioPredial.values());
        model.addAttribute("moduloActivo", "tributaria");
        model.addAttribute("submenuActivo", "impuesto-predial");
        model.addAttribute("esNuevoCalculo", true);
        return "admin/tributaria/predial/simular";
    }

    @PostMapping("/nuevo")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_PREDIAL_CALCULAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_JEFE_OFICINA', 'ROLE_ESPECIALISTA_TRIBUTARIO')")
    public String guardarCalculoPreliminar(@Valid @ModelAttribute("request") CalculoPredialCreateRequest request,
                                           BindingResult bindingResult,
                                           Principal principal,
                                           RedirectAttributes redirectAttributes,
                                           Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("contribuyentes", contribuyenteRepository.findAll(Sort.by("nombreRazonSocial").ascending()));
            model.addAttribute("periodos", periodoTributarioRepository.findAllByOrderByAnioDesc());
            model.addAttribute("beneficios", TipoBeneficioPredial.values());
            model.addAttribute("moduloActivo", "tributaria");
            model.addAttribute("submenuActivo", "impuesto-predial");
            model.addAttribute("esNuevoCalculo", true);
            return "admin/tributaria/predial/simular";
        }

        try {
            String username = principal != null ? principal.getName() : "SISTEMA";
            CalculoPredialDetailResponse resultado = calculoService.registrarCalculo(request, username);
            redirectAttributes.addFlashAttribute("success", "Cálculo preliminar generado exitosamente con código " + resultado.getCodigoCalculo());
            return "redirect:/dashboard/tributaria/impuesto-predial/calculos/" + resultado.getId();
        } catch (BusinessException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("contribuyentes", contribuyenteRepository.findAll(Sort.by("nombreRazonSocial").ascending()));
            model.addAttribute("periodos", periodoTributarioRepository.findAllByOrderByAnioDesc());
            model.addAttribute("beneficios", TipoBeneficioPredial.values());
            model.addAttribute("moduloActivo", "tributaria");
            model.addAttribute("submenuActivo", "impuesto-predial");
            model.addAttribute("esNuevoCalculo", true);
            return "admin/tributaria/predial/simular";
        }
    }

    @GetMapping("/calculos/{id}")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_PREDIAL_VER', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_JEFE_OFICINA', 'ROLE_ESPECIALISTA_TRIBUTARIO')")
    public String verDetalleCalculo(@PathVariable UUID id, Model model) {
        CalculoPredialDetailResponse calculo = calculoService.obtenerCalculoPorId(id);
        List<HistorialCalculoResponse> historial = calculoService.obtenerHistorial(id);

        model.addAttribute("c", calculo);
        model.addAttribute("historial", historial);
        model.addAttribute("moduloActivo", "tributaria");
        model.addAttribute("submenuActivo", "impuesto-predial");
        return "admin/tributaria/predial/detail";
    }

    @PostMapping("/calculos/{id}/revision")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_PREDIAL_REVISAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_JEFE_OFICINA')")
    public String enviarARevision(@PathVariable UUID id,
                                  @ModelAttribute CambioEstadoCalculoRequest request,
                                  Principal principal,
                                  RedirectAttributes redirectAttributes) {
        String username = principal != null ? principal.getName() : "SISTEMA";
        try {
            calculoService.enviarARevision(id, request != null ? request.getMotivo() : "Pase a revisión", username);
            redirectAttributes.addFlashAttribute("success", "El cálculo fue derivado al estado EN REVISIÓN administrativa.");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/dashboard/tributaria/impuesto-predial/calculos/" + id;
    }

    @PostMapping("/calculos/{id}/observaciones")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_PREDIAL_REVISAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_JEFE_OFICINA')")
    public String observarCalculo(@PathVariable UUID id,
                                  @ModelAttribute CambioEstadoCalculoRequest request,
                                  Principal principal,
                                  RedirectAttributes redirectAttributes) {
        String username = principal != null ? principal.getName() : "SISTEMA";
        try {
            calculoService.registrarObservaciones(id, request != null ? request.getMotivo() : "Observaciones técnicas", username);
            redirectAttributes.addFlashAttribute("warning", "El cálculo ha sido marcado como OBSERVADO con los motivos indicados.");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/dashboard/tributaria/impuesto-predial/calculos/" + id;
    }

    @PostMapping("/calculos/{id}/aprobar")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_PREDIAL_APROBAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_JEFE_OFICINA')")
    public String aprobarCalculo(@PathVariable UUID id,
                                 @ModelAttribute CambioEstadoCalculoRequest request,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) {
        String username = principal != null ? principal.getName() : "SISTEMA";
        String resolucion = (request != null && request.getResolucionSustento() != null) ? request.getResolucionSustento() : "RESOLUCION-PREDIAL-AUTORIZADA";
        String motivo = (request != null && request.getMotivo() != null) ? request.getMotivo() : "Aprobación formal de determinación predial";
        try {
            calculoService.aprobarCalculo(id, resolucion, motivo, username);
            redirectAttributes.addFlashAttribute("success", "Determinación tributaria APROBADA formalmente. No genera obligaciones en cuenta corriente hasta la emisión del P57.");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/dashboard/tributaria/impuesto-predial/calculos/" + id;
    }

    @PostMapping("/calculos/{id}/anular")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_PREDIAL_ANULAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public String anularCalculo(@PathVariable UUID id,
                                @ModelAttribute CambioEstadoCalculoRequest request,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {
        String username = principal != null ? principal.getName() : "SISTEMA";
        try {
            calculoService.anularCalculo(id, request != null ? request.getMotivo() : "Anulación formal", username);
            redirectAttributes.addFlashAttribute("info", "El cálculo predial ha sido ANULADO.");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/dashboard/tributaria/impuesto-predial/calculos/" + id;
    }

    @PostMapping("/calculos/{id}/recalcular")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_PREDIAL_CALCULAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_JEFE_OFICINA')")
    public String recalcular(@PathVariable UUID id,
                             @RequestParam("motivo") String motivo,
                             Principal principal,
                             RedirectAttributes redirectAttributes) {
        String username = principal != null ? principal.getName() : "SISTEMA";
        try {
            CalculoPredialDetailResponse nuevo = calculoService.recalcularNuevaVersion(id, motivo, username);
            redirectAttributes.addFlashAttribute("success", "Nueva versión generada: " + nuevo.getCodigoCalculo() + " (v" + nuevo.getVersionCalculo() + ")");
            return "redirect:/dashboard/tributaria/impuesto-predial/calculos/" + nuevo.getId();
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/dashboard/tributaria/impuesto-predial/calculos/" + id;
        }
    }

    @GetMapping("/exportar/excel")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_PREDIAL_EXPORTAR', 'TRIBUTARIA_PREDIAL_VER', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public ResponseEntity<byte[]> exportarExcel(@ModelAttribute CalculoPredialFilterDto filtro) {
        byte[] contenido = exportService.exportarExcel(filtro);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"determinaciones_prediales_quinua.xlsx\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(contenido);
    }

    @GetMapping("/exportar/pdf")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_PREDIAL_EXPORTAR', 'TRIBUTARIA_PREDIAL_VER', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public ResponseEntity<byte[]> exportarPdf(@ModelAttribute CalculoPredialFilterDto filtro) {
        byte[] contenido = exportService.exportarPdf(filtro);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"reporte_determinaciones_prediales.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(contenido);
    }

    @GetMapping("/calculos/{id}/pdf")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_PREDIAL_EXPORTAR', 'TRIBUTARIA_PREDIAL_VER', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public ResponseEntity<byte[]> exportarHojaLiquidacion(@PathVariable UUID id) {
        byte[] contenido = exportService.exportarHojaLiquidacionPdf(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"hoja_liquidacion_predial_" + id + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(contenido);
    }
}
