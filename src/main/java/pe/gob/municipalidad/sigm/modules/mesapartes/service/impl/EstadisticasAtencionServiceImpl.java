package pe.gob.municipalidad.sigm.modules.mesapartes.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.AtencionDtos.EstadisticasAtencionResponse;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoEmisionDocumento;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoEvaluacion;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoExpediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoPropuesta;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.EstadisticasAtencionService;

@Service
public class EstadisticasAtencionServiceImpl implements EstadisticasAtencionService {

    private final ExpedienteRepository expedienteRepository;
    private final EvaluacionExpedienteRepository evaluacionRepository;
    private final PropuestaDecisionRepository propuestaRepository;
    private final DecisionExpedienteRepository decisionRepository;
    private final NotificacionAdministrativaRepository notificacionRepository;
    private final CierreExpedienteRepository cierreRepository;
    private final ArchivoExpedienteRepository archivoRepository;
    private final UsuarioRepository usuarioRepository;

    public EstadisticasAtencionServiceImpl(
            ExpedienteRepository expedienteRepository,
            EvaluacionExpedienteRepository evaluacionRepository,
            PropuestaDecisionRepository propuestaRepository,
            DecisionExpedienteRepository decisionRepository,
            NotificacionAdministrativaRepository notificacionRepository,
            CierreExpedienteRepository cierreRepository,
            ArchivoExpedienteRepository archivoRepository,
            UsuarioRepository usuarioRepository) {
        this.expedienteRepository = expedienteRepository;
        this.evaluacionRepository = evaluacionRepository;
        this.propuestaRepository = propuestaRepository;
        this.decisionRepository = decisionRepository;
        this.notificacionRepository = notificacionRepository;
        this.cierreRepository = cierreRepository;
        this.archivoRepository = archivoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public EstadisticasAtencionResponse obtenerEstadisticasAtencion(Long oficinaId, String username) {
        long enAtencion = (oficinaId != null)
                ? expedienteRepository.countByOficinaResponsableIdAndEstadoActual(oficinaId, EstadoExpediente.EN_ATENCION)
                : expedienteRepository.countByEstadoActual(EstadoExpediente.EN_ATENCION);

        long resueltos = (oficinaId != null)
                ? expedienteRepository.countByOficinaResponsableIdAndEstadoActual(oficinaId, EstadoExpediente.RESUELTO)
                : expedienteRepository.countByEstadoActual(EstadoExpediente.RESUELTO);

        long archivados = (oficinaId != null)
                ? expedienteRepository.countByOficinaResponsableIdAndEstadoActual(oficinaId, EstadoExpediente.ARCHIVADO)
                : expedienteRepository.countByEstadoActual(EstadoExpediente.ARCHIVADO);

        long evalPend = evaluacionRepository.countByEstadoAndOficina(EstadoEvaluacion.BORRADOR, oficinaId);
        long propRev = propuestaRepository.countByEstadoAndOficina(EstadoPropuesta.EN_REVISION, oficinaId);
        long docFirma = decisionRepository.countByEstadoEmisionAndOficina(EstadoEmisionDocumento.PENDIENTE_FIRMA, oficinaId);
        long notifPend = notificacionRepository.countPendientesNotificacion();
        long cerrados = cierreRepository.countCierres(oficinaId);

        return EstadisticasAtencionResponse.builder()
                .expedientesEnAtencion(enAtencion)
                .evaluacionesPendientes(evalPend)
                .propuestasEnRevision(propRev)
                .documentosPendientesFirma(docFirma)
                .expedientesResueltos(resueltos)
                .notificacionesPendientes(notifPend)
                .expedientesCerrados(cerrados)
                .expedientesArchivados(archivados)
                .build();
    }
}
