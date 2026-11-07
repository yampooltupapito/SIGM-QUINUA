package pe.gob.municipalidad.sigm.modules.serenazgo.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.EquipamientoSerenazgo;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.PersonalSerenazgo;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.UnidadMovilSerenazgo;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.AtencionIncidenciaSerenazgoService;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/serenazgo/atenciones")
@RequiredArgsConstructor
public class AtencionesRestController {

    private final AtencionIncidenciaSerenazgoService atencionService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('SERENAZGO_ATENCIONES_VER', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_SERENO')")
    public ResponseEntity<Page<AtencionResponse>> listarAtenciones(
            @ModelAttribute AtencionFilter filtro,
            @PageableDefault(size = 15, sort = "fechaHoraAsignacion", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(atencionService.listarAtenciones(filtro, pageable));
    }

    @GetMapping("/metricas")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_ATENCIONES_VER', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO')")
    public ResponseEntity<DashboardAtencionesResponse> obtenerMetricas() {
        return ResponseEntity.ok(atencionService.obtenerMetricas());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_ATENCIONES_VER', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_SERENO')")
    public ResponseEntity<AtencionDetailResponse> obtenerPorId(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(atencionService.obtenerDetallePorId(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('SERENAZGO_ATENCIONES_CREAR', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO')")
    public ResponseEntity<AtencionDetailResponse> crearAtencion(
            @Valid @RequestBody AtencionCrearRequest request,
            Principal principal) {
        return ResponseEntity.ok(atencionService.crearAtencion(request, principal != null ? principal.getName() : "SISTEMA"));
    }

    @PostMapping("/{id}/asignar")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_ATENCIONES_ASIGNAR', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO')")
    public ResponseEntity<AtencionDetailResponse> asignarRecursos(
            @PathVariable("id") UUID id,
            @Valid @RequestBody AtencionAsignarRecursosRequest request,
            Principal principal) {
        return ResponseEntity.ok(atencionService.asignarRecursos(id, request, principal != null ? principal.getName() : "SISTEMA"));
    }

    @PostMapping("/{id}/despachar")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_ATENCIONES_DESPACHAR', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO')")
    public ResponseEntity<AtencionDetailResponse> despachar(
            @PathVariable("id") UUID id,
            @Valid @RequestBody(required = false) AtencionDespacharRequest request,
            Principal principal) {
        return ResponseEntity.ok(atencionService.despachar(id, request, principal != null ? principal.getName() : "SISTEMA"));
    }

    @PostMapping("/{id}/responder-despacho")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_ATENCIONES_ACEPTAR', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_SERENO')")
    public ResponseEntity<AtencionDetailResponse> responderDespacho(
            @PathVariable("id") UUID id,
            @Valid @RequestBody AtencionAceptarRechazarRequest request,
            Principal principal) {
        return ResponseEntity.ok(atencionService.responderDespacho(id, request, principal != null ? principal.getName() : "SISTEMA"));
    }

    @PostMapping("/{id}/iniciar")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_ATENCIONES_INICIAR', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_SERENO')")
    public ResponseEntity<AtencionDetailResponse> iniciarAtencion(
            @PathVariable("id") UUID id,
            @Valid @RequestBody(required = false) AtencionIniciarRequest request,
            Principal principal) {
        return ResponseEntity.ok(atencionService.iniciarAtencion(id, request, principal != null ? principal.getName() : "SISTEMA"));
    }

    @PostMapping("/{id}/novedades")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_ATENCIONES_REGISTRAR_NOVEDAD', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_SERENO')")
    public ResponseEntity<AtencionDetailResponse> registrarNovedad(
            @PathVariable("id") UUID id,
            @Valid @RequestBody AtencionNovedadRequest request,
            Principal principal) {
        return ResponseEntity.ok(atencionService.registrarNovedad(id, request, principal != null ? principal.getName() : "SISTEMA"));
    }

