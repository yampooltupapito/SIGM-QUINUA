package pe.gob.municipalidad.sigm.modules.serenazgo.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoAlertaCiudadana;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoSectorPatrullaje;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.PrioridadIncidenciaSeguridad;
import pe.gob.municipalidad.sigm.modules.serenazgo.repository.IncidenciaSeguridadCiudadanaRepository;
import pe.gob.municipalidad.sigm.modules.serenazgo.repository.SectorPatrullajeRepository;
import pe.gob.municipalidad.sigm.modules.serenazgo.repository.TipoIncidenciaSeguridadRepository;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.AlertaCiudadanaSeguridadService;

import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@Controller
@RequestMapping("/dashboard/serenazgo/alertas")
@RequiredArgsConstructor
public class AlertasAdminWebController {

    private final AlertaCiudadanaSeguridadService alertaService;
    private final TipoIncidenciaSeguridadRepository tipoIncidenciaRepository;
    private final SectorPatrullajeRepository sectorRepository;
    private final IncidenciaSeguridadCiudadanaRepository incidenciaRepository;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('SERENAZGO_ALERTAS_VER', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_OPERADOR_SERENAZGO', 'ROLE_SERENO')")
    public String bandejaAlertas(@ModelAttribute("filtro") AlertaFilter filtro,
                                 @RequestParam(value = "page", defaultValue = "0") int page,
                                 @RequestParam(value = "size", defaultValue = "15") int size,
                                 Model model) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(size, 50), Sort.by(Sort.Direction.DESC, "fechaRecepcion"));
        Page<AlertaResponse> alertas = alertaService.listarConFiltrosAdmin(filtro, pageable);
        DashboardAlertasResponse kpis = alertaService.obtenerDashboardAlertas();

        model.addAttribute("alertas", alertas);
        model.addAttribute("kpis", kpis);
        model.addAttribute("tiposReporte", tipoIncidenciaRepository.findByActivoTrue());
        model.addAttribute("sectores", sectorRepository.findByEstadoOrderByCodigoAsc(EstadoSectorPatrullaje.ACTIVO));
        model.addAttribute("estados", EstadoAlertaCiudadana.values());
        model.addAttribute("prioridades", PrioridadIncidenciaSeguridad.values());
        model.addAttribute("moduloActivo", "serenazgo");
        model.addAttribute("submenuActivo", "alertas");

