package pe.gob.municipalidad.sigm.modules.mercados.controller.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pe.gob.municipalidad.sigm.modules.mercados.dto.*;
import pe.gob.municipalidad.sigm.modules.mercados.dto.MercadosDtos.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoMercado;
import pe.gob.municipalidad.sigm.modules.mercados.repository.CategoriaComercialRepository;
import pe.gob.municipalidad.sigm.modules.mercados.repository.TipoPuestoComercialRepository;
import pe.gob.municipalidad.sigm.modules.mercados.service.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping("/dashboard/mercados")
@PreAuthorize("hasAuthority('MERCADOS_MODULO_ACCEDER') or hasAnyRole('ADMIN', 'SUPERADMIN', 'OPERADOR', 'JEFE_OFICINA')")
public class MercadosWebController {

    private final MercadosDashboardService dashboardService;
    private final MercadoMunicipalService mercadoService;
    private final SectorMercadoService sectorService;
    private final PabellonMercadoService pabellonService;
    private final EstructuraMercadoService estructuraService;
    private final CategoriaComercialRepository categoriaRepository;
    private final PuestoComercialService puestoService;
    private final TipoPuestoComercialRepository tipoPuestoRepository;
    private final ZonaComercialService zonaService;

    public MercadosWebController(
            MercadosDashboardService dashboardService,
            MercadoMunicipalService mercadoService,
            SectorMercadoService sectorService,
            PabellonMercadoService pabellonService,
            EstructuraMercadoService estructuraService,
            CategoriaComercialRepository categoriaRepository,
            PuestoComercialService puestoService,
            TipoPuestoComercialRepository tipoPuestoRepository,
            ZonaComercialService zonaService) {
        this.dashboardService = dashboardService;
        this.mercadoService = mercadoService;
        this.sectorService = sectorService;
        this.pabellonService = pabellonService;
        this.estructuraService = estructuraService;
        this.categoriaRepository = categoriaRepository;
        this.puestoService = puestoService;
        this.tipoPuestoRepository = tipoPuestoRepository;
        this.zonaService = zonaService;
    }

    // Dashboard principal del módulo
    @GetMapping
    public String dashboard(Model model) {
        log.info("Cargando dashboard del módulo de Mercados Municipales de Quinua");

        DashboardMercadosMetrics metricas = dashboardService.obtenerMetricas();
        MercadoFilter filter = MercadoFilter.builder().page(0).size(10).sortBy("nombre").sortDir("asc").build();
        List<MercadoResponse> mercados = mercadoService.listarMercados(filter).getContent();

        model.addAttribute("moduloActivo", "mercados");
        model.addAttribute("submenuActivo", "dashboard");
        model.addAttribute("metricas", metricas);
        model.addAttribute("mercados", mercados);

        return "admin/mercados/dashboard";
    }

    // Listado completo con filtros, búsqueda y paginación
    @GetMapping("/listado")
    @PreAuthorize("hasAuthority('MERCADOS_MERCADO_VER') or hasAnyRole('ADMIN', 'SUPERADMIN', 'OPERADOR', 'JEFE_OFICINA')")
    public String listado(
            @RequestParam(required = false) String termino,
            @RequestParam(required = false) EstadoMercado estado,
            @RequestParam(required = false) Long oficinaId,
            @RequestParam(required = false) String distrito,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(defaultValue = "nombre") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        log.info("Cargando listado administrativo de mercados municipales");

        MercadoFilter filter = MercadoFilter.builder()
                .termino(termino)
                .estado(estado)
                .oficinaId(oficinaId)
                .distrito(distrito)
                .fechaDesde(fechaDesde)
                .fechaHasta(fechaHasta)
                .sortBy(sortBy)
                .sortDir(sortDir)
                .page(page)
                .size(size)
                .build();

        Page<MercadoResponse> paginaMercados = mercadoService.listarMercados(filter);
        MercadoEstadisticasResponse estadisticas = mercadoService.obtenerEstadisticas();
        List<OficinaSimpleResponse> oficinas = mercadoService.listarOficinasDisponibles();

        model.addAttribute("moduloActivo", "mercados");
        model.addAttribute("submenuActivo", "mercados");
        model.addAttribute("pagina", paginaMercados);
        model.addAttribute("mercados", paginaMercados.getContent());
        model.addAttribute("estadisticas", estadisticas);
        model.addAttribute("oficinas", oficinas);
        model.addAttribute("filtro", filter);
        model.addAttribute("estados", EstadoMercado.values());

        return "admin/mercados/listado";
    }

