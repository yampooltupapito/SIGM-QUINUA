package pe.gob.municipalidad.sigm.modules.mesapartes.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.CierreExpediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.DecisionExpediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.Expediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoExpediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.TipoCierreExpediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.CierreExpedienteRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.DecisionExpedienteRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.ExpedienteRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.CierreExpedienteService;
import pe.gob.municipalidad.sigm.modules.mesapartes.workflow.ExpedienteWorkflowService;

import java.time.OffsetDateTime;

@Service
public class CierreExpedienteServiceImpl implements CierreExpedienteService {

    private static final Logger log = LoggerFactory.getLogger(CierreExpedienteServiceImpl.class);

    private final CierreExpedienteRepository cierreRepository;
    private final ExpedienteRepository expedienteRepository;
    private final DecisionExpedienteRepository decisionRepository;
    private final ExpedienteWorkflowService workflowService;
    private final UsuarioRepository usuarioRepository;
    private final PersonaOficinaRepository personaOficinaRepository;

    public CierreExpedienteServiceImpl(
            CierreExpedienteRepository cierreRepository,
            ExpedienteRepository expedienteRepository,
            DecisionExpedienteRepository decisionRepository,
            ExpedienteWorkflowService workflowService,
            UsuarioRepository usuarioRepository,
            PersonaOficinaRepository personaOficinaRepository) {
        this.cierreRepository = cierreRepository;
        this.expedienteRepository = expedienteRepository;
        this.decisionRepository = decisionRepository;
        this.workflowService = workflowService;
        this.usuarioRepository = usuarioRepository;
        this.personaOficinaRepository = personaOficinaRepository;
    }

    @Override
    @Transactional
    public CierreResponse cerrarExpediente(Long expedienteId, CerrarExpedienteRequest request, String username) {
        Usuario usuario = obtenerUsuario(username);
        Expediente expediente = expedienteRepository.findByIdWithDetalles(expedienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Expediente no encontrado con ID: " + expedienteId));

        Oficina oficina = expediente.getOficinaResponsable();
        if (oficina == null) {
            throw new BusinessException("El expediente no tiene oficina responsable asociada.");
        }

        validarCompetenciaCierre(usuario, oficina.getId());

        // Reglas de cierre administrativo según LPAG (Ley 27444)
        if (request.getTipoCierre() == TipoCierreExpediente.ATENCION_CONCLUIDA) {
            if (expediente.getEstadoActual() != EstadoExpediente.RESUELTO && expediente.getEstadoActual() != EstadoExpediente.NOTIFICADO) {
                throw new BusinessException("Para cerrar por atención concluida, el expediente debe encontrarse formalmente en estado RESUELTO o NOTIFICADO.");
            }
        }

        DecisionExpediente decision = null;
        if (request.getDecisionId() != null) {
            decision = decisionRepository.findById(request.getDecisionId()).orElse(null);
        }

        CierreExpediente cierre = CierreExpediente.builder()
                .expediente(expediente)
                .decision(decision)
                .usuarioCierre(usuario)
                .oficinaCierre(oficina)
                .motivoCierre(request.getMotivoCierre().trim())
                .tipoCierre(request.getTipoCierre())
                .fechaCierre(OffsetDateTime.now())
                .createdBy(usuario.getUsername())
                .build();

        CierreExpediente guardado = cierreRepository.save(cierre);

        expediente.setFechaCierre(OffsetDateTime.now());
        expedienteRepository.save(expediente);

        log.info("Expediente {} cerrado por motivo: {} (tipo: {})", expediente.getNumeroExpediente(), request.getMotivoCierre(), request.getTipoCierre());

        return mapearAResponse(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public CierreResponse obtenerCierre(Long expedienteId, String username) {
        return cierreRepository.findFirstByExpedienteIdOrderByFechaCierreDesc(expedienteId)
                .map(this::mapearAResponse)
                .orElse(null);
    }

    private CierreResponse mapearAResponse(CierreExpediente c) {
        return CierreResponse.builder()
                .id(c.getId())
                .expedienteId(c.getExpediente().getId())
                .numeroExpediente(c.getExpediente().getNumeroExpediente())
                .decisionId(c.getDecision() != null ? c.getDecision().getId() : null)
                .usuarioCierreId(c.getUsuarioCierre().getId())
                .usuarioCierreNombre(c.getUsuarioCierre().getNombreCompleto())
                .oficinaCierreId(c.getOficinaCierre().getId())
                .oficinaCierreNombre(c.getOficinaCierre().getNombre())
                .motivoCierre(c.getMotivoCierre())
                .tipoCierre(c.getTipoCierre())
                .fechaCierre(c.getFechaCierre())
                .build();
    }

    private Usuario obtenerUsuario(String username) {
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));
    }

    private void validarCompetenciaCierre(Usuario usuario, Long oficinaId) {
        boolean esAdmin = usuario.getRoles().stream().anyMatch(r -> r.getCodigo().equals("ROLE_SUPERADMIN") || r.getCodigo().equals("ROLE_ADMIN"));
        if (esAdmin) return;

        boolean trabajaEnOficina = personaOficinaRepository.findByPersonaIdAndEstado(usuario.getPersona().getId(), "ACTIVO")
                .stream()
                .anyMatch(po -> po.getOficina().getId().equals(oficinaId));

        if (!trabajaEnOficina) {
            throw new AccessDeniedException("No posee autorización funcional para registrar el cierre administrativo de este expediente.");
        }
    }
}
