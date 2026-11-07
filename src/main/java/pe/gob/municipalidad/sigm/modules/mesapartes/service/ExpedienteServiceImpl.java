package pe.gob.municipalidad.sigm.modules.mesapartes.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.document.entity.Documento;
import pe.gob.municipalidad.sigm.common.document.repository.DocumentoRepository;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.common.model.Persona;
import pe.gob.municipalidad.sigm.common.model.Rol;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.OficinaRepository;
import pe.gob.municipalidad.sigm.common.repository.PersonaRepository;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvDtos.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.mapper.MpvMapper;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.workflow.ExpedienteWorkflowService;
import pe.gob.municipalidad.sigm.security.authorization.OfficeSecurityService;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExpedienteServiceImpl implements ExpedienteService {

    private static final Logger log = LoggerFactory.getLogger(ExpedienteServiceImpl.class);

    private final ExpedienteRepository expedienteRepository;
    private final TipoTramiteRepository tipoTramiteRepository;
    private final ExpedienteHistorialRepository historialRepository;
    private final ExpedienteDocumentoRepository expedienteDocumentoRepository;
    private final DerivacionExpedienteRepository derivacionRepository;
    private final ObservacionExpedienteRepository observacionRepository;
    private final SubsanacionExpedienteRepository subsanacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final PersonaRepository personaRepository;
    private final OficinaRepository oficinaRepository;
    private final DocumentoRepository documentoRepository;
    private final NumeracionExpedienteService numeracionService;
    private final PlazoAdministrativoService plazoService;
    private final ExpedienteWorkflowService workflowService;
    private final MpvMapper mpvMapper;
    private final OfficeSecurityService officeSecurityService;

    public ExpedienteServiceImpl(ExpedienteRepository expedienteRepository,
                                 TipoTramiteRepository tipoTramiteRepository,
                                 ExpedienteHistorialRepository historialRepository,
                                 ExpedienteDocumentoRepository expedienteDocumentoRepository,
                                 DerivacionExpedienteRepository derivacionRepository,
                                 ObservacionExpedienteRepository observacionRepository,
                                 SubsanacionExpedienteRepository subsanacionRepository,
                                 UsuarioRepository usuarioRepository,
                                 PersonaRepository personaRepository,
                                 OficinaRepository oficinaRepository,
                                 DocumentoRepository documentoRepository,
                                 NumeracionExpedienteService numeracionService,
                                 PlazoAdministrativoService plazoService,
                                 ExpedienteWorkflowService workflowService,
                                 MpvMapper mpvMapper,
                                 OfficeSecurityService officeSecurityService) {
        this.expedienteRepository = expedienteRepository;
        this.tipoTramiteRepository = tipoTramiteRepository;
        this.historialRepository = historialRepository;
        this.expedienteDocumentoRepository = expedienteDocumentoRepository;
        this.derivacionRepository = derivacionRepository;
        this.observacionRepository = observacionRepository;
        this.subsanacionRepository = subsanacionRepository;
        this.usuarioRepository = usuarioRepository;
        this.personaRepository = personaRepository;
        this.oficinaRepository = oficinaRepository;
        this.documentoRepository = documentoRepository;
        this.numeracionService = numeracionService;
        this.plazoService = plazoService;
        this.workflowService = workflowService;
        this.mpvMapper = mpvMapper;
        this.officeSecurityService = officeSecurityService;
    }

    @Override
    @Transactional
    public ExpedienteResponse crearBorrador(CrearBorradorRequest request, String username) {
        Usuario usuario = obtenerUsuario(username);
        TipoTramite tipoTramite = tipoTramiteRepository.findById(request.getTipoTramiteId())
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de trámite no encontrado con ID: " + request.getTipoTramiteId()));

        if (!"ACTIVO".equalsIgnoreCase(tipoTramite.getEstado())) {
            throw new BusinessException("El tipo de trámite seleccionado no se encuentra activo.");
        }

        // Determinar solicitante (Persona)
        Persona solicitante;
        if (request.getSolicitanteId() != null) {
            if (esCiudadano(usuario) && !usuario.getPersona().getId().equals(request.getSolicitanteId())) {
                throw new AccessDeniedException("Un ciudadano no puede registrar expedientes a nombre de otra persona.");
            }
            solicitante = personaRepository.findById(request.getSolicitanteId())
                    .orElseThrow(() -> new ResourceNotFoundException("Persona solicitante no encontrada con ID: " + request.getSolicitanteId()));
        } else {
            solicitante = usuario.getPersona();
            if (solicitante == null) {
                throw new BusinessException("El usuario autenticado no tiene un registro de persona asociado.");
            }
        }

        // Determinar oficina responsable
        Oficina oficina = null;
        if (request.getOficinaResponsableId() != null) {
            oficina = oficinaRepository.findById(request.getOficinaResponsableId())
                    .orElseThrow(() -> new ResourceNotFoundException("Oficina responsable no encontrada con ID: " + request.getOficinaResponsableId()));
        } else if (tipoTramite.getOficinaResponsable() != null) {
            oficina = tipoTramite.getOficinaResponsable();
        }

        int anioActual = LocalDate.now().getYear();

        Expediente expediente = Expediente.builder()
                .uuid(UUID.randomUUID())
                .numeroExpediente(null) // Se asigna al presentar formalmente
                .anio(anioActual)
                .asunto(request.getAsunto().trim())
                .descripcion(request.getDescripcion())
                .tipoTramite(tipoTramite)
                .solicitante(solicitante)
                .oficinaResponsable(oficina)
                .estadoActual(EstadoExpediente.BORRADOR)
                .canalIngreso(request.getCanalIngreso() != null ? request.getCanalIngreso() : CanalIngreso.VIRTUAL)
                .prioridad(request.getPrioridad() != null ? request.getPrioridad() : PrioridadExpediente.NORMAL)
                .confidencialidad(request.getConfidencialidad() != null ? request.getConfidencialidad() : ConfidencialidadExpediente.INTERNO)
                .usuarioRegistro(usuario)
                .build();

        Expediente guardado = expedienteRepository.save(expediente);

        // Registro de historial inicial
        ExpedienteHistorial historial = ExpedienteHistorial.builder()
                .expediente(guardado)
                .estadoAnterior(null)
                .estadoNuevo(EstadoExpediente.BORRADOR)
                .accion("CREACION_BORRADOR")
                .observacion("Registro de borrador de expediente por " + usuario.getUsername())
                .usuarioResponsable(usuario)
                .oficina(oficina)
                .fechaMovimiento(OffsetDateTime.now())
                .identificadorCorrelacion(UUID.randomUUID().toString())
                .build();
        historialRepository.save(historial);

        log.info("Borrador de expediente creado con ID {} por usuario {}", guardado.getId(), username);
        return mpvMapper.toExpedienteResponse(guardado);
    }

    @Override
    @Transactional
    public ExpedienteResponse actualizarBorrador(Long id, ActualizarBorradorRequest request, String username) {
        Usuario usuario = obtenerUsuario(username);
        Expediente expediente = obtenerExpedienteEntity(id);

        validarAccesoEdicion(expediente, usuario);

        if (expediente.getEstadoActual() != EstadoExpediente.BORRADOR) {
            throw new BusinessException("Solo es posible modificar el expediente mientras se encuentre en estado BORRADOR.");
        }

        if (request.getAsunto() != null) {
            expediente.setAsunto(request.getAsunto().trim());
        }
        if (request.getDescripcion() != null) {
            expediente.setDescripcion(request.getDescripcion());
        }
        if (request.getTipoTramiteId() != null) {
            TipoTramite nuevoTramite = tipoTramiteRepository.findById(request.getTipoTramiteId())
                    .orElseThrow(() -> new ResourceNotFoundException("Tipo de trámite no encontrado con ID: " + request.getTipoTramiteId()));
            expediente.setTipoTramite(nuevoTramite);
            if (nuevoTramite.getOficinaResponsable() != null) {
                expediente.setOficinaResponsable(nuevoTramite.getOficinaResponsable());
            }
        }
        if (request.getPrioridad() != null) {
            expediente.setPrioridad(request.getPrioridad());
        }
        if (request.getConfidencialidad() != null) {
            expediente.setConfidencialidad(request.getConfidencialidad());
        }

        Expediente guardado = expedienteRepository.save(expediente);
        return mpvMapper.toExpedienteResponse(guardado);
    }

    @Override
    @Transactional
    public ExpedienteResponse presentarExpediente(Long id, PresentarExpedienteRequest request, String username) {
        Usuario usuario = obtenerUsuario(username);
        Expediente expediente = obtenerExpedienteEntity(id);

        validarAccesoEdicion(expediente, usuario);

        if (expediente.getEstadoActual() != EstadoExpediente.BORRADOR) {
            throw new BusinessException("Solo se pueden presentar expedientes que se encuentren en estado BORRADOR.");
        }

        // Validación de requisitos documentales del tipo de trámite
        TipoTramite tramite = expediente.getTipoTramite();
        if (Boolean.TRUE.equals(tramite.getRequiereDocumentos())) {
            long totalDocs = expedienteDocumentoRepository.countByExpedienteId(expediente.getId());
            if (totalDocs == 0) {
                throw new BusinessException("Este trámite requiere adjuntar al menos un documento antes de ser presentado.");
            }
        }

        // Generar correlativo atómico sin duplicados
        int anio = (expediente.getAnio() != null) ? expediente.getAnio() : LocalDate.now().getYear();
        String numeroGenerado = numeracionService.generarSiguienteNumero(anio);
        expediente.setNumeroExpediente(numeroGenerado);

        // Calcular fecha límite inicial según la configuración del tipo de trámite
        OffsetDateTime ahora = OffsetDateTime.now();
        if (tramite.getPlazoDias() != null && tramite.getPlazoDias() > 0) {
            OffsetDateTime fechaLimite = plazoService.calcularFechaLimite(ahora, tramite.getPlazoDias(), tramite.getUnidadPlazo());
            expediente.setFechaLimite(fechaLimite);
        }

        String obs = (request != null && request.getObservaciones() != null) ? request.getObservaciones() : "Presentación formal de expediente";

        // Transición de estado vía workflow engine
        Expediente presentado = workflowService.transicionar(
                expediente,
                EstadoExpediente.PRESENTADO,
                "PRESENTACION_EXPEDIENTE",
                obs,
                usuario,
                expediente.getOficinaResponsable()
        );

        log.info("Expediente presentado formalmente: ID={}, Número={}", presentado.getId(), presentado.getNumeroExpediente());
        return mpvMapper.toExpedienteResponse(presentado);
    }

    @Override
    @Transactional
    public ExpedienteResponse asociarDocumento(Long expedienteId, AsociarDocumentoRequest request, String username) {
        Usuario usuario = obtenerUsuario(username);
        Expediente expediente = obtenerExpedienteEntity(expedienteId);

        validarAccesoEdicion(expediente, usuario);

        if (expediente.getEstadoActual() != EstadoExpediente.BORRADOR && expediente.getEstadoActual() != EstadoExpediente.OBSERVADO) {
            throw new BusinessException("Solo se pueden adjuntar documentos en estado BORRADOR o durante la etapa de observación.");
        }

        Documento documento = documentoRepository.findById(request.getDocumentoId())
                .orElseThrow(() -> new ResourceNotFoundException("Documento no encontrado con ID: " + request.getDocumentoId()));

        if (!"ACTIVO".equalsIgnoreCase(documento.getEstado().name())) {
            throw new BusinessException("El documento especificado no se encuentra activo.");
        }

        if (expedienteDocumentoRepository.existsByExpedienteIdAndDocumentoId(expedienteId, request.getDocumentoId())) {
            throw new BusinessException("El documento ya se encuentra asociado a este expediente.");
        }

        int siguienteOrden = (request.getOrden() != null) ? request.getOrden() : (expediente.getDocumentos().size() + 1);

        ExpedienteDocumento relacion = ExpedienteDocumento.builder()
                .expediente(expediente)
                .documento(documento)
                .tipoRelacion(request.getTipoRelacion() != null ? request.getTipoRelacion() : TipoRelacionDocumentoExpediente.ANEXO)
                .fechaAsociacion(OffsetDateTime.now())
                .usuarioAsocia(usuario)
                .orden(siguienteOrden)
                .estado("ACTIVO")
                .build();

        expedienteDocumentoRepository.save(relacion);
        expediente.getDocumentos().add(relacion);

        log.info("Documento ID {} asociado al expediente ID {} por {}", documento.getId(), expedienteId, username);
        return mpvMapper.toExpedienteResponse(expediente);
    }

    @Override
    @Transactional
    public void desasociarDocumento(Long expedienteId, Long documentoId, String username) {
        Usuario usuario = obtenerUsuario(username);
        Expediente expediente = obtenerExpedienteEntity(expedienteId);

        validarAccesoEdicion(expediente, usuario);

        if (expediente.getEstadoActual() != EstadoExpediente.BORRADOR && expediente.getEstadoActual() != EstadoExpediente.OBSERVADO) {
            throw new BusinessException("Solo se pueden remover documentos en estado BORRADOR o durante la etapa de observación.");
        }

        ExpedienteDocumento rel = expedienteDocumentoRepository.findByExpedienteIdAndDocumentoId(expedienteId, documentoId)
                .orElseThrow(() -> new ResourceNotFoundException("El documento no se encuentra vinculado a este expediente."));

        expedienteDocumentoRepository.delete(rel);
        expediente.getDocumentos().remove(rel);
        log.info("Documento ID {} desasociado del expediente ID {} por {}", documentoId, expedienteId, username);
    }

    @Override
    @Transactional
    public ExpedienteResponse recibirExpediente(Long id, String observaciones, String username) {
        Usuario usuario = obtenerUsuario(username);
        Expediente expediente = obtenerExpedienteEntity(id);

        if (expediente.getEstadoActual() != EstadoExpediente.PRESENTADO) {
            throw new BusinessException("Solo se pueden recibir expedientes en estado PRESENTADO.");
        }

        String obs = (observaciones != null && !observaciones.isBlank()) ? observaciones : "Expediente recibido formalmente por operador";

        Expediente recibido = workflowService.transicionar(
                expediente,
                EstadoExpediente.RECIBIDO,
                "RECEPCION_EXPEDIENTE",
                obs,
                usuario,
                expediente.getOficinaResponsable()
        );

        return mpvMapper.toExpedienteResponse(recibido);
    }

    @Override
    @Transactional
    public ExpedienteResponse derivarExpediente(Long id, DerivarExpedienteRequest request, String username) {
        Usuario usuario = obtenerUsuario(username);
        Expediente expediente = obtenerExpedienteEntity(id);

        Oficina origen = expediente.getOficinaResponsable();
        Oficina destino = oficinaRepository.findById(request.getOficinaDestinoId())
                .orElseThrow(() -> new ResourceNotFoundException("Oficina de destino no encontrada con ID: " + request.getOficinaDestinoId()));

        if (origen != null && origen.getId().equals(destino.getId())) {
            throw new BusinessException("La oficina de destino debe ser diferente a la oficina de origen.");
        }

        // Crear registro de derivación
        DerivacionExpediente derivacion = DerivacionExpediente.builder()
                .expediente(expediente)
                .oficinaOrigen(origen)
                .oficinaDestino(destino)
                .usuarioDerivador(usuario)
                .motivo(request.getMotivo().trim())
                .fechaDerivacion(OffsetDateTime.now())
                .estado(EstadoDerivacion.PENDIENTE)
                .observaciones(request.getObservaciones())
                .build();
        derivacionRepository.save(derivacion);

        // Actualizar oficina responsable del expediente
        expediente.setOficinaResponsable(destino);

        // Transicionar estado a DERIVADO
        Expediente derivado = workflowService.transicionar(
                expediente,
                EstadoExpediente.DERIVADO,
                "DERIVACION_EXPEDIENTE",
                "Derivado a " + destino.getNombre() + ". Motivo: " + request.getMotivo(),
                usuario,
                destino
        );

        return mpvMapper.toExpedienteResponse(derivado);
    }

    @Override
    @Transactional
    public ExpedienteResponse observarExpediente(Long id, ObservarExpedienteRequest request, String username) {
        Usuario usuario = obtenerUsuario(username);
        Expediente expediente = obtenerExpedienteEntity(id);

        OffsetDateTime ahora = OffsetDateTime.now();
        int diasPlazo = (request.getDiasPlazoSubsanacion() != null && request.getDiasPlazoSubsanacion() > 0)
                ? request.getDiasPlazoSubsanacion() : 10; // Plazo estándar para subsanaciones

        OffsetDateTime fechaLimite = plazoService.calcularFechaLimite(ahora, diasPlazo, UnidadPlazo.DIAS_HABILES);

        ObservacionExpediente obs = ObservacionExpediente.builder()
                .expediente(expediente)
                .descripcion(request.getDescripcion().trim())
                .usuarioObservador(usuario)
                .fechaRegistro(ahora)
                .fechaNotificacion(ahora)
                .fechaLimite(fechaLimite)
                .estado(EstadoObservacion.PENDIENTE)
                .build();
        observacionRepository.save(obs);

        Expediente observado = workflowService.transicionar(
                expediente,
                EstadoExpediente.OBSERVADO,
                "OBSERVACION_EXPEDIENTE",
                request.getDescripcion().trim(),
                usuario,
                expediente.getOficinaResponsable()
        );

        return mpvMapper.toExpedienteResponse(observado);
    }

    @Override
    @Transactional
    public ExpedienteResponse subsanarExpediente(Long id, SubsanarExpedienteRequest request, String username) {
        Usuario usuario = obtenerUsuario(username);
        Expediente expediente = obtenerExpedienteEntity(id);

        validarAccesoEdicion(expediente, usuario);

        if (expediente.getEstadoActual() != EstadoExpediente.OBSERVADO) {
            throw new BusinessException("Solo se pueden subsanar expedientes en estado OBSERVADO.");
        }

        ObservacionExpediente obsPendiente = observacionRepository.findTopByExpedienteIdAndEstadoOrderByFechaRegistroDesc(expediente.getId(), EstadoObservacion.PENDIENTE)
                .orElseThrow(() -> new BusinessException("No existe ninguna observación pendiente de subsanar en este expediente."));

        // Asociar documentos nuevos como subsanación si se enviaron
        if (request.getDocumentoIds() != null && !request.getDocumentoIds().isEmpty()) {
            for (Long docId : request.getDocumentoIds()) {
                if (!expedienteDocumentoRepository.existsByExpedienteIdAndDocumentoId(expediente.getId(), docId)) {
                    documentoRepository.findById(docId).ifPresent(doc -> {
                        ExpedienteDocumento ed = ExpedienteDocumento.builder()
                                .expediente(expediente)
                                .documento(doc)
                                .tipoRelacion(TipoRelacionDocumentoExpediente.SUBSANACION)
                                .fechaAsociacion(OffsetDateTime.now())
                                .usuarioAsocia(usuario)
                                .orden(expediente.getDocumentos().size() + 1)
                                .estado("ACTIVO")
                                .build();
                        expedienteDocumentoRepository.save(ed);
                        expediente.getDocumentos().add(ed);
                    });
                }
            }
        }

        // Crear registro de subsanación
        OffsetDateTime ahora = OffsetDateTime.now();
        SubsanacionExpediente sub = SubsanacionExpediente.builder()
                .observacion(obsPendiente)
                .expediente(expediente)
                .respuestaCiudadano(request.getRespuestaCiudadano().trim())
                .fechaSubsanacion(ahora)
                .usuarioSubsanador(usuario)
                .estado(EstadoSubsanacion.PRESENTADA)
                .build();
        subsanacionRepository.save(sub);

        // Actualizar observación
        obsPendiente.setEstado(EstadoObservacion.SUBSANADA);
        obsPendiente.setFechaRespuesta(ahora);
        observacionRepository.save(obsPendiente);

        // Transición de estado a EN_SUBSANACION
        Expediente subsanado = workflowService.transicionar(
                expediente,
                EstadoExpediente.EN_SUBSANACION,
                "SUBSANACION_PRESENTADA",
                request.getRespuestaCiudadano().trim(),
                usuario,
                expediente.getOficinaResponsable()
        );

        return mpvMapper.toExpedienteResponse(subsanado);
    }

    @Override
    @Transactional
    public ExpedienteResponse resolverExpediente(Long id, CambiarEstadoRequest request, String username) {
        Usuario usuario = obtenerUsuario(username);
        Expediente expediente = obtenerExpedienteEntity(id);

        Expediente resuelto = workflowService.transicionar(
                expediente,
                EstadoExpediente.RESUELTO,
                "RESOLUCION_EXPEDIENTE",
                request.getMotivo().trim(),
                usuario,
                expediente.getOficinaResponsable()
        );

        return mpvMapper.toExpedienteResponse(resuelto);
    }

    @Override
    @Transactional
    public ExpedienteResponse archivarExpediente(Long id, CambiarEstadoRequest request, String username) {
        Usuario usuario = obtenerUsuario(username);
        Expediente expediente = obtenerExpedienteEntity(id);

        Expediente archivado = workflowService.transicionar(
                expediente,
                EstadoExpediente.ARCHIVADO,
                "ARCHIVO_EXPEDIENTE",
                request.getMotivo().trim(),
                usuario,
                expediente.getOficinaResponsable()
        );

        return mpvMapper.toExpedienteResponse(archivado);
    }

    @Override
    @Transactional
    public ExpedienteResponse anularExpediente(Long id, CambiarEstadoRequest request, String username) {
        Usuario usuario = obtenerUsuario(username);
        Expediente expediente = obtenerExpedienteEntity(id);

        Expediente anulado = workflowService.transicionar(
                expediente,
                EstadoExpediente.ANULADO,
                "ANULACION_EXPEDIENTE",
                request.getMotivo().trim(),
                usuario,
                expediente.getOficinaResponsable()
        );

        return mpvMapper.toExpedienteResponse(anulado);
    }

    @Override
    @Transactional(readOnly = true)
    public ExpedienteDetalleResponse obtenerDetalle(Long id, String username) {
        Usuario usuario = obtenerUsuario(username);
        Expediente expediente = obtenerExpedienteEntity(id);

        validarAccesoLectura(expediente, usuario);

        List<ExpedienteDocumentoResponse> docs = expediente.getDocumentos().stream()
                .map(mpvMapper::toDocumentoResponse)
                .collect(Collectors.toList());

        List<ExpedienteHistorialResponse> hist = historialRepository.findByExpedienteIdOrderByFechaMovimientoAsc(id)
                .stream()
                .map(mpvMapper::toHistorialResponse)
                .collect(Collectors.toList());

        List<DerivacionResponse> derivaciones = derivacionRepository.findByExpedienteIdOrderByFechaDerivacionDesc(id)
                .stream()
                .map(mpvMapper::toDerivacionResponse)
                .collect(Collectors.toList());

        List<ObservacionResponse> observaciones = observacionRepository.findByExpedienteIdOrderByFechaRegistroDesc(id)
                .stream()
                .map(mpvMapper::toObservacionResponse)
                .collect(Collectors.toList());

        return ExpedienteDetalleResponse.builder()
                .expediente(mpvMapper.toExpedienteResponse(expediente))
                .documentos(docs)
                .historial(hist)
                .derivaciones(derivaciones)
                .observaciones(observaciones)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ExpedienteResponse> listarExpedientes(EstadoExpediente estado, Long oficinaId, Long tipoTramiteId, String termino, Pageable pageable, String username) {
        Usuario usuario = obtenerUsuario(username);

        Long solicitanteFiltro = null;
        Long oficinaFiltro = oficinaId;

        if (esCiudadano(usuario)) {
            // Un ciudadano solo ve sus propios expedientes (Anti-IDOR)
            solicitanteFiltro = usuario.getPersona() != null ? usuario.getPersona().getId() : -1L;
            oficinaFiltro = null;
        }

        String searchPattern = (termino != null && !termino.trim().isEmpty())
                ? "%" + termino.trim().toLowerCase() + "%"
                : null;

        return expedienteRepository.buscarConFiltros(estado, oficinaFiltro, tipoTramiteId, solicitanteFiltro, null, searchPattern, pageable)
                .map(mpvMapper::toExpedienteResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpedienteHistorialResponse> obtenerHistorial(Long id, String username) {
        Usuario usuario = obtenerUsuario(username);
        Expediente expediente = obtenerExpedienteEntity(id);
        validarAccesoLectura(expediente, usuario);

        return historialRepository.findByExpedienteIdOrderByFechaMovimientoAsc(id)
                .stream()
                .map(mpvMapper::toHistorialResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpedienteDocumentoResponse> obtenerDocumentos(Long id, String username) {
        Usuario usuario = obtenerUsuario(username);
        Expediente expediente = obtenerExpedienteEntity(id);
        validarAccesoLectura(expediente, usuario);

        return expediente.getDocumentos().stream()
                .map(mpvMapper::toDocumentoResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public MpvEstadisticasResponse obtenerEstadisticas(String username) {
        Usuario usuario = obtenerUsuario(username);
        Long oficinaId = null;

        // Si es usuario interno de oficina no admin, acota las métricas a su oficina
        if (!officeSecurityService.tieneAccesoTransversalMesaPartes(usuario)) {
            oficinaId = officeSecurityService.obtenerOficinaActivaId(usuario.getUsername());
        }

        return MpvEstadisticasResponse.builder()
                .totalExpedientes(expedienteRepository.countByOficina(oficinaId))
                .borradores(expedienteRepository.countByEstadoAndOficina(EstadoExpediente.BORRADOR, oficinaId))
                .presentados(expedienteRepository.countByEstadoAndOficina(EstadoExpediente.PRESENTADO, oficinaId))
                .recibidos(expedienteRepository.countByEstadoAndOficina(EstadoExpediente.RECIBIDO, oficinaId))
                .enRevision(expedienteRepository.countByEstadoAndOficina(EstadoExpediente.EN_REVISION, oficinaId))
                .observados(expedienteRepository.countByEstadoAndOficina(EstadoExpediente.OBSERVADO, oficinaId))
                .enAtencion(expedienteRepository.countByEstadoAndOficina(EstadoExpediente.EN_ATENCION, oficinaId))
                .resueltos(expedienteRepository.countByEstadoAndOficina(EstadoExpediente.RESUELTO, oficinaId))
                .archivados(expedienteRepository.countByEstadoAndOficina(EstadoExpediente.ARCHIVADO, oficinaId))
                .build();
    }

    // --- Métodos auxiliares de seguridad y validación Anti-IDOR ---

    private Usuario obtenerUsuario(String username) {
        return usuarioRepository.findByUsernameWithRolesAndPersona(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));
    }

    private Expediente obtenerExpedienteEntity(Long id) {
        return expedienteRepository.findByIdWithDetalles(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expediente no encontrado con ID: " + id));
    }

    private boolean esCiudadano(Usuario usuario) {
        if (usuario.getRoles() == null || usuario.getRoles().isEmpty()) {
            return true;
        }
        boolean tieneRolInterno = usuario.getRoles().stream()
                .anyMatch(r -> coincideRol(r, "ROLE_ADMIN")
                        || coincideRol(r, "ROLE_SUPERADMIN")
                        || coincideRol(r, "ROLE_MESA_PARTES")
                        || coincideRol(r, "ROLE_OPERADOR")
                        || coincideRol(r, "ROLE_JEFE_OFICINA")
                        || coincideRol(r, "ROLE_TRABAJADOR"));
        return !tieneRolInterno;
    }

    private boolean tieneRol(Usuario usuario, String nombreRol) {
        if (usuario.getRoles() == null) return false;
        return usuario.getRoles().stream()
                .anyMatch(r -> coincideRol(r, nombreRol));
    }

    private boolean coincideRol(Rol r, String target) {
        if (r == null || target == null) return false;
        return (r.getCodigo() != null && r.getCodigo().equalsIgnoreCase(target))
                || (r.getNombre() != null && r.getNombre().equalsIgnoreCase(target));
    }

    private void validarAccesoLectura(Expediente expediente, Usuario usuario) {
        if (officeSecurityService.tieneAccesoTransversalMesaPartes(usuario)) {
            return; // Personal centralizado o administrador con acceso total
        }

        if (esCiudadano(usuario)) {
            if (expediente.getSolicitante() == null || usuario.getPersona() == null
                    || !expediente.getSolicitante().getId().equals(usuario.getPersona().getId())) {
                log.warn("Intento de acceso no autorizado a expediente ID {} por ciudadano {}", expediente.getId(), usuario.getUsername());
                throw new AccessDeniedException("No tiene permisos para acceder a este expediente.");
            }
            return;
        }

        Long oficinaUsuarioId = officeSecurityService.obtenerOficinaActivaId(usuario.getUsername());
        if (oficinaUsuarioId != null) {
            boolean esDeSuOficina = expediente.getOficinaResponsable() != null
                    && oficinaUsuarioId.equals(expediente.getOficinaResponsable().getId());
            boolean esSuOrigen = expediente.getOficinaOrigen() != null
                    && oficinaUsuarioId.equals(expediente.getOficinaOrigen().getId());
            boolean esResponsableDirecto = expediente.getResponsableActual() != null
                    && usuario.getId().equals(expediente.getResponsableActual().getId());
            boolean participoEnDerivacion = derivacionRepository.findByExpedienteIdOrderByFechaDerivacionDesc(expediente.getId())
                    .stream()
                    .anyMatch(d -> (d.getOficinaOrigen() != null && oficinaUsuarioId.equals(d.getOficinaOrigen().getId()))
                            || (d.getOficinaDestino() != null && oficinaUsuarioId.equals(d.getOficinaDestino().getId())));

            if (!esDeSuOficina && !esSuOrigen && !esResponsableDirecto && !participoEnDerivacion) {
                log.warn("Intento de acceso cruzado de oficina bloqueado: usuario={} (oficinaId={}) intentó ver expediente ID={} (oficinaResponsable={})",
                        usuario.getUsername(), oficinaUsuarioId, expediente.getId(),
                        expediente.getOficinaResponsable() != null ? expediente.getOficinaResponsable().getId() : null);
                throw new AccessDeniedException("Acceso restringido: El expediente pertenece a otra unidad orgánica municipal.");
            }
        }
    }

    private void validarAccesoEdicion(Expediente expediente, Usuario usuario) {
        if (tieneRol(usuario, "ROLE_ADMIN") || tieneRol(usuario, "ROLE_SUPERADMIN")) {
            return;
        }

        if (esCiudadano(usuario)) {
            if (expediente.getSolicitante() == null || usuario.getPersona() == null
                    || !expediente.getSolicitante().getId().equals(usuario.getPersona().getId())) {
                log.warn("Intento de modificación no autorizada a expediente ID {} por ciudadano {}", expediente.getId(), usuario.getUsername());
                throw new AccessDeniedException("No tiene autorización para modificar este expediente.");
            }
        }
    }
}