    // Formulario de registro de nuevo mercado
    @GetMapping("/nuevo")
    @PreAuthorize("hasAuthority('MERCADOS_MERCADO_CREAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public String nuevoMercado(Model model) {
        log.info("Cargando formulario de registro de mercado municipal");

        List<OficinaSimpleResponse> oficinas = mercadoService.listarOficinasDisponibles();

        model.addAttribute("moduloActivo", "mercados");
        model.addAttribute("submenuActivo", "mercados");
        model.addAttribute("esEdicion", false);
        model.addAttribute("version", 0L);
        model.addAttribute("mercado", new MercadoRequest());
        model.addAttribute("oficinas", oficinas);
        model.addAttribute("estados", EstadoMercado.values());

        return "admin/mercados/formulario";
    }

    // Detalle integral con pestañas
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('MERCADOS_MERCADO_VER') or hasAnyRole('ADMIN', 'SUPERADMIN', 'OPERADOR', 'JEFE_OFICINA')")
    public String detalleMercado(@PathVariable UUID id, Model model) {
        log.info("Cargando detalle del mercado municipal con ID: {}", id);

        MercadoDetalleResponse detalle = mercadoService.obtenerDetalle(id);

        model.addAttribute("moduloActivo", "mercados");
        model.addAttribute("submenuActivo", "mercados");
        model.addAttribute("detalle", detalle);
        model.addAttribute("mercado", detalle.getMercado());
        model.addAttribute("estados", EstadoMercado.values());

        return "admin/mercados/detalle";
    }

    // Formulario de edición de mercado existente
    @GetMapping("/{id}/editar")
    @PreAuthorize("hasAuthority('MERCADOS_MERCADO_EDITAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public String editarMercado(@PathVariable UUID id, Model model) {
        log.info("Cargando formulario de edición para mercado municipal: {}", id);

        MercadoResponse mercado = mercadoService.obtenerPorId(id);
        List<OficinaSimpleResponse> oficinas = mercadoService.listarOficinasDisponibles();

        MercadoActualizacionRequest form = MercadoActualizacionRequest.builder()
                .nombre(mercado.getNombre())
                .descripcion(mercado.getDescripcion())
                .direccion(mercado.getDireccion())
                .referenciaUbicacion(mercado.getReferenciaUbicacion())
                .distrito(mercado.getDistrito())
                .provincia(mercado.getProvincia())
                .departamento(mercado.getDepartamento())
                .latitud(mercado.getLatitud())
                .longitud(mercado.getLongitud())
                .capacidadReferencial(mercado.getCapacidadReferencial())
                .estado(mercado.getEstado())
                .oficinaAdministradoraId(mercado.getOficinaAdministradoraId())
                .version(mercado.getVersion())
                .build();

        model.addAttribute("moduloActivo", "mercados");
        model.addAttribute("submenuActivo", "mercados");
        model.addAttribute("esEdicion", true);
        model.addAttribute("mercadoId", id);
        model.addAttribute("mercadoCodigo", mercado.getCodigo());
        model.addAttribute("version", mercado.getVersion());
        model.addAttribute("mercado", form);
        model.addAttribute("oficinas", oficinas);
        model.addAttribute("estados", EstadoMercado.values());

        return "admin/mercados/formulario";
    }

    // ==========================================
    // PROMPT 23: SECTORES COMERCIALES (WEB)
    // ==========================================

