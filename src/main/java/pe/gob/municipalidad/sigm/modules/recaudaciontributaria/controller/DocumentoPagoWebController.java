package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.controller;

import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.documentopago.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.PagoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoDocumentoPagoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoPagoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.PagoTributarioRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.DocumentoPagoTributarioService;
import pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Controller
@RequestMapping("/dashboard/tributaria/documentos")
@RequiredArgsConstructor
public class DocumentoPagoWebController {

    private final DocumentoPagoTributarioService documentoService;
    private final PagoTributarioRepository pagoRepository;

    @GetMapping
    @PreAuthorize("hasAuthority('TRIBUTARIA_DOCUMENTOS_VER') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_CAJERO', 'ROLE_TRIBUTACION')")
    public String listarDocumentos(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) UUID tipoDocId,
            @RequestParam(required = false) UUID serieId,
            @RequestParam(required = false) EstadoDocumentoPagoTributario estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            Model model) {

        Page<DocumentoPagoResponse> documentos = documentoService.listarDocumentos(
                query, tipoDocId, serieId, null, null, estado, desde, hasta,
                PageRequest.of(page, size, Sort.by("fechaEmision").descending()));

        DashboardDocumentalResponse metricas = documentoService.obtenerMetricas();

        model.addAttribute("documentos", documentos);
        model.addAttribute("metricas", metricas);
        model.addAttribute("tiposDocumento", documentoService.listarTiposDocumento());
        model.addAttribute("query", query);
        model.addAttribute("tipoDocId", tipoDocId);
        model.addAttribute("serieId", serieId);
        model.addAttribute("estado", estado);
        model.addAttribute("desde", desde);
        model.addAttribute("hasta", hasta);
        model.addAttribute("estados", EstadoDocumentoPagoTributario.values());

        return "admin/tributaria/documentos/list";
    }

    @GetMapping("/emitir")
    @PreAuthorize("hasAuthority('TRIBUTARIA_DOCUMENTOS_EMITIR') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_CAJERO', 'ROLE_TRIBUTACION')")
    public String mostrarFormularioEmision(
            @RequestParam(required = false) UUID pagoId,
            Model model) {

        EmitirDocumentoRequest form = new EmitirDocumentoRequest();
        form.setTipoDocumentoCodigo("RECIBO_PAGO");

        if (pagoId != null) {
            form.setPagoId(pagoId);
            pagoRepository.findById(pagoId).ifPresent(pago -> model.addAttribute("pagoSeleccionado", pago));
        }

        // Listar pagos confirmados recientes elegibles
        List<PagoTributario> pagosElegibles = pagoRepository.findAll().stream()
                .filter(p -> p.getEstado() == EstadoPagoTributario.CONFIRMADO || p.getEstado() == EstadoPagoTributario.PROCESADO)
                .sorted((a, b) -> b.getFechaHoraPago().compareTo(a.getFechaHoraPago()))
                .limit(20)
                .toList();

        model.addAttribute("form", form);
        model.addAttribute("pagosElegibles", pagosElegibles);
        model.addAttribute("tiposDocumento", documentoService.listarTiposDocumento());
        return "admin/tributaria/documentos/form";
    }

    @PostMapping("/emitir")
    @PreAuthorize("hasAuthority('TRIBUTARIA_DOCUMENTOS_EMITIR') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_CAJERO', 'ROLE_TRIBUTACION')")
    public String procesarEmision(
            @Valid @ModelAttribute("form") EmitirDocumentoRequest form,
            BindingResult bindingResult,
            @AuthenticationPrincipal CustomUserDetails user,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("tiposDocumento", documentoService.listarTiposDocumento());
            return "admin/tributaria/documentos/form";
        }

        try {
            String ip = request.getRemoteAddr();
            String userAgent = request.getHeader("User-Agent");
            DocumentoPagoResponse doc = documentoService.emitirDocumento(form, user, ip, userAgent);
            redirectAttributes.addFlashAttribute("mensajeExito",
                    "Documento oficial " + doc.getCodigoDocumento() + " emitido exitosamente.");
            return "redirect:/dashboard/tributaria/documentos/" + doc.getId();
        } catch (BusinessException e) {
            log.error("Error al emitir documento tributario: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
            return "redirect:/dashboard/tributaria/documentos/emitir" + (form.getPagoId() != null ? "?pagoId=" + form.getPagoId() : "");
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('TRIBUTARIA_DOCUMENTOS_VER') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_CAJERO', 'ROLE_TRIBUTACION')")
    public String verDetalleDocumento(@PathVariable UUID id, Model model) {
        DocumentoPagoResponse doc = documentoService.obtenerPorId(id);
        List<HistorialDocumentoResponse> historial = documentoService.listarHistorial(id);

        model.addAttribute("doc", doc);
        model.addAttribute("historial", historial);
        model.addAttribute("anularForm", new AnularDocumentoRequest());
        model.addAttribute("sustituirForm", new SustituirDocumentoRequest());
        return "admin/tributaria/documentos/detail";
    }

    @GetMapping("/{id}/pdf")
    @PreAuthorize("hasAuthority('TRIBUTARIA_DOCUMENTOS_DESCARGAR') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_CAJERO', 'ROLE_TRIBUTACION')")
    public ResponseEntity<byte[]> descargarPdf(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "inline") String disposition,
            @AuthenticationPrincipal CustomUserDetails user,
            HttpServletRequest request) {

        DocumentoPagoResponse doc = documentoService.obtenerPorId(id);
        byte[] pdfBytes = documentoService.obtenerContenidoPdf(id, user, request.getRemoteAddr(), request.getHeader("User-Agent"));

        String headerValue = ("attachment".equalsIgnoreCase(disposition) ? "attachment" : "inline")
                + "; filename=\"" + doc.getCodigoDocumento() + ".pdf\"";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, headerValue)
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @GetMapping("/{id}/qr")
    @PreAuthorize("hasAuthority('TRIBUTARIA_DOCUMENTOS_VER') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_CAJERO', 'ROLE_TRIBUTACION')")
    public ResponseEntity<byte[]> verQrPng(@PathVariable UUID id) {
        byte[] qrBytes = documentoService.obtenerQrPng(id);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(qrBytes);
    }

    @PostMapping("/{id}/reimprimir")
    @PreAuthorize("hasAuthority('TRIBUTARIA_DOCUMENTOS_REIMPRIMIR') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_CAJERO', 'ROLE_TRIBUTACION')")
    public String registrarReimpresion(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails user,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        try {
            DocumentoPagoResponse doc = documentoService.registrarReimpresion(
                    id, user, request.getRemoteAddr(), request.getHeader("User-Agent"));
            redirectAttributes.addFlashAttribute("mensajeExito",
                    "Copia de reimpresión N° " + doc.getNumeroReimpresiones() + " registrada y lista para imprimir.");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/dashboard/tributaria/documentos/" + id;
    }

    @PostMapping("/{id}/anular")
    @PreAuthorize("hasAuthority('TRIBUTARIA_DOCUMENTOS_ANULAR') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_TRIBUTACION')")
    public String anularDocumento(
            @PathVariable UUID id,
            @Valid @ModelAttribute("anularForm") AnularDocumentoRequest requestForm,
            BindingResult bindingResult,
            @AuthenticationPrincipal CustomUserDetails user,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("mensajeError", "El motivo de anulación es inválido o no cumple la longitud requerida.");
            return "redirect:/dashboard/tributaria/documentos/" + id;
        }

        try {
            DocumentoPagoResponse doc = documentoService.anularDocumento(
                    id, requestForm, user, request.getRemoteAddr(), request.getHeader("User-Agent"));
            redirectAttributes.addFlashAttribute("mensajeExito",
                    "Documento " + doc.getCodigoDocumento() + " fue ANULADO formalmente.");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/dashboard/tributaria/documentos/" + id;
    }

    @PostMapping("/{id}/sustituir")
    @PreAuthorize("hasAuthority('TRIBUTARIA_DOCUMENTOS_SUSTITUIR') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_TRIBUTACION')")
    public String sustituirDocumento(
            @PathVariable UUID id,
            @Valid @ModelAttribute("sustituirForm") SustituirDocumentoRequest requestForm,
            BindingResult bindingResult,
            @AuthenticationPrincipal CustomUserDetails user,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("mensajeError", "El motivo de sustitución es inválido o no cumple la longitud requerida.");
            return "redirect:/dashboard/tributaria/documentos/" + id;
        }

        try {
            DocumentoPagoResponse nuevoDoc = documentoService.sustituirDocumento(
                    id, requestForm, user, request.getRemoteAddr(), request.getHeader("User-Agent"));
            redirectAttributes.addFlashAttribute("mensajeExito",
                    "Documento sustituido exitosamente. Se ha emitido el nuevo documento " + nuevoDoc.getCodigoDocumento());
            return "redirect:/dashboard/tributaria/documentos/" + nuevoDoc.getId();
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
            return "redirect:/dashboard/tributaria/documentos/" + id;
        }
    }
}
