package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.deuda.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoExigibilidadObligacion;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoObligacionTributaria;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TramoAntiguedadDeuda;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.DeudaTributariaService;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.InteresMoratorioService;
import pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tributaria/deudas")
@RequiredArgsConstructor
public class DeudaTributariaRestController {

    private final DeudaTributariaService deudaService;
    private final InteresMoratorioService interesService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasAuthority('TRIBUTARIA_DEUDAS_VER') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_TRIBUTACION')")
    public ResponseEntity<DashboardDeudasResponse> obtenerDashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaCorte) {
        return ResponseEntity.ok(deudaService.obtenerDashboardDeudas(fechaCorte));
    }

    @GetMapping("/cartera")
    @PreAuthorize("hasAuthority('TRIBUTARIA_DEUDAS_VER') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_TRIBUTACION')")
    public ResponseEntity<Page<CarteraItemDto>> listarCartera(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) UUID contribuyenteId,
            @RequestParam(required = false) UUID predioId,
            @RequestParam(required = false) UUID conceptoId,
            @RequestParam(required = false) UUID periodoId,
            @RequestParam(required = false) EstadoObligacionTributaria estado,
            @RequestParam(required = false) EstadoExigibilidadObligacion exigibilidad,
            @RequestParam(required = false, defaultValue = "true") Boolean soloConSaldo,
            @RequestParam(required = false) TramoAntiguedadDeuda tramo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaCorte,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<CarteraItemDto> resultado = deudaService.listarCartera(
                query, contribuyenteId, predioId, conceptoId, periodoId,
                estado, exigibilidad, soloConSaldo, fechaCorte, tramo,
                PageRequest.of(page, size, Sort.by("fechaVencimiento").ascending()));

        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('TRIBUTARIA_DEUDAS_VER_DETALLE') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_TRIBUTACION')")
    public ResponseEntity<DeudaDetalleResponse> obtenerFicha(
            @PathVariable UUID id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaCorte) {
        return ResponseEntity.ok(deudaService.obtenerFichaDeuda(id, fechaCorte));
    }

    @PostMapping("/{id}/clasificar")
    @PreAuthorize("hasAuthority('TRIBUTARIA_DEUDAS_CLASIFICAR') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_TRIBUTACION')")
    public ResponseEntity<DeudaDetalleResponse> clasificar(
            @PathVariable UUID id,
            @Valid @RequestBody ClasificarDeudaRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(deudaService.clasificarObligacion(id, request, userDetails != null ? userDetails.getUsername() : "SYSTEM"));
    }

    @PostMapping("/{id}/asignar")
    @PreAuthorize("hasAuthority('TRIBUTARIA_DEUDAS_ASIGNAR') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_TRIBUTACION')")
    public ResponseEntity<DeudaDetalleResponse> asignar(
            @PathVariable UUID id,
            @Valid @RequestBody AsignarResponsableRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(deudaService.asignarResponsable(id, request, userDetails != null ? userDetails.getUsername() : "SYSTEM"));
    }

    @PostMapping("/{id}/gestiones")
    @PreAuthorize("hasAuthority('TRIBUTARIA_DEUDAS_REGISTRAR_GESTION') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_TRIBUTACION')")
    public ResponseEntity<DeudaDetalleResponse> registrarGestion(
            @PathVariable UUID id,
            @Valid @RequestBody RegistrarGestionDeudaRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(deudaService.registrarGestion(id, request, userDetails != null ? userDetails.getUsername() : "SYSTEM"));
    }

    @PostMapping("/{id}/compromisos")
    @PreAuthorize("hasAuthority('TRIBUTARIA_DEUDAS_REGISTRAR_COMPROMISO') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_TRIBUTACION', 'ROLE_CAJERO')")
    public ResponseEntity<DeudaDetalleResponse> registrarCompromiso(
            @PathVariable UUID id,
            @Valid @RequestBody RegistrarCompromisoDeudaRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(deudaService.registrarCompromiso(id, request, userDetails != null ? userDetails.getUsername() : "SYSTEM"));
    }

    @PostMapping("/intereses/simular")
    @PreAuthorize("hasAuthority('TRIBUTARIA_DEUDAS_CALCULAR_INTERESES') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_TRIBUTACION', 'ROLE_CAJERO')")
    public ResponseEntity<SimulacionInteresResponse> simularInteres(
            @RequestBody SimulacionInteresRequest request) {
        return ResponseEntity.ok(interesService.simularInteresGenerico(request));
    }

    @GetMapping("/intereses/parametros")
    @PreAuthorize("hasAuthority('TRIBUTARIA_DEUDAS_REVISAR_INTERESES') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_TRIBUTACION')")
    public ResponseEntity<List<ParametroInteresResponse>> listarParametros() {
        return ResponseEntity.ok(interesService.listarParametros());
    }

    @PostMapping("/intereses/parametros")
    @PreAuthorize("hasAuthority('TRIBUTARIA_DEUDAS_CONFIGURAR_INTERESES') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public ResponseEntity<ParametroInteresResponse> crearParametro(
            @Valid @RequestBody ParametroInteresRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(interesService.crearParametro(request, userDetails != null ? userDetails.getUsername() : "SYSTEM"));
    }
}
