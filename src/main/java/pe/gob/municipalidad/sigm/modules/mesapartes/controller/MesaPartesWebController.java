package pe.gob.municipalidad.sigm.modules.mesapartes.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pe.gob.municipalidad.sigm.common.model.Persona;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvDtos.ExpedienteDetalleResponse;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvDtos.TipoTramiteResponse;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvSolicitudDtos.ConstanciaPresentacionResponse;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvSolicitudDtos.MisSolicitudesItemResponse;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.Expediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoExpediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.ExpedienteRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.ConstanciaPresentacionService;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.ConsultaPublicaExpedienteService;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.SeguimientoCiudadanoService;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.SolicitudCiudadanaService;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.TipoTramiteService;
import pe.gob.municipalidad.sigm.modules.portal.service.PortalContenidoService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Controlador Web (Thymeleaf) para la interacción ciudadana de Mesa de Partes Virtual.
 * Provee la interfaz paso a paso para registro, confirmación, seguimiento y consulta de solicitudes.
 */
@Slf4j
@Controller
@RequestMapping("/mesa-partes")
public class MesaPartesWebController {

    private final TipoTramiteService tipoTramiteService;
    private final SolicitudCiudadanaService solicitudCiudadanaService;
    private final ConstanciaPresentacionService constanciaService;
    private final UsuarioRepository usuarioRepository;
    private final ExpedienteRepository expedienteRepository;
    private final PortalContenidoService portalContenidoService;
    private final ConsultaPublicaExpedienteService consultaPublicaExpedienteService;
    private final SeguimientoCiudadanoService seguimientoCiudadanoService;

    @Value("${sigm.app.base-url:http://localhost:8080}")
    private String baseUrl;

    public MesaPartesWebController(TipoTramiteService tipoTramiteService,
                                   SolicitudCiudadanaService solicitudCiudadanaService,
                                   ConstanciaPresentacionService constanciaService,
                                   UsuarioRepository usuarioRepository,
                                   ExpedienteRepository expedienteRepository,
                                   PortalContenidoService portalContenidoService,
                                   ConsultaPublicaExpedienteService consultaPublicaExpedienteService,
                                   SeguimientoCiudadanoService seguimientoCiudadanoService) {
        this.tipoTramiteService = tipoTramiteService;
        this.solicitudCiudadanaService = solicitudCiudadanaService;
        this.constanciaService = constanciaService;
        this.usuarioRepository = usuarioRepository;
        this.expedienteRepository = expedienteRepository;
        this.portalContenidoService = portalContenidoService;
        this.consultaPublicaExpedienteService = consultaPublicaExpedienteService;
        this.seguimientoCiudadanoService = seguimientoCiudadanoService;
    }

    @ModelAttribute("config")
    public Map<String, String> populateConfig() {
        return portalContenidoService.obtenerMapaConfiguracion();
    }

    /**
     * Paso a paso de Registro de Nueva Solicitud Ciudadana.
     */
    @GetMapping("/nueva-solicitud")
    @PreAuthorize("isAuthenticated()")
    public String nuevaSolicitud(
            @RequestParam(value = "borrador", required = false) UUID borradorUuid,
            @RequestParam(value = "tramiteId", required = false) Long tramiteId,
            Model model,
            Authentication auth
    ) {
        Usuario usuario = obtenerUsuario(auth.getName());
        Persona persona = usuario.getPersona();
        List<TipoTramiteResponse> tramites = tipoTramiteService.listarActivos();

        model.addAttribute("usuario", usuario);
        model.addAttribute("persona", persona);
        model.addAttribute("tramites", tramites);
        model.addAttribute("borradorUuid", borradorUuid);
        model.addAttribute("tramiteIdInicial", tramiteId);
        model.addAttribute("paginaActiva", "mesa-partes");

        return "mesa-partes/nueva-solicitud";
    }

    /**
     * Bandeja de "Mis Solicitudes" del ciudadano autenticado.
     */
    @GetMapping("/mis-solicitudes")
    @PreAuthorize("isAuthenticated()")
    public String misSolicitudes(
            @RequestParam(value = "estado", required = false) EstadoExpediente estado,
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model,
            Authentication auth
    ) {
        Pageable pageable = PageRequest.of(Math.max(0, page), size, Sort.by("createdAt").descending());
        Page<MisSolicitudesItemResponse> solicitudes = solicitudCiudadanaService.listarMisSolicitudes(estado, q, pageable, auth.getName());

        model.addAttribute("solicitudes", solicitudes);
        model.addAttribute("estadoSeleccionado", estado);
        model.addAttribute("terminoBusqueda", q);
        model.addAttribute("estados", EstadoExpediente.values());
        model.addAttribute("paginaActiva", "mesa-partes");

        return "mesa-partes/mis-solicitudes";
    }

