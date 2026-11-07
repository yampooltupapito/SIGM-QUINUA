package pe.gob.municipalidad.sigm.modules.serenazgo.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoEntidadTerritorial;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.SectorPatrullajeService;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.TerritorioSeguridadService;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.ZonaPatrullajeService;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/serenazgo/territorio")
@RequiredArgsConstructor
@Slf4j
public class TerritorioRestController {

    private final SectorPatrullajeService sectorService;
    private final ZonaPatrullajeService zonaService;
    private final TerritorioSeguridadService territorioService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_SECTORES_VER', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_SERENO')")
    public ResponseEntity<DashboardTerritorialResponse> obtenerDashboard() {
        return ResponseEntity.ok(territorioService.obtenerDashboardTerritorial());
    }

    @GetMapping("/mapa")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_SECTORES_VER', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_SERENO')")
    public ResponseEntity<GeoJsonFeatureCollectionResponse> obtenerMapaCompleto() {
        return ResponseEntity.ok(territorioService.obtenerMapaTerritorialCompleto());
    }

    @PostMapping("/sectores/{id}/estado")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_SECTORES_CAMBIAR_ESTADO', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public ResponseEntity<SectorResponse> cambiarEstadoSector(
            @PathVariable UUID id,
            @Valid @RequestBody CambioEstadoTerritorioRequest request,
            Principal principal) {
        String username = principal != null ? principal.getName() : "admin";
        return ResponseEntity.ok(sectorService.cambiarEstado(id, request, username));
    }

    @PostMapping("/sectores/{id}/delimitacion")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_GEOGRAFIA_EDITAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public ResponseEntity<SectorResponse> guardarDelimitacionSector(
            @PathVariable UUID id,
            @Valid @RequestBody DelimitacionGeoJsonRequest request,
            Principal principal) {
        String username = principal != null ? principal.getName() : "admin";
        return ResponseEntity.ok(sectorService.guardarDelimitacion(id, request, username));
    }

    @PostMapping("/zonas/{id}/estado")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_ZONAS_EDITAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public ResponseEntity<ZonaPatrullajeResponse> cambiarEstadoZona(
            @PathVariable UUID id,
            @Valid @RequestBody CambioEstadoTerritorioRequest request,
            Principal principal) {
        String username = principal != null ? principal.getName() : "admin";
        return ResponseEntity.ok(zonaService.cambiarEstado(id, request, username));
    }

    @PostMapping("/zonas/{id}/reasignar")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_ZONAS_REASIGNAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public ResponseEntity<ZonaPatrullajeResponse> reasignarSectorZona(
            @PathVariable UUID id,
            @Valid @RequestBody ReasignarZonaRequest request,
            Principal principal) {
        String username = principal != null ? principal.getName() : "admin";
        return ResponseEntity.ok(zonaService.reasignarSector(id, request, username));
    }

    @PostMapping("/zonas/{id}/delimitacion")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_GEOGRAFIA_EDITAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public ResponseEntity<ZonaPatrullajeResponse> guardarDelimitacionZona(
            @PathVariable UUID id,
            @Valid @RequestBody DelimitacionGeoJsonRequest request,
            Principal principal) {
        String username = principal != null ? principal.getName() : "admin";
        return ResponseEntity.ok(zonaService.guardarDelimitacion(id, request, username));
    }

    @PostMapping("/validar-geometria")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_GEOGRAFIA_EDITAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public ResponseEntity<ValidacionGeoJsonResult> validarGeometria(@RequestBody Map<String, String> payload) {
        String geojson = payload.get("geometriaGeojson");
        return ResponseEntity.ok(territorioService.validarGeometria(geojson));
    }

    @PostMapping("/importar-geojson")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_GEOGRAFIA_IMPORTAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public ResponseEntity<Map<String, String>> importarGeoJson(
            @Valid @RequestBody ImportarGeoJsonRequest request,
            Principal principal) {
        String username = principal != null ? principal.getName() : "admin";
        territorioService.importarGeoJson(request, username);
        return ResponseEntity.ok(Map.of("mensaje", "Capa GeoJSON importada y asignada exitosamente"));
    }

    @GetMapping("/exportar-geojson")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_GEOGRAFIA_EXPORTAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public ResponseEntity<String> exportarGeoJson(
            @RequestParam TipoEntidadTerritorial tipo,
            @RequestParam UUID id) {
        String geoJson = territorioService.exportarGeoJson(tipo, id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + tipo.name().toLowerCase() + "_" + id + ".geojson\"")
                .contentType(MediaType.APPLICATION_JSON)
                .body(geoJson);
    }

    @GetMapping("/exportar-excel")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_GEOGRAFIA_EXPORTAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public ResponseEntity<ByteArrayResource> exportarExcel(
            @ModelAttribute SectorFilter sectorFilter,
            @ModelAttribute ZonaFilter zonaFilter) {
        byte[] bytesExcel = territorioService.exportarExcel(sectorFilter, zonaFilter);
        ByteArrayResource resource = new ByteArrayResource(bytesExcel);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"territorio_serenazgo.xlsx\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .contentLength(bytesExcel.length)
                .body(resource);
    }
}
