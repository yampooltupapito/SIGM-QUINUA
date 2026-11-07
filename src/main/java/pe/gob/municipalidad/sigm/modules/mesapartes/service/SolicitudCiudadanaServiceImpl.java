package pe.gob.municipalidad.sigm.modules.mesapartes.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pe.gob.municipalidad.sigm.common.document.dto.DocumentDtos.DocumentoResponse;
import pe.gob.municipalidad.sigm.common.document.dto.DocumentDtos.SubirDocumentoRequest;
import pe.gob.municipalidad.sigm.common.document.entity.CategoriaDocumento;
import pe.gob.municipalidad.sigm.common.document.entity.ClasificacionAcceso;
import pe.gob.municipalidad.sigm.common.document.entity.Documento;
import pe.gob.municipalidad.sigm.common.document.repository.DocumentoRepository;
import pe.gob.municipalidad.sigm.common.document.service.DocumentoService;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.common.model.Persona;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.PersonaRepository;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvDtos.ExpedienteDetalleResponse;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvDtos.ExpedienteResponse;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvSolicitudDtos.GuardarBorradorSolicitudRequest;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvSolicitudDtos.MisSolicitudesItemResponse;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvSolicitudDtos.PresentarSolicitudRequest;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.CanalIngreso;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.ConfidencialidadExpediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoExpediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.PrioridadExpediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.TipoRelacionDocumentoExpediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.mapper.MpvMapper;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.workflow.ExpedienteWorkflowService;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class SolicitudCiudadanaServiceImpl implements SolicitudCiudadanaService {

    private static final ZoneId ZONA_LIMA = ZoneId.of("America/Lima");
    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final ExpedienteRepository expedienteRepository;
    private final TipoTramiteRepository tipoTramiteRepository;
    private final UsuarioRepository usuarioRepository;
    private final PersonaRepository personaRepository;
    private final DocumentoRepository documentoRepository;
    private final DocumentoService documentoService;
    private final ExpedienteDocumentoRepository expedienteDocumentoRepository;
    private final FormularioDinamicoService formularioDinamicoService;
    private final RequisitoDocumentalRepository requisitoRepository;
    private final PresentacionIdempotenciaRepository idempotenciaRepository;
    private final NumeracionExpedienteService numeracionService;
    private final ExpedienteWorkflowService workflowService;
    private final PlazoAdministrativoService plazoService;
    private final ConstanciaPresentacionService constanciaService;
    private final CodigoSeguimientoService codigoSeguimientoService;
    private final MpvMapper mpvMapper;

    @Value("${sigm.app.base-url:http://localhost:8080}")
    private String defaultBaseUrl;

    public SolicitudCiudadanaServiceImpl(ExpedienteRepository expedienteRepository,
                                         TipoTramiteRepository tipoTramiteRepository,
                                         UsuarioRepository usuarioRepository,
                                         PersonaRepository personaRepository,
                                         DocumentoRepository documentoRepository,
                                         DocumentoService documentoService,
                                         ExpedienteDocumentoRepository expedienteDocumentoRepository,
                                         FormularioDinamicoService formularioDinamicoService,
                                         RequisitoDocumentalRepository requisitoRepository,
                                         PresentacionIdempotenciaRepository idempotenciaRepository,
                                         NumeracionExpedienteService numeracionService,
                                         ExpedienteWorkflowService workflowService,
                                         PlazoAdministrativoService plazoService,
                                         ConstanciaPresentacionService constanciaService,
                                         CodigoSeguimientoService codigoSeguimientoService,
                                         MpvMapper mpvMapper) {
        this.expedienteRepository = expedienteRepository;
        this.tipoTramiteRepository = tipoTramiteRepository;
        this.usuarioRepository = usuarioRepository;
        this.personaRepository = personaRepository;
        this.documentoRepository = documentoRepository;
        this.documentoService = documentoService;
        this.expedienteDocumentoRepository = expedienteDocumentoRepository;
        this.formularioDinamicoService = formularioDinamicoService;
        this.requisitoRepository = requisitoRepository;
        this.idempotenciaRepository = idempotenciaRepository;
        this.numeracionService = numeracionService;
        this.workflowService = workflowService;
        this.plazoService = plazoService;
        this.constanciaService = constanciaService;
        this.codigoSeguimientoService = codigoSeguimientoService;
        this.mpvMapper = mpvMapper;
    }

    @Override
    @Transactional
    public ExpedienteDetalleResponse guardarBorrador(UUID expedienteUuid, GuardarBorradorSolicitudRequest request, String username) {
        Usuario usuario = obtenerUsuario(username);
        Persona solicitante = usuario.getPersona();
        if (solicitante == null) {
            throw new BusinessException("El usuario autenticado no cuenta con una ficha de persona registrada en el sistema.");
        }

        TipoTramite tramite = tipoTramiteRepository.findById(request.getTipoTramiteId())
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de trámite no encontrado con ID: " + request.getTipoTramiteId()));

        if (!Boolean.TRUE.equals(tramite.getPermitePresentacionVirtual())) {
            throw new BusinessException("El tipo de trámite seleccionado no admite presentación virtual por Mesa de Partes.");
        }

        Expediente expediente;
        if (expedienteUuid != null) {
            expediente = expedienteRepository.findByUuidWithDetalles(expedienteUuid)
                    .orElseThrow(() -> new ResourceNotFoundException("Borrador no encontrado con identificador: " + expedienteUuid));

            validarAccesoPropietario(expediente, usuario);

            if (expediente.getEstadoActual() != EstadoExpediente.BORRADOR) {
                throw new BusinessException("No es posible modificar el expediente porque ya no se encuentra en estado BORRADOR (Estado actual: " + expediente.getEstadoActual() + ").");
            }

            expediente.setTipoTramite(tramite);
            expediente.setAsunto(request.getAsunto());
            expediente.setDescripcion(request.getDescripcion());
            expediente.setOficinaResponsable(tramite.getOficinaResponsable());
            expediente.setUpdatedBy(username);
        } else {
            expediente = Expediente.builder()
                    .uuid(UUID.randomUUID())
                    .anio(LocalDate.now(ZONA_LIMA).getYear())
                    .asunto(request.getAsunto())
                    .descripcion(request.getDescripcion())
                    .tipoTramite(tramite)
                    .solicitante(solicitante)
                    .oficinaResponsable(tramite.getOficinaResponsable())
                    .estadoActual(EstadoExpediente.BORRADOR)
                    .canalIngreso(CanalIngreso.VIRTUAL)
                    .prioridad(PrioridadExpediente.NORMAL)
                    .confidencialidad(ConfidencialidadExpediente.PUBLICO)
                    .usuarioRegistro(usuario)
                    .createdBy(username)
                    .build();
        }

        expediente = expedienteRepository.save(expediente);

        if (request.getValoresFormulario() != null && !request.getValoresFormulario().isEmpty()) {
            formularioDinamicoService.guardarRespuestas(expediente, request.getValoresFormulario(), false);
        }

        return mpvMapper.toExpedienteDetalleResponse(expediente);
    }

    @Override
    @Transactional(readOnly = true)
    public ExpedienteDetalleResponse obtenerBorrador(UUID expedienteUuid, String username) {
        Usuario usuario = obtenerUsuario(username);
        Expediente expediente = expedienteRepository.findByUuidWithDetalles(expedienteUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Expediente no encontrado con identificador: " + expedienteUuid));

        validarAccesoPropietario(expediente, usuario);
        return mpvMapper.toExpedienteDetalleResponse(expediente);
    }

    @Override
    @Transactional
    public ExpedienteDetalleResponse subirDocumentoBorrador(UUID expedienteUuid, MultipartFile archivo,
                                                            TipoRelacionDocumentoExpediente tipoRelacion, String username) {
        Usuario usuario = obtenerUsuario(username);
        Expediente expediente = expedienteRepository.findByUuidWithDetalles(expedienteUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Expediente no encontrado con identificador: " + expedienteUuid));

        validarAccesoPropietario(expediente, usuario);

        if (expediente.getEstadoActual() != EstadoExpediente.BORRADOR) {
            throw new BusinessException("Solo se pueden adjuntar documentos en estado BORRADOR o mediante requerimiento de subsanación.");
        }

        if (archivo == null || archivo.isEmpty()) {
            throw new BusinessException("Debe seleccionar un archivo para adjuntar.");
        }

        SubirDocumentoRequest subReq = SubirDocumentoRequest.builder()
                .categoria(CategoriaDocumento.SOLICITUD)
                .clasificacionAcceso(ClasificacionAcceso.PUBLICO)
                .descripcion("Documento adjunto a solicitud virtual " + expediente.getUuid())
                .moduloOrigen("MESA_PARTES")
                .tipoEntidad("EXPEDIENTE")
                .entidadId(expediente.getId().toString())
                .rolDocumento(tipoRelacion != null ? tipoRelacion.name() : "ANEXO")
                .build();

        DocumentoResponse docResp = documentoService.subirDocumento(archivo, subReq, username);

        Documento doc = documentoRepository.findById(docResp.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Error al recuperar el documento recién almacenado."));

        ExpedienteDocumento expDoc = ExpedienteDocumento.builder()
                .expediente(expediente)
                .documento(doc)
                .tipoRelacion(tipoRelacion != null ? tipoRelacion : TipoRelacionDocumentoExpediente.ANEXO)
                .usuarioAsocia(usuario)
                .fechaAsociacion(OffsetDateTime.now(ZONA_LIMA))
                .orden((int) (expedienteDocumentoRepository.countByExpedienteId(expediente.getId()) + 1))
                .build();

        expDoc = expedienteDocumentoRepository.save(expDoc);
        if (expediente.getDocumentos() != null) {
            expediente.getDocumentos().add(expDoc);
        } else {
            expediente.setDocumentos(new ArrayList<>(List.of(expDoc)));
        }

        return mpvMapper.toExpedienteDetalleResponse(expediente);
    }

    @Override
    @Transactional
    public void eliminarDocumentoBorrador(UUID expedienteUuid, UUID documentoUuid, String username) {
        Usuario usuario = obtenerUsuario(username);
        Expediente expediente = expedienteRepository.findByUuidWithDetalles(expedienteUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Expediente no encontrado con identificador: " + expedienteUuid));

        validarAccesoPropietario(expediente, usuario);

        if (expediente.getEstadoActual() != EstadoExpediente.BORRADOR) {
            throw new BusinessException("Solo se pueden desvincular documentos cuando el expediente está en estado BORRADOR.");
        }

        Documento doc = documentoRepository.findByIdentificadorPublico(documentoUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Documento no encontrado con UUID: " + documentoUuid));

        ExpedienteDocumento expDoc = expedienteDocumentoRepository.findByExpedienteIdAndDocumentoId(expediente.getId(), doc.getId())
                .orElseThrow(() -> new ResourceNotFoundException("El documento no está asociado a este expediente."));

        expedienteDocumentoRepository.delete(expDoc);
        if (expediente.getDocumentos() != null) {
            expediente.getDocumentos().remove(expDoc);
        }
        log.info("Documento {} desvinculado del expediente {}", documentoUuid, expedienteUuid);
    }

    @Override
    @Transactional
    public ExpedienteResponse presentarSolicitud(UUID expedienteUuid, PresentarSolicitudRequest request, String username) {
        Usuario usuario = obtenerUsuario(username);
        Expediente expediente = expedienteRepository.findByUuidWithDetalles(expedienteUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Expediente no encontrado con identificador: " + expedienteUuid));

        validarAccesoPropietario(expediente, usuario);

        // Control de Idempotencia estricto
        String claveIdem = request != null && request.getClaveIdempotencia() != null && !request.getClaveIdempotencia().isBlank()
                ? request.getClaveIdempotencia().trim()
                : null;

        if (claveIdem != null) {
            Optional<PresentacionIdempotencia> idemOpt = idempotenciaRepository.findByClaveIdempotencia(claveIdem);
            if (idemOpt.isPresent()) {
                log.warn("Presentación idempotente detectada para clave '{}'. Retornando expediente ya confirmado.", claveIdem);
                return mpvMapper.toExpedienteResponse(idemOpt.get().getExpediente());
            }
        }

        if (expediente.getEstadoActual() != EstadoExpediente.BORRADOR) {
            throw new BusinessException("El expediente ya fue presentado formalmente con número: " + expediente.getNumeroExpediente());
        }

        if (request != null && Boolean.FALSE.equals(request.getDeclaracionVeracidad())) {
            throw new BusinessException("Debe aceptar la declaración jurada de veracidad de la información para presentar la solicitud.");
        }

        if (expediente.getAsunto() == null || expediente.getAsunto().isBlank()) {
            throw new BusinessException("El asunto de la solicitud es obligatorio.");
        }

        // Validación de respuestas dinámicas
        FormularioVersion version = formularioDinamicoService.obtenerVersionActivaPorTramite(expediente.getTipoTramite().getId());
        if (version != null) {
            java.util.Map<String, String> respuestas = formularioDinamicoService.obtenerMapaRespuestas(expediente.getId());
            // Validar que los campos obligatorios del formulario estén completos
            formularioDinamicoService.validarRespuestas(version, respuestas, true);
        }

        // Validación de requisitos documentales obligatorios
        long obligatorios = requisitoRepository.countByTipoTramiteIdAndObligatorioTrueAndEstado(expediente.getTipoTramite().getId(), "ACTIVO");
        long adjuntos = expedienteDocumentoRepository.countByExpedienteId(expediente.getId());
        if (obligatorios > 0 && adjuntos == 0) {
            throw new BusinessException("El tipo de trámite seleccionado requiere que adjunte los documentos obligatorios exigidos.");
        }

        // Generar número oficial de expediente
        int anio = LocalDate.now(ZONA_LIMA).getYear();
        String numeroGenerado = numeracionService.generarSiguienteNumero("MPV", anio);
        expediente.setNumeroExpediente(numeroGenerado);
        expediente.setAnio(anio);
        expediente.setFechaPresentacion(OffsetDateTime.now(ZONA_LIMA));

        // Calcular fecha límite de atención administrativa
        if (expediente.getTipoTramite().getPlazoDias() != null && expediente.getTipoTramite().getPlazoDias() > 0) {
            OffsetDateTime fechaLimite = plazoService.calcularFechaLimite(
                    expediente.getFechaPresentacion(),
                    expediente.getTipoTramite().getPlazoDias(),
                    expediente.getTipoTramite().getUnidadPlazo()
            );
            expediente.setFechaLimite(fechaLimite);
        }

        // Transicionar a PRESENTADO
        workflowService.transicionar(
                expediente,
                EstadoExpediente.PRESENTADO,
                "PRESENTACION_VIRTUAL",
                "Solicitud ciudadana presentada formalmente a través de la Mesa de Partes Virtual.",
                usuario,
                expediente.getOficinaResponsable()
        );

        expediente = expedienteRepository.save(expediente);
        codigoSeguimientoService.generarOObtenerCodigo(expediente);

        // Guardar registro de idempotencia si se proporcionó clave
        if (claveIdem != null) {
            PresentacionIdempotencia pi = PresentacionIdempotencia.builder()
                    .claveIdempotencia(claveIdem)
                    .expediente(expediente)
                    .usuario(usuario)
                    .fechaPresentacion(expediente.getFechaPresentacion())
                    .numeroExpediente(expediente.getNumeroExpediente())
                    .build();
            idempotenciaRepository.save(pi);
        }

        log.info("Expediente presentado formalmente con código: {} por usuario: {}", numeroGenerado, username);
        return mpvMapper.toExpedienteResponse(expediente);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MisSolicitudesItemResponse> listarMisSolicitudes(EstadoExpediente estado, String termino, Pageable pageable, String username) {
        Usuario usuario = obtenerUsuario(username);
        Persona solicitante = usuario.getPersona();
        if (solicitante == null) {
            return Page.empty(pageable);
        }

        String term = (termino != null && !termino.isBlank()) ? "%" + termino.trim().toLowerCase() + "%" : null;

        Page<Expediente> expedientes = expedienteRepository.buscarConFiltros(
                estado,
                null,
                null,
                solicitante.getId(),
                null,
                term,
                pageable
        );

        return expedientes.map(e -> {
            long totalDocs = expedienteDocumentoRepository.countByExpedienteId(e.getId());
            int etapa = pe.gob.municipalidad.sigm.modules.mesapartes.service.impl.ConsultaPublicaExpedienteServiceImpl.calcularNumeroEtapa(e.getEstadoActual());
            String descCiudadana = pe.gob.municipalidad.sigm.modules.mesapartes.service.impl.ConsultaPublicaExpedienteServiceImpl.traducirEstadoCiudadano(e.getEstadoActual());
            return MisSolicitudesItemResponse.builder()
                    .id(e.getId())
                    .uuid(e.getUuid())
                    .numeroExpediente(e.getNumeroExpediente() != null ? e.getNumeroExpediente() : "BORRADOR")
                    .codigoSeguimiento(e.getCodigoSeguimiento())
                    .progresoEtapa(etapa)
                    .estadoDescripcionCiudadana(descCiudadana)
                    .asunto(e.getAsunto())
                    .tipoTramiteNombre(e.getTipoTramite() != null ? e.getTipoTramite().getNombre() : "")
                    .fechaRegistro(e.getCreatedAt() != null ? e.getCreatedAt().atZoneSameInstant(ZONA_LIMA).format(FORMATO_FECHA_HORA) : "")
                    .fechaPresentacion(e.getFechaPresentacion() != null ? e.getFechaPresentacion().atZoneSameInstant(ZONA_LIMA).format(FORMATO_FECHA_HORA) : "-")
                    .estado(e.getEstadoActual().name())
                    .oficinaResponsableNombre(e.getOficinaResponsable() != null ? e.getOficinaResponsable().getNombre() : "")
                    .totalDocumentos((int) totalDocs)
                    .esBorrador(e.getEstadoActual() == EstadoExpediente.BORRADOR)
                    .tieneObservaciones(e.getEstadoActual() == EstadoExpediente.OBSERVADO)
                    .resultadoFinal(e.getResultadoFinal())
                    .esResuelto(e.getEstadoActual() == EstadoExpediente.RESUELTO || e.getEstadoActual() == EstadoExpediente.NOTIFICADO || e.getEstadoActual() == EstadoExpediente.ARCHIVADO)
                    .build();
        });
    }

    @Override
    @Transactional(readOnly = true)
    public ExpedienteDetalleResponse obtenerMiExpediente(UUID expedienteUuid, String username) {
        Usuario usuario = obtenerUsuario(username);
        Expediente expediente = expedienteRepository.findByUuidWithDetalles(expedienteUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Expediente no encontrado con identificador: " + expedienteUuid));

        validarAccesoPropietario(expediente, usuario);
        return mpvMapper.toExpedienteDetalleResponse(expediente);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] descargarConstanciaPdf(UUID expedienteUuid, String baseUrl, String username) {
        Usuario usuario = obtenerUsuario(username);
        Expediente expediente = expedienteRepository.findByUuidWithDetalles(expedienteUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Expediente no encontrado con identificador: " + expedienteUuid));

        validarAccesoPropietario(expediente, usuario);

        if (expediente.getEstadoActual() == EstadoExpediente.BORRADOR) {
            throw new BusinessException("No se puede generar la constancia de presentación para una solicitud en estado BORRADOR.");
        }

        return constanciaService.generarConstanciaPdf(expediente, baseUrl != null ? baseUrl : defaultBaseUrl);
    }

    private void validarAccesoPropietario(Expediente expediente, Usuario usuario) {
        boolean esAdmin = usuario.getRoles().stream()
                .anyMatch(r -> "ROLE_ADMIN".equals(r.getCodigo()) || "ROLE_SUPERADMIN".equals(r.getCodigo()) || "ROLE_MESA_PARTES".equals(r.getCodigo()));

        if (!esAdmin) {
            if (usuario.getPersona() == null || !expediente.getSolicitante().getId().equals(usuario.getPersona().getId())) {
                throw new AccessDeniedException("Acceso denegado: No tiene autorización para consultar o gestionar un expediente ajeno.");
            }
        }
    }

    private Usuario obtenerUsuario(String username) {
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con username: " + username));
    }
}
