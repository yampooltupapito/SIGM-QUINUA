package pe.gob.municipalidad.sigm.modules.portal.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.gob.municipalidad.sigm.common.response.ApiResponse;
import pe.gob.municipalidad.sigm.modules.portal.dto.CiudadanoRegistroDto;
import pe.gob.municipalidad.sigm.modules.portal.dto.PortalContactoDtos;
import pe.gob.municipalidad.sigm.modules.portal.dto.PortalFaqDtos;
import pe.gob.municipalidad.sigm.modules.portal.dto.PortalNoticiaDtos;
import pe.gob.municipalidad.sigm.modules.portal.dto.PortalServicioDtos;
import pe.gob.municipalidad.sigm.modules.portal.service.CiudadanoRegistroService;
import pe.gob.municipalidad.sigm.modules.portal.service.PortalContactoService;
import pe.gob.municipalidad.sigm.modules.portal.service.PortalContenidoService;
import pe.gob.municipalidad.sigm.modules.portal.service.PortalServicioService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/portal")
@RequiredArgsConstructor
public class PortalPublicRestController {

    private final PortalContenidoService contenidoService;
    private final PortalServicioService servicioService;
    private final PortalContactoService contactoService;
    private final CiudadanoRegistroService registroService;

    @GetMapping("/servicios")
    public ResponseEntity<ApiResponse<List<PortalServicioDtos.ServicioResponse>>> listarServicios() {
        return ApiResponse.ok("Servicios municipales disponibles", servicioService.listarServiciosPublicos());
    }

    @GetMapping("/noticias")
    public ResponseEntity<ApiResponse<Page<PortalNoticiaDtos.NoticiaResponse>>> listarNoticias(
            @RequestParam(value = "tipo", required = false) String tipo,
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "9") int size) {
        Page<PortalNoticiaDtos.NoticiaResponse> noticias = contenidoService.listarNoticiasPublicas(tipo, q, page, size);
        return ApiResponse.ok("Noticias públicas", noticias);
    }

    @GetMapping("/faq")
    public ResponseEntity<ApiResponse<List<PortalFaqDtos.FaqResponse>>> buscarFaq(
            @RequestParam(value = "q", required = false) String q) {
        return ApiResponse.ok("Preguntas frecuentes", contenidoService.buscarFaqPublicas(q));
    }

    @PostMapping("/contacto")
    public ResponseEntity<ApiResponse<PortalContactoDtos.ContactoResponse>> enviarContacto(
            @Valid @RequestBody PortalContactoDtos.ContactoPublicRequest request,
            HttpServletRequest httpRequest) {
        String clientIp = obtenerIpCliente(httpRequest);
        PortalContactoDtos.ContactoResponse response = contactoService.registrarContactoCiudadano(request, clientIp);
        return ApiResponse.created("Consulta ciudadana registrada", response);
    }

    @PostMapping("/registro")
    public ResponseEntity<ApiResponse<CiudadanoRegistroDto.CiudadanoRegistroResponse>> registrarCiudadano(
            @Valid @RequestBody CiudadanoRegistroDto.CiudadanoRegistroRequest request,
            HttpServletRequest httpRequest) {
        String clientIp = obtenerIpCliente(httpRequest);
        CiudadanoRegistroDto.CiudadanoRegistroResponse response = registroService.registrarCiudadano(request, clientIp);
        return ApiResponse.created("Registro ciudadano exitoso", response);
    }

    private String obtenerIpCliente(HttpServletRequest request) {
        String xf = request.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isEmpty()) {
            return xf.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
