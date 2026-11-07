package pe.gob.municipalidad.sigm.modules.mercados.controller.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.modules.mercados.dto.*;
import pe.gob.municipalidad.sigm.modules.mercados.service.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.*;

/**
 * Controlador REST para operaciones del plano interactivo y editor visual de mercados.
 */
@Slf4j
@RestController
@RequestMapping("/api/mercados")
@RequiredArgsConstructor
public class PlanoMercadoApiController {

    private final PlanoMercadoService planoService;
    private final VersionPlanoMercadoService versionService;
    private final EditorPlanoMercadoService editorService;
    private final PublicacionPlanoMercadoService publicacionService;
    private final ConsultaPlanoMercadoService consultaService;
    private final EstadoVisualPuestoService estadoVisualService;

    private static final Set<String> EXTENSIONES_IMAGEN = Set.of("png", "jpg", "jpeg", "webp");
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB

    /**
     * Obtiene el plano activo de un mercado con todos sus elementos y estados dinámicos.
     */
    @GetMapping("/{mercadoId}/plano")
    @PreAuthorize("hasAuthority('MERCADOS_PLANO_VER') or hasAnyRole('ADMIN', 'SUPERADMIN', 'OPERADOR')")
    public ResponseEntity<PlanoMercadoDetailResponse> obtenerPlano(
            @PathVariable UUID mercadoId,
            @RequestParam(required = false) UUID versionId) {
        PlanoMercadoDetailResponse detalle = consultaService.obtenerPlanoDetalleParaVisor(mercadoId, versionId);
        return ResponseEntity.ok(detalle);
    }

