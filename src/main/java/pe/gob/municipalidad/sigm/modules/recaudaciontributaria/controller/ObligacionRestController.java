package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.ObligacionTributariaService;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tributaria/obligaciones")
@RequiredArgsConstructor
public class ObligacionRestController {

    private final ObligacionTributariaService obligacionService;

    @GetMapping
    @PreAuthorize("hasAuthority('TRIBUTARIA_OBLIGACIONES_VER') or hasRole('ADMIN')")
    public ResponseEntity<Page<ObligacionSummaryDto>> listar(
            @ModelAttribute ObligacionFilterDto filter,
            Pageable pageable
    ) {
        return ResponseEntity.ok(obligacionService.listarObligaciones(filter, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('TRIBUTARIA_OBLIGACIONES_VER') or hasRole('ADMIN')")
    public ResponseEntity<ObligacionDetailResponse> obtenerPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(obligacionService.obtenerPorId(id));
    }

    @PostMapping("/generar")
    @PreAuthorize("hasAuthority('TRIBUTARIA_OBLIGACIONES_GENERAR') or hasRole('ADMIN')")
    public ResponseEntity<ObligacionDetailResponse> generar(
            @Valid @RequestBody GeneracionObligacionRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String usuario = userDetails != null ? userDetails.getUsername() : "API_USER";
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(obligacionService.generarDesdeDeterminacion(request, usuario));
    }

    @PostMapping("/previsualizar-lote")
    @PreAuthorize("hasAuthority('TRIBUTARIA_OBLIGACIONES_GENERAR_LOTE') or hasRole('ADMIN')")
    public ResponseEntity<PrevisualizacionLoteDto> previsualizarLote(@Valid @RequestBody GeneracionLoteRequest request) {
        return ResponseEntity.ok(obligacionService.previsualizarLote(request));
    }

    @PostMapping("/generar-lote")
    @PreAuthorize("hasAuthority('TRIBUTARIA_OBLIGACIONES_GENERAR_LOTE') or hasRole('ADMIN')")
    public ResponseEntity<LoteGeneracionResponse> generarLote(
            @Valid @RequestBody GeneracionLoteRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String usuario = userDetails != null ? userDetails.getUsername() : "API_USER";
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(obligacionService.generarLote(request, usuario));
    }

    @PostMapping("/{id}/validar")
    @PreAuthorize("hasAuthority('TRIBUTARIA_OBLIGACIONES_VALIDAR') or hasRole('ADMIN')")
    public ResponseEntity<ObligacionDetailResponse> validar(
            @PathVariable UUID id,
            @RequestParam(required = false) String resolucion,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String usuario = userDetails != null ? userDetails.getUsername() : "API_USER";
        return ResponseEntity.ok(obligacionService.validarObligacion(id, resolucion, usuario));
    }

    @PostMapping("/{id}/suspender")
    @PreAuthorize("hasAuthority('TRIBUTARIA_OBLIGACIONES_SUSPENDER') or hasRole('ADMIN')")
    public ResponseEntity<ObligacionDetailResponse> suspender(
            @PathVariable UUID id,
            @RequestBody AccionObligacionRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String usuario = userDetails != null ? userDetails.getUsername() : "API_USER";
        return ResponseEntity.ok(obligacionService.suspenderObligacion(id, request, usuario));
    }

    @PostMapping("/{id}/rectificar")
    @PreAuthorize("hasAuthority('TRIBUTARIA_OBLIGACIONES_RECTIFICAR') or hasRole('ADMIN')")
    public ResponseEntity<ObligacionDetailResponse> rectificar(
            @PathVariable UUID id,
            @RequestBody AccionObligacionRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String usuario = userDetails != null ? userDetails.getUsername() : "API_USER";
        return ResponseEntity.ok(obligacionService.rectificarObligacion(id, request, usuario));
    }

    @PostMapping("/{id}/anular")
    @PreAuthorize("hasAuthority('TRIBUTARIA_OBLIGACIONES_ANULAR') or hasRole('ADMIN')")
    public ResponseEntity<ObligacionDetailResponse> anular(
            @PathVariable UUID id,
            @RequestBody AccionObligacionRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String usuario = userDetails != null ? userDetails.getUsername() : "API_USER";
        return ResponseEntity.ok(obligacionService.anularObligacion(id, request, usuario));
    }

    @GetMapping("/metricas")
    @PreAuthorize("hasAuthority('TRIBUTARIA_OBLIGACIONES_VER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> metricas() {
        return ResponseEntity.ok(obligacionService.obtenerMetricasGenerales());
    }
}
