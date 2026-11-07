package pe.gob.municipalidad.sigm.modules.mesapartes.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.PersonaOficinaRepository;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.AtencionDtos.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.PropuestaDecision;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.RevisionPropuesta;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.AccionRevisionPropuesta;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoPropuesta;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.PropuestaDecisionRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.RevisionPropuestaRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.PropuestaDecisionService;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.RevisionDecisionService;
import pe.gob.municipalidad.sigm.common.notification.dto.NotificationDtos.EnviarNotificacionRequest;
import pe.gob.municipalidad.sigm.common.notification.service.CentralNotificationService;

import java.time.OffsetDateTime;

@Service
public class RevisionDecisionServiceImpl implements RevisionDecisionService {

    private static final Logger log = LoggerFactory.getLogger(RevisionDecisionServiceImpl.class);

    private final PropuestaDecisionRepository propuestaRepository;
    private final RevisionPropuestaRepository revisionPropuestaRepository;
    private final PropuestaDecisionService propuestaService;
    private final UsuarioRepository usuarioRepository;
    private final PersonaOficinaRepository personaOficinaRepository;
    private final CentralNotificationService notificationService;

    public RevisionDecisionServiceImpl(
            PropuestaDecisionRepository propuestaRepository,
            RevisionPropuestaRepository revisionPropuestaRepository,
            PropuestaDecisionService propuestaService,
            UsuarioRepository usuarioRepository,
            PersonaOficinaRepository personaOficinaRepository,
            CentralNotificationService notificationService) {
        this.propuestaRepository = propuestaRepository;
        this.revisionPropuestaRepository = revisionPropuestaRepository;
        this.propuestaService = propuestaService;
        this.usuarioRepository = usuarioRepository;
        this.personaOficinaRepository = personaOficinaRepository;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public PropuestaResponse revisarPropuesta(Long propuestaId, RevisarPropuestaRequest request, String username) {
        Usuario revisor = obtenerUsuario(username);
        PropuestaDecision propuesta = propuestaRepository.findById(propuestaId)
                .orElseThrow(() -> new ResourceNotFoundException("Propuesta no encontrada con ID: " + propuestaId));

        if (propuesta.getEstado() != EstadoPropuesta.EN_REVISION) {
            throw new BusinessException("Solo es posible evaluar propuestas que se encuentren en estado EN_REVISION.");
        }

        // REGLA CRÍTICA: Separación de Funciones (Sección 12 de Prompt 16)
        if (propuesta.getUsuarioElaborador().getId().equals(revisor.getId()) && !esSuperAdmin(revisor)) {
            throw new BusinessException("Principio de segregación de funciones: el servidor que elaboró la propuesta no puede revisarla ni aprobarla a sí mismo.");
        }

        validarCompetenciaRevision(revisor, propuesta.getOficina().getId());

        // Registrar revisión histórica
        RevisionPropuesta rev = RevisionPropuesta.builder()
                .propuesta(propuesta)
                .usuarioRevisor(revisor)
                .oficina(propuesta.getOficina())
                .accion(request.getAccion())
                .observaciones(request.getObservaciones().trim())
                .fechaRevision(OffsetDateTime.now())
                .build();
        revisionPropuestaRepository.save(rev);

        // Actualizar estado de la propuesta
        if (request.getAccion() == AccionRevisionPropuesta.APROBADA_INTERNAMENTE) {
            propuesta.setEstado(EstadoPropuesta.APROBADA_INTERNAMENTE);
            propuesta.setFechaResolucionRevision(OffsetDateTime.now());
        } else if (request.getAccion() == AccionRevisionPropuesta.DEVUELTA_CON_OBSERVACIONES) {
            propuesta.setEstado(EstadoPropuesta.OBSERVADA);
        } else if (request.getAccion() == AccionRevisionPropuesta.RECHAZADA) {
            propuesta.setEstado(EstadoPropuesta.RECHAZADA);
            propuesta.setFechaResolucionRevision(OffsetDateTime.now());
        }

        propuesta.setUpdatedBy(revisor.getUsername());
        propuestaRepository.save(propuesta);

        // Notificar al proyectista
        try {
            String titulo = (request.getAccion() == AccionRevisionPropuesta.APROBADA_INTERNAMENTE)
                    ? "Propuesta Aprobada Internamente" : "Propuesta Observada / Devuelta";
            notificationService.enviarNotificacion(
                    EnviarNotificacionRequest.builder()
                            .usuarioId(propuesta.getUsuarioElaborador().getId())
                            .titulo(titulo)
                            .mensaje("Su propuesta para el expediente " + propuesta.getExpediente().getNumeroExpediente() + " fue evaluada con resultado: " + request.getAccion())
                            .tipo("APROBACION")
                            .referenciaId(propuesta.getExpediente().getNumeroExpediente())
                            .urlDestino("/dashboard/mesa-partes/atencion/" + propuesta.getExpediente().getId())
                            .build(),
                    revisor.getUsername()
            );
        } catch (Exception e) {
            log.warn("No se pudo emitir notificación de revisión: {}", e.getMessage());
        }

        return propuestaService.obtenerPorId(propuestaId, username);
    }

    private Usuario obtenerUsuario(String username) {
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));
    }

    private boolean esSuperAdmin(Usuario usuario) {
        return usuario.getRoles().stream().anyMatch(r -> r.getCodigo().equals("ROLE_SUPERADMIN"));
    }

    private void validarCompetenciaRevision(Usuario usuario, Long oficinaId) {
        if (esSuperAdmin(usuario)) return;
        boolean trabajaEnOficina = personaOficinaRepository.findByPersonaIdAndEstado(usuario.getPersona().getId(), "ACTIVO")
                .stream()
                .anyMatch(po -> po.getOficina().getId().equals(oficinaId));
        if (!trabajaEnOficina) {
            throw new AccessDeniedException("No tiene competencia de revisión en la dependencia titular del expediente.");
        }
    }
}
