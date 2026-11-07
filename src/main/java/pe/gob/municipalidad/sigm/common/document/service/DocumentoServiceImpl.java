package pe.gob.municipalidad.sigm.common.document.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pe.gob.municipalidad.sigm.common.document.audit.DocumentoAuditService;
import pe.gob.municipalidad.sigm.common.document.dto.DocumentDtos;
import pe.gob.municipalidad.sigm.common.document.dto.DocumentDtos.*;
import pe.gob.municipalidad.sigm.common.document.entity.*;
import pe.gob.municipalidad.sigm.common.document.mapper.DocumentoMapper;
import pe.gob.municipalidad.sigm.common.document.pdf.PdfGeneratorService;
import pe.gob.municipalidad.sigm.common.document.repository.*;
import pe.gob.municipalidad.sigm.common.document.storage.DocumentStorageService;
import pe.gob.municipalidad.sigm.common.document.validation.DocumentFileValidator;
import pe.gob.municipalidad.sigm.common.document.validation.Sha256Calculator;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.notification.dto.NotificationDtos.EnviarNotificacionRequest;
import pe.gob.municipalidad.sigm.common.notification.service.CentralNotificationService;
import pe.gob.municipalidad.sigm.common.repository.OficinaRepository;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DocumentoServiceImpl implements DocumentoService {

    private static final Logger log = LoggerFactory.getLogger(DocumentoServiceImpl.class);

    private final DocumentoRepository documentoRepository;
    private final DocumentoVersionRepository versionRepository;
    private final DocumentoRelacionRepository relacionRepository;
    private final DocumentoVerificacionRepository verificacionRepository;
    private final DocumentoAccesoRepository accesoRepository;
    private final UsuarioRepository usuarioRepository;
    private final OficinaRepository oficinaRepository;
    private final DocumentStorageService storageService;
    private final DocumentFileValidator fileValidator;
    private final DocumentoAuditService auditService;
    private final PdfGeneratorService pdfGeneratorService;
    private final CentralNotificationService notificationService;

    public DocumentoServiceImpl(
            DocumentoRepository documentoRepository,
            DocumentoVersionRepository versionRepository,
            DocumentoRelacionRepository relacionRepository,
            DocumentoVerificacionRepository verificacionRepository,
            DocumentoAccesoRepository accesoRepository,
            UsuarioRepository usuarioRepository,
            OficinaRepository oficinaRepository,
            DocumentStorageService storageService,
            DocumentFileValidator fileValidator,
            DocumentoAuditService auditService,
            PdfGeneratorService pdfGeneratorService,
            CentralNotificationService notificationService
    ) {
        this.documentoRepository = documentoRepository;
        this.versionRepository = versionRepository;
        this.relacionRepository = relacionRepository;
        this.verificacionRepository = verificacionRepository;
        this.accesoRepository = accesoRepository;
        this.usuarioRepository = usuarioRepository;
        this.oficinaRepository = oficinaRepository;
        this.storageService = storageService;
        this.fileValidator = fileValidator;
        this.auditService = auditService;
        this.pdfGeneratorService = pdfGeneratorService;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public DocumentoResponse subirDocumento(MultipartFile file, SubirDocumentoRequest request, String username) {
        fileValidator.validate(file);

        Usuario usuario = resolveUsuario(username);
        Oficina oficina = resolveOficina(request != null ? request.getOficinaId() : null);

        String extension = fileValidator.extractExtension(file.getOriginalFilename());
        String mimeType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";
        byte[] fileBytes;
        try {
            fileBytes = file.getBytes();
        } catch (IOException e) {
            throw new BusinessException("Error al leer los bytes del archivo documental.");
        }

        String sha256 = Sha256Calculator.calculate(fileBytes);
        String nombreInterno = storageService.store(new ByteArrayInputStream(fileBytes), extension);

        CategoriaDocumento categoria = request != null && request.getCategoria() != null ? request.getCategoria() : CategoriaDocumento.OTRO;
        ClasificacionAcceso clasificacion = request != null && request.getClasificacionAcceso() != null ? request.getClasificacionAcceso() : ClasificacionAcceso.INTERNO;
        String moduloOrigen = request != null && request.getModuloOrigen() != null ? request.getModuloOrigen() : "SISTEMA";

        Documento documento = Documento.builder()
                .identificadorPublico(UUID.randomUUID())
                .nombreOriginal(file.getOriginalFilename())
                .nombreInterno(nombreInterno)
                .extension(extension)
                .mimeType(mimeType)
                .tamanoBytes((long) fileBytes.length)
                .hashSha256(sha256)
                .categoria(categoria)
                .clasificacionAcceso(clasificacion)
                .descripcion(request != null ? request.getDescripcion() : null)
                .estado(EstadoDocumento.ACTIVO)
                .versionActual(1)
                .bloqueoEliminacion(false)
                .usuarioPropietario(usuario)
                .oficinaResponsable(oficina)
                .moduloOrigen(moduloOrigen)
                .build();

        documento = documentoRepository.save(documento);

        // Registro de la versión 1 inicial
        DocumentoVersion version1 = DocumentoVersion.builder()
                .documento(documento)
                .numeroVersion(1)
                .nombreInterno(nombreInterno)
                .tamanoBytes((long) fileBytes.length)
                .mimeType(mimeType)
                .hashSha256(sha256)
                .motivoCambio("Carga inicial del documento")
                .usuarioResponsable(usuario)
                .build();
        versionRepository.save(version1);

        // Si se indicó asociación relacional inicial
        if (request != null && request.getTipoEntidad() != null && request.getEntidadId() != null) {
            DocumentoRelacion relacion = DocumentoRelacion.builder()
                    .documento(documento)
                    .modulo(moduloOrigen)
                    .tipoEntidad(request.getTipoEntidad())
                    .entidadId(request.getEntidadId())
                    .rolDocumento(request.getRolDocumento() != null ? request.getRolDocumento() : "PRINCIPAL")
                    .build();
            relacionRepository.save(relacion);
        }

        auditService.registrarAcceso(documento, usuario, AccionAuditoriaDocumento.SUBIDA, "EXITOSO");

        // Notificación interna preventiva (despachada de forma segura tras el commit exitoso)
        if (usuario != null) {
            Long usuarioId = usuario.getId();
            String nombreOriginal = documento.getNombreOriginal();
            UUID publicUuid = documento.getIdentificadorPublico();

            Runnable sendNotificationTask = () -> {
                try {
                    notificationService.enviarNotificacion(
                            EnviarNotificacionRequest.builder()
                                    .usuarioId(usuarioId)
                                    .titulo("Documento cargado con éxito")
                                    .mensaje("El archivo '" + nombreOriginal + "' fue registrado en el repositorio institucional.")
                                    .tipo("DOCUMENTO")
                                    .urlDestino("/dashboard/documentos?doc=" + publicUuid)
                                    .build(),
                            "SISTEMA"
                    );
                } catch (Exception e) {
                    log.warn("No se pudo enviar notificación de carga de documento: {}", e.getMessage());
                }
            };

            if (org.springframework.transaction.support.TransactionSynchronizationManager.isSynchronizationActive()) {
                org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(
                        new org.springframework.transaction.support.TransactionSynchronization() {
                            @Override
                            public void afterCommit() {
                                sendNotificationTask.run();
                            }
                        }
                );
            } else {
                sendNotificationTask.run();
            }
        }

        return DocumentoMapper.toResponse(documento);
    }

    @Override
    @Transactional
    public DocumentoResponse crearNuevaVersion(Long documentoId, MultipartFile file, NuevaVersionRequest request, String username) {
        Documento documento = obtenerDocumentoEntidad(documentoId);
        validarPermisoModificacion(documento, username);

        if (documento.getEstado() == EstadoDocumento.ELIMINADO_LOGICO) {
            throw new BusinessException("No es posible agregar versiones a un documento eliminado.");
        }

        fileValidator.validate(file);

        Usuario usuario = resolveUsuario(username);
        String extension = fileValidator.extractExtension(file.getOriginalFilename());
        String mimeType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";
        byte[] fileBytes;
        try {
            fileBytes = file.getBytes();
        } catch (IOException e) {
            throw new BusinessException("Error al leer los bytes del nuevo archivo de versión.");
        }

        String sha256 = Sha256Calculator.calculate(fileBytes);
        String nombreInterno = storageService.store(new ByteArrayInputStream(fileBytes), extension);

        int siguienteVersion = documento.getVersionActual() + 1;

        DocumentoVersion nuevaVersion = DocumentoVersion.builder()
                .documento(documento)
                .numeroVersion(siguienteVersion)
                .nombreInterno(nombreInterno)
                .tamanoBytes((long) fileBytes.length)
                .mimeType(mimeType)
                .hashSha256(sha256)
                .motivoCambio(request.getMotivoCambio())
                .usuarioResponsable(usuario)
                .build();
        versionRepository.save(nuevaVersion);

        // Actualizar cabecera del documento con la versión vigente
        documento.setVersionActual(siguienteVersion);
        documento.setNombreInterno(nombreInterno);
        documento.setNombreOriginal(file.getOriginalFilename());
        documento.setExtension(extension);
        documento.setMimeType(mimeType);
        documento.setTamanoBytes((long) fileBytes.length);
        documento.setHashSha256(sha256);
        documento = documentoRepository.save(documento);

        auditService.registrarAcceso(documento, usuario, AccionAuditoriaDocumento.NUEVA_VERSION, "EXITOSO");
        return DocumentoMapper.toResponse(documento);
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentoResponse obtenerPorId(Long documentoId, String username) {
        Documento documento = obtenerDocumentoEntidad(documentoId);
        validarPermisoLectura(documento, username);
        return DocumentoMapper.toResponse(documento);
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentoResponse obtenerPorUuid(UUID uuid, String username) {
        Documento documento = documentoRepository.findByIdentificadorPublico(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Documento no encontrado con UUID: " + uuid));
        validarPermisoLectura(documento, username);
        return DocumentoMapper.toResponse(documento);
    }

    @Override
    @Transactional
    public DocumentoResponse actualizarMetadatos(Long documentoId, ActualizarDocumentoRequest request, String username) {
        Documento documento = obtenerDocumentoEntidad(documentoId);
        validarPermisoModificacion(documento, username);

        if (request.getCategoria() != null) {
            documento.setCategoria(request.getCategoria());
        }
        if (request.getClasificacionAcceso() != null) {
            documento.setClasificacionAcceso(request.getClasificacionAcceso());
        }
        if (request.getDescripcion() != null) {
            documento.setDescripcion(request.getDescripcion());
        }

        documento = documentoRepository.save(documento);
        Usuario usuario = resolveUsuario(username);
        auditService.registrarAcceso(documento, usuario, AccionAuditoriaDocumento.CAMBIO_ESTADO, "METADATOS_ACTUALIZADOS");
        return DocumentoMapper.toResponse(documento);
    }

    @Override
    @Transactional
    public Resource descargarDocumento(Long documentoId, Integer versionOpcional, String username) {
        Documento documento = obtenerDocumentoEntidad(documentoId);
        validarPermisoLectura(documento, username);
        documento = asegurarArchivoFisicoYVersionInicial(documento);

        String nombreArchivoFisico = documento.getNombreInterno();
        if (versionOpcional != null && versionOpcional > 0 && versionOpcional != documento.getVersionActual()) {
            DocumentoVersion version = versionRepository.findByDocumentoIdAndNumeroVersion(documentoId, versionOpcional)
                    .orElseThrow(() -> new ResourceNotFoundException("La versión " + versionOpcional + " no existe para este documento."));
            if (storageService.exists(version.getNombreInterno())) {
                nombreArchivoFisico = version.getNombreInterno();
            }
        }

        byte[] bytes;
        try (InputStream inputStream = storageService.read(nombreArchivoFisico)) {
            bytes = inputStream.readAllBytes();
        } catch (IOException e) {
            throw new BusinessException("No se pudo leer el archivo físico del documento.");
        }

        Usuario usuario = resolveUsuario(username);
        auditService.registrarAcceso(documento, usuario, AccionAuditoriaDocumento.DESCARGA, "EXITOSO");

        return new ByteArrayResource(bytes);
    }

    @Override
    @Transactional
    public Resource vistaPreviaDocumento(Long documentoId, String username) {
        Documento documento = obtenerDocumentoEntidad(documentoId);
        validarPermisoLectura(documento, username);
        documento = asegurarArchivoFisicoYVersionInicial(documento);

        if (!documento.isVisualizable()) {
            throw new BusinessException("El formato del documento no admite vista previa directa. Por favor, utilice la opción de descarga.");
        }

        byte[] bytes;
        try (InputStream inputStream = storageService.read(documento.getNombreInterno())) {
            bytes = inputStream.readAllBytes();
        } catch (IOException e) {
            throw new BusinessException("No se pudo leer el archivo físico para vista previa.");
        }

        Usuario usuario = resolveUsuario(username);
        auditService.registrarAcceso(documento, usuario, AccionAuditoriaDocumento.VISTA_PREVIA, "EXITOSO");

        return new ByteArrayResource(bytes);
    }

    @Override
    @Transactional
    public VerificacionIntegridadResponse verificarIntegridad(Long documentoId, Integer versionOpcional, String username) {
        Documento documento = obtenerDocumentoEntidad(documentoId);
        validarPermisoLectura(documento, username);
        documento = asegurarArchivoFisicoYVersionInicial(documento);

        int versionEvaluar = versionOpcional != null && versionOpcional > 0 ? versionOpcional : documento.getVersionActual();
        String hashEsperado;
        String nombreFisico;

        if (versionEvaluar == documento.getVersionActual()) {
            hashEsperado = documento.getHashSha256();
            nombreFisico = documento.getNombreInterno();
        } else {
            DocumentoVersion v = versionRepository.findByDocumentoIdAndNumeroVersion(documentoId, versionEvaluar)
                    .orElseThrow(() -> new ResourceNotFoundException("La versión " + versionEvaluar + " no existe."));
            hashEsperado = v.getHashSha256();
            nombreFisico = v.getNombreInterno();
        }

        ResultadoVerificacion resultado;
        String hashCalculado = "";
        String detalle;

        if (!storageService.exists(nombreFisico)) {
            resultado = ResultadoVerificacion.NO_ENCONTRADO;
            detalle = "El archivo físico no fue localizado en el repositorio de almacenamiento seguro.";
        } else {
            try (InputStream is = storageService.read(nombreFisico)) {
                hashCalculado = Sha256Calculator.calculate(is);
                if (hashEsperado.equalsIgnoreCase(hashCalculado)) {
                    resultado = ResultadoVerificacion.INTEGRO;
                    detalle = "La huella criptográfica SHA-256 coincide exactamente. El documento no ha sufrido alteraciones.";
                } else {
                    resultado = ResultadoVerificacion.ALTERADO;
                    detalle = "DISCREPANCIA DETECTADA: La huella calculada difiere de la huella registrada. El archivo ha sido modificado o está corrupto.";
                }
            } catch (Exception e) {
                resultado = ResultadoVerificacion.NO_ENCONTRADO;
                detalle = "Error al leer el archivo para cálculo de hash: " + e.getMessage();
            }
        }

        Usuario usuario = resolveUsuario(username);
        DocumentoVerificacion verif = DocumentoVerificacion.builder()
                .documento(documento)
                .versionEvaluada(versionEvaluar)
                .hashEsperado(hashEsperado)
                .hashCalculado(hashCalculado)
                .resultado(resultado)
                .verificadoPor(usuario)
                .detalle(detalle)
                .build();
        verif = verificacionRepository.save(verif);

        auditService.registrarAcceso(documento, usuario, AccionAuditoriaDocumento.VERIFICACION_INTEGRIDAD, resultado.name());
        return DocumentoMapper.toVerificacionResponse(verif);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DocumentoResponse> listarDocumentos(
            EstadoDocumento estado,
            CategoriaDocumento categoria,
            ClasificacionAcceso clasificacion,
            String termino,
            Pageable pageable,
            String username
    ) {
        Usuario usuario = resolveUsuario(username);
        Long usuarioId = null;
        boolean isAdmin = usuario != null && usuario.getRoles().stream()
                .anyMatch(r -> r.getCodigo().equals("ROLE_ADMIN") || r.getCodigo().equals("ROLE_SUPERADMIN"));

        if (!isAdmin && usuario != null) {
            usuarioId = usuario.getId();
        }

        String searchPattern = (termino != null && !termino.trim().isEmpty())
                ? "%" + termino.trim().toLowerCase() + "%"
                : null;

        Page<Documento> docs = documentoRepository.buscarConFiltros(estado, categoria, clasificacion, searchPattern, usuarioId, pageable);
        return docs.map(DocumentoMapper::toResponse);
    }

    @Override
    @Transactional
    public DocumentoResponse archivarDocumento(Long documentoId, EliminarDocumentoRequest request, String username) {
        Documento documento = obtenerDocumentoEntidad(documentoId);
        validarPermisoModificacion(documento, username);

        documento.setEstado(EstadoDocumento.ARCHIVADO);
        documento.setMotivoEliminacion(request.getMotivo());
        documento = documentoRepository.save(documento);

        Usuario usuario = resolveUsuario(username);
        auditService.registrarAcceso(documento, usuario, AccionAuditoriaDocumento.CAMBIO_ESTADO, "ARCHIVADO");
        return DocumentoMapper.toResponse(documento);
    }

    @Override
    @Transactional
    public DocumentoResponse eliminarLogicamente(Long documentoId, EliminarDocumentoRequest request, String username) {
        Documento documento = obtenerDocumentoEntidad(documentoId);
        validarPermisoModificacion(documento, username);

        if (Boolean.TRUE.equals(documento.getBloqueoEliminacion())) {
            throw new BusinessException(
                    "El documento cuenta con bloqueo institucional de conservación activo. " +
                    "No puede eliminarse mientras rijan las obligaciones legales de retención documental."
            );
        }

        documento.setEstado(EstadoDocumento.ELIMINADO_LOGICO);
        documento.setFechaEliminacionLogica(OffsetDateTime.now());
        documento.setMotivoEliminacion(request.getMotivo());
        documento = documentoRepository.save(documento);

        Usuario usuario = resolveUsuario(username);
        auditService.registrarAcceso(documento, usuario, AccionAuditoriaDocumento.ELIMINACION_LOGICA, "ELIMINADO_LOGICO");
        return DocumentoMapper.toResponse(documento);
    }

    @Override
    @Transactional
    public DocumentoResponse cambiarBloqueoConservacion(Long documentoId, boolean bloqueo, String username) {
        Documento documento = obtenerDocumentoEntidad(documentoId);
        Usuario usuario = resolveUsuario(username);
        boolean isAdmin = usuario != null && usuario.getRoles().stream()
                .anyMatch(r -> r.getCodigo().equals("ROLE_ADMIN") || r.getCodigo().equals("ROLE_SUPERADMIN"));

        if (!isAdmin) {
            throw new AccessDeniedException("Únicamente los administradores pueden modificar el bloqueo de conservación documental.");
        }

        documento.setBloqueoEliminacion(bloqueo);
        documento = documentoRepository.save(documento);

        auditService.registrarAcceso(documento, usuario, AccionAuditoriaDocumento.CAMBIO_ESTADO, "BLOQUEO_CONSERVACION_" + bloqueo);
        return DocumentoMapper.toResponse(documento);
    }

    @Override
    @Transactional
    public List<DocumentoVersionResponse> listarVersiones(Long documentoId, String username) {
        Documento documento = obtenerDocumentoEntidad(documentoId);
        validarPermisoLectura(documento, username);
        documento = asegurarArchivoFisicoYVersionInicial(documento);
        return versionRepository.findByDocumentoIdOrderByNumeroVersionDesc(documentoId).stream()
                .map(DocumentoMapper::toVersionResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentoAuditoriaResponse> listarAuditoria(Long documentoId, String username) {
        Documento documento = obtenerDocumentoEntidad(documentoId);
        validarPermisoLectura(documento, username);
        return accesoRepository.findByDocumentoIdOrderByFechaAccesoDesc(documentoId).stream()
                .map(DocumentoMapper::toAuditoriaResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentoEstadisticasResponse obtenerEstadisticas() {
        long activos = documentoRepository.countActivos();
        long bytes = documentoRepository.sumTamanoBytesActivos();
        return DocumentoEstadisticasResponse.builder()
                .totalActivos(activos)
                .espacioUtilizadoBytes(bytes)
                .espacioUtilizadoFormateado(DocumentDtos.formatBytes(bytes))
                .build();
    }

    @Override
    @Transactional
    public void asociarDocumento(Long documentoId, String modulo, String tipoEntidad, String entidadId, String rol) {
        Documento documento = obtenerDocumentoEntidad(documentoId);
        DocumentoRelacion relacion = DocumentoRelacion.builder()
                .documento(documento)
                .modulo(modulo != null ? modulo : "SISTEMA")
                .tipoEntidad(tipoEntidad)
                .entidadId(entidadId)
                .rolDocumento(rol != null ? rol : "PRINCIPAL")
                .build();
        relacionRepository.save(relacion);
    }

    @Override
    @Transactional
    public DocumentoResponse generarYGuardarPdf(GenerarPdfRequest request, String username) {
        Usuario usuario = resolveUsuario(username);
        UUID publicUuid = UUID.randomUUID();
        String verificacionUrl = "https://muniquinua.gob.pe/verificar/" + publicUuid;

        byte[] pdfBytes = pdfGeneratorService.generarDocumentoPdf(request, verificacionUrl);
        String sha256 = Sha256Calculator.calculate(pdfBytes);
        String nombreInterno = storageService.store(new ByteArrayInputStream(pdfBytes), "pdf");

        String nombreOriginal = (request.getCodigoDocumental() != null ? request.getCodigoDocumental() : "DOCUMENTO") + ".pdf";

        CategoriaDocumento cat = CategoriaDocumento.CONSTANCIA;
        if ("COMPROBANTE".equalsIgnoreCase(request.getTipoPlantilla())) {
            cat = CategoriaDocumento.COMPROBANTE;
        } else if ("OFICIO".equalsIgnoreCase(request.getTipoPlantilla())) {
            cat = CategoriaDocumento.OFICIO;
        } else if ("INFORME".equalsIgnoreCase(request.getTipoPlantilla())) {
            cat = CategoriaDocumento.INFORME;
        }

        Documento documento = Documento.builder()
                .identificadorPublico(publicUuid)
                .nombreOriginal(nombreOriginal)
                .nombreInterno(nombreInterno)
                .extension("pdf")
                .mimeType("application/pdf")
                .tamanoBytes((long) pdfBytes.length)
                .hashSha256(sha256)
                .categoria(cat)
                .clasificacionAcceso(ClasificacionAcceso.INTERNO)
                .descripcion(request.getTitulo() + " - " + request.getAsunto())
                .estado(EstadoDocumento.ACTIVO)
                .versionActual(1)
                .bloqueoEliminacion(true) // Documentos oficiales nacen con política de conservación
                .usuarioPropietario(usuario)
                .moduloOrigen("SISTEMA")
                .build();

        documento = documentoRepository.save(documento);

        DocumentoVersion version1 = DocumentoVersion.builder()
                .documento(documento)
                .numeroVersion(1)
                .nombreInterno(nombreInterno)
                .tamanoBytes((long) pdfBytes.length)
                .mimeType("application/pdf")
                .hashSha256(sha256)
                .motivoCambio("Generación inicial de PDF oficial del sistema")
                .usuarioResponsable(usuario)
                .build();
        versionRepository.save(version1);

        auditService.registrarAcceso(documento, usuario, AccionAuditoriaDocumento.SUBIDA, "PDF_GENERADO");
        return DocumentoMapper.toResponse(documento);
    }

    private Documento asegurarArchivoFisicoYVersionInicial(Documento documento) {
        boolean modificado = false;
        if ((documento.getNombreInterno() == null || !storageService.exists(documento.getNombreInterno()))
                && ("pdf".equalsIgnoreCase(documento.getExtension()) || "application/pdf".equalsIgnoreCase(documento.getMimeType()))) {
            String codigoDoc = documento.getNombreOriginal() != null
                    ? documento.getNombreOriginal().replace(".pdf", "").replace(".PDF", "")
                    : "DOC-" + documento.getId();
            GenerarPdfRequest req = GenerarPdfRequest.builder()
                    .tipoPlantilla(documento.getCategoria() != null ? documento.getCategoria().name() : "COMPROBANTE")
                    .codigoDocumental(codigoDoc)
                    .titulo(documento.getCategoria() != null ? "DOCUMENTO OFICIAL - " + documento.getCategoria().name() : "DOCUMENTO OFICIAL MUNICIPAL")
                    .destinatario("Administrado / Contribuyente Municipal")
                    .asunto(documento.getDescripcion() != null ? documento.getDescripcion() : codigoDoc)
                    .cuerpo("Documento electrónico oficial custodiado en el repositorio institucional de la Municipalidad Distrital de Quinua.\n\n"
                            + "Referencia: " + (documento.getDescripcion() != null ? documento.getDescripcion() : documento.getNombreOriginal()) + "\n"
                            + "Identificador Público UUID: " + documento.getIdentificadorPublico())
                    .build();
            String verificacionUrl = "https://muniquinua.gob.pe/verificar/" + documento.getIdentificadorPublico();
            byte[] pdfBytes = pdfGeneratorService.generarDocumentoPdf(req, verificacionUrl);
            String sha256 = Sha256Calculator.calculate(pdfBytes);
            String nombreInterno = storageService.store(new ByteArrayInputStream(pdfBytes), "pdf");

            documento.setNombreInterno(nombreInterno);
            documento.setTamanoBytes((long) pdfBytes.length);
            documento.setHashSha256(sha256);
            documento = documentoRepository.save(documento);
            modificado = true;
        }

        List<DocumentoVersion> versiones = versionRepository.findByDocumentoIdOrderByNumeroVersionDesc(documento.getId());
        if (versiones.isEmpty()) {
            DocumentoVersion version1 = DocumentoVersion.builder()
                    .documento(documento)
                    .numeroVersion(documento.getVersionActual() != null ? documento.getVersionActual() : 1)
                    .nombreInterno(documento.getNombreInterno())
                    .tamanoBytes(documento.getTamanoBytes())
                    .mimeType(documento.getMimeType())
                    .hashSha256(documento.getHashSha256())
                    .motivoCambio("Custodia inicial del documento en el repositorio municipal")
                    .usuarioResponsable(documento.getUsuarioPropietario())
                    .build();
            versionRepository.save(version1);
        } else if (modificado) {
            DocumentoVersion ultima = versiones.get(0);
            ultima.setNombreInterno(documento.getNombreInterno());
            ultima.setTamanoBytes(documento.getTamanoBytes());
            ultima.setHashSha256(documento.getHashSha256());
            versionRepository.save(ultima);
        }

        return documento;
    }

    private Documento obtenerDocumentoEntidad(Long id) {
        return documentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Documento no encontrado con ID: " + id));
    }

    private Usuario resolveUsuario(String username) {
        if (username == null || username.isBlank() || "anonymousUser".equalsIgnoreCase(username)) {
            return null;
        }
        return usuarioRepository.findByUsername(username).orElse(null);
    }

    private Oficina resolveOficina(Long oficinaId) {
        if (oficinaId == null) return null;
        return oficinaRepository.findById(oficinaId).orElse(null);
    }

    private void validarPermisoLectura(Documento doc, String username) {
        if (doc.getClasificacionAcceso() == ClasificacionAcceso.PUBLICO) {
            return;
        }

        Usuario usuario = resolveUsuario(username);
        if (usuario == null) {
            throw new AccessDeniedException("Debe autenticarse para consultar documentos no públicos.");
        }

        boolean isAdmin = usuario.getRoles().stream()
                .anyMatch(r -> r.getCodigo().equals("ROLE_ADMIN") || r.getCodigo().equals("ROLE_SUPERADMIN"));
        if (isAdmin) {
            return;
        }

        // Propietario tiene acceso
        if (doc.getUsuarioPropietario() != null && doc.getUsuarioPropietario().getId().equals(usuario.getId())) {
            return;
        }

        // Si es confidencial o restringido y no es el dueño ni admin
        if (doc.getClasificacionAcceso() == ClasificacionAcceso.CONFIDENCIAL
                || doc.getClasificacionAcceso() == ClasificacionAcceso.RESTRINGIDO) {
            throw new AccessDeniedException("No cuenta con autorización para acceder a este documento confidencial/restringido.");
        }
    }

    private void validarPermisoModificacion(Documento doc, String username) {
        Usuario usuario = resolveUsuario(username);
        if (usuario == null) {
            throw new AccessDeniedException("Debe autenticarse para modificar documentos.");
        }

        boolean isAdmin = usuario.getRoles().stream()
                .anyMatch(r -> r.getCodigo().equals("ROLE_ADMIN") || r.getCodigo().equals("ROLE_SUPERADMIN"));
        if (isAdmin) {
            return;
        }

        if (doc.getUsuarioPropietario() != null && doc.getUsuarioPropietario().getId().equals(usuario.getId())) {
            return;
        }

        throw new AccessDeniedException("No tiene permisos para modificar o versionar este documento institucional.");
    }
}
