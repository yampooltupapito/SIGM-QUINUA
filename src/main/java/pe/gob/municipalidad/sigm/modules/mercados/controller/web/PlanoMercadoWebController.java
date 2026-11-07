package pe.gob.municipalidad.sigm.modules.mercados.controller.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.modules.mercados.dto.*;
import pe.gob.municipalidad.sigm.modules.mercados.entity.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoVisualPuesto;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoElementoPlano;
import pe.gob.municipalidad.sigm.modules.mercados.repository.*;
import pe.gob.municipalidad.sigm.modules.mercados.service.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.*;

/**
 * Controlador MVC para las vistas Thymeleaf del plano interactivo de mercados.
 */
@Slf4j
@Controller
@RequestMapping("/dashboard/mercados/planos")
@RequiredArgsConstructor
public class PlanoMercadoWebController {

    private final PlanoMercadoService planoService;
    private final VersionPlanoMercadoService versionService;
    private final ConsultaPlanoMercadoService consultaService;
    private final MercadoMunicipalRepository mercadoRepository;
    private final SectorMercadoRepository sectorRepository;
    private final PabellonMercadoRepository pabellonRepository;
    private final PuestoComercialRepository puestoRepository;

    /**
     * Pantalla principal: Selector de mercados y listado de planos disponibles.
     */
    @GetMapping
    @PreAuthorize("hasAuthority('MERCADOS_PLANO_VER') or hasAnyRole('ADMIN', 'SUPERADMIN', 'OPERADOR')")
    public String bandejaPlanos(
            @RequestParam(required = false) UUID mercadoId,
            Model model) {
        List<MercadoMunicipal> mercados = mercadoRepository.findAll();
        model.addAttribute("mercados", mercados);

        if (mercadoId == null && !mercados.isEmpty()) {
            mercadoId = mercados.get(0).getId();
        }

        model.addAttribute("mercadoSeleccionadoId", mercadoId);

        if (mercadoId != null) {
            final UUID mid = mercadoId;
            MercadoMunicipal mercado = mercados.stream()
                    .filter(m -> m.getId().equals(mid))
                    .findFirst()
                    .orElse(null);
            model.addAttribute("mercado", mercado);

            Optional<PlanoMercadoResponse> planoOpt = planoService.obtenerPlanoActivoPorMercado(mercadoId);
            if (planoOpt.isPresent()) {
                model.addAttribute("plano", planoOpt.get());
                try {
                    PlanoMercadoDetailResponse detalle = consultaService.obtenerPlanoDetalleParaVisor(mercadoId, null);
                    model.addAttribute("detalle", detalle);
                    model.addAttribute("estadisticas", detalle.getEstadisticas());
                } catch (Exception e) {
                    log.warn("No se pudo obtener detalle del plano para mercado {}", mercadoId, e);
                }
            } else {
                model.addAttribute("plano", null);
            }
        }

        return "admin/mercados/planos/listado";
    }

    /**
     * Visor interactivo del plano de un mercado municipal.
     */
    @GetMapping("/{mercadoId}")
    @PreAuthorize("hasAuthority('MERCADOS_PLANO_VER') or hasAnyRole('ADMIN', 'SUPERADMIN', 'OPERADOR')")
    public String visorPlano(
            @PathVariable UUID mercadoId,
            @RequestParam(required = false) UUID versionId,
            Model model,
            RedirectAttributes redirectAttributes) {
        MercadoMunicipal mercado = mercadoRepository.findById(mercadoId).orElse(null);
        if (mercado == null) {
            redirectAttributes.addFlashAttribute("error", "Mercado no encontrado.");
            return "redirect:/dashboard/mercados/planos";
        }

        try {
            PlanoMercadoDetailResponse detalle = consultaService.obtenerPlanoDetalleParaVisor(mercadoId, versionId);
            model.addAttribute("mercado", mercado);
            model.addAttribute("detalle", detalle);
            model.addAttribute("plano", detalle);
            model.addAttribute("versionActual", detalle.getVersionSeleccionada());
            model.addAttribute("estadisticas", detalle.getEstadisticas());
            model.addAttribute("estadosVisuales", EstadoVisualPuesto.values());

            // Filtros de sectores y pabellones del mercado
            model.addAttribute("sectores", sectorRepository.findByMercadoIdOrderByOrdenVisualAsc(mercadoId));
            model.addAttribute("pabellones", pabellonRepository.findByMercadoIdOrderByOrdenVisualAsc(mercadoId));

            return "admin/mercados/planos/visor";
        } catch (Exception e) {
            log.info("El mercado {} no tiene plano activo aún, redirigiendo a pantalla de creación", mercadoId);
            return "redirect:/dashboard/mercados/planos/" + mercadoId + "/crear";
        }
    }

