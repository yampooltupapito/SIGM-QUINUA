package pe.gob.municipalidad.sigm.modules.serenazgo.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.IntervencionSerenazgoService;

import java.security.Principal;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/serenazgo/intervenciones")
@RequiredArgsConstructor
public class IntervencionesRestController {

    private final IntervencionSerenazgoService intervencionService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('SERENAZGO_INTERVENCIONES_VER', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_OPERADOR_CENTRAL', 'ROLE_SERENO')")
    public ResponseEntity<Page<IntervencionResponse>> listar(@ModelAttribute IntervencionFilter filtro,
                                                            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(intervencionService.listarIntervenciones(filtro, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_INTERVENCIONES_VER', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_OPERADOR_CENTRAL', 'ROLE_SERENO')")
    public ResponseEntity<IntervencionDetailResponse> obtenerPorId(@PathVariable("id") UUID id,
                                                                   Principal principal) {
        String usuario = principal != null ? principal.getName() : "API_USER";
        return ResponseEntity.ok(intervencionService.obtenerPorId(id, usuario));
    }

    @GetMapping("/codigo/{codigo}")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_INTERVENCIONES_VER', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_OPERADOR_CENTRAL', 'ROLE_SERENO')")
    public ResponseEntity<IntervencionDetailResponse> obtenerPorCodigo(@PathVariable("codigo") String codigo,
                                                                       Principal principal) {
        String usuario = principal != null ? principal.getName() : "API_USER";
        return ResponseEntity.ok(intervencionService.obtenerPorCodigo(codigo, usuario));
    }

    @GetMapping("/metricas")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_INTERVENCIONES_VER', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_OPERADOR_CENTRAL', 'ROLE_SERENO')")
    public ResponseEntity<DashboardIntervencionesResponse> metricas() {
        return ResponseEntity.ok(intervencionService.obtenerDashboard());
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('SERENAZGO_INTERVENCIONES_CREAR', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_OPERADOR_CENTRAL', 'ROLE_SERENO')")
    public ResponseEntity<IntervencionDetailResponse> crear(@Valid @RequestBody IntervencionCrearRequest request,
                                                           Principal principal) {
        String usuario = principal != null ? principal.getName() : "API_USER";
        IntervencionDetailResponse res = intervencionService.crearIntervencion(request, usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_INTERVENCIONES_EDITAR', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_OPERADOR_CENTRAL', 'ROLE_SERENO')")
    public ResponseEntity<IntervencionDetailResponse> actualizar(@PathVariable("id") UUID id,
                                                                 @Valid @RequestBody IntervencionEditarRequest request,
                                                                 Principal principal) {
        String usuario = principal != null ? principal.getName() : "API_USER";
        return ResponseEntity.ok(intervencionService.actualizarIntervencion(id, request, usuario));
    }

    @PostMapping("/{id}/actuaciones")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_INTERVENCIONES_REGISTRAR_ACTUACION', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_OPERADOR_CENTRAL', 'ROLE_SERENO')")
    public ResponseEntity<IntervencionDetailResponse> agregarActuacion(@PathVariable("id") UUID id,
                                                                       @Valid @RequestBody IntervencionActuacionRequest request,
                                                                       Principal principal) {
        String usuario = principal != null ? principal.getName() : "API_USER";
        return ResponseEntity.ok(intervencionService.registrarActuacion(id, request, usuario));
    }

    @PostMapping("/{id}/comunicaciones")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_INTERVENCIONES_REGISTRAR_ACTUACION', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_OPERADOR_CENTRAL', 'ROLE_SERENO')")
    public ResponseEntity<IntervencionDetailResponse> agregarComunicacion(@PathVariable("id") UUID id,
                                                                          @Valid @RequestBody IntervencionComunicacionRequest request,
                                                                          Principal principal) {
        String usuario = principal != null ? principal.getName() : "API_USER";
        return ResponseEntity.ok(intervencionService.registrarComunicacion(id, request, usuario));
    }

    @PostMapping("/{id}/enviar-revision")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_INTERVENCIONES_EDITAR', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_OPERADOR_CENTRAL', 'ROLE_SERENO')")
    public ResponseEntity<IntervencionDetailResponse> enviarRevision(@PathVariable("id") UUID id,
                                                                     Principal principal) {
        String usuario = principal != null ? principal.getName() : "API_USER";
        return ResponseEntity.ok(intervencionService.enviarARevision(id, usuario));
    }

    @PostMapping("/{id}/validar")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_INTERVENCIONES_VALIDAR', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO')")
    public ResponseEntity<IntervencionDetailResponse> validar(@PathVariable("id") UUID id,
                                                              @RequestBody(required = false) IntervencionValidarRequest request,
                                                              Principal principal) {
        String usuario = principal != null ? principal.getName() : "API_USER";
        return ResponseEntity.ok(intervencionService.validarIntervencion(id, request, usuario));
    }
}
