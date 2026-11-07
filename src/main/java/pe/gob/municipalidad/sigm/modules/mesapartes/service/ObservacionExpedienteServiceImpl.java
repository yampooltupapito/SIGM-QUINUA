package pe.gob.municipalidad.sigm.modules.mesapartes.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.common.model.Rol;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvAdminDtos.RegistrarObservacionAdminRequest;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvDtos.ObservacionResponse;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.Expediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.ObservacionExpediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.RequisitoDocumental;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.mapper.MpvMapper;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.ExpedienteRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.ObservacionExpedienteRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.RequisitoDocumentalRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.workflow.ExpedienteWorkflowService;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ObservacionExpedienteServiceImpl implements ObservacionExpedienteService {

    private final ObservacionExpedienteRepository observacionRepository;
    private final ExpedienteRepository expedienteRepository;
    private final RequisitoDocumentalRepository requisitoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ExpedienteWorkflowService workflowService;
    private final PlazoAdministrativoService plazoService;
    private final MpvMapper mpvMapper;

    public ObservacionExpedienteServiceImpl(ObservacionExpedienteRepository observacionRepository,
                                            ExpedienteRepository expedienteRepository,
                                            RequisitoDocumentalRepository requisitoRepository,
                                            UsuarioRepository usuarioRepository,
                                            ExpedienteWorkflowService workflowService,
                                            PlazoAdministrativoService plazoService,
                                            MpvMapper mpvMapper) {
        this.observacionRepository = observacionRepository;
        this.expedienteRepository = expedienteRepository;
        this.requisitoRepository = requisitoRepository;
        this.usuarioRepository = usuarioRepository;
        this.workflowService = workflowService;
        this.plazoService = plazoService;
        this.mpvMapper = mpvMapper;
    }

    @Override
    @Transactional
    public ObservacionResponse registrarObservacion(Long expedienteId, RegistrarObservacionAdminRequest request, String username) {
        Usuario usuario = obtenerUsuario(username);

        if (esCiudadano(usuario)) {
            throw new AccessDeniedException("Operación no autorizada: los ciudadanos no pueden formular observaciones administrativas.");
        }

        Expediente expediente = expedienteRepository.findByIdWithDetalles(expedienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Expediente no encontrado con ID: " + expedienteId));

        OffsetDateTime ahora = OffsetDateTime.now();
        OffsetDateTime fechaLimite = null;

        RequisitoDocumental req = null;
        if (request.getRequisitoId() != null) {
            req = requisitoRepository.findById(request.getRequisitoId()).orElse(null);
        }

        boolean solicitaSub = Boolean.TRUE.equals(request.getSolicitarSubsanacion())
                && request.getVisibilidad() == VisibilidadObservacion.CIUDADANO;

        if (solicitaSub) {
            int dias = (request.getDiasPlazoSubsanacion() != null && request.getDiasPlazoSubsanacion() > 0)
                    ? request.getDiasPlazoSubsanacion() : 10;
            fechaLimite = plazoService.calcularFechaLimite(ahora, dias, UnidadPlazo.DIAS_HABILES);
        }

        ObservacionExpediente obs = ObservacionExpediente.builder()
                .expediente(expediente)
                .tipoObservacion(request.getTipoObservacion() != null ? request.getTipoObservacion() : TipoObservacionExpediente.FORMA)
                .visibilidad(request.getVisibilidad() != null ? request.getVisibilidad() : VisibilidadObservacion.CIUDADANO)
                .requisito(req)
                .descripcion(request.getDescripcion().trim())
                .usuarioObservador(usuario)
                .fechaRegistro(ahora)
                .fechaNotificacion(request.getVisibilidad() == VisibilidadObservacion.CIUDADANO ? ahora : null)
                .fechaLimite(fechaLimite)
                .estado(EstadoObservacion.PENDIENTE)
                .build();

        ObservacionExpediente guardada = observacionRepository.save(obs);

        if (solicitaSub && expediente.getEstadoActual() != EstadoExpediente.OBSERVADO) {
            if (workflowService.esTransicionPermitida(expediente.getEstadoActual(), EstadoExpediente.OBSERVADO)) {
                workflowService.transicionar(
                        expediente,
                        EstadoExpediente.OBSERVADO,
                        "OBSERVACION_EXPEDIENTE",
                        request.getDescripcion().trim(),
                        usuario,
                        expediente.getOficinaResponsable()
                );
            }
        }

        return mpvMapper.toObservacionResponse(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ObservacionResponse> listarObservaciones(Long expedienteId, String username) {
        Usuario usuario = obtenerUsuario(username);
        Expediente expediente = expedienteRepository.findById(expedienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Expediente no encontrado con ID: " + expedienteId));

        if (esCiudadano(usuario)) {
            // Anti-IDOR: verificar propiedad
            if (expediente.getSolicitante() == null || usuario.getPersona() == null
                    || !expediente.getSolicitante().getId().equals(usuario.getPersona().getId())) {
                throw new AccessDeniedException("No tiene permisos para ver observaciones de este expediente.");
            }
            // Los administrados solo pueden ver observaciones dirigidas a ellos (visibilidad = CIUDADANO)
            return observacionRepository.findByExpedienteIdAndVisibilidadOrderByFechaRegistroDesc(expedienteId, VisibilidadObservacion.CIUDADANO)
                    .stream()
                    .map(mpvMapper::toObservacionResponse)
                    .collect(Collectors.toList());
        }

        // Operadores y administradores ven todas las observaciones (internas y ciudadanas)
        return observacionRepository.findByExpedienteIdOrderByFechaRegistroDesc(expedienteId)
                .stream()
                .map(mpvMapper::toObservacionResponse)
                .collect(Collectors.toList());
    }

    private Usuario obtenerUsuario(String username) {
        return usuarioRepository.findByUsernameWithRolesAndPersona(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));
    }

    private boolean esCiudadano(Usuario usuario) {
        if (usuario.getRoles() == null || usuario.getRoles().isEmpty()) return true;
        boolean esCiu = usuario.getRoles().stream()
                .anyMatch(r -> (r.getCodigo() != null && r.getCodigo().equalsIgnoreCase("ROLE_CIUDADANO"))
                        || (r.getNombre() != null && r.getNombre().toUpperCase().contains("CIUDADANO")));
        boolean esFuncionario = usuario.getRoles().stream()
                .anyMatch(r -> {
                    String cod = r.getCodigo() != null ? r.getCodigo().toUpperCase() : "";
                    String nom = r.getNombre() != null ? r.getNombre().toUpperCase() : "";
                    return cod.contains("ADMIN") || cod.contains("OPERADOR") || cod.contains("MESA") || cod.contains("JEFE")
                            || nom.startsWith("ADMINISTRADOR") || nom.contains("OPERADOR") || nom.contains("MESA") || nom.contains("JEFE");
                });
        return esCiu && !esFuncionario;
    }
}
