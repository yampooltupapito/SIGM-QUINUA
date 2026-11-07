package pe.gob.municipalidad.sigm.modules.mesapartes.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.web.bind.annotation.*;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.OficinaRepository;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvAdminDtos.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvDtos.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.CanalIngreso;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoExpediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.PrioridadExpediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Controlador Web (Thymeleaf) para la gestión institucional de Mesa de Partes (Prompt 14).
 * Provee la bandeja de entrada de expedientes, estadísticas reales, revisión de requisitos y observaciones.
 */
@Slf4j
@Controller
@RequestMapping("/dashboard/mesa-partes")
public class MesaPartesAdminWebController {

    private final BandejaMesaPartesService bandejaService;
    private final ExpedienteService expedienteService;
    private final TipoTramiteService tipoTramiteService;
    private final RevisionRequisitosService revisionRequisitosService;
    private final ObservacionExpedienteService observacionService;
    private final PrioridadExpedienteService prioridadService;
    private final ConstanciaRecepcionService constanciaRecepcionService;
    private final OficinaRepository oficinaRepository;
    private final UsuarioRepository usuarioRepository;

    private final DerivacionExpedienteService derivacionService;
    private final AsignacionExpedienteService asignacionService;
    private final ActuacionExpedienteService actuacionService;
    private final SeguimientoInternoService seguimientoService;
    private final EstadisticasDerivacionService estadisticasDerivacionService;

    private final AtencionExpedienteService atencionService;
    private final PropuestaDecisionService propuestaService;
    private final DecisionExpedienteService decisionService;
    private final NotificacionResultadoService notificacionResultadoService;
    private final EstadisticasAtencionService estadisticasAtencionService;
    private final ReporteMesaPartesService reporteService;
    private final IndicadoresMesaPartesService indicadoresService;
    private final EstadisticasExpedienteService estadisticasExpedienteService;

    @Value("${sigm.app.base-url:http://localhost:8080}")
    private String baseUrl;

    public MesaPartesAdminWebController(BandejaMesaPartesService bandejaService,
                                        ExpedienteService expedienteService,
                                        TipoTramiteService tipoTramiteService,
                                        RevisionRequisitosService revisionRequisitosService,
                                        ObservacionExpedienteService observacionService,
                                        PrioridadExpedienteService prioridadService,
                                        ConstanciaRecepcionService constanciaRecepcionService,
                                        OficinaRepository oficinaRepository,
                                        UsuarioRepository usuarioRepository,
                                        DerivacionExpedienteService derivacionService,
                                        AsignacionExpedienteService asignacionService,
                                        ActuacionExpedienteService actuacionService,
                                        SeguimientoInternoService seguimientoService,
                                        EstadisticasDerivacionService estadisticasDerivacionService,
                                        AtencionExpedienteService atencionService,
                                        PropuestaDecisionService propuestaService,
                                        DecisionExpedienteService decisionService,
                                        NotificacionResultadoService notificacionResultadoService,
                                        EstadisticasAtencionService estadisticasAtencionService,
                                        ReporteMesaPartesService reporteService,
                                        IndicadoresMesaPartesService indicadoresService,
                                        EstadisticasExpedienteService estadisticasExpedienteService) {
        this.bandejaService = bandejaService;
        this.expedienteService = expedienteService;
        this.tipoTramiteService = tipoTramiteService;
        this.revisionRequisitosService = revisionRequisitosService;
        this.observacionService = observacionService;
        this.prioridadService = prioridadService;
        this.constanciaRecepcionService = constanciaRecepcionService;
        this.oficinaRepository = oficinaRepository;
        this.usuarioRepository = usuarioRepository;
        this.derivacionService = derivacionService;
        this.asignacionService = asignacionService;
        this.actuacionService = actuacionService;
        this.seguimientoService = seguimientoService;
        this.estadisticasDerivacionService = estadisticasDerivacionService;
        this.atencionService = atencionService;
        this.propuestaService = propuestaService;
        this.decisionService = decisionService;
        this.notificacionResultadoService = notificacionResultadoService;
        this.estadisticasAtencionService = estadisticasAtencionService;
        this.reporteService = reporteService;
        this.indicadoresService = indicadoresService;
        this.estadisticasExpedienteService = estadisticasExpedienteService;
    }

