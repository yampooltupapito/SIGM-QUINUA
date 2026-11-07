package pe.gob.municipalidad.sigm.modules.serenazgo.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import pe.gob.municipalidad.sigm.modules.serenazgo.service.RecursosSeguridadService;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.UnidadesMovilesService;

import java.util.UUID;

@Slf4j
@Controller
@RequestMapping("/dashboard/serenazgo/unidades")
@RequiredArgsConstructor
public class UnidadesMovilesWebController {

    private final UnidadesMovilesService unidadesService;
    private final RecursosSeguridadService recursosService;
    private final OficinaRepository oficinaRepository;
    private final PersonalSerenazgoRepository personalRepository;

    @GetMapping
    @PreAuthorize("hasAuthority('SERENAZGO_UNIDADES_VER') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA', 'SERENO')")
    public String listar(@ModelAttribute UnidadMovilFilter filter,
                         @RequestParam(defaultValue = "0") int page,
                         @RequestParam(defaultValue = "10") int size,
                         Model model) {
        Page<UnidadMovilResponse> unidades = unidadesService.listar(
                filter, PageRequest.of(page, size, Sort.by("createdAt").descending()));

        model.addAttribute("unidades", unidades);
        model.addAttribute("filter", filter);
        model.addAttribute("dashboard", recursosService.obtenerDashboardRecursos());
        model.addAttribute("tiposUnidad", TipoUnidadMovil.values());
        model.addAttribute("estadosOperativos", EstadoUnidadMovil.values());
        model.addAttribute("oficinas", oficinaRepository.findByEstado("ACTIVO"));
        model.addAttribute("moduloActivo", "serenazgo");
        model.addAttribute("submenuActivo", "unidades");

        return "admin/serenazgo/unidades/listado";
    }

    @GetMapping("/nueva")
    @PreAuthorize("hasAuthority('SERENAZGO_UNIDADES_CREAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public String formularioNueva(Model model) {
        model.addAttribute("request", new UnidadMovilCreateRequest());
        model.addAttribute("tiposUnidad", TipoUnidadMovil.values());
        model.addAttribute("oficinas", oficinaRepository.findByEstado("ACTIVO"));
        model.addAttribute("moduloActivo", "serenazgo");
        model.addAttribute("submenuActivo", "unidades");
        return "admin/serenazgo/unidades/formulario";
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SERENAZGO_UNIDADES_CREAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public String registrar(@Valid @ModelAttribute("request") UnidadMovilCreateRequest request,
                            BindingResult bindingResult,
                            Authentication auth,
                            RedirectAttributes redirect,
                            Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("tiposUnidad", TipoUnidadMovil.values());
            model.addAttribute("oficinas", oficinaRepository.findByEstado("ACTIVO"));
            model.addAttribute("moduloActivo", "serenazgo");
            model.addAttribute("submenuActivo", "unidades");
            return "admin/serenazgo/unidades/formulario";
        }

        try {
            UnidadMovilResponse resp = unidadesService.registrar(request, auth.getName());
            redirect.addFlashAttribute("mensajeExito", "Unidad móvil " + resp.getCodigoInterno() + " registrada exitosamente.");
            return "redirect:/dashboard/serenazgo/unidades/" + resp.getId();
        } catch (Exception e) {
            log.error("Error al registrar unidad móvil", e);
            model.addAttribute("error", e.getMessage());
            model.addAttribute("tiposUnidad", TipoUnidadMovil.values());
            model.addAttribute("oficinas", oficinaRepository.findByEstado("ACTIVO"));
            model.addAttribute("moduloActivo", "serenazgo");
            model.addAttribute("submenuActivo", "unidades");
            return "admin/serenazgo/unidades/formulario";
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SERENAZGO_UNIDADES_VER') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA', 'SERENO')")
    public String verDetalle(@PathVariable UUID id, Model model) {
        UnidadMovilDetailResponse detalle = unidadesService.obtenerDetalle(id);
        model.addAttribute("unidad", detalle);
        model.addAttribute("personalActivo", personalRepository.findByEstado(EstadoPersonalSerenazgo.ACTIVO));
        model.addAttribute("tiposMantenimiento", TipoMantenimientoRecurso.values());
        model.addAttribute("tiposIncidencia", TipoIncidenciaTecnica.values());
        model.addAttribute("nivelesGravedad", NivelGravedadIncidencia.values());
        model.addAttribute("estadosOperativos", EstadoUnidadMovil.values());
        model.addAttribute("moduloActivo", "serenazgo");
        model.addAttribute("submenuActivo", "unidades");
        return "admin/serenazgo/unidades/detalle";
    }

    @GetMapping("/{id}/editar")
    @PreAuthorize("hasAuthority('SERENAZGO_UNIDADES_EDITAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public String formularioEditar(@PathVariable UUID id, Model model) {
        UnidadMovilDetailResponse detalle = unidadesService.obtenerDetalle(id);
        UnidadMovilUpdateRequest req = UnidadMovilUpdateRequest.builder()
                .tipoUnidad(detalle.getTipoUnidad())
                .placa(detalle.getPlaca())
                .marca(detalle.getMarca())
                .modelo(detalle.getModelo())
                .anio(detalle.getAnio())
                .color(detalle.getColor())
                .numeroMotor(detalle.getNumeroMotor())
                .numeroChasis(detalle.getNumeroChasis())
                .codigoPatrimonial(detalle.getCodigoPatrimonial())
                .oficinaResponsableId(detalle.getOficinaId())
                .kilometrajeActual(detalle.getKilometrajeActual())
                .observaciones(detalle.getObservaciones())
                .build();

        model.addAttribute("unidadId", id);
        model.addAttribute("codigoInterno", detalle.getCodigoInterno());
        model.addAttribute("request", req);
        model.addAttribute("tiposUnidad", TipoUnidadMovil.values());
        model.addAttribute("oficinas", oficinaRepository.findByEstado("ACTIVO"));
        model.addAttribute("moduloActivo", "serenazgo");
        model.addAttribute("submenuActivo", "unidades");
        return "admin/serenazgo/unidades/editar";
    }

    @PostMapping("/{id}/editar")
    @PreAuthorize("hasAuthority('SERENAZGO_UNIDADES_EDITAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public String actualizar(@PathVariable UUID id,
                             @Valid @ModelAttribute("request") UnidadMovilUpdateRequest request,
                             BindingResult bindingResult,
                             Authentication auth,
                             RedirectAttributes redirect,
                             Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("unidadId", id);
            model.addAttribute("tiposUnidad", TipoUnidadMovil.values());
            model.addAttribute("oficinas", oficinaRepository.findByEstado("ACTIVO"));
            model.addAttribute("moduloActivo", "serenazgo");
            model.addAttribute("submenuActivo", "unidades");
            return "admin/serenazgo/unidades/editar";
        }

        try {
            unidadesService.actualizar(id, request, auth.getName());
            redirect.addFlashAttribute("mensajeExito", "Unidad móvil actualizada exitosamente.");
            return "redirect:/dashboard/serenazgo/unidades/" + id;
        } catch (Exception e) {
            log.error("Error al actualizar unidad móvil", e);
            model.addAttribute("unidadId", id);
            model.addAttribute("error", e.getMessage());
            model.addAttribute("tiposUnidad", TipoUnidadMovil.values());
            model.addAttribute("oficinas", oficinaRepository.findByEstado("ACTIVO"));
            model.addAttribute("moduloActivo", "serenazgo");
            model.addAttribute("submenuActivo", "unidades");
            return "admin/serenazgo/unidades/editar";
        }
    }

