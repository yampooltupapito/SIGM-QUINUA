package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.cajapago.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoCajaMunicipal;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.CajaMunicipalService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tributaria/cajas")
@RequiredArgsConstructor
public class CajaRestController {

    private final CajaMunicipalService cajaService;

    @GetMapping
    @PreAuthorize("hasAuthority('TRIBUTARIA_CAJA_VER') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Page<CajaResponse>> listarCajas(
            @RequestParam(value = "busqueda", required = false) String busqueda,
            @RequestParam(value = "estado", required = false) EstadoCajaMunicipal estado,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(cajaService.listarCajas(busqueda, estado, PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "codigo"))));
    }

    @GetMapping("/activas")
    @PreAuthorize("hasAuthority('TRIBUTARIA_CAJA_VER') or hasAnyRole('ADMIN', 'SUPERADMIN', 'CAJERO')")
    public ResponseEntity<List<CajaResponse>> listarActivas() {
        return ResponseEntity.ok(cajaService.listarCajasActivas());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('TRIBUTARIA_CAJA_VER') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<CajaResponse> obtenerPorId(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(cajaService.obtenerCajaPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('TRIBUTARIA_CAJA_ADMINISTRAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<CajaResponse> crearCaja(
            @Valid @RequestBody CrearCajaRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String username = userDetails != null ? userDetails.getUsername() : "API_USER";
        return ResponseEntity.status(HttpStatus.CREATED).body(cajaService.crearCaja(request, username));
    }

    @PostMapping("/sesion/abrir")
    @PreAuthorize("hasAuthority('TRIBUTARIA_CAJA_ABRIR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'CAJERO')")
    public ResponseEntity<SesionCajaResponse> abrirSesion(
            @Valid @RequestBody AbrirSesionCajaRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String username = userDetails != null ? userDetails.getUsername() : "API_USER";
        return ResponseEntity.status(HttpStatus.CREATED).body(cajaService.abrirSesion(request, username));
    }

    @PostMapping("/sesion/{id}/cerrar")
    @PreAuthorize("hasAuthority('TRIBUTARIA_CAJA_CERRAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'CAJERO')")
    public ResponseEntity<SesionCajaResponse> cerrarSesion(
            @PathVariable("id") UUID id,
            @Valid @RequestBody CerrarSesionCajaRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String username = userDetails != null ? userDetails.getUsername() : "API_USER";
        return ResponseEntity.ok(cajaService.cerrarSesion(id, request, username));
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasAuthority('TRIBUTARIA_CAJA_VER') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<DashboardCajaResponse> obtenerDashboard() {
        return ResponseEntity.ok(cajaService.obtenerMetricasDashboard());
    }
}