    @GetMapping("/{mercadoId}/sectores")
    @PreAuthorize("hasAuthority('MERCADOS_SECTOR_VER') or hasAuthority('MERCADOS_SECTOR_GESTIONAR') or hasAuthority('MERCADOS_MERCADO_VER') or hasAnyRole('ADMIN', 'SUPERADMIN', 'OPERADOR', 'JEFE_OFICINA')")
    public String listarSectores(
            @PathVariable UUID mercadoId,
            @RequestParam(required = false) String termino,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String categoria,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            Model model) {

        log.info("Listando sectores del mercado {}", mercadoId);
        MercadoResponse mercado = mercadoService.obtenerPorId(mercadoId);
        Pageable pageable = PageRequest.of(page, size, Sort.by("ordenVisual").ascending());
        Page<SectorResponse> paginaSectores = sectorService.listarSectores(mercadoId, termino, estado, categoria, pageable);
        List<CategoriaComercialResponse> categorias = obtenerCategoriasDisponibles();

        model.addAttribute("moduloActivo", "mercados");
        model.addAttribute("submenuActivo", "sectores");
        model.addAttribute("mercado", mercado);
        model.addAttribute("pagina", paginaSectores);
        model.addAttribute("sectores", paginaSectores.getContent());
        model.addAttribute("termino", termino);
        model.addAttribute("estadoSeleccionado", estado);
        model.addAttribute("categoriaSeleccionada", categoria);
        model.addAttribute("categorias", categorias);
        model.addAttribute("estados", EstadoMercado.values());

        return "admin/mercados/sectores/listado";
    }

    @GetMapping("/{mercadoId}/sectores/nuevo")
    @PreAuthorize("hasAuthority('MERCADOS_SECTOR_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public String nuevoSector(@PathVariable UUID mercadoId, Model model) {
        log.info("Cargando formulario de nuevo sector para mercado {}", mercadoId);
        MercadoResponse mercado = mercadoService.obtenerPorId(mercadoId);
        List<CategoriaComercialResponse> categorias = obtenerCategoriasDisponibles();

        SectorRequest form = SectorRequest.builder()
                .mercadoId(mercadoId)
                .estado("ACTIVO")
                .ordenVisual(0)
                .build();

        model.addAttribute("moduloActivo", "mercados");
        model.addAttribute("submenuActivo", "sectores");
        model.addAttribute("mercado", mercado);
        model.addAttribute("sector", form);
        model.addAttribute("esEdicion", false);
        model.addAttribute("categorias", categorias);
        model.addAttribute("estados", EstadoMercado.values());

        return "admin/mercados/sectores/formulario";
    }

    @GetMapping("/{mercadoId}/sectores/{sectorId}")
    @PreAuthorize("hasAuthority('MERCADOS_SECTOR_VER') or hasAuthority('MERCADOS_SECTOR_GESTIONAR') or hasAuthority('MERCADOS_MERCADO_VER') or hasAnyRole('ADMIN', 'SUPERADMIN', 'OPERADOR', 'JEFE_OFICINA')")
    public String detalleSector(
            @PathVariable UUID mercadoId,
            @PathVariable UUID sectorId,
            Model model) {

        log.info("Cargando detalle del sector {} en mercado {}", sectorId, mercadoId);
        MercadoResponse mercado = mercadoService.obtenerPorId(mercadoId);
        SectorResponse sector = sectorService.obtenerPorIdYMercado(sectorId, mercadoId);
        List<PabellonResponse> pabellones = pabellonService.listarPorSector(sectorId);
        List<HistorialCambioEstadoResponse> historial = sectorService.consultarHistorial(sectorId, mercadoId);

        model.addAttribute("moduloActivo", "mercados");
        model.addAttribute("submenuActivo", "sectores");
        model.addAttribute("mercado", mercado);
        model.addAttribute("sector", sector);
        model.addAttribute("pabellones", pabellones);
        model.addAttribute("historial", historial);
        model.addAttribute("estados", EstadoMercado.values());

        return "admin/mercados/sectores/detalle";
    }

    @GetMapping("/{mercadoId}/sectores/{sectorId}/editar")
    @PreAuthorize("hasAuthority('MERCADOS_SECTOR_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public String editarSector(
            @PathVariable UUID mercadoId,
            @PathVariable UUID sectorId,
            Model model) {

        log.info("Cargando formulario de edición para sector {} en mercado {}", sectorId, mercadoId);
        MercadoResponse mercado = mercadoService.obtenerPorId(mercadoId);
        SectorResponse sector = sectorService.obtenerPorIdYMercado(sectorId, mercadoId);
        List<CategoriaComercialResponse> categorias = obtenerCategoriasDisponibles();

        SectorActualizacionRequest form = SectorActualizacionRequest.builder()
                .nombre(sector.getNombre())
                .descripcion(sector.getDescripcion())
                .tipoSector(sector.getTipoSector())
                .categoriaComercial(sector.getCategoriaComercial())
                .ordenVisual(sector.getOrdenVisual())
                .version(sector.getVersion())
                .build();

        model.addAttribute("moduloActivo", "mercados");
        model.addAttribute("submenuActivo", "sectores");
        model.addAttribute("mercado", mercado);
        model.addAttribute("sector", form);
        model.addAttribute("sectorOriginal", sector);
        model.addAttribute("esEdicion", true);
        model.addAttribute("categorias", categorias);
        model.addAttribute("estados", EstadoMercado.values());

        return "admin/mercados/sectores/formulario";
    }

