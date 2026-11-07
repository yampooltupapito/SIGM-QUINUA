package pe.gob.municipalidad.sigm.security.authentication;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvDtos.ExpedienteResponse;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvSolicitudDtos.MisSolicitudesItemResponse;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoExpediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.mapper.MpvMapper;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.ExpedienteRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.SolicitudCiudadanaService;
import pe.gob.municipalidad.sigm.security.authorization.OfficeSecurityService;
import pe.gob.municipalidad.sigm.security.dto.LoginRequest;
import pe.gob.municipalidad.sigm.security.dto.ResetPasswordValidationResponse;
import pe.gob.municipalidad.sigm.security.service.AuthService;
import pe.gob.municipalidad.sigm.security.service.PasswordRecoveryService;

/**
 * Controlador Web MVC para las interfaces institucionales de:
 * - Login institucional (/login)
 * - Recuperación de contraseña (/forgot-password)
 * - Restablecimiento de contraseña (/reset-password)
 * - Cierre de sesión seguro (/logout)
 * - Página de bienvenida y dashboard personalizado por rol/oficina (/bienvenida)
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class AuthWebController {

    private final AuthService authService;
    private final PasswordRecoveryService passwordRecoveryService;
    private final OfficeSecurityService officeSecurityService;
    private final ExpedienteRepository expedienteRepository;
    private final SolicitudCiudadanaService solicitudCiudadanaService;
    private final MpvMapper mpvMapper;

    @GetMapping("/login")
    public String vistaLogin(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            @RequestParam(value = "reset", required = false) String reset,
            @RequestParam(value = "redirect", required = false) String redirect,
            HttpServletRequest request,
            Model model
    ) {
        ensureCsrfTokenMaterialized(request);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return "redirect:" + determineTargetUrl(auth, redirect);
        }

        if (logout != null) {
            model.addAttribute("logoutMessage", "Su sesión institucional ha sido cerrada de forma segura.");
        }
        if (reset != null) {
            model.addAttribute("resetSuccessMessage", "Su contraseña ha sido restablecida exitosamente. Ingrese con sus nuevas credenciales.");
        }
        if (error != null && !model.containsAttribute("errorMessage")) {
            model.addAttribute("errorMessage", "Credenciales de acceso incorrectas. Verifique su usuario y contraseña.");
        }

        model.addAttribute("redirect", sanitizeRedirect(redirect));
        return "auth/login";
    }

    @PostMapping("/login")
    public String procesarLogin(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam(value = "redirect", required = false) String redirect,
            HttpServletRequest request,
            HttpServletResponse response,
            RedirectAttributes redirectAttributes
    ) {
        try {
            LoginRequest loginRequest = new LoginRequest(username, password);
            pe.gob.municipalidad.sigm.security.dto.AuthResponse authResponse = authService.login(loginRequest, request, response);

            String target = determineTargetUrl(authResponse, redirect);
            return "redirect:" + target;

        } catch (BadCredentialsException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Credenciales de acceso incorrectas. Verifique su usuario y contraseña.");
            redirectAttributes.addFlashAttribute("prevUsername", username);
            return buildErrorRedirect(redirect);
        } catch (LockedException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "La cuenta ha sido bloqueada temporalmente por seguridad tras múltiples intentos fallidos. Contacte al administrador.");
            redirectAttributes.addFlashAttribute("prevUsername", username);
            return buildErrorRedirect(redirect);
        } catch (DisabledException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "La cuenta de usuario se encuentra deshabilitada o inactiva en el sistema.");
            redirectAttributes.addFlashAttribute("prevUsername", username);
            return buildErrorRedirect(redirect);
        } catch (Exception ex) {
            log.error("Error inesperado en inicio de sesión web: {}", ex.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Ocurrió un error al procesar el inicio de sesión. Intente nuevamente.");
            redirectAttributes.addFlashAttribute("prevUsername", username);
            return buildErrorRedirect(redirect);
        }
    }

    @GetMapping("/forgot-password")
    public String vistaForgotPassword(
            @RequestParam(required = false) String enviado,
            Model model
    ) {
        if (enviado != null && !model.containsAttribute("successMessage")) {
            model.addAttribute("successMessage",
                    "Si los datos ingresados corresponden a una cuenta activa registrada, se ha enviado un enlace seguro de recuperación.");
        }
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String procesarForgotPassword(
            @RequestParam String identificador,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes
    ) {
        String mensaje = passwordRecoveryService.solicitarRecuperacion(identificador, request);
        redirectAttributes.addFlashAttribute("successMessage", mensaje);
        return "redirect:/forgot-password?enviado=true";
    }

    @GetMapping("/reset-password")
    public String vistaResetPassword(
            @RequestParam(required = false) String token,
            Model model
    ) {
        if (token == null || token.isBlank()) {
            model.addAttribute("tokenInvalido", true);
            model.addAttribute("mensajeError", "El token de recuperación no fue proporcionado en el enlace.");
            return "auth/reset-password";
        }

        ResetPasswordValidationResponse val = passwordRecoveryService.validarToken(token);
        if (!val.isValid()) {
            model.addAttribute("tokenInvalido", true);
            model.addAttribute("mensajeError", val.getMessage());
        } else {
            model.addAttribute("tokenValido", true);
            model.addAttribute("token", token);
            model.addAttribute("username", val.getUsername());
        }

        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String procesarResetPassword(
            @RequestParam String token,
            @RequestParam String nuevaContrasena,
            @RequestParam String confirmarContrasena,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        try {
            passwordRecoveryService.restablecerContrasena(token, nuevaContrasena, confirmarContrasena, request);
            redirectAttributes.addFlashAttribute("resetSuccessMessage",
                    "Su contraseña ha sido actualizada con éxito. Ingrese al sistema con su nueva credencial.");
            return "redirect:/login?reset=success";
        } catch (BusinessException ex) {
            model.addAttribute("tokenValido", true);
            model.addAttribute("token", token);
            model.addAttribute("errorMessage", ex.getMessage());
            return "auth/reset-password";
        }
    }

    @GetMapping("/logout")
    public String cerrarSesionGet(HttpServletRequest request, HttpServletResponse response) {
        authService.logout(null, request, response);
        return "redirect:/login?logout=true";
    }

    @PostMapping("/logout")
    public String cerrarSesionPost(HttpServletRequest request, HttpServletResponse response) {
        authService.logout(null, request, response);
        return "redirect:/login?logout=true";
    }

    @GetMapping("/bienvenida")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public String vistaBienvenida(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return "redirect:/login";
        }

        boolean esAdmin = auth.getAuthorities().stream().anyMatch(a ->
                "ROLE_ADMIN".equals(a.getAuthority()) ||
                "ROLE_SUPERADMIN".equals(a.getAuthority()) ||
                "ADMINISTRACION_MANAGE".equals(a.getAuthority()));

        boolean esJefeOficina = !esAdmin && auth.getAuthorities().stream().anyMatch(a ->
                "ROLE_JEFE_OFICINA".equals(a.getAuthority()));

        boolean esOperador = !esAdmin && !esJefeOficina && auth.getAuthorities().stream().anyMatch(a ->
                "ROLE_OPERADOR".equals(a.getAuthority()) ||
                "ROLE_MESA_PARTES".equals(a.getAuthority()) ||
                "ROLE_SERENO".equals(a.getAuthority()) ||
                "ROLE_CAJERO".equals(a.getAuthority()));

        boolean esCiudadano = !esAdmin && !esJefeOficina && !esOperador;

        java.util.List<String> rolesPrincipales = auth.getAuthorities().stream()
                .map(org.springframework.security.core.GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .sorted()
                .toList();

        java.util.List<String> permisosTecnicos = auth.getAuthorities().stream()
                .map(org.springframework.security.core.GrantedAuthority::getAuthority)
                .filter(a -> !a.startsWith("ROLE_"))
                .sorted()
                .toList();

        long countMpv = permisosTecnicos.stream().filter(p -> p.startsWith("MESAPARTES_") || p.startsWith("DOCUMENTO_")).count();
        long countMercados = permisosTecnicos.stream().filter(p -> p.startsWith("MERCADOS_") || p.startsWith("PORTAL_COMERCIANTE_")).count();
        long countSerenazgo = permisosTecnicos.stream().filter(p -> p.startsWith("SERENAZGO_")).count();
        long countTributaria = permisosTecnicos.stream().filter(p -> p.startsWith("TRIBUTARIA_") || p.startsWith("PORTAL_TRIBUTARIO_")).count();

        Oficina oficinaUsuario = officeSecurityService.obtenerOficinaActiva(auth.getName());
        long kpiRecibidos = 0L;
        long kpiPendientes = 0L;
        long kpiEnProceso = 0L;
        long kpiObservados = 0L;
        long kpiFinalizados = 0L;
        java.util.List<ExpedienteResponse> expedientesOficina = java.util.List.of();

        if (oficinaUsuario != null && oficinaUsuario.getId() != null) {
            Long ofId = oficinaUsuario.getId();
            kpiRecibidos = expedienteRepository.countByOficinaResponsableId(ofId);
            kpiPendientes = expedienteRepository.countByOficinaResponsableIdAndEstadoActual(ofId, EstadoExpediente.PRESENTADO)
                    + expedienteRepository.countByOficinaResponsableIdAndEstadoActual(ofId, EstadoExpediente.RECIBIDO)
                    + expedienteRepository.countByOficinaResponsableIdAndEstadoActual(ofId, EstadoExpediente.DERIVADO);
            kpiEnProceso = expedienteRepository.countByOficinaResponsableIdAndEstadoActual(ofId, EstadoExpediente.EN_REVISION)
                    + expedienteRepository.countByOficinaResponsableIdAndEstadoActual(ofId, EstadoExpediente.EN_ATENCION)
                    + expedienteRepository.countByOficinaResponsableIdAndEstadoActual(ofId, EstadoExpediente.EN_SUBSANACION);
            kpiObservados = expedienteRepository.countByOficinaResponsableIdAndEstadoActual(ofId, EstadoExpediente.OBSERVADO);
            kpiFinalizados = expedienteRepository.countByOficinaResponsableIdAndEstadoActual(ofId, EstadoExpediente.RESUELTO)
                    + expedienteRepository.countByOficinaResponsableIdAndEstadoActual(ofId, EstadoExpediente.NOTIFICADO)
                    + expedienteRepository.countByOficinaResponsableIdAndEstadoActual(ofId, EstadoExpediente.ARCHIVADO);

            expedientesOficina = expedienteRepository.findByOficinaResponsableId(
                            ofId,
                            PageRequest.of(0, 20, Sort.by("createdAt").descending())
                    )
                    .map(mpvMapper::toExpedienteResponse)
                    .getContent();
        }

        java.util.List<MisSolicitudesItemResponse> misSolicitudesRecientes = java.util.List.of();
        long totalMisSolicitudes = 0L;
        if (esCiudadano) {
            try {
                org.springframework.data.domain.Page<MisSolicitudesItemResponse> pageCiudadano =
                        solicitudCiudadanaService.listarMisSolicitudes(
                                null,
                                null,
                                PageRequest.of(0, 10, Sort.by("createdAt").descending()),
                                auth.getName()
                        );
                misSolicitudesRecientes = pageCiudadano.getContent();
                totalMisSolicitudes = pageCiudadano.getTotalElements();
            } catch (Exception ex) {
                log.debug("No se pudieron cargar solicitudes ciudadanas para {}: {}", auth.getName(), ex.getMessage());
            }
        }

        model.addAttribute("moduloActivo", "dashboard");
        model.addAttribute("username", auth.getName());
        model.addAttribute("authorities", auth.getAuthorities());
        model.addAttribute("rolesPrincipales", rolesPrincipales.isEmpty() ? java.util.List.of("ROLE_USUARIO") : rolesPrincipales);
        model.addAttribute("permisosTecnicos", permisosTecnicos);
        model.addAttribute("totalPermisos", permisosTecnicos.size());
        model.addAttribute("countMpv", countMpv);
        model.addAttribute("countMercados", countMercados);
        model.addAttribute("countSerenazgo", countSerenazgo);
        model.addAttribute("countTributaria", countTributaria);
        model.addAttribute("esAdmin", esAdmin);
        model.addAttribute("esJefeOficina", esJefeOficina);
        model.addAttribute("esOperador", esOperador);
        model.addAttribute("esCiudadano", esCiudadano);
        model.addAttribute("oficinaUsuario", oficinaUsuario);
        model.addAttribute("kpiRecibidos", kpiRecibidos);
        model.addAttribute("kpiPendientes", kpiPendientes);
        model.addAttribute("kpiEnProceso", kpiEnProceso);
        model.addAttribute("kpiObservados", kpiObservados);
        model.addAttribute("kpiFinalizados", kpiFinalizados);
        model.addAttribute("expedientesOficina", expedientesOficina);
        model.addAttribute("misSolicitudesRecientes", misSolicitudesRecientes);
        model.addAttribute("totalMisSolicitudes", totalMisSolicitudes);
        return "pages/bienvenida";
    }

    public String determineTargetUrl(pe.gob.municipalidad.sigm.security.dto.AuthResponse authResponse, String requestedRedirect) {
        String safeRedirect = sanitizeRedirect(requestedRedirect);
        if (safeRedirect != null) {
            return safeRedirect;
        }

        if (authResponse == null) {
            return "/login";
        }

        boolean esAdmin = (authResponse.getRoles() != null && (authResponse.getRoles().contains("ROLE_ADMIN")
                || authResponse.getRoles().contains("ROLE_SUPERADMIN")
                || authResponse.getRoles().contains("ADMIN")))
                || (authResponse.getPermisos() != null && authResponse.getPermisos().contains("ADMINISTRACION_MANAGE"));

        if (esAdmin) {
            return "/admin/dashboard";
        }

        return "/bienvenida";
    }

    public String determineTargetUrl(Authentication authentication, String requestedRedirect) {
        String safeRedirect = sanitizeRedirect(requestedRedirect);
        if (safeRedirect != null) {
            return safeRedirect;
        }

        if (authentication == null) {
            return "/login";
        }

        boolean esAdmin = authentication.getAuthorities().stream().anyMatch(a ->
                "ROLE_ADMIN".equals(a.getAuthority()) ||
                "ROLE_SUPERADMIN".equals(a.getAuthority()) ||
                "ADMINISTRACION_MANAGE".equals(a.getAuthority()));

        if (esAdmin) {
            return "/admin/dashboard";
        }

        return "/bienvenida";
    }

    public String sanitizeRedirect(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }
        String trimmed = url.trim();

        // Debe comenzar con / y no con // ni /\ (previene Open Redirect protocol-relative)
        if (!trimmed.startsWith("/") || trimmed.startsWith("//") || trimmed.startsWith("/\\")) {
            return null;
        }

        // Evitar esquemas de protocolo externos y caracteres maliciosos
        if (trimmed.contains(":") || trimmed.contains("@") || trimmed.contains("\\")
                || trimmed.contains("\r") || trimmed.contains("\n") || trimmed.contains("%0d") || trimmed.contains("%0a")) {
            return null;
        }

        // Excluir endpoints de autenticación propios
        if (trimmed.startsWith("/login") || trimmed.startsWith("/logout")
                || trimmed.startsWith("/forgot-password") || trimmed.startsWith("/reset-password")) {
            return null;
        }

        return trimmed;
    }

    private String buildErrorRedirect(String redirect) {
        String safe = sanitizeRedirect(redirect);
        if (safe != null) {
            return "redirect:/login?error=true&redirect=" + safe;
        }
        return "redirect:/login?error=true";
    }

    private void ensureCsrfTokenMaterialized(HttpServletRequest request) {
        if (request == null) {
            return;
        }
        org.springframework.security.web.csrf.CsrfToken csrfToken =
                (org.springframework.security.web.csrf.CsrfToken) request.getAttribute(org.springframework.security.web.csrf.CsrfToken.class.getName());
        if (csrfToken != null) {
            csrfToken.getToken();
        }
    }
}
