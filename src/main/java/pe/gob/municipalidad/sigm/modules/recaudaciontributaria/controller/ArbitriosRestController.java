package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request.ArbitrioFilterDto;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request.CalculoArbitrioCreateRequest;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request.CambioEstadoArbitrioRequest;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request.SimulacionArbitrioRequest;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response.ArbitrioDetailResponse;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response.ArbitrioSummaryDto;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.ArbitrioMunicipalService;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/tributaria/arbitrios")
@RequiredArgsConstructor
public class ArbitriosRestController {

    private final ArbitrioMunicipalService arbitrioService;

    @GetMapping("/calculos")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_ARBITRIOS_VER', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_JEFE_OFICINA')")
    public ResponseEntity<Page<ArbitrioSummaryDto>> listarCalculos(
            @ModelAttribute ArbitrioFilterDto filtro,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Page<ArbitrioSummaryDto> calculos = arbitrioService.listarCalculos(
                filtro,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "fechaCalculo"))
        );
        return ResponseEntity.ok(calculos);
    }

    @GetMapping("/calculos/{id}")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_ARBITRIOS_VER', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_JEFE_OFICINA')")
    public ResponseEntity<ArbitrioDetailResponse> obtenerDetalleCalculo(@PathVariable UUID id) {
        ArbitrioDetailResponse response = arbitrioService.obtenerCalculoPorId(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/simular")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_ARBITRIOS_SIMULAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_JEFE_OFICINA')")
    public ResponseEntity<ArbitrioDetailResponse> simularCalculo(
            @Valid @RequestBody SimulacionArbitrioRequest request,
            Principal principal) {
        String username = principal != null ? principal.getName() : "SISTEMA";
        ArbitrioDetailResponse resultado = arbitrioService.simularCalculo(request, username);
        return ResponseEntity.ok(resultado);
    }

    @PostMapping("/calculos")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_ARBITRIOS_CALCULAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_JEFE_OFICINA')")
    public ResponseEntity<ArbitrioDetailResponse> crearCalculoPreliminar(
            @Valid @RequestBody CalculoArbitrioCreateRequest request,
            Principal principal) {
        String username = principal != null ? principal.getName() : "SISTEMA";
        ArbitrioDetailResponse resultado = arbitrioService.crearCalculoPreliminar(request, username);
        return ResponseEntity.ok(resultado);
    }

    @PostMapping("/calculos/{id}/aprobar")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_ARBITRIOS_APROBAR_CALCULO', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_JEFE_OFICINA')")
    public ResponseEntity<ArbitrioDetailResponse> aprobarCalculo(
            @PathVariable UUID id,
            Principal principal) {
        String username = principal != null ? principal.getName() : "SISTEMA";
        ArbitrioDetailResponse resultado = arbitrioService.aprobarCalculo(id, username);
        return ResponseEntity.ok(resultado);
    }

    @PostMapping("/calculos/{id}/anular")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_ARBITRIOS_ANULAR_CALCULO', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_JEFE_OFICINA')")
    public ResponseEntity<ArbitrioDetailResponse> anularCalculo(
            @PathVariable UUID id,
            @RequestBody Map<String, String> payload,
            Principal principal) {
        String motivo = payload.getOrDefault("motivo", "Anulación solicitada por funcionario autorizado");
        String username = principal != null ? principal.getName() : "SISTEMA";
        ArbitrioDetailResponse resultado = arbitrioService.anularCalculo(id, motivo, username);
        return ResponseEntity.ok(resultado);
    }

    @PostMapping("/calculos/{id}/cambiar-estado")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_ARBITRIOS_REVISAR_CALCULO', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_JEFE_OFICINA')")
    public ResponseEntity<ArbitrioDetailResponse> cambiarEstado(
            @PathVariable UUID id,
            @Valid @RequestBody CambioEstadoArbitrioRequest request,
            Principal principal) {
        String username = principal != null ? principal.getName() : "SISTEMA";
        ArbitrioDetailResponse resultado = arbitrioService.cambiarEstado(id, request, username);
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/estadisticas")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_ARBITRIOS_VER', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_JEFE_OFICINA')")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas() {
        return ResponseEntity.ok(arbitrioService.obtenerEstadisticas());
    }
}