    // ==========================================
    // PROMPT 23: PABELLONES (WEB)
    // ==========================================

    @GetMapping("/{mercadoId}/pabellones")
    @PreAuthorize("hasAuthority('MERCADOS_PABELLON_VER') or hasAuthority('MERCADOS_PABELLON_GESTIONAR') or hasAuthority('MERCADOS_MERCADO_VER') or hasAnyRole('ADMIN', 'SUPERADMIN', 'OPERADOR', 'JEFE_OFICINA')")
    public String listarPabellones(
            @PathVariable UUID mercadoId,
            @RequestParam(required = false) String termino,
            @RequestParam(required = false) UUID sectorId,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Integer nivel,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            Model model) {

        log.info("Listando pabellones del mercado {}", mercadoId);
        MercadoResponse mercado = mercadoService.obtenerPorId(mercadoId);
        Pageable pageable = PageRequest.of(page, size, Sort.by("ordenVisual").ascending());
        Page<PabellonResponse> paginaPabellones = pabellonService.listarPabellones(mercadoId, termino, sectorId, estado, nivel, pageable);
        List<SectorResponse> sectores = sectorService.listarPorMercado(mercadoId);

        model.addAttribute("moduloActivo", "mercados");
        model.addAttribute("submenuActivo", "pabellones");
        model.addAttribute("mercado", mercado);
        model.addAttribute("pagina", paginaPabellones);
        model.addAttribute("pabellones", paginaPabellones.getContent());
        model.addAttribute("sectores", sectores);
        model.addAttribute("termino", termino);
        model.addAttribute("sectorIdSeleccionado", sectorId);
        model.addAttribute("estadoSeleccionado", estado);
        model.addAttribute("nivelSeleccionado", nivel);
        model.addAttribute("estados", EstadoMercado.values());

        return "admin/mercados/pabellones/listado";
    }

    @GetMapping("/{mercadoId}/pabellones/nuevo")
    @PreAuthorize("hasAuthority('MERCADOS_PABELLON_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public String nuevoPabellon(@PathVariable UUID mercadoId, Model model) {
        log.info("Cargando formulario de nuevo pabellón para mercado {}", mercadoId);
        MercadoResponse mercado = mercadoService.obtenerPorId(mercadoId);
        List<SectorResponse> sectores = sectorService.listarPorMercado(mercadoId);

        PabellonRequest form = PabellonRequest.builder()
                .mercadoId(mercadoId)
                .nivel(1)
                .estado("ACTIVO")
                .ordenVisual(0)
                .build();

        model.addAttribute("moduloActivo", "mercados");
        model.addAttribute("submenuActivo", "pabellones");
        model.addAttribute("mercado", mercado);
        model.addAttribute("pabellon", form);
        model.addAttribute("sectores", sectores);
        model.addAttribute("esEdicion", false);
        model.addAttribute("estados", EstadoMercado.values());

        return "admin/mercados/pabellones/formulario";
    }

    @GetMapping("/{mercadoId}/pabellones/{pabellonId}")
    @PreAuthorize("hasAuthority('MERCADOS_PABELLON_VER') or hasAuthority('MERCADOS_PABELLON_GESTIONAR') or hasAuthority('MERCADOS_MERCADO_VER') or hasAnyRole('ADMIN', 'SUPERADMIN', 'OPERADOR', 'JEFE_OFICINA')")
    public String detallePabellon(
            @PathVariable UUID mercadoId,
            @PathVariable UUID pabellonId,
            Model model) {

        log.info("Cargando detalle del pabellón {} en mercado {}", pabellonId, mercadoId);
        MercadoResponse mercado = mercadoService.obtenerPorId(mercadoId);
        PabellonResponse pabellon = pabellonService.obtenerPorIdYMercado(pabellonId, mercadoId);
        List<HistorialCambioEstadoResponse> historial = pabellonService.consultarHistorial(pabellonId, mercadoId);

        model.addAttribute("moduloActivo", "mercados");
        model.addAttribute("submenuActivo", "pabellones");
        model.addAttribute("mercado", mercado);
        model.addAttribute("pabellon", pabellon);
        model.addAttribute("historial", historial);
        model.addAttribute("estados", EstadoMercado.values());

        return "admin/mercados/pabellones/detalle";
    }

