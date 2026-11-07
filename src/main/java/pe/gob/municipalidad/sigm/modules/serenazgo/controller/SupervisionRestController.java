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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.supervision.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.SupervisionSerenazgoService;
import pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/serenazgo/supervision")
@RequiredArgsConstructor
public class SupervisionRestController {

    private final SupervisionSerenazgoService supervisionService;

    @GetMapping("/indicadores")
    @PreAuthorize("hasAuthority('SERENAZGO_SUPERVISION_VER_INDICADORES') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA', 'SUPERVISOR_SERENAZGO')")
    public ResponseEntity<IndicadoresSupervisionResponse> obtenerIndicadores(
            FiltroSupervisionRequest filtro,
            @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(supervisionService.obtenerIndicadores(filtro, user));
    }

    @GetMapping("/pendientes")
    @PreAuthorize("hasAuthority('SERENAZGO_SUPERVISION_VER_PENDIENTES') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA', 'SUPERVISOR_SERENAZGO')")
    public ResponseEntity<List<BandejaPendienteItemDto>> obtenerPendientes(
            FiltroSupervisionRequest filtro,
            @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(supervisionService.obtenerBandejaPendientes(filtro, user));
    }

    @GetMapping("/graficos")
    @PreAuthorize("hasAuthority('SERENAZGO_SUPERVISION_VER_INDICADORES') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA', 'SUPERVISOR_SERENAZGO')")
    public ResponseEntity<GraficosSupervisionDto> obtenerGraficos(
            FiltroSupervisionRequest filtro,
            @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(supervisionService.obtenerGraficos(filtro, user));
    }

    @GetMapping("/seguimientos")
    @PreAuthorize("hasAuthority('SERENAZGO_SUPERVISION_VER') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA', 'SUPERVISOR_SERENAZGO')")
    public ResponseEntity<Page<SeguimientoResponse>> listarSeguimientos(
            FiltroSupervisionRequest filtro,
            @PageableDefault(size = 15, sort = "fechaCreacion") Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(supervisionService.listarSeguimientos(filtro, pageable, user));
    }

    @GetMapping("/seguimientos/{id}")
    @PreAuthorize("hasAuthority('SERENAZGO_SUPERVISION_VER') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA', 'SUPERVISOR_SERENAZGO')")
    public ResponseEntity<SeguimientoDetalleResponse> obtenerDetalle(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(supervisionService.obtenerDetalleSeguimiento(id, user));
    }

    @PostMapping("/seguimientos")
    @PreAuthorize("hasAuthority('SERENAZGO_SUPERVISION_CREAR_SEGUIMIENTO') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA', 'SUPERVISOR_SERENAZGO')")
    public ResponseEntity<SeguimientoResponse> crearSeguimiento(
            @Valid @RequestBody CrearSeguimientoRequest req,
            @AuthenticationPrincipal CustomUserDetails user,
            HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        return ResponseEntity.status(HttpStatus.CREATED).body(supervisionService.crearSeguimiento(req, user, ip));
    }

    @PostMapping("/seguimientos/{id}/asignar")
    @PreAuthorize("hasAuthority('SERENAZGO_SUPERVISION_ASIGNAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA', 'SUPERVISOR_SERENAZGO')")
    public ResponseEntity<SeguimientoResponse> asignarResponsable(
            @PathVariable UUID id,
            @Valid @RequestBody AsignarResponsableRequest req,
            @AuthenticationPrincipal CustomUserDetails user,
            HttpServletRequest request) {
        return ResponseEntity.ok(supervisionService.asignarResponsable(id, req, user, request.getRemoteAddr()));
    }

    @PostMapping("/seguimientos/{id}/observar")
    @PreAuthorize("hasAuthority('SERENAZGO_SUPERVISION_OBSERVAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA', 'SUPERVISOR_SERENAZGO')")
    public ResponseEntity<SeguimientoResponse> registrarObservacion(
            @PathVariable UUID id,
            @Valid @RequestBody RegistrarObservacionRequest req,
            @AuthenticationPrincipal CustomUserDetails user,
            HttpServletRequest request) {
        return ResponseEntity.ok(supervisionService.registrarObservacion(id, req, user, request.getRemoteAddr()));
    }

