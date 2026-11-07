package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.notificacion.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.specification.NotificacionSpecification;
import pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/tributaria/notificaciones")
@RequiredArgsConstructor
public class NotificacionTributariaRestController {

    private final NotificacionTributariaService notificacionService;
    private final PreparacionNotificacionService preparacionService;
    private final AsignacionNotificadorService asignacionService;
    private final IntentoNotificacionService intentoService;
    private final CargoRecepcionService cargoService;
    private final ValidacionNotificacionService validacionService;
    private final HistorialNotificacionService historialService;
    private final UsuarioRepository usuarioRepository;

    @GetMapping
    @PreAuthorize("hasAuthority('TRIBUTARIA_NOTIFICACIONES_VER') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_TRIBUTACION')")
    public ResponseEntity<Page<NotificacionResponse>> listarNotificaciones(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) UUID tipoId,
            @RequestParam(required = false) CategoriaTipoNotificacion categoria,
            @RequestParam(required = false) UUID medioId,
            @RequestParam(required = false) EstadoAdministrativoNotificacion estadoAdm,
            @RequestParam(required = false) EstadoEntregaNotificacion estadoEntrega,
            @RequestParam(required = false) EstadoValidezLegalNotificacion estadoValidez,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Specification<NotificacionTributaria> spec = NotificacionSpecification.conFiltros(
                q, tipoId, categoria, medioId, estadoAdm, estadoEntrega, estadoValidez, null, null);

        Page<NotificacionResponse> resultado = notificacionService.listar(
                spec, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "fechaCreacion")));
        return ResponseEntity.ok(resultado);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('TRIBUTARIA_NOTIFICACIONES_CREAR') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_TRIBUTACION')")
    public ResponseEntity<NotificacionResponse> registrarNotificacion(
            @Valid @RequestBody CrearNotificacionRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Usuario operador = obtenerUsuarioAutenticado(userDetails);
        Oficina oficina = null;
        NotificacionTributaria n = preparacionService.prepararNotificacion(request, operador, oficina);
        return ResponseEntity.ok(notificacionService.convertirAResponse(n));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('TRIBUTARIA_NOTIFICACIONES_VER') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_TRIBUTACION', 'ROLE_CAJERO')")
    public ResponseEntity<NotificacionResponse> obtenerPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(notificacionService.obtenerPorId(id));
    }

    @PostMapping("/{id}/validar")
    @PreAuthorize("hasAuthority('TRIBUTARIA_NOTIFICACIONES_VALIDAR') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_TRIBUTACION')")
    public ResponseEntity<NotificacionResponse> validar(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Usuario operador = obtenerUsuarioAutenticado(userDetails);
        return ResponseEntity.ok(notificacionService.validarNotificacion(id, operador));
    }

    @PostMapping("/{id}/autorizar")
    @PreAuthorize("hasAuthority('TRIBUTARIA_NOTIFICACIONES_AUTORIZAR') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_TRIBUTACION')")
    public ResponseEntity<NotificacionResponse> autorizar(
            @PathVariable UUID id,
            HttpServletRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String baseUrl = request.getScheme() + "://" + request.getServerName() +
                (request.getServerPort() != 80 && request.getServerPort() != 443 ? ":" + request.getServerPort() : "");
        Usuario operador = obtenerUsuarioAutenticado(userDetails);
        return ResponseEntity.ok(notificacionService.autorizarNotificacion(id, baseUrl, operador));
    }

    @PostMapping("/{id}/asignar")
    @PreAuthorize("hasAuthority('TRIBUTARIA_NOTIFICACIONES_ASIGNAR') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_TRIBUTACION')")
    public ResponseEntity<AsignacionNotificadorTributario> asignar(
            @PathVariable UUID id,
            @Valid @RequestBody AsignarNotificadorRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Usuario operador = obtenerUsuarioAutenticado(userDetails);
        AsignacionNotificadorTributario asig = asignacionService.asignarNotificador(
                id, request.getNotificadorId(), request.getFechaProgramada(), request.getSectorRuta(),
                request.getObservaciones(), operador);
        return ResponseEntity.ok(asig);
    }

    @PostMapping("/{id}/intentos")
    @PreAuthorize("hasAuthority('TRIBUTARIA_NOTIFICACIONES_REGISTRAR_INTENTO') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_TRIBUTACION')")
    public ResponseEntity<IntentoNotificacionTributaria> registrarIntento(
            @PathVariable UUID id,
            @Valid @RequestBody RegistrarIntentoRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Usuario notificador = obtenerUsuarioAutenticado(userDetails);
        return ResponseEntity.ok(intentoService.registrarIntento(id, request, notificador));
    }

    @PostMapping("/{id}/cargo")
    @PreAuthorize("hasAuthority('TRIBUTARIA_NOTIFICACIONES_REGISTRAR_CARGO') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_TRIBUTACION')")
    public ResponseEntity<CargoRecepcionTributaria> registrarCargo(
            @PathVariable UUID id,
            @Valid @RequestBody RegistrarCargoRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Usuario operador = obtenerUsuarioAutenticado(userDetails);
        return ResponseEntity.ok(cargoService.registrarCargo(id, request, operador));
    }

    @PostMapping("/{id}/validar-procedimiento")
    @PreAuthorize("hasAuthority('TRIBUTARIA_NOTIFICACIONES_VALIDAR_PROCEDIMIENTO') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_TRIBUTACION')")
    public ResponseEntity<ValidacionProcedimientoNotificacion> validarProcedimiento(
            @PathVariable UUID id,
            @Valid @RequestBody ValidarProcedimientoRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Usuario revisor = obtenerUsuarioAutenticado(userDetails);
        return ResponseEntity.ok(validacionService.calificarProcedimiento(id, request, revisor));
    }

    @PostMapping("/{id}/anular")
    @PreAuthorize("hasAuthority('TRIBUTARIA_NOTIFICACIONES_ANULAR') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_TRIBUTACION')")
    public ResponseEntity<NotificacionResponse> anular(
            @PathVariable UUID id,
            @Valid @RequestBody AnularNotificacionRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Usuario operador = obtenerUsuarioAutenticado(userDetails);
        return ResponseEntity.ok(notificacionService.anularNotificacion(id, request.getMotivoAnulacion(), operador));
    }

    @GetMapping("/{id}/historial")
    @PreAuthorize("hasAuthority('TRIBUTARIA_NOTIFICACIONES_VER_HISTORIAL') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_TRIBUTACION')")
    public ResponseEntity<List<HistorialNotificacionTributaria>> historial(@PathVariable UUID id) {
        return ResponseEntity.ok(historialService.listarEventos(id));
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasAuthority('TRIBUTARIA_NOTIFICACIONES_VER') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_TRIBUTACION')")
    public ResponseEntity<DashboardNotificacionResponse> metricasDashboard() {
        return ResponseEntity.ok(notificacionService.obtenerDashboardMetrics());
    }

    private Usuario obtenerUsuarioAutenticado(CustomUserDetails userDetails) {
        String username = userDetails != null ? userDetails.getUsername() : "admin";
        return usuarioRepository.findByUsername(username).orElse(null);
    }
}