    @GetMapping("/{mercadoId}/pabellones/{pabellonId}/editar")
    @PreAuthorize("hasAuthority('MERCADOS_PABELLON_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public String editarPabellon(
            @PathVariable UUID mercadoId,
            @PathVariable UUID pabellonId,
            Model model) {

        log.info("Cargando formulario de edición para pabellón {} en mercado {}", pabellonId, mercadoId);
        MercadoResponse mercado = mercadoService.obtenerPorId(mercadoId);
        PabellonResponse pabellon = pabellonService.obtenerPorIdYMercado(pabellonId, mercadoId);
        List<SectorResponse> sectores = sectorService.listarPorMercado(mercadoId);

        PabellonActualizacionRequest form = PabellonActualizacionRequest.builder()
                .nombre(pabellon.getNombre())
                .descripcion(pabellon.getDescripcion())
                .sectorId(pabellon.getSectorId())
                .nivel(pabellon.getNivel())
                .ordenVisual(pabellon.getOrdenVisual())
                .version(pabellon.getVersion())
                .build();

        model.addAttribute("moduloActivo", "mercados");
        model.addAttribute("submenuActivo", "pabellones");
        model.addAttribute("mercado", mercado);
        model.addAttribute("pabellon", form);
        model.addAttribute("pabellonOriginal", pabellon);
        model.addAttribute("sectores", sectores);
        model.addAttribute("esEdicion", true);
        model.addAttribute("estados", EstadoMercado.values());

        return "admin/mercados/pabellones/formulario";
    }

    // ==========================================
    // PROMPT 23: ESTRUCTURA JERÁRQUICA (WEB)
    // ==========================================

    @GetMapping("/{mercadoId}/estructura")
    @PreAuthorize("hasAuthority('MERCADOS_ESTRUCTURA_VER') or hasAuthority('MERCADOS_MERCADO_VER') or hasAnyRole('ADMIN', 'SUPERADMIN', 'OPERADOR', 'JEFE_OFICINA')")
    public String estructuraMercado(@PathVariable UUID mercadoId, Model model) {
        log.info("Visualizando estructura jerárquica del mercado {}", mercadoId);

        EstructuraMercadoResponse estructura = estructuraService.obtenerEstructuraCompleta(mercadoId);
        MercadoResponse mercado = mercadoService.obtenerPorId(mercadoId);
        List<CategoriaComercialResponse> categorias = obtenerCategoriasDisponibles();
        List<SectorResponse> sectores = sectorService.listarPorMercado(mercadoId);

        model.addAttribute("moduloActivo", "mercados");
        model.addAttribute("submenuActivo", "estructura");
        model.addAttribute("mercado", mercado);
        model.addAttribute("estructura", estructura);
        model.addAttribute("estadisticas", estructura.getEstadisticas());
        model.addAttribute("sectores", sectores);
        model.addAttribute("categorias", categorias);
        model.addAttribute("estados", EstadoMercado.values());

        return "admin/mercados/estructura";
    }

    // ==========================================
    // PROMPT 24: PUESTOS COMERCIALES (WEB)
    // ==========================================

