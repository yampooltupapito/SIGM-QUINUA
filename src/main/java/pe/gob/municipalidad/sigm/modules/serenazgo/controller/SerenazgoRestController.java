package pe.gob.municipalidad.sigm.modules.serenazgo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.CatalogoSeguridadService;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.DashboardSeguridadService;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.SectorPatrullajeService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/serenazgo")
@RequiredArgsConstructor
public class SerenazgoRestController {

    private final CatalogoSeguridadService catalogoService;
    private final SectorPatrullajeService sectorService;
    private final DashboardSeguridadService dashboardService;

    @GetMapping("/catalogos")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_CATALOGOS_VER', 'SERENAZGO_MODULO_ACCEDER', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_SERENO')")
    public ResponseEntity<CatalogoSeguridadResponse> obtenerCatalogos() {
        return ResponseEntity.ok(catalogoService.obtenerCatalogosCompletos());
    }

    @GetMapping("/catalogos/tipos-incidencia")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_CATALOGOS_VER', 'SERENAZGO_MODULO_ACCEDER', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_SERENO')")
    public ResponseEntity<List<TipoIncidenciaResponse>> obtenerTiposIncidencia() {
        return ResponseEntity.ok(catalogoService.obtenerTiposIncidenciaActivos());
    }

    @GetMapping("/catalogos/prioridades")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_CATALOGOS_VER', 'SERENAZGO_MODULO_ACCEDER', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_SERENO')")
    public ResponseEntity<List<ItemCatalogoResponse>> obtenerPrioridades() {
        return ResponseEntity.ok(catalogoService.obtenerPrioridades());
    }

    @GetMapping("/sectores")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_SECTORES_VER', 'SERENAZGO_MODULO_ACCEDER', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_SERENO')")
    public ResponseEntity<List<SectorPatrullajeResponse>> listarSectores() {
        return ResponseEntity.ok(sectorService.listarSectoresActivos());
    }

    @GetMapping("/sectores/{id}/zonas")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_SECTORES_VER', 'SERENAZGO_MODULO_ACCEDER', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_SERENO')")
    public ResponseEntity<List<ZonaPatrullajeResponse>> listarZonasPorSector(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(sectorService.listarZonasPorSector(id));
    }

    @GetMapping("/resumen")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_MODULO_ACCEDER', 'SERENAZGO_REPORTES_VER', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_SERENO')")
    public ResponseEntity<ResumenDashboardSeguridadResponse> obtenerResumen() {
        return ResponseEntity.ok(dashboardService.obtenerResumenOperativo());
    }
}
