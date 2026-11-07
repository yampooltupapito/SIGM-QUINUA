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
import pe.gob.municipalidad.sigm.modules.serenazgo.service.PatrullajeSerenazgoService;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/serenazgo/patrullajes")
@RequiredArgsConstructor
public class PatrullajesRestController {

    private final PatrullajeSerenazgoService patrullajeService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('SERENAZGO_PATRULLAJES_VER', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_OPERADOR_CENTRAL', 'ROLE_SERENO')")
    public ResponseEntity<Page<PatrullajeResponse>> listarPatrullajes(PatrullajeFilter filter,
                                                                      @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(patrullajeService.listarPatrullajes(filter, pageable));
    }

    @GetMapping("/dashboard/kpis")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_PATRULLAJES_VER', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_OPERADOR_CENTRAL')")
    public ResponseEntity<DashboardPatrullajesResponse> obtenerKpis() {
        return ResponseEntity.ok(patrullajeService.obtenerDashboardKpis());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_PATRULLAJES_VER', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_OPERADOR_CENTRAL', 'ROLE_SERENO')")
    public ResponseEntity<PatrullajeDetailResponse> obtenerPorId(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(patrullajeService.obtenerPatrullajePorId(id));
    }

    @GetMapping("/rutas")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_PATRULLAJES_VER', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_OPERADOR_CENTRAL', 'ROLE_SERENO')")
    public ResponseEntity<List<RutaPatrullajeResponse>> listarRutasActivas() {
        return ResponseEntity.ok(patrullajeService.listarRutasActivas());
    }

    @GetMapping("/rutas/sector/{sectorId}")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_PATRULLAJES_VER', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_OPERADOR_CENTRAL', 'ROLE_SERENO')")
    public ResponseEntity<List<RutaPatrullajeResponse>> listarRutasPorSector(@PathVariable("sectorId") UUID sectorId) {
        return ResponseEntity.ok(patrullajeService.listarRutasPorSector(sectorId));
    }

    @GetMapping("/rutas/{rutaId}")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_PATRULLAJES_VER', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_OPERADOR_CENTRAL', 'ROLE_SERENO')")
    public ResponseEntity<RutaPatrullajeResponse> obtenerRutaPorId(@PathVariable("rutaId") UUID rutaId) {
        return ResponseEntity.ok(patrullajeService.obtenerRutaPorId(rutaId));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('SERENAZGO_PATRULLAJES_CREAR', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_OPERADOR_CENTRAL')")
    public ResponseEntity<PatrullajeDetailResponse> crearPatrullaje(@Valid @RequestBody PatrullajeCrearRequest request,
                                                                   Principal principal) {
        String username = (principal != null) ? principal.getName() : "API_USER";
        PatrullajeDetailResponse nuevo = patrullajeService.crearPatrullaje(request, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
    }

    @PostMapping("/{id}/iniciar")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_PATRULLAJES_INICIAR', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_OPERADOR_CENTRAL', 'ROLE_SERENO')")
    public ResponseEntity<PatrullajeDetailResponse> iniciarPatrullaje(@PathVariable("id") UUID id,
                                                                     @RequestBody(required = false) PatrullajeIniciarRequest request,
                                                                     Principal principal) {
        String username = (principal != null) ? principal.getName() : "API_USER";
        return ResponseEntity.ok(patrullajeService.iniciarPatrullaje(id, request, username));
    }

    @PostMapping("/{id}/puntos")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_PATRULLAJES_REGISTRAR_RECORRIDO', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_OPERADOR_CENTRAL', 'ROLE_SERENO')")
    public ResponseEntity<PatrullajeDetailResponse> registrarPasoPunto(@PathVariable("id") UUID id,
                                                                      @Valid @RequestBody PatrullajeRegistrarVisitaRequest request,
                                                                      Principal principal) {
        String username = (principal != null) ? principal.getName() : "API_USER";
        return ResponseEntity.ok(patrullajeService.registrarPasoPunto(id, request, username));
    }

    @PostMapping("/{id}/novedades")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_PATRULLAJES_REGISTRAR_NOVEDAD', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_OPERADOR_CENTRAL', 'ROLE_SERENO')")
    public ResponseEntity<PatrullajeDetailResponse> registrarNovedad(@PathVariable("id") UUID id,
                                                                    @Valid @RequestBody PatrullajeRegistrarNovedadRequest request,
                                                                    Principal principal) {
        String username = (principal != null) ? principal.getName() : "API_USER";
        return ResponseEntity.ok(patrullajeService.registrarNovedad(id, request, username));
    }

    @PostMapping("/{id}/pausar")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_PATRULLAJES_INICIAR', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_OPERADOR_CENTRAL', 'ROLE_SERENO')")
    public ResponseEntity<PatrullajeDetailResponse> pausarPatrullaje(@PathVariable("id") UUID id,
                                                                    @Valid @RequestBody PatrullajePausarRequest request,
                                                                    Principal principal) {
        String username = (principal != null) ? principal.getName() : "API_USER";
        return ResponseEntity.ok(patrullajeService.pausarPatrullaje(id, request, username));
    }

    @PostMapping("/{id}/reanudar")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_PATRULLAJES_INICIAR', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_OPERADOR_CENTRAL', 'ROLE_SERENO')")
    public ResponseEntity<PatrullajeDetailResponse> reanudarPatrullaje(@PathVariable("id") UUID id,
                                                                      @RequestBody(required = false) PatrullajeReanudarRequest request,
                                                                      Principal principal) {
        String username = (principal != null) ? principal.getName() : "API_USER";
        return ResponseEntity.ok(patrullajeService.reanudarPatrullaje(id, request, username));
    }

    @PostMapping("/{id}/cambiar-ruta")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_PATRULLAJES_CAMBIAR_RUTA', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_OPERADOR_CENTRAL')")
    public ResponseEntity<PatrullajeDetailResponse> cambiarRuta(@PathVariable("id") UUID id,
                                                               @Valid @RequestBody PatrullajeCambioRutaRequest request,
                                                               Principal principal) {
        String username = (principal != null) ? principal.getName() : "API_USER";
        return ResponseEntity.ok(patrullajeService.cambiarRuta(id, request, username));
    }

    @PostMapping("/{id}/finalizar")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_PATRULLAJES_FINALIZAR', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_OPERADOR_CENTRAL', 'ROLE_SERENO')")
    public ResponseEntity<PatrullajeDetailResponse> finalizarPatrullaje(@PathVariable("id") UUID id,
                                                                       @Valid @RequestBody PatrullajeFinalizarRequest request,
                                                                       Principal principal) {
        String username = (principal != null) ? principal.getName() : "API_USER";
        return ResponseEntity.ok(patrullajeService.finalizarPatrullaje(id, request, username));
    }

    @PostMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_PATRULLAJES_CANCELAR', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO')")
    public ResponseEntity<PatrullajeDetailResponse> cancelarPatrullaje(@PathVariable("id") UUID id,
                                                                      @Valid @RequestBody PatrullajeCancelarRequest request,
                                                                      Principal principal) {
        String username = (principal != null) ? principal.getName() : "API_USER";
        return ResponseEntity.ok(patrullajeService.cancelarPatrullaje(id, request, username));
    }
}
