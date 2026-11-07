package pe.gob.municipalidad.sigm.modules.portal.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.gob.municipalidad.sigm.common.response.ApiResponse;
import pe.gob.municipalidad.sigm.modules.portal.dto.PortalContactoDtos;
import pe.gob.municipalidad.sigm.modules.portal.dto.PortalFaqDtos;
import pe.gob.municipalidad.sigm.modules.portal.dto.PortalNoticiaDtos;
import pe.gob.municipalidad.sigm.modules.portal.dto.PortalServicioDtos;
import pe.gob.municipalidad.sigm.modules.portal.service.PortalContactoService;
import pe.gob.municipalidad.sigm.modules.portal.service.PortalContenidoService;
import pe.gob.municipalidad.sigm.modules.portal.service.PortalServicioService;

@Slf4j
@RestController
@RequestMapping("/api/admin/portal")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'PORTAL_MANAGE', 'ADMINISTRACION_MANAGE')")
public class PortalAdminRestController {

    private final PortalContenidoService contenidoService;
    private final PortalServicioService servicioService;
    private final PortalContactoService contactoService;

    // --- NOTICIAS ---

    @GetMapping("/noticias/{id}")
    public ResponseEntity<ApiResponse<PortalNoticiaDtos.NoticiaDetailResponse>> obtenerNoticia(@PathVariable("id") Long id) {
        PortalNoticiaDtos.NoticiaDetailResponse noticia = contenidoService.obtenerNoticiaPorId(id);
        return ApiResponse.ok("Noticia obtenida", noticia);
    }

    @PostMapping("/noticias")
    public ResponseEntity<ApiResponse<PortalNoticiaDtos.NoticiaDetailResponse>> crearNoticia(
            @Valid @RequestBody PortalNoticiaDtos.NoticiaCreateRequest request) {
        PortalNoticiaDtos.NoticiaDetailResponse creada = contenidoService.crearNoticia(request);
        return ApiResponse.created("Publicación creada exitosamente", creada);
    }

    @PutMapping("/noticias/{id}")
    public ResponseEntity<ApiResponse<PortalNoticiaDtos.NoticiaDetailResponse>> actualizarNoticia(
            @PathVariable("id") Long id,
            @Valid @RequestBody PortalNoticiaDtos.NoticiaUpdateRequest request) {
        PortalNoticiaDtos.NoticiaDetailResponse actualizada = contenidoService.actualizarNoticia(id, request);
        return ApiResponse.ok("Publicación actualizada exitosamente", actualizada);
    }

    @PatchMapping("/noticias/{id}/estado")
    public ResponseEntity<ApiResponse<Void>> cambiarEstadoNoticia(
            @PathVariable("id") Long id,
            @RequestParam("estado") String estado) {
        contenidoService.cambiarEstadoNoticia(id, estado);
        return ApiResponse.ok("Estado de publicación actualizado a " + estado, null);
    }

    @DeleteMapping("/noticias/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminarNoticia(@PathVariable("id") Long id) {
        contenidoService.eliminarNoticia(id);
        return ApiResponse.ok("Publicación eliminada exitosamente", null);
    }

    // --- PREGUNTAS FRECUENTES ---

    @PostMapping("/faq")
    public ResponseEntity<ApiResponse<PortalFaqDtos.FaqResponse>> crearFaq(
            @Valid @RequestBody PortalFaqDtos.FaqCreateRequest request) {
        PortalFaqDtos.FaqResponse faq = contenidoService.crearFaq(request);
        return ApiResponse.created("Pregunta frecuente creada exitosamente", faq);
    }

    @PutMapping("/faq/{id}")
    public ResponseEntity<ApiResponse<PortalFaqDtos.FaqResponse>> actualizarFaq(
            @PathVariable("id") Long id,
            @Valid @RequestBody PortalFaqDtos.FaqUpdateRequest request) {
        PortalFaqDtos.FaqResponse faq = contenidoService.actualizarFaq(id, request);
        return ApiResponse.ok("Pregunta frecuente actualizada exitosamente", faq);
    }

    @DeleteMapping("/faq/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminarFaq(@PathVariable("id") Long id) {
        contenidoService.eliminarFaq(id);
        return ApiResponse.ok("Pregunta frecuente eliminada", null);
    }

    // --- CONTACTOS CIUDADANOS ---

    @GetMapping("/contactos/{id}")
    public ResponseEntity<ApiResponse<PortalContactoDtos.ContactoResponse>> obtenerContacto(@PathVariable("id") Long id) {
        PortalContactoDtos.ContactoResponse contacto = contactoService.obtenerDetalleContacto(id);
        return ApiResponse.ok("Detalle de consulta ciudadana", contacto);
    }

    @PostMapping("/contactos/{id}/atender")
    public ResponseEntity<ApiResponse<PortalContactoDtos.ContactoResponse>> atenderContacto(
            @PathVariable("id") Long id,
            @Valid @RequestBody PortalContactoDtos.ContactoAtencionRequest request,
            Authentication authentication) {
        String usuario = authentication != null ? authentication.getName() : "ADMIN_PORTAL";
        PortalContactoDtos.ContactoResponse contacto = contactoService.atenderContacto(id, request, usuario);
        return ApiResponse.ok("Consulta ciudadana atendida exitosamente", contacto);
    }

    // --- SERVICIOS MUNICIPALES ---

    @PutMapping("/servicios/{id}")
    public ResponseEntity<ApiResponse<PortalServicioDtos.ServicioResponse>> actualizarServicio(
            @PathVariable("id") Long id,
            @Valid @RequestBody PortalServicioDtos.ServicioUpdateRequest request) {
        PortalServicioDtos.ServicioResponse servicio = servicioService.actualizarServicio(id, request);
        return ApiResponse.ok("Servicio municipal actualizado", servicio);
    }
}
