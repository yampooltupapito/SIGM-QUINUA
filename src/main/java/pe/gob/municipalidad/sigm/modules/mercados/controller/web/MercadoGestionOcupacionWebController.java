package pe.gob.municipalidad.sigm.modules.mercados.controller.web;

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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pe.gob.municipalidad.sigm.modules.mercados.dto.SolicitudGestionOcupacionDetailResponse;
import pe.gob.municipalidad.sigm.modules.mercados.dto.SolicitudGestionOcupacionResponse;
import pe.gob.municipalidad.sigm.modules.mercados.entity.AsignacionPuesto;
import pe.gob.municipalidad.sigm.modules.mercados.entity.Comerciante;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoAsignacion;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoSolicitudGestion;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoSolicitudGestion;
import pe.gob.municipalidad.sigm.modules.mercados.repository.*;
import pe.gob.municipalidad.sigm.modules.mercados.service.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Controller
@RequestMapping
public class MercadoGestionOcupacionWebController {

    private final RenovacionOcupacionService renovacionService;
    private final CambioTitularService cambioTitularService;
    private final TransferenciaPuestoService transferenciaService;
    private final EvaluacionGestionOcupacionService evaluacionService;
    private final ReporteGestionOcupacionService reporteService;
    private final MercadoMunicipalRepository mercadoRepository;
    private final PuestoComercialRepository puestoRepository;
    private final AsignacionPuestoRepository asignacionRepository;
    private final ComercianteRepository comercianteRepository;
    private final ProcedimientoGestionOcupacionRepository procedimientoRepository;

    public MercadoGestionOcupacionWebController(
            RenovacionOcupacionService renovacionService,
            CambioTitularService cambioTitularService,
            TransferenciaPuestoService transferenciaService,
            EvaluacionGestionOcupacionService evaluacionService,
            ReporteGestionOcupacionService reporteService,
            MercadoMunicipalRepository mercadoRepository,
            PuestoComercialRepository puestoRepository,
            AsignacionPuestoRepository asignacionRepository,
            ComercianteRepository comercianteRepository,
            ProcedimientoGestionOcupacionRepository procedimientoRepository) {
        this.renovacionService = renovacionService;
        this.cambioTitularService = cambioTitularService;
        this.transferenciaService = transferenciaService;
        this.evaluacionService = evaluacionService;
        this.reporteService = reporteService;
        this.mercadoRepository = mercadoRepository;
        this.puestoRepository = puestoRepository;
        this.asignacionRepository = asignacionRepository;
        this.comercianteRepository = comercianteRepository;
        this.procedimientoRepository = procedimientoRepository;
    }

    // 1. Dashboard de Gestiones de Ocupación
    @GetMapping("/dashboard/mercados/gestiones-ocupacion/dashboard")
    @PreAuthorize("hasAnyAuthority('MERCADOS_RENOVACION_VER', 'MERCADOS_CAMBIO_TITULAR_VER', 'MERCADOS_TRANSFERENCIA_VER')")
    public String dashboard(Model model) {
        model.addAttribute("stats", reporteService.obtenerEstadisticas());
        model.addAttribute("mercados", mercadoRepository.findAll());
        return "admin/mercados/gestiones-ocupacion/dashboard";
    }

    // 2. Renovaciones - Listado
    @GetMapping("/dashboard/mercados/renovaciones")
    @PreAuthorize("hasAuthority('MERCADOS_RENOVACION_VER')")
    public String listarRenovaciones(
            @RequestParam(required = false) UUID mercadoId,
            @RequestParam(required = false) EstadoSolicitudGestion estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaPresentacion").descending());
        Page<SolicitudGestionOcupacionResponse> renovacionesPage = renovacionService.buscarPaginado(
                mercadoId, null, null, null, estado, fechaDesde, fechaHasta, q, pageable);

        model.addAttribute("renovacionesPage", renovacionesPage);
        model.addAttribute("mercados", mercadoRepository.findAll());
        model.addAttribute("estados", EstadoSolicitudGestion.values());
        model.addAttribute("mercadoId", mercadoId);
        model.addAttribute("estado", estado);
        model.addAttribute("fechaDesde", fechaDesde);
        model.addAttribute("fechaHasta", fechaHasta);
        model.addAttribute("q", q);

        return "admin/mercados/renovaciones/listado";
    }