    /**
     * Crea un nuevo plano maestro para un mercado.
     */
    @PostMapping("/{mercadoId}/plano")
    @PreAuthorize("hasAuthority('MERCADOS_PLANO_CREAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<PlanoMercadoResponse> crearPlano(
            @PathVariable UUID mercadoId,
            @Valid @RequestBody PlanoMercadoCreateRequest request,
            Authentication auth) {
        request.setMercadoId(mercadoId);
        String username = auth != null ? auth.getName() : "SISTEMA";
        PlanoMercadoResponse response = planoService.crearPlano(request, username, null);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Lista todas las versiones de un plano de mercado.
     */
    @GetMapping("/{mercadoId}/plano/versiones")
    @PreAuthorize("hasAuthority('MERCADOS_PLANO_VERSIONES_VER') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<List<VersionPlanoResponse>> listarVersiones(@PathVariable UUID mercadoId) {
        PlanoMercadoDetailResponse plano = consultaService.obtenerPlanoDetalleParaVisor(mercadoId, null);
        List<VersionPlanoResponse> versiones = versionService.listarVersiones(plano.getId());
        return ResponseEntity.ok(versiones);
    }

    /**
     * Obtiene una versión específica del plano con sus elementos.
     */
    @GetMapping("/{mercadoId}/plano/versiones/{versionId}")
    @PreAuthorize("hasAuthority('MERCADOS_PLANO_VERSIONES_VER') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<PlanoMercadoDetailResponse> obtenerVersionDetalle(
            @PathVariable UUID mercadoId,
            @PathVariable UUID versionId) {
        PlanoMercadoDetailResponse detalle = consultaService.obtenerPlanoDetalleParaVisor(mercadoId, versionId);
        return ResponseEntity.ok(detalle);
    }

    /**
     * Crea un nuevo borrador de versión para edición o restaura desde una versión anterior.
     */
    @PostMapping("/{mercadoId}/plano/borradores")
    @PreAuthorize("hasAuthority('MERCADOS_PLANO_EDITAR') or hasAuthority('MERCADOS_PLANO_RESTAURAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<VersionPlanoResponse> crearBorrador(
            @PathVariable UUID mercadoId,
            @RequestParam(required = false) UUID restaurarDesdeVersionId,
            @RequestParam(required = false) String descripcion,
            Authentication auth) {
        PlanoMercadoDetailResponse plano = consultaService.obtenerPlanoDetalleParaVisor(mercadoId, null);
        String username = auth != null ? auth.getName() : "SISTEMA";

        VersionPlanoResponse response;
        if (restaurarDesdeVersionId != null) {
            response = versionService.restaurarComoBorrador(plano.getId(), restaurarDesdeVersionId, username, null);
        } else {
            response = versionService.crearNuevoBorrador(plano.getId(), descripcion, username, null);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Guarda la distribución gráfica y elementos de una versión borrador.
     */
    @PutMapping("/{mercadoId}/plano/borradores/{versionId}")
    @PreAuthorize("hasAuthority('MERCADOS_PLANO_GUARDAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<VersionPlanoResponse> guardarBorrador(
            @PathVariable UUID mercadoId,
            @PathVariable UUID versionId,
            @Valid @RequestBody GuardarPlanoRequest request,
            Authentication auth) {
        PlanoMercadoDetailResponse plano = consultaService.obtenerPlanoDetalleParaVisor(mercadoId, null);
        request.setVersionId(versionId);
        String username = auth != null ? auth.getName() : "SISTEMA";
        VersionPlanoResponse response = editorService.guardarBorrador(plano.getId(), request, username, null);
        return ResponseEntity.ok(response);
    }

    /**
     * Publica oficialmente una versión del plano y archiva las versiones anteriores.
     */
    @PostMapping("/{mercadoId}/plano/versiones/{versionId}/publicar")
    @PreAuthorize("hasAuthority('MERCADOS_PLANO_PUBLICAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<VersionPlanoResponse> publicarVersion(
            @PathVariable UUID mercadoId,
            @PathVariable UUID versionId,
            @RequestBody(required = false) PublicarPlanoRequest request,
            Authentication auth) {
        PlanoMercadoDetailResponse plano = consultaService.obtenerPlanoDetalleParaVisor(mercadoId, null);
        String username = auth != null ? auth.getName() : "SISTEMA";
        VersionPlanoResponse response = publicacionService.publicarVersion(plano.getId(), versionId, request, username, null);
        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene estadísticas de puestos del plano del mercado.
     */
    @GetMapping("/{mercadoId}/plano/estadisticas")
    @PreAuthorize("hasAuthority('MERCADOS_PLANO_VER') or hasAnyRole('ADMIN', 'SUPERADMIN', 'OPERADOR')")
    public ResponseEntity<EstadisticasPlanoResponse> obtenerEstadisticas(@PathVariable UUID mercadoId) {
        PlanoMercadoDetailResponse detalle = consultaService.obtenerPlanoDetalleParaVisor(mercadoId, null);
        return ResponseEntity.ok(detalle.getEstadisticas());
    }

    /**
     * Obtiene el detalle modal interactivo de un puesto comercial específico.
     */
    @GetMapping("/puestos/{puestoId}/informacion-plano")
    @PreAuthorize("hasAuthority('MERCADOS_PLANO_VER') or hasAnyRole('ADMIN', 'SUPERADMIN', 'OPERADOR')")
    public ResponseEntity<PuestoPlanoResponse> obtenerDetallePuesto(
            @PathVariable UUID puestoId,
            Authentication auth) {
        PuestoPlanoResponse response = consultaService.obtenerDetallePuestoParaModal(puestoId, auth);
        return ResponseEntity.ok(response);
    }

    /**
     * Carga una imagen de referencia para calcar el plano en el editor.
     */
    @PostMapping("/{mercadoId}/plano/referencia")
    @PreAuthorize("hasAuthority('MERCADOS_PLANO_REFERENCIA_GESTIONAR') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Map<String, Object>> subirImagenReferencia(
            @PathVariable UUID mercadoId,
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam("versionId") UUID versionId,
            Authentication auth) {
        if (archivo.isEmpty()) {
            throw new BusinessException("El archivo de imagen es obligatorio.");
        }
        if (archivo.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("La imagen excede el tamaño máximo permitido de 10 MB.");
        }

        String orig = archivo.getOriginalFilename();
        if (orig == null || !orig.contains(".")) {
            throw new BusinessException("Nombre de archivo inválido.");
        }

        String ext = orig.substring(orig.lastIndexOf(".") + 1).toLowerCase();
        if (!EXTENSIONES_IMAGEN.contains(ext)) {
            throw new BusinessException("Extensión no permitida (." + ext + "). Formatos permitidos: PNG, JPG, JPEG, WEBP.");
        }

        try {
            String dirBase = System.getProperty("user.home") + "/.sigm_quinua/planos_referencias";
            Path dirPath = Paths.get(dirBase);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }

            String nombreGuardado = UUID.randomUUID() + "." + ext;
            Path destino = dirPath.resolve(nombreGuardado);
            Files.copy(archivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);

            String urlRelativa = "/api/mercados/planos/referencia/" + nombreGuardado;

            String username = auth != null ? auth.getName() : "SISTEMA";
            versionService.actualizarImagenReferencia(
                    versionId, urlRelativa,
                    new BigDecimal("1.000"), BigDecimal.ZERO, BigDecimal.ZERO,
                    new BigDecimal("0.50"), true, false,
                    username, null
            );

            Map<String, Object> resp = new HashMap<>();
            resp.put("url", urlRelativa);
            resp.put("nombreArchivo", nombreGuardado);
            resp.put("tamanoBytes", archivo.getSize());
            resp.put("mensaje", "Imagen de referencia subida y configurada correctamente.");
            return ResponseEntity.ok(resp);
        } catch (IOException e) {
            log.error("Error al guardar imagen de referencia", e);
            throw new BusinessException("Error al almacenar la imagen de referencia: " + e.getMessage());
        }
    }

    /**
     * Sirve la imagen de referencia de forma controlada y segura.
     */
    @GetMapping("/planos/referencia/{nombreArchivo:.+}")
    @PreAuthorize("hasAuthority('MERCADOS_PLANO_VER') or hasAnyRole('ADMIN', 'SUPERADMIN', 'OPERADOR')")
    public ResponseEntity<Resource> servirImagenReferencia(@PathVariable String nombreArchivo) {
        // Prevenir path traversal
        if (nombreArchivo.contains("..") || nombreArchivo.contains("/") || nombreArchivo.contains("\\")) {
            return ResponseEntity.badRequest().build();
        }

        try {
            Path filePath = Paths.get(System.getProperty("user.home") + "/.sigm_quinua/planos_referencias")
                    .resolve(nombreArchivo).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
