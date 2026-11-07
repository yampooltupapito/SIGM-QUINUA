package pe.gob.municipalidad.sigm.modules.mesapartes.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.common.repository.OficinaRepository;
import pe.gob.municipalidad.sigm.common.response.ApiResponse;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.AtencionDtos.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.DerivacionDtos.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.DerivacionDtos.DerivacionResponse;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.DerivacionDtos.DerivarExpedienteRequest;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvAdminDtos.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvDtos.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvSolicitudDtos.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoDerivacion;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoExpediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoPropuesta;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.TipoRelacionDocumentoExpediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.*;

import java.util.List;
import java.util.UUID;

/**
 * Controlador REST para las operaciones de Mesa de Partes Virtual de SIGM-QUINUA.
 * Soporta registro ciudadano, formularios dinámicos, presentación atómica, constancias, bandejas,
 * derivaciones internas, asignaciones, actuaciones administrativas y seguimiento institucional.
 */
@RestController
@RequestMapping("/api/mesa-partes")
public class MesaPartesRestController {

    private final ExpedienteService expedienteService;
    private final TipoTramiteService tipoTramiteService;
    private final FormularioDinamicoService formularioDinamicoService;
    private final SolicitudCiudadanaService solicitudCiudadanaService;
    private final BandejaMesaPartesService bandejaMesaPartesService;
    private final RevisionRequisitosService revisionRequisitosService;
    private final ObservacionExpedienteService observacionExpedienteService;
    private final PrioridadExpedienteService prioridadExpedienteService;
    private final ConstanciaRecepcionService constanciaRecepcionService;
    private final DerivacionExpedienteService derivacionExpedienteService;
    private final AsignacionExpedienteService asignacionExpedienteService;
    private final ActuacionExpedienteService actuacionExpedienteService;
    private final SeguimientoInternoService seguimientoInternoService;
    private final EstadisticasDerivacionService estadisticasDerivacionService;
    private final OficinaRepository oficinaRepository;

    private final AtencionExpedienteService atencionExpedienteService;
    private final EvaluacionExpedienteService evaluacionExpedienteService;
    private final PropuestaDecisionService propuestaDecisionService;
    private final RevisionDecisionService revisionDecisionService;
    private final DecisionExpedienteService decisionExpedienteService;
    private final NotificacionResultadoService notificacionResultadoService;
    private final CierreExpedienteService cierreExpedienteService;
    private final ArchivoExpedienteService archivoExpedienteService;
    private final EstadisticasAtencionService estadisticasAtencionService;

    public MesaPartesRestController(ExpedienteService expedienteService,
                                    TipoTramiteService tipoTramiteService,
                                    FormularioDinamicoService formularioDinamicoService,
                                    SolicitudCiudadanaService solicitudCiudadanaService,
                                    BandejaMesaPartesService bandejaMesaPartesService,
                                    RevisionRequisitosService revisionRequisitosService,
                                    ObservacionExpedienteService observacionExpedienteService,
                                    PrioridadExpedienteService prioridadExpedienteService,
                                    ConstanciaRecepcionService constanciaRecepcionService,
                                    DerivacionExpedienteService derivacionExpedienteService,
                                    AsignacionExpedienteService asignacionExpedienteService,
                                    ActuacionExpedienteService actuacionExpedienteService,
                                    SeguimientoInternoService seguimientoInternoService,
                                    EstadisticasDerivacionService estadisticasDerivacionService,
                                    OficinaRepository oficinaRepository,
                                    AtencionExpedienteService atencionExpedienteService,
                                    EvaluacionExpedienteService evaluacionExpedienteService,
                                    PropuestaDecisionService propuestaDecisionService,
                                    RevisionDecisionService revisionDecisionService,
                                    DecisionExpedienteService decisionExpedienteService,
                                    NotificacionResultadoService notificacionResultadoService,
                                    CierreExpedienteService cierreExpedienteService,
                                    ArchivoExpedienteService archivoExpedienteService,
                                    EstadisticasAtencionService estadisticasAtencionService) {
        this.expedienteService = expedienteService;
        this.tipoTramiteService = tipoTramiteService;
        this.formularioDinamicoService = formularioDinamicoService;
        this.solicitudCiudadanaService = solicitudCiudadanaService;
        this.bandejaMesaPartesService = bandejaMesaPartesService;
        this.revisionRequisitosService = revisionRequisitosService;
        this.observacionExpedienteService = observacionExpedienteService;
        this.prioridadExpedienteService = prioridadExpedienteService;
        this.constanciaRecepcionService = constanciaRecepcionService;
        this.derivacionExpedienteService = derivacionExpedienteService;
        this.asignacionExpedienteService = asignacionExpedienteService;
        this.actuacionExpedienteService = actuacionExpedienteService;
        this.seguimientoInternoService = seguimientoInternoService;
        this.estadisticasDerivacionService = estadisticasDerivacionService;
        this.oficinaRepository = oficinaRepository;
        this.atencionExpedienteService = atencionExpedienteService;
        this.evaluacionExpedienteService = evaluacionExpedienteService;
        this.propuestaDecisionService = propuestaDecisionService;
        this.revisionDecisionService = revisionDecisionService;
        this.decisionExpedienteService = decisionExpedienteService;
        this.notificacionResultadoService = notificacionResultadoService;
        this.cierreExpedienteService = cierreExpedienteService;
        this.archivoExpedienteService = archivoExpedienteService;
        this.estadisticasAtencionService = estadisticasAtencionService;
    }

    /* =========================================================================
     * TIPOS DE TRÁMITE Y FORMULARIOS DINÁMICOS
     * ========================================================================= */

    @GetMapping("/tipos-tramite")
    public ResponseEntity<ApiResponse<List<TipoTramiteResponse>>> listarTiposTramite(
            @RequestParam(value = "soloActivos", defaultValue = "true") boolean soloActivos
    ) {
        List<TipoTramiteResponse> list = soloActivos ? tipoTramiteService.listarActivos() : tipoTramiteService.listarTodos();
        return ResponseEntity.ok(ApiResponse.of("Tipos de trámite recuperados.", list));
    }