    @GetMapping("/{mercadoId}/puestos")
    @PreAuthorize("hasAuthority('MERCADOS_PUESTO_VER') or hasAuthority('MERCADOS_PUESTO_GESTIONAR') or hasAuthority('MERCADOS_MERCADO_VER') or hasAnyRole('ADMIN', 'SUPERADMIN', 'OPERADOR', 'JEFE_OFICINA')")
    public String listarPuestosWeb(
            @PathVariable UUID mercadoId,
            @RequestParam(required = false) String termino,
            @RequestParam(required = false) UUID sectorId,
            @RequestParam(required = false) UUID pabellonId,
            @RequestParam(required = false) UUID zonaId,
            @RequestParam(required = false) Long tipoPuestoId,
            @RequestParam(required = false) pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoFisicoPuesto estadoFisico,
            @RequestParam(required = false) pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoPuesto estado,
            @RequestParam(required = false) Integer nivel,
            @RequestParam(required = false) java.math.BigDecimal minArea,
            @RequestParam(required = false) java.math.BigDecimal maxArea,
            @RequestParam(defaultValue = "tabla") String vista,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            Model model) {

        log.info("Listando puestos comerciales del mercado {}", mercadoId);
        MercadoResponse mercado = mercadoService.obtenerPorId(mercadoId);
        Pageable pageable = PageRequest.of(page, size, Sort.by("codigo").ascending());
        Page<PuestoComercialResponse> pagina = puestoService.listarPuestosPaginado(
                mercadoId, termino, sectorId, pabellonId, zonaId, tipoPuestoId, estadoFisico, estado, nivel, minArea, maxArea, pageable);

        PuestoEstadisticasResponse stats = puestoService.obtenerEstadisticas(mercadoId);
        List<SectorResponse> sectores = sectorService.listarPorMercado(mercadoId);
        List<PabellonResponse> pabellones = pabellonService.listarPorMercado(mercadoId);
        List<TipoPuestoResponse> tipos = tipoPuestoRepository.findByEstadoOrderByNombreAsc("ACTIVO")
                .stream()
                .map(t -> TipoPuestoResponse.builder()
                        .id(t.getId())
                        .codigo(t.getCodigo())
                        .nombre(t.getNombre())
                        .descripcion(t.getDescripcion())
                        .permiteInstalacionFija(t.getPermiteInstalacionFija())
                        .estado(t.getEstado())
                        .build())
                .collect(Collectors.toList());

        model.addAttribute("moduloActivo", "mercados");
        model.addAttribute("submenuActivo", "puestos");
        model.addAttribute("mercado", mercado);
        model.addAttribute("pagina", pagina);
        model.addAttribute("puestos", pagina.getContent());
        model.addAttribute("estadisticas", stats);
        model.addAttribute("sectores", sectores);
        model.addAttribute("pabellones", pabellones);
        model.addAttribute("tiposPuesto", tipos);
        model.addAttribute("estadosFisicos", pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoFisicoPuesto.values());
        model.addAttribute("estadosOperativos", pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoPuesto.values());
        model.addAttribute("termino", termino);
        model.addAttribute("sectorIdSeleccionado", sectorId);
        model.addAttribute("pabellonIdSeleccionado", pabellonId);
        model.addAttribute("tipoPuestoIdSeleccionado", tipoPuestoId);
        model.addAttribute("estadoFisicoSeleccionado", estadoFisico);
        model.addAttribute("estadoSeleccionado", estado);
        model.addAttribute("vista", vista);

        return "admin/mercados/puestos/listado";
    }

    @GetMapping("/{mercadoId}/puestos/nuevo")
    @PreAuthorize("hasAuthority('MERCADOS_PUESTO_CREAR') or hasAuthority('MERCADOS_PUESTO_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public String nuevoPuestoWeb(@PathVariable UUID mercadoId, Model model) {
        log.info("Cargando formulario de nuevo puesto comercial para mercado {}", mercadoId);
        MercadoResponse mercado = mercadoService.obtenerPorId(mercadoId);
        List<SectorResponse> sectores = sectorService.listarPorMercado(mercadoId);
        List<PabellonResponse> pabellones = pabellonService.listarPorMercado(mercadoId);
        List<TipoPuestoResponse> tipos = tipoPuestoRepository.findByEstadoOrderByNombreAsc("ACTIVO")
                .stream()
                .map(t -> TipoPuestoResponse.builder()
                        .id(t.getId())
                        .codigo(t.getCodigo())
                        .nombre(t.getNombre())
                        .build())
                .collect(Collectors.toList());

        PuestoComercialCreateRequest form = PuestoComercialCreateRequest.builder()
                .mercadoId(mercadoId)
                .nivel(1)
                .estadoFisico(pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoFisicoPuesto.OPERATIVO)
                .estado(pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoPuesto.DISPONIBLE)
                .build();

        model.addAttribute("moduloActivo", "mercados");
        model.addAttribute("submenuActivo", "puestos");
        model.addAttribute("mercado", mercado);
        model.addAttribute("puesto", form);
        model.addAttribute("esEdicion", false);
        model.addAttribute("sectores", sectores);
        model.addAttribute("pabellones", pabellones);
        model.addAttribute("tiposPuesto", tipos);
        model.addAttribute("estadosFisicos", pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoFisicoPuesto.values());
        model.addAttribute("estadosOperativos", pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoPuesto.values());

        return "admin/mercados/puestos/formulario";
    }

