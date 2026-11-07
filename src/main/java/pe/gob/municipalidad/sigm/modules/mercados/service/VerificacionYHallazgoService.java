package pe.gob.municipalidad.sigm.modules.mercados.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.administracion.service.AuditoriaAdministrativaService;
import pe.gob.municipalidad.sigm.modules.mercados.dto.HallazgoCreateRequest;
import pe.gob.municipalidad.sigm.modules.mercados.dto.HallazgoResponse;
import pe.gob.municipalidad.sigm.modules.mercados.dto.RespuestaVerificacionDto;
import pe.gob.municipalidad.sigm.modules.mercados.dto.VerificacionItemRequest;
import pe.gob.municipalidad.sigm.modules.mercados.entity.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoInspeccion;
import pe.gob.municipalidad.sigm.modules.mercados.repository.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class VerificacionYHallazgoService {

    private final InspeccionMercadoRepository inspeccionRepository;
    private final ItemVerificacionPlantillaRepository itemRepository;
    private final RespuestaVerificacionInspeccionRepository respuestaRepository;
    private final HallazgoInspeccionRepository hallazgoRepository;
    private final EvidenciaInspeccionMercadoRepository evidenciaRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuditoriaAdministrativaService auditoriaService;

    private static final Set<String> EXTENSIONES_PERMITIDAS = Set.of("jpg", "jpeg", "png", "webp", "pdf");
    private static final long MAX_FILE_SIZE = 15 * 1024 * 1024; // 15 MB

    public VerificacionYHallazgoService(InspeccionMercadoRepository inspeccionRepository,
                                       ItemVerificacionPlantillaRepository itemRepository,
                                       RespuestaVerificacionInspeccionRepository respuestaRepository,
                                       HallazgoInspeccionRepository hallazgoRepository,
                                       EvidenciaInspeccionMercadoRepository evidenciaRepository,
                                       UsuarioRepository usuarioRepository,
                                       AuditoriaAdministrativaService auditoriaService) {
        this.inspeccionRepository = inspeccionRepository;
        this.itemRepository = itemRepository;
        this.respuestaRepository = respuestaRepository;
        this.hallazgoRepository = hallazgoRepository;
        this.evidenciaRepository = evidenciaRepository;
        this.usuarioRepository = usuarioRepository;
        this.auditoriaService = auditoriaService;
    }

    @Transactional
    public List<RespuestaVerificacionDto> registrarVerificaciones(UUID inspeccionId, List<VerificacionItemRequest> items, String username, Long usuarioId) {
        InspeccionMercado inspeccion = inspeccionRepository.findById(inspeccionId)
                .orElseThrow(() -> new ResourceNotFoundException("Inspección no encontrada: " + inspeccionId));

        if (inspeccion.getEstado() == EstadoInspeccion.CANCELADA || inspeccion.getEstado() == EstadoInspeccion.FINALIZADA) {
            throw new BusinessException("No se pueden registrar verificaciones en una inspección " + inspeccion.getEstado());
        }

        Usuario inspector = resolverUsuario(usuarioId, username);

        List<RespuestaVerificacionDto> resultado = new ArrayList<>();

        for (VerificacionItemRequest req : items) {
            ItemVerificacionPlantilla item = itemRepository.findById(req.getItemVerificacionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Ítem de verificación no encontrado: " + req.getItemVerificacionId()));

            Optional<RespuestaVerificacionInspeccion> existente = respuestaRepository.findByInspeccionIdAndItemVerificacionId(inspeccionId, item.getId());
            RespuestaVerificacionInspeccion entity;
            if (existente.isPresent()) {
                entity = existente.get();
                entity.setResultado(req.getResultado());
                entity.setObservaciones(req.getObservaciones());
                entity.setInspectorRegistro(inspector);
            } else {
                entity = RespuestaVerificacionInspeccion.builder()
                        .inspeccion(inspeccion)
                        .itemVerificacion(item)
                        .resultado(req.getResultado())
                        .observaciones(req.getObservaciones())
                        .inspectorRegistro(inspector)
                        .build();
            }
            RespuestaVerificacionInspeccion guardada = respuestaRepository.save(entity);

            resultado.add(RespuestaVerificacionDto.builder()
                    .id(guardada.getId())
                    .itemVerificacionId(item.getId())
                    .itemCodigo(item.getCodigo())
                    .itemCategoria(item.getCategoria())
                    .itemDescripcion(item.getDescripcion())
                    .itemObligatorio(item.getObligatorio())
                    .itemReferenciaNormativa(item.getReferenciaNormativa())
                    .resultado(guardada.getResultado())
                    .resultadoDescripcion(guardada.getResultado() != null ? guardada.getResultado().getDescripcion() : null)
                    .observaciones(guardada.getObservaciones())
                    .fechaRegistro(guardada.getFechaRegistro())
                    .inspectorRegistroNombre(inspector.getUsername())
                    .build());
        }

        auditoriaService.registrar("REGISTRAR_VERIFICACIONES_INSPECCION", "mercados_inspecciones_verificaciones", inspeccion.getId(),
                "EXITO", "Se registraron " + items.size() + " ítems de verificación para la inspección: " + inspeccion.getCodigoInspeccion());

        return resultado;
    }

    @Transactional
    public HallazgoResponse registrarHallazgo(UUID inspeccionId, HallazgoCreateRequest request, String username, Long usuarioId) {
        InspeccionMercado inspeccion = inspeccionRepository.findById(inspeccionId)
                .orElseThrow(() -> new ResourceNotFoundException("Inspección no encontrada: " + inspeccionId));

        if (inspeccion.getEstado() == EstadoInspeccion.CANCELADA || inspeccion.getEstado() == EstadoInspeccion.FINALIZADA) {
            throw new BusinessException("No se pueden registrar hallazgos en una inspección " + inspeccion.getEstado());
        }

        Usuario inspector = resolverUsuario(usuarioId, username);

        int correlativo = inspeccion.getHallazgos() != null ? inspeccion.getHallazgos().size() + 1 : 1;
        String codigo = String.format("HAL-%03d", correlativo);

        HallazgoInspeccionMercado entity = HallazgoInspeccionMercado.builder()
                .inspeccion(inspeccion)
                .codigo(codigo)
                .categoria(request.getCategoria().trim().toUpperCase())
                .descripcion(request.getDescripcion().trim())
                .gravedadTecnica(request.getGravedadTecnica())
                .estado("ABIERTO")
                .responsableRegistro(inspector)
                .medidasCorrectivas(new ArrayList<>())
                .evidencias(new ArrayList<>())
                .build();

        HallazgoInspeccionMercado guardado = hallazgoRepository.save(entity);

        auditoriaService.registrar("REGISTRAR_HALLAZGO_INSPECCION", "mercados_hallazgos_inspeccion", guardado.getId(),
                "EXITO", "Hallazgo técnico registrado: " + guardado.getCodigo() + " en " + inspeccion.getCodigoInspeccion());

        return HallazgoResponse.builder()
                .id(guardado.getId())
                .inspeccionId(inspeccion.getId())
                .codigo(guardado.getCodigo())
                .categoria(guardado.getCategoria())
                .descripcion(guardado.getDescripcion())
                .gravedadTecnica(guardado.getGravedadTecnica())
                .gravedadTecnicaDescripcion(guardado.getGravedadTecnica() != null ? guardado.getGravedadTecnica().getDescripcion() : null)
                .estado(guardado.getEstado())
                .fechaRegistro(guardado.getFechaRegistro())
                .responsableRegistroNombre(inspector.getUsername())
                .totalMedidas(0)
                .totalEvidencias(0)
                .build();
    }

    @Transactional
    public EvidenciaInspeccionMercado adjuntarEvidencia(UUID inspeccionId, UUID hallazgoId, MultipartFile archivo, String tipoEvidencia, String descripcion, String username, Long usuarioId) {
        if (archivo == null || archivo.isEmpty()) {
            throw new BusinessException("El archivo de evidencia es obligatorio.");
        }

        if (archivo.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("El archivo excede el tamaño máximo permitido de 15 MB.");
        }

        String originalFilename = archivo.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new BusinessException("Nombre de archivo inválido.");
        }

        String ext = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        if (!EXTENSIONES_PERMITIDAS.contains(ext)) {
            throw new BusinessException("Extensión de archivo no permitida (." + ext + "). Formatos permitidos: JPG, PNG, WEBP, PDF.");
        }

        InspeccionMercado inspeccion = inspeccionRepository.findById(inspeccionId)
                .orElseThrow(() -> new ResourceNotFoundException("Inspección no encontrada: " + inspeccionId));

        HallazgoInspeccionMercado hallazgo = null;
        if (hallazgoId != null) {
            hallazgo = hallazgoRepository.findById(hallazgoId)
                    .orElseThrow(() -> new ResourceNotFoundException("Hallazgo no encontrado: " + hallazgoId));
        }

        Usuario usuario = resolverUsuario(usuarioId, username);

        try {
            byte[] bytes = archivo.getBytes();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(bytes);
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            String hashSha256 = sb.toString();

            // Almacenamiento seguro local
            String baseDir = System.getProperty("user.home") + "/.sigm_quinua/evidencias_inspeccion";
            Path uploadPath = Paths.get(baseDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String savedFileName = UUID.randomUUID() + "_" + originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");
            Path targetFile = uploadPath.resolve(savedFileName);
            Files.copy(archivo.getInputStream(), targetFile, StandardCopyOption.REPLACE_EXISTING);

            EvidenciaInspeccionMercado entity = EvidenciaInspeccionMercado.builder()
                    .inspeccion(inspeccion)
                    .hallazgo(hallazgo)
                    .tipoEvidencia(tipoEvidencia != null ? tipoEvidencia : "FOTOGRAFIA")
                    .nombreArchivo(originalFilename)
                    .contentType(archivo.getContentType() != null ? archivo.getContentType() : "application/octet-stream")
                    .tamanioBytes(archivo.getSize())
                    .rutaAlmacenamiento(targetFile.toAbsolutePath().toString())
                    .hashSha256(hashSha256)
                    .descripcion(descripcion)
                    .usuarioSubida(usuario)
                    .build();

            EvidenciaInspeccionMercado guardada = evidenciaRepository.save(entity);

            auditoriaService.registrar("ADJUNTAR_EVIDENCIA_INSPECCION", "mercados_evidencias_inspeccion", guardada.getId(),
                    "EXITO", "Evidencia adjunta: " + originalFilename + " (Hash: " + hashSha256.substring(0, 8) + "...)");

            return guardada;
        } catch (Exception e) {
            log.error("Error al almacenar archivo de evidencia: {}", e.getMessage(), e);
            throw new BusinessException("Error al procesar y almacenar el archivo de evidencia: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<HallazgoResponse> listarHallazgos(UUID inspeccionId) {
        return hallazgoRepository.findByInspeccionIdOrderByFechaRegistroAsc(inspeccionId).stream()
                .map(h -> HallazgoResponse.builder()
                        .id(h.getId())
                        .inspeccionId(h.getInspeccion().getId())
                        .codigo(h.getCodigo())
                        .categoria(h.getCategoria())
                        .descripcion(h.getDescripcion())
                        .gravedadTecnica(h.getGravedadTecnica())
                        .gravedadTecnicaDescripcion(h.getGravedadTecnica() != null ? h.getGravedadTecnica().getDescripcion() : null)
                        .estado(h.getEstado())
                        .fechaRegistro(h.getFechaRegistro())
                        .responsableRegistroNombre(h.getResponsableRegistro() != null ? h.getResponsableRegistro().getUsername() : null)
                        .totalMedidas(h.getMedidasCorrectivas() != null ? h.getMedidasCorrectivas().size() : 0)
                        .totalEvidencias(h.getEvidencias() != null ? h.getEvidencias().size() : 0)
                        .build())
                .collect(Collectors.toList());
    }

    private Usuario resolverUsuario(Long usuarioId, String username) {
        if (usuarioId != null) {
            Optional<Usuario> u = usuarioRepository.findById(usuarioId);
            if (u.isPresent()) {
                return u.get();
            }
        }
        if (username != null && !username.isBlank() && !"sistema".equalsIgnoreCase(username.trim())) {
            Optional<Usuario> u = usuarioRepository.findByUsername(username.trim());
            if (u.isPresent()) {
                return u.get();
            }
        }
        if (username != null && !username.isBlank()) {
            Optional<Usuario> u = usuarioRepository.findByUsername(username.trim());
            if (u.isPresent()) {
                return u.get();
            }
        }
        return usuarioRepository.findAll().stream()
                .filter(u -> Boolean.TRUE.equals(u.getEnabled()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró ningún usuario habilitado en el sistema."));
    }
}
