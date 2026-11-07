package pe.gob.municipalidad.sigm.modules.serenazgo.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoIncidenciaSeguridad;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.NivelConfidencialidad;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.PrioridadIncidenciaSeguridad;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.IncidenciaSeguridadCiudadanaService;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/serenazgo/incidencias")
@RequiredArgsConstructor
@Slf4j
public class IncidenciasRestController {

    private final IncidenciaSeguridadCiudadanaService incidenciaService;

    @GetMapping
    @PreAuthorize("hasAuthority('SERENAZGO_INCIDENCIAS_VER') or hasRole('ROLE_ADMIN') or hasRole('ROLE_SERENO') or hasRole('ROLE_SUPERADMIN')")
    public ResponseEntity<Page<IncidenciaResponse>> listarIncidencias(
            @RequestParam(name = "termino", required = false) String termino,
            @RequestParam(name = "desde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(name = "hasta", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(name = "estado", required = false) EstadoIncidenciaSeguridad estado,
            @RequestParam(name = "prioridad", required = false) PrioridadIncidenciaSeguridad prioridad,
            @RequestParam(name = "tipoId", required = false) UUID tipoId,
            @RequestParam(name = "sectorId", required = false) UUID sectorId,
            @RequestParam(name = "zonaId", required = false) UUID zonaId,
            @RequestParam(name = "confidencialidad", required = false) NivelConfidencialidad confidencialidad,
            Pageable pageable) {

        IncidenciaFilter filtro = IncidenciaFilter.builder()
                .termino(termino)
                .desde(desde)
                .hasta(hasta)
                .estado(estado)
                .prioridad(prioridad)
                .tipoId(tipoId)
                .sectorId(sectorId)
                .zonaId(zonaId)
                .confidencialidad(confidencialidad)
                .build();

        return ResponseEntity.ok(incidenciaService.listarConFiltros(filtro, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SERENAZGO_INCIDENCIAS_VER') or hasRole('ROLE_ADMIN') or hasRole('ROLE_SERENO') or hasRole('ROLE_SUPERADMIN')")
    public ResponseEntity<IncidenciaDetailResponse> obtenerDetalle(@PathVariable UUID id, Authentication auth) {
        boolean puedeVerSensibles = auth != null && auth.getAuthorities().stream().anyMatch(a ->
                a.getAuthority().equals("ROLE_ADMIN") ||
                a.getAuthority().equals("ROLE_SUPERADMIN") ||
                a.getAuthority().equals("SERENAZGO_INCIDENCIAS_EDITAR"));

        return ResponseEntity.ok(incidenciaService.obtenerDetallePorId(id, puedeVerSensibles));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SERENAZGO_INCIDENCIAS_CREAR') or hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPERADMIN')")
    public ResponseEntity<IncidenciaResponse> crearIncidencia(@Valid @RequestBody IncidenciaCreateRequest request,
                                                              Principal principal) {
        String username = principal != null ? principal.getName() : "SISTEMA";
        IncidenciaResponse response = incidenciaService.registrarIncidencia(request, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SERENAZGO_INCIDENCIAS_EDITAR') or hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPERADMIN')")
    public ResponseEntity<IncidenciaResponse> actualizarIncidencia(@PathVariable UUID id,
                                                                   @Valid @RequestBody IncidenciaUpdateRequest request,
                                                                   Principal principal) {
        String username = principal != null ? principal.getName() : "SISTEMA";
        return ResponseEntity.ok(incidenciaService.actualizarIncidencia(id, request, username));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAuthority('SERENAZGO_INCIDENCIAS_CAMBIAR_ESTADO') or hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPERADMIN')")
    public ResponseEntity<IncidenciaResponse> cambiarEstado(@PathVariable UUID id,
                                                            @Valid @RequestBody CambioEstadoIncidenciaRequest request,
                                                            Principal principal) {
        String username = principal != null ? principal.getName() : "SISTEMA";
        return ResponseEntity.ok(incidenciaService.cambiarEstado(id, request, username));
    }

    @PatchMapping("/{id}/prioridad")
    @PreAuthorize("hasAuthority('SERENAZGO_INCIDENCIAS_PRIORIZAR') or hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPERADMIN')")
    public ResponseEntity<IncidenciaResponse> cambiarPrioridad(@PathVariable UUID id,
                                                               @Valid @RequestBody CambioPrioridadIncidenciaRequest request,
                                                               Principal principal) {
        String username = principal != null ? principal.getName() : "SISTEMA";
        return ResponseEntity.ok(incidenciaService.cambiarPrioridad(id, request, username));
    }

    @PostMapping("/{id}/cerrar")
    @PreAuthorize("hasAuthority('SERENAZGO_INCIDENCIAS_CERRAR') or hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPERADMIN')")
    public ResponseEntity<IncidenciaResponse> cerrarIncidencia(@PathVariable UUID id,
                                                               @Valid @RequestBody CierreIncidenciaRequest request,
                                                               Principal principal) {
        String username = principal != null ? principal.getName() : "SISTEMA";
        return ResponseEntity.ok(incidenciaService.cerrarIncidencia(id, request, username));
    }

    @PostMapping("/{id}/descartar")
    @PreAuthorize("hasAuthority('SERENAZGO_INCIDENCIAS_CERRAR') or hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPERADMIN')")
    public ResponseEntity<IncidenciaResponse> descartarIncidencia(@PathVariable UUID id,
                                                                  @Valid @RequestBody DescarteIncidenciaRequest request,
                                                                  Principal principal) {
        String username = principal != null ? principal.getName() : "SISTEMA";
        return ResponseEntity.ok(incidenciaService.descartarIncidencia(id, request, username));
    }

    @PostMapping("/{id}/comentarios")
    @PreAuthorize("hasAuthority('SERENAZGO_INCIDENCIAS_EDITAR') or hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPERADMIN')")
    public ResponseEntity<IncidenciaComentarioResponse> agregarComentario(@PathVariable UUID id,
                                                                          @Valid @RequestBody AgregarComentarioRequest request,
                                                                          Principal principal) {
        String username = principal != null ? principal.getName() : "SISTEMA";
        return ResponseEntity.status(HttpStatus.CREATED).body(incidenciaService.agregarComentario(id, request, username));
    }

    @PostMapping(value = "/{id}/adjuntos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('SERENAZGO_INCIDENCIAS_ADJUNTAR') or hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPERADMIN')")
    public ResponseEntity<IncidenciaDocumentoResponse> adjuntarDocumento(
            @PathVariable UUID id,
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam(name = "descripcion", required = false) String descripcion,
            @RequestParam(name = "esFotografia", defaultValue = "false") boolean esFotografia,
            Principal principal) throws IOException {

        String username = principal != null ? principal.getName() : "SISTEMA";
        IncidenciaDocumentoResponse res = incidenciaService.adjuntarDocumento(
                id,
                archivo.getOriginalFilename(),
                archivo.getContentType(),
                archivo.getBytes(),
                descripcion,
                esFotografia,
                username
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @GetMapping("/kpis")
    @PreAuthorize("hasAuthority('SERENAZGO_INCIDENCIAS_VER') or hasRole('ROLE_ADMIN') or hasRole('ROLE_SERENO') or hasRole('ROLE_SUPERADMIN')")
    public ResponseEntity<DashboardIncidenciasResponse> obtenerKpis() {
        return ResponseEntity.ok(incidenciaService.obtenerDashboardIncidencias());
    }

    @GetMapping("/duplicados")
    @PreAuthorize("hasAuthority('SERENAZGO_INCIDENCIAS_VER') or hasRole('ROLE_ADMIN') or hasRole('ROLE_SERENO') or hasRole('ROLE_SUPERADMIN')")
    public ResponseEntity<List<PosibleDuplicadoResponse>> verificarDuplicados(
            @RequestParam("tipoId") UUID tipoId,
            @RequestParam(name = "sectorId", required = false) UUID sectorId,
            @RequestParam("fecha") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime fecha,
            @RequestParam(name = "excluirId", required = false) UUID excluirId) {

        return ResponseEntity.ok(incidenciaService.buscarPosiblesDuplicados(tipoId, sectorId, fecha, excluirId));
    }
}