    /**
     * Bandeja Institucional de Mesa de Partes.
     */
    @GetMapping("/bandeja")
    @PreAuthorize("hasAuthority('MESAPARTES_EXPEDIENTE_VER_OFICINA') or hasAuthority('MESAPARTES_EXPEDIENTE_VER_TODOS') or hasAnyRole('ADMIN', 'SUPERADMIN', 'MESA_PARTES', 'OPERADOR', 'JEFE_OFICINA')")
    public String bandeja(
            @RequestParam(value = "estado", required = false) EstadoExpediente estado,
            @RequestParam(value = "oficinaId", required = false) Long oficinaId,
            @RequestParam(value = "tipoTramiteId", required = false) Long tipoTramiteId,
            @RequestParam(value = "prioridad", required = false) PrioridadExpediente prioridad,
            @RequestParam(value = "canalIngreso", required = false) CanalIngreso canalIngreso,
            @RequestParam(value = "fechaDesde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(value = "fechaHasta", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "15") int size,
            Model model,
            Authentication auth
    ) {
        Pageable pageable = PageRequest.of(Math.max(0, page), size, Sort.by("createdAt").descending());
        Page<ExpedienteResponse> expedientes = bandejaService.listarBandeja(
                estado, oficinaId, tipoTramiteId, prioridad, canalIngreso, fechaDesde, fechaHasta, q, pageable, auth.getName()
        );

        BandejaEstadisticasAdminResponse stats = bandejaService.obtenerEstadisticasBandeja(auth.getName());
        List<TipoTramiteResponse> tramites = tipoTramiteService.listarActivos();
        List<Oficina> oficinas = oficinaRepository.findByEstado("ACTIVO");

        model.addAttribute("expedientes", expedientes);
        model.addAttribute("stats", stats);
        model.addAttribute("tramites", tramites);
        model.addAttribute("oficinas", oficinas);
        model.addAttribute("estados", EstadoExpediente.values());
        model.addAttribute("prioridades", PrioridadExpediente.values());
        model.addAttribute("canales", CanalIngreso.values());

        // Parámetros seleccionados
        model.addAttribute("estadoSel", estado);
        model.addAttribute("oficinaIdSel", oficinaId);
        model.addAttribute("tipoTramiteIdSel", tipoTramiteId);
        model.addAttribute("prioridadSel", prioridad);
        model.addAttribute("canalSel", canalIngreso);
        model.addAttribute("fechaDesdeSel", fechaDesde);
        model.addAttribute("fechaHastaSel", fechaHasta);
        model.addAttribute("qSel", q);

        model.addAttribute("moduloActivo", "mesa-partes");
        return "admin/mesa-partes/bandeja";
    }

    /**
     * Detalle Institucional y Centro de Revisión de Expediente.
     */
    @GetMapping("/expedientes/{id}")
    @PreAuthorize("hasAuthority('MESAPARTES_EXPEDIENTE_VER_OFICINA') or hasAuthority('MESAPARTES_EXPEDIENTE_VER_TODOS') or hasAnyRole('ADMIN', 'SUPERADMIN', 'MESA_PARTES', 'OPERADOR', 'JEFE_OFICINA')")
    public String detalleExpediente(
            @PathVariable Long id,
            @RequestParam(value = "tab", defaultValue = "general") String tab,
            Model model,
            Authentication auth
    ) {
        ExpedienteDetalleResponse detalle = expedienteService.obtenerDetalle(id, auth.getName());
        List<RevisionRequisitoResponse> requisitos = revisionRequisitosService.obtenerOInicializarRequisitos(id, auth.getName());
        List<ObservacionResponse> observaciones = observacionService.listarObservaciones(id, auth.getName());
        List<CambioPrioridadResponse> cambiosPrioridad = prioridadService.listarHistorialPrioridades(id, auth.getName());
        List<Oficina> oficinas = oficinaRepository.findByEstado("ACTIVO");

        model.addAttribute("expediente", detalle.getExpediente());
        model.addAttribute("documentos", detalle.getDocumentos());
        model.addAttribute("historial", detalle.getHistorial());
        model.addAttribute("derivaciones", detalle.getDerivaciones());
        model.addAttribute("requisitos", requisitos);
        model.addAttribute("observaciones", observaciones);
        model.addAttribute("cambiosPrioridad", cambiosPrioridad);
        model.addAttribute("oficinas", oficinas);
        model.addAttribute("tabActiva", tab);
        model.addAttribute("prioridades", PrioridadExpediente.values());
        model.addAttribute("moduloActivo", "mesa-partes");

        return "admin/mesa-partes/detalle";
    }

    /**
     * Bandeja de Derivaciones Emitidas / General.
     */
    @GetMapping("/derivaciones")
    @PreAuthorize("hasAuthority('MESAPARTES_DERIVACION_VER') or hasAnyRole('ADMIN', 'SUPERADMIN', 'MESA_PARTES', 'OPERADOR', 'JEFE_OFICINA')")
    public String bandejaDerivaciones(
            @RequestParam(value = "oficinaId", required = false) Long oficinaId,
            @RequestParam(value = "estado", required = false) pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoDerivacion estado,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "15") int size,
            Model model,
            Authentication auth
    ) {
        Pageable pageable = PageRequest.of(Math.max(0, page), size, Sort.by("fechaDerivacion").descending());
        var derivaciones = derivacionService.listarDerivaciones(oficinaId, estado, pageable, auth.getName());
        var stats = estadisticasDerivacionService.obtenerEstadisticas(oficinaId, auth.getName());
        List<Oficina> oficinas = oficinaRepository.findByEstado("ACTIVO");

        model.addAttribute("derivaciones", derivaciones);
        model.addAttribute("stats", stats);
        model.addAttribute("oficinas", oficinas);
        model.addAttribute("estados", pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoDerivacion.values());
        model.addAttribute("estadoSel", estado);
        model.addAttribute("oficinaIdSel", oficinaId);
        model.addAttribute("moduloActivo", "mesa-partes");

        return "admin/mesa-partes/derivaciones";
    }