    // 3. Renovaciones - Formulario
    @GetMapping("/dashboard/mercados/renovaciones/nueva")
    @PreAuthorize("hasAuthority('MERCADOS_RENOVACION_CREAR')")
    public String formularioRenovacion(
            @RequestParam(required = false) UUID asignacionId,
            Model model) {
        model.addAttribute("mercados", mercadoRepository.findAll());
        List<AsignacionPuesto> asignaciones = new java.util.ArrayList<>(asignacionRepository.findByEstadoConRelaciones(EstadoAsignacion.VIGENTE));
        if (asignacionId != null && asignaciones.stream().noneMatch(a -> a.getId().equals(asignacionId))) {
            asignacionRepository.findByIdCompleto(asignacionId).ifPresent(asignaciones::add);
        }
        model.addAttribute("asignaciones", asignaciones);
        model.addAttribute("procedimientos", procedimientoRepository.findByActivoTrueOrderByNombreAsc());
        model.addAttribute("asignacionId", asignacionId);
        return "admin/mercados/renovaciones/formulario";
    }

    // 4. Cambios de Titular - Listado
    @GetMapping("/dashboard/mercados/cambios-titular")
    @PreAuthorize("hasAuthority('MERCADOS_CAMBIO_TITULAR_VER')")
    public String listarCambiosTitular(
            @RequestParam(required = false) UUID mercadoId,
            @RequestParam(required = false) EstadoSolicitudGestion estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaPresentacion").descending());
        Page<SolicitudGestionOcupacionResponse> cambiosPage = cambioTitularService.buscarPaginado(
                mercadoId, null, null, estado, fechaDesde, fechaHasta, q, pageable);

        model.addAttribute("cambiosPage", cambiosPage);
        model.addAttribute("mercados", mercadoRepository.findAll());
        model.addAttribute("estados", EstadoSolicitudGestion.values());
        model.addAttribute("mercadoId", mercadoId);
        model.addAttribute("estado", estado);
        model.addAttribute("fechaDesde", fechaDesde);
        model.addAttribute("fechaHasta", fechaHasta);
        model.addAttribute("q", q);

        return "admin/mercados/cambios-titular/listado";
    }

    // 5. Cambios de Titular - Formulario
    @GetMapping("/dashboard/mercados/cambios-titular/nuevo")
    @PreAuthorize("hasAuthority('MERCADOS_CAMBIO_TITULAR_CREAR')")
    public String formularioCambioTitular(
            @RequestParam(required = false) UUID puestoId,
            Model model) {
        model.addAttribute("mercados", mercadoRepository.findAll());
        model.addAttribute("puestos", puestoRepository.findAll());
        model.addAttribute("asignaciones", asignacionRepository.findByEstadoConRelaciones(EstadoAsignacion.VIGENTE));
        model.addAttribute("comerciantes", comercianteRepository.findAllConRelaciones());
        model.addAttribute("procedimientos", procedimientoRepository.findByTipoSolicitudAndActivoTrue(TipoSolicitudGestion.CAMBIO_TITULAR));
        model.addAttribute("puestoId", puestoId);
        return "admin/mercados/cambios-titular/formulario";
    }

