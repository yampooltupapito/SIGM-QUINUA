package pe.gob.municipalidad.sigm.modules.serenazgo.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoProgramacionServicio;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.ProgramacionServicioService;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.TurnoSerenazgoService;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/serenazgo")
@RequiredArgsConstructor
@Slf4j
public class ProgramacionRestController {

    private final ProgramacionServicioService programacionService;
    private final TurnoSerenazgoService turnoService;

    @GetMapping("/programacion/dashboard")
    @PreAuthorize("hasAuthority('SERENAZGO_PROGRAMACION_VER') or hasRole('ROLE_ADMIN') or hasRole('ROLE_SERENO')")
    public ResponseEntity<DashboardProgramacionResponse> obtenerDashboard() {
        return ResponseEntity.ok(programacionService.obtenerDashboardProgramacion());
    }

    @GetMapping("/programacion/calendario")
    @PreAuthorize("hasAuthority('SERENAZGO_PROGRAMACION_VER') or hasRole('ROLE_ADMIN') or hasRole('ROLE_SERENO')")
    public ResponseEntity<List<CalendarioEventoResponse>> obtenerEventosCalendario(
            @RequestParam(name = "desde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(name = "hasta", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(name = "sectorId", required = false) UUID sectorId,
            @RequestParam(name = "estado", required = false) EstadoProgramacionServicio estado) {

        return ResponseEntity.ok(programacionService.obtenerEventosCalendario(desde, hasta, sectorId, estado));
    }

    @PostMapping("/programacion/verificar-conflictos")
    @PreAuthorize("hasAuthority('SERENAZGO_PROGRAMACION_CREAR') or hasAuthority('SERENAZGO_PROGRAMACION_EDITAR') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<ConflictoDisponibilidadResponse>> verificarConflictos(
            @RequestParam("fecha") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam("turnoId") UUID turnoId,
            @RequestBody(required = false) Map<String, List<UUID>> recursos,
            @RequestParam(name = "excluirId", required = false) UUID excluirId) {

        List<UUID> personalIds = recursos != null ? recursos.get("personalIds") : null;
        List<UUID> unidadIds = recursos != null ? recursos.get("unidadIds") : null;
        List<UUID> equipamientoIds = recursos != null ? recursos.get("equipamientoIds") : null;

        List<ConflictoDisponibilidadResponse> conflictos = programacionService.verificarConflictos(
                fecha, turnoId, personalIds, unidadIds, equipamientoIds, excluirId);

        return ResponseEntity.ok(conflictos);
    }

    @PostMapping("/programacion/{id}/confirmar")
    @PreAuthorize("hasAuthority('SERENAZGO_PROGRAMACION_CONFIRMAR') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<ProgramacionResponse> confirmarServicio(
            @PathVariable UUID id,
            @RequestParam(name = "motivo", required = false) String motivo,
            Principal principal) {
        String username = principal != null ? principal.getName() : "admin";
        return ResponseEntity.ok(programacionService.confirmarProgramacion(id, motivo, username));
    }

    @PostMapping("/programacion/{id}/reprogramar")
    @PreAuthorize("hasAuthority('SERENAZGO_PROGRAMACION_REPROGRAMAR') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<ProgramacionResponse> reprogramarServicio(
            @PathVariable UUID id,
            @Valid @RequestBody ReprogramarServicioRequest request,
            Principal principal) {
        String username = principal != null ? principal.getName() : "admin";
        return ResponseEntity.ok(programacionService.reprogramarServicio(id, request, username));
    }

    @PostMapping("/programacion/{id}/cancelar")
    @PreAuthorize("hasAuthority('SERENAZGO_PROGRAMACION_CANCELAR') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<ProgramacionResponse> cancelarServicio(
            @PathVariable UUID id,
            @Valid @RequestBody CancelarServicioRequest request,
            Principal principal) {
        String username = principal != null ? principal.getName() : "admin";
        return ResponseEntity.ok(programacionService.cancelarServicio(id, request, username));
    }

    @GetMapping("/programacion/exportar/pdf")
    @PreAuthorize("hasAuthority('SERENAZGO_PROGRAMACION_EXPORTAR') or hasRole('ROLE_ADMIN') or hasRole('ROLE_SERENO')")
    public ResponseEntity<byte[]> exportarDespachoPdf(
            @RequestParam(name = "fecha", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        LocalDate f = fecha != null ? fecha : LocalDate.now();
        byte[] pdf = programacionService.exportarDespachoDiarioPdf(f);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "Despacho_Serenazgo_" + f + ".pdf");
        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
    }

    @GetMapping("/programacion/exportar/excel")
    @PreAuthorize("hasAuthority('SERENAZGO_PROGRAMACION_EXPORTAR') or hasRole('ROLE_ADMIN') or hasRole('ROLE_SERENO')")
    public ResponseEntity<byte[]> exportarExcel(
            @RequestParam(name = "desde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(name = "hasta", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {

        byte[] excel = programacionService.exportarProgramacionExcel(desde, hasta);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", "Rol_Servicios_Serenazgo.xlsx");
        return new ResponseEntity<>(excel, headers, HttpStatus.OK);
    }

    @GetMapping("/turnos")
    @PreAuthorize("hasAuthority('SERENAZGO_TURNOS_VER') or hasRole('ROLE_ADMIN') or hasRole('ROLE_SERENO')")
    public ResponseEntity<List<TurnoResponse>> listarTurnos() {
        return ResponseEntity.ok(turnoService.listarTurnosActivos());
    }
}
