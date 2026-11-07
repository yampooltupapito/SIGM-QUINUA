package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request.ContribuyenteFilterDto;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response.ContribuyenteDetailResponse;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response.ContribuyenteSummaryDto;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response.DuplicateCheckResponse;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.ContribuyenteService;

import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/tributaria/contribuyentes")
@RequiredArgsConstructor
public class ContribuyenteRestController {

    private final ContribuyenteService contribuyenteService;

    @PostMapping("/verificar-duplicado")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_CONTRIBUYENTES_VER', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public ResponseEntity<DuplicateCheckResponse> verificarDuplicado(
            @RequestParam(value = "tipoDocumento", required = false) String tipoDocumento,
            @RequestParam(value = "numeroDocumento", required = false) String numeroDocumento,
            @RequestParam(value = "nombreRazonSocial", required = false) String nombreRazonSocial,
            @RequestParam(value = "excludeId", required = false) UUID excludeId) {

        log.debug("Verificando duplicado para doc={}-{}, nombre={}", tipoDocumento, numeroDocumento, nombreRazonSocial);
        DuplicateCheckResponse res = contribuyenteService.verificarDuplicados(tipoDocumento, numeroDocumento, nombreRazonSocial, excludeId);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_CONTRIBUYENTES_VER', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public ResponseEntity<ContribuyenteDetailResponse> obtenerPorId(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(contribuyenteService.obtenerDetalle(id));
    }

    @GetMapping("/buscar")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_CONTRIBUYENTES_VER', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public ResponseEntity<Page<ContribuyenteSummaryDto>> buscar(
            @RequestParam(value = "q", required = false) String query,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        ContribuyenteFilterDto filtro = ContribuyenteFilterDto.builder().query(query).build();
        return ResponseEntity.ok(contribuyenteService.listarConFiltros(filtro, PageRequest.of(page, size, Sort.by("codigoContribuyente"))));
    }

    @GetMapping("/estadisticas")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_CONTRIBUYENTES_VER', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public ResponseEntity<Map<String, Object>> estadisticas() {
        return ResponseEntity.ok(contribuyenteService.obtenerEstadisticas());
    }
}