    // 6. Transferencias Autorizadas - Listado
    @GetMapping("/dashboard/mercados/transferencias")
    @PreAuthorize("hasAuthority('MERCADOS_TRANSFERENCIA_VER')")
    public String listarTransferencias(
            @RequestParam(required = false) UUID mercadoId,
            @RequestParam(required = false) EstadoSolicitudGestion estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaPresentacion").descending());
        Page<SolicitudGestionOcupacionResponse> transferenciasPage = transferenciaService.buscarPaginado(
                mercadoId, null, null, estado, fechaDesde, fechaHasta, q, pageable);

        model.addAttribute("transferenciasPage", transferenciasPage);
        model.addAttribute("mercados", mercadoRepository.findAll());
        model.addAttribute("estados", EstadoSolicitudGestion.values());
        model.addAttribute("mercadoId", mercadoId);
        model.addAttribute("estado", estado);
        model.addAttribute("fechaDesde", fechaDesde);
        model.addAttribute("fechaHasta", fechaHasta);
        model.addAttribute("q", q);

        return "admin/mercados/transferencias/listado";
    }

    // 7. Transferencias Autorizadas - Formulario
    @GetMapping("/dashboard/mercados/transferencias/nueva")
    @PreAuthorize("hasAuthority('MERCADOS_TRANSFERENCIA_CREAR')")
    public String formularioTransferencia(Model model) {
        model.addAttribute("mercados", mercadoRepository.findAll());
        model.addAttribute("puestos", puestoRepository.findAll());
        model.addAttribute("asignaciones", asignacionRepository.findByEstado(EstadoAsignacion.VIGENTE));
        model.addAttribute("comerciantes", comercianteRepository.findAll());
        model.addAttribute("procedimientos", procedimientoRepository.findByTipoSolicitudAndActivoTrue(TipoSolicitudGestion.TRANSFERENCIA_AUTORIZADA));
        return "admin/mercados/transferencias/formulario";
    }

    // 8. Detalle Integral de Solicitud de Ocupación
    @GetMapping("/dashboard/mercados/gestiones-ocupacion/{id}")
    @PreAuthorize("hasAnyAuthority('MERCADOS_RENOVACION_VER', 'MERCADOS_CAMBIO_TITULAR_VER', 'MERCADOS_TRANSFERENCIA_VER')")
    public String detalleSolicitud(@PathVariable UUID id, Model model) {
        SolicitudGestionOcupacionDetailResponse detalle = evaluacionService.obtenerDetalle(id);
        model.addAttribute("solicitud", detalle);
        return "admin/mercados/gestiones-ocupacion/detalle";
    }

    // 9. Descarga de Reportes
    @GetMapping("/dashboard/mercados/gestiones-ocupacion/reportes/excel")
    @PreAuthorize("hasAuthority('MERCADOS_GESTION_OCUPACION_EXPORTAR')")
    public ResponseEntity<byte[]> descargarExcel(
            @RequestParam(required = false) TipoSolicitudGestion tipo,
            @RequestParam(required = false) EstadoSolicitudGestion estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {

        byte[] bytes = reporteService.exportarExcel(tipo, estado, desde, hasta);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"reporte_gestiones_ocupacion.xlsx\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }

    @GetMapping("/dashboard/mercados/gestiones-ocupacion/reportes/pdf")
    @PreAuthorize("hasAuthority('MERCADOS_GESTION_OCUPACION_EXPORTAR')")
    public ResponseEntity<byte[]> descargarPdf(
            @RequestParam(required = false) TipoSolicitudGestion tipo,
            @RequestParam(required = false) EstadoSolicitudGestion estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {

        byte[] bytes = reporteService.exportarPdf(tipo, estado, desde, hasta);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"reporte_gestiones_ocupacion.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(bytes);
    }

    // 10. Verificación Pública con QR
    @GetMapping({"/publico/verificar/gestion-ocupacion/{token}", "/verificar/gestion-ocupacion/{token}"})
    public String verificarPublicoQr(@PathVariable String token, Model model) {
        SolicitudGestionOcupacionResponse s = reporteService.verificarTokenQr(token);
        model.addAttribute("solicitud", s);
        return "publico/mercados/verificacion-gestion-ocupacion";
    }
}
