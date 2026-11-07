package pe.gob.municipalidad.sigm.modules.serenazgo.service.impl;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pe.gob.municipalidad.sigm.common.document.entity.CategoriaDocumento;
import pe.gob.municipalidad.sigm.common.document.entity.ClasificacionAcceso;
import pe.gob.municipalidad.sigm.common.document.entity.Documento;
import pe.gob.municipalidad.sigm.common.document.entity.EstadoDocumento;
import pe.gob.municipalidad.sigm.common.document.repository.DocumentoRepository;
import pe.gob.municipalidad.sigm.common.document.storage.DocumentStorageService;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.documentos.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.repository.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.DocumentoSeguridadCiudadanaService;
import pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentoSeguridadCiudadanaServiceImpl implements DocumentoSeguridadCiudadanaService {

    private static final ZoneId ZONA_LIMA = ZoneId.of("America/Lima");
    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final long LIMITE_TAMANO_BYTES = 50 * 1024 * 1024; // 50 MB

    private static final Set<String> EXTENSIONES_PERMITIDAS = Set.of(
            "jpg", "jpeg", "png", "webp", "gif",
            "mp4", "webm", "mov", "avi", "mkv",
            "pdf", "doc", "docx", "xls", "xlsx", "txt", "csv"
    );

    private static final Set<String> MIMES_PERMITIDOS = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif",
            "video/mp4", "video/webm", "video/quicktime", "video/x-msvideo", "video/x-matroska",
            "application/pdf", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "text/plain", "text/csv", "application/octet-stream"
    );

    private final DocumentStorageService storageService;
    private final DocumentoRepository commonDocumentoRepository;
    private final DocumentoSeguridadCiudadanaRepository documentoRepository;
    private final DocumentoSeguridadRelacionRepository relacionRepository;
    private final DocumentoSeguridadVerificacionRepository verificacionRepository;
    private final DocumentoSeguridadHistorialRepository historialRepository;
    private final UsuarioRepository usuarioRepository;

    private final IncidenciaSeguridadCiudadanaRepository incidenciaRepository;
    private final AlertaCiudadanaSeguridadRepository alertaRepository;
    private final AtencionIncidenciaSerenazgoRepository atencionRepository;
    private final IntervencionSerenazgoRepository intervencionRepository;
    private final PatrullajeSerenazgoRepository patrullajeRepository;

    @Override
    @Transactional
    public DocumentoSeguridadResponse registrarDocumento(DocumentoCargaRequest request, CustomUserDetails user, String ip) {
        MultipartFile file = request.getArchivo();
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Debe seleccionar un archivo válido para registrar.");
        }

        if (file.getSize() > LIMITE_TAMANO_BYTES) {
            throw new BusinessException("El archivo excede el tamaño máximo permitido de 50 MB.");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            originalFilename = "archivo_sin_nombre";
        }
        originalFilename = sanitizarNombreArchivo(originalFilename);

        String ext = extraerExtension(originalFilename);
        if (!EXTENSIONES_PERMITIDAS.contains(ext.toLowerCase())) {
            throw new BusinessException("Extensión de archivo no permitida: " + ext);
        }

        String mime = file.getContentType();
        if (mime == null || (!MIMES_PERMITIDOS.contains(mime.toLowerCase()) && !mime.startsWith("image/") && !mime.startsWith("video/"))) {
            throw new BusinessException("Tipo MIME no autorizado para evidencias de seguridad: " + mime);
        }

        Usuario usuario = obtenerUsuario(user);

        byte[] contenido;
        try {
            contenido = file.getBytes();
        } catch (IOException e) {
            log.error("Error al leer bytes del archivo subido: {}", originalFilename, e);
            throw new BusinessException("Error de lectura del archivo subido.");
        }

        String hashCalculado = calcularSha256(contenido);

        // Guardar archivo físico en almacenamiento seguro privado
        String nombreAlmacenado;
        try (InputStream is = new ByteArrayInputStream(contenido)) {
            nombreAlmacenado = storageService.store(is, ext);
        } catch (IOException e) {
            log.error("Error al persistir archivo físico en almacenamiento: {}", originalFilename, e);
            throw new BusinessException("Error al almacenar el archivo físico en el repositorio institucional.");
        }

        // Crear registro en el acervo institucional central (Documento común)
        Documento commonDoc = Documento.builder()
                .identificadorPublico(UUID.randomUUID())
                .nombreOriginal(originalFilename)
                .nombreInterno(nombreAlmacenado)
                .extension(ext)
                .mimeType(mime)
                .tamanoBytes((long) contenido.length)
                .hashSha256(hashCalculado)
                .categoria(determinarCategoriaComun(request.getTipoDocumento()))
                .clasificacionAcceso(determinarClasificacionComun(request.getNivelConfidencialidad()))
                .descripcion(request.getDescripcion())
                .estado(EstadoDocumento.ACTIVO)
                .usuarioPropietario(usuario)
                .moduloOrigen("SEGURIDAD_CIUDADANA")
                .fechaCreacion(OffsetDateTime.now())
                .fechaActualizacion(OffsetDateTime.now())
                .build();
        commonDoc = commonDocumentoRepository.save(commonDoc);

        // Generar código único para el documento de seguridad
        String nuevoCodigo = generarSiguienteCodigo();

        // Construir DocumentoSeguridadCiudadana
        DocumentoSeguridadCiudadana doc = DocumentoSeguridadCiudadana.builder()
                .codigo(nuevoCodigo)
                .documento(commonDoc)
                .tipoDocumento(request.getTipoDocumento())
                .nombreOriginal(originalFilename)
                .nombreAlmacenado(nombreAlmacenado)
                .extension(ext)
                .mimeType(mime)
                .tamanoBytes((long) contenido.length)
                .hashSha256(hashCalculado)
                .fechaHoraCarga(OffsetDateTime.now())
                .usuarioCarga(usuario)
                .origenDocumento(request.getOrigenDocumento() != null ? request.getOrigenDocumento() : OrigenDocumentoSeguridad.ADMINISTRATIVO)
                .descripcion(request.getDescripcion())
                .estado(EstadoDocumentoSeguridad.REGISTRADO)
                .nivelConfidencialidad(request.getNivelConfidencialidad() != null ? request.getNivelConfidencialidad() : NivelConfidencialidad.CONFIDENCIAL)
                .observaciones(request.getObservaciones())
                .versionActual(1)
                .fechaCaptura(request.getFechaCaptura() != null ? request.getFechaCaptura().atStartOfDay(ZONA_LIMA).toOffsetDateTime() : null)
                .ubicacionCaptura(request.getUbicacionCaptura())
                .latitud(request.getLatitud())
                .longitud(request.getLongitud())
                .autoriaDeclarada(request.getAutoriaDeclarada())
                .ultimoResultadoVerificacion(ResultadoVerificacionIntegridad.COINCIDENTE)
                .fechaUltimaVerificacion(OffsetDateTime.now())
                .usuarioUltimaVerificacion(usuario)
                .createdBy(usuario.getUsername())
                .updatedBy(usuario.getUsername())
                .build();

        // Vincular con actuaciones si se especificaron
        asociarEntidadesDirectas(doc, request);

        doc = documentoRepository.save(doc);

        // Crear relaciones múltiples si aplica
        crearRelacionesMultiples(doc, request, usuario);

        // Registrar auditoría de integridad inicial
        DocumentoSeguridadVerificacion verifInicial = DocumentoSeguridadVerificacion.builder()
                .documentoSeguridad(doc)
                .hashRegistrado(hashCalculado)
                .hashCalculado(hashCalculado)
                .resultado(ResultadoVerificacionIntegridad.COINCIDENTE)
                .usuario(usuario)
                .detalleTecnico("Verificación de huella digital SHA-256 inicial durante la carga del archivo.")
                .build();
        verificacionRepository.save(verifInicial);

        // Registrar trazabilidad histórica
        DocumentoSeguridadHistorial hist = DocumentoSeguridadHistorial.builder()
                .documentoSeguridad(doc)
                .accion("CARGA")
                .descripcion("Archivo registrado con huella SHA-256: " + hashCalculado + " (" + formatearTamano(doc.getTamanoBytes()) + ")")
                .usuario(usuario)
                .ipOrigen(ip)
                .build();
        historialRepository.save(hist);

        log.info("Documento de seguridad ciudadana registrado: {} por usuario {}", nuevoCodigo, usuario.getUsername());
        return mapToResponse(doc);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DocumentoSeguridadResponse> listar(DocumentoFiltroRequest filtro, Pageable pageable) {
        Specification<DocumentoSeguridadCiudadana> spec = (root, query, cb) -> {
            List<Predicate> preds = new ArrayList<>();

            if (filtro != null) {
                if (filtro.getBusqueda() != null && !filtro.getBusqueda().isBlank()) {
                    String pattern = "%" + filtro.getBusqueda().trim().toLowerCase() + "%";
                    preds.add(cb.or(
                            cb.like(cb.lower(root.get("codigo")), pattern),
                            cb.like(cb.lower(root.get("nombreOriginal")), pattern),
                            cb.like(cb.lower(root.get("descripcion")), pattern)
                    ));
                }
                if (filtro.getTipoDocumento() != null) {
                    preds.add(cb.equal(root.get("tipoDocumento"), filtro.getTipoDocumento()));
                }
                if (filtro.getEstado() != null) {
                    preds.add(cb.equal(root.get("estado"), filtro.getEstado()));
                }
                if (filtro.getOrigenDocumento() != null) {
                    preds.add(cb.equal(root.get("origenDocumento"), filtro.getOrigenDocumento()));
                }
                if (filtro.getNivelConfidencialidad() != null) {
                    preds.add(cb.equal(root.get("nivelConfidencialidad"), filtro.getNivelConfidencialidad()));
                }
                if (filtro.getResultadoIntegridad() != null) {
                    preds.add(cb.equal(root.get("ultimoResultadoVerificacion"), filtro.getResultadoIntegridad()));
                }
                if (filtro.getDesde() != null) {
                    preds.add(cb.greaterThanOrEqualTo(root.get("fechaHoraCarga"), filtro.getDesde().atStartOfDay(ZONA_LIMA).toOffsetDateTime()));
                }
                if (filtro.getHasta() != null) {
                    preds.add(cb.lessThan(root.get("fechaHoraCarga"), filtro.getHasta().plusDays(1).atStartOfDay(ZONA_LIMA).toOffsetDateTime()));
                }
                if (filtro.getIncidenciaId() != null) {
                    preds.add(cb.equal(root.get("incidencia").get("id"), filtro.getIncidenciaId()));
                }
                if (filtro.getIntervencionId() != null) {
                    preds.add(cb.equal(root.get("intervencion").get("id"), filtro.getIntervencionId()));
                }
                if (filtro.getPatrullajeId() != null) {
                    preds.add(cb.equal(root.get("patrullaje").get("id"), filtro.getPatrullajeId()));
                }
            }

            return cb.and(preds.toArray(new Predicate[0]));
        };

        return documentoRepository.findAll(spec, pageable).map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentoSeguridadDetalleResponse obtenerDetalle(UUID id, CustomUserDetails user) {
        DocumentoSeguridadCiudadana doc = documentoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Documento no encontrado: " + id));

        validarAccesoConfidencialidad(doc, user);

        return mapToDetalleResponse(doc);
    }

    @Override
    @Transactional
    public Resource descargar(UUID id, CustomUserDetails user, String ip) {
        DocumentoSeguridadCiudadana doc = documentoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Documento no encontrado: " + id));

        validarAccesoConfidencialidad(doc, user);

        if (!storageService.exists(doc.getNombreAlmacenado())) {
            throw new BusinessException("El archivo físico no se encuentra disponible en el almacenamiento seguro.");
        }

        Usuario usuario = obtenerUsuario(user);

        // Registrar auditoría de descarga
        DocumentoSeguridadHistorial hist = DocumentoSeguridadHistorial.builder()
                .documentoSeguridad(doc)
                .accion("DESCARGA")
                .descripcion("Descarga de archivo por usuario " + usuario.getUsername())
                .usuario(usuario)
                .ipOrigen(ip)
                .build();
        historialRepository.save(hist);

        InputStream inputStream = storageService.read(doc.getNombreAlmacenado());
        return new InputStreamResource(inputStream);
    }

    @Override
    @Transactional
    public Resource obtenerVistaPrevia(UUID id, CustomUserDetails user) {
        DocumentoSeguridadCiudadana doc = documentoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Documento no encontrado: " + id));

        validarAccesoConfidencialidad(doc, user);

        if (!storageService.exists(doc.getNombreAlmacenado())) {
            throw new BusinessException("El archivo físico no se encuentra disponible en el almacenamiento seguro.");
        }

        InputStream inputStream = storageService.read(doc.getNombreAlmacenado());
        return new InputStreamResource(inputStream);
    }

    @Override
    @Transactional
    public VerificacionIntegridadResponse verificarIntegridad(UUID id, CustomUserDetails user) {
        DocumentoSeguridadCiudadana doc = documentoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Documento no encontrado: " + id));

        Usuario usuario = obtenerUsuario(user);

        ResultadoVerificacionIntegridad resultado;
        String hashCalculado = null;
        String detalleTecnico;

        if (!storageService.exists(doc.getNombreAlmacenado())) {
            resultado = ResultadoVerificacionIntegridad.ARCHIVO_NO_DISPONIBLE;
            detalleTecnico = "El archivo físico no existe en el almacenamiento seguro: " + doc.getNombreAlmacenado();
        } else {
            try (InputStream is = storageService.read(doc.getNombreAlmacenado())) {
                hashCalculado = calcularSha256(is);
                if (hashCalculado.equalsIgnoreCase(doc.getHashSha256())) {
                    resultado = ResultadoVerificacionIntegridad.COINCIDENTE;
                    detalleTecnico = "Comprobación exitosa. Huella SHA-256 coincide exactamente con el valor original registrado.";
                } else {
                    resultado = ResultadoVerificacionIntegridad.NO_COINCIDENTE;
                    detalleTecnico = "ADVERTENCIA: La huella digital calculada (" + hashCalculado + ") difiere de la huella registrada (" + doc.getHashSha256() + ").";
                }
            } catch (Exception e) {
                log.error("Error al calcular hash de verificación para documento: {}", doc.getCodigo(), e);
                resultado = ResultadoVerificacionIntegridad.ERROR_LECTURA;
                detalleTecnico = "Error de lectura de archivo físico: " + e.getMessage();
            }
        }

        doc.setUltimoResultadoVerificacion(resultado);
        doc.setFechaUltimaVerificacion(OffsetDateTime.now());
        doc.setUsuarioUltimaVerificacion(usuario);
        doc.setUpdatedBy(usuario.getUsername());
        documentoRepository.save(doc);

        DocumentoSeguridadVerificacion verif = DocumentoSeguridadVerificacion.builder()
                .documentoSeguridad(doc)
                .hashRegistrado(doc.getHashSha256())
                .hashCalculado(hashCalculado)
                .resultado(resultado)
                .usuario(usuario)
                .detalleTecnico(detalleTecnico)
                .build();
        verificacionRepository.save(verif);

        DocumentoSeguridadHistorial hist = DocumentoSeguridadHistorial.builder()
                .documentoSeguridad(doc)
                .accion("VERIFICACION_INTEGRIDAD")
                .descripcion("Verificación de integridad ejecutada: " + resultado.getDescripcion())
                .usuario(usuario)
                .build();
        historialRepository.save(hist);

        return VerificacionIntegridadResponse.builder()
                .codigo(doc.getCodigo())
                .hashRegistrado(doc.getHashSha256())
                .hashCalculado(hashCalculado)
                .resultado(resultado)
                .resultadoDesc(resultado.getDescripcion())
                .badgeClase(resultado.getBadgeClase())
                .fechaVerificacion(doc.getFechaUltimaVerificacion().format(FORMATO_FECHA_HORA))
                .detalleTecnico(detalleTecnico)
                .verificador(usuario.getUsername())
                .coincide(resultado == ResultadoVerificacionIntegridad.COINCIDENTE)
                .build();
    }

    @Override
    @Transactional
    public DocumentoSeguridadResponse revisar(UUID id, TransicionEstadoRequest req, CustomUserDetails user) {
        DocumentoSeguridadCiudadana doc = documentoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Documento no encontrado: " + id));

        Usuario usuario = obtenerUsuario(user);
        doc.setEstado(EstadoDocumentoSeguridad.EN_REVISION);
        if (req != null && req.getObservaciones() != null) {
            doc.setObservaciones(req.getObservaciones());
        }
        doc.setUpdatedBy(usuario.getUsername());
        doc = documentoRepository.save(doc);

        DocumentoSeguridadHistorial hist = DocumentoSeguridadHistorial.builder()
                .documentoSeguridad(doc)
                .accion("REVISION")
                .descripcion("Estado cambiado a EN_REVISION. Motivo: " + (req != null && req.getMotivo() != null ? req.getMotivo() : "Revisión ordinaria de contenido"))
                .usuario(usuario)
                .build();
        historialRepository.save(hist);

        return mapToResponse(doc);
    }

    @Override
    @Transactional
    public DocumentoSeguridadResponse observar(UUID id, TransicionEstadoRequest req, CustomUserDetails user) {
        DocumentoSeguridadCiudadana doc = documentoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Documento no encontrado: " + id));

        Usuario usuario = obtenerUsuario(user);
        doc.setEstado(EstadoDocumentoSeguridad.OBSERVADO);
        if (req != null && req.getObservaciones() != null) {
            doc.setObservaciones(req.getObservaciones());
        }
        doc.setUpdatedBy(usuario.getUsername());
        doc = documentoRepository.save(doc);

        DocumentoSeguridadHistorial hist = DocumentoSeguridadHistorial.builder()
                .documentoSeguridad(doc)
                .accion("OBSERVACION")
                .descripcion("Documento OBSERVADO. Motivo: " + (req != null && req.getMotivo() != null ? req.getMotivo() : "Observación técnica/administrativa"))
                .usuario(usuario)
                .build();
        historialRepository.save(hist);

        return mapToResponse(doc);
    }

    @Override
    @Transactional
    public DocumentoSeguridadResponse validarAdministrativamente(UUID id, TransicionEstadoRequest req, CustomUserDetails user) {
        DocumentoSeguridadCiudadana doc = documentoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Documento no encontrado: " + id));

        Usuario usuario = obtenerUsuario(user);
        doc.setEstado(EstadoDocumentoSeguridad.VALIDADO_ADMINISTRATIVAMENTE);
        doc.setFechaHoraValidacion(OffsetDateTime.now());
        doc.setUsuarioValidador(usuario);
        if (req != null && req.getObservaciones() != null) {
            doc.setObservaciones(req.getObservaciones());
        }
        doc.setUpdatedBy(usuario.getUsername());
        doc = documentoRepository.save(doc);

        DocumentoSeguridadHistorial hist = DocumentoSeguridadHistorial.builder()
                .documentoSeguridad(doc)
                .accion("VALIDACION_ADMINISTRATIVA")
                .descripcion("Documento validado para respaldo municipal de acuerdo con la Ley N° 31297 (No constituye peritaje forense ni cadena de custodia judicial).")
                .usuario(usuario)
                .build();
        historialRepository.save(hist);

        return mapToResponse(doc);
    }

    @Override
    @Transactional
    public DocumentoSeguridadResponse restringir(UUID id, TransicionEstadoRequest req, CustomUserDetails user) {
        DocumentoSeguridadCiudadana doc = documentoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Documento no encontrado: " + id));

        Usuario usuario = obtenerUsuario(user);
        doc.setEstado(EstadoDocumentoSeguridad.RESTRINGIDO);
        if (req != null) {
            if (req.getNuevoNivel() != null) {
                doc.setNivelConfidencialidad(req.getNuevoNivel());
            }
            if (req.getMotivo() != null) {
                doc.setMotivoRestriccion(req.getMotivo());
            }
        }
        doc.setUpdatedBy(usuario.getUsername());
        doc = documentoRepository.save(doc);

        DocumentoSeguridadHistorial hist = DocumentoSeguridadHistorial.builder()
                .documentoSeguridad(doc)
                .accion("RESTRICCION")
                .descripcion("Documento clasificado como RESTRINGIDO por protección de datos sensibles. Motivo: " + doc.getMotivoRestriccion())
                .usuario(usuario)
                .build();
        historialRepository.save(hist);

        return mapToResponse(doc);
    }

    @Override
    @Transactional
    public DocumentoSeguridadResponse archivar(UUID id, TransicionEstadoRequest req, CustomUserDetails user) {
        DocumentoSeguridadCiudadana doc = documentoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Documento no encontrado: " + id));

        Usuario usuario = obtenerUsuario(user);
        doc.setEstado(EstadoDocumentoSeguridad.ARCHIVADO);
        if (req != null && req.getMotivo() != null) {
            doc.setMotivoArchivado(req.getMotivo());
        }
        doc.setUpdatedBy(usuario.getUsername());
        doc = documentoRepository.save(doc);

        DocumentoSeguridadHistorial hist = DocumentoSeguridadHistorial.builder()
                .documentoSeguridad(doc)
                .accion("ARCHIVADO")
                .descripcion("Documento enviado a archivo pasivo municipal. Motivo: " + doc.getMotivoArchivado())
                .usuario(usuario)
                .build();
        historialRepository.save(hist);

        return mapToResponse(doc);
    }

    @Override
    @Transactional
    public DocumentoSeguridadResponse crearNuevaVersion(UUID id, NuevaVersionRequest req, CustomUserDetails user, String ip) {
        DocumentoSeguridadCiudadana docPadre = documentoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Documento original no encontrado: " + id));

        MultipartFile file = req.getNuevoArchivo();
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Debe seleccionar el archivo de la nueva versión.");
        }

        Usuario usuario = obtenerUsuario(user);

        String originalFilename = sanitizarNombreArchivo(file.getOriginalFilename());
        String ext = extraerExtension(originalFilename);
        if (!EXTENSIONES_PERMITIDAS.contains(ext.toLowerCase())) {
            throw new BusinessException("Extensión no permitida para nueva versión: " + ext);
        }

        byte[] contenido;
        try {
            contenido = file.getBytes();
        } catch (IOException e) {
            throw new BusinessException("Error al leer el nuevo archivo de versión.");
        }

        String hashCalculado = calcularSha256(contenido);

        String nombreAlmacenado;
        try (InputStream is = new ByteArrayInputStream(contenido)) {
            nombreAlmacenado = storageService.store(is, ext);
        } catch (IOException e) {
            throw new BusinessException("Error al persistir archivo de la nueva versión.");
        }

        int siguienteVersion = docPadre.getVersionActual() + 1;
        docPadre.setVersionActual(siguienteVersion);
        docPadre.setUpdatedBy(usuario.getUsername());
        documentoRepository.save(docPadre);

        String nuevoCodigo = generarSiguienteCodigo();

        DocumentoSeguridadCiudadana nuevaVersion = DocumentoSeguridadCiudadana.builder()
                .codigo(nuevoCodigo)
                .documento(docPadre.getDocumento())
                .tipoDocumento(docPadre.getTipoDocumento())
                .nombreOriginal(originalFilename)
                .nombreAlmacenado(nombreAlmacenado)
                .extension(ext)
                .mimeType(file.getContentType())
                .tamanoBytes((long) contenido.length)
                .hashSha256(hashCalculado)
                .fechaHoraCarga(OffsetDateTime.now())
                .usuarioCarga(usuario)
                .origenDocumento(docPadre.getOrigenDocumento())
                .descripcion("Versión " + siguienteVersion + " de " + docPadre.getCodigo() + ". " + (req.getMotivoVersion() != null ? req.getMotivoVersion() : ""))
                .estado(EstadoDocumentoSeguridad.REGISTRADO)
                .nivelConfidencialidad(docPadre.getNivelConfidencialidad())
                .observaciones(req.getMotivoVersion())
                .versionActual(siguienteVersion)
                .documentoPadre(docPadre)
                .incidencia(docPadre.getIncidencia())
                .alerta(docPadre.getAlerta())
                .atencion(docPadre.getAtencion())
                .intervencion(docPadre.getIntervencion())
                .patrullaje(docPadre.getPatrullaje())
                .ultimoResultadoVerificacion(ResultadoVerificacionIntegridad.COINCIDENTE)
                .fechaUltimaVerificacion(OffsetDateTime.now())
                .usuarioUltimaVerificacion(usuario)
                .createdBy(usuario.getUsername())
                .updatedBy(usuario.getUsername())
                .build();

        nuevaVersion = documentoRepository.save(nuevaVersion);

        DocumentoSeguridadHistorial hist = DocumentoSeguridadHistorial.builder()
                .documentoSeguridad(docPadre)
                .accion("NUEVA_VERSION")
                .descripcion("Se registró la versión " + siguienteVersion + " bajo el código " + nuevoCodigo + ". Motivo: " + req.getMotivoVersion())
                .usuario(usuario)
                .ipOrigen(ip)
                .build();
        historialRepository.save(hist);

        return mapToResponse(nuevaVersion);
    }

    @Override
    @Transactional
    public void asociarRegistro(UUID id, TipoRelacionDocumentoSeguridad tipo, UUID entidadId, RolRelacionDocumento rol, CustomUserDetails user) {
        DocumentoSeguridadCiudadana doc = documentoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Documento no encontrado: " + id));

        Usuario usuario = obtenerUsuario(user);

        if (relacionRepository.existsByDocumentoSeguridadIdAndTipoEntidadAndEntidadId(id, tipo, entidadId)) {
            throw new BusinessException("La asociación con este registro ya existe.");
        }

        DocumentoSeguridadRelacion rel = DocumentoSeguridadRelacion.builder()
                .documentoSeguridad(doc)
                .tipoEntidad(tipo)
                .entidadId(entidadId)
                .rolRelacion(rol != null ? rol : RolRelacionDocumento.RESPALDO)
                .usuarioAsocia(usuario)
                .build();
        relacionRepository.save(rel);

        DocumentoSeguridadHistorial hist = DocumentoSeguridadHistorial.builder()
                .documentoSeguridad(doc)
                .accion("ASOCIACION")
                .descripcion("Asociado con " + tipo.getDescripcion() + " ID: " + entidadId + " con rol " + rel.getRolRelacion().getDescripcion())
                .usuario(usuario)
                .build();
        historialRepository.save(hist);
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentoEstadisticasResponse obtenerEstadisticas() {
        long total = documentoRepository.count();
        long fotos = documentoRepository.countByTipoDocumento(TipoDocumentoSeguridad.FOTOGRAFIA);
        long videos = documentoRepository.countByTipoDocumento(TipoDocumentoSeguridad.VIDEO);
        long enRevision = documentoRepository.countByEstado(EstadoDocumentoSeguridad.EN_REVISION);
        long validados = documentoRepository.countByEstado(EstadoDocumentoSeguridad.VALIDADO_ADMINISTRATIVAMENTE);
        long restringidos = documentoRepository.countByEstado(EstadoDocumentoSeguridad.RESTRINGIDO);
        long noCoincidente = documentoRepository.countByUltimoResultadoVerificacion(ResultadoVerificacionIntegridad.NO_COINCIDENTE);
        long verificados = documentoRepository.countByUltimoResultadoVerificacion(ResultadoVerificacionIntegridad.COINCIDENTE);

        return DocumentoEstadisticasResponse.builder()
                .totalDocumentos(total)
                .totalFotografias(fotos)
                .totalVideos(videos)
                .totalEnRevision(enRevision)
                .totalValidados(validados)
                .totalRestringidos(restringidos)
                .totalIntegridadNoCoincidente(noCoincidente)
                .totalVerificados(verificados)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportarExcel(DocumentoFiltroRequest filtro) {
        Page<DocumentoSeguridadResponse> pagina = listar(filtro, Pageable.unpaged());
        List<DocumentoSeguridadResponse> lista = pagina.getContent();

        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("Evidencias y Documentos");

            CellStyle headerStyle = wb.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            org.apache.poi.ss.usermodel.Font headerFont = wb.createFont();
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            Row r0 = sheet.createRow(0);
            Cell c0 = r0.createCell(0);
            c0.setCellValue("MUNICIPALIDAD DISTRITAL DE QUINUA — SISTEMA DE SEGURIDAD CIUDADANA Y SERENAZGO");
            sheet.createRow(1).createCell(0).setCellValue("REPORTE OFICIAL DE EVIDENCIAS Y DOCUMENTOS DE RESPALDO (LEY N° 31297)");
            sheet.createRow(2).createCell(0).setCellValue("Fecha de emisión: " + OffsetDateTime.now(ZONA_LIMA).format(FORMATO_FECHA_HORA) + " | Registros: " + lista.size());

            String[] cols = {
                    "N°", "Código", "Nombre Original", "Tipo", "Estado",
                    "Confidencialidad", "Origen", "Tamaño", "Hash SHA-256",
                    "Integridad Digital", "Fecha Carga", "Responsable", "Registro Asociado"
            };

            Row rHeader = sheet.createRow(4);
            for (int i = 0; i < cols.length; i++) {
                Cell cell = rHeader.createCell(i);
                cell.setCellValue(cols[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 5;
            for (int i = 0; i < lista.size(); i++) {
                DocumentoSeguridadResponse d = lista.get(i);
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(i + 1);
                row.createCell(1).setCellValue(d.getCodigo());
                row.createCell(2).setCellValue(d.getNombreOriginal());
                row.createCell(3).setCellValue(d.getTipoDocumentoDesc());
                row.createCell(4).setCellValue(d.getEstadoDesc());
                row.createCell(5).setCellValue(d.getNivelConfidencialidadDesc());
                row.createCell(6).setCellValue(d.getOrigenDocumentoDesc());
                row.createCell(7).setCellValue(d.getTamanoFormateado());
                row.createCell(8).setCellValue(d.getHashSha256());
                row.createCell(9).setCellValue(d.getUltimoResultadoVerificacionDesc());
                row.createCell(10).setCellValue(d.getFechaHoraCargaFormateada());
                row.createCell(11).setCellValue(d.getUsuarioCargaNombre());
                row.createCell(12).setCellValue(d.getRegistroAsociadoCodigo() != null ? d.getRegistroAsociadoCodigo() : "-");
            }

            // Descargo legal
            Row rDisclaimer = sheet.createRow(rowIdx + 2);
            rDisclaimer.createCell(0).setCellValue("NOTA LEGAL: Los archivos son de respaldo municipal de conformidad con la Ley N° 31297. No acreditan por sí mismos cadena de custodia judicial.");

            for (int i = 0; i < cols.length; i++) {
                sheet.autoSizeColumn(i);
            }

            wb.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            log.error("Error al exportar Excel de evidencias de seguridad", e);
            throw new BusinessException("Error al generar el archivo Excel.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportarPdfFicha(UUID id, CustomUserDetails user) {
        DocumentoSeguridadCiudadana doc = documentoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Documento no encontrado: " + id));

        validarAccesoConfidencialidad(doc, user);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 40, 40);
            PdfWriter.getInstance(document, out);
            document.open();

            Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 15, Color.BLACK);
            Font fontSub = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.DARK_GRAY);
            Font fontSeccion = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, new Color(29, 78, 216));
            Font fontLabel = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.DARK_GRAY);
            Font fontValor = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK);
            Font fontLegal = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, Color.GRAY);

            // Encabezado
            Paragraph pHeader = new Paragraph("MUNICIPALIDAD DISTRITAL DE QUINUA", fontTitulo);
            pHeader.setAlignment(Element.ALIGN_CENTER);
            document.add(pHeader);

            Paragraph pSub = new Paragraph("GERENCIA DE SEGURIDAD CIUDADANA Y SERENAZGO\nFICHA TÉCNICA DE EVIDENCIA Y DOCUMENTO DE RESPALDO", fontSub);
            pSub.setAlignment(Element.ALIGN_CENTER);
            pSub.setSpacingAfter(15);
            document.add(pSub);

            // Tabla 1: Datos Generales
            PdfPTable tGeneral = new PdfPTable(2);
            tGeneral.setWidthPercentage(100);
            tGeneral.setSpacingAfter(12);

            PdfPCell cSec1 = new PdfPCell(new Phrase("1. IDENTIFICACIÓN Y METADATOS DEL ARCHIVO", fontSeccion));
            cSec1.setColspan(2);
            cSec1.setBackgroundColor(new Color(241, 245, 249));
            cSec1.setPadding(6);
            tGeneral.addCell(cSec1);

            agregarFila(tGeneral, "Código del Documento:", doc.getCodigo(), fontLabel, fontValor);
            agregarFila(tGeneral, "Nombre Original:", doc.getNombreOriginal(), fontLabel, fontValor);
            agregarFila(tGeneral, "Tipo Documental:", doc.getTipoDocumento().getDescripcion(), fontLabel, fontValor);
            agregarFila(tGeneral, "Estado Administrativo:", doc.getEstado().getDescripcion(), fontLabel, fontValor);
            agregarFila(tGeneral, "Nivel de Confidencialidad:", doc.getNivelConfidencialidad().getDescripcion(), fontLabel, fontValor);
            agregarFila(tGeneral, "Origen del Documento:", doc.getOrigenDocumento().getDescripcion(), fontLabel, fontValor);
            agregarFila(tGeneral, "Formato y Tamaño:", doc.getMimeType() + " (" + formatearTamano(doc.getTamanoBytes()) + ")", fontLabel, fontValor);
            agregarFila(tGeneral, "Fecha y Hora de Carga:", doc.getFechaHoraCarga().format(FORMATO_FECHA_HORA), fontLabel, fontValor);
            agregarFila(tGeneral, "Usuario Responsable de Carga:", doc.getUsuarioCarga().getUsername(), fontLabel, fontValor);
            agregarFila(tGeneral, "Versión Actual:", "Versión " + doc.getVersionActual(), fontLabel, fontValor);
            document.add(tGeneral);

            // Tabla 2: Integridad Digital
            PdfPTable tHash = new PdfPTable(2);
            tHash.setWidthPercentage(100);
            tHash.setSpacingAfter(12);

            PdfPCell cSec2 = new PdfPCell(new Phrase("2. INTEGRIDAD DIGITAL Y HUELLA CRIPTOGRÁFICA", fontSeccion));
            cSec2.setColspan(2);
            cSec2.setBackgroundColor(new Color(241, 245, 249));
            cSec2.setPadding(6);
            tHash.addCell(cSec2);

            agregarFila(tHash, "Algoritmo de Integridad:", "SHA-256 (Secure Hash Algorithm 256 bits)", fontLabel, fontValor);
            agregarFila(tHash, "Huella Digital Registrada:", doc.getHashSha256(), fontLabel, fontValor);
            agregarFila(tHash, "Resultado de Verificación:", doc.getUltimoResultadoVerificacion().getDescripcion(), fontLabel, fontValor);
            agregarFila(tHash, "Fecha Última Verificación:", doc.getFechaUltimaVerificacion() != null ? doc.getFechaUltimaVerificacion().format(FORMATO_FECHA_HORA) : "Pendiente", fontLabel, fontValor);
            document.add(tHash);

            // Tabla 3: Actuaciones y Relaciones
            PdfPTable tRel = new PdfPTable(2);
            tRel.setWidthPercentage(100);
            tRel.setSpacingAfter(15);

            PdfPCell cSec3 = new PdfPCell(new Phrase("3. VINCULACIÓN CON ACTUACIONES DE SERENAZGO", fontSeccion));
            cSec3.setColspan(2);
            cSec3.setBackgroundColor(new Color(241, 245, 249));
            cSec3.setPadding(6);
            tRel.addCell(cSec3);

            if (doc.getIncidencia() != null) {
                agregarFila(tRel, "Incidencia de Seguridad:", doc.getIncidencia().getCodigo() + " - " + doc.getIncidencia().getTitulo(), fontLabel, fontValor);
            }
            if (doc.getAlerta() != null) {
                agregarFila(tRel, "Alerta Ciudadana:", doc.getAlerta().getCodigo() + " - " + doc.getAlerta().getTitulo(), fontLabel, fontValor);
            }
            if (doc.getAtencion() != null) {
                agregarFila(tRel, "Atención / Despacho:", doc.getAtencion().getCodigo(), fontLabel, fontValor);
            }
            if (doc.getIntervencion() != null) {
                agregarFila(tRel, "Intervención de Campo:", doc.getIntervencion().getCodigo(), fontLabel, fontValor);
            }
            if (doc.getPatrullaje() != null) {
                agregarFila(tRel, "Patrullaje Municipal:", doc.getPatrullaje().getCodigo(), fontLabel, fontValor);
            }
            if (doc.getIncidencia() == null && doc.getAlerta() == null && doc.getAtencion() == null && doc.getIntervencion() == null && doc.getPatrullaje() == null) {
                agregarFila(tRel, "Vinculación Principal:", "Documento administrativo general de reserva / archivo", fontLabel, fontValor);
            }
            document.add(tRel);

            // Cláusula legal obligatoria
            Paragraph pLegal = new Paragraph(
                    "DESCARGO LEGAL INSTITUCIONAL (LEY N° 31297 / LEY N° 27933):\n" +
                            "El presente documento y sus metadatos forman parte del acervo documental de respaldo administrativo de la Municipalidad Distrital de Quinua. " +
                            "El cálculo de huella criptográfica SHA-256 permite detectar alteraciones respecto a la referencia almacenada, pero no constituye por sí mismo " +
                            "peritaje forense ni certificación de cadena de custodia penal. La validez probatoria en procesos judiciales corresponde exclusivamente a las autoridades competentes.",
                    fontLegal
            );
            pLegal.setSpacingBefore(10);
            document.add(pLegal);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Error al generar PDF de ficha técnica de evidencia: {}", id, e);
            throw new BusinessException("Error al generar la ficha técnica en PDF.");
        }
    }

    // =========================================================================
    // MÉTODOS AUXILIARES
    // =========================================================================

    private void agregarFila(PdfPTable table, String label, String valor, Font fLabel, Font fValor) {
        PdfPCell c1 = new PdfPCell(new Phrase(label, fLabel));
        c1.setPadding(5);
        c1.setBorderColor(new Color(226, 232, 240));
        table.addCell(c1);

        PdfPCell c2 = new PdfPCell(new Phrase(valor != null ? valor : "-", fValor));
        c2.setPadding(5);
        c2.setBorderColor(new Color(226, 232, 240));
        table.addCell(c2);
    }

    private void validarAccesoConfidencialidad(DocumentoSeguridadCiudadana doc, CustomUserDetails user) {
        if (doc.getNivelConfidencialidad() == NivelConfidencialidad.PUBLICO) {
            return;
        }
        if (user == null) {
            throw new BusinessException("Se requiere autenticación para acceder a este documento.");
        }
        boolean esAdmin = user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean esSupervisor = user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPERVISOR_SERENAZGO"));
        boolean tienePermisoVer = user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("SERENAZGO_DOCUMENTOS_VER"));

        if (!esAdmin && !esSupervisor && !tienePermisoVer) {
            throw new BusinessException("No tiene privilegios suficientes para acceder a documentos de nivel: " + doc.getNivelConfidencialidad());
        }
    }

    private void asociarEntidadesDirectas(DocumentoSeguridadCiudadana doc, DocumentoCargaRequest req) {
        if (req.getIncidenciaId() != null) {
            incidenciaRepository.findById(req.getIncidenciaId()).ifPresent(doc::setIncidencia);
        }
        if (req.getAlertaId() != null) {
            alertaRepository.findById(req.getAlertaId()).ifPresent(doc::setAlerta);
        }
        if (req.getAtencionId() != null) {
            atencionRepository.findById(req.getAtencionId()).ifPresent(doc::setAtencion);
        }
        if (req.getIntervencionId() != null) {
            intervencionRepository.findById(req.getIntervencionId()).ifPresent(doc::setIntervencion);
        }
        if (req.getPatrullajeId() != null) {
            patrullajeRepository.findById(req.getPatrullajeId()).ifPresent(doc::setPatrullaje);
        }
    }

    private void crearRelacionesMultiples(DocumentoSeguridadCiudadana doc, DocumentoCargaRequest req, Usuario usuario) {
        if (req.getIncidenciaId() != null) {
            relacionRepository.save(DocumentoSeguridadRelacion.builder()
                    .documentoSeguridad(doc)
                    .tipoEntidad(TipoRelacionDocumentoSeguridad.INCIDENCIA)
                    .entidadId(req.getIncidenciaId())
                    .rolRelacion(RolRelacionDocumento.EVIDENCIA_PRIMARIA)
                    .usuarioAsocia(usuario)
                    .build());
        }
        if (req.getAlertaId() != null) {
            relacionRepository.save(DocumentoSeguridadRelacion.builder()
                    .documentoSeguridad(doc)
                    .tipoEntidad(TipoRelacionDocumentoSeguridad.ALERTA)
                    .entidadId(req.getAlertaId())
                    .rolRelacion(RolRelacionDocumento.RESPALDO)
                    .usuarioAsocia(usuario)
                    .build());
        }
        if (req.getAtencionId() != null) {
            relacionRepository.save(DocumentoSeguridadRelacion.builder()
                    .documentoSeguridad(doc)
                    .tipoEntidad(TipoRelacionDocumentoSeguridad.ATENCION)
                    .entidadId(req.getAtencionId())
                    .rolRelacion(RolRelacionDocumento.ANEXO)
                    .usuarioAsocia(usuario)
                    .build());
        }
        if (req.getIntervencionId() != null) {
            relacionRepository.save(DocumentoSeguridadRelacion.builder()
                    .documentoSeguridad(doc)
                    .tipoEntidad(TipoRelacionDocumentoSeguridad.INTERVENCION)
                    .entidadId(req.getIntervencionId())
                    .rolRelacion(RolRelacionDocumento.EVIDENCIA_PRIMARIA)
                    .usuarioAsocia(usuario)
                    .build());
        }
        if (req.getPatrullajeId() != null) {
            relacionRepository.save(DocumentoSeguridadRelacion.builder()
                    .documentoSeguridad(doc)
                    .tipoEntidad(TipoRelacionDocumentoSeguridad.PATRULLAJE)
                    .entidadId(req.getPatrullajeId())
                    .rolRelacion(RolRelacionDocumento.RESPALDO)
                    .usuarioAsocia(usuario)
                    .build());
        }
    }

    private synchronized String generarSiguienteCodigo() {
        int anio = LocalDate.now().getYear();
        String prefijo = "DOC-SEG-" + anio + "-";
        String maxCodigo = documentoRepository.findMaxCodigoConPrefijo(prefijo);

        int siguiente = 1;
        if (maxCodigo != null && maxCodigo.startsWith(prefijo)) {
            try {
                String correlativoStr = maxCodigo.substring(prefijo.length());
                siguiente = Integer.parseInt(correlativoStr) + 1;
            } catch (NumberFormatException ignored) {}
        }
        return String.format("%s%05d", prefijo, siguiente);
    }

    private String calcularSha256(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(data);
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 no disponible", e);
        }
    }

    private String calcularSha256(InputStream is) throws IOException {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] buf = new byte[8192];
            int read;
            while ((read = is.read(buf)) != -1) {
                md.update(buf, 0, read);
            }
            byte[] hash = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 no disponible", e);
        }
    }

    private String sanitizarNombreArchivo(String originalFilename) {
        if (originalFilename == null) return "archivo";
        return originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private String extraerExtension(String filename) {
        int idx = filename.lastIndexOf('.');
        if (idx > 0 && idx < filename.length() - 1) {
            return filename.substring(idx + 1).toLowerCase();
        }
        return "bin";
    }

    private CategoriaDocumento determinarCategoriaComun(TipoDocumentoSeguridad tipo) {
        if (tipo == null) return CategoriaDocumento.OTRO;
        return switch (tipo) {
            case FOTOGRAFIA -> CategoriaDocumento.ANEXO;
            case INFORME -> CategoriaDocumento.INFORME;
            case ACTA_ADMINISTRATIVA -> CategoriaDocumento.ANEXO;
            case COMUNICACION_INSTITUCIONAL -> CategoriaDocumento.OFICIO;
            default -> CategoriaDocumento.OTRO;
        };
    }

    private ClasificacionAcceso determinarClasificacionComun(NivelConfidencialidad nivel) {
        if (nivel == null) return ClasificacionAcceso.INTERNO;
        return switch (nivel) {
            case PUBLICO -> ClasificacionAcceso.PUBLICO;
            case ORDINARIO -> ClasificacionAcceso.INTERNO;
            case RESERVADO, CONFIDENCIAL -> ClasificacionAcceso.CONFIDENCIAL;
            case RESTRINGIDO -> ClasificacionAcceso.RESTRINGIDO;
        };
    }

    private Usuario obtenerUsuario(CustomUserDetails user) {
        if (user == null) {
            return usuarioRepository.findAll().stream().findFirst()
                    .orElseThrow(() -> new BusinessException("No existen usuarios en el sistema"));
        }
        return usuarioRepository.findByUsername(user.getUsername())
                .orElseGet(() -> usuarioRepository.findAll().stream().findFirst()
                        .orElseThrow(() -> new BusinessException("Usuario no encontrado: " + user.getUsername())));
    }

    private String formatearTamano(Long bytes) {
        if (bytes == null || bytes == 0) return "0 B";
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format(Locale.US, "%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format(Locale.US, "%.2f MB", bytes / (1024.0 * 1024.0));
        return String.format(Locale.US, "%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }

    private DocumentoSeguridadResponse mapToResponse(DocumentoSeguridadCiudadana doc) {
        String regCodigo = null;
        String regTipo = null;
        String regUrl = null;

        if (doc.getIncidencia() != null) {
            regCodigo = doc.getIncidencia().getCodigo();
            regTipo = "Incidencia";
            regUrl = "/dashboard/serenazgo/incidencias/" + doc.getIncidencia().getId();
        } else if (doc.getAlerta() != null) {
            regCodigo = doc.getAlerta().getCodigo();
            regTipo = "Alerta";
            regUrl = "/dashboard/serenazgo/alertas/" + doc.getAlerta().getId();
        } else if (doc.getAtencion() != null) {
            regCodigo = doc.getAtencion().getCodigo();
            regTipo = "Atención";
            regUrl = "/dashboard/serenazgo/atenciones/" + doc.getAtencion().getId();
        } else if (doc.getIntervencion() != null) {
            regCodigo = doc.getIntervencion().getCodigo();
            regTipo = "Intervención";
            regUrl = "/dashboard/serenazgo/intervenciones/" + doc.getIntervencion().getId();
        } else if (doc.getPatrullaje() != null) {
            regCodigo = doc.getPatrullaje().getCodigo();
            regTipo = "Patrullaje";
            regUrl = "/dashboard/serenazgo/patrullajes/" + doc.getPatrullaje().getId();
        }

        boolean esImg = doc.getMimeType() != null && doc.getMimeType().startsWith("image/");
        boolean esVid = doc.getMimeType() != null && doc.getMimeType().startsWith("video/");
        boolean esPdf = doc.getMimeType() != null && doc.getMimeType().equals("application/pdf");

        return DocumentoSeguridadResponse.builder()
                .id(doc.getId())
                .codigo(doc.getCodigo())
                .nombreOriginal(doc.getNombreOriginal())
                .extension(doc.getExtension())
                .mimeType(doc.getMimeType())
                .tamanoBytes(doc.getTamanoBytes())
                .tamanoFormateado(formatearTamano(doc.getTamanoBytes()))
                .hashSha256(doc.getHashSha256())
                .tipoDocumento(doc.getTipoDocumento())
                .tipoDocumentoDesc(doc.getTipoDocumento().getDescripcion())
                .tipoDocumentoIcono(doc.getTipoDocumento().getIcono())
                .estado(doc.getEstado())
                .estadoDesc(doc.getEstado().getDescripcion())
                .estadoBadgeClase(doc.getEstado().getBadgeClase())
                .origenDocumento(doc.getOrigenDocumento())
                .origenDocumentoDesc(doc.getOrigenDocumento().getDescripcion())
                .nivelConfidencialidad(doc.getNivelConfidencialidad())
                .nivelConfidencialidadDesc(doc.getNivelConfidencialidad().getDescripcion())
                .descripcion(doc.getDescripcion())
                .fechaHoraCarga(doc.getFechaHoraCarga())
                .fechaHoraCargaFormateada(doc.getFechaHoraCarga().format(FORMATO_FECHA_HORA))
                .usuarioCargaNombre(doc.getUsuarioCarga() != null ? doc.getUsuarioCarga().getUsername() : "-")
                .versionActual(doc.getVersionActual())
                .registroAsociadoCodigo(regCodigo)
                .registroAsociadoTipo(regTipo)
                .registroAsociadoUrl(regUrl)
                .ultimoResultadoVerificacion(doc.getUltimoResultadoVerificacion())
                .ultimoResultadoVerificacionDesc(doc.getUltimoResultadoVerificacion().getDescripcion())
                .verificacionBadgeClase(doc.getUltimoResultadoVerificacion().getBadgeClase())
                .fechaUltimaVerificacionFormateada(doc.getFechaUltimaVerificacion() != null ? doc.getFechaUltimaVerificacion().format(FORMATO_FECHA_HORA) : "Pendiente")
                .esImagen(esImg)
                .esVideo(esVid)
                .esPdf(esPdf)
                .build();
    }

    private DocumentoSeguridadDetalleResponse mapToDetalleResponse(DocumentoSeguridadCiudadana doc) {
        DocumentoSeguridadResponse base = mapToResponse(doc);

        List<DocumentoSeguridadRelacion> rels = relacionRepository.findByDocumentoSeguridadIdOrderByFechaAsociacionDesc(doc.getId());
        List<DocumentoSeguridadDetalleResponse.RelacionItemResponse> listaRels = new ArrayList<>();
        for (DocumentoSeguridadRelacion r : rels) {
            String cod = "-";
            String url = "#";
            if (r.getTipoEntidad() == TipoRelacionDocumentoSeguridad.INCIDENCIA && doc.getIncidencia() != null) {
                cod = doc.getIncidencia().getCodigo();
                url = "/dashboard/serenazgo/incidencias/" + doc.getIncidencia().getId();
            } else if (r.getTipoEntidad() == TipoRelacionDocumentoSeguridad.ALERTA && doc.getAlerta() != null) {
                cod = doc.getAlerta().getCodigo();
                url = "/dashboard/serenazgo/alertas/" + doc.getAlerta().getId();
            } else if (r.getTipoEntidad() == TipoRelacionDocumentoSeguridad.ATENCION && doc.getAtencion() != null) {
                cod = doc.getAtencion().getCodigo();
                url = "/dashboard/serenazgo/atenciones/" + doc.getAtencion().getId();
            } else if (r.getTipoEntidad() == TipoRelacionDocumentoSeguridad.INTERVENCION && doc.getIntervencion() != null) {
                cod = doc.getIntervencion().getCodigo();
                url = "/dashboard/serenazgo/intervenciones/" + doc.getIntervencion().getId();
            } else if (r.getTipoEntidad() == TipoRelacionDocumentoSeguridad.PATRULLAJE && doc.getPatrullaje() != null) {
                cod = doc.getPatrullaje().getCodigo();
                url = "/dashboard/serenazgo/patrullajes/" + doc.getPatrullaje().getId();
            }

            listaRels.add(DocumentoSeguridadDetalleResponse.RelacionItemResponse.builder()
                    .id(r.getId())
                    .tipoEntidad(r.getTipoEntidad())
                    .tipoEntidadDesc(r.getTipoEntidad().getDescripcion())
                    .entidadId(r.getEntidadId())
                    .entidadCodigo(cod)
                    .urlDetalle(url)
                    .rolRelacion(r.getRolRelacion())
                    .rolRelacionDesc(r.getRolRelacion().getDescripcion())
                    .fechaAsociacionFormateada(r.getFechaAsociacion().format(FORMATO_FECHA_HORA))
                    .usuarioAsociaNombre(r.getUsuarioAsocia() != null ? r.getUsuarioAsocia().getUsername() : "-")
                    .build());
        }

        List<DocumentoSeguridadVerificacion> vers = verificacionRepository.findByDocumentoSeguridadIdOrderByFechaVerificacionDesc(doc.getId());
        List<DocumentoSeguridadDetalleResponse.VerificacionItemResponse> listaVerif = new ArrayList<>();
        for (DocumentoSeguridadVerificacion v : vers) {
            listaVerif.add(DocumentoSeguridadDetalleResponse.VerificacionItemResponse.builder()
                    .id(v.getId())
                    .hashRegistrado(v.getHashRegistrado())
                    .hashCalculado(v.getHashCalculado())
                    .resultado(v.getResultado())
                    .resultadoDesc(v.getResultado().getDescripcion())
                    .badgeClase(v.getResultado().getBadgeClase())
                    .fechaVerificacionFormateada(v.getFechaVerificacion().format(FORMATO_FECHA_HORA))
                    .usuarioNombre(v.getUsuario() != null ? v.getUsuario().getUsername() : "-")
                    .detalleTecnico(v.getDetalleTecnico())
                    .build());
        }

        List<DocumentoSeguridadHistorial> hists = historialRepository.findByDocumentoSeguridadIdOrderByFechaHoraDesc(doc.getId());
        List<DocumentoSeguridadDetalleResponse.HistorialItemResponse> listaHist = new ArrayList<>();
        for (DocumentoSeguridadHistorial h : hists) {
            listaHist.add(DocumentoSeguridadDetalleResponse.HistorialItemResponse.builder()
                    .id(h.getId())
                    .accion(h.getAccion())
                    .descripcion(h.getDescripcion())
                    .usuarioNombre(h.getUsuario() != null ? h.getUsuario().getUsername() : "-")
                    .fechaHoraFormateada(h.getFechaHora().format(FORMATO_FECHA_HORA))
                    .ipOrigen(h.getIpOrigen())
                    .build());
        }

        List<DocumentoSeguridadCiudadana> versionesHermanas = documentoRepository.findByDocumentoPadreIdOrderByVersionActualDesc(doc.getId());
        List<DocumentoSeguridadDetalleResponse.VersionItemResponse> listaVersiones = new ArrayList<>();
        for (DocumentoSeguridadCiudadana v : versionesHermanas) {
            listaVersiones.add(DocumentoSeguridadDetalleResponse.VersionItemResponse.builder()
                    .id(v.getId())
                    .codigo(v.getCodigo())
                    .version(v.getVersionActual())
                    .nombreOriginal(v.getNombreOriginal())
                    .tamanoBytes(v.getTamanoBytes())
                    .tamanoFormateado(formatearTamano(v.getTamanoBytes()))
                    .hashSha256(v.getHashSha256())
                    .fechaHoraCargaFormateada(v.getFechaHoraCarga().format(FORMATO_FECHA_HORA))
                    .usuarioCargaNombre(v.getUsuarioCarga() != null ? v.getUsuarioCarga().getUsername() : "-")
                    .motivo(v.getObservaciones())
                    .build());
        }

        return DocumentoSeguridadDetalleResponse.builder()
                .id(doc.getId())
                .codigo(doc.getCodigo())
                .nombreOriginal(doc.getNombreOriginal())
                .nombreAlmacenado(doc.getNombreAlmacenado())
                .extension(doc.getExtension())
                .mimeType(doc.getMimeType())
                .tamanoBytes(doc.getTamanoBytes())
                .tamanoFormateado(base.getTamanoFormateado())
                .hashSha256(doc.getHashSha256())
                .tipoDocumento(doc.getTipoDocumento())
                .tipoDocumentoDesc(base.getTipoDocumentoDesc())
                .tipoDocumentoIcono(base.getTipoDocumentoIcono())
                .estado(doc.getEstado())
                .estadoDesc(base.getEstadoDesc())
                .estadoBadgeClase(base.getEstadoBadgeClase())
                .origenDocumento(doc.getOrigenDocumento())
                .origenDocumentoDesc(base.getOrigenDocumentoDesc())
                .nivelConfidencialidad(doc.getNivelConfidencialidad())
                .nivelConfidencialidadDesc(base.getNivelConfidencialidadDesc())
                .descripcion(doc.getDescripcion())
                .observaciones(doc.getObservaciones())
                .motivoRestriccion(doc.getMotivoRestriccion())
                .motivoArchivado(doc.getMotivoArchivado())
                .fechaHoraCarga(doc.getFechaHoraCarga())
                .fechaHoraCargaFormateada(base.getFechaHoraCargaFormateada())
                .usuarioCargaNombre(base.getUsuarioCargaNombre())
                .usuarioCargaUsername(doc.getUsuarioCarga() != null ? doc.getUsuarioCarga().getUsername() : "-")
                .fechaHoraValidacion(doc.getFechaHoraValidacion())
                .fechaHoraValidacionFormateada(doc.getFechaHoraValidacion() != null ? doc.getFechaHoraValidacion().format(FORMATO_FECHA_HORA) : "-")
                .usuarioValidadorNombre(doc.getUsuarioValidador() != null ? doc.getUsuarioValidador().getUsername() : "-")
                .versionActual(doc.getVersionActual())
                .documentoPadreId(doc.getDocumentoPadre() != null ? doc.getDocumentoPadre().getId() : null)
                .fechaCaptura(doc.getFechaCaptura())
                .fechaCapturaFormateada(doc.getFechaCaptura() != null ? doc.getFechaCaptura().format(FORMATO_FECHA_HORA) : "-")
                .ubicacionCaptura(doc.getUbicacionCaptura())
                .latitud(doc.getLatitud())
                .longitud(doc.getLongitud())
                .autoriaDeclarada(doc.getAutoriaDeclarada())
                .ultimoResultadoVerificacion(doc.getUltimoResultadoVerificacion())
                .ultimoResultadoVerificacionDesc(base.getUltimoResultadoVerificacionDesc())
                .verificacionBadgeClase(base.getVerificacionBadgeClase())
                .fechaUltimaVerificacion(doc.getFechaUltimaVerificacion())
                .fechaUltimaVerificacionFormateada(base.getFechaUltimaVerificacionFormateada())
                .usuarioUltimaVerificacionNombre(doc.getUsuarioUltimaVerificacion() != null ? doc.getUsuarioUltimaVerificacion().getUsername() : "-")
                .esImagen(base.isEsImagen())
                .esVideo(base.isEsVideo())
                .esPdf(base.isEsPdf())
                .relaciones(listaRels)
                .verificaciones(listaVerif)
                .historial(listaHist)
                .versiones(listaVersiones)
                .build();
    }
}
