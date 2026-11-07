package pe.gob.municipalidad.sigm.modules.mesapartes.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.AtencionDtos.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.DecisionExpediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.Expediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.NotificacionAdministrativa;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoExpediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoNotificacion;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.DecisionExpedienteRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.ExpedienteRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.NotificacionAdministrativaRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.NotificacionResultadoService;
import pe.gob.municipalidad.sigm.modules.mesapartes.workflow.ExpedienteWorkflowService;
import pe.gob.municipalidad.sigm.common.notification.dto.NotificationDtos.EnviarNotificacionRequest;
import pe.gob.municipalidad.sigm.common.notification.service.CentralNotificationService;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificacionResultadoServiceImpl implements NotificacionResultadoService {

    private static final Logger log = LoggerFactory.getLogger(NotificacionResultadoServiceImpl.class);

    private final NotificacionAdministrativaRepository notificacionRepository;
    private final ExpedienteRepository expedienteRepository;
    private final DecisionExpedienteRepository decisionRepository;
    private final ExpedienteWorkflowService workflowService;
    private final DocumentoRepository documentoRepository;
    private final DocumentoService documentoService;
    private final UsuarioRepository usuarioRepository;
    private final CentralNotificationService notificationService;

    public NotificacionResultadoServiceImpl(
            NotificacionAdministrativaRepository notificacionRepository,
            ExpedienteRepository expedienteRepository,
            DecisionExpedienteRepository decisionRepository,
            ExpedienteWorkflowService workflowService,
            DocumentoRepository documentoRepository,
            DocumentoService documentoService,
            UsuarioRepository usuarioRepository,
            CentralNotificationService notificationService) {
        this.notificacionRepository = notificacionRepository;
        this.expedienteRepository = expedienteRepository;
        this.decisionRepository = decisionRepository;
        this.workflowService = workflowService;
        this.documentoRepository = documentoRepository;
        this.documentoService = documentoService;
        this.usuarioRepository = usuarioRepository;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public NotificacionResponse registrarNotificacion(Long expedienteId, RegistrarNotificacionRequest request, String username) {
        Usuario usuario = obtenerUsuario(username);
        Expediente expediente = expedienteRepository.findByIdWithDetalles(expedienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Expediente no encontrado con ID: " + expedienteId));

        DecisionExpediente decision = null;
        if (request.getDecisionId() != null) {
            decision = decisionRepository.findById(request.getDecisionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Decisión no encontrada con ID: " + request.getDecisionId()));
        }

        Documento documento = decision != null ? decision.getDocumento() : null;

        NotificacionAdministrativa notif = NotificacionAdministrativa.builder()
                .expediente(expediente)
                .decision(decision)
                .documento(documento)
                .destinatarioNombre(request.getDestinatarioNombre().trim())
                .destinatarioDocumento(request.getDestinatarioDocumento() != null ? request.getDestinatarioDocumento().trim() : null)
                .destinatarioDireccion(request.getDestinatarioDireccion() != null ? request.getDestinatarioDireccion().trim() : null)
                .destinatarioEmail(request.getDestinatarioEmail() != null ? request.getDestinatarioEmail().trim() : null)
                .medioNotificacion(request.getMedioNotificacion())
                .estadoNotificacion(EstadoNotificacion.PENDIENTE)
                .esLegalmenteValida(false)
                .observaciones(request.getObservaciones() != null ? request.getObservaciones().trim() : null)
                .usuarioResponsable(usuario)
                .createdBy(usuario.getUsername())
                .build();

        NotificacionAdministrativa guardada = notificacionRepository.save(notif);
        log.info("Cédula/Aviso de notificación registrado ID {} para exp {}", guardada.getId(), expediente.getNumeroExpediente());

        return mapearAResponse(guardada);
    }

    @Override
    @Transactional
    public NotificacionResponse registrarIntentoEnvio(Long notificacionId, String observaciones, String username) {
        Usuario usuario = obtenerUsuario(username);
        NotificacionAdministrativa notif = notificacionRepository.findById(notificacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Notificación no encontrada con ID: " + notificacionId));

        notif.setFechaIntento(OffsetDateTime.now());
        notif.setEstadoNotificacion(EstadoNotificacion.INTENTO_ENVIADO);
        if (observaciones != null && !observaciones.isBlank()) {
            notif.setObservaciones((notif.getObservaciones() != null ? notif.getObservaciones() + "\n" : "") +
                    "[Intento registrado por " + username + " - " + OffsetDateTime.now() + "]: " + observaciones.trim());
        }
        notif.setUpdatedBy(usuario.getUsername());

        NotificacionAdministrativa guardada = notificacionRepository.save(notif);
        return mapearAResponse(guardada);
    }

    @Override
    @Transactional
    public NotificacionResponse acreditarNotificacionValida(Long notificacionId, AcreditarNotificacionRequest request, MultipartFile cargoPdf, String username) {
        Usuario usuario = obtenerUsuario(username);
        NotificacionAdministrativa notif = notificacionRepository.findById(notificacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Notificación no encontrada con ID: " + notificacionId));

        if (Boolean.TRUE.equals(notif.getEsLegalmenteValida())) {
            log.info("La notificación ID {} ya fue formalmente acreditada.", notificacionId);
            return mapearAResponse(notif);
        }

        // Subir evidencia documental si se adjunta cargo firmado o constancia postal
        if (cargoPdf != null && !cargoPdf.isEmpty()) {
            SubirDocumentoRequest uploadRequest = SubirDocumentoRequest.builder()
                    .categoria(CategoriaDocumento.CONSTANCIA)
                    .clasificacionAcceso(ClasificacionAcceso.INTERNO)
                    .descripcion("Cargo o constancia de notificación válida #" + notif.getId() + " - Acuse: " + request.getNumeroAcuseRecibo())
                    .oficinaId(notif.getExpediente().getOficinaResponsable() != null ? notif.getExpediente().getOficinaResponsable().getId() : null)
                    .build();

            DocumentoResponse docResp = documentoService.subirDocumento(cargoPdf, uploadRequest, username);
            Documento evidencia = documentoRepository.findById(docResp.getId()).orElse(null);
            notif.setEvidenciaDocumental(evidencia);
        }

        notif.setNumeroAcuseRecibo(request.getNumeroAcuseRecibo().trim());
        notif.setFechaPracticaValida(OffsetDateTime.now());
        notif.setEsLegalmenteValida(true);
        notif.setEstadoNotificacion(EstadoNotificacion.NOTIFICADA_VALIDA);
        if (request.getObservaciones() != null && !request.getObservaciones().isBlank()) {
            notif.setObservaciones((notif.getObservaciones() != null ? notif.getObservaciones() + "\n" : "") +
                    "[Acreditación formal]: " + request.getObservaciones().trim());
        }
        notif.setUpdatedBy(usuario.getUsername());

        NotificacionAdministrativa guardada = notificacionRepository.save(notif);

        // Si el expediente está en estado RESUELTO, transicionar legalmente a NOTIFICADO
        Expediente expediente = notif.getExpediente();
        if (expediente.getEstadoActual() == EstadoExpediente.RESUELTO) {
            workflowService.transicionar(
                    expediente,
                    EstadoExpediente.NOTIFICADO,
                    "ACREDITACION_NOTIFICACION",
                    "Notificación administrativa legalmente practicada con acuse N°: " + notif.getNumeroAcuseRecibo(),
                    usuario,
                    expediente.getOficinaResponsable()
            );
        }

        // Notificación al administrado
        try {
            if (expediente.getSolicitante() != null) {
                usuarioRepository.findByPersonaId(expediente.getSolicitante().getId()).ifPresent(usuarioSolicitante -> {
                    notificationService.enviarNotificacion(
                            EnviarNotificacionRequest.builder()
                                    .usuarioId(usuarioSolicitante.getId())
                                    .titulo("Notificación Formal Entregada")
                                    .mensaje("Se acreditó la práctica formal de la notificación del expediente " + expediente.getNumeroExpediente())
                                    .tipo("DOCUMENTO")
                                    .referenciaId(expediente.getNumeroExpediente())
                                    .urlDestino("/mesa-partes/mis-solicitudes")
                                    .enviarCorreo(true)
                                    .build(),
                            usuario.getUsername()
                    );
                });
            }
        } catch (Exception e) {
            log.warn("No se pudo notificar acreditación: {}", e.getMessage());
        }

        return mapearAResponse(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificacionResponse> listarNotificacionesPorExpediente(Long expedienteId, String username) {
        return notificacionRepository.findByExpedienteIdOrderByCreatedAtDesc(expedienteId)
                .stream()
                .map(this::mapearAResponse)
                .collect(Collectors.toList());
    }

    private NotificacionResponse mapearAResponse(NotificacionAdministrativa n) {
        return NotificacionResponse.builder()
                .id(n.getId())
                .expedienteId(n.getExpediente().getId())
                .numeroExpediente(n.getExpediente().getNumeroExpediente())
                .decisionId(n.getDecision() != null ? n.getDecision().getId() : null)
                .numeroDocumentoDecision(n.getDecision() != null ? n.getDecision().getNumeroDocumento() : "Sin documento")
                .destinatarioNombre(n.getDestinatarioNombre())
                .destinatarioDocumento(n.getDestinatarioDocumento())
                .destinatarioDireccion(n.getDestinatarioDireccion())
                .destinatarioEmail(n.getDestinatarioEmail())
                .medioNotificacion(n.getMedioNotificacion())
                .estadoNotificacion(n.getEstadoNotificacion())
                .fechaIntento(n.getFechaIntento())
                .fechaPracticaValida(n.getFechaPracticaValida())
                .numeroAcuseRecibo(n.getNumeroAcuseRecibo())
                .evidenciaDocumentalId(n.getEvidenciaDocumental() != null ? n.getEvidenciaDocumental().getId() : null)
                .observaciones(n.getObservaciones())
                .usuarioResponsableId(n.getUsuarioResponsable().getId())
                .usuarioResponsableNombre(n.getUsuarioResponsable().getNombreCompleto())
                .esLegalmenteValida(Boolean.TRUE.equals(n.getEsLegalmenteValida()))
                .createdAt(n.getCreatedAt())
                .build();
    }

    private Usuario obtenerUsuario(String username) {
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));
    }
}