    @GetMapping("/tipos-tramite/{id}")
    public ResponseEntity<ApiResponse<TipoTramiteResponse>> obtenerTipoTramite(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.of(tipoTramiteService.obtenerPorId(id)));
    }

    @GetMapping("/tipos-tramite/{id}/formulario")
    public ResponseEntity<ApiResponse<EsquemaTramiteResponse>> obtenerEsquemaTramite(@PathVariable Long id) {
        EsquemaTramiteResponse esquema = formularioDinamicoService.obtenerEsquemaTramite(id);
        return ResponseEntity.ok(ApiResponse.of("Esquema de trámite y formulario dinámico recuperado.", esquema));
    }

    @PostMapping("/tipos-tramite")
    @PreAuthorize("hasAuthority('MESAPARTES_CONFIGURAR_TRAMITES') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<ApiResponse<TipoTramiteResponse>> crearTipoTramite(
            @Valid @RequestBody TipoTramiteRequest request,
            Authentication auth
    ) {
        TipoTramiteResponse resp = tipoTramiteService.crearTipoTramite(request, auth.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of("Tipo de trámite creado exitosamente.", resp));
    }

    @PutMapping("/tipos-tramite/{id}")
    @PreAuthorize("hasAuthority('MESAPARTES_CONFIGURAR_TRAMITES') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<ApiResponse<TipoTramiteResponse>> actualizarTipoTramite(
            @PathVariable Long id,
            @Valid @RequestBody TipoTramiteRequest request,
            Authentication auth
    ) {
        TipoTramiteResponse resp = tipoTramiteService.actualizarTipoTramite(id, request, auth.getName());
        return ResponseEntity.ok(ApiResponse.of("Tipo de trámite actualizado exitosamente.", resp));
    }

    /* =========================================================================
     * SOLICITUDES CIUDADANAS: BORRADORES, PRESENTACIÓN Y CONSTANCIAS
     * ========================================================================= */

    @PostMapping("/expedientes/borradores")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ExpedienteDetalleResponse>> guardarNuevoBorrador(
            @Valid @RequestBody GuardarBorradorSolicitudRequest request,
            Authentication auth
    ) {
        ExpedienteDetalleResponse resp = solicitudCiudadanaService.guardarBorrador(null, request, auth.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of("Borrador de solicitud guardado exitosamente.", resp));
    }

    @RequestMapping(value = "/expedientes/borradores/{uuid}", method = {RequestMethod.PUT, RequestMethod.PATCH})
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ExpedienteDetalleResponse>> actualizarBorrador(
            @PathVariable UUID uuid,
            @Valid @RequestBody GuardarBorradorSolicitudRequest request,
            Authentication auth
    ) {
        ExpedienteDetalleResponse resp = solicitudCiudadanaService.guardarBorrador(uuid, request, auth.getName());
        return ResponseEntity.ok(ApiResponse.of("Borrador de solicitud actualizado exitosamente.", resp));
    }

    @GetMapping("/expedientes/borradores/{uuid}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ExpedienteDetalleResponse>> obtenerBorrador(
            @PathVariable UUID uuid,
            Authentication auth
    ) {
        ExpedienteDetalleResponse resp = solicitudCiudadanaService.obtenerBorrador(uuid, auth.getName());
        return ResponseEntity.ok(ApiResponse.of("Borrador recuperado.", resp));
    }

    @PostMapping(value = "/expedientes/{uuid}/documentos/subir", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ExpedienteDetalleResponse>> subirDocumentoBorrador(
            @PathVariable UUID uuid,
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam(value = "tipoRelacion", defaultValue = "ANEXO") TipoRelacionDocumentoExpediente tipoRelacion,
            Authentication auth
    ) {
        ExpedienteDetalleResponse resp = solicitudCiudadanaService.subirDocumentoBorrador(uuid, archivo, tipoRelacion, auth.getName());
        return ResponseEntity.ok(ApiResponse.of("Documento adjuntado exitosamente a la solicitud.", resp));
    }

    @DeleteMapping("/expedientes/{uuid}/documentos/archivo/{documentoUuid}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> eliminarDocumentoBorrador(
            @PathVariable UUID uuid,
            @PathVariable UUID documentoUuid,
            Authentication auth
    ) {
        solicitudCiudadanaService.eliminarDocumentoBorrador(uuid, documentoUuid, auth.getName());
        return ResponseEntity.ok(ApiResponse.of("Documento eliminado del borrador.", null));
    }

    @PostMapping("/expedientes/{identificador}/presentar")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ExpedienteResponse>> presentarExpediente(
            @PathVariable String identificador,
            @RequestBody(required = false) PresentarSolicitudRequest request,
            Authentication auth
    ) {
        ExpedienteResponse resp;
        if (identificador.contains("-")) {
            // Es un identificador UUID
            resp = solicitudCiudadanaService.presentarSolicitud(UUID.fromString(identificador), request, auth.getName());
        } else {
            // Es un ID numérico secuencial (compatibilidad)
            PresentarExpedienteRequest legReq = request != null ? PresentarExpedienteRequest.builder().build() : null;
            resp = expedienteService.presentarExpediente(Long.parseLong(identificador), legReq, auth.getName());
        }
        return ResponseEntity.ok(ApiResponse.of("Expediente presentado formalmente. Número generado: " + resp.getNumeroExpediente(), resp));
    }

    @GetMapping("/mis-solicitudes")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<MisSolicitudesItemResponse>>> listarMisSolicitudes(
            @RequestParam(value = "estado", required = false) EstadoExpediente estado,
            @RequestParam(value = "termino", required = false) String termino,
            @PageableDefault(size = 15, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Authentication auth
    ) {
        Page<MisSolicitudesItemResponse> page = solicitudCiudadanaService.listarMisSolicitudes(estado, termino, pageable, auth.getName());
        return ResponseEntity.ok(ApiResponse.of("Solicitudes del administrado recuperadas.", page));
    }

    @GetMapping("/expedientes/{uuid}/constancia")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> descargarConstanciaPdf(
            @PathVariable UUID uuid,
            @RequestParam(value = "baseUrl", required = false) String baseUrl,
            @RequestParam(value = "download", defaultValue = "false") boolean download,
            Authentication auth
    ) {
        byte[] pdfBytes = solicitudCiudadanaService.descargarConstanciaPdf(uuid, baseUrl, auth.getName());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        String filename = "Constancia-Expediente-" + uuid + ".pdf";
        headers.setContentDisposition(download
                ? org.springframework.http.ContentDisposition.attachment().filename(filename).build()
                : org.springframework.http.ContentDisposition.inline().filename(filename).build());
        headers.setContentLength(pdfBytes.length);
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    @GetMapping("/expedientes/{uuid}/constancia/datos")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ConstanciaPresentacionResponse>> obtenerDatosConstancia(
            @PathVariable UUID uuid,
            @RequestParam(value = "baseUrl", required = false) String baseUrl,
            Authentication auth
    ) {
        ExpedienteDetalleResponse detalle = solicitudCiudadanaService.obtenerMiExpediente(uuid, auth.getName());
        return ResponseEntity.ok(ApiResponse.of("Datos de la constancia de presentación.", null));
    }

    /* =========================================================================
     * GESTIÓN GENERAL DE EXPEDIENTES (OPERADORES Y CONSULTAS)
     * ========================================================================= */

    @GetMapping("/expedientes")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<ExpedienteResponse>>> listarExpedientes(
            @RequestParam(value = "estado", required = false) EstadoExpediente estado,
            @RequestParam(value = "oficinaId", required = false) Long oficinaId,
            @RequestParam(value = "tipoTramiteId", required = false) Long tipoTramiteId,
            @RequestParam(value = "termino", required = false) String termino,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Authentication auth
    ) {
        Page<ExpedienteResponse> page = expedienteService.listarExpedientes(estado, oficinaId, tipoTramiteId, termino, pageable, auth.getName());
        return ResponseEntity.ok(ApiResponse.of("Expedientes recuperados.", page));
    }

    @GetMapping("/expedientes/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ExpedienteDetalleResponse>> obtenerExpediente(
            @PathVariable Long id,
            Authentication auth
    ) {
        ExpedienteDetalleResponse detalle = expedienteService.obtenerDetalle(id, auth.getName());
        return ResponseEntity.ok(ApiResponse.of("Detalle de expediente recuperado.", detalle));
    }

    @GetMapping("/expedientes/{id}/historial")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<ExpedienteHistorialResponse>>> obtenerHistorial(
            @PathVariable Long id,
            Authentication auth
    ) {
        List<ExpedienteHistorialResponse> hist = expedienteService.obtenerHistorial(id, auth.getName());
        return ResponseEntity.ok(ApiResponse.of("Historial de movimientos recuperado.", hist));
    }

    @GetMapping("/expedientes/{id}/documentos")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<ExpedienteDocumentoResponse>>> obtenerDocumentos(
            @PathVariable Long id,
            Authentication auth
    ) {
        List<ExpedienteDocumentoResponse> docs = expedienteService.obtenerDocumentos(id, auth.getName());
        return ResponseEntity.ok(ApiResponse.of("Documentos vinculados al expediente.", docs));
    }

    @GetMapping("/estadisticas")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'MESA_PARTES', 'OPERADOR', 'JEFE_OFICINA')")
    public ResponseEntity<ApiResponse<MpvEstadisticasResponse>> obtenerEstadisticas(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.of("Estadísticas de Mesa de Partes.", expedienteService.obtenerEstadisticas(auth.getName())));
    }

    @PostMapping("/expedientes/{id}/documentos")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ExpedienteResponse>> asociarDocumento(
            @PathVariable Long id,
            @Valid @RequestBody AsociarDocumentoRequest request,
            Authentication auth
    ) {
        ExpedienteResponse resp = expedienteService.asociarDocumento(id, request, auth.getName());
        return ResponseEntity.ok(ApiResponse.of("Documento vinculado al expediente exitosamente.", resp));
    }

    @DeleteMapping("/expedientes/{id}/documentos/{documentoId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> desasociarDocumento(
            @PathVariable Long id,
            @PathVariable Long documentoId,
            Authentication auth
    ) {
        expedienteService.desasociarDocumento(id, documentoId, auth.getName());
        return ResponseEntity.ok(ApiResponse.of("Documento desvinculado del expediente correctamente.", null));
    }

    @PostMapping("/expedientes/{id}/recibir")
    @PreAuthorize("hasAuthority('MESAPARTES_EXPEDIENTE_RECIBIR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'MESA_PARTES', 'OPERADOR')")
    public ResponseEntity<ApiResponse<ExpedienteResponse>> recibirExpediente(
            @PathVariable Long id,
            @RequestParam(value = "observaciones", required = false) String observaciones,
            Authentication auth
    ) {
        ExpedienteResponse resp = expedienteService.recibirExpediente(id, observaciones, auth.getName());
        return ResponseEntity.ok(ApiResponse.of("Expediente recibido formalmente.", resp));
    }


    @PostMapping("/expedientes/{id}/observar")
    @PreAuthorize("hasAuthority('MESAPARTES_EXPEDIENTE_OBSERVAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'MESA_PARTES', 'OPERADOR', 'JEFE_OFICINA')")
    public ResponseEntity<ApiResponse<ExpedienteResponse>> observarExpediente(
            @PathVariable Long id,
            @Valid @RequestBody ObservarExpedienteRequest request,
            Authentication auth
    ) {
        ExpedienteResponse resp = expedienteService.observarExpediente(id, request, auth.getName());
        return ResponseEntity.ok(ApiResponse.of("Expediente observado formalmente.", resp));
    }

    @PostMapping("/expedientes/{id}/subsanar")
    @PreAuthorize("hasAuthority('MESAPARTES_EXPEDIENTE_SUBSANAR') or hasRole('CIUDADANO')")
    public ResponseEntity<ApiResponse<ExpedienteResponse>> subsanarExpediente(
            @PathVariable Long id,
            @Valid @RequestBody SubsanarExpedienteRequest request,
            Authentication auth
    ) {
        ExpedienteResponse resp = expedienteService.subsanarExpediente(id, request, auth.getName());
        return ResponseEntity.ok(ApiResponse.of("Subsanación registrada exitosamente.", resp));
    }

    @PostMapping("/expedientes/{id}/resolver")
    @PreAuthorize("hasAuthority('MESAPARTES_EXPEDIENTE_RESOLVER') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA')")
    public ResponseEntity<ApiResponse<ExpedienteResponse>> resolverExpediente(
            @PathVariable Long id,
            @Valid @RequestBody CambiarEstadoRequest request,
            Authentication auth
    ) {
        ExpedienteResponse resp = expedienteService.resolverExpediente(id, request, auth.getName());
        return ResponseEntity.ok(ApiResponse.of("Expediente resuelto satisfactoriamente.", resp));
    }

    @PostMapping("/expedientes/{id}/archivar-estado")
    @PreAuthorize("hasAuthority('MESAPARTES_EXPEDIENTE_ARCHIVAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA')")
    public ResponseEntity<ApiResponse<ExpedienteResponse>> archivarEstadoExpediente(
            @PathVariable Long id,
            @Valid @RequestBody CambiarEstadoRequest request,
            Authentication auth
    ) {
        ExpedienteResponse resp = expedienteService.archivarExpediente(id, request, auth.getName());
        return ResponseEntity.ok(ApiResponse.of("Expediente archivado correctamente.", resp));
    }

    @PostMapping("/expedientes/{id}/anular")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<ApiResponse<ExpedienteResponse>> anularExpediente(
            @PathVariable Long id,
            @Valid @RequestBody CambiarEstadoRequest request,
            Authentication auth
    ) {
        ExpedienteResponse resp = expedienteService.anularExpediente(id, request, auth.getName());
        return ResponseEntity.ok(ApiResponse.of("Expediente anulado correctamente.", resp));
    }

    /* =========================================================================
     * BANDEJA INSTITUCIONAL, REVISIÓN, OBSERVACIONES Y CONSTANCIA DE RECEPCIÓN (PROMPT 14)
     * ========================================================================= */

    @GetMapping("/bandeja")
    @PreAuthorize("hasAuthority('MESAPARTES_EXPEDIENTE_VER_OFICINA') or hasAuthority('MESAPARTES_EXPEDIENTE_VER_TODOS') or hasAnyRole('ADMIN', 'SUPERADMIN', 'MESA_PARTES', 'OPERADOR', 'JEFE_OFICINA')")
    public ResponseEntity<ApiResponse<Page<ExpedienteResponse>>> listarBandejaInstitucional(
            @RequestParam(value = "estado", required = false) EstadoExpediente estado,
            @RequestParam(value = "oficinaId", required = false) Long oficinaId,
            @RequestParam(value = "tipoTramiteId", required = false) Long tipoTramiteId,
            @RequestParam(value = "prioridad", required = false) pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.PrioridadExpediente prioridad,
            @RequestParam(value = "canalIngreso", required = false) pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.CanalIngreso canalIngreso,
            @RequestParam(value = "fechaDesde", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate fechaDesde,
            @RequestParam(value = "fechaHasta", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate fechaHasta,
            @RequestParam(value = "q", required = false) String q,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Authentication auth
    ) {
        Page<ExpedienteResponse> page = bandejaMesaPartesService.listarBandeja(
                estado, oficinaId, tipoTramiteId, prioridad, canalIngreso, fechaDesde, fechaHasta, q, pageable, auth.getName()
        );
        return ResponseEntity.ok(ApiResponse.of("Bandeja institucional de expedientes recuperada.", page));
    }

    @GetMapping("/bandeja/estadisticas")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'MESA_PARTES', 'OPERADOR', 'JEFE_OFICINA')")
    public ResponseEntity<ApiResponse<pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvAdminDtos.BandejaEstadisticasAdminResponse>> obtenerEstadisticasBandeja(
            Authentication auth
    ) {
        return ResponseEntity.ok(ApiResponse.of("Estadísticas de bandeja institucional.",
                bandejaMesaPartesService.obtenerEstadisticasBandeja(auth.getName())));
    }

    @GetMapping("/expedientes/{id}/requisitos")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvAdminDtos.RevisionRequisitoResponse>>> obtenerRequisitosExpediente(
            @PathVariable Long id,
            Authentication auth
    ) {
        List<pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvAdminDtos.RevisionRequisitoResponse> list =
                revisionRequisitosService.obtenerOInicializarRequisitos(id, auth.getName());
        return ResponseEntity.ok(ApiResponse.of("Lista de verificación de requisitos del expediente.", list));
    }

    @PostMapping("/expedientes/{id}/requisitos/{requisitoId}/verificar")
    @PreAuthorize("hasAuthority('MESAPARTES_EXPEDIENTE_RECIBIR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'MESA_PARTES', 'OPERADOR')")
    public ResponseEntity<ApiResponse<pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvAdminDtos.RevisionRequisitoResponse>> verificarRequisito(
            @PathVariable Long id,
            @PathVariable Long requisitoId,
            @Valid @RequestBody pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvAdminDtos.VerificarRequisitoRequest request,
            Authentication auth
    ) {
        pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvAdminDtos.RevisionRequisitoResponse resp =
                revisionRequisitosService.verificarRequisito(id, requisitoId, request, auth.getName());
        return ResponseEntity.ok(ApiResponse.of("Requisito verificado correctamente.", resp));
    }

    @PostMapping("/expedientes/{id}/observaciones")
    @PreAuthorize("hasAuthority('MESAPARTES_EXPEDIENTE_OBSERVAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'MESA_PARTES', 'OPERADOR', 'JEFE_OFICINA')")
    public ResponseEntity<ApiResponse<ObservacionResponse>> registrarObservacion(
            @PathVariable Long id,
            @Valid @RequestBody pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvAdminDtos.RegistrarObservacionAdminRequest request,
            Authentication auth
    ) {
        ObservacionResponse resp = observacionExpedienteService.registrarObservacion(id, request, auth.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of("Observación registrada formalmente.", resp));
    }

    @GetMapping("/expedientes/{id}/observaciones")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<ObservacionResponse>>> listarObservacionesExpediente(
            @PathVariable Long id,
            Authentication auth
    ) {
        List<ObservacionResponse> list = observacionExpedienteService.listarObservaciones(id, auth.getName());
        return ResponseEntity.ok(ApiResponse.of("Observaciones del expediente recuperadas.", list));
    }

    @PostMapping("/expedientes/{id}/prioridad")
    @PreAuthorize("hasAuthority('MESAPARTES_EXPEDIENTE_OBSERVAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'MESA_PARTES', 'JEFE_OFICINA')")
    public ResponseEntity<ApiResponse<ExpedienteResponse>> actualizarPrioridad(
            @PathVariable Long id,
            @Valid @RequestBody pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvAdminDtos.ActualizarPrioridadRequest request,
            Authentication auth
    ) {
        ExpedienteResponse resp = prioridadExpedienteService.actualizarPrioridad(id, request, auth.getName());
        return ResponseEntity.ok(ApiResponse.of("Prioridad del expediente actualizada.", resp));
    }

    @GetMapping("/expedientes/{id}/prioridad/historial")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'MESA_PARTES', 'OPERADOR', 'JEFE_OFICINA')")
    public ResponseEntity<ApiResponse<List<pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvAdminDtos.CambioPrioridadResponse>>> listarHistorialPrioridades(
            @PathVariable Long id,
            Authentication auth
    ) {
        List<pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvAdminDtos.CambioPrioridadResponse> hist =
                prioridadExpedienteService.listarHistorialPrioridades(id, auth.getName());
        return ResponseEntity.ok(ApiResponse.of("Historial de cambios de prioridad.", hist));
    }

    @GetMapping("/expedientes/{id}/constancia-recepcion")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> descargarConstanciaRecepcionPdf(
            @PathVariable Long id,
            @RequestParam(value = "baseUrl", required = false) String baseUrl,
            @RequestParam(value = "download", defaultValue = "false") boolean download,
            Authentication auth
    ) {
        byte[] pdfBytes = constanciaRecepcionService.generarConstanciaRecepcionPdf(id, baseUrl, auth.getName());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        String filename = "Constancia-Recepcion-Expediente-" + id + ".pdf";
        headers.setContentDisposition(download
                ? org.springframework.http.ContentDisposition.attachment().filename(filename).build()
                : org.springframework.http.ContentDisposition.inline().filename(filename).build());
        headers.setContentLength(pdfBytes.length);
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    @GetMapping("/expedientes/{id}/constancia-recepcion/datos")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvAdminDtos.ConstanciaRecepcionResponse>> obtenerDatosConstanciaRecepcion(
            @PathVariable Long id,
            @RequestParam(value = "baseUrl", required = false) String baseUrl,
            Authentication auth
    ) {
        pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvAdminDtos.ConstanciaRecepcionResponse datos =
                constanciaRecepcionService.obtenerDatosConstancia(id, baseUrl, auth.getName());
        return ResponseEntity.ok(ApiResponse.of("Datos de la constancia de recepción administrativa.", datos));
    }

    /* =========================================================================
     * PROMPT 15: DERIVACIÓN DE EXPEDIENTES Y CIRCULACIÓN INTERNA
     * ========================================================================= */

    @GetMapping("/derivaciones")
    @PreAuthorize("hasAuthority('MESAPARTES_DERIVACION_VER') or hasAnyRole('ADMIN', 'SUPERADMIN', 'MESA_PARTES', 'OPERADOR', 'JEFE_OFICINA')")
    public ResponseEntity<ApiResponse<Page<DerivacionResponse>>> listarDerivaciones(
            @RequestParam(value = "oficinaId", required = false) Long oficinaId,
            @RequestParam(value = "estado", required = false) EstadoDerivacion estado,
            @PageableDefault(size = 15, sort = "fechaDerivacion", direction = Sort.Direction.DESC) Pageable pageable,
            Authentication auth
    ) {
        Page<DerivacionResponse> page = derivacionExpedienteService.listarDerivaciones(oficinaId, estado, pageable, auth.getName());
        return ResponseEntity.ok(ApiResponse.of("Listado de derivaciones de expedientes.", page));
    }

    @GetMapping("/derivaciones/recibidas")
    @PreAuthorize("hasAuthority('MESAPARTES_DERIVACION_VER') or hasAnyRole('ADMIN', 'SUPERADMIN', 'MESA_PARTES', 'OPERADOR', 'JEFE_OFICINA')")
    public ResponseEntity<ApiResponse<Page<DerivacionResponse>>> listarDerivacionesRecibidas(
            @RequestParam(value = "estado", required = false) EstadoDerivacion estado,
            @PageableDefault(size = 15, sort = "fechaDerivacion", direction = Sort.Direction.DESC) Pageable pageable,
            Authentication auth
    ) {
        Page<DerivacionResponse> page = derivacionExpedienteService.listarDerivacionesRecibidas(estado, pageable, auth.getName());
        return ResponseEntity.ok(ApiResponse.of("Listado de derivaciones recibidas en su unidad orgánica.", page));
    }

    @GetMapping("/derivaciones/{id}")
    @PreAuthorize("hasAuthority('MESAPARTES_DERIVACION_VER') or hasAnyRole('ADMIN', 'SUPERADMIN', 'MESA_PARTES', 'OPERADOR', 'JEFE_OFICINA')")
    public ResponseEntity<ApiResponse<DerivacionResponse>> obtenerDerivacionPorId(
            @PathVariable Long id,
            Authentication auth
    ) {
        DerivacionResponse response = derivacionExpedienteService.obtenerPorId(id, auth.getName());
        return ResponseEntity.ok(ApiResponse.of("Detalle de la derivación.", response));
    }

    @PostMapping("/expedientes/{id}/derivar")
    @PreAuthorize("hasAuthority('MESAPARTES_DERIVACION_CREAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'MESA_PARTES', 'OPERADOR', 'JEFE_OFICINA')")
    public ResponseEntity<ApiResponse<DerivacionResponse>> derivarExpediente(
            @PathVariable Long id,
            @Valid @RequestBody DerivarExpedienteRequest request,
            Authentication auth
    ) {
        DerivacionResponse response = derivacionExpedienteService.derivarExpediente(id, request, auth.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of("Expediente derivado exitosamente.", response));
    }

    @PostMapping("/derivaciones/{id}/recibir")
    @PreAuthorize("hasAuthority('MESAPARTES_DERIVACION_RECIBIR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'MESA_PARTES', 'OPERADOR', 'JEFE_OFICINA')")
    public ResponseEntity<ApiResponse<DerivacionResponse>> recibirDerivacion(
            @PathVariable Long id,
            @RequestBody(required = false) RecepcionDerivacionRequest request,
            Authentication auth
    ) {
        DerivacionResponse response = derivacionExpedienteService.recibirDerivacion(id, request, auth.getName());
        return ResponseEntity.ok(ApiResponse.of("Recepción de derivación confirmada con éxito.", response));
    }

    @PostMapping("/derivaciones/{id}/rechazar")
    @PreAuthorize("hasAuthority('MESAPARTES_DERIVACION_RECHAZAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'MESA_PARTES', 'OPERADOR', 'JEFE_OFICINA')")
    public ResponseEntity<ApiResponse<DerivacionResponse>> rechazarDerivacion(
            @PathVariable Long id,
            @Valid @RequestBody RechazoDevolucionRequest request,
            Authentication auth
    ) {
        DerivacionResponse response = derivacionExpedienteService.rechazarDerivacion(id, request, auth.getName());
        return ResponseEntity.ok(ApiResponse.of("Derivación rechazada formalmente.", response));
    }

    @PostMapping("/derivaciones/{id}/devolver")
    @PreAuthorize("hasAuthority('MESAPARTES_DERIVACION_DEVOLVER') or hasAnyRole('ADMIN', 'SUPERADMIN', 'MESA_PARTES', 'OPERADOR', 'JEFE_OFICINA')")
    public ResponseEntity<ApiResponse<DerivacionResponse>> devolverDerivacion(
            @PathVariable Long id,
            @Valid @RequestBody RechazoDevolucionRequest request,
            Authentication auth
    ) {
        DerivacionResponse response = derivacionExpedienteService.devolverDerivacion(id, request, auth.getName());
        return ResponseEntity.ok(ApiResponse.of("Derivación devuelta a oficina de origen.", response));
    }

    /* =========================================================================
     * ASIGNACIÓN Y REASIGNACIÓN DE RESPONSABLES
     * ========================================================================= */

    @PostMapping("/expedientes/{id}/asignar")
    @PreAuthorize("hasAuthority('MESAPARTES_ASIGNACION_CREAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'MESA_PARTES', 'OPERADOR', 'JEFE_OFICINA')")
    public ResponseEntity<ApiResponse<AsignacionResponse>> asignarResponsable(
            @PathVariable Long id,
            @Valid @RequestBody AsignarExpedienteRequest request,
            Authentication auth
    ) {
        AsignacionResponse response = asignacionExpedienteService.asignarResponsable(id, request, auth.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of("Responsable asignado al expediente exitosamente.", response));
    }

    @PostMapping("/expedientes/{id}/reasignar")
    @PreAuthorize("hasAuthority('MESAPARTES_ASIGNACION_MODIFICAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'MESA_PARTES', 'OPERADOR', 'JEFE_OFICINA')")
    public ResponseEntity<ApiResponse<AsignacionResponse>> reasignarResponsable(
            @PathVariable Long id,
            @Valid @RequestBody ReasignarExpedienteRequest request,
            Authentication auth
    ) {
        AsignacionResponse response = asignacionExpedienteService.reasignarResponsable(id, request, auth.getName());
        return ResponseEntity.ok(ApiResponse.of("Expediente reasignado exitosamente con auditoría.", response));
    }

    @GetMapping("/mis-expedientes")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<ExpedienteResponse>>> listarMisExpedientes(
            @PageableDefault(size = 15, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Authentication auth
    ) {
        Page<ExpedienteResponse> page = asignacionExpedienteService.listarMisExpedientes(pageable, auth.getName());
        return ResponseEntity.ok(ApiResponse.of("Mis expedientes asignados para atención.", page));
    }

    @GetMapping("/oficinas/{id}/trabajadores")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<UsuarioSimpleDto>>> listarTrabajadoresDeOficina(
            @PathVariable Long id,
            Authentication auth
    ) {
        List<UsuarioSimpleDto> lista = asignacionExpedienteService.listarTrabajadoresHabilitados(id, auth.getName());
        return ResponseEntity.ok(ApiResponse.of("Trabajadores activos de la oficina.", lista));
    }

    @GetMapping("/oficinas-activas")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<OficinaSimpleDto>>> listarOficinasActivas() {
        List<OficinaSimpleDto> list = oficinaRepository.findByEstado("ACTIVO").stream()
                .map(o -> OficinaSimpleDto.builder()
                        .id(o.getId())
                        .codigo(o.getCodigo())
                        .nombre(o.getNombre())
                        .sigla(o.getSigla())
                        .build())
                .toList();
        return ResponseEntity.ok(ApiResponse.of("Catálogo institucional de oficinas activas.", list));
    }

    /* =========================================================================
     * ACTUACIONES INTERNAS Y SEGUIMIENTO INTEGRAL
     * ========================================================================= */

    @PostMapping("/expedientes/{id}/actuaciones")
    @PreAuthorize("hasAuthority('MESAPARTES_ACTUACION_CREAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'MESA_PARTES', 'OPERADOR', 'JEFE_OFICINA')")
    public ResponseEntity<ApiResponse<ActuacionResponse>> registrarActuacion(
            @PathVariable Long id,
            @Valid @RequestBody RegistrarActuacionRequest request,
            Authentication auth
    ) {
        ActuacionResponse response = actuacionExpedienteService.registrarActuacion(id, request, auth.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of("Actuación interna registrada satisfactoriamente.", response));
    }

    @GetMapping("/expedientes/{id}/actuaciones")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<ActuacionResponse>>> listarActuaciones(
            @PathVariable Long id,
            Authentication auth
    ) {
        List<ActuacionResponse> lista = actuacionExpedienteService.listarActuaciones(id, auth.getName());
        return ResponseEntity.ok(ApiResponse.of("Listado de actuaciones del expediente.", lista));
    }

    @GetMapping("/expedientes/{id}/seguimiento")
    @PreAuthorize("hasAuthority('MESAPARTES_SEGUIMIENTO_VER') or hasAnyRole('ADMIN', 'SUPERADMIN', 'MESA_PARTES', 'OPERADOR', 'JEFE_OFICINA', 'CIUDADANO')")
    public ResponseEntity<ApiResponse<SeguimientoInternoResponse>> obtenerSeguimientoInterno(
            @PathVariable Long id,
            Authentication auth
    ) {
        SeguimientoInternoResponse response = seguimientoInternoService.obtenerSeguimiento(id, auth.getName());
        return ResponseEntity.ok(ApiResponse.of("Trazabilidad y seguimiento interno del expediente.", response));
    }

    @GetMapping("/derivaciones/estadisticas")
    @PreAuthorize("hasAuthority('MESAPARTES_ESTADISTICAS_OFICINA') or hasAnyRole('ADMIN', 'SUPERADMIN', 'MESA_PARTES', 'OPERADOR', 'JEFE_OFICINA')")
    public ResponseEntity<ApiResponse<EstadisticasDerivacionResponse>> obtenerEstadisticasDerivacion(
            @RequestParam(value = "oficinaId", required = false) Long oficinaId,
            Authentication auth
    ) {
        EstadisticasDerivacionResponse stats = estadisticasDerivacionService.obtenerEstadisticas(oficinaId, auth.getName());
        return ResponseEntity.ok(ApiResponse.of("Estadísticas de derivaciones y carga laboral.", stats));
    }

    @GetMapping("/panel-oficina")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'MESA_PARTES', 'JEFE_OFICINA')")
    public ResponseEntity<ApiResponse<PanelOficinaResponse>> obtenerPanelOficina(
            @RequestParam(value = "oficinaId", required = false) Long oficinaId,
            Authentication auth
    ) {
        PanelOficinaResponse panel = estadisticasDerivacionService.obtenerPanelOficina(oficinaId, auth.getName());
        return ResponseEntity.ok(ApiResponse.of("Panel de supervisión y gestión de la unidad orgánica.", panel));
    }

    /* =========================================================================
     * PROMPT 16: ATENCIÓN, RESOLUCIÓN, NOTIFICACIÓN Y CIERRE DE EXPEDIENTES
     * ========================================================================= */

    @GetMapping("/atencion")
    @PreAuthorize("hasAuthority('MESAPARTES_ATENCION_VER') or hasAnyRole('ADMIN', 'SUPERADMIN', 'MESA_PARTES', 'OPERADOR', 'JEFE_OFICINA')")
    public ResponseEntity<ApiResponse<Page<AtencionItemResponse>>> listarBandejaAtencion(
            @RequestParam(value = "oficinaId", required = false) Long oficinaId,
            @RequestParam(value = "estado", required = false) EstadoExpediente estado,
            @RequestParam(value = "tipoTramiteId", required = false) Long tipoTramiteId,
            @RequestParam(value = "termino", required = false) String termino,
            @PageableDefault(size = 15, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Authentication auth
    ) {
        Page<AtencionItemResponse> page = atencionExpedienteService.listarBandejaAtencion(oficinaId, estado, tipoTramiteId, termino, pageable, auth.getName());
        return ResponseEntity.ok(ApiResponse.of("Bandeja de expedientes en atención obtenida exitosamente.", page));
    }

    @PostMapping("/expedientes/{id}/iniciar-atencion")
    @PreAuthorize("hasAuthority('MESAPARTES_ATENCION_INICIAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'OPERADOR', 'JEFE_OFICINA')")
    public ResponseEntity<ApiResponse<AtencionItemResponse>> iniciarAtencion(
            @PathVariable Long id,
            @RequestBody(required = false) IniciarAtencionRequest request,
            Authentication auth
    ) {
        AtencionItemResponse resp = atencionExpedienteService.iniciarAtencion(id, request, auth.getName());
        return ResponseEntity.ok(ApiResponse.of("Atención de expediente iniciada formalmente.", resp));
    }

    @GetMapping("/expedientes/{id}/detalle-atencion")
    @PreAuthorize("hasAuthority('MESAPARTES_ATENCION_VER') or hasAnyRole('ADMIN', 'SUPERADMIN', 'MESA_PARTES', 'OPERADOR', 'JEFE_OFICINA')")
    public ResponseEntity<ApiResponse<DetalleAtencionResponse>> obtenerDetalleAtencion(
            @PathVariable Long id,
            Authentication auth
    ) {
        DetalleAtencionResponse detalle = atencionExpedienteService.obtenerDetalleAtencion(id, auth.getName());
        return ResponseEntity.ok(ApiResponse.of("Detalle integral de atención del expediente.", detalle));
    }

    @PostMapping("/expedientes/{id}/evaluaciones")
    @PreAuthorize("hasAuthority('MESAPARTES_EVALUACION_CREAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'OPERADOR', 'JEFE_OFICINA')")
    public ResponseEntity<ApiResponse<EvaluacionResponse>> crearEvaluacion(
            @PathVariable Long id,
            @Valid @RequestBody CrearEvaluacionRequest request,
            Authentication auth
    ) {
        EvaluacionResponse resp = evaluacionExpedienteService.crearEvaluacion(id, request, auth.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of("Evaluación administrativa registrada.", resp));
    }

    @GetMapping("/expedientes/{id}/evaluaciones")
    @PreAuthorize("hasAuthority('MESAPARTES_ATENCION_VER') or hasAnyRole('ADMIN', 'SUPERADMIN', 'MESA_PARTES', 'OPERADOR', 'JEFE_OFICINA')")
    public ResponseEntity<ApiResponse<List<EvaluacionResponse>>> listarEvaluaciones(
            @PathVariable Long id,
            Authentication auth
    ) {
        List<EvaluacionResponse> lista = evaluacionExpedienteService.listarEvaluacionesPorExpediente(id, auth.getName());
        return ResponseEntity.ok(ApiResponse.of("Evaluaciones del expediente.", lista));
    }

    @RequestMapping(value = "/evaluaciones/{id}/finalizar", method = {RequestMethod.POST, RequestMethod.PUT})
    @PreAuthorize("hasAuthority('MESAPARTES_EVALUACION_FINALIZAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'OPERADOR', 'JEFE_OFICINA')")
    public ResponseEntity<ApiResponse<EvaluacionResponse>> finalizarEvaluacion(
            @PathVariable Long id,
            Authentication auth
    ) {
        EvaluacionResponse resp = evaluacionExpedienteService.finalizarEvaluacion(id, auth.getName());
        return ResponseEntity.ok(ApiResponse.of("Evaluación técnica finalizada correctamente.", resp));
    }

    @PostMapping("/evaluaciones/{id}/documentos")
    @PreAuthorize("hasAuthority('MESAPARTES_EVALUACION_CREAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'OPERADOR', 'JEFE_OFICINA')")
    public ResponseEntity<ApiResponse<EvaluacionDocumentoResponse>> adjuntarDocumentoEvaluacion(
            @PathVariable Long id,
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam(value = "tipoDocumento", defaultValue = "INFORME_TECNICO") String tipoDocumento,
            Authentication auth
    ) {
        EvaluacionDocumentoResponse resp = evaluacionExpedienteService.adjuntarDocumento(id, archivo, tipoDocumento, auth.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of("Documento adjuntado a la evaluación.", resp));
    }

    @PostMapping("/expedientes/{id}/propuestas")
    @PreAuthorize("hasAuthority('MESAPARTES_PROPUESTA_CREAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'OPERADOR', 'JEFE_OFICINA')")
    public ResponseEntity<ApiResponse<PropuestaResponse>> crearPropuesta(
            @PathVariable Long id,
            @Valid @RequestBody CrearPropuestaRequest request,
            Authentication auth
    ) {
        PropuestaResponse resp = propuestaDecisionService.crearPropuesta(id, request, auth.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of("Propuesta de decisión registrada en borrador.", resp));
    }

    @GetMapping("/expedientes/{id}/propuestas")
    @PreAuthorize("hasAuthority('MESAPARTES_PROPUESTA_REVISAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'OPERADOR', 'JEFE_OFICINA')")
    public ResponseEntity<ApiResponse<List<PropuestaResponse>>> listarPropuestas(
            @PathVariable Long id,
            Authentication auth
    ) {
        List<PropuestaResponse> lista = propuestaDecisionService.listarPropuestasPorExpediente(id, auth.getName());
        return ResponseEntity.ok(ApiResponse.of("Propuestas de decisión del expediente.", lista));
    }

    @GetMapping("/propuestas")
    @PreAuthorize("hasAuthority('MESAPARTES_PROPUESTA_REVISAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA')")
    public ResponseEntity<ApiResponse<Page<PropuestaResponse>>> listarBandejaPropuestas(
            @RequestParam(value = "oficinaId", required = false) Long oficinaId,
            @RequestParam(value = "estado", required = false) EstadoPropuesta estado,
            @PageableDefault(size = 15, sort = "fechaCreacion", direction = Sort.Direction.DESC) Pageable pageable,
            Authentication auth
    ) {
        Page<PropuestaResponse> page = propuestaDecisionService.listarBandejaPropuestas(oficinaId, estado, pageable, auth.getName());
        return ResponseEntity.ok(ApiResponse.of("Bandeja de propuestas para revisión.", page));
    }

    @PostMapping("/propuestas/{id}/enviar-revision")
    @PreAuthorize("hasAuthority('MESAPARTES_PROPUESTA_CREAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'OPERADOR', 'JEFE_OFICINA')")
    public ResponseEntity<ApiResponse<PropuestaResponse>> enviarPropuestaARevision(
            @PathVariable Long id,
            Authentication auth
    ) {
        PropuestaResponse resp = propuestaDecisionService.enviarARevision(id, auth.getName());
        return ResponseEntity.ok(ApiResponse.of("Propuesta elevada a revisión del despacho.", resp));
    }

    @PostMapping({"/propuestas/{id}/revisar", "/propuestas/{id}/aprobar"})
    @PreAuthorize("hasAuthority('MESAPARTES_PROPUESTA_APROBAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA')")
    public ResponseEntity<ApiResponse<PropuestaResponse>> revisarPropuesta(
            @PathVariable Long id,
            @Valid @RequestBody RevisarPropuestaRequest request,
            Authentication auth
    ) {
        PropuestaResponse resp = revisionDecisionService.revisarPropuesta(id, request, auth.getName());
        return ResponseEntity.ok(ApiResponse.of("Revisión de propuesta procesada.", resp));
    }

    @PostMapping("/expedientes/{id}/decisiones")
    @PreAuthorize("hasAuthority('MESAPARTES_DECISION_EMITIR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA')")
    public ResponseEntity<ApiResponse<DecisionResponse>> registrarDecision(
            @PathVariable Long id,
            @Valid @RequestBody RegistrarDecisionRequest request,
            Authentication auth
    ) {
        DecisionResponse resp = decisionExpedienteService.registrarDecision(id, request, auth.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of("Decisión administrativa registrada.", resp));
    }

    @GetMapping("/expedientes/{id}/decisiones")
    @PreAuthorize("hasAuthority('MESAPARTES_ATENCION_VER') or hasAnyRole('ADMIN', 'SUPERADMIN', 'MESA_PARTES', 'OPERADOR', 'JEFE_OFICINA')")
    public ResponseEntity<ApiResponse<List<DecisionResponse>>> listarDecisiones(
            @PathVariable Long id,
            Authentication auth
    ) {
        List<DecisionResponse> lista = decisionExpedienteService.listarDecisionesPorExpediente(id, auth.getName());
        return ResponseEntity.ok(ApiResponse.of("Decisiones administrativas del expediente.", lista));
    }

    @PostMapping("/decisiones/{id}/generar-documento")
    @PreAuthorize("hasAuthority('MESAPARTES_DOCUMENTO_GENERAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA')")
    public ResponseEntity<ApiResponse<DecisionResponse>> generarDocumentoDecision(
            @PathVariable Long id,
            Authentication auth
    ) {
        DecisionResponse resp = decisionExpedienteService.generarDocumentoPdf(id, null, auth.getName());
        return ResponseEntity.ok(ApiResponse.of("Documento PDF oficial compilado con éxito.", resp));
    }

    @PostMapping("/decisiones/{id}/registrar-emision")
    @PreAuthorize("hasAuthority('MESAPARTES_DOCUMENTO_FORMALIZAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA')")
    public ResponseEntity<ApiResponse<DecisionResponse>> registrarEmisionFormal(
            @PathVariable Long id,
            @Valid @RequestBody FormalizarDecisionRequest request,
            Authentication auth
    ) {
        DecisionResponse resp = decisionExpedienteService.registrarFormalizacionEmision(id, request, auth.getName());
        return ResponseEntity.ok(ApiResponse.of("Emisión formal registrada y expediente resuelto.", resp));
    }

    @GetMapping("/decisiones/{id}/descargar")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> descargarDocumentoDecision(
            @PathVariable Long id,
            Authentication auth
    ) {
        byte[] pdfBytes = decisionExpedienteService.descargarDocumentoDecisionPdf(id, auth.getName());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("inline", "Resolucion-" + id + ".pdf");
        headers.setContentLength(pdfBytes.length);
        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }

    @PostMapping("/expedientes/{id}/notificaciones")
    @PreAuthorize("hasAuthority('MESAPARTES_NOTIFICACION_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'MESA_PARTES', 'JEFE_OFICINA')")
    public ResponseEntity<ApiResponse<NotificacionResponse>> registrarNotificacion(
            @PathVariable Long id,
            @Valid @RequestBody RegistrarNotificacionRequest request,
            Authentication auth
    ) {
        NotificacionResponse resp = notificacionResultadoService.registrarNotificacion(id, request, auth.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of("Cédula de notificación registrada.", resp));
    }

    @GetMapping("/expedientes/{id}/notificaciones")
    @PreAuthorize("hasAuthority('MESAPARTES_ATENCION_VER') or hasAnyRole('ADMIN', 'SUPERADMIN', 'MESA_PARTES', 'OPERADOR', 'JEFE_OFICINA')")
    public ResponseEntity<ApiResponse<List<NotificacionResponse>>> listarNotificaciones(
            @PathVariable Long id,
            Authentication auth
    ) {
        List<NotificacionResponse> lista = notificacionResultadoService.listarNotificacionesPorExpediente(id, auth.getName());
        return ResponseEntity.ok(ApiResponse.of("Notificaciones del expediente.", lista));
    }

    @PostMapping("/notificaciones/{id}/intento")
    @PreAuthorize("hasAuthority('MESAPARTES_NOTIFICACION_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'MESA_PARTES')")
    public ResponseEntity<ApiResponse<NotificacionResponse>> registrarIntentoNotificacion(
            @PathVariable Long id,
            @RequestParam(value = "observaciones", required = false) String observaciones,
            Authentication auth
    ) {
        NotificacionResponse resp = notificacionResultadoService.registrarIntentoEnvio(id, observaciones, auth.getName());
        return ResponseEntity.ok(ApiResponse.of("Intento de envío de notificación registrado.", resp));
    }

    @PostMapping("/notificaciones/{id}/acreditar")
    @PreAuthorize("hasAuthority('MESAPARTES_NOTIFICACION_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'MESA_PARTES')")
    public ResponseEntity<ApiResponse<NotificacionResponse>> acreditarNotificacion(
            @PathVariable Long id,
            @RequestParam("numeroAcuseRecibo") String numeroAcuseRecibo,
            @RequestParam(value = "observaciones", required = false) String observaciones,
            @RequestParam(value = "cargoPdf", required = false) MultipartFile cargoPdf,
            Authentication auth
    ) {
        AcreditarNotificacionRequest req = AcreditarNotificacionRequest.builder()
                .numeroAcuseRecibo(numeroAcuseRecibo)
                .observaciones(observaciones)
                .build();
        NotificacionResponse resp = notificacionResultadoService.acreditarNotificacionValida(id, req, cargoPdf, auth.getName());
        return ResponseEntity.ok(ApiResponse.of("Notificación administrativa acreditada legalmente.", resp));
    }

    @PostMapping("/expedientes/{id}/cerrar")
    @PreAuthorize("hasAuthority('MESAPARTES_EXPEDIENTE_CERRAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA')")
    public ResponseEntity<ApiResponse<CierreResponse>> cerrarExpediente(
            @PathVariable Long id,
            @Valid @RequestBody CerrarExpedienteRequest request,
            Authentication auth
    ) {
        CierreResponse resp = cierreExpedienteService.cerrarExpediente(id, request, auth.getName());
        return ResponseEntity.ok(ApiResponse.of("Expediente cerrado administrativamente.", resp));
    }

    @PostMapping("/expedientes/{id}/archivar")
    @PreAuthorize("hasAuthority('MESAPARTES_EXPEDIENTE_ARCHIVAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'MESA_PARTES', 'JEFE_OFICINA')")
    public ResponseEntity<ApiResponse<ArchivoResponse>> archivarExpediente(
            @PathVariable Long id,
            @Valid @RequestBody ArchivarExpedienteRequest request,
            Authentication auth
    ) {
        ArchivoResponse resp = archivoExpedienteService.archivarExpediente(id, request, auth.getName());
        return ResponseEntity.ok(ApiResponse.of("Expediente remitido a custodia de archivo.", resp));
    }

    @PostMapping("/expedientes/{id}/reabrir")
    @PreAuthorize("hasAuthority('MESAPARTES_EXPEDIENTE_REABRIR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA')")
    public ResponseEntity<ApiResponse<ReaperturaResponse>> reabrirExpediente(
            @PathVariable Long id,
            @Valid @RequestBody ReabrirExpedienteRequest request,
            Authentication auth
    ) {
        ReaperturaResponse resp = archivoExpedienteService.reabrirExpediente(id, request, auth.getName());
        return ResponseEntity.ok(ApiResponse.of("Reapertura administrativa efectuada.", resp));
    }

    @GetMapping({"/estadisticas-atencion", "/atencion/estadisticas"})
    @PreAuthorize("hasAuthority('MESAPARTES_ATENCION_VER') or hasAnyRole('ADMIN', 'SUPERADMIN', 'MESA_PARTES', 'JEFE_OFICINA')")
    public ResponseEntity<ApiResponse<EstadisticasAtencionResponse>> obtenerEstadisticasAtencion(
            @RequestParam(value = "oficinaId", required = false) Long oficinaId,
            Authentication auth
    ) {
        EstadisticasAtencionResponse stats = estadisticasAtencionService.obtenerEstadisticasAtencion(oficinaId, auth.getName());
        return ResponseEntity.ok(ApiResponse.of("Estadísticas e indicadores de atención administrativa.", stats));
    }
}

