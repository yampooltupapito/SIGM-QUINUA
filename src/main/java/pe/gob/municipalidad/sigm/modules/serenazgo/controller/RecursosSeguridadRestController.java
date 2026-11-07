package pe.gob.municipalidad.sigm.modules.serenazgo.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.EquipamientoSerenazgoService;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.RecursosSeguridadService;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.UnidadesMovilesService;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/serenazgo/recursos")
@RequiredArgsConstructor
public class RecursosSeguridadRestController {

    private final RecursosSeguridadService recursosService;
    private final UnidadesMovilesService unidadesService;
    private final EquipamientoSerenazgoService equipamientoService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasAuthority('SERENAZGO_UNIDADES_VER') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA', 'SERENO')")
    public ResponseEntity<DashboardRecursosResponse> obtenerDashboard() {
        return ResponseEntity.ok(recursosService.obtenerDashboardRecursos());
    }

    @PostMapping("/mantenimientos/{id}/finalizar")
    @PreAuthorize("hasAuthority('SERENAZGO_UNIDADES_MANTENIMIENTO') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA')")
    public ResponseEntity<MantenimientoResponse> finalizarMantenimiento(
            @PathVariable UUID id,
            @Valid @RequestBody FinalizarMantenimientoRequest request,
            Authentication auth) {
        return ResponseEntity.ok(recursosService.finalizarMantenimiento(id, request, auth.getName()));
    }

    @PostMapping("/incidencias/{id}/resolver")
    @PreAuthorize("hasAuthority('SERENAZGO_UNIDADES_INCIDENCIAS') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA')")
    public ResponseEntity<IncidenciaTecnicaResponse> resolverIncidencia(
            @PathVariable UUID id,
            @Valid @RequestBody ResolverIncidenciaTecnicaRequest request,
            Authentication auth) {
        return ResponseEntity.ok(recursosService.resolverIncidencia(id, request, auth.getName()));
    }

    @GetMapping("/exportar-excel")
    @PreAuthorize("hasAuthority('SERENAZGO_EQUIPAMIENTO_EXPORTAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA')")
    public ResponseEntity<byte[]> exportarExcel(
            @ModelAttribute UnidadMovilFilter unidadFilter,
            @ModelAttribute EquipamientoFilter equipFilter) {
        byte[] excel = recursosService.exportarExcel(unidadFilter, equipFilter);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"inventario_recursos_serenazgo.xlsx\"")
                .body(excel);
    }
}