    @PostMapping("/{id}/estado")
    @PreAuthorize("hasAuthority('SERENAZGO_UNIDADES_CAMBIAR_ESTADO') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public String cambiarEstado(@PathVariable UUID id,
                                @ModelAttribute CambioEstadoUnidadRequest request,
                                Authentication auth,
                                RedirectAttributes redirect) {
        try {
            unidadesService.cambiarEstado(id, request, auth.getName());
            redirect.addFlashAttribute("mensajeExito", "Estado operativo actualizado.");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/dashboard/serenazgo/unidades/" + id;
    }

    @PostMapping("/{id}/asignar")
    @PreAuthorize("hasAuthority('SERENAZGO_UNIDADES_ASIGNAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA')")
    public String asignar(@PathVariable UUID id,
                          @ModelAttribute AsignarUnidadRequest request,
                          Authentication auth,
                          RedirectAttributes redirect) {
        try {
            unidadesService.asignar(id, request, auth.getName());
            redirect.addFlashAttribute("mensajeExito", "Unidad móvil asignada correctamente en servicio.");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/dashboard/serenazgo/unidades/" + id;
    }

    @PostMapping("/{id}/devolver")
    @PreAuthorize("hasAuthority('SERENAZGO_UNIDADES_ASIGNAR') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA')")
    public String devolver(@PathVariable UUID id,
                           @ModelAttribute DevolverUnidadRequest request,
                           Authentication auth,
                           RedirectAttributes redirect) {
        try {
            unidadesService.devolver(id, request, auth.getName());
            redirect.addFlashAttribute("mensajeExito", "Recepción y devolución de unidad registrada con éxito.");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/dashboard/serenazgo/unidades/" + id;
    }

    @PostMapping("/{id}/mantenimiento")
    @PreAuthorize("hasAuthority('SERENAZGO_UNIDADES_MANTENIMIENTO') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA')")
    public String registrarMantenimiento(@PathVariable UUID id,
                                         @ModelAttribute RegistroMantenimientoRequest request,
                                         Authentication auth,
                                         RedirectAttributes redirect) {
        try {
            request.setTipoRecurso(TipoRecursoSeguridad.UNIDAD_MOVIL);
            unidadesService.registrarMantenimiento(id, request, auth.getName());
            redirect.addFlashAttribute("mensajeExito", "Mantenimiento técnico programado exitosamente.");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/dashboard/serenazgo/unidades/" + id;
    }

    @PostMapping("/{id}/incidencia")
    @PreAuthorize("hasAuthority('SERENAZGO_UNIDADES_INCIDENCIAS') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA', 'SERENO')")
    public String registrarIncidencia(@PathVariable UUID id,
                                      @ModelAttribute RegistroIncidenciaTecnicaRequest request,
                                      Authentication auth,
                                      RedirectAttributes redirect) {
        try {
            request.setTipoRecurso(TipoRecursoSeguridad.UNIDAD_MOVIL);
            unidadesService.registrarIncidencia(id, request, auth.getName());
            redirect.addFlashAttribute("mensajeExito", "Incidencia técnica registrada correctamente.");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/dashboard/serenazgo/unidades/" + id;
    }

    @GetMapping("/{id}/ficha-pdf")
    @PreAuthorize("hasAuthority('SERENAZGO_UNIDADES_VER') or hasAnyRole('ADMIN', 'SUPERADMIN', 'JEFE_OFICINA')")
    public ResponseEntity<byte[]> descargarFichaPdf(@PathVariable UUID id) {
        byte[] pdf = unidadesService.generarFichaPdf(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"ficha_unidad_" + id + ".pdf\"")
                .body(pdf);
    }
}
