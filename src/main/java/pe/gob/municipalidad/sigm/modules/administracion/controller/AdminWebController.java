package pe.gob.municipalidad.sigm.modules.administracion.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pe.gob.municipalidad.sigm.modules.administracion.service.AuditoriaAdministrativaService;
import pe.gob.municipalidad.sigm.modules.administracion.service.CargoAdminService;
import pe.gob.municipalidad.sigm.modules.administracion.service.OficinaAdminService;
import pe.gob.municipalidad.sigm.modules.administracion.service.PermisoAdminService;
import pe.gob.municipalidad.sigm.modules.administracion.service.RolAdminService;
import pe.gob.municipalidad.sigm.modules.administracion.service.TipoActorAdminService;
import pe.gob.municipalidad.sigm.modules.administracion.service.UsuarioAdminService;

/**
 * Controlador Web MVC para vistas Thymeleaf de la interfaz de administración de SIGM-QUINUA.
 */
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ADMINISTRACION_MANAGE')")
public class AdminWebController {

    private final UsuarioAdminService usuarioService;
    private final RolAdminService rolService;
    private final PermisoAdminService permisoService;
    private final OficinaAdminService oficinaService;
    private final CargoAdminService cargoService;
    private final TipoActorAdminService tipoActorService;
    private final AuditoriaAdministrativaService auditoriaService;

    @GetMapping({"", "/", "/usuarios"})
    public String vistaUsuarios(
            @RequestParam(required = false) String term,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(required = false) Boolean locked,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model
    ) {
        PageRequest pr = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        model.addAttribute("usuariosPage", usuarioService.buscarUsuarios(term, enabled, locked, pr));
        model.addAttribute("rolesDisponibles", rolService.listarTodosActivos());
        model.addAttribute("oficinasDisponibles", oficinaService.listarTodasActivas());
        model.addAttribute("cargosDisponibles", cargoService.listarTodosActivos());
        model.addAttribute("actoresDisponibles", tipoActorService.listarTodosActivos());
        model.addAttribute("term", term);
        model.addAttribute("moduloActivo", "usuarios");
        return "admin/usuarios";
    }

    @GetMapping("/roles")
    public String vistaRoles(
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) String estado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model
    ) {
        PageRequest pr = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "nombre"));
        model.addAttribute("rolesPage", rolService.buscarRoles(busqueda, estado, pr));
        model.addAttribute("permisosDisponibles", permisoService.listarTodos());
        model.addAttribute("busqueda", busqueda);
        model.addAttribute("moduloActivo", "roles");
        return "admin/roles";
    }

    @GetMapping("/permisos")
    public String vistaPermisos(
            @RequestParam(required = false) Long rolId,
            Model model
    ) {
        model.addAttribute("matriz", permisoService.obtenerMatrizPermisos(rolId));
        model.addAttribute("rolesDisponibles", rolService.listarTodosActivos());
        model.addAttribute("rolSeleccionadoId", rolId);
        model.addAttribute("moduloActivo", "permisos");
        return "admin/permisos";
    }

    @GetMapping("/oficinas")
    public String vistaOficinas(
            @RequestParam(required = false) String term,
            @RequestParam(required = false) String estado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model
    ) {
        PageRequest pr = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "nombre"));
        model.addAttribute("oficinasPage", oficinaService.buscarOficinas(term, estado, pr));
        model.addAttribute("oficinasTodas", oficinaService.listarTodasActivas());
        model.addAttribute("arbolJerarquico", oficinaService.obtenerArbolJerarquico());
        model.addAttribute("term", term);
        model.addAttribute("moduloActivo", "oficinas");
        return "admin/oficinas";
    }

    @GetMapping("/cargos")
    public String vistaCargos(
            @RequestParam(required = false) String term,
            @RequestParam(required = false) String estado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model
    ) {
        PageRequest pr = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "nombre"));
        model.addAttribute("cargosPage", cargoService.buscarCargos(term, estado, pr));
        model.addAttribute("term", term);
        model.addAttribute("moduloActivo", "cargos");
        return "admin/cargos";
    }

    @GetMapping("/actores")
    public String vistaActores(
            @RequestParam(required = false) String term,
            @RequestParam(required = false) String estado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model
    ) {
        PageRequest pr = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "nombre"));
        model.addAttribute("actoresPage", tipoActorService.buscarTiposActores(term, estado, pr));
        model.addAttribute("term", term);
        model.addAttribute("moduloActivo", "actores");
        return "admin/actores";
    }

    @GetMapping("/auditoria")
    public String vistaAuditoria(
            @RequestParam(required = false) String usuario,
            @RequestParam(required = false) String entidad,
            @RequestParam(required = false) String accion,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            Model model
    ) {
        PageRequest pr = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        model.addAttribute("auditoriaPage", auditoriaService.buscarAuditoria(usuario, entidad, accion, pr));
        model.addAttribute("usuarioFiltro", usuario);
        model.addAttribute("entidadFiltro", entidad);
        model.addAttribute("accionFiltro", accion);
        model.addAttribute("moduloActivo", "auditoria");
        return "admin/auditoria";
    }
}
