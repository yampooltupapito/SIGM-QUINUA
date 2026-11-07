package pe.gob.municipalidad.sigm.modules.administracion.perfil.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import pe.gob.municipalidad.sigm.modules.administracion.perfil.dto.PerfilDtos;
import pe.gob.municipalidad.sigm.modules.administracion.perfil.service.PerfilService;

@Controller
@RequiredArgsConstructor
public class PerfilWebController {

    private final PerfilService perfilService;

    @GetMapping({"/perfil", "/admin/perfil", "/dashboard/perfil"})
    public String verPerfil(Authentication auth, Model model) {
        String username = auth.getName();
        PerfilDtos.PerfilResponse perfil = perfilService.obtenerPerfilUsuario(username);
        PerfilDtos.PermisosConsolidadosDto permisos = perfilService.obtenerPermisosUsuario(username);
        var asignaciones = perfilService.obtenerAsignacionesUsuario(username);
        var preferencias = perfilService.obtenerPreferencias(username);
        var sesiones = perfilService.obtenerSesionesActivas(username);
        var actividad = perfilService.obtenerActividadSeguridad(username, PageRequest.of(0, 10));

        model.addAttribute("perfil", perfil);
        model.addAttribute("permisos", permisos);
        model.addAttribute("asignaciones", asignaciones);
        model.addAttribute("preferencias", preferencias);
        model.addAttribute("sesiones", sesiones);
        model.addAttribute("actividad", actividad);

        return "admin/perfil/index";
    }
}
