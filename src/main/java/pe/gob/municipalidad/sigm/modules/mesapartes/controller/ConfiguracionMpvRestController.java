package pe.gob.municipalidad.sigm.modules.mesapartes.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pe.gob.municipalidad.sigm.common.response.ApiResponse;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.ConfiguracionMpvDtos.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvSolicitudDtos.EsquemaTramiteResponse;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoCatalogoTramite;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.TipoDocumentalPlantilla;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.TipoEntidadConfiguracion;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mesa-partes/configuracion")
public class ConfiguracionMpvRestController {

    private final CatalogoTramitesService catalogoService;
    private final RequisitosTramiteService requisitosService;
    private final AdministracionFormulariosService formulariosService;
    private final ConfiguracionPlazosService plazosService;
    private final CalendarioAdministrativoService calendarioService;
    private final PlantillasDocumentalesService plantillasService;
    private final ConfiguracionNumeracionService numeracionService;
    private final ConfiguracionHistorialService historialService;

    public ConfiguracionMpvRestController(
            CatalogoTramitesService catalogoService,
            RequisitosTramiteService requisitosService,
            AdministracionFormulariosService formulariosService,
            ConfiguracionPlazosService plazosService,
            CalendarioAdministrativoService calendarioService,
            PlantillasDocumentalesService plantillasService,
            ConfiguracionNumeracionService numeracionService,
            ConfiguracionHistorialService historialService
    ) {
        this.catalogoService = catalogoService;
        this.requisitosService = requisitosService;
        this.formulariosService = formulariosService;
        this.plazosService = plazosService;
        this.calendarioService = calendarioService;
        this.plantillasService = plantillasService;
        this.numeracionService = numeracionService;
        this.historialService = historialService;
    }

    // =========================================================================
    // 1. CATÁLOGO DE TRÁMITES Y PROCEDIMIENTOS TUPA
    // =========================================================================

