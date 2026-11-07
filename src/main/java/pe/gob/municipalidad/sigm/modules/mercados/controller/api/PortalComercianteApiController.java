package pe.gob.municipalidad.sigm.modules.mercados.controller.api;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.gob.municipalidad.sigm.modules.mercados.dto.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoAutorizacion;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoContrato;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoIncidencia;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoInspeccion;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoObligacion;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoSolicitudGestion;
import pe.gob.municipalidad.sigm.modules.mercados.service.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/portal/comerciante")
public class PortalComercianteApiController {

    private final ComerciantePortalContextService contextService;
    private final PortalComercianteDashboardService dashboardService;
    private final PortalComerciantePerfilService perfilService;
    private final PortalComerciantePuestoService puestoService;
    private final PortalComercianteContratoService contratoService;
    private final PortalComercianteAutorizacionService autorizacionService;
    private final PortalComercianteFinanzasService finanzasService;
    private final PortalComercianteSolicitudService solicitudService;
    private final PortalComercianteInspeccionService inspeccionService;
    private final PortalComercianteIncidenciaService incidenciaService;
    private final PortalComercianteDocumentoService documentoService;

    public PortalComercianteApiController(
            ComerciantePortalContextService contextService,
            PortalComercianteDashboardService dashboardService,
            PortalComerciantePerfilService perfilService,
            PortalComerciantePuestoService puestoService,
            PortalComercianteContratoService contratoService,
            PortalComercianteAutorizacionService autorizacionService,
            PortalComercianteFinanzasService finanzasService,
            PortalComercianteSolicitudService solicitudService,
            PortalComercianteInspeccionService inspeccionService,
            PortalComercianteIncidenciaService incidenciaService,
            PortalComercianteDocumentoService documentoService) {
        this.contextService = contextService;
        this.dashboardService = dashboardService;
        this.perfilService = perfilService;
        this.puestoService = puestoService;
        this.contratoService = contratoService;
        this.autorizacionService = autorizacionService;
        this.finanzasService = finanzasService;
        this.solicitudService = solicitudService;
        this.inspeccionService = inspeccionService;
        this.incidenciaService = incidenciaService;
        this.documentoService = documentoService;
    }

    // -------------------------------------------------------------------------
    // CONTEXTO Y DASHBOARD
    // -------------------------------------------------------------------------

    @GetMapping("/contextos")
    @PreAuthorize("hasAuthority('PORTAL_COMERCIANTE_ACCEDER') or hasRole('COMERCIANTE') or hasRole('CIUDADANO') or hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<List<PortalComercianteContextoResponse>> listarContextos() {
        return ResponseEntity.ok(contextService.listarContextosDisponibles());
    }