    /**
     * Bandeja de Derivaciones Recibidas en Despacho.
     */
    @GetMapping("/derivaciones/recibidas")
    @PreAuthorize("hasAuthority('MESAPARTES_DERIVACION_VER') or hasAnyRole('ADMIN', 'SUPERADMIN', 'MESA_PARTES', 'OPERADOR', 'JEFE_OFICINA')")
    public String bandejaDerivacionesRecibidas(
            @RequestParam(value = "estado", required = false) pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoDerivacion estado,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "15") int size,
            Model model,
            Authentication auth
    ) {
        Pageable pageable = PageRequest.of(Math.max(0, page), size, Sort.by("fechaDerivacion").descending());
        var derivaciones = derivacionService.listarDerivacionesRecibidas(estado, pageable, auth.getName());
        var stats = estadisticasDerivacionService.obtenerEstadisticas(null, auth.getName());

        model.addAttribute("derivaciones", derivaciones);
        model.addAttribute("stats", stats);
        model.addAttribute("estados", pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoDerivacion.values());
        model.addAttribute("estadoSel", estado);
        model.addAttribute("moduloActivo", "mesa-partes");

        return "admin/mesa-partes/derivaciones-recibidas";
    }

    /**
     * Bandeja de Mis Expedientes Asignados.
     */
    @GetMapping("/mis-expedientes")
    @PreAuthorize("isAuthenticated()")
    public String misExpedientes(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "15") int size,
            Model model,
            Authentication auth
    ) {
        Pageable pageable = PageRequest.of(Math.max(0, page), size, Sort.by("createdAt").descending());
        var expedientes = asignacionService.listarMisExpedientes(pageable, auth.getName());

        model.addAttribute("expedientes", expedientes);
        model.addAttribute("moduloActivo", "mesa-partes");

        return "admin/mesa-partes/mis-expedientes";
    }

    /**
     * Vista de Seguimiento Interno, Recorrido y Actuaciones.
     */
    @GetMapping("/expedientes/{id}/seguimiento")
    @PreAuthorize("hasAuthority('MESAPARTES_SEGUIMIENTO_VER') or hasAnyRole('ADMIN', 'SUPERADMIN', 'MESA_PARTES', 'OPERADOR', 'JEFE_OFICINA')")
    public String seguimientoExpediente(
            @PathVariable Long id,
            Model model,
            Authentication auth
    ) {
        var seguimiento = seguimientoService.obtenerSeguimiento(id, auth.getName());
        List<Oficina> oficinas = oficinaRepository.findByEstado("ACTIVO");

        model.addAttribute("seguimiento", seguimiento);
        model.addAttribute("expediente", seguimiento.getExpediente());
        model.addAttribute("recorrido", seguimiento.getRecorrido());
        model.addAttribute("plazo", seguimiento.getPlazo());
        model.addAttribute("timeline", seguimiento.getTimeline());
        model.addAttribute("actuaciones", seguimiento.getActuaciones());
        model.addAttribute("oficinas", oficinas);
        model.addAttribute("tiposActuacion", pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.TipoActuacionExpediente.values());
        model.addAttribute("moduloActivo", "mesa-partes");

        return "admin/mesa-partes/seguimiento";
    }

    /**
     * Panel del Jefe de Unidad Orgánica.
     */
    @GetMapping("/panel-oficina")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'MESA_PARTES', 'JEFE_OFICINA')")
    public String panelOficina(
            @RequestParam(value = "oficinaId", required = false) Long oficinaId,
            Model model,
            Authentication auth
    ) {
        var panel = estadisticasDerivacionService.obtenerPanelOficina(oficinaId, auth.getName());
        List<Oficina> oficinas = oficinaRepository.findByEstado("ACTIVO");

        model.addAttribute("panel", panel);
        model.addAttribute("oficinas", oficinas);
        model.addAttribute("oficinaIdSel", oficinaId);
        model.addAttribute("moduloActivo", "mesa-partes");

        return "admin/mesa-partes/panel-oficina";
    }

    /**
     * Descarga de Constancia Oficial de Recepción Administrativa en PDF.
     */
    @GetMapping("/expedientes/{id}/constancia-recepcion")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> descargarConstanciaRecepcion(
            @PathVariable Long id,
            @RequestParam(value = "download", defaultValue = "false") boolean download,
            Authentication auth
    ) {
        byte[] pdfBytes = constanciaRecepcionService.generarConstanciaRecepcionPdf(id, baseUrl, auth.getName());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        String filename = "Constancia-Recepcion-" + id + ".pdf";
        headers.setContentDisposition(download
                ? org.springframework.http.ContentDisposition.attachment().filename(filename).build()
                : org.springframework.http.ContentDisposition.inline().filename(filename).build());
        headers.setContentLength(pdfBytes.length);
        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }

    /**
     * Bandeja de Atención de Expedientes Asignados.
     */
    @GetMapping("/atencion")
    @PreAuthorize("hasAuthority('MESAPARTES_ATENCION_VER') or hasAnyRole('ADMIN', 'SUPERADMIN', 'MESA_PARTES', 'OPERADOR', 'JEFE_OFICINA')")
    public String bandejaAtencion(
            @RequestParam(value = "oficinaId", required = false) Long oficinaId,
            @RequestParam(value = "estado", required = false) EstadoExpediente estado,
            @RequestParam(value = "tipoTramiteId", required = false) Long tipoTramiteId,
            @RequestParam(value = "termino", required = false) String termino,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "15") int size,
            Model model,
            Authentication auth
    ) {
        Pageable pageable = PageRequest.of(Math.max(0, page), size, Sort.by("createdAt").descending());
        var expedientes = atencionService.listarBandejaAtencion(oficinaId, estado, tipoTramiteId, termino, pageable, auth.getName());
        var stats = estadisticasAtencionService.obtenerEstadisticasAtencion(oficinaId, auth.getName());
        List<Oficina> oficinas = oficinaRepository.findByEstado("ACTIVO");
        var tiposTramite = tipoTramiteService.listarActivos();

        model.addAttribute("expedientes", expedientes);
        model.addAttribute("stats", stats);
        model.addAttribute("oficinas", oficinas);
        model.addAttribute("tiposTramite", tiposTramite);
        model.addAttribute("estados", EstadoExpediente.values());
        model.addAttribute("estadoSel", estado);
        model.addAttribute("oficinaSel", oficinaId);
        model.addAttribute("tipoTramiteSel", tipoTramiteId);
        model.addAttribute("terminoSel", termino);
        model.addAttribute("moduloActivo", "mesa-partes");

        return "admin/mesa-partes/atencion";
    }

    /**
     * Detalle y Gestión de Atención del Expediente.
     */
    @GetMapping("/atencion/{id}")
    @PreAuthorize("hasAuthority('MESAPARTES_ATENCION_VER') or hasAnyRole('ADMIN', 'SUPERADMIN', 'MESA_PARTES', 'OPERADOR', 'JEFE_OFICINA')")
    public String detalleAtencion(
            @PathVariable Long id,
            Model model,
            Authentication auth
    ) {
        var detalle = atencionService.obtenerDetalleAtencion(id, auth.getName());
        List<Oficina> oficinas = oficinaRepository.findByEstado("ACTIVO");

        model.addAttribute("detalle", detalle);
        model.addAttribute("oficinas", oficinas);
        model.addAttribute("tiposEvaluacion", pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.TipoEvaluacionExpediente.values());
        model.addAttribute("tiposDecision", pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.TipoDecisionExpediente.values());
        model.addAttribute("resultadosDecision", pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.ResultadoDecision.values());
        model.addAttribute("mediosNotificacion", pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.MedioNotificacion.values());
        model.addAttribute("tiposCierre", pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.TipoCierreExpediente.values());
        model.addAttribute("moduloActivo", "mesa-partes");

        return "admin/mesa-partes/detalle-atencion";
    }

    /**
     * Bandeja de Revisión de Propuestas de Decisión.
     */
    @GetMapping("/propuestas")
    @PreAuthorize("hasAuthority('MESAPARTES_PROPUESTA_REVISAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA')")
    public String bandejaPropuestas(
            @RequestParam(value = "oficinaId", required = false) Long oficinaId,
            @RequestParam(value = "estado", required = false) pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoPropuesta estado,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "15") int size,
            Model model,
            Authentication auth
    ) {
        Pageable pageable = PageRequest.of(Math.max(0, page), size, Sort.by("fechaCreacion").descending());
        var propuestas = propuestaService.listarBandejaPropuestas(oficinaId, estado, pageable, auth.getName());
        var stats = estadisticasAtencionService.obtenerEstadisticasAtencion(oficinaId, auth.getName());
        List<Oficina> oficinas = oficinaRepository.findByEstado("ACTIVO");

        model.addAttribute("propuestas", propuestas);
        model.addAttribute("stats", stats);
        model.addAttribute("oficinas", oficinas);
        model.addAttribute("estados", pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoPropuesta.values());
        model.addAttribute("estadoSel", estado);
        model.addAttribute("oficinaSel", oficinaId);
        model.addAttribute("moduloActivo", "mesa-partes");

        return "admin/mesa-partes/propuestas";
    }

    /**
     * Descarga de Documento Oficial de Decisión Administrativa en PDF.
     */
    @GetMapping("/decisiones/{id}/descargar-pdf")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> descargarDecisionPdf(
            @PathVariable Long id,
            @RequestParam(value = "download", defaultValue = "false") boolean download,
            Authentication auth
    ) {
        byte[] pdfBytes = decisionService.descargarDocumentoDecisionPdf(id, auth.getName());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        String filename = "Resolucion-" + id + ".pdf";
        headers.setContentDisposition(download
                ? org.springframework.http.ContentDisposition.attachment().filename(filename).build()
                : org.springframework.http.ContentDisposition.inline().filename(filename).build());
        headers.setContentLength(pdfBytes.length);
        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }

    /**
     * Panel Principal de Reportes y Estadísticas de Mesa de Partes (Prompt 18).
     */
    @GetMapping("/reportes")
    @PreAuthorize("hasAuthority('MESAPARTES_REPORTES_VER') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA', 'MESA_PARTES', 'MESA_PARTES_OPERADOR')")
    public String reportes(
            @ModelAttribute pe.gob.municipalidad.sigm.modules.mesapartes.dto.ReporteDtos.FiltroReporteRequest filtro,
            Model model,
            Authentication auth
    ) {
        var resumen = reporteService.obtenerResumen(filtro, auth.getName());
        var oficinas = oficinaRepository.findByEstado("ACTIVO");
        var tiposTramite = tipoTramiteService.listarActivos();

        model.addAttribute("resumen", resumen);
        model.addAttribute("oficinas", oficinas);
        model.addAttribute("tiposTramite", tiposTramite);
        model.addAttribute("estados", EstadoExpediente.values());
        model.addAttribute("prioridades", PrioridadExpediente.values());
        model.addAttribute("canales", CanalIngreso.values());
        model.addAttribute("filtro", filtro);
        model.addAttribute("moduloActivo", "mesa-partes");
        model.addAttribute("submenuActivo", "reportes");

        return "admin/mesa-partes/reportes";
    }

    /**
     * Vista de Estadísticas e Indicadores Gráficos.
     */
    @GetMapping("/estadisticas")
    @PreAuthorize("hasAuthority('MESAPARTES_ESTADISTICAS_VER') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA', 'MESA_PARTES', 'MESA_PARTES_OPERADOR')")
    public String estadisticas(Model model, Authentication auth) {
        return "redirect:/dashboard/mesa-partes/reportes#seccion-graficos";
    }

    /**
     * Vista de Supervisión Municipal de Tiempos y Carga.
     */
    @GetMapping("/supervision")
    @PreAuthorize("hasAuthority('MESAPARTES_SUPERVISION_VER') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA')")
    public String supervision(Model model, Authentication auth) {
        return "redirect:/dashboard/mesa-partes/reportes#seccion-supervision";
    }
}

