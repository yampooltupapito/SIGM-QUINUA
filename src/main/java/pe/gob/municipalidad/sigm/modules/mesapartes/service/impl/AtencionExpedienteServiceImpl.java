package pe.gob.municipalidad.sigm.modules.mesapartes.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.PersonaOficinaRepository;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.AtencionDtos.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.Expediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoExpediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.workflow.ExpedienteWorkflowService;
import pe.gob.municipalidad.sigm.common.notification.dto.NotificationDtos.EnviarNotificacionRequest;
import pe.gob.municipalidad.sigm.common.notification.service.CentralNotificationService;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class AtencionExpedienteServiceImpl implements AtencionExpedienteService {

    private static final Logger log = LoggerFactory.getLogger(AtencionExpedienteServiceImpl.class);

    private final ExpedienteRepository expedienteRepository;
    private final ExpedienteWorkflowService workflowService;
    private final UsuarioRepository usuarioRepository;
    private final PersonaOficinaRepository personaOficinaRepository;
    private final CentralNotificationService notificationService;
    private final EvaluacionExpedienteService evaluacionService;
    private final PropuestaDecisionService propuestaService;
    private final DecisionExpedienteService decisionService;
    private final NotificacionResultadoService notificacionService;
    private final CierreExpedienteService cierreService;
    private final ArchivoExpedienteService archivoService;

    public AtencionExpedienteServiceImpl(
            ExpedienteRepository expedienteRepository,
            ExpedienteWorkflowService workflowService,
            UsuarioRepository usuarioRepository,
            PersonaOficinaRepository personaOficinaRepository,
            CentralNotificationService notificationService,
            EvaluacionExpedienteService evaluacionService,
            PropuestaDecisionService propuestaService,
            DecisionExpedienteService decisionService,
            NotificacionResultadoService notificacionService,
            CierreExpedienteService cierreService,
            ArchivoExpedienteService archivoService) {
        this.expedienteRepository = expedienteRepository;
        this.workflowService = workflowService;
        this.usuarioRepository = usuarioRepository;
        this.personaOficinaRepository = personaOficinaRepository;
        this.notificationService = notificationService;
        this.evaluacionService = evaluacionService;
        this.propuestaService = propuestaService;
        this.decisionService = decisionService;
        this.notificacionService = notificacionService;
        this.cierreService = cierreService;
        this.archivoService = archivoService;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AtencionItemResponse> listarBandejaAtencion(
            Long oficinaId,
            EstadoExpediente estado,
            Long tipoTramiteId,
            String termino,
            Pageable pageable,
            String username
    ) {
        Usuario usuario = obtenerUsuario(username);
        boolean isAdmin = esAdmin(usuario);

        Long filtroResponsable = null;
        Long filtroOficina = oficinaId;

        // Si no es admin global, se restringe a su ámbito de trabajo
        if (!isAdmin) {
            filtroResponsable = usuario.getId();
        }

        String terminoBusqueda = (termino != null && !termino.isBlank())
                ? "%" + termino.trim().toLowerCase() + "%" : null;

        Page<Expediente> expedientes = expedienteRepository.buscarAtencionConFiltros(
                filtroResponsable,
                filtroOficina,
                estado,
                tipoTramiteId,
                terminoBusqueda,
                pageable
        );

        return expedientes.map(this::mapearAItem);
    }

    @Override
    @Transactional
    public AtencionItemResponse iniciarAtencion(Long expedienteId, IniciarAtencionRequest request, String username) {
        Usuario usuario = obtenerUsuario(username);
        Expediente expediente = expedienteRepository.findByIdWithDetalles(expedienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Expediente no encontrado con ID: " + expedienteId));

        Oficina oficina = expediente.getOficinaResponsable();
        if (oficina == null) {
            throw new BusinessException("El expediente no cuenta con oficina responsable.");
        }

        boolean isAdmin = esAdmin(usuario);

        // Validar que el expediente no sea ajeno
        if (!isAdmin) {
            boolean esResponsableActual = expediente.getResponsableActual() != null &&
                    expediente.getResponsableActual().getId().equals(usuario.getId());

            boolean trabajaEnOficina = personaOficinaRepository.findByPersonaIdAndEstado(usuario.getPersona().getId(), "ACTIVO")
                    .stream()
                    .anyMatch(po -> po.getOficina().getId().equals(oficina.getId()));

            if (!esResponsableActual && !trabajaEnOficina) {
                throw new AccessDeniedException("No está autorizado para iniciar la atención de un expediente asignado a otra dependencia o servidor.");
            }
        }

        // Si ya está en atención, verificar idempotencia
        if (expediente.getEstadoActual() == EstadoExpediente.EN_ATENCION) {
            log.info("Expediente {} ya se encuentra en estado EN_ATENCION.", expediente.getNumeroExpediente());
            return mapearAItem(expediente);
        }

        // Transición legal mediante workflow
        String obs = (request != null && request.getIndicaciones() != null && !request.getIndicaciones().isBlank())
                ? request.getIndicaciones().trim()
                : "Inicio formal de atención técnica y sustanciación del trámite.";

        expediente.setFechaInicioAtencion(OffsetDateTime.now());
        expediente.setUsuarioAtencion(usuario);
        if (expediente.getResponsableActual() == null) {
            expediente.setResponsableActual(usuario);
        }

        Expediente actualizado = workflowService.transicionar(
                expediente,
                EstadoExpediente.EN_ATENCION,
                "INICIO_ATENCION",
                obs,
                usuario,
                oficina
        );

        // Notificación institucional
        try {
            notificationService.enviarNotificacion(
                    EnviarNotificacionRequest.builder()
                            .usuarioId(usuario.getId())
                            .titulo("Atención Iniciada: " + actualizado.getNumeroExpediente())
                            .mensaje("Ha iniciado formalmente la evaluación del expediente " + actualizado.getNumeroExpediente())
                            .tipo("TRAMITE")
                            .referenciaId(actualizado.getNumeroExpediente())
                            .urlDestino("/dashboard/mesa-partes/atencion/" + actualizado.getId())
                            .build(),
                    usuario.getUsername()
            );
        } catch (Exception e) {
            log.warn("No se pudo emitir notificación de inicio de atención: {}", e.getMessage());
        }

        return mapearAItem(actualizado);
    }

    @Override
    @Transactional(readOnly = true)
    public DetalleAtencionResponse obtenerDetalleAtencion(Long expedienteId, String username) {
        Usuario usuario = obtenerUsuario(username);
        Expediente expediente = expedienteRepository.findByIdWithDetalles(expedienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Expediente no encontrado con ID: " + expedienteId));

        validarAccesoConsulta(usuario, expediente);

        List<EvaluacionResponse> evaluaciones = evaluacionService.listarEvaluacionesPorExpediente(expedienteId, username);
        List<PropuestaResponse> propuestas = propuestaService.listarPropuestasPorExpediente(expedienteId, username);
        List<DecisionResponse> decisiones = decisionService.listarDecisionesPorExpediente(expedienteId, username);
        List<NotificacionResponse> notificaciones = notificacionService.listarNotificacionesPorExpediente(expedienteId, username);

        CierreResponse cierre = null;
        try {
            cierre = cierreService.obtenerCierre(expedienteId, username);
        } catch (Exception ignored) {}

        ArchivoResponse archivo = null;
        try {
            archivo = archivoService.obtenerArchivo(expedienteId, username);
        } catch (Exception ignored) {}

        Long diasRestantes = null;
        boolean vencido = false;
        if (expediente.getFechaLimite() != null) {
            Duration duracion = Duration.between(OffsetDateTime.now(), expediente.getFechaLimite());
            diasRestantes = duracion.toDays();
            vencido = OffsetDateTime.now().isAfter(expediente.getFechaLimite());
        }

        return DetalleAtencionResponse.builder()
                .expedienteId(expediente.getId())
                .numeroExpediente(expediente.getNumeroExpediente())
                .asunto(expediente.getAsunto())
                .tipoTramite(expediente.getTipoTramite() != null ? expediente.getTipoTramite().getNombre() : "Trámite General")
                .solicitanteNombre(expediente.getSolicitante() != null ? expediente.getSolicitante().getNombreCompleto() : "Anónimo")
                .solicitanteDocumento(expediente.getSolicitante() != null ? expediente.getSolicitante().getNumeroDocumento() : "")
                .oficinaResponsableId(expediente.getOficinaResponsable() != null ? expediente.getOficinaResponsable().getId() : null)
                .oficinaResponsableNombre(expediente.getOficinaResponsable() != null ? expediente.getOficinaResponsable().getNombre() : "Sin Oficina")
                .responsableActualId(expediente.getResponsableActual() != null ? expediente.getResponsableActual().getId() : null)
                .responsableActualNombre(expediente.getResponsableActual() != null ? expediente.getResponsableActual().getNombreCompleto() : "No Asignado")
                .estadoActual(expediente.getEstadoActual())
                .prioridad(expediente.getPrioridad())
                .fechaPresentacion(expediente.getFechaPresentacion())
                .fechaRecepcion(expediente.getFechaRecepcion())
                .fechaInicioAtencion(expediente.getFechaInicioAtencion())
                .fechaLimite(expediente.getFechaLimite())
                .diasRestantes(diasRestantes)
                .vencido(vencido)
                .reabierto(Boolean.TRUE.equals(expediente.getEsReabierto()))
                .resultadoFinal(expediente.getResultadoFinal())
                .ubicacionArchivo(expediente.getUbicacionArchivo())
                .evaluaciones(evaluaciones)
                .propuestas(propuestas)
                .decisiones(decisiones)
                .notificaciones(notificaciones)
                .cierre(cierre)
                .archivo(archivo)
                .build();
    }

    private AtencionItemResponse mapearAItem(Expediente e) {
        Long diasRestantes = null;
        boolean vencido = false;
        if (e.getFechaLimite() != null) {
            Duration duracion = Duration.between(OffsetDateTime.now(), e.getFechaLimite());
            diasRestantes = duracion.toDays();
            vencido = OffsetDateTime.now().isAfter(e.getFechaLimite());
        }

        return AtencionItemResponse.builder()
                .id(e.getId())
                .uuid(e.getUuid())
                .numeroExpediente(e.getNumeroExpediente())
                .asunto(e.getAsunto())
                .tipoTramite(e.getTipoTramite() != null ? e.getTipoTramite().getNombre() : "General")
                .solicitanteNombre(e.getSolicitante() != null ? e.getSolicitante().getNombreCompleto() : "Ciudadano")
                .solicitanteDocumento(e.getSolicitante() != null ? e.getSolicitante().getNumeroDocumento() : "")
                .oficinaId(e.getOficinaResponsable() != null ? e.getOficinaResponsable().getId() : null)
                .oficinaNombre(e.getOficinaResponsable() != null ? e.getOficinaResponsable().getNombre() : "Sin Oficina")
                .responsableId(e.getResponsableActual() != null ? e.getResponsableActual().getId() : null)
                .responsableNombre(e.getResponsableActual() != null ? e.getResponsableActual().getNombreCompleto() : "Sin Asignar")
                .estado(e.getEstadoActual())
                .prioridad(e.getPrioridad())
                .fechaPresentacion(e.getFechaPresentacion())
                .fechaRecepcion(e.getFechaRecepcion())
                .fechaInicioAtencion(e.getFechaInicioAtencion())
                .fechaLimite(e.getFechaLimite())
                .diasRestantes(diasRestantes)
                .vencido(vencido)
                .reabierto(Boolean.TRUE.equals(e.getEsReabierto()))
                .build();
    }

    private Usuario obtenerUsuario(String username) {
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));
    }

    private boolean esAdmin(Usuario usuario) {
        return usuario.getRoles().stream()
                .anyMatch(r -> r.getCodigo().equals("ROLE_ADMIN") || r.getCodigo().equals("ROLE_SUPERADMIN"));
    }

    private void validarAccesoConsulta(Usuario usuario, Expediente expediente) {
        if (esAdmin(usuario)) return;
        // Permitir si es responsable actual o pertenece a la oficina responsable
        boolean esResponsable = expediente.getResponsableActual() != null &&
                expediente.getResponsableActual().getId().equals(usuario.getId());
        if (esResponsable) return;

        boolean perteneceOficina = expediente.getOficinaResponsable() != null &&
                personaOficinaRepository.findByPersonaIdAndEstado(usuario.getPersona().getId(), "ACTIVO")
                        .stream()
                        .anyMatch(po -> po.getOficina().getId().equals(expediente.getOficinaResponsable().getId()));

        if (!perteneceOficina) {
            throw new AccessDeniedException("No tiene permisos para consultar este expediente en atención.");
        }
    }
}
