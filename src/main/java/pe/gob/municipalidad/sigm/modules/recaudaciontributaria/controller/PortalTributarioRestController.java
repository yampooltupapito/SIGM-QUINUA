package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.portal.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.*;

import java.util.List;
import java.util.UUID;

/**
 * Endpoints REST del Portal Tributario Ciudadano.
 */
@RestController
@RequestMapping("/api/portal/tributario")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('PORTAL_TRIBUTARIO_ACCEDER') or hasAnyRole('ROLE_CIUDADANO', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_TRIBUTACION')")
public class PortalTributarioRestController {

    private final PortalTributarioContextService contextService;
    private final VinculacionContribuyenteService vinculacionService;
    private final ResumenTributarioCiudadanoService resumenService;
    private final ConsultaPrediosCiudadanoService prediosService;
    private final ConsultaObligacionesCiudadanoService obligacionesService;
    private final ConsultaCuentaCorrienteCiudadanoService cuentaCorrienteService;
    private final ConsultaPagosCiudadanoService pagosService;
    private final ConsultaDocumentosCiudadanoService documentosService;
    private final ConsultaDeudasCiudadanoService deudasService;
    private final ConsultaFraccionamientosCiudadanoService fraccionamientosService;
    private final ConsultaNotificacionesCiudadanoService notificacionesService;

    @GetMapping("/contextos")
    public ResponseEntity<List<PortalTributarioContextoDto>> listarContextos() {
        return ResponseEntity.ok(contextService.listarContextosDisponibles());
    }

    @PostMapping("/cambiar-contexto/{contribuyenteId}")
    public ResponseEntity<Void> cambiarContexto(@PathVariable UUID contribuyenteId) {
        contextService.cambiarContexto(contribuyenteId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/dashboard")
    public ResponseEntity<PortalTributarioDashboardDto> obtenerDashboard() {
        return ResponseEntity.ok(resumenService.obtenerDashboardCiudadano());
    }

    @PostMapping("/vinculaciones/solicitar")
    public ResponseEntity<VinculacionResponse> solicitarVinculacion(@Valid @RequestBody SolicitarVinculacionRequest request) {
        Usuario usuario = contextService.obtenerUsuarioAutenticado();
        return ResponseEntity.ok(vinculacionService.solicitarVinculacion(request, usuario));
    }

    @GetMapping("/predios")
    public ResponseEntity<List<PredioCiudadanoDto>> listarPredios() {
        return ResponseEntity.ok(prediosService.listarMisPredios());
    }

    @GetMapping("/predios/{id}")
    public ResponseEntity<PredioCiudadanoDto> detallePredio(@PathVariable UUID id) {
        return ResponseEntity.ok(prediosService.obtenerDetallePredio(id));
    }

    @GetMapping("/obligaciones")
    public ResponseEntity<Page<ObligacionCiudadanoDto>> listarObligaciones(@PageableDefault(size = 15) Pageable pageable) {
        return ResponseEntity.ok(obligacionesService.listarMisObligaciones(pageable));
    }

    @GetMapping("/obligaciones/{id}")
    public ResponseEntity<ObligacionCiudadanoDto> detalleObligacion(@PathVariable UUID id) {
        return ResponseEntity.ok(obligacionesService.obtenerDetalleObligacion(id));
    }

    @GetMapping("/cuenta-corriente/movimientos")
    public ResponseEntity<Page<MovimientoCuentaCiudadanoDto>> listarMovimientos(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(cuentaCorrienteService.listarMisMovimientos(pageable));
    }

    @GetMapping("/pagos")
    public ResponseEntity<Page<PagoCiudadanoDto>> listarPagos(@PageableDefault(size = 15) Pageable pageable) {
        return ResponseEntity.ok(pagosService.listarMisPagos(pageable));
    }

    @GetMapping("/pagos/{id}")
    public ResponseEntity<PagoCiudadanoDto> detallePago(@PathVariable UUID id) {
        return ResponseEntity.ok(pagosService.obtenerDetallePago(id));
    }

    @GetMapping("/recibos")
    public ResponseEntity<List<ReciboCiudadanoDto>> listarRecibos() {
        return ResponseEntity.ok(documentosService.listarMisRecibos());
    }

    @GetMapping("/deudas")
    public ResponseEntity<DeudaCiudadanoDto> resumenDeudas() {
        return ResponseEntity.ok(deudasService.obtenerResumenDeudas());
    }

    @GetMapping("/fraccionamientos")
    public ResponseEntity<Page<FraccionamientoCiudadanoDto>> listarFraccionamientos(@PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(fraccionamientosService.listarMisFraccionamientos(pageable));
    }

    @GetMapping("/notificaciones")
    public ResponseEntity<List<NotificacionCiudadanoDto>> listarNotificaciones() {
        return ResponseEntity.ok(notificacionesService.listarMisNotificaciones());
    }
}