        return "admin/serenazgo/alertas/index";
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_ALERTAS_VER', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_OPERADOR_SERENAZGO', 'ROLE_SERENO')")
    public String detalleAlerta(@PathVariable("id") UUID id,
                                Authentication auth,
                                Model model) {
        boolean puedeVerSensibles = auth.getAuthorities().stream().anyMatch(a ->
                a.getAuthority().equals("ROLE_ADMIN") ||
                a.getAuthority().equals("ROLE_SUPERVISOR_SERENAZGO") ||
                a.getAuthority().equals("SERENAZGO_INCIDENCIAS_VER"));

        AlertaDetailResponse alerta = alertaService.obtenerDetalleAdmin(id, puedeVerSensibles);

        model.addAttribute("alerta", alerta);
        model.addAttribute("tiposReporte", tipoIncidenciaRepository.findByActivoTrue());
        model.addAttribute("estados", EstadoAlertaCiudadana.values());
        model.addAttribute("prioridades", PrioridadIncidenciaSeguridad.values());
        model.addAttribute("incidenciasActivas", incidenciaRepository.findAll(Sort.by(Sort.Direction.DESC, "fechaReporte")));
        model.addAttribute("moduloActivo", "serenazgo");
        model.addAttribute("submenuActivo", "alertas");

        return "admin/serenazgo/alertas/alerta-detalle";
    }

    @PostMapping("/{id}/clasificar")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_ALERTAS_CLASIFICAR', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_OPERADOR_SERENAZGO')")
    public String clasificar(@PathVariable("id") UUID id,
                             @Valid @ModelAttribute("reqClasificar") AlertaClasificarRequest req,
                             BindingResult result,
                             Authentication auth,
                             RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("mensajeError", "Datos de clasificación inválidos");
            return "redirect:/dashboard/serenazgo/alertas/" + id;
        }

        try {
            alertaService.clasificarAlerta(id, req, auth.getName());
            redirectAttributes.addFlashAttribute("mensajeExito", "Alerta clasificada correctamente");
        } catch (Exception e) {
            log.error("Fallo al clasificar alerta {}", id, e);
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }

        return "redirect:/dashboard/serenazgo/alertas/" + id;
    }

    @PostMapping("/{id}/prioridad")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_ALERTAS_PRIORIZAR', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_OPERADOR_SERENAZGO')")
    public String cambiarPrioridad(@PathVariable("id") UUID id,
                                   @Valid @ModelAttribute("reqPrioridad") AlertaPrioridadRequest req,
                                   BindingResult result,
                                   Authentication auth,
                                   RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("mensajeError", "Debe justificar el cambio de prioridad");
            return "redirect:/dashboard/serenazgo/alertas/" + id;
        }

        try {
            alertaService.cambiarPrioridad(id, req, auth.getName());
            redirectAttributes.addFlashAttribute("mensajeExito", "Prioridad administrativa actualizada");
        } catch (Exception e) {
            log.error("Fallo al cambiar prioridad", e);
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }

        return "redirect:/dashboard/serenazgo/alertas/" + id;
    }

    @PostMapping("/{id}/estado")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_ALERTAS_CAMBIAR_ESTADO', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_OPERADOR_SERENAZGO')")
    public String cambiarEstado(@PathVariable("id") UUID id,
                                @Valid @ModelAttribute("reqEstado") AlertaCambioEstadoRequest req,
                                BindingResult result,
                                Authentication auth,
                                RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("mensajeError", "Datos de transición de estado incompletos");
            return "redirect:/dashboard/serenazgo/alertas/" + id;
        }

        try {
            alertaService.cambiarEstado(id, req, auth.getName());
            redirectAttributes.addFlashAttribute("mensajeExito", "Estado actualizado exitosamente");
        } catch (Exception e) {
            log.error("Fallo al cambiar estado", e);
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }

        return "redirect:/dashboard/serenazgo/alertas/" + id;
    }

    @PostMapping("/{id}/solicitar-informacion")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_ALERTAS_SOLICITAR_INFORMACION', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_OPERADOR_SERENAZGO')")
    public String solicitarInformacion(@PathVariable("id") UUID id,
                                       @Valid @ModelAttribute("reqInfo") AlertaSolicitarInfoRequest req,
                                       BindingResult result,
                                       Authentication auth,
                                       RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("mensajeError", "Debe especificar el requerimiento al ciudadano");
            return "redirect:/dashboard/serenazgo/alertas/" + id;
        }

        try {
            alertaService.solicitarInformacion(id, req, auth.getName());
            redirectAttributes.addFlashAttribute("mensajeExito", "Solicitud de información complementaria generada y notificada");
        } catch (Exception e) {
            log.error("Fallo al solicitar información", e);
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }

        return "redirect:/dashboard/serenazgo/alertas/" + id;
    }

    @PostMapping("/{id}/vincular-incidencia")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_ALERTAS_VINCULAR', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_OPERADOR_SERENAZGO')")
    public String vincularIncidencia(@PathVariable("id") UUID id,
                                     @Valid @ModelAttribute("reqVincular") AlertaVincularIncidenciaRequest req,
                                     BindingResult result,
                                     Authentication auth,
                                     RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("mensajeError", "Debe especificar el motivo de vinculación");
            return "redirect:/dashboard/serenazgo/alertas/" + id;
        }

        try {
            alertaService.vincularConIncidencia(id, req, auth.getName());
            redirectAttributes.addFlashAttribute("mensajeExito", "Alerta ciudadana vinculada formalmente con la Incidencia");
        } catch (Exception e) {
            log.error("Fallo al vincular incidencia", e);
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }

        return "redirect:/dashboard/serenazgo/alertas/" + id;
    }

    @PostMapping("/{id}/cerrar")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_ALERTAS_CERRAR', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO')")
    public String cerrar(@PathVariable("id") UUID id,
                         @Valid @ModelAttribute("reqCierre") AlertaCierreRequest req,
                         BindingResult result,
                         Authentication auth,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("mensajeError", "El motivo de cierre administrativo es obligatorio");
            return "redirect:/dashboard/serenazgo/alertas/" + id;
        }

        try {
            alertaService.cerrarAlerta(id, req, auth.getName());
            redirectAttributes.addFlashAttribute("mensajeExito", "Cierre administrativo de la alerta completado");
        } catch (Exception e) {
            log.error("Fallo al cerrar alerta", e);
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }

        return "redirect:/dashboard/serenazgo/alertas/" + id;
    }

    @PostMapping("/{id}/descartar")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_ALERTAS_CERRAR', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO')")
    public String descartar(@PathVariable("id") UUID id,
                            @Valid @ModelAttribute("reqDescarte") AlertaDescarteRequest req,
                            BindingResult result,
                            Authentication auth,
                            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("mensajeError", "Debe sustentar el motivo de descarte");
            return "redirect:/dashboard/serenazgo/alertas/" + id;
        }

        try {
            alertaService.descartarAlerta(id, req, auth.getName());
            redirectAttributes.addFlashAttribute("mensajeExito", "Alerta descartada con registro en bitácora");
        } catch (Exception e) {
            log.error("Fallo al descartar alerta", e);
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }

        return "redirect:/dashboard/serenazgo/alertas/" + id;
    }

    @GetMapping("/{id}/pdf")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_ALERTAS_EXPORTAR', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_OPERADOR_SERENAZGO')")
    public ResponseEntity<byte[]> exportarPdf(@PathVariable("id") UUID id) {
        byte[] pdfBytes = alertaService.exportarFichaPdf(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"alerta_" + id + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @GetMapping("/exportar/excel")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_ALERTAS_EXPORTAR', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO')")
    public ResponseEntity<byte[]> exportarExcel(@RequestParam(value = "desde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                                @RequestParam(value = "hasta", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        byte[] excelBytes = alertaService.exportarAlertasExcel(desde, hasta);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"alertas_ciudadanas_quinua.xlsx\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }
}