    @GetMapping("/tramites")
    @PreAuthorize("hasAuthority('MESAPARTES_CONFIG_VER')")
    public ResponseEntity<ApiResponse<Page<TramiteAdminResponse>>> listarTramites(
            @RequestParam(required = false) EstadoCatalogoTramite estadoCatalogo,
            @RequestParam(required = false) String q,
            @PageableDefault(size = 15, sort = "nombre", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<TramiteAdminResponse> pagina = catalogoService.listarTramites(estadoCatalogo, q, pageable);
        return ApiResponse.ok("Trámites obtenidos correctamente.", pagina);
    }

    @GetMapping("/tramites/{id}")
    @PreAuthorize("hasAuthority('MESAPARTES_CONFIG_VER')")
    public ResponseEntity<ApiResponse<TramiteAdminResponse>> obtenerTramite(@PathVariable Long id) {
        return ApiResponse.ok(catalogoService.obtenerPorId(id));
    }

    @PostMapping("/tramites")
    @PreAuthorize("hasAuthority('MESAPARTES_TRAMITE_CREAR')")
    public ResponseEntity<ApiResponse<TramiteAdminResponse>> crearTramite(
            @Valid @RequestBody GuardarTramiteRequest request,
            Authentication auth,
            HttpServletRequest req
    ) {
        String username = auth != null ? auth.getName() : "ADMIN_CONFIG";
        TramiteAdminResponse resp = catalogoService.crearBorrador(request, username, req.getRemoteAddr());
        return ApiResponse.created("Trámite registrado exitosamente en borrador.", resp);
    }

    @PutMapping("/tramites/{id}")
    @PreAuthorize("hasAuthority('MESAPARTES_TRAMITE_EDITAR')")
    public ResponseEntity<ApiResponse<TramiteAdminResponse>> editarTramite(
            @PathVariable Long id,
            @Valid @RequestBody GuardarTramiteRequest request,
            Authentication auth,
            HttpServletRequest req
    ) {
        String username = auth != null ? auth.getName() : "ADMIN_CONFIG";
        TramiteAdminResponse resp = catalogoService.editarBorrador(id, request, username, req.getRemoteAddr());
        return ApiResponse.ok("Trámite actualizado correctamente.", resp);
    }

    @PostMapping("/tramites/{id}/enviar-revision")
    @PreAuthorize("hasAuthority('MESAPARTES_TRAMITE_REVISAR')")
    public ResponseEntity<ApiResponse<TramiteAdminResponse>> enviarARevision(
            @PathVariable Long id,
            @RequestBody(required = false) AccionTransicionRequest transicion,
            Authentication auth,
            HttpServletRequest req
    ) {
        String username = auth != null ? auth.getName() : "ADMIN_CONFIG";
        TramiteAdminResponse resp = catalogoService.enviarARevision(id, transicion, username, req.getRemoteAddr());
        return ApiResponse.ok("Trámite enviado a revisión técnica.", resp);
    }

    @PostMapping("/tramites/{id}/aprobar")
    @PreAuthorize("hasAuthority('MESAPARTES_TRAMITE_APROBAR')")
    public ResponseEntity<ApiResponse<TramiteAdminResponse>> aprobarTramite(
            @PathVariable Long id,
            @RequestBody(required = false) AccionTransicionRequest transicion,
            Authentication auth,
            HttpServletRequest req
    ) {
        String username = auth != null ? auth.getName() : "ADMIN_CONFIG";
        TramiteAdminResponse resp = catalogoService.aprobar(id, transicion, username, req.getRemoteAddr());
        return ApiResponse.ok("Trámite aprobado satisfactoriamente.", resp);
    }

    @PostMapping("/tramites/{id}/publicar")
    @PreAuthorize("hasAuthority('MESAPARTES_TRAMITE_PUBLICAR')")
    public ResponseEntity<ApiResponse<TramiteAdminResponse>> publicarTramite(
            @PathVariable Long id,
            @RequestBody(required = false) AccionTransicionRequest transicion,
            Authentication auth,
            HttpServletRequest req
    ) {
        String username = auth != null ? auth.getName() : "ADMIN_CONFIG";
        TramiteAdminResponse resp = catalogoService.publicar(id, transicion, username, req.getRemoteAddr());
        return ApiResponse.ok("Trámite publicado oficialmente en el portal ciudadano.", resp);
    }

    @PostMapping("/tramites/{id}/suspender")
    @PreAuthorize("hasAuthority('MESAPARTES_TRAMITE_SUSPENDER')")
    public ResponseEntity<ApiResponse<TramiteAdminResponse>> suspenderTramite(
            @PathVariable Long id,
            @RequestBody(required = false) AccionTransicionRequest transicion,
            Authentication auth,
            HttpServletRequest req
    ) {
        String username = auth != null ? auth.getName() : "ADMIN_CONFIG";
        TramiteAdminResponse resp = catalogoService.suspender(id, transicion, username, req.getRemoteAddr());
        return ApiResponse.ok("Trámite suspendido temporalmente.", resp);
    }

    @PostMapping("/tramites/{id}/desactivar")
    @PreAuthorize("hasAuthority('MESAPARTES_TRAMITE_SUSPENDER')")
    public ResponseEntity<ApiResponse<TramiteAdminResponse>> desactivarTramite(
            @PathVariable Long id,
            @RequestBody(required = false) AccionTransicionRequest transicion,
            Authentication auth,
            HttpServletRequest req
    ) {
        String username = auth != null ? auth.getName() : "ADMIN_CONFIG";
        TramiteAdminResponse resp = catalogoService.desactivar(id, transicion, username, req.getRemoteAddr());
        return ApiResponse.ok("Trámite inactivado en el catálogo institucional.", resp);
    }

    @GetMapping("/tramites/{id}/versiones")
    @PreAuthorize("hasAuthority('MESAPARTES_CONFIG_VER')")
    public ResponseEntity<ApiResponse<List<TramiteVersionResponse>>> listarVersionesTramite(@PathVariable Long id) {
        return ApiResponse.ok(catalogoService.listarVersiones(id));
    }

    // =========================================================================
    // 2. REQUISITOS DOCUMENTALES
    // =========================================================================

    @GetMapping("/requisitos/tramite/{tipoTramiteId}")
    @PreAuthorize("hasAuthority('MESAPARTES_CONFIG_VER')")
    public ResponseEntity<ApiResponse<List<RequisitoAdminResponse>>> listarRequisitosPorTramite(@PathVariable Long tipoTramiteId) {
        return ApiResponse.ok(requisitosService.listarPorTramite(tipoTramiteId));
    }

    @GetMapping("/requisitos/{id}")
    @PreAuthorize("hasAuthority('MESAPARTES_CONFIG_VER')")
    public ResponseEntity<ApiResponse<RequisitoAdminResponse>> obtenerRequisito(@PathVariable Long id) {
        return ApiResponse.ok(requisitosService.obtenerPorId(id));
    }

    @PostMapping("/requisitos")
    @PreAuthorize("hasAuthority('MESAPARTES_REQUISITO_GESTIONAR')")
    public ResponseEntity<ApiResponse<RequisitoAdminResponse>> guardarRequisito(
            @Valid @RequestBody GuardarRequisitoRequest request,
            Authentication auth,
            HttpServletRequest req
    ) {
        String username = auth != null ? auth.getName() : "ADMIN_CONFIG";
        RequisitoAdminResponse resp = requisitosService.guardarRequisito(request, username, req.getRemoteAddr());
        return ApiResponse.created("Requisito registrado exitosamente.", resp);
    }

    @PutMapping("/requisitos/{id}")
    @PreAuthorize("hasAuthority('MESAPARTES_REQUISITO_GESTIONAR')")
    public ResponseEntity<ApiResponse<RequisitoAdminResponse>> actualizarRequisito(
            @PathVariable Long id,
            @Valid @RequestBody GuardarRequisitoRequest request,
            Authentication auth,
            HttpServletRequest req
    ) {
        String username = auth != null ? auth.getName() : "ADMIN_CONFIG";
        RequisitoAdminResponse resp = requisitosService.actualizarRequisito(id, request, username, req.getRemoteAddr());
        return ApiResponse.ok("Requisito actualizado correctamente.", resp);
    }

    @PutMapping("/requisitos/{id}/estado")
    @PreAuthorize("hasAuthority('MESAPARTES_REQUISITO_GESTIONAR')")
    public ResponseEntity<ApiResponse<Void>> cambiarEstadoRequisito(
            @PathVariable Long id,
            @RequestParam String estado,
            @RequestParam(required = false) String motivo,
            Authentication auth,
            HttpServletRequest req
    ) {
        String username = auth != null ? auth.getName() : "ADMIN_CONFIG";
        requisitosService.cambiarEstadoRequisito(id, estado, motivo, username, req.getRemoteAddr());
        return ApiResponse.ok("Estado del requisito modificado satisfactoriamente.");
    }

    // =========================================================================
    // 3. FORMULARIOS DINÁMICOS
    // =========================================================================

    @GetMapping("/formularios")
    @PreAuthorize("hasAuthority('MESAPARTES_CONFIG_VER')")
    public ResponseEntity<ApiResponse<List<FormularioAdminResponse>>> listarFormularios() {
        return ApiResponse.ok(formulariosService.listarFormularios());
    }

    @GetMapping("/formularios/{id}")
    @PreAuthorize("hasAuthority('MESAPARTES_CONFIG_VER')")
    public ResponseEntity<ApiResponse<FormularioAdminResponse>> obtenerFormulario(@PathVariable Long id) {
        return ApiResponse.ok(formulariosService.obtenerPorId(id));
    }

    @GetMapping("/formularios/tramite/{tipoTramiteId}")
    @PreAuthorize("hasAuthority('MESAPARTES_CONFIG_VER')")
    public ResponseEntity<ApiResponse<FormularioAdminResponse>> obtenerFormularioPorTramite(@PathVariable Long tipoTramiteId) {
        return ApiResponse.ok(formulariosService.obtenerPorTramiteId(tipoTramiteId));
    }

    @PostMapping("/formularios")
    @PreAuthorize("hasAuthority('MESAPARTES_FORMULARIO_GESTIONAR')")
    public ResponseEntity<ApiResponse<FormularioAdminResponse>> guardarFormulario(
            @Valid @RequestBody GuardarFormularioRequest request,
            Authentication auth,
            HttpServletRequest req
    ) {
        String username = auth != null ? auth.getName() : "ADMIN_CONFIG";
        FormularioAdminResponse resp = formulariosService.guardarFormulario(request, username, req.getRemoteAddr());
        return ApiResponse.ok("Formulario y campos guardados correctamente.", resp);
    }

    @PostMapping("/formularios/{id}/publicar")
    @PreAuthorize("hasAuthority('MESAPARTES_FORMULARIO_PUBLICAR')")
    public ResponseEntity<ApiResponse<FormularioAdminResponse>> publicarFormulario(
            @PathVariable Long id,
            Authentication auth,
            HttpServletRequest req
    ) {
        String username = auth != null ? auth.getName() : "ADMIN_CONFIG";
        FormularioAdminResponse resp = formulariosService.publicarVersion(id, username, req.getRemoteAddr());
        return ApiResponse.ok("Versión de formulario publicada exitosamente.", resp);
    }

    @GetMapping("/formularios/{id}/preview")
    @PreAuthorize("hasAuthority('MESAPARTES_CONFIG_VER')")
    public ResponseEntity<ApiResponse<EsquemaTramiteResponse>> previsualizarFormulario(@PathVariable Long id) {
        return ApiResponse.ok("Vista previa generada.", formulariosService.generarVistaPrevia(id));
    }

    // =========================================================================
    // 4. PLAZOS ADMINISTRATIVOS
    // =========================================================================

    @GetMapping("/plazos")
    @PreAuthorize("hasAuthority('MESAPARTES_CONFIG_VER')")
    public ResponseEntity<ApiResponse<List<TramiteAdminResponse>>> listarPlazos() {
        return ApiResponse.ok(plazosService.listarPlazosPorTramite());
    }

    @PutMapping("/plazos/{tipoTramiteId}")
    @PreAuthorize("hasAuthority('MESAPARTES_PLAZO_GESTIONAR')")
    public ResponseEntity<ApiResponse<TramiteAdminResponse>> actualizarPlazo(
            @PathVariable Long tipoTramiteId,
            @RequestParam Integer dias,
            @RequestParam pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.UnidadPlazo unidad,
            @RequestParam(required = false) String baseNormativa,
            @RequestParam(required = false) String motivo,
            Authentication auth,
            HttpServletRequest req
    ) {
        String username = auth != null ? auth.getName() : "ADMIN_CONFIG";
        TramiteAdminResponse resp = plazosService.actualizarPlazo(tipoTramiteId, dias, unidad, baseNormativa, motivo, username, req.getRemoteAddr());
        return ApiResponse.ok("Plazo administrativo actualizado correctamente.", resp);
    }

    // =========================================================================
    // 5. CALENDARIO Y DÍAS NO LABORABLES
    // =========================================================================

    @GetMapping("/calendario")
    @PreAuthorize("hasAuthority('MESAPARTES_CONFIG_VER')")
    public ResponseEntity<ApiResponse<List<CalendarioAdminResponse>>> listarCalendario(
            @RequestParam(required = false) Integer anio
    ) {
        int anioReq = (anio != null && anio > 1900) ? anio : LocalDate.now().getYear();
        return ApiResponse.ok(calendarioService.listarPorAnio(anioReq));
    }

    @PostMapping("/calendario")
    @PreAuthorize("hasAuthority('MESAPARTES_CALENDARIO_GESTIONAR')")
    public ResponseEntity<ApiResponse<CalendarioAdminResponse>> guardarFeriado(
            @Valid @RequestBody GuardarCalendarioRequest request,
            Authentication auth,
            HttpServletRequest req
    ) {
        String username = auth != null ? auth.getName() : "ADMIN_CONFIG";
        CalendarioAdminResponse resp = calendarioService.guardarFeriado(request, username, req.getRemoteAddr());
        return ApiResponse.ok("Día no laborable / feriado configurado correctamente.", resp);
    }

    @PutMapping("/calendario/{id}/estado")
    @PreAuthorize("hasAuthority('MESAPARTES_CALENDARIO_GESTIONAR')")
    public ResponseEntity<ApiResponse<Void>> cambiarEstadoCalendario(
            @PathVariable Long id,
            @RequestParam boolean activo,
            Authentication auth,
            HttpServletRequest req
    ) {
        String username = auth != null ? auth.getName() : "ADMIN_CONFIG";
        calendarioService.cambiarEstado(id, activo, username, req.getRemoteAddr());
        return ApiResponse.ok("Estado de día no laborable actualizado.");
    }

    // =========================================================================
    // 6. PLANTILLAS DOCUMENTALES
    // =========================================================================

    @GetMapping("/plantillas")
    @PreAuthorize("hasAuthority('MESAPARTES_CONFIG_VER')")
    public ResponseEntity<ApiResponse<List<PlantillaAdminResponse>>> listarPlantillas() {
        return ApiResponse.ok(plantillasService.listarPlantillas());
    }

    @GetMapping("/plantillas/{id}")
    @PreAuthorize("hasAuthority('MESAPARTES_CONFIG_VER')")
    public ResponseEntity<ApiResponse<PlantillaAdminResponse>> obtenerPlantilla(@PathVariable Long id) {
        return ApiResponse.ok(plantillasService.obtenerPorId(id));
    }

    @PostMapping("/plantillas")
    @PreAuthorize("hasAuthority('MESAPARTES_PLANTILLA_GESTIONAR')")
    public ResponseEntity<ApiResponse<PlantillaAdminResponse>> guardarPlantilla(
            @Valid @RequestBody GuardarPlantillaRequest request,
            Authentication auth,
            HttpServletRequest req
    ) {
        String username = auth != null ? auth.getName() : "ADMIN_CONFIG";
        PlantillaAdminResponse resp = plantillasService.guardarPlantilla(request, username, req.getRemoteAddr());
        return ApiResponse.created("Plantilla documental registrada exitosamente.", resp);
    }

    @PutMapping("/plantillas/{id}")
    @PreAuthorize("hasAuthority('MESAPARTES_PLANTILLA_GESTIONAR')")
    public ResponseEntity<ApiResponse<PlantillaAdminResponse>> actualizarPlantilla(
            @PathVariable Long id,
            @Valid @RequestBody GuardarPlantillaRequest request,
            Authentication auth,
            HttpServletRequest req
    ) {
        String username = auth != null ? auth.getName() : "ADMIN_CONFIG";
        PlantillaAdminResponse resp = plantillasService.actualizarPlantilla(id, request, username, req.getRemoteAddr());
        return ApiResponse.ok("Plantilla documental actualizada y versionada correctamente.", resp);
    }

    @PostMapping("/plantillas/{id}/preview")
    @PreAuthorize("hasAuthority('MESAPARTES_CONFIG_VER')")
    public ResponseEntity<ApiResponse<String>> previewPlantilla(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> variables
    ) {
        String render = plantillasService.previsualizarContenido(id, variables);
        return ApiResponse.ok("Vista previa de plantilla generada.", render);
    }

    // =========================================================================
    // 7. NUMERACIÓN Y CORRELATIVOS
    // =========================================================================

    @GetMapping("/numeracion")
    @PreAuthorize("hasAuthority('MESAPARTES_NUMERACION_CONFIGURAR')")
    public ResponseEntity<ApiResponse<List<NumeracionResponse>>> listarNumeracion() {
        return ApiResponse.ok(numeracionService.listarCorrelativos());
    }

    // =========================================================================
    // 8. HISTORIAL DE AUDITORÍA Y VERSIONADO
    // =========================================================================

    @GetMapping("/historial")
    @PreAuthorize("hasAuthority('MESAPARTES_CONFIG_HISTORIAL_VER')")
    public ResponseEntity<ApiResponse<Page<HistorialConfiguracionResponse>>> listarHistorial(
            @RequestParam(required = false) TipoEntidadConfiguracion entidadTipo,
            @PageableDefault(size = 20, sort = "fechaCambio", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ApiResponse.ok(historialService.listarHistorial(entidadTipo, pageable));
    }

    @GetMapping("/historial/{entidadTipo}/{entidadId}")
    @PreAuthorize("hasAuthority('MESAPARTES_CONFIG_HISTORIAL_VER')")
    public ResponseEntity<ApiResponse<List<HistorialConfiguracionResponse>>> listarHistorialEntidad(
            @PathVariable TipoEntidadConfiguracion entidadTipo,
            @PathVariable Long entidadId
    ) {
        return ApiResponse.ok(historialService.listarPorEntidad(entidadTipo, entidadId));
    }
}
