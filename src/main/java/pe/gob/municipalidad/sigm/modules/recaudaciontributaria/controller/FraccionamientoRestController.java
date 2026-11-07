package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.fraccionamiento.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoConvenioFraccionamiento;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoSolicitudFraccionamiento;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.ConvenioFraccionamientoSpecification;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.SolicitudFraccionamientoSpecification;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.AplicacionPagoFraccionamientoService;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.ConvenioFraccionamientoService;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.SimulacionFraccionamientoService;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.SolicitudFraccionamientoService;
import pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/tributaria/fraccionamientos")
@RequiredArgsConstructor
public class FraccionamientoRestController {

    private final SolicitudFraccionamientoService solicitudService;
    private final ConvenioFraccionamientoService convenioService;
    private final SimulacionFraccionamientoService simulacionService;
    private final AplicacionPagoFraccionamientoService aplicacionPagoService;
    private final UsuarioRepository usuarioRepository;

    @GetMapping("/dashboard")
    @PreAuthorize("hasAuthority('TRIBUTARIA_FRACCIONAMIENTOS_VER') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_TRIBUTACION', 'ROLE_CAJERO')")
    public ResponseEntity<DashboardFraccionamientoResponse> obtenerDashboard() {
        return ResponseEntity.ok(solicitudService.obtenerDashboardMetrics());
    }

    @PostMapping("/simulaciones")
    @PreAuthorize("hasAuthority('TRIBUTARIA_FRACCIONAMIENTOS_SIMULAR') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_TRIBUTACION', 'ROLE_CAJERO')")
    public ResponseEntity<SimulacionFraccionamientoResponse> simular(@Valid @RequestBody SimulacionFraccionamientoRequest request) {
        return ResponseEntity.ok(simulacionService.simular(request));
    }

    @GetMapping("/solicitudes")
    @PreAuthorize("hasAuthority('TRIBUTARIA_FRACCIONAMIENTOS_VER') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_TRIBUTACION', 'ROLE_CAJERO')")
    public ResponseEntity<Page<SolicitudFraccionamientoResponse>> listarSolicitudes(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UUID contribuyenteId,
            @RequestParam(required = false) UUID programaId,
            @RequestParam(required = false) EstadoSolicitudFraccionamiento estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Specification<pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.SolicitudFraccionamientoTributario> spec =
                SolicitudFraccionamientoSpecification.conFiltros(search, contribuyenteId, programaId, estado, fechaDesde, fechaHasta);

        return ResponseEntity.ok(solicitudService.listarSolicitudes(spec, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))));
    }

    @PostMapping("/solicitudes")
    @PreAuthorize("hasAuthority('TRIBUTARIA_FRACCIONAMIENTOS_SOLICITAR') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_TRIBUTACION', 'ROLE_CAJERO')")
    public ResponseEntity<SolicitudFraccionamientoResponse> crearSolicitud(
            @Valid @RequestBody SolicitudFraccionamientoRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Usuario usuario = obtenerUsuario(userDetails);
        return ResponseEntity.ok(solicitudService.crearSolicitud(request, usuario));
    }

    @GetMapping("/solicitudes/{id}")
    @PreAuthorize("hasAuthority('TRIBUTARIA_FRACCIONAMIENTOS_VER') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_TRIBUTACION', 'ROLE_CAJERO')")
    public ResponseEntity<SolicitudFraccionamientoResponse> obtenerSolicitud(@PathVariable UUID id) {
        return ResponseEntity.ok(solicitudService.obtenerPorId(id));
    }

    @GetMapping("/convenios")
    @PreAuthorize("hasAuthority('TRIBUTARIA_FRACCIONAMIENTOS_VER') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_TRIBUTACION', 'ROLE_CAJERO')")
    public ResponseEntity<Page<ConvenioFraccionamientoResponse>> listarConvenios(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UUID contribuyenteId,
            @RequestParam(required = false) UUID programaId,
            @RequestParam(required = false) EstadoConvenioFraccionamiento estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Specification<pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ConvenioFraccionamientoTributario> spec =
                ConvenioFraccionamientoSpecification.conFiltros(search, contribuyenteId, programaId, estado, fechaDesde, fechaHasta);

        return ResponseEntity.ok(convenioService.listarConvenios(spec, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "fechaAprobacion"))));
    }

    @GetMapping("/convenios/{id}")
    @PreAuthorize("hasAuthority('TRIBUTARIA_FRACCIONAMIENTOS_VER') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_TRIBUTACION', 'ROLE_CAJERO')")
    public ResponseEntity<ConvenioFraccionamientoResponse> obtenerConvenio(@PathVariable UUID id) {
        return ResponseEntity.ok(convenioService.obtenerPorId(id));
    }

    @PostMapping("/cuotas/{cuotaId}/aplicar-pago")
    @PreAuthorize("hasAuthority('TRIBUTARIA_FRACCIONAMIENTOS_GESTIONAR_CONVENIO') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_TRIBUTACION', 'ROLE_CAJERO')")
    public ResponseEntity<Map<String, String>> aplicarPago(
            @PathVariable UUID cuotaId,
            @RequestBody Map<String, BigDecimal> body,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        BigDecimal importe = body.get("importe");
        Usuario usuario = obtenerUsuario(userDetails);
        aplicacionPagoService.aplicarPagoACuota(cuotaId, importe, usuario);

        return ResponseEntity.ok(Map.of("message", "Pago aplicado exitosamente a la cuota"));
    }

    private Usuario obtenerUsuario(CustomUserDetails userDetails) {
        String username = userDetails != null ? userDetails.getUsername() : "admin";
        return usuarioRepository.findByUsername(username).orElse(null);
    }
}
