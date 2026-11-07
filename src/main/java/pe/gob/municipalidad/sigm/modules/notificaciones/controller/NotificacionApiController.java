package pe.gob.municipalidad.sigm.modules.notificaciones.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.gob.municipalidad.sigm.common.notification.dto.NotificationDtos.EnviarNotificacionOficinaRequest;
import pe.gob.municipalidad.sigm.common.notification.dto.NotificationDtos.NotificacionItemDto;
import pe.gob.municipalidad.sigm.common.notification.dto.NotificationDtos.NotificacionPreferenciasDto;
import pe.gob.municipalidad.sigm.common.notification.dto.NotificationDtos.NotificacionProgramadaDto;
import pe.gob.municipalidad.sigm.common.notification.dto.NotificationDtos.ProgramarNotificacionRequest;
import pe.gob.municipalidad.sigm.common.notification.service.CentralNotificationService;
import pe.gob.municipalidad.sigm.common.response.ApiResponse;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador API REST unificado para el sistema de notificaciones institucionales de SIGM-QUINUA.
 * Soporta consultas paginadas, filtros por categoría/lectura, gestión de preferencias,
 * avisos por oficina y programación diferida.
 */
@RestController
@RequestMapping("/api/notificaciones")
@RequiredArgsConstructor
public class NotificacionApiController {

    private final CentralNotificationService centralNotificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> listarNotificaciones(
            @RequestParam(required = false) Boolean leida,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 10, sort = "fechaCreacion", direction = Sort.Direction.DESC) Pageable pageable,
            Authentication authentication
    ) {
        String username = authentication.getName();
        Page<NotificacionItemDto> page = centralNotificationService.buscarNotificacionesFiltradas(username, leida, tipo, search, pageable);
        long noLeidas = centralNotificationService.contarNoLeidas(username);

        Map<String, Object> data = new HashMap<>();
        data.put("notificaciones", page.getContent());
        data.put("content", page.getContent());
        data.put("noLeidas", noLeidas);
        data.put("totalElements", page.getTotalElements());
        data.put("totalPages", page.getTotalPages());
        data.put("page", page.getNumber());
        data.put("size", page.getSize());
        data.put("first", page.isFirst());
        data.put("last", page.isLast());

        return ApiResponse.ok("Notificaciones recuperadas con éxito", data);
    }

    @GetMapping("/no-leidas/count")
    public ResponseEntity<ApiResponse<Map<String, Object>>> contarNoLeidas(Authentication authentication) {
        long count = centralNotificationService.contarNoLeidas(authentication.getName());
        return ApiResponse.ok("Contador recuperado", Map.of("noLeidas", count, "count", count));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NotificacionItemDto>> obtenerPorId(
            @PathVariable Long id,
            Authentication authentication
    ) {
        NotificacionItemDto dto = centralNotificationService.obtenerNotificacionPorId(id, authentication.getName());
        return ApiResponse.ok("Detalle de notificación recuperado con éxito", dto);
    }

    @RequestMapping(value = "/{id}/leer", method = {RequestMethod.PATCH, RequestMethod.PUT})
    public ResponseEntity<ApiResponse<Void>> marcarComoLeida(
            @PathVariable Long id,
            Authentication authentication
    ) {
        centralNotificationService.marcarComoLeida(id, authentication.getName());
        return ApiResponse.ok("Notificación marcada como leída");
    }

    @RequestMapping(value = {"/leer-todas", "/marcar-todas"}, method = {RequestMethod.PATCH, RequestMethod.PUT})
    public ResponseEntity<ApiResponse<Void>> marcarTodasComoLeidas(Authentication authentication) {
        centralNotificationService.marcarTodasComoLeidas(authentication.getName());
        return ApiResponse.ok("Todas las notificaciones han sido marcadas como leídas");
    }

    @GetMapping("/preferencias")
    public ResponseEntity<ApiResponse<NotificacionPreferenciasDto>> obtenerPreferencias(Authentication authentication) {
        NotificacionPreferenciasDto prefs = centralNotificationService.obtenerOInicializarPreferencias(authentication.getName());
        return ApiResponse.ok("Preferencias de notificación recuperadas con éxito", prefs);
    }

    @RequestMapping(value = "/preferencias", method = {RequestMethod.PATCH, RequestMethod.PUT})
    public ResponseEntity<ApiResponse<NotificacionPreferenciasDto>> actualizarPreferencias(
            @Valid @RequestBody NotificacionPreferenciasDto dto,
            Authentication authentication
    ) {
        NotificacionPreferenciasDto actualizado = centralNotificationService.actualizarPreferencias(authentication.getName(), dto);
        return ApiResponse.ok("Preferencias de notificación actualizadas exitosamente", actualizado);
    }

    @PostMapping("/programar")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ADMINISTRACION_MANAGE', 'NOTIFICACION_MANAGE')")
    public ResponseEntity<ApiResponse<NotificacionProgramadaDto>> programarNotificacion(
            @Valid @RequestBody ProgramarNotificacionRequest request,
            Authentication authentication
    ) {
        NotificacionProgramadaDto programada = centralNotificationService.programarNotificacion(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<NotificacionProgramadaDto>builder()
                        .success(true)
                        .message("Notificación programada exitosamente")
                        .data(programada)
                        .build());
    }

    @PostMapping("/oficina")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ADMINISTRACION_MANAGE', 'NOTIFICACION_MANAGE')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> enviarNotificacionOficina(
            @Valid @RequestBody EnviarNotificacionOficinaRequest request,
            Authentication authentication
    ) {
        int enviados = centralNotificationService.enviarNotificacionOficina(request, authentication.getName());
        return ApiResponse.ok("Notificación difundida a la oficina con éxito", Map.of("destinatariosNotificados", enviados));
    }

    @DeleteMapping("/programadas/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ADMINISTRACION_MANAGE', 'NOTIFICACION_MANAGE')")
    public ResponseEntity<ApiResponse<Void>> cancelarProgramacion(
            @PathVariable Long id,
            Authentication authentication
    ) {
        centralNotificationService.cancelarProgramacion(id, authentication.getName());
        return ApiResponse.ok("Programación de notificación cancelada exitosamente");
    }

    @GetMapping("/programadas")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ADMINISTRACION_MANAGE', 'NOTIFICACION_MANAGE')")
    public ResponseEntity<ApiResponse<Page<NotificacionProgramadaDto>>> listarProgramadas(
            @PageableDefault(size = 10, sort = "fechaProgramada", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<NotificacionProgramadaDto> programadas = centralNotificationService.listarProgramadas(pageable);
        return ApiResponse.ok("Listado de notificaciones programadas recuperado con éxito", programadas);
    }
}
