package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
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
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.cajapago.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoPagoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.MedioPagoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.ContribuyenteTributarioRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.CajaMunicipalService;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.PagoExportService;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.PagoTributarioService;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Controller
@RequestMapping("/dashboard/tributaria/pagos")
@RequiredArgsConstructor
public class PagoWebController {

    private final PagoTributarioService pagoService;
    private final CajaMunicipalService cajaService;
    private final PagoExportService exportService;
    private final ContribuyenteTributarioRepository contribuyenteRepository;
    private final UsuarioRepository usuarioRepository;

    @GetMapping
    @PreAuthorize("hasAuthority('TRIBUTARIA_PAGOS_VER') or hasAnyRole('ADMIN', 'SUPERADMIN', 'CAJERO', 'JEFE_OFICINA')")
    public String listarPagos(@RequestParam(value = "busqueda", required = false) String busqueda,
                              @RequestParam(value = "contribuyenteId", required = false) UUID contribuyenteId,
                              @RequestParam(value = "sesionCajaId", required = false) UUID sesionCajaId,
                              @RequestParam(value = "medioPago", required = false) MedioPagoTributario medioPago,
                              @RequestParam(value = "estado", required = false) EstadoPagoTributario estado,
                              @RequestParam(value = "desde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime desde,
                              @RequestParam(value = "hasta", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime hasta,
                              @RequestParam(value = "page", defaultValue = "0") int page,
                              @RequestParam(value = "size", defaultValue = "15") int size,
                              Model model) {

        Page<PagoResponse> pagos = pagoService.listarPagos(
                busqueda, contribuyenteId, sesionCajaId, medioPago, estado, desde, hasta,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "fechaHoraPago"))
        );

        model.addAttribute("pagos", pagos);
        model.addAttribute("busqueda", busqueda);
        model.addAttribute("contribuyenteId", contribuyenteId);
        model.addAttribute("sesionCajaId", sesionCajaId);
        model.addAttribute("medioPago", medioPago);
        model.addAttribute("estado", estado);
        model.addAttribute("mediosPago", MedioPagoTributario.values());
        model.addAttribute("estadosPago", EstadoPagoTributario.values());
        model.addAttribute("dashboard", cajaService.obtenerMetricasDashboard());

        model.addAttribute("moduloActivo", "tributaria");
        model.addAttribute("submenuActivo", "pagos");
        return "admin/tributaria/pagos/list";
    }

    @GetMapping("/nuevo")
    @PreAuthorize("hasAuthority('TRIBUTARIA_PAGOS_REGISTRAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'CAJERO')")
    public String nuevoPagoForm(@RequestParam(value = "contribuyenteId", required = false) UUID contribuyenteId,
                                @AuthenticationPrincipal UserDetails userDetails,
                                Model model) {

        Usuario cajero = userDetails != null ? usuarioRepository.findByUsername(userDetails.getUsername()).orElse(null) : null;
        Optional<SesionCajaResponse> sesionActiva = cajero != null ? cajaService.obtenerSesionActivaPorCajero(cajero.getId()) : Optional.empty();

        model.addAttribute("sesionActiva", sesionActiva.orElse(null));
        model.addAttribute("contribuyentes", contribuyenteRepository.findAll(Sort.by(Sort.Direction.ASC, "nombreRazonSocial")));
        model.addAttribute("mediosPago", MedioPagoTributario.values());
        model.addAttribute("selectedContribuyenteId", contribuyenteId);

        model.addAttribute("moduloActivo", "tributaria");
        model.addAttribute("submenuActivo", "pagos");
        return "admin/tributaria/pagos/form";
    }

    @PostMapping
    @PreAuthorize("hasAuthority('TRIBUTARIA_PAGOS_REGISTRAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'CAJERO')")
    public String registrarPago(@Valid @RequestBody RegistrarPagoRequest request,
                                BindingResult result,
                                @AuthenticationPrincipal UserDetails userDetails) {
        if (result.hasErrors()) {
            throw new BusinessException(result.getAllErrors().get(0).getDefaultMessage());
        }

        String username = userDetails != null ? userDetails.getUsername() : "CAJERO";
        PagoResponse response = pagoService.registrarPago(request, username);
        return "redirect:/dashboard/tributaria/pagos/" + response.getId();
    }

    @PostMapping("/form-submit")
    @PreAuthorize("hasAuthority('TRIBUTARIA_PAGOS_REGISTRAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'CAJERO')")
    public String registrarPagoForm(@ModelAttribute @Valid RegistrarPagoRequest request,
                                    BindingResult result,
                                    @AuthenticationPrincipal UserDetails userDetails,
                                    RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", result.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/dashboard/tributaria/pagos/nuevo";
        }

