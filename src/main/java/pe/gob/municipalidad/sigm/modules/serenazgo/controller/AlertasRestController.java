package pe.gob.municipalidad.sigm.modules.serenazgo.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoAlertaCiudadana;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.PrioridadIncidenciaSeguridad;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.AlertaCiudadanaSeguridadService;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
public class AlertasRestController {

    private final AlertaCiudadanaSeguridadService alertaService;

    // ==========================================
    // SECCIÓN ADMINISTRATIVA: /api/serenazgo/alertas
    // ==========================================

    @GetMapping("/api/serenazgo/alertas")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_ALERTAS_VER', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_OPERADOR_SERENAZGO', 'ROLE_SERENO')")
    public ResponseEntity<Page<AlertaResponse>> listarAlertas(
            @RequestParam(name = "termino", required = false) String termino,
            @RequestParam(name = "desde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(name = "hasta", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(name = "estado", required = false) EstadoAlertaCiudadana estado,
            @RequestParam(name = "prioridad", required = false) PrioridadIncidenciaSeguridad prioridad,
            @RequestParam(name = "tipoId", required = false) UUID tipoId,
            @RequestParam(name = "sectorId", required = false) UUID sectorId,
            @RequestParam(name = "vinculada", required = false) Boolean vinculada,
            Pageable pageable) {

        AlertaFilter filtro = AlertaFilter.builder()
                .termino(termino)
                .desde(desde)
                .hasta(hasta)
                .estado(estado)
                .prioridad(prioridad)
                .tipoId(tipoId)
                .sectorId(sectorId)
                .vinculada(vinculada)
                .build();

        return ResponseEntity.ok(alertaService.listarConFiltrosAdmin(filtro, pageable));
    }

    @GetMapping("/api/serenazgo/alertas/dashboard")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_ALERTAS_VER', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_OPERADOR_SERENAZGO', 'ROLE_SERENO')")
    public ResponseEntity<DashboardAlertasResponse> obtenerDashboard() {
        return ResponseEntity.ok(alertaService.obtenerDashboardAlertas());
    }

    @GetMapping("/api/serenazgo/alertas/{id}")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_ALERTAS_VER', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_OPERADOR_SERENAZGO', 'ROLE_SERENO')")
    public ResponseEntity<AlertaDetailResponse> obtenerDetalle(@PathVariable UUID id, Authentication auth) {
        boolean puedeVerSensibles = auth != null && auth.getAuthorities().stream().anyMatch(a ->
                a.getAuthority().equals("ROLE_ADMIN") ||
                a.getAuthority().equals("ROLE_SUPERVISOR_SERENAZGO") ||
                a.getAuthority().equals("SERENAZGO_INCIDENCIAS_VER"));

        return ResponseEntity.ok(alertaService.obtenerDetalleAdmin(id, puedeVerSensibles));
    }

    @PostMapping("/api/serenazgo/alertas/{id}/clasificar")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_ALERTAS_CLASIFICAR', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_OPERADOR_SERENAZGO')")
    public ResponseEntity<Void> clasificarAlerta(@PathVariable UUID id,
                                                 @Valid @RequestBody AlertaClasificarRequest request,
                                                 Principal principal) {
        String username = principal != null ? principal.getName() : "SISTEMA";
        alertaService.clasificarAlerta(id, request, username);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/serenazgo/alertas/{id}/prioridad")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_ALERTAS_PRIORIZAR', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_OPERADOR_SERENAZGO')")
    public ResponseEntity<Void> cambiarPrioridad(@PathVariable UUID id,
                                                @Valid @RequestBody AlertaPrioridadRequest request,
                                                Principal principal) {
        String username = principal != null ? principal.getName() : "SISTEMA";
        alertaService.cambiarPrioridad(id, request, username);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/serenazgo/alertas/{id}/estado")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_ALERTAS_CAMBIAR_ESTADO', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_OPERADOR_SERENAZGO')")
    public ResponseEntity<Void> cambiarEstado(@PathVariable UUID id,
                                              @Valid @RequestBody AlertaCambioEstadoRequest request,
                                              Principal principal) {
        String username = principal != null ? principal.getName() : "SISTEMA";
        alertaService.cambiarEstado(id, request, username);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/serenazgo/alertas/{id}/solicitar-informacion")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_ALERTAS_SOLICITAR_INFORMACION', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_OPERADOR_SERENAZGO')")
    public ResponseEntity<Void> solicitarInformacion(@PathVariable UUID id,
                                                     @Valid @RequestBody AlertaSolicitarInfoRequest request,
                                                     Principal principal) {
        String username = principal != null ? principal.getName() : "SISTEMA";
        alertaService.solicitarInformacion(id, request, username);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/serenazgo/alertas/{id}/vincular-incidencia")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_ALERTAS_VINCULAR', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_OPERADOR_SERENAZGO')")
    public ResponseEntity<Void> vincularIncidencia(@PathVariable UUID id,
                                                   @Valid @RequestBody AlertaVincularIncidenciaRequest request,
                                                   Principal principal) {
        String username = principal != null ? principal.getName() : "SISTEMA";
        alertaService.vincularConIncidencia(id, request, username);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/serenazgo/alertas/{id}/cerrar")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_ALERTAS_CERRAR', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO')")
    public ResponseEntity<Void> cerrarAlerta(@PathVariable UUID id,
                                            @Valid @RequestBody AlertaCierreRequest request,
                                            Principal principal) {
        String username = principal != null ? principal.getName() : "SISTEMA";
        alertaService.cerrarAlerta(id, request, username);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/serenazgo/alertas/{id}/descartar")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_ALERTAS_CERRAR', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO')")
    public ResponseEntity<Void> descartarAlerta(@PathVariable UUID id,
                                               @Valid @RequestBody AlertaDescarteRequest request,
                                               Principal principal) {
        String username = principal != null ? principal.getName() : "SISTEMA";
        alertaService.descartarAlerta(id, request, username);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/serenazgo/alertas/{id}/pdf")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_ALERTAS_EXPORTAR', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_OPERADOR_SERENAZGO')")
    public ResponseEntity<byte[]> exportarPdf(@PathVariable UUID id) {
        byte[] pdfBytes = alertaService.exportarFichaPdf(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"alerta_" + id + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @GetMapping("/api/serenazgo/alertas/exportar/excel")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_ALERTAS_EXPORTAR', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO')")
    public ResponseEntity<byte[]> exportarExcel(
            @RequestParam(value = "desde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(value = "hasta", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        byte[] excelBytes = alertaService.exportarAlertasExcel(desde, hasta);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"alertas_ciudadanas_quinua.xlsx\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }

    // ==========================================
    // SECCIÓN PORTAL CIUDADANO: /api/portal/seguridad
    // ==========================================

    @PostMapping(value = "/api/portal/seguridad/reportar", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<AlertaConfirmacionResponse> registrarReportePortal(
            @Valid @RequestPart("reporte") AlertaRegistroCiudadanoRequest reporte,
            @RequestPart(value = "archivos", required = false) List<MultipartFile> archivos,
            HttpServletRequest httpRequest,
            Principal principal) {

        String username = principal != null ? principal.getName() : null;
        String ipCliente = httpRequest.getRemoteAddr();

        AlertaConfirmacionResponse response = alertaService.registrarAlertaCiudadana(reporte, archivos, username, ipCliente);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/api/portal/seguridad/seguimiento")
    public ResponseEntity<AlertaDetailResponse> consultarSeguimiento(
            @Valid @RequestBody AlertaSeguimientoConsultaRequest consulta,
            Principal principal) {
        String username = principal != null ? principal.getName() : null;
        AlertaDetailResponse detalle = alertaService.consultarSeguimiento(consulta.getCodigo(), consulta.getPinConsulta(), username);
        return ResponseEntity.ok(detalle);
    }

    @PostMapping("/api/portal/seguridad/responder-solicitud")
    public ResponseEntity<Void> responderSolicitud(
            @Valid @RequestBody AlertaRespuestaCiudadanoRequest respuesta,
            Principal principal) {
        String username = principal != null ? principal.getName() : null;
        alertaService.responderSolicitudInformacion(respuesta, username);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/portal/seguridad/mis-reportes")
    public ResponseEntity<Page<AlertaResponse>> misReportes(Principal principal, Pageable pageable) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(alertaService.listarMisReportes(principal.getName(), pageable));
    }
}
