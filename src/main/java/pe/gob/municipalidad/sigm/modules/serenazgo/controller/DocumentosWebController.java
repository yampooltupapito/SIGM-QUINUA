package pe.gob.municipalidad.sigm.modules.serenazgo.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.documentos.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.DocumentoSeguridadCiudadanaService;
import pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails;

import java.util.UUID;

@Slf4j
@Controller
@RequestMapping("/dashboard/serenazgo/documentos")
@RequiredArgsConstructor
public class DocumentosWebController {

    private final DocumentoSeguridadCiudadanaService documentoService;

    @GetMapping
    @PreAuthorize("hasAuthority('SERENAZGO_DOCUMENTOS_VER')")
    public String listar(
            @ModelAttribute("filtro") DocumentoFiltroRequest filtro,
            @PageableDefault(size = 15) Pageable pageable,
            Model model) {

        Page<DocumentoSeguridadResponse> pagina = documentoService.listar(filtro, pageable);
        DocumentoEstadisticasResponse stats = documentoService.obtenerEstadisticas();

        model.addAttribute("pagina", pagina);
        model.addAttribute("stats", stats);
        model.addAttribute("filtro", filtro);
        model.addAttribute("tipos", TipoDocumentoSeguridad.values());
        model.addAttribute("estados", EstadoDocumentoSeguridad.values());
        model.addAttribute("niveles", NivelConfidencialidad.values());
        model.addAttribute("origenes", OrigenDocumentoSeguridad.values());
        model.addAttribute("resultadosIntegridad", ResultadoVerificacionIntegridad.values());

        return "admin/serenazgo/documentos/index";
    }