    @GetMapping("/{mercadoId}/puestos/{puestoId}")
    @PreAuthorize("hasAuthority('MERCADOS_PUESTO_VER') or hasAuthority('MERCADOS_PUESTO_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'OPERADOR', 'JEFE_OFICINA')")
    public String detallePuestoWeb(@PathVariable UUID mercadoId, @PathVariable UUID puestoId, Model model) {
        log.info("Visualizando detalle completo del puesto {} en mercado {}", puestoId, mercadoId);
        MercadoResponse mercado = mercadoService.obtenerPorId(mercadoId);
        PuestoComercialDetailResponse detalle = puestoService.obtenerDetalleCompleto(puestoId, mercadoId);
        List<SectorResponse> sectores = sectorService.listarPorMercado(mercadoId);
        List<PabellonResponse> pabellones = pabellonService.listarPorMercado(mercadoId);

        model.addAttribute("moduloActivo", "mercados");
        model.addAttribute("submenuActivo", "puestos");
        model.addAttribute("mercado", mercado);
        model.addAttribute("detalle", detalle);
        model.addAttribute("puesto", detalle.getPuesto());
        model.addAttribute("sectores", sectores);
        model.addAttribute("pabellones", pabellones);
        model.addAttribute("estadosFisicos", pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoFisicoPuesto.values());
        model.addAttribute("estadosOperativos", pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoPuesto.values());

        return "admin/mercados/puestos/detalle";
    }

    @GetMapping("/{mercadoId}/puestos/{puestoId}/editar")
    @PreAuthorize("hasAuthority('MERCADOS_PUESTO_EDITAR') or hasAuthority('MERCADOS_PUESTO_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public String editarPuestoWeb(@PathVariable UUID mercadoId, @PathVariable UUID puestoId, Model model) {
        log.info("Cargando formulario de edición para puesto {} en mercado {}", puestoId, mercadoId);
        MercadoResponse mercado = mercadoService.obtenerPorId(mercadoId);
        PuestoComercialResponse puesto = puestoService.obtenerPorIdYMercado(puestoId, mercadoId);
        List<TipoPuestoResponse> tipos = tipoPuestoRepository.findByEstadoOrderByNombreAsc("ACTIVO")
                .stream()
                .map(t -> TipoPuestoResponse.builder()
                        .id(t.getId())
                        .codigo(t.getCodigo())
                        .nombre(t.getNombre())
                        .build())
                .collect(Collectors.toList());

        PuestoComercialUpdateRequest form = PuestoComercialUpdateRequest.builder()
                .nombreReferencial(puesto.getNombreReferencial())
                .tipoPuestoId(puesto.getTipoPuestoId())
                .descripcion(puesto.getDescripcion())
                .areaMetrosCuadrados(puesto.getAreaMetrosCuadrados())
                .anchoMetros(puesto.getAnchoMetros())
                .largoMetros(puesto.getLargoMetros())
                .nivel(puesto.getNivel())
                .ubicacionReferencia(puesto.getUbicacionReferencia())
                .version(puesto.getVersion())
                .build();

        model.addAttribute("moduloActivo", "mercados");
        model.addAttribute("submenuActivo", "puestos");
        model.addAttribute("mercado", mercado);
        model.addAttribute("puesto", form);
        model.addAttribute("puestoOriginal", puesto);
        model.addAttribute("tiposPuesto", tipos);
        model.addAttribute("esEdicion", true);

        return "admin/mercados/puestos/formulario";
    }

    private List<CategoriaComercialResponse> obtenerCategoriasDisponibles() {
        return categoriaRepository.findByEstadoOrderByOrdenVisualAsc("ACTIVO")
                .stream()
                .map(c -> CategoriaComercialResponse.builder()
                        .id(c.getId())
                        .codigo(c.getCodigo())
                        .nombre(c.getNombre())
                        .descripcion(c.getDescripcion())
                        .estado(c.getEstado())
                        .ordenVisual(c.getOrdenVisual())
                        .build())
                .collect(Collectors.toList());
    }
}
