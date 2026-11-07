package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request.PredioFilterDto;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response.PredioDetailResponse;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response.PredioSummaryDto;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response.RelacionContribuyenteResponse;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.PredioTributarioRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.PredioService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Controlador REST institucional para operaciones asíncronas y consultas del subsistema de predios.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/tributaria/predios")
@RequiredArgsConstructor
public class PredioRestController {

    private final PredioService predioService;
    private final PredioTributarioRepository predioRepository;

    @GetMapping("/buscar")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_PREDIOS_VER', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_JEFE_OFICINA')")
    public ResponseEntity<Page<PredioSummaryDto>> buscarPredios(
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        PredioFilterDto filtro = PredioFilterDto.builder().query(query).build();
        Page<PredioSummaryDto> resultado = predioService.listarPredios(filtro, PageRequest.of(page, size, Sort.by("codigoPredio").ascending()));
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_PREDIOS_VER', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_JEFE_OFICINA')")
    public ResponseEntity<PredioDetailResponse> obtenerDetalle(@PathVariable UUID id) {
        PredioDetailResponse predio = predioService.obtenerPredioPorId(id);
        return ResponseEntity.ok(predio);
    }

    @PostMapping("/validar-catastral")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_PREDIOS_VER', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_JEFE_OFICINA')")
    public ResponseEntity<Map<String, Object>> validarCodigoCatastral(@RequestBody Map<String, String> payload) {
        String codigoCatastral = payload.get("codigoCatastral");
        boolean existe = false;
        if (codigoCatastral != null && !codigoCatastral.trim().isEmpty()) {
            existe = predioRepository.existsByCodigoCatastralIgnoreCase(codigoCatastral.trim());
        }
        return ResponseEntity.ok(Map.of(
                "codigoCatastral", codigoCatastral != null ? codigoCatastral : "",
                "duplicado", existe
        ));
    }

    @GetMapping("/contribuyente/{contribuyenteId}")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_PREDIOS_VER', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_JEFE_OFICINA')")
    public ResponseEntity<List<RelacionContribuyenteResponse>> listarPorContribuyente(@PathVariable UUID contribuyenteId) {
        List<RelacionContribuyenteResponse> lista = predioService.listarPrediosPorContribuyente(contribuyenteId);
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/estadisticas")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_PREDIOS_VER', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_JEFE_OFICINA')")
    public ResponseEntity<Map<String, Long>> obtenerEstadisticas() {
        return ResponseEntity.ok(predioService.obtenerEstadisticas());
    }
}
