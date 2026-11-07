package pe.gob.municipalidad.sigm.modules.serenazgo.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.gob.municipalidad.sigm.common.repository.OficinaRepository;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.repository.PersonalSerenazgoRepository;
import pe.gob.municipalidad.sigm.modules.serenazgo.repository.UnidadMovilSerenazgoRepository;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.EquipamientoSerenazgoService;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.RecursosSeguridadService;

import java.util.UUID;

@Slf4j
@Controller
@RequestMapping("/dashboard/serenazgo/equipamiento")
@RequiredArgsConstructor
public class EquipamientoWebController {

    private final EquipamientoSerenazgoService equipamientoService;
    private final RecursosSeguridadService recursosService;
    private final OficinaRepository oficinaRepository;
    private final PersonalSerenazgoRepository personalRepository;
    private final UnidadMovilSerenazgoRepository unidadRepository;

    @GetMapping
    @PreAuthorize("hasAuthority('SERENAZGO_EQUIPAMIENTO_VER') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA', 'SERENO')")
    public String listar(@ModelAttribute EquipamientoFilter filter,
                         @RequestParam(defaultValue = "0") int page,
                         @RequestParam(defaultValue = "10") int size,
                         Model model) {
        Page<EquipamientoResponse> equipos = equipamientoService.listar(
                filter, PageRequest.of(page, size, Sort.by("createdAt").descending()));

        model.addAttribute("equipos", equipos);
        model.addAttribute("filter", filter);
        model.addAttribute("dashboard", recursosService.obtenerDashboardRecursos());
        model.addAttribute("tiposEquipo", TipoEquipamientoSerenazgo.values());
        model.addAttribute("estadosEquipo", EstadoEquipamientoSerenazgo.values());
        model.addAttribute("oficinas", oficinaRepository.findByEstado("ACTIVO"));
        model.addAttribute("moduloActivo", "serenazgo");
        model.addAttribute("submenuActivo", "equipamiento");

        return "admin/serenazgo/equipamiento/listado";
    }

    @GetMapping("/nuevo")
    @PreAuthorize("hasAuthority('SERENAZGO_EQUIPAMIENTO_CREAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public String formularioNuevo(Model model) {
        model.addAttribute("request", new EquipamientoCreateRequest());
        model.addAttribute("tiposEquipo", TipoEquipamientoSerenazgo.values());
        model.addAttribute("oficinas", oficinaRepository.findByEstado("ACTIVO"));
        model.addAttribute("moduloActivo", "serenazgo");
        model.addAttribute("submenuActivo", "equipamiento");
        return "admin/serenazgo/equipamiento/formulario";
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SERENAZGO_EQUIPAMIENTO_CREAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public String registrar(@Valid @ModelAttribute("request") EquipamientoCreateRequest request,
                            BindingResult bindingResult,
                            Authentication auth,
                            RedirectAttributes redirect,
                            Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("tiposEquipo", TipoEquipamientoSerenazgo.values());
            model.addAttribute("oficinas", oficinaRepository.findByEstado("ACTIVO"));
            model.addAttribute("moduloActivo", "serenazgo");
            model.addAttribute("submenuActivo", "equipamiento");
            return "admin/serenazgo/equipamiento/formulario";
        }

        try {
            EquipamientoResponse resp = equipamientoService.registrar(request, auth.getName());
            redirect.addFlashAttribute("mensajeExito", "Equipamiento " + resp.getCodigoInterno() + " registrado exitosamente.");
            return "redirect:/dashboard/serenazgo/equipamiento/" + resp.getId();
        } catch (Exception e) {
            log.error("Error al registrar equipamiento", e);
            model.addAttribute("error", e.getMessage());
            model.addAttribute("tiposEquipo", TipoEquipamientoSerenazgo.values());
            model.addAttribute("oficinas", oficinaRepository.findByEstado("ACTIVO"));
            model.addAttribute("moduloActivo", "serenazgo");
            model.addAttribute("submenuActivo", "equipamiento");
            return "admin/serenazgo/equipamiento/formulario";
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SERENAZGO_EQUIPAMIENTO_VER') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA', 'SERENO')")
    public String verDetalle(@PathVariable UUID id, Model model) {
        EquipamientoDetailResponse detalle = equipamientoService.obtenerDetalle(id);
        model.addAttribute("equipo", detalle);
        model.addAttribute("personalActivo", personalRepository.findByEstado(EstadoPersonalSerenazgo.ACTIVO));
        model.addAttribute("unidadesActivas", unidadRepository.findAll());
        model.addAttribute("tiposMantenimiento", TipoMantenimientoRecurso.values());
        model.addAttribute("tiposIncidencia", TipoIncidenciaTecnica.values());
        model.addAttribute("nivelesGravedad", NivelGravedadIncidencia.values());
        model.addAttribute("moduloActivo", "serenazgo");
        model.addAttribute("submenuActivo", "equipamiento");
        return "admin/serenazgo/equipamiento/detalle";
    }

    @GetMapping("/{id}/editar")
    @PreAuthorize("hasAuthority('SERENAZGO_EQUIPAMIENTO_EDITAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public String formularioEditar(@PathVariable UUID id, Model model) {
        EquipamientoDetailResponse detalle = equipamientoService.obtenerDetalle(id);
        EquipamientoUpdateRequest req = EquipamientoUpdateRequest.builder()
                .descripcion(detalle.getDescripcion())
                .marca(detalle.getMarca())
                .modelo(detalle.getModelo())
                .numeroSerie(detalle.getNumeroSerie())
                .codigoPatrimonial(detalle.getCodigoPatrimonial())
                .oficinaResponsableId(detalle.getOficinaResponsableId())
                .stockTotal(detalle.getStockTotal())
                .observaciones(detalle.getObservaciones())
                .build();

        model.addAttribute("equipoId", id);
        model.addAttribute("codigoInterno", detalle.getCodigoInterno());
        model.addAttribute("esControladoPorCantidad", detalle.getEsControladoPorCantidad());
        model.addAttribute("request", req);
        model.addAttribute("oficinas", oficinaRepository.findByEstado("ACTIVO"));
        model.addAttribute("moduloActivo", "serenazgo");
        model.addAttribute("submenuActivo", "equipamiento");
        return "admin/serenazgo/equipamiento/editar";
    }

    @PostMapping("/{id}/editar")
    @PreAuthorize("hasAuthority('SERENAZGO_EQUIPAMIENTO_EDITAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public String actualizar(@PathVariable UUID id,
                             @Valid @ModelAttribute("request") EquipamientoUpdateRequest request,
                             BindingResult bindingResult,
                             Authentication auth,
                             RedirectAttributes redirect,
                             Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("equipoId", id);
            model.addAttribute("oficinas", oficinaRepository.findByEstado("ACTIVO"));
            model.addAttribute("moduloActivo", "serenazgo");
            model.addAttribute("submenuActivo", "equipamiento");
            return "admin/serenazgo/equipamiento/editar";
        }

        try {
            equipamientoService.actualizar(id, request, auth.getName());
            redirect.addFlashAttribute("mensajeExito", "Equipamiento actualizado exitosamente.");
            return "redirect:/dashboard/serenazgo/equipamiento/" + id;
        } catch (Exception e) {
            log.error("Error al actualizar equipamiento", e);
            model.addAttribute("equipoId", id);
            model.addAttribute("error", e.getMessage());
            model.addAttribute("oficinas", oficinaRepository.findByEstado("ACTIVO"));
            model.addAttribute("moduloActivo", "serenazgo");
            model.addAttribute("submenuActivo", "equipamiento");
            return "admin/serenazgo/equipamiento/editar";
        }
    }