        try {
            String username = userDetails != null ? userDetails.getUsername() : "CAJERO";
            PagoResponse response = pagoService.registrarPago(request, username);
            redirectAttributes.addFlashAttribute("success", "Pago registrado y procesado exitosamente: " + response.getCodigoPago());
            return "redirect:/dashboard/tributaria/pagos/" + response.getId();
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/dashboard/tributaria/pagos/nuevo";
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('TRIBUTARIA_PAGOS_VER') or hasAnyRole('ADMIN', 'SUPERADMIN', 'CAJERO', 'JEFE_OFICINA')")
    public String detallePago(@PathVariable("id") UUID id, Model model) {
        PagoResponse pago = pagoService.obtenerPorId(id);
        model.addAttribute("pago", pago);
        model.addAttribute("moduloActivo", "tributaria");
        model.addAttribute("submenuActivo", "pagos");
        return "admin/tributaria/pagos/detail";
    }

    @PostMapping("/{id}/confirmar")
    @PreAuthorize("hasAuthority('TRIBUTARIA_PAGOS_CONFIRMAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA')")
    public String confirmarPago(@PathVariable("id") UUID id,
                                @RequestParam(value = "observaciones", required = false) String observaciones,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        try {
            String username = userDetails != null ? userDetails.getUsername() : "SUPERVISOR";
            PagoResponse res = pagoService.confirmarPago(id, new ConfirmarPagoRequest(observaciones), username);
            redirectAttributes.addFlashAttribute("success", "Pago " + res.getCodigoPago() + " confirmado correctamente.");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/dashboard/tributaria/pagos/" + id;
    }

    @PostMapping("/{id}/revertir")
    @PreAuthorize("hasAuthority('TRIBUTARIA_PAGOS_REVERTIR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA')")
    public String revertirPago(@PathVariable("id") UUID id,
                               @RequestParam("motivoReversion") String motivoReversion,
                               @RequestParam(value = "observaciones", required = false) String observaciones,
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        try {
            String username = userDetails != null ? userDetails.getUsername() : "SUPERVISOR";
            PagoResponse res = pagoService.revertirPago(id, new RevertirPagoRequest(motivoReversion, observaciones), username);
            redirectAttributes.addFlashAttribute("success", "Pago " + res.getCodigoPago() + " revertido con contraasiento contable.");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/dashboard/tributaria/pagos/" + id;
    }

    @GetMapping("/api/contribuyente/{id}/obligaciones")
    @ResponseBody
    @PreAuthorize("hasAuthority('TRIBUTARIA_PAGOS_REGISTRAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'CAJERO')")
    public List<ObligacionPagoItemResponse> obtenerObligacionesContribuyente(@PathVariable("id") UUID id) {
        return pagoService.buscarObligacionesParaCobro(id);
    }

    @GetMapping("/exportar/excel")
    @PreAuthorize("hasAuthority('TRIBUTARIA_PAGOS_EXPORTAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA')")
    public ResponseEntity<byte[]> exportarExcel(@RequestParam(value = "busqueda", required = false) String busqueda,
                                                @RequestParam(value = "contribuyenteId", required = false) UUID contribuyenteId,
                                                @RequestParam(value = "sesionCajaId", required = false) UUID sesionCajaId,
                                                @RequestParam(value = "medioPago", required = false) MedioPagoTributario medioPago,
                                                @RequestParam(value = "estado", required = false) EstadoPagoTributario estado,
                                                @RequestParam(value = "desde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime desde,
                                                @RequestParam(value = "hasta", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime hasta) {

        byte[] bytes = exportService.exportarPagosExcel(busqueda, contribuyenteId, sesionCajaId, medioPago, estado, desde, hasta);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"reporte_pagos_quinua.xlsx\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(bytes);
    }

    @GetMapping("/exportar/pdf")
    @PreAuthorize("hasAuthority('TRIBUTARIA_PAGOS_EXPORTAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA')")
    public ResponseEntity<byte[]> exportarPdf(@RequestParam(value = "busqueda", required = false) String busqueda,
                                              @RequestParam(value = "contribuyenteId", required = false) UUID contribuyenteId,
                                              @RequestParam(value = "sesionCajaId", required = false) UUID sesionCajaId,
                                              @RequestParam(value = "medioPago", required = false) MedioPagoTributario medioPago,
                                              @RequestParam(value = "estado", required = false) EstadoPagoTributario estado,
                                              @RequestParam(value = "desde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime desde,
                                              @RequestParam(value = "hasta", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime hasta) {

        byte[] bytes = exportService.exportarPagosPdf(busqueda, contribuyenteId, sesionCajaId, medioPago, estado, desde, hasta);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"reporte_pagos_quinua.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(bytes);
    }
}