    /**
     * Editor visual del plano de mercado.
     */
    @GetMapping("/{mercadoId}/editar")
    @PreAuthorize("hasAuthority('MERCADOS_PLANO_EDITAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'OPERADOR', 'JEFE_OFICINA')")
    public String editorPlano(
            @PathVariable UUID mercadoId,
            @RequestParam(required = false) UUID versionId,
            Model model,
            RedirectAttributes redirectAttributes,
            Authentication auth) {
        MercadoMunicipal mercado = mercadoRepository.findById(mercadoId).orElse(null);
        if (mercado == null) {
            redirectAttributes.addFlashAttribute("error", "Mercado no encontrado.");
            return "redirect:/dashboard/mercados/planos";
        }

        try {
            PlanoMercadoDetailResponse detalle = consultaService.obtenerPlanoDetalleParaVisor(mercadoId, versionId);

            // Si la versión seleccionada está publicada o archivada, o no se especificó borrador,
            // verificar si existe un borrador editable o sugerir crear uno nuevo
            VersionPlanoResponse ver = detalle.getVersionSeleccionada();
            if (ver != null && ver.getEstado().name().equals("PUBLICADA")) {
                // Buscar si existe un borrador pendiente
                List<VersionPlanoResponse> versiones = versionService.listarVersiones(detalle.getId());
                Optional<VersionPlanoResponse> borradorExistente = versiones.stream()
                        .filter(v -> v.getEstado().name().equals("BORRADOR") || v.getEstado().name().equals("EN_REVISION"))
                        .findFirst();

                if (borradorExistente.isPresent()) {
                    return "redirect:/dashboard/mercados/planos/" + mercadoId + "/editar?versionId=" + borradorExistente.get().getId();
                } else {
                    // Crear automáticamente un nuevo borrador clonado desde la versión publicada vigente
                    String username = auth != null ? auth.getName() : "SISTEMA";
                    VersionPlanoResponse nuevoBorrador = versionService.restaurarComoBorrador(detalle.getId(), ver.getId(), username, null);
                    return "redirect:/dashboard/mercados/planos/" + mercadoId + "/editar?versionId=" + nuevoBorrador.getId();
                }
            }

            model.addAttribute("mercado", mercado);
            model.addAttribute("detalle", detalle);
            model.addAttribute("versionEditable", ver);
            model.addAttribute("tiposElementos", TipoElementoPlano.values());
            model.addAttribute("sectores", sectorRepository.findByMercadoIdOrderByOrdenVisualAsc(mercadoId));
            model.addAttribute("pabellones", pabellonRepository.findByMercadoIdOrderByOrdenVisualAsc(mercadoId));

            // Puestos del mercado disponibles para vincular
            List<PuestoComercial> puestos = puestoRepository.findByMercadoId(mercadoId);
            model.addAttribute("puestos", puestos);

            return "admin/mercados/planos/editor";
        } catch (Exception e) {
            log.warn("Error al abrir editor para mercado {}", mercadoId, e);
            return "redirect:/dashboard/mercados/planos/" + mercadoId + "/crear";
        }
    }

    /**
     * Formulario para crear un plano inicial si el mercado aún no cuenta con uno.
     */
    @GetMapping("/{mercadoId}/crear")
    @PreAuthorize("hasAuthority('MERCADOS_PLANO_CREAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'OPERADOR', 'JEFE_OFICINA')")
    public String formularioCrearPlano(@PathVariable UUID mercadoId, Model model, RedirectAttributes redirectAttributes) {
        MercadoMunicipal mercado = mercadoRepository.findById(mercadoId).orElse(null);
        if (mercado == null) {
            redirectAttributes.addFlashAttribute("error", "Mercado no encontrado.");
            return "redirect:/dashboard/mercados/planos";
        }

        // Si ya existe un plano activo, redirigir al visor
        Optional<PlanoMercadoResponse> existente = planoService.obtenerPlanoActivoPorMercado(mercadoId);
        if (existente.isPresent()) {
            return "redirect:/dashboard/mercados/planos/" + mercadoId;
        }

        PlanoMercadoCreateRequest form = PlanoMercadoCreateRequest.builder()
                .mercadoId(mercadoId)
                .codigo("PLN-" + mercado.getCodigo())
                .nombre("Plano General - " + mercado.getNombre())
                .descripcion("Distribución física y espacial de puestos comerciales de " + mercado.getNombre())
                .anchoBase(new BigDecimal("1400.00"))
                .altoBase(new BigDecimal("900.00"))
                .unidadReferencia("PIXEL")
                .build();

        model.addAttribute("mercado", mercado);
        model.addAttribute("form", form);
        return "admin/mercados/planos/crear";
    }

