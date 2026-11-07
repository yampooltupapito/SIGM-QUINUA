package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.ConceptoTributarioService;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/tributaria/conceptos")
@RequiredArgsConstructor
public class ConceptoRestController {

    private final ConceptoTributarioService conceptoService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_CONCEPTOS_VER', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_JEFE_OFICINA')")
    public ResponseEntity<Page<ConceptoSummaryDto>> listarConceptos(ConceptoFilterDto filtro, Pageable pageable) {
        return ResponseEntity.ok(conceptoService.listarConceptos(filtro, pageable));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_CONCEPTOS_CREAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public ResponseEntity<ConceptoDetailResponse> registrarConcepto(@Valid @RequestBody ConceptoCreateRequest request,
                                                                    Principal principal) {
        String usuario = principal != null ? principal.getName() : "SISTEMA";
        return ResponseEntity.status(HttpStatus.CREATED).body(conceptoService.registrarConcepto(request, usuario));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_CONCEPTOS_VER', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_JEFE_OFICINA')")
    public ResponseEntity<ConceptoDetailResponse> obtenerConcepto(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(conceptoService.obtenerConceptoPorId(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_CONCEPTOS_EDITAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public ResponseEntity<ConceptoDetailResponse> actualizarConcepto(@PathVariable("id") UUID id,
                                                                     @Valid @RequestBody ConceptoUpdateRequest request,
                                                                     Principal principal) {
        String usuario = principal != null ? principal.getName() : "SISTEMA";
        return ResponseEntity.ok(conceptoService.actualizarConcepto(id, request, usuario));
    }

    @PostMapping("/{id}/configuraciones")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_CONCEPTOS_CONFIGURAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public ResponseEntity<ConfiguracionDetailResponse> crearConfiguracion(@PathVariable("id") UUID conceptoId,
                                                                          @Valid @RequestBody ConfiguracionCreateRequest request,
                                                                          Principal principal) {
        request.setConceptoId(conceptoId);
        String usuario = principal != null ? principal.getName() : "SISTEMA";
        return ResponseEntity.status(HttpStatus.CREATED).body(conceptoService.crearConfiguracion(request, usuario));
    }

    @GetMapping("/configuraciones/{cid}")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_CONCEPTOS_VER', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_JEFE_OFICINA')")
    public ResponseEntity<ConfiguracionDetailResponse> obtenerConfiguracion(@PathVariable("cid") UUID cid) {
        return ResponseEntity.ok(conceptoService.obtenerConfiguracionPorId(cid));
    }

    @PostMapping("/configuraciones/{cid}/parametros")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_CONCEPTOS_CONFIGURAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public ResponseEntity<ConfiguracionDetailResponse> agregarParametro(@PathVariable("cid") UUID cid,
                                                                        @Valid @RequestBody ParametroCreateRequest request,
                                                                        Principal principal) {
        String usuario = principal != null ? principal.getName() : "SISTEMA";
        return ResponseEntity.ok(conceptoService.agregarParametro(cid, request, usuario));
    }

    @DeleteMapping("/configuraciones/{cid}/parametros/{pid}")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_CONCEPTOS_CONFIGURAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public ResponseEntity<ConfiguracionDetailResponse> eliminarParametro(@PathVariable("cid") UUID cid,
                                                                         @PathVariable("pid") UUID pid,
                                                                         Principal principal) {
        String usuario = principal != null ? principal.getName() : "SISTEMA";
        return ResponseEntity.ok(conceptoService.eliminarParametro(cid, pid, usuario));
    }

    @PostMapping("/configuraciones/{cid}/revision")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_CONCEPTOS_REVISAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public ResponseEntity<ConfiguracionDetailResponse> enviarARevision(@PathVariable("cid") UUID cid,
                                                                       @RequestBody(required = false) CambioEstadoConfiguracionRequest request,
                                                                       Principal principal) {
        String usuario = principal != null ? principal.getName() : "SISTEMA";
        String motivo = request != null ? request.getMotivo() : null;
        return ResponseEntity.ok(conceptoService.enviarARevision(cid, motivo, usuario));
    }

    @PostMapping("/configuraciones/{cid}/aprobar")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_CONCEPTOS_APROBAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public ResponseEntity<ConfiguracionDetailResponse> aprobarConfiguracion(@PathVariable("cid") UUID cid,
                                                                            @Valid @RequestBody CambioEstadoConfiguracionRequest request,
                                                                            Principal principal) {
        String usuario = principal != null ? principal.getName() : "SISTEMA";
        return ResponseEntity.ok(conceptoService.aprobarConfiguracion(cid, request.getResolucionSustento(), request.getMotivo(), usuario));
    }

    @PostMapping("/configuraciones/{cid}/activar")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_CONCEPTOS_ACTIVAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public ResponseEntity<ConfiguracionDetailResponse> activarConfiguracion(@PathVariable("cid") UUID cid,
                                                                            @RequestBody(required = false) CambioEstadoConfiguracionRequest request,
                                                                            Principal principal) {
        String usuario = principal != null ? principal.getName() : "SISTEMA";
        String motivo = request != null ? request.getMotivo() : null;
        return ResponseEntity.ok(conceptoService.activarConfiguracion(cid, motivo, usuario));
    }

    @PostMapping("/configuraciones/{cid}/desactivar")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_CONCEPTOS_DESACTIVAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public ResponseEntity<ConfiguracionDetailResponse> desactivarConfiguracion(@PathVariable("cid") UUID cid,
                                                                               @Valid @RequestBody CambioEstadoConfiguracionRequest request,
                                                                               Principal principal) {
        String usuario = principal != null ? principal.getName() : "SISTEMA";
        return ResponseEntity.ok(conceptoService.desactivarConfiguracion(cid, request.getMotivo(), usuario));
    }

    @PostMapping("/configuraciones/{cid}/nueva-version")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_CONCEPTOS_CONFIGURAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public ResponseEntity<ConfiguracionDetailResponse> generarNuevaVersion(@PathVariable("cid") UUID cid,
                                                                           @RequestBody(required = false) CambioEstadoConfiguracionRequest request,
                                                                           Principal principal) {
        String usuario = principal != null ? principal.getName() : "SISTEMA";
        String motivo = request != null ? request.getMotivo() : null;
        return ResponseEntity.status(HttpStatus.CREATED).body(conceptoService.crearNuevaVersion(cid, motivo, usuario));
    }

    @GetMapping("/configuraciones/comparar")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_CONCEPTOS_VER_HISTORIAL', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public ResponseEntity<ComparacionVersionesResponse> compararVersiones(@RequestParam("versionBaseId") UUID baseId,
                                                                          @RequestParam("versionComparadaId") UUID compId) {
        return ResponseEntity.ok(conceptoService.compararVersiones(baseId, compId));
    }

    @GetMapping("/estadisticas")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_CONCEPTOS_VER', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_JEFE_OFICINA')")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas() {
        return ResponseEntity.ok(conceptoService.obtenerEstadisticas());
    }
}
