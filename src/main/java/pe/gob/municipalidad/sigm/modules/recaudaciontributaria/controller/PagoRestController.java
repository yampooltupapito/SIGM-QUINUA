package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.cajapago.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoPagoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.MedioPagoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.PagoTributarioService;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tributaria/pagos")
@RequiredArgsConstructor
public class PagoRestController {

    private final PagoTributarioService pagoService;

    @GetMapping
    @PreAuthorize("hasAuthority('TRIBUTARIA_PAGOS_VER') or hasAnyRole('ADMIN', 'SUPERADMIN', 'CAJERO')")
    public ResponseEntity<Page<PagoResponse>> listarPagos(
            @RequestParam(value = "busqueda", required = false) String busqueda,
            @RequestParam(value = "contribuyenteId", required = false) UUID contribuyenteId,
            @RequestParam(value = "sesionCajaId", required = false) UUID sesionCajaId,
            @RequestParam(value = "medioPago", required = false) MedioPagoTributario medioPago,
            @RequestParam(value = "estado", required = false) EstadoPagoTributario estado,
            @RequestParam(value = "desde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime desde,
            @RequestParam(value = "hasta", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime hasta,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        Page<PagoResponse> response = pagoService.listarPagos(
                busqueda, contribuyenteId, sesionCajaId, medioPago, estado, desde, hasta,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "fechaHoraPago"))
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('TRIBUTARIA_PAGOS_VER') or hasAnyRole('ADMIN', 'SUPERADMIN', 'CAJERO')")
    public ResponseEntity<PagoResponse> obtenerPorId(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(pagoService.obtenerPorId(id));
    }

    @GetMapping("/contribuyente/{id}/obligaciones")
    @PreAuthorize("hasAuthority('TRIBUTARIA_PAGOS_REGISTRAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'CAJERO')")
    public ResponseEntity<List<ObligacionPagoItemResponse>> obtenerObligaciones(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(pagoService.buscarObligacionesParaCobro(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('TRIBUTARIA_PAGOS_REGISTRAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'CAJERO')")
    public ResponseEntity<PagoResponse> registrarPago(
            @Valid @RequestBody RegistrarPagoRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String username = userDetails != null ? userDetails.getUsername() : "API_USER";
        PagoResponse response = pagoService.registrarPago(request, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/confirmar")
    @PreAuthorize("hasAuthority('TRIBUTARIA_PAGOS_CONFIRMAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<PagoResponse> confirmarPago(
            @PathVariable("id") UUID id,
            @RequestBody(required = false) ConfirmarPagoRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String username = userDetails != null ? userDetails.getUsername() : "API_USER";
        return ResponseEntity.ok(pagoService.confirmarPago(id, request, username));
    }

    @PostMapping("/{id}/revertir")
    @PreAuthorize("hasAuthority('TRIBUTARIA_PAGOS_REVERTIR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<PagoResponse> revertirPago(
            @PathVariable("id") UUID id,
            @Valid @RequestBody RevertirPagoRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String username = userDetails != null ? userDetails.getUsername() : "API_USER";
        return ResponseEntity.ok(pagoService.revertirPago(id, request, username));
    }
}