    @PostMapping("/contextos/{comercianteId}/activar")
    @PreAuthorize("hasAuthority('PORTAL_COMERCIANTE_ACCEDER') or hasRole('COMERCIANTE') or hasRole('CIUDADANO') or hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<Map<String, String>> cambiarContexto(@PathVariable UUID comercianteId) {
        contextService.cambiarContextoActivo(comercianteId);
        return ResponseEntity.ok(Map.of("message", "Contexto comercial actualizado correctamente."));
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasAuthority('PORTAL_COMERCIANTE_ACCEDER') or hasRole('COMERCIANTE') or hasRole('CIUDADANO') or hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<PortalComercianteDashboardResponse> obtenerDashboard() {
        return ResponseEntity.ok(dashboardService.obtenerDashboard());
    }

    // -------------------------------------------------------------------------
    // PERFIL Y DATOS DE CONTACTO
    // -------------------------------------------------------------------------

    @GetMapping("/perfil")
    @PreAuthorize("hasAuthority('PORTAL_COMERCIANTE_PERFIL_VER') or hasRole('COMERCIANTE') or hasRole('CIUDADANO') or hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<PortalComerciantePerfilResponse> obtenerPerfil() {
        return ResponseEntity.ok(perfilService.obtenerPerfil());
    }

    @PutMapping("/perfil/contacto")
    @PreAuthorize("hasAuthority('PORTAL_COMERCIANTE_CONTACTO_EDITAR') or hasRole('COMERCIANTE') or hasRole('CIUDADANO') or hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<PortalComerciantePerfilResponse> actualizarContacto(
            @Valid @RequestBody PortalComercianteContactoUpdateRequest request) {
        return ResponseEntity.ok(perfilService.actualizarContacto(request));
    }

    // -------------------------------------------------------------------------
    // PUESTOS Y ASIGNACIONES
    // -------------------------------------------------------------------------

    @GetMapping("/puestos")
    @PreAuthorize("hasAuthority('PORTAL_COMERCIANTE_PUESTOS_VER') or hasRole('COMERCIANTE') or hasRole('CIUDADANO') or hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<List<PortalComerciantePuestoResponse>> listarPuestos() {
        return ResponseEntity.ok(puestoService.listarMisPuestos());
    }

    @GetMapping("/puestos/{id}")
    @PreAuthorize("hasAuthority('PORTAL_COMERCIANTE_PUESTOS_VER') or hasRole('COMERCIANTE') or hasRole('CIUDADANO') or hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<PortalComerciantePuestoResponse> obtenerDetallePuesto(@PathVariable UUID id) {
        return ResponseEntity.ok(puestoService.obtenerDetallePuesto(id));
    }

    @GetMapping("/asignaciones")
    @PreAuthorize("hasAuthority('PORTAL_COMERCIANTE_PUESTOS_VER') or hasRole('COMERCIANTE') or hasRole('CIUDADANO') or hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<List<PortalComercianteAsignacionResponse>> listarAsignaciones() {
        return ResponseEntity.ok(puestoService.listarMisAsignaciones());
    }

    // -------------------------------------------------------------------------
    // CONTRATOS
    // -------------------------------------------------------------------------

    @GetMapping("/contratos")
    @PreAuthorize("hasAuthority('PORTAL_COMERCIANTE_CONTRATOS_VER') or hasRole('COMERCIANTE') or hasRole('CIUDADANO') or hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<Page<PortalComercianteContratoResponse>> listarContratos(
            @RequestParam(required = false) EstadoContrato estado,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(contratoService.listarMisContratos(estado, pageable));
    }

    @GetMapping("/contratos/{id}")
    @PreAuthorize("hasAuthority('PORTAL_COMERCIANTE_CONTRATOS_VER') or hasRole('COMERCIANTE') or hasRole('CIUDADANO') or hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<PortalComercianteContratoResponse> obtenerDetalleContrato(@PathVariable UUID id) {
        return ResponseEntity.ok(contratoService.obtenerDetalleContrato(id));
    }

    @GetMapping("/contratos/{id}/descargar")
    @PreAuthorize("hasAuthority('PORTAL_COMERCIANTE_DOCUMENTOS_DESCARGAR') or hasRole('COMERCIANTE') or hasRole('CIUDADANO') or hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<byte[]> descargarContrato(@PathVariable UUID id) {
        byte[] pdf = contratoService.descargarContratoPdf(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"contrato_" + id + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    // -------------------------------------------------------------------------
    // AUTORIZACIONES
    // -------------------------------------------------------------------------

    @GetMapping("/autorizaciones")
    @PreAuthorize("hasAuthority('PORTAL_COMERCIANTE_AUTORIZACIONES_VER') or hasRole('COMERCIANTE') or hasRole('CIUDADANO') or hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<Page<PortalComercianteAutorizacionResponse>> listarAutorizaciones(
            @RequestParam(required = false) EstadoAutorizacion estado,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(autorizacionService.listarMisAutorizaciones(estado, pageable));
    }

    @GetMapping("/autorizaciones/{id}")
    @PreAuthorize("hasAuthority('PORTAL_COMERCIANTE_AUTORIZACIONES_VER') or hasRole('COMERCIANTE') or hasRole('CIUDADANO') or hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<PortalComercianteAutorizacionResponse> obtenerDetalleAutorizacion(@PathVariable UUID id) {
        return ResponseEntity.ok(autorizacionService.obtenerDetalleAutorizacion(id));
    }

    @GetMapping("/autorizaciones/{id}/descargar")
    @PreAuthorize("hasAuthority('PORTAL_COMERCIANTE_DOCUMENTOS_DESCARGAR') or hasRole('COMERCIANTE') or hasRole('CIUDADANO') or hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<byte[]> descargarAutorizacion(@PathVariable UUID id) {
        byte[] pdf = autorizacionService.descargarAutorizacionPdf(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"autorizacion_" + id + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    // -------------------------------------------------------------------------
    // FINANZAS, OBLIGACIONES Y PAGOS
    // -------------------------------------------------------------------------

    @GetMapping("/obligaciones")
    @PreAuthorize("hasAuthority('PORTAL_COMERCIANTE_FINANZAS_VER') or hasRole('COMERCIANTE') or hasRole('CIUDADANO') or hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<Page<PortalComercianteObligacionResponse>> listarObligaciones(
            @RequestParam(required = false) EstadoObligacion estado,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(finanzasService.listarMisObligaciones(estado, pageable));
    }

    @GetMapping("/obligaciones/{id}")
    @PreAuthorize("hasAuthority('PORTAL_COMERCIANTE_FINANZAS_VER') or hasRole('COMERCIANTE') or hasRole('CIUDADANO') or hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<PortalComercianteObligacionResponse> obtenerDetalleObligacion(@PathVariable UUID id) {
        return ResponseEntity.ok(finanzasService.obtenerDetalleObligacion(id));
    }

    @GetMapping("/pagos")
    @PreAuthorize("hasAuthority('PORTAL_COMERCIANTE_FINANZAS_VER') or hasRole('COMERCIANTE') or hasRole('CIUDADANO') or hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<Page<PortalComerciantePagoResponse>> listarPagos(
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(finanzasService.listarMisPagos(pageable));
    }

    @GetMapping("/pagos/{id}")
    @PreAuthorize("hasAuthority('PORTAL_COMERCIANTE_FINANZAS_VER') or hasRole('COMERCIANTE') or hasRole('CIUDADANO') or hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<PortalComerciantePagoResponse> obtenerDetallePago(@PathVariable UUID id) {
        return ResponseEntity.ok(finanzasService.obtenerDetallePago(id));
    }

    @GetMapping("/pagos/{id}/constancia")
    @PreAuthorize("hasAuthority('PORTAL_COMERCIANTE_DOCUMENTOS_DESCARGAR') or hasRole('COMERCIANTE') or hasRole('CIUDADANO') or hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<byte[]> descargarConstanciaPago(@PathVariable UUID id) {
        byte[] pdf = finanzasService.descargarConstanciaPagoPdf(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"recibo_pago_" + id + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/estado-cuenta")
    @PreAuthorize("hasAuthority('PORTAL_COMERCIANTE_FINANZAS_VER') or hasRole('COMERCIANTE') or hasRole('CIUDADANO') or hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<PortalComercianteEstadoCuentaResponse> obtenerEstadoCuenta() {
        return ResponseEntity.ok(finanzasService.obtenerEstadoCuenta());
    }

    @GetMapping("/estado-cuenta/descargar")
    @PreAuthorize("hasAuthority('PORTAL_COMERCIANTE_DOCUMENTOS_DESCARGAR') or hasRole('COMERCIANTE') or hasRole('CIUDADANO') or hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<byte[]> descargarEstadoCuentaPdf() {
        byte[] pdf = finanzasService.generarEstadoCuentaPdf();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"estado_cuenta_comerciante.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    // -------------------------------------------------------------------------
    // SOLICITUDES Y GESTIÓN DE OCUPACIÓN
    // -------------------------------------------------------------------------

    @GetMapping("/solicitudes")
    @PreAuthorize("hasAuthority('PORTAL_COMERCIANTE_SOLICITUDES_VER') or hasRole('COMERCIANTE') or hasRole('CIUDADANO') or hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<Page<PortalComercianteSolicitudResponse>> listarSolicitudes(
            @RequestParam(required = false) EstadoSolicitudGestion estado,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(solicitudService.listarMisSolicitudes(estado, pageable));
    }

    @GetMapping("/solicitudes/{id}")
    @PreAuthorize("hasAuthority('PORTAL_COMERCIANTE_SOLICITUDES_VER') or hasRole('COMERCIANTE') or hasRole('CIUDADANO') or hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<PortalComercianteSolicitudResponse> obtenerDetalleSolicitud(@PathVariable UUID id) {
        return ResponseEntity.ok(solicitudService.obtenerDetalleSolicitud(id));
    }

    @PostMapping("/solicitudes/renovacion")
    @PreAuthorize("hasAuthority('PORTAL_COMERCIANTE_SOLICITUDES_CREAR') or hasRole('COMERCIANTE') or hasRole('CIUDADANO') or hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<SolicitudGestionOcupacionResponse> presentarRenovacion(
            @Valid @RequestBody PortalComercianteRenovacionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(solicitudService.presentarSolicitudRenovacion(request));
    }

    // -------------------------------------------------------------------------
    // INSPECCIONES
    // -------------------------------------------------------------------------

    @GetMapping("/inspecciones")
    @PreAuthorize("hasAuthority('PORTAL_COMERCIANTE_ACCEDER') or hasRole('COMERCIANTE') or hasRole('CIUDADANO') or hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<Page<PortalComercianteInspeccionResponse>> listarInspecciones(
            @RequestParam(required = false) EstadoInspeccion estado,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(inspeccionService.listarMisInspecciones(estado, pageable));
    }

    @GetMapping("/inspecciones/{id}")
    @PreAuthorize("hasAuthority('PORTAL_COMERCIANTE_ACCEDER') or hasRole('COMERCIANTE') or hasRole('CIUDADANO') or hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<PortalComercianteInspeccionResponse> obtenerDetalleInspeccion(@PathVariable UUID id) {
        return ResponseEntity.ok(inspeccionService.obtenerDetalleInspeccion(id));
    }

    // -------------------------------------------------------------------------
    // INCIDENCIAS
    // -------------------------------------------------------------------------

    @GetMapping("/incidencias")
    @PreAuthorize("hasAuthority('PORTAL_COMERCIANTE_ACCEDER') or hasRole('COMERCIANTE') or hasRole('CIUDADANO') or hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<Page<PortalComercianteIncidenciaResponse>> listarIncidencias(
            @RequestParam(required = false) EstadoIncidencia estado,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(incidenciaService.listarMisIncidencias(estado, pageable));
    }

    @GetMapping("/incidencias/{id}")
    @PreAuthorize("hasAuthority('PORTAL_COMERCIANTE_ACCEDER') or hasRole('COMERCIANTE') or hasRole('CIUDADANO') or hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<PortalComercianteIncidenciaResponse> obtenerDetalleIncidencia(@PathVariable UUID id) {
        return ResponseEntity.ok(incidenciaService.obtenerDetalleIncidencia(id));
    }

    @PostMapping("/incidencias")
    @PreAuthorize("hasAuthority('PORTAL_COMERCIANTE_INCIDENCIAS_CREAR') or hasRole('COMERCIANTE') or hasRole('CIUDADANO') or hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<IncidenciaResponse> registrarIncidencia(
            @Valid @RequestBody PortalComercianteIncidenciaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(incidenciaService.reportarIncidencia(request));
    }

    // -------------------------------------------------------------------------
    // CENTRO DE DOCUMENTOS Y DESCARGA CONSOLIDADA
    // -------------------------------------------------------------------------

    @GetMapping("/documentos")
    @PreAuthorize("hasAuthority('PORTAL_COMERCIANTE_DOCUMENTOS_DESCARGAR') or hasRole('COMERCIANTE') or hasRole('CIUDADANO') or hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<Page<PortalComercianteDocumentoResponse>> listarDocumentos(
            @RequestParam(required = false) String tipo,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(documentoService.listarMisDocumentos(tipo, pageable));
    }

    @GetMapping("/documentos/{tipo}/{id}/descargar")
    @PreAuthorize("hasAuthority('PORTAL_COMERCIANTE_DOCUMENTOS_DESCARGAR') or hasRole('COMERCIANTE') or hasRole('CIUDADANO') or hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<byte[]> descargarDocumentoConsolidado(
            @PathVariable String tipo,
            @PathVariable UUID id) {
        PortalComercianteDocumentoService.DocumentoDescarga descarga = documentoService.descargarDocumento(tipo, id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + descarga.getNombreArchivo() + "\"")
                .contentType(MediaType.parseMediaType(descarga.getContentType()))
                .body(descarga.getContenido());
    }
}