    /**
     * Pantalla de Confirmación de Solicitud Presentada Formalmente.
     */
    @GetMapping("/confirmacion/{uuid}")
    @PreAuthorize("isAuthenticated()")
    public String confirmacionPresentacion(
            @PathVariable UUID uuid,
            Model model,
            Authentication auth
    ) {
        Expediente expediente = expedienteRepository.findByUuidWithDetalles(uuid)
                .orElseThrow(() -> new IllegalArgumentException("Expediente no encontrado con identificador: " + uuid));

        ConstanciaPresentacionResponse constancia = constanciaService.obtenerDatosConstancia(expediente, baseUrl);

        model.addAttribute("constancia", constancia);
        model.addAttribute("expediente", expediente);
        model.addAttribute("paginaActiva", "mesa-partes");

        return "mesa-partes/confirmacion";
    }

    /**
     * Detalle ciudadano del Expediente y Seguimiento de Actuaciones.
     */
    @GetMapping("/solicitud/{uuid}")
    @PreAuthorize("isAuthenticated()")
    public String detalleSolicitud(
            @PathVariable UUID uuid,
            Model model,
            Authentication auth
    ) {
        ExpedienteDetalleResponse detalle = solicitudCiudadanaService.obtenerMiExpediente(uuid, auth.getName());

        model.addAttribute("expediente", detalle);
        model.addAttribute("paginaActiva", "mesa-partes");

        return "mesa-partes/detalle-solicitud";
    }

    /**
     * Descarga directa de la Constancia Oficial de Presentación en PDF.
     */
    @GetMapping("/expedientes/{uuid}/constancia")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> descargarConstanciaPdf(
            @PathVariable UUID uuid,
            Authentication auth
    ) {
        byte[] pdfBytes = solicitudCiudadanaService.descargarConstanciaPdf(uuid, baseUrl, auth.getName());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "Constancia-MesaPartes-" + uuid + ".pdf");
        headers.setContentLength(pdfBytes.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    /**
     * Portal público de seguimiento mediante código seguro.
     */
    @GetMapping("/seguimiento")
    public String paginaSeguimientoPublico(
            @RequestParam(value = "codigo", required = false) String codigo,
            jakarta.servlet.http.HttpServletRequest request,
            Model model
    ) {
        model.addAttribute("paginaActiva", "seguimiento");
        model.addAttribute("codigoIngresado", codigo != null ? codigo.trim() : "");

        if (codigo != null && !codigo.trim().isBlank()) {
            try {
                String ip = obtenerIpCliente(request);
                String ua = request.getHeader("User-Agent");
                pe.gob.municipalidad.sigm.modules.mesapartes.dto.SeguimientoDtos.ConsultaPublicaResponse resultado =
                        consultaPublicaExpedienteService.consultarPublicamente(
                                new pe.gob.municipalidad.sigm.modules.mesapartes.dto.SeguimientoDtos.ConsultaPublicaRequest(codigo.trim()),
                                ip, ua
                        );
                model.addAttribute("resultado", resultado);
                model.addAttribute("consultado", true);
            } catch (Exception ex) {
                model.addAttribute("errorAlerta", ex.getMessage());
                model.addAttribute("consultado", false);
            }
        } else {
            model.addAttribute("consultado", false);
        }

        return "mesa-partes/seguimiento";
    }

    /**
     * Redirección institucional para enlaces o códigos QR históricos de consulta de trámite.
     */
    @GetMapping("/consulta-tramite")
    public String consultaTramiteRedirect(
            @RequestParam(value = "numero", required = false) String numero,
            @RequestParam(value = "codigo", required = false) String codigo) {
        String cod = (codigo != null && !codigo.isBlank()) ? codigo : (numero != null ? numero : "");
        return "redirect:/mesa-partes/seguimiento" + (!cod.isBlank() ? "?codigo=" + cod : "");
    }

    /**
     * Detalle integral de seguimiento para el administrado autenticado.
     */
    @GetMapping("/seguimiento/{id}")
    @PreAuthorize("isAuthenticated()")
    public String detalleSeguimientoCiudadano(
            @PathVariable Long id,
            Model model,
            Authentication auth
    ) {
        pe.gob.municipalidad.sigm.modules.mesapartes.dto.SeguimientoDtos.SeguimientoCiudadanoDetalleDto detalle =
                seguimientoCiudadanoService.obtenerSeguimientoDetalle(id, auth.getName());
        model.addAttribute("expediente", detalle);
        model.addAttribute("paginaActiva", "mis-solicitudes");
        return "mesa-partes/seguimiento-detalle";
    }

    private String obtenerIpCliente(jakarta.servlet.http.HttpServletRequest request) {
        String xf = request.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isBlank()) {
            return xf.split(",")[0].trim();
        }
        return request.getRemoteAddr() != null ? request.getRemoteAddr() : "127.0.0.1";
    }

    private Usuario obtenerUsuario(String username) {
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con username: " + username));
    }
}
