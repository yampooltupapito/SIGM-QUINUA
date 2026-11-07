package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request.CalculoPredialCreateRequest;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request.CalculoPredialFilterDto;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request.CambioEstadoCalculoRequest;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request.SimulacionPredialRequest;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response.CalculoPredialDetailResponse;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response.CalculoPredialSummaryDto;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response.HistorialCalculoResponse;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.CalculoPredialService;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.ImpuestoPredialExportService;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/tributaria/impuesto-predial")
@RequiredArgsConstructor
public class ImpuestoPredialRestController {

    private final CalculoPredialService calculoService;
    private final ImpuestoPredialExportService exportService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_PREDIAL_VER', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public ResponseEntity<Page<CalculoPredialSummaryDto>> listar(
            @ModelAttribute CalculoPredialFilterDto filtro,
            @PageableDefault(size = 10, sort = "fechaCalculo", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(calculoService.listarCalculos(filtro, pageable));
    }

    @GetMapping("/estadisticas")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_PREDIAL_VER', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public ResponseEntity<Map<String, Object>> estadisticas() {
        return ResponseEntity.ok(calculoService.obtenerEstadisticas());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_PREDIAL_VER', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public ResponseEntity<CalculoPredialDetailResponse> obtenerPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(calculoService.obtenerCalculoPorId(id));
    }

    @PostMapping("/simular")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_PREDIAL_SIMULAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public ResponseEntity<CalculoPredialDetailResponse> simular(
            @Valid @RequestBody SimulacionPredialRequest request,
            Principal principal) {
        String username = principal != null ? principal.getName() : "API_USER";
        return ResponseEntity.ok(calculoService.simularCalculo(request, username));
    }

    @PostMapping("/preliminar")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_PREDIAL_CALCULAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public ResponseEntity<CalculoPredialDetailResponse> guardarPreliminar(
            @Valid @RequestBody CalculoPredialCreateRequest request,
            Principal principal) {
        String username = principal != null ? principal.getName() : "API_USER";
        return new ResponseEntity<>(calculoService.registrarCalculo(request, username), HttpStatus.CREATED);
    }

    @PostMapping("/{id}/revision")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_PREDIAL_REVISAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public ResponseEntity<CalculoPredialDetailResponse> enviarARevision(
            @PathVariable UUID id,
            @RequestBody CambioEstadoCalculoRequest request,
            Principal principal) {
        String username = principal != null ? principal.getName() : "API_USER";
        return ResponseEntity.ok(calculoService.enviarARevision(id, request != null ? request.getMotivo() : "Pase a revisión", username));
    }

    @PostMapping("/{id}/observaciones")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_PREDIAL_REVISAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public ResponseEntity<CalculoPredialDetailResponse> registrarObservaciones(
            @PathVariable UUID id,
            @RequestBody CambioEstadoCalculoRequest request,
            Principal principal) {
        String username = principal != null ? principal.getName() : "API_USER";
        return ResponseEntity.ok(calculoService.registrarObservaciones(id, request != null ? request.getMotivo() : "Observaciones técnicas", username));
    }

    @PostMapping("/{id}/aprobar")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_PREDIAL_APROBAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public ResponseEntity<CalculoPredialDetailResponse> aprobar(
            @PathVariable UUID id,
            @RequestBody CambioEstadoCalculoRequest request,
            Principal principal) {
        String username = principal != null ? principal.getName() : "API_USER";
        String resolucion = (request != null && request.getResolucionSustento() != null) ? request.getResolucionSustento() : "RESOLUCION-PREDIAL-AUTORIZADA";
        String motivo = (request != null && request.getMotivo() != null) ? request.getMotivo() : "Aprobación formal de cálculo predial";
        return ResponseEntity.ok(calculoService.aprobarCalculo(id, resolucion, motivo, username));
    }

    @PostMapping("/{id}/anular")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_PREDIAL_ANULAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public ResponseEntity<CalculoPredialDetailResponse> anular(
            @PathVariable UUID id,
            @RequestBody CambioEstadoCalculoRequest request,
            Principal principal) {
        String username = principal != null ? principal.getName() : "API_USER";
        return ResponseEntity.ok(calculoService.anularCalculo(id, request != null ? request.getMotivo() : "Anulación formal", username));
    }

    @PostMapping("/{id}/recalcular")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_PREDIAL_CALCULAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public ResponseEntity<CalculoPredialDetailResponse> recalcular(
            @PathVariable UUID id,
            @RequestParam String motivo,
            Principal principal) {
        String username = principal != null ? principal.getName() : "API_USER";
        return ResponseEntity.ok(calculoService.recalcularNuevaVersion(id, motivo, username));
    }

    @GetMapping("/{id}/historial")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_PREDIAL_VER_HISTORIAL', 'TRIBUTARIA_PREDIAL_VER', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public ResponseEntity<List<HistorialCalculoResponse>> listarHistorial(@PathVariable UUID id) {
        return ResponseEntity.ok(calculoService.obtenerHistorial(id));
    }

    @GetMapping("/exportar/excel")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_PREDIAL_EXPORTAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public ResponseEntity<byte[]> exportarExcel(@ModelAttribute CalculoPredialFilterDto filtro) {
        byte[] contenido = exportService.exportarExcel(filtro);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"determinaciones_prediales.xlsx\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(contenido);
    }

    @GetMapping("/exportar/pdf")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_PREDIAL_EXPORTAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public ResponseEntity<byte[]> exportarPdf(@ModelAttribute CalculoPredialFilterDto filtro) {
        byte[] contenido = exportService.exportarPdf(filtro);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"reporte_determinaciones_prediales.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(contenido);
    }

    @GetMapping("/{id}/pdf")
    @PreAuthorize("hasAnyAuthority('TRIBUTARIA_PREDIAL_EXPORTAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public ResponseEntity<byte[]> exportarHojaLiquidacion(@PathVariable UUID id) {
        byte[] contenido = exportService.exportarHojaLiquidacionPdf(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"hoja_liquidacion_predial.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(contenido);
    }
}
