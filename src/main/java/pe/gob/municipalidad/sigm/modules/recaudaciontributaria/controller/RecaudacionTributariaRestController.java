package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.RecaudacionTributariaService;

@RestController
@RequestMapping("/api/v1/tributaria")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('TRIBUTARIA_MODULO_VER', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_CAJERO', 'ROLE_JEFE_OFICINA')")
public class RecaudacionTributariaRestController {

    private final RecaudacionTributariaService tributariaService;

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardTributarioResponse> obtenerDashboard() {
        return ResponseEntity.ok(tributariaService.obtenerDashboard());
    }

    @GetMapping("/contribuyentes")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_CONTRIBUYENTES_VER', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_JEFE_OFICINA')")
    public ResponseEntity<Page<ContribuyenteSummaryDto>> listarContribuyentes(
            @RequestParam(value = "q", required = false) String q,
            Pageable pageable) {
        return ResponseEntity.ok(tributariaService.listarContribuyentes(q, pageable));
    }

    @GetMapping("/predios")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_PREDIOS_VER', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_JEFE_OFICINA')")
    public ResponseEntity<Page<PredioSummaryDto>> listarPredios(
            @RequestParam(value = "q", required = false) String q,
            Pageable pageable) {
        return ResponseEntity.ok(tributariaService.listarPredios(q, pageable));
    }



    @GetMapping("/periodos")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_CONCEPTOS_VER', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_JEFE_OFICINA')")
    public ResponseEntity<Page<PeriodoSummaryDto>> listarPeriodos(Pageable pageable) {
        return ResponseEntity.ok(tributariaService.listarPeriodos(pageable));
    }
}
