package pe.gob.municipalidad.sigm.modules.serenazgo.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.PersonalSerenazgoService;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/serenazgo/personal")
@RequiredArgsConstructor
public class PersonalSerenazgoRestController {

    private final PersonalSerenazgoService personalService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('SERENAZGO_PERSONAL_VER', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_JEFE_OFICINA')")
    public ResponseEntity<Page<PersonalSerenazgoResponse>> listar(
            @ModelAttribute PersonalSerenazgoFilter filter,
            @PageableDefault(size = 15) Pageable pageable) {
        return ResponseEntity.ok(personalService.listar(filter, pageable));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('SERENAZGO_PERSONAL_CREAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public ResponseEntity<PersonalSerenazgoResponse> registrar(
            @Valid @RequestBody PersonalSerenazgoCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails != null ? userDetails.getUsername() : "admin";
        PersonalSerenazgoResponse creado = personalService.registrar(request, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_PERSONAL_VER', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_JEFE_OFICINA', 'ROLE_SERENO')")
    public ResponseEntity<PersonalSerenazgoDetailResponse> obtenerDetalle(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(personalService.obtenerDetalle(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_PERSONAL_EDITAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public ResponseEntity<PersonalSerenazgoResponse> actualizar(
            @PathVariable("id") UUID id,
            @Valid @RequestBody PersonalSerenazgoUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails != null ? userDetails.getUsername() : "admin";
        return ResponseEntity.ok(personalService.actualizar(id, request, username));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_PERSONAL_ESTADO_CAMBIAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public ResponseEntity<PersonalSerenazgoResponse> cambiarEstado(
            @PathVariable("id") UUID id,
            @Valid @RequestBody CambioEstadoPersonalRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails != null ? userDetails.getUsername() : "admin";
        return ResponseEntity.ok(personalService.cambiarEstado(id, request, username));
    }

    @PostMapping("/{id}/cambios-cargo")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_PERSONAL_CARGO_CAMBIAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public ResponseEntity<PersonalSerenazgoResponse> cambiarCargo(
            @PathVariable("id") UUID id,
            @Valid @RequestBody CambioCargoPersonalRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails != null ? userDetails.getUsername() : "admin";
        return ResponseEntity.ok(personalService.cambiarCargo(id, request, username));
    }

    @GetMapping("/{id}/historial")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_PERSONAL_HISTORIAL_VER', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_JEFE_OFICINA')")
    public ResponseEntity<List<HistorialCargoResponse>> obtenerHistorialCargos(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(personalService.obtenerHistorialCargos(id));
    }

    @GetMapping("/{id}/documentos")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_PERSONAL_DOCUMENTOS_VER', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_JEFE_OFICINA')")
    public ResponseEntity<List<DocumentoPersonalResponse>> obtenerDocumentos(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(personalService.obtenerDocumentos(id));
    }

    @GetMapping("/{id}/disponibilidad")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_PERSONAL_VER', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_JEFE_OFICINA', 'ROLE_SERENO')")
    public ResponseEntity<DisponibilidadPersonalResponse> verificarDisponibilidad(
            @PathVariable("id") UUID id,
            @RequestParam(name = "fecha", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(personalService.verificarDisponibilidad(id, fecha));
    }

    @GetMapping("/estadisticas")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_PERSONAL_ESTADISTICAS_VER', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_JEFE_OFICINA')")
    public ResponseEntity<EstadisticasPersonalResponse> obtenerEstadisticas() {
        return ResponseEntity.ok(personalService.obtenerEstadisticas());
    }

    @GetMapping("/{id}/ficha.pdf")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_PERSONAL_FICHA_EXPORTAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_JEFE_OFICINA')")
    public ResponseEntity<byte[]> descargarFichaPdf(@PathVariable("id") UUID id) {
        byte[] pdfBytes = personalService.generarFichaPdf(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"ficha_serenazgo_" + id + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
