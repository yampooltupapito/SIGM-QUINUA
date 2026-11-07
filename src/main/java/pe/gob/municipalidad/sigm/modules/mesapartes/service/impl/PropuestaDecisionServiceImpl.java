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
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.PropuestaDecision;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoPropuesta;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.ExpedienteRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.PropuestaDecisionRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.PropuestaDecisionService;
import pe.gob.municipalidad.sigm.common.notification.dto.NotificationDtos.EnviarNotificacionRequest;
import pe.gob.municipalidad.sigm.common.notification.service.CentralNotificationService;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PropuestaDecisionServiceImpl implements PropuestaDecisionService {

    private static final Logger log = LoggerFactory.getLogger(PropuestaDecisionServiceImpl.class);

    private final PropuestaDecisionRepository propuestaRepository;
    private final ExpedienteRepository expedienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final PersonaOficinaRepository personaOficinaRepository;
    private final CentralNotificationService notificationService;

    public PropuestaDecisionServiceImpl(
            PropuestaDecisionRepository propuestaRepository,
            ExpedienteRepository expedienteRepository,
            UsuarioRepository usuarioRepository,
            PersonaOficinaRepository personaOficinaRepository,
            CentralNotificationService notificationService) {
        this.propuestaRepository = propuestaRepository;
        this.expedienteRepository = expedienteRepository;
        this.usuarioRepository = usuarioRepository;
        this.personaOficinaRepository = personaOficinaRepository;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public PropuestaResponse crearPropuesta(Long expedienteId, CrearPropuestaRequest request, String username) {
        Usuario usuario = obtenerUsuario(username);
        Expediente expediente = expedienteRepository.findByIdWithDetalles(expedienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Expediente no encontrado con ID: " + expedienteId));

        Oficina oficina = expediente.getOficinaResponsable();
        if (oficina == null) {
            throw new BusinessException("El expediente no tiene asignada una oficina responsable.");
        }

        validarAccesoTrabajadorOAdmin(usuario, oficina.getId());

        PropuestaDecision propuesta = PropuestaDecision.builder()
                .expediente(expediente)
                .tipoDecision(request.getTipoDecision())
                .resultadoPropuesto(request.getResultadoPropuesto())
                .fundamento(request.getFundamento().trim())
                .descripcion(request.getDescripcion().trim())
                .usuarioElaborador(usuario)
                .oficina(oficina)
                .estado(EstadoPropuesta.BORRADOR)
                .fechaCreacion(OffsetDateTime.now())
                .createdBy(usuario.getUsername())
                .build();

        PropuestaDecision guardada = propuestaRepository.save(propuesta);
        log.info("Propuesta de decisión #{} creada para expediente {}", guardada.getId(), expediente.getNumeroExpediente());

        return mapearAResponse(guardada);
    }

    @Override
    @Transactional
    public PropuestaResponse enviarARevision(Long propuestaId, String username) {
        Usuario usuario = obtenerUsuario(username);
        PropuestaDecision propuesta = propuestaRepository.findById(propuestaId)
                .orElseThrow(() -> new ResourceNotFoundException("Propuesta no encontrada con ID: " + propuestaId));

        if (propuesta.getEstado() != EstadoPropuesta.BORRADOR && propuesta.getEstado() != EstadoPropuesta.OBSERVADA) {
            throw new BusinessException("Solo pueden enviarse a revisión propuestas en estado BORRADOR u OBSERVADA.");
        }

        boolean esAutor = propuesta.getUsuarioElaborador().getId().equals(usuario.getId());
        if (!esAutor && !esAdmin(usuario)) {
            throw new AccessDeniedException("Solo el proyectista o un administrador pueden remitir la propuesta a revisión.");
        }

        propuesta.setEstado(EstadoPropuesta.EN_REVISION);
        propuesta.setFechaEnvioRevision(OffsetDateTime.now());
        propuesta.setUpdatedBy(usuario.getUsername());

        PropuestaDecision actualizada = propuestaRepository.save(propuesta);

        // Notificar al proyectista / oficina
        try {
            notificationService.enviarNotificacion(
                    EnviarNotificacionRequest.builder()
                            .usuarioId(usuario.getId())
                            .titulo("Propuesta enviada a revisión")
                            .mensaje("La propuesta #" + propuestaId + " para el expediente " + propuesta.getExpediente().getNumeroExpediente() + " fue elevada a revisión.")
                            .tipo("APROBACION")
                            .referenciaId(propuesta.getExpediente().getNumeroExpediente())
                            .urlDestino("/dashboard/mesa-partes/propuestas")
                            .build(),
                    usuario.getUsername()
            );
        } catch (Exception e) {
            log.warn("No se pudo emitir notificación de envío de propuesta: {}", e.getMessage());
        }

        return mapearAResponse(actualizada);
    }

    @Override
    @Transactional
    public PropuestaResponse cancelarPropuesta(Long propuestaId, String motivo, String username) {
        Usuario usuario = obtenerUsuario(username);
        PropuestaDecision propuesta = propuestaRepository.findById(propuestaId)
                .orElseThrow(() -> new ResourceNotFoundException("Propuesta no encontrada con ID: " + propuestaId));

        if (propuesta.getEstado() == EstadoPropuesta.APROBADA_INTERNAMENTE) {
            throw new BusinessException("No es posible cancelar una propuesta que ya ha sido aprobada internamente.");
        }

        propuesta.setEstado(EstadoPropuesta.CANCELADA);
        propuesta.setUpdatedBy(usuario.getUsername());
        PropuestaDecision actualizada = propuestaRepository.save(propuesta);

        return mapearAResponse(actualizada);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PropuestaResponse> listarPropuestasPorExpediente(Long expedienteId, String username) {
        return propuestaRepository.findByExpedienteIdOrderByFechaCreacionDesc(expedienteId)
                .stream()
                .map(this::mapearAResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PropuestaResponse> listarBandejaPropuestas(Long oficinaId, EstadoPropuesta estado, Pageable pageable, String username) {
        Usuario usuario = obtenerUsuario(username);
        boolean isAdmin = esAdmin(usuario);

        Long oficinaFiltro = oficinaId;
        if (!isAdmin && oficinaFiltro == null) {
            // Asignar primera oficina activa del usuario
            oficinaFiltro = personaOficinaRepository.findByPersonaIdAndEstado(usuario.getPersona().getId(), "ACTIVO")
                    .stream()
                    .map(po -> po.getOficina().getId())
                    .findFirst()
                    .orElse(null);
        }

        Page<PropuestaDecision> page;
        if (oficinaFiltro != null && estado != null) {
            page = propuestaRepository.findByOficinaIdAndEstado(oficinaFiltro, estado, pageable);
        } else if (oficinaFiltro != null) {
            page = propuestaRepository.findByOficinaId(oficinaFiltro, pageable);
        } else if (estado != null) {
            page = propuestaRepository.findByEstado(estado, pageable);
        } else {
            page = propuestaRepository.findAll(pageable);
        }

        return page.map(this::mapearAResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PropuestaResponse obtenerPorId(Long propuestaId, String username) {
        PropuestaDecision propuesta = propuestaRepository.findById(propuestaId)
                .orElseThrow(() -> new ResourceNotFoundException("Propuesta no encontrada con ID: " + propuestaId));
        return mapearAResponse(propuesta);
    }

    private PropuestaResponse mapearAResponse(PropuestaDecision p) {
        List<RevisionPropuestaResponse> revs = p.getRevisiones().stream()
                .map(r -> RevisionPropuestaResponse.builder()
                        .id(r.getId())
                        .propuestaId(p.getId())
                        .usuarioRevisorId(r.getUsuarioRevisor().getId())
                        .usuarioRevisorNombre(r.getUsuarioRevisor().getNombreCompleto())
                        .oficinaId(r.getOficina().getId())
                        .oficinaNombre(r.getOficina().getNombre())
                        .accion(r.getAccion())
                        .observaciones(r.getObservaciones())
                        .fechaRevision(r.getFechaRevision())
                        .build())
                .collect(Collectors.<RevisionPropuestaResponse>toList());

        return PropuestaResponse.builder()
                .id(p.getId())
                .expedienteId(p.getExpediente().getId())
                .numeroExpediente(p.getExpediente().getNumeroExpediente())
                .tipoDecision(p.getTipoDecision())
                .resultadoPropuesto(p.getResultadoPropuesto())
                .fundamento(p.getFundamento())
                .descripcion(p.getDescripcion())
                .usuarioElaboradorId(p.getUsuarioElaborador().getId())
                .usuarioElaboradorNombre(p.getUsuarioElaborador().getNombreCompleto())
                .oficinaId(p.getOficina().getId())
                .oficinaNombre(p.getOficina().getNombre())
                .estado(p.getEstado())
                .fechaCreacion(p.getFechaCreacion())
                .fechaEnvioRevision(p.getFechaEnvioRevision())
                .fechaResolucionRevision(p.getFechaResolucionRevision())
                .version(p.getVersion())
                .revisiones(revs)
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

    private void validarAccesoTrabajadorOAdmin(Usuario usuario, Long oficinaId) {
        if (esAdmin(usuario)) return;
        boolean activoEnOficina = personaOficinaRepository.findByPersonaIdAndEstado(usuario.getPersona().getId(), "ACTIVO")
                .stream()
                .anyMatch(po -> po.getOficina().getId().equals(oficinaId));
        if (!activoEnOficina) {
            throw new AccessDeniedException("No pertenece a la dependencia correspondiente.");
        }
    }
}