    @PostMapping("/{id}/pausar")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_ATENCIONES_REGISTRAR_NOVEDAD', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_SERENO')")
    public ResponseEntity<AtencionDetailResponse> pausarEnEspera(
            @PathVariable("id") UUID id,
            @Valid @RequestBody AtencionPausarReanudarRequest request,
            Principal principal) {
        return ResponseEntity.ok(atencionService.pausarEnEspera(id, request, principal != null ? principal.getName() : "SISTEMA"));
    }

    @PostMapping("/{id}/reanudar")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_ATENCIONES_REGISTRAR_NOVEDAD', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_SERENO')")
    public ResponseEntity<AtencionDetailResponse> reanudarAtencion(
            @PathVariable("id") UUID id,
            @Valid @RequestBody AtencionPausarReanudarRequest request,
            Principal principal) {
        return ResponseEntity.ok(atencionService.reanudarAtencion(id, request, principal != null ? principal.getName() : "SISTEMA"));
    }

    @PostMapping("/{id}/reasignar")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_ATENCIONES_REASIGNAR', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO')")
    public ResponseEntity<AtencionDetailResponse> reasignar(
            @PathVariable("id") UUID id,
            @Valid @RequestBody AtencionReasignarRequest request,
            Principal principal) {
        return ResponseEntity.ok(atencionService.reasignar(id, request, principal != null ? principal.getName() : "SISTEMA"));
    }

    @PostMapping("/{id}/escalar")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_ATENCIONES_ESCALAR', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO')")
    public ResponseEntity<AtencionDetailResponse> escalar(
            @PathVariable("id") UUID id,
            @Valid @RequestBody AtencionEscalarRequest request,
            Principal principal) {
        return ResponseEntity.ok(atencionService.escalar(id, request, principal != null ? principal.getName() : "SISTEMA"));
    }

    @PostMapping("/{id}/finalizar")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_ATENCIONES_FINALIZAR', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO')")
    public ResponseEntity<AtencionDetailResponse> finalizarAtencion(
            @PathVariable("id") UUID id,
            @Valid @RequestBody AtencionFinalizarRequest request,
            Principal principal) {
        return ResponseEntity.ok(atencionService.finalizarAtencion(id, request, principal != null ? principal.getName() : "SISTEMA"));
    }

    @PostMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_ATENCIONES_CANCELAR', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO')")
    public ResponseEntity<AtencionDetailResponse> cancelarAtencion(
            @PathVariable("id") UUID id,
            @Valid @RequestBody AtencionCancelarRequest request,
            Principal principal) {
        return ResponseEntity.ok(atencionService.cancelarAtencion(id, request, principal != null ? principal.getName() : "SISTEMA"));
    }

    @GetMapping("/disponibilidad/personal")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_ATENCIONES_VER', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO')")
    public ResponseEntity<List<PersonalSerenazgo>> listarPersonalDisponible() {
        return ResponseEntity.ok(atencionService.listarPersonalDisponible());
    }

    @GetMapping("/disponibilidad/unidades")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_ATENCIONES_VER', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO')")
    public ResponseEntity<List<UnidadMovilSerenazgo>> listarUnidadesDisponibles() {
        return ResponseEntity.ok(atencionService.listarUnidadesDisponibles());
    }

    @GetMapping("/disponibilidad/equipos")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_ATENCIONES_VER', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO')")
    public ResponseEntity<List<EquipamientoSerenazgo>> listarEquipamientoDisponible() {
        return ResponseEntity.ok(atencionService.listarEquipamientoDisponible());
    }

    @GetMapping("/exportar/pdf")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_ATENCIONES_EXPORTAR', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO')")
    public ResponseEntity<byte[]> exportarPdf(@ModelAttribute AtencionFilter filtro) {
        byte[] pdf = atencionService.exportarPdf(filtro);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"atenciones_serenazgo.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/exportar/excel")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_ATENCIONES_EXPORTAR', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO')")
    public ResponseEntity<byte[]> exportarExcel(@ModelAttribute AtencionFilter filtro) {
        byte[] excel = atencionService.exportarExcel(filtro);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"atenciones_serenazgo.xlsx\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel);
    }
}