    @PostMapping("/seguimientos/{id}/subsanar")
    @PreAuthorize("hasAuthority('SERENAZGO_SUPERVISION_SUBSANAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA', 'SUPERVISOR_SERENAZGO', 'SERENO')")
    public ResponseEntity<SeguimientoResponse> registrarSubsanacion(
            @PathVariable UUID id,
            @Valid @RequestBody RegistrarSubsanacionRequest req,
            @AuthenticationPrincipal CustomUserDetails user,
            HttpServletRequest request) {
        return ResponseEntity.ok(supervisionService.registrarSubsanacion(id, req, user, request.getRemoteAddr()));
    }

    @PostMapping("/seguimientos/{id}/revisar")
    @PreAuthorize("hasAuthority('SERENAZGO_SUPERVISION_REVISAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA', 'SUPERVISOR_SERENAZGO')")
    public ResponseEntity<SeguimientoResponse> revisarRespuesta(
            @PathVariable UUID id,
            @Valid @RequestBody RevisarRespuestaRequest req,
            @AuthenticationPrincipal CustomUserDetails user,
            HttpServletRequest request) {
        return ResponseEntity.ok(supervisionService.revisarRespuesta(id, req, user, request.getRemoteAddr()));
    }

    @PostMapping("/seguimientos/{id}/escalar")
    @PreAuthorize("hasAuthority('SERENAZGO_SUPERVISION_ESCALAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA', 'SUPERVISOR_SERENAZGO')")
    public ResponseEntity<SeguimientoResponse> escalarSeguimiento(
            @PathVariable UUID id,
            @Valid @RequestBody EscalarSeguimientoRequest req,
            @AuthenticationPrincipal CustomUserDetails user,
            HttpServletRequest request) {
        return ResponseEntity.ok(supervisionService.escalarSeguimiento(id, req, user, request.getRemoteAddr()));
    }

    @PostMapping("/seguimientos/{id}/resolver")
    @PreAuthorize("hasAuthority('SERENAZGO_SUPERVISION_RESOLVER') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA', 'SUPERVISOR_SERENAZGO')")
    public ResponseEntity<SeguimientoResponse> resolverSeguimiento(
            @PathVariable UUID id,
            @Valid @RequestBody ResolverSeguimientoRequest req,
            @AuthenticationPrincipal CustomUserDetails user,
            HttpServletRequest request) {
        return ResponseEntity.ok(supervisionService.resolverSeguimiento(id, req, user, request.getRemoteAddr()));
    }

    @PostMapping("/seguimientos/{id}/cerrar")
    @PreAuthorize("hasAuthority('SERENAZGO_SUPERVISION_CERRAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA', 'SUPERVISOR_SERENAZGO')")
    public ResponseEntity<SeguimientoResponse> cerrarSeguimiento(
            @PathVariable UUID id,
            @Valid @RequestBody CerrarSeguimientoRequest req,
            @AuthenticationPrincipal CustomUserDetails user,
            HttpServletRequest request) {
        return ResponseEntity.ok(supervisionService.cerrarSeguimiento(id, req, user, request.getRemoteAddr()));
    }

    @PostMapping("/seguimientos/{id}/cancelar")
    @PreAuthorize("hasAuthority('SERENAZGO_SUPERVISION_CERRAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA', 'SUPERVISOR_SERENAZGO')")
    public ResponseEntity<SeguimientoResponse> cancelarSeguimiento(
            @PathVariable UUID id,
            @Valid @RequestBody CancelarSeguimientoRequest req,
            @AuthenticationPrincipal CustomUserDetails user,
            HttpServletRequest request) {
        return ResponseEntity.ok(supervisionService.cancelarSeguimiento(id, req, user, request.getRemoteAddr()));
    }

    @GetMapping("/exportar/excel")
    @PreAuthorize("hasAuthority('SERENAZGO_SUPERVISION_EXPORTAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA', 'SUPERVISOR_SERENAZGO')")
    public ResponseEntity<Resource> exportarExcel(
            FiltroSupervisionRequest filtro,
            @AuthenticationPrincipal CustomUserDetails user) {
        Resource res = supervisionService.exportarExcel(filtro, user);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"supervision_serenazgo_quinua.xlsx\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(res);
    }

    @GetMapping("/exportar/pdf")
    @PreAuthorize("hasAuthority('SERENAZGO_SUPERVISION_EXPORTAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA', 'SUPERVISOR_SERENAZGO')")
    public ResponseEntity<Resource> exportarPdf(
            FiltroSupervisionRequest filtro,
            @AuthenticationPrincipal CustomUserDetails user) {
        Resource res = supervisionService.exportarPdf(filtro, user);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"reporte_supervision_serenazgo.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(res);
    }
}