    @PostMapping("/{id}/asignar")
    @PreAuthorize("hasAuthority('SERENAZGO_EQUIPAMIENTO_ASIGNAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA')")
    public String asignar(@PathVariable UUID id,
                          @ModelAttribute AsignarEquipamientoRequest request,
                          Authentication auth,
                          RedirectAttributes redirect) {
        try {
            equipamientoService.asignar(id, request, auth.getName());
            redirect.addFlashAttribute("mensajeExito", "Equipamiento asignado correctamente.");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/dashboard/serenazgo/equipamiento/" + id;
    }

    @PostMapping("/{id}/devolver")
    @PreAuthorize("hasAuthority('SERENAZGO_EQUIPAMIENTO_ASIGNAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA')")
    public String devolver(@PathVariable UUID id,
                           @ModelAttribute DevolverEquipamientoRequest request,
                           Authentication auth,
                           RedirectAttributes redirect) {
        try {
            equipamientoService.devolver(id, request, auth.getName());
            redirect.addFlashAttribute("mensajeExito", "Devolución de equipamiento registrada.");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/dashboard/serenazgo/equipamiento/" + id;
    }

    @PostMapping("/{id}/mantenimiento")
    @PreAuthorize("hasAuthority('SERENAZGO_UNIDADES_MANTENIMIENTO') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA')")
    public String registrarMantenimiento(@PathVariable UUID id,
                                         @ModelAttribute RegistroMantenimientoRequest request,
                                         Authentication auth,
                                         RedirectAttributes redirect) {
        try {
            request.setTipoRecurso(TipoRecursoSeguridad.EQUIPAMIENTO);
            equipamientoService.registrarMantenimiento(id, request, auth.getName());
            redirect.addFlashAttribute("mensajeExito", "Mantenimiento técnico programado.");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/dashboard/serenazgo/equipamiento/" + id;
    }

    @PostMapping("/{id}/incidencia")
    @PreAuthorize("hasAuthority('SERENAZGO_UNIDADES_INCIDENCIAS') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA', 'SERENO')")
    public String registrarIncidencia(@PathVariable UUID id,
                                      @ModelAttribute RegistroIncidenciaTecnicaRequest request,
                                      Authentication auth,
                                      RedirectAttributes redirect) {
        try {
            request.setTipoRecurso(TipoRecursoSeguridad.EQUIPAMIENTO);
            equipamientoService.registrarIncidencia(id, request, auth.getName());
            redirect.addFlashAttribute("mensajeExito", "Incidencia técnica reportada.");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/dashboard/serenazgo/equipamiento/" + id;
    }
}
