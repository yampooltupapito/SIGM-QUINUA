package pe.gob.municipalidad.sigm.modules.serenazgo.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.GeoJsonFeatureCollectionResponse;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.mapa.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.MapaGeograficoSerenazgoService;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/serenazgo/mapa")
@RequiredArgsConstructor
public class MapaRestController {

    private final MapaGeograficoSerenazgoService mapaService;

    @GetMapping("/sectores")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_MAPA_VER', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_OPERADOR_CC', 'ROLE_SERENO')")
    public ResponseEntity<GeoJsonFeatureCollectionResponse> obtenerSectores(
            @ModelAttribute MapaFiltroRequest filtro) {
        return ResponseEntity.ok(mapaService.obtenerCapaSectores(filtro));
    }

    @GetMapping("/zonas")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_MAPA_VER', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_OPERADOR_CC', 'ROLE_SERENO')")
    public ResponseEntity<GeoJsonFeatureCollectionResponse> obtenerZonas(
            @ModelAttribute MapaFiltroRequest filtro) {
        return ResponseEntity.ok(mapaService.obtenerCapaZonas(filtro));
    }

    @GetMapping("/incidencias")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_MAPA_VER_INCIDENCIAS', 'SERENAZGO_INCIDENCIAS_VER', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_OPERADOR_CC', 'ROLE_SERENO')")
    public ResponseEntity<GeoJsonFeatureCollectionResponse> obtenerIncidencias(
            @ModelAttribute MapaFiltroRequest filtro,
            @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(mapaService.obtenerCapaIncidencias(filtro, user));
    }

    @GetMapping("/alertas")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_MAPA_VER_ALERTAS', 'SERENAZGO_ALERTAS_VER', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_OPERADOR_CC')")
    public ResponseEntity<GeoJsonFeatureCollectionResponse> obtenerAlertas(
            @ModelAttribute MapaFiltroRequest filtro,
            @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(mapaService.obtenerCapaAlertas(filtro, user));
    }

    @GetMapping("/intervenciones")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_MAPA_VER_INTERVENCIONES', 'SERENAZGO_INTERVENCIONES_VER', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_OPERADOR_CC')")
    public ResponseEntity<GeoJsonFeatureCollectionResponse> obtenerIntervenciones(
            @ModelAttribute MapaFiltroRequest filtro,
            @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(mapaService.obtenerCapaIntervenciones(filtro, user));
    }

    @GetMapping("/patrullajes")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_MAPA_VER_PATRULLAJES', 'SERENAZGO_PATRULLAJES_VER', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_OPERADOR_CC', 'ROLE_SERENO')")
    public ResponseEntity<GeoJsonFeatureCollectionResponse> obtenerPatrullajes(
            @ModelAttribute MapaFiltroRequest filtro,
            @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(mapaService.obtenerCapaPatrullajes(filtro, user));
    }

    @GetMapping("/rutas")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_MAPA_VER', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_OPERADOR_CC', 'ROLE_SERENO')")
    public ResponseEntity<GeoJsonFeatureCollectionResponse> obtenerRutas(
            @ModelAttribute MapaFiltroRequest filtro,
            @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(mapaService.obtenerCapaRutas(filtro, user));
    }

    @GetMapping("/puntos")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_MAPA_VER', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_OPERADOR_CC', 'ROLE_SERENO')")
    public ResponseEntity<GeoJsonFeatureCollectionResponse> obtenerPuntosControl(
            @ModelAttribute MapaFiltroRequest filtro,
            @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(mapaService.obtenerCapaPuntosControl(filtro, user));
    }

    @GetMapping("/estadisticas")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_MAPA_VER', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_OPERADOR_CC')")
    public ResponseEntity<MapaEstadisticasResponse> obtenerEstadisticas(
            @ModelAttribute MapaFiltroRequest filtro) {
        return ResponseEntity.ok(mapaService.obtenerEstadisticas(filtro));
    }

    @GetMapping("/detalle/{tipo}/{id}")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_MAPA_VER', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_OPERADOR_CC', 'ROLE_SERENO')")
    public ResponseEntity<ElementoGeograficoDetalleResponse> obtenerDetalle(
            @PathVariable String tipo,
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(mapaService.obtenerDetalleElemento(tipo, id, user));
    }

    @GetMapping("/buscar")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_MAPA_VER', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_OPERADOR_CC', 'ROLE_SERENO')")
    public ResponseEntity<List<ItemBusquedaMapaResponse>> buscar(
            @RequestParam("q") String query,
            @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(mapaService.buscarElementos(query, user));
    }

    @GetMapping(value = "/exportar/geojson", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyAuthority('SERENAZGO_MAPA_EXPORTAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_SUPERVISOR_SERENAZGO')")
    public ResponseEntity<String> exportarGeoJson(
            @RequestParam(value = "capa", defaultValue = "TODAS") String capa,
            @ModelAttribute MapaFiltroRequest filtro,
            @AuthenticationPrincipal CustomUserDetails user) {
        String json = mapaService.exportarGeoJson(capa, filtro, user);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"sigm_mapa_" + capa.toLowerCase() + ".geojson\"")
                .body(json);
    }
}
