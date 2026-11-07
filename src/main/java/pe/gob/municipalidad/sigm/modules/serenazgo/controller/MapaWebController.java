package pe.gob.municipalidad.sigm.modules.serenazgo.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.mapa.MapaEstadisticasResponse;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.mapa.MapaFiltroRequest;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoIncidenciaSeguridad;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoPatrullajeSerenazgo;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.PrioridadIncidenciaSeguridad;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoPatrullajeSerenazgo;
import pe.gob.municipalidad.sigm.modules.serenazgo.repository.SectorPatrullajeRepository;
import pe.gob.municipalidad.sigm.modules.serenazgo.repository.TipoIncidenciaSeguridadRepository;
import pe.gob.municipalidad.sigm.modules.serenazgo.repository.ZonaPatrullajeRepository;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.MapaGeograficoSerenazgoService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Controller
@RequestMapping("/dashboard/serenazgo/mapa")
@RequiredArgsConstructor
public class MapaWebController {

    private final MapaGeograficoSerenazgoService mapaService;
    private final SectorPatrullajeRepository sectorRepository;
    private final ZonaPatrullajeRepository zonaRepository;
    private final TipoIncidenciaSeguridadRepository tipoIncidenciaRepository;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('SERENAZGO_MAPA_VER', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_OPERADOR_CC', 'ROLE_SERENO')")
    public String verMapaPrincipal(
            @ModelAttribute("filtro") MapaFiltroRequest filtro,
            Model model,
            @AuthenticationPrincipal CustomUserDetails user) {

        log.debug("Acceso al mapa geográfico de seguridad ciudadana por: {}", user != null ? user.getUsername() : "ANONYMOUS");

        MapaEstadisticasResponse stats = mapaService.obtenerEstadisticas(filtro);

        model.addAttribute("stats", stats);
        model.addAttribute("sectores", sectorRepository.findAll());
        model.addAttribute("zonas", zonaRepository.findAll());
        model.addAttribute("tiposIncidencia", tipoIncidenciaRepository.findByActivoTrueOrderByNombreAsc());
        model.addAttribute("prioridades", PrioridadIncidenciaSeguridad.values());
        model.addAttribute("estadosIncidencia", EstadoIncidenciaSeguridad.values());
        model.addAttribute("tiposPatrullaje", TipoPatrullajeSerenazgo.values());
        model.addAttribute("estadosPatrullaje", EstadoPatrullajeSerenazgo.values());

        return "admin/serenazgo/mapa/index";
    }

    @GetMapping("/exportar/excel")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_MAPA_EXPORTAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_SUPERVISOR_SERENAZGO')")
    public ResponseEntity<byte[]> exportarExcel(
            @ModelAttribute MapaFiltroRequest filtro,
            @AuthenticationPrincipal CustomUserDetails user) {

        byte[] bytes = mapaService.exportarExcel(filtro, user);
        String filename = "mapa_serenazgo_quinua_" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }

    @GetMapping("/exportar/pdf")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_MAPA_EXPORTAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_SUPERVISOR_SERENAZGO')")
    public ResponseEntity<byte[]> exportarPdf(
            @ModelAttribute MapaFiltroRequest filtro,
            @AuthenticationPrincipal CustomUserDetails user) {

        byte[] bytes = mapaService.exportarPdf(filtro, user);
        String filename = "reporte_cartografico_quinua_" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(bytes);
    }

    @GetMapping("/exportar/geojson")
    @PreAuthorize("hasAnyAuthority('SERENAZGO_MAPA_EXPORTAR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_SUPERVISOR_SERENAZGO')")
    public ResponseEntity<String> exportarGeoJson(
            @RequestParam(value = "capa", defaultValue = "TODAS") String capa,
            @ModelAttribute MapaFiltroRequest filtro,
            @AuthenticationPrincipal CustomUserDetails user) {

        String json = mapaService.exportarGeoJson(capa, filtro, user);
        String filename = "capa_geografica_" + capa.toLowerCase() + "_" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + ".geojson";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_JSON)
                .body(json);
    }
}