    /**
     * Procesa la creación del plano inicial.
     */
    @PostMapping("/{mercadoId}/crear")
    @PreAuthorize("hasAuthority('MERCADOS_PLANO_CREAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'OPERADOR', 'JEFE_OFICINA')")
    public String guardarPlanoInicial(
            @PathVariable UUID mercadoId,
            @Valid @ModelAttribute("form") PlanoMercadoCreateRequest form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes,
            Authentication auth) {
        MercadoMunicipal mercado = mercadoRepository.findById(mercadoId).orElse(null);
        if (mercado == null) {
            redirectAttributes.addFlashAttribute("error", "Mercado no encontrado.");
            return "redirect:/dashboard/mercados/planos";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("mercado", mercado);
            return "admin/mercados/planos/crear";
        }

        try {
            form.setMercadoId(mercadoId);
            String username = auth != null ? auth.getName() : "SISTEMA";
            planoService.crearPlano(form, username, null);
            redirectAttributes.addFlashAttribute("exito", "Plano creado con éxito. Ahora puedes configurar su distribución visual en el editor.");
            return "redirect:/dashboard/mercados/planos/" + mercadoId + "/editar";
        } catch (BusinessException e) {
            model.addAttribute("mercado", mercado);
            model.addAttribute("error", e.getMessage());
            return "admin/mercados/planos/crear";
        }
    }

    /**
     * Historial de versiones del plano de un mercado.
     */
    @GetMapping("/{mercadoId}/versiones")
    @PreAuthorize("hasAuthority('MERCADOS_PLANO_VERSIONES_VER') or hasAnyRole('ADMIN', 'SUPERADMIN', 'OPERADOR', 'JEFE_OFICINA')")
    public String historialVersiones(
            @PathVariable UUID mercadoId,
            Model model,
            RedirectAttributes redirectAttributes) {
        MercadoMunicipal mercado = mercadoRepository.findById(mercadoId).orElse(null);
        if (mercado == null) {
            redirectAttributes.addFlashAttribute("error", "Mercado no encontrado.");
            return "redirect:/dashboard/mercados/planos";
        }

        try {
            PlanoMercadoDetailResponse detalle = consultaService.obtenerPlanoDetalleParaVisor(mercadoId, null);
            List<VersionPlanoResponse> versiones = versionService.listarVersiones(detalle.getId());

            model.addAttribute("mercado", mercado);
            model.addAttribute("plano", detalle);
            model.addAttribute("versiones", versiones);
            return "admin/mercados/planos/versiones";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("advertencia", "El mercado aún no cuenta con un plano configurado.");
            return "redirect:/dashboard/mercados/planos/" + mercadoId + "/crear";
        }
    }

    /**
     * Restaura una versión histórica como un nuevo borrador desde la vista de versiones.
     */
    @PostMapping("/{mercadoId}/versiones/{versionOrigenId}/restaurar")
    @PreAuthorize("hasAuthority('MERCADOS_PLANO_RESTAURAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'OPERADOR', 'JEFE_OFICINA')")
    public String restaurarVersion(
            @PathVariable UUID mercadoId,
            @PathVariable UUID versionOrigenId,
            RedirectAttributes redirectAttributes,
            Authentication auth) {
        try {
            PlanoMercadoDetailResponse detalle = consultaService.obtenerPlanoDetalleParaVisor(mercadoId, null);
            String username = auth != null ? auth.getName() : "SISTEMA";
            VersionPlanoResponse nuevoBorrador = versionService.restaurarComoBorrador(detalle.getId(), versionOrigenId, username, null);
            redirectAttributes.addFlashAttribute("exito", "Versión restaurada con éxito como borrador v" + nuevoBorrador.getNumeroVersion());
            return "redirect:/dashboard/mercados/planos/" + mercadoId + "/editar?versionId=" + nuevoBorrador.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al restaurar versión: " + e.getMessage());
            return "redirect:/dashboard/mercados/planos/" + mercadoId + "/versiones";
        }
    }
}
