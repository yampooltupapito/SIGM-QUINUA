package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.CuentaCorrienteDetailResponse;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.CuentaCorrienteSummaryDto;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.MovimientoResponse;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoCuentaCorriente;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.CuentaCorrienteTributariaService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tributaria/cuentas-corrientes")
@RequiredArgsConstructor
public class CuentaCorrienteRestController {

    private final CuentaCorrienteTributariaService cuentaService;

    @GetMapping
    @PreAuthorize("hasAuthority('TRIBUTARIA_CUENTA_CORRIENTE_VER') or hasRole('ADMIN')")
    public ResponseEntity<Page<CuentaCorrienteSummaryDto>> listar(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) EstadoCuentaCorriente estado,
            @RequestParam(required = false) Boolean conDeuda,
            Pageable pageable
    ) {
        return ResponseEntity.ok(cuentaService.listarCuentas(query, estado, conDeuda, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('TRIBUTARIA_CUENTA_CORRIENTE_VER') or hasRole('ADMIN')")
    public ResponseEntity<CuentaCorrienteDetailResponse> obtenerPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(cuentaService.obtenerPorId(id));
    }

    @GetMapping("/{id}/movimientos")
    @PreAuthorize("hasAuthority('TRIBUTARIA_CUENTA_CORRIENTE_VER_MOVIMIENTOS') or hasRole('ADMIN')")
    public ResponseEntity<Page<MovimientoResponse>> listarMovimientos(@PathVariable UUID id, Pageable pageable) {
        return ResponseEntity.ok(cuentaService.listarMovimientos(id, pageable));
    }

    @GetMapping("/contribuyente/{contribuyenteId}")
    @PreAuthorize("hasAuthority('TRIBUTARIA_CUENTA_CORRIENTE_VER') or hasRole('ADMIN')")
    public ResponseEntity<CuentaCorrienteDetailResponse> obtenerPorContribuyente(@PathVariable UUID contribuyenteId) {
        return ResponseEntity.ok(cuentaService.obtenerPorContribuyenteId(contribuyenteId));
    }
}