    @GetMapping("/nuevo")
    @PreAuthorize("hasAuthority('SERENAZGO_DOCUMENTOS_CREAR')")
    public String nuevo(
            @RequestParam(value = "incidenciaId", required = false) UUID incidenciaId,
            @RequestParam(value = "alertaId", required = false) UUID alertaId,
            @RequestParam(value = "intervencionId", required = false) UUID intervencionId,
            @RequestParam(value = "patrullajeId", required = false) UUID patrullajeId,
            Model model) {

        DocumentoCargaRequest form = DocumentoCargaRequest.builder()
                .tipoDocumento(TipoDocumentoSeguridad.FOTOGRAFIA)
                .nivelConfidencialidad(NivelConfidencialidad.CONFIDENCIAL)
                .origenDocumento(OrigenDocumentoSeguridad.ADMINISTRATIVO)
                .incidenciaId(incidenciaId)
                .alertaId(alertaId)
                .intervencionId(intervencionId)
                .patrullajeId(patrullajeId)
                .build();

        model.addAttribute("form", form);
        model.addAttribute("tipos", TipoDocumentoSeguridad.values());
        model.addAttribute("niveles", NivelConfidencialidad.values());
        model.addAttribute("origenes", OrigenDocumentoSeguridad.values());

        return "admin/serenazgo/documentos/form";
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SERENAZGO_DOCUMENTOS_CREAR')")
    public String guardar(
            @ModelAttribute("form") @Valid DocumentoCargaRequest form,
            BindingResult bindingResult,
            @AuthenticationPrincipal CustomUserDetails user,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (form.getArchivo() == null || form.getArchivo().isEmpty()) {
            bindingResult.rejectValue("archivo", "error.archivo", "Debe seleccionar un archivo válido para registrar.");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("tipos", TipoDocumentoSeguridad.values());
            model.addAttribute("niveles", NivelConfidencialidad.values());
            model.addAttribute("origenes", OrigenDocumentoSeguridad.values());
            return "admin/serenazgo/documentos/form";
        }

        try {
            String ip = request.getRemoteAddr();
            DocumentoSeguridadResponse resp = documentoService.registrarDocumento(form, user, ip);
            redirectAttributes.addFlashAttribute("mensajeExito", "Documento registrado exitosamente con código: " + resp.getCodigo());
            return "redirect:/dashboard/serenazgo/documentos/" + resp.getId();
        } catch (Exception e) {
            log.error("Error al registrar documento", e);
            model.addAttribute("mensajeError", e.getMessage());
            model.addAttribute("tipos", TipoDocumentoSeguridad.values());
            model.addAttribute("niveles", NivelConfidencialidad.values());
            model.addAttribute("origenes", OrigenDocumentoSeguridad.values());
            return "admin/serenazgo/documentos/form";
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SERENAZGO_DOCUMENTOS_VER')")
    public String verDetalle(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails user,
            Model model) {

        DocumentoSeguridadDetalleResponse doc = documentoService.obtenerDetalle(id, user);
        model.addAttribute("doc", doc);
        model.addAttribute("niveles", NivelConfidencialidad.values());
        return "admin/serenazgo/documentos/detalle";
    }

    @GetMapping("/{id}/descargar")
    @PreAuthorize("hasAuthority('SERENAZGO_DOCUMENTOS_DESCARGAR')")
    public ResponseEntity<Resource> descargar(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails user,
            HttpServletRequest request) {

        String ip = request.getRemoteAddr();
        Resource resource = documentoService.descargar(id, user, ip);
        DocumentoSeguridadDetalleResponse det = documentoService.obtenerDetalle(id, user);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(det.getMimeType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + det.getNombreOriginal() + "\"")
                .body(resource);
    }

    @GetMapping("/{id}/vista-previa")
    @PreAuthorize("hasAuthority('SERENAZGO_DOCUMENTOS_VER')")
    public ResponseEntity<Resource> vistaPrevia(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails user) {

        Resource resource = documentoService.obtenerVistaPrevia(id, user);
        DocumentoSeguridadDetalleResponse det = documentoService.obtenerDetalle(id, user);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(det.getMimeType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + det.getNombreOriginal() + "\"")
                .body(resource);
    }

    @PostMapping("/{id}/verificar")
    @PreAuthorize("hasAuthority('SERENAZGO_DOCUMENTOS_VERIFICAR')")
    public String verificarIntegridad(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails user,
            RedirectAttributes redirectAttributes) {

        VerificacionIntegridadResponse verif = documentoService.verificarIntegridad(id, user);
        if (verif.isCoincide()) {
            redirectAttributes.addFlashAttribute("mensajeExito", "Verificación SHA-256 exitosa: Integridad confirmada.");
        } else {
            redirectAttributes.addFlashAttribute("mensajeError", "Alerta de integridad: " + verif.getResultadoDesc());
        }
        return "redirect:/dashboard/serenazgo/documentos/" + id;
    }

    @PostMapping("/{id}/revisar")
    @PreAuthorize("hasAuthority('SERENAZGO_DOCUMENTOS_REVISAR')")
    public String revisar(
            @PathVariable UUID id,
            @RequestParam(value = "motivo", required = false) String motivo,
            @RequestParam(value = "observaciones", required = false) String observaciones,
            @AuthenticationPrincipal CustomUserDetails user,
            RedirectAttributes redirectAttributes) {

        TransicionEstadoRequest req = TransicionEstadoRequest.builder()
                .motivo(motivo)
                .observaciones(observaciones)
                .build();
        documentoService.revisar(id, req, user);
        redirectAttributes.addFlashAttribute("mensajeExito", "Documento enviado a revisión.");
        return "redirect:/dashboard/serenazgo/documentos/" + id;
    }

    @PostMapping("/{id}/observar")
    @PreAuthorize("hasAuthority('SERENAZGO_DOCUMENTOS_REVISAR')")
    public String observar(
            @PathVariable UUID id,
            @RequestParam(value = "motivo", required = false) String motivo,
            @RequestParam(value = "observaciones", required = false) String observaciones,
            @AuthenticationPrincipal CustomUserDetails user,
            RedirectAttributes redirectAttributes) {

        TransicionEstadoRequest req = TransicionEstadoRequest.builder()
                .motivo(motivo)
                .observaciones(observaciones)
                .build();
        documentoService.observar(id, req, user);
        redirectAttributes.addFlashAttribute("mensajeExito", "Documento marcado como observado.");
        return "redirect:/dashboard/serenazgo/documentos/" + id;
    }

    @PostMapping("/{id}/validar")
    @PreAuthorize("hasAuthority('SERENAZGO_DOCUMENTOS_VALIDAR')")
    public String validar(
            @PathVariable UUID id,
            @RequestParam(value = "observaciones", required = false) String observaciones,
            @AuthenticationPrincipal CustomUserDetails user,
            RedirectAttributes redirectAttributes) {

        TransicionEstadoRequest req = TransicionEstadoRequest.builder()
                .observaciones(observaciones)
                .build();
        documentoService.validarAdministrativamente(id, req, user);
        redirectAttributes.addFlashAttribute("mensajeExito", "Documento validado administrativamente de acuerdo con la Ley N° 31297.");
        return "redirect:/dashboard/serenazgo/documentos/" + id;
    }

    @PostMapping("/{id}/restringir")
    @PreAuthorize("hasAuthority('SERENAZGO_DOCUMENTOS_RESTRINGIR')")
    public String restringir(
            @PathVariable UUID id,
            @RequestParam(value = "motivo", required = false) String motivo,
            @RequestParam(value = "nuevoNivel", required = false) NivelConfidencialidad nuevoNivel,
            @AuthenticationPrincipal CustomUserDetails user,
            RedirectAttributes redirectAttributes) {

        TransicionEstadoRequest req = TransicionEstadoRequest.builder()
                .motivo(motivo)
                .nuevoNivel(nuevoNivel != null ? nuevoNivel : NivelConfidencialidad.RESTRINGIDO)
                .build();
        documentoService.restringir(id, req, user);
        redirectAttributes.addFlashAttribute("mensajeExito", "Acceso al documento restringido por confidencialidad.");
        return "redirect:/dashboard/serenazgo/documentos/" + id;
    }

    @PostMapping("/{id}/archivar")
    @PreAuthorize("hasAuthority('SERENAZGO_DOCUMENTOS_ARCHIVAR')")
    public String archivar(
            @PathVariable UUID id,
            @RequestParam(value = "motivo", required = false) String motivo,
            @AuthenticationPrincipal CustomUserDetails user,
            RedirectAttributes redirectAttributes) {

        TransicionEstadoRequest req = TransicionEstadoRequest.builder()
                .motivo(motivo)
                .build();
        documentoService.archivar(id, req, user);
        redirectAttributes.addFlashAttribute("mensajeExito", "Documento enviado a archivo pasivo.");
        return "redirect:/dashboard/serenazgo/documentos/" + id;
    }

    @PostMapping("/{id}/versiones")
    @PreAuthorize("hasAuthority('SERENAZGO_DOCUMENTOS_VERSIONAR')")
    public String crearVersion(
            @PathVariable UUID id,
            @ModelAttribute NuevaVersionRequest req,
            @AuthenticationPrincipal CustomUserDetails user,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        String ip = request.getRemoteAddr();
        DocumentoSeguridadResponse nuevaVersion = documentoService.crearNuevaVersion(id, req, user, ip);
        redirectAttributes.addFlashAttribute("mensajeExito", "Nueva versión registrada exitosamente: " + nuevaVersion.getCodigo());
        return "redirect:/dashboard/serenazgo/documentos/" + nuevaVersion.getId();
    }

    @GetMapping("/exportar/excel")
    @PreAuthorize("hasAuthority('SERENAZGO_DOCUMENTOS_EXPORTAR')")
    public ResponseEntity<byte[]> exportarExcel(@ModelAttribute DocumentoFiltroRequest filtro) {
        byte[] bytes = documentoService.exportarExcel(filtro);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"evidencias_serenazgo.xlsx\"")
                .body(bytes);
    }

    @GetMapping("/{id}/exportar/pdf")
    @PreAuthorize("hasAuthority('SERENAZGO_DOCUMENTOS_EXPORTAR')")
    public ResponseEntity<byte[]> exportarPdf(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails user) {

        byte[] bytes = documentoService.exportarPdfFicha(id, user);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"ficha_evidencia_" + id + ".pdf\"")
                .body(bytes);
    }
}
