package pe.gob.municipalidad.sigm.modules.mesapartes.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.common.model.PersonaOficina;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.notification.dto.NotificationDtos.EnviarNotificacionRequest;
import pe.gob.municipalidad.sigm.common.notification.service.CentralNotificationService;
import pe.gob.municipalidad.sigm.common.repository.PersonaOficinaRepository;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.DerivacionDtos.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvDtos.ExpedienteResponse;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.AsignacionExpediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.Expediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.ExpedienteHistorial;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoAsignacion;
import pe.gob.municipalidad.sigm.modules.mesapartes.mapper.DerivacionMapper;
import pe.gob.municipalidad.sigm.modules.mesapartes.mapper.MpvMapper;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.AsignacionExpedienteRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.ExpedienteHistorialRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.ExpedienteRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.AsignacionExpedienteService;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AsignacionExpedienteServiceImpl implements AsignacionExpedienteService {

    private final AsignacionExpedienteRepository asignacionRepository;
    private final ExpedienteRepository expedienteRepository;
    private final ExpedienteHistorialRepository historialRepository;
    private final UsuarioRepository usuarioRepository;
    private final PersonaOficinaRepository personaOficinaRepository;
    private final CentralNotificationService notificationService;
    private final DerivacionMapper derivacionMapper;
    private final MpvMapper mpvMapper;

    public AsignacionExpedienteServiceImpl(
            AsignacionExpedienteRepository asignacionRepository,
            ExpedienteRepository expedienteRepository,
            ExpedienteHistorialRepository historialRepository,
            UsuarioRepository usuarioRepository,
            PersonaOficinaRepository personaOficinaRepository,
            CentralNotificationService notificationService,
            DerivacionMapper derivacionMapper,
            MpvMapper mpvMapper) {
        this.asignacionRepository = asignacionRepository;
        this.expedienteRepository = expedienteRepository;
        this.historialRepository = historialRepository;
        this.usuarioRepository = usuarioRepository;
        this.personaOficinaRepository = personaOficinaRepository;
        this.notificationService = notificationService;
        this.derivacionMapper = derivacionMapper;
        this.mpvMapper = mpvMapper;
    }

    @Override
    @Transactional
    public AsignacionResponse asignarResponsable(Long expedienteId, AsignarExpedienteRequest request, String username) {
        Usuario usuarioAsignador = obtenerUsuario(username);
        Expediente expediente = expedienteRepository.findByIdWithDetalles(expedienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Expediente no encontrado con ID: " + expedienteId));

        Oficina oficina = expediente.getOficinaResponsable();
        if (oficina == null) {
            throw new BusinessException("El expediente no tiene oficina responsable asignada.");
        }

        validarPertenenciaOAdmin(usuarioAsignador, oficina.getId());

        Usuario trabajadorAsignado = usuarioRepository.findById(request.getUsuarioAsignadoId())
                .orElseThrow(() -> new ResourceNotFoundException("Trabajador no encontrado con ID: " + request.getUsuarioAsignadoId()));

        validarTrabajadorActivoEnOficina(trabajadorAsignado, oficina.getId());

        OffsetDateTime ahora = OffsetDateTime.now();

        // Si ya tenía asignación activa previa, se cierra
        Usuario usuarioAnterior = expediente.getResponsableActual();
        Optional<AsignacionExpediente> asignacionPrevia = asignacionRepository.findFirstByExpedienteIdAndEstado(expedienteId, EstadoAsignacion.ACTIVO);
        asignacionPrevia.ifPresent(prev -> {
            prev.setEstado(EstadoAsignacion.REASIGNADO);
            prev.setFechaFin(ahora);
            prev.setUpdatedBy(usuarioAsignador.getUsername());
            asignacionRepository.save(prev);
        });

        AsignacionExpediente nueva = AsignacionExpediente.builder()
                .expediente(expediente)
                .oficina(oficina)
                .usuarioAsignador(usuarioAsignador)
                .usuarioAsignado(trabajadorAsignado)
                .usuarioAnterior(usuarioAnterior)
                .motivo(request.getMotivo() != null ? request.getMotivo().trim() : "Asignación inicial de atención")
                .indicaciones(request.getIndicaciones() != null ? request.getIndicaciones().trim() : null)
                .fechaAsignacion(ahora)
                .estado(EstadoAsignacion.ACTIVO)
                .createdBy(usuarioAsignador.getUsername())
                .build();

        AsignacionExpediente guardada = asignacionRepository.save(nueva);

        expediente.setResponsableActual(trabajadorAsignado);
        expedienteRepository.save(expediente);

        // Registro de historial inmutable
        String nombreTrabajador = trabajadorAsignado.getPersona() != null ? trabajadorAsignado.getPersona().getNombreCompleto() : trabajadorAsignado.getUsername();
        historialRepository.save(ExpedienteHistorial.builder()
                .expediente(expediente)
                .estadoAnterior(expediente.getEstadoActual())
                .estadoNuevo(expediente.getEstadoActual())
                .accion("RESPONSABLE_ASIGNADO")
                .observacion("Asignado a: " + nombreTrabajador + (request.getMotivo() != null ? ". Motivo: " + request.getMotivo() : ""))
                .usuarioResponsable(usuarioAsignador)
                .oficina(oficina)
                .fechaMovimiento(ahora)
                .identificadorCorrelacion(UUID.randomUUID().toString())
                .build());

        // Notificación al trabajador asignado
        try {
            notificationService.enviarNotificacion(
                    EnviarNotificacionRequest.builder()
                            .usuarioId(trabajadorAsignado.getId())
                            .titulo("Expediente Asignado: " + expediente.getNumeroExpediente())
                            .mensaje("Se le ha asignado el expediente '" + expediente.getAsunto() +
                                    "' para su atención. Indicaciones: " + (request.getIndicaciones() != null ? request.getIndicaciones() : "Sin indicaciones adicionales."))
                            .tipo("TRAMITE")
                            .referenciaId(expediente.getNumeroExpediente())
                            .urlDestino("/dashboard/mesa-partes/expedientes/" + expediente.getId())
                            .enviarCorreo(true)
                            .build(),
                    usuarioAsignador.getUsername()
            );
        } catch (Exception ex) {
            log.warn("Error enviando notificación de asignación: {}", ex.getMessage());
        }

        return derivacionMapper.toAsignacionResponse(guardada);
    }

    @Override
    @Transactional
    public AsignacionResponse reasignarResponsable(Long expedienteId, ReasignarExpedienteRequest request, String username) {
        if (request.getMotivo() == null || request.getMotivo().trim().isBlank()) {
            throw new BusinessException("El motivo de reasignación es estrictamente obligatorio.");
        }

        Usuario usuarioAsignador = obtenerUsuario(username);
        Expediente expediente = expedienteRepository.findByIdWithDetalles(expedienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Expediente no encontrado con ID: " + expedienteId));

        Oficina oficina = expediente.getOficinaResponsable();
        if (oficina == null) {
            throw new BusinessException("El expediente no tiene oficina responsable asignada.");
        }

        validarPertenenciaOAdmin(usuarioAsignador, oficina.getId());

        Usuario nuevoTrabajador = usuarioRepository.findById(request.getNuevoUsuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Nuevo trabajador no encontrado con ID: " + request.getNuevoUsuarioId()));

        validarTrabajadorActivoEnOficina(nuevoTrabajador, oficina.getId());

        Usuario usuarioAnterior = expediente.getResponsableActual();
        if (usuarioAnterior != null && usuarioAnterior.getId().equals(nuevoTrabajador.getId())) {
            throw new BusinessException("El expediente ya está asignado al trabajador seleccionado.");
        }

        OffsetDateTime ahora = OffsetDateTime.now();

        // Cerrar asignación previa
        Optional<AsignacionExpediente> asignacionPrevia = asignacionRepository.findFirstByExpedienteIdAndEstado(expedienteId, EstadoAsignacion.ACTIVO);
        asignacionPrevia.ifPresent(prev -> {
            prev.setEstado(EstadoAsignacion.REASIGNADO);
            prev.setFechaFin(ahora);
            prev.setUpdatedBy(usuarioAsignador.getUsername());
            asignacionRepository.save(prev);
        });

        AsignacionExpediente nueva = AsignacionExpediente.builder()
                .expediente(expediente)
                .oficina(oficina)
                .usuarioAsignador(usuarioAsignador)
                .usuarioAsignado(nuevoTrabajador)
                .usuarioAnterior(usuarioAnterior)
                .motivo(request.getMotivo().trim())
                .indicaciones(request.getIndicaciones() != null ? request.getIndicaciones().trim() : null)
                .fechaAsignacion(ahora)
                .estado(EstadoAsignacion.ACTIVO)
                .createdBy(usuarioAsignador.getUsername())
                .build();

        AsignacionExpediente guardada = asignacionRepository.save(nueva);

        expediente.setResponsableActual(nuevoTrabajador);
        expedienteRepository.save(expediente);

        String nombreNuevo = nuevoTrabajador.getPersona() != null ? nuevoTrabajador.getPersona().getNombreCompleto() : nuevoTrabajador.getUsername();
        String nombreAnt = usuarioAnterior != null ? (usuarioAnterior.getPersona() != null ? usuarioAnterior.getPersona().getNombreCompleto() : usuarioAnterior.getUsername()) : "Sin anterior";

        historialRepository.save(ExpedienteHistorial.builder()
                .expediente(expediente)
                .estadoAnterior(expediente.getEstadoActual())
                .estadoNuevo(expediente.getEstadoActual())
                .accion("RESPONSABLE_REASIGNADO")
                .observacion("Reasignado de " + nombreAnt + " a " + nombreNuevo + ". Motivo: " + request.getMotivo())
                .usuarioResponsable(usuarioAsignador)
                .oficina(oficina)
                .fechaMovimiento(ahora)
                .identificadorCorrelacion(UUID.randomUUID().toString())
                .build());

        // Notificar a ambos
        try {
            notificationService.enviarNotificacion(
                    EnviarNotificacionRequest.builder()
                            .usuarioId(nuevoTrabajador.getId())
                            .titulo("Expediente Reasignado: " + expediente.getNumeroExpediente())
                            .mensaje("Se le ha reasignado el expediente '" + expediente.getAsunto() +
                                    "'. Motivo: " + request.getMotivo())
                            .tipo("TRAMITE")
                            .referenciaId(expediente.getNumeroExpediente())
                            .urlDestino("/dashboard/mesa-partes/expedientes/" + expediente.getId())
                            .enviarCorreo(true)
                            .build(),
                    usuarioAsignador.getUsername()
            );

            if (usuarioAnterior != null) {
                notificationService.enviarNotificacion(
                        EnviarNotificacionRequest.builder()
                                .usuarioId(usuarioAnterior.getId())
                                .titulo("Expediente Reasignado a otro responsable: " + expediente.getNumeroExpediente())
                                .mensaje("El expediente '" + expediente.getAsunto() +
                                        "' ha sido reasignado a " + nombreNuevo + ". Motivo: " + request.getMotivo())
                                .tipo("INFO")
                                .referenciaId(expediente.getNumeroExpediente())
                                .urlDestino("/dashboard/mesa-partes/expedientes/" + expediente.getId())
                                .enviarCorreo(false)
                                .build(),
                        usuarioAsignador.getUsername()
                );
            }
        } catch (Exception ex) {
            log.warn("Error enviando notificaciones de reasignación: {}", ex.getMessage());
        }

        return derivacionMapper.toAsignacionResponse(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ExpedienteResponse> listarMisExpedientes(Pageable pageable, String username) {
        Usuario usuario = obtenerUsuario(username);
        return expedienteRepository.findByResponsableActualId(usuario.getId(), pageable)
                .map(mpvMapper::toExpedienteResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioSimpleDto> listarTrabajadoresHabilitados(Long oficinaId, String username) {
        Usuario usuario = obtenerUsuario(username);
        validarPertenenciaOAdmin(usuario, oficinaId);

        List<Usuario> trabajadores = usuarioRepository.findActivosByOficinaId(oficinaId);
        return trabajadores.stream()
                .map(derivacionMapper::toUsuarioSimpleDto)
                .collect(Collectors.toList());
    }

    private Usuario obtenerUsuario(String username) {
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));
    }

    private Long resolverOficinaUsuario(Usuario usuario) {
        if (usuario.getPersona() == null) return null;
        List<PersonaOficina> asignaciones = personaOficinaRepository.findByPersonaIdAndEstado(usuario.getPersona().getId(), "ACTIVO");
        return asignaciones.stream()
                .filter(PersonaOficina::getPrincipal)
                .findFirst()
                .or(() -> asignaciones.stream().findFirst())
                .map(po -> po.getOficina().getId())
                .orElse(null);
    }

    private boolean esAdmin(Usuario usuario) {
        return usuario.getRoles().stream()
                .anyMatch(r -> "ROLE_ADMIN".equals(r.getCodigo()) || "ROLE_SUPERADMIN".equals(r.getCodigo()) || "ROLE_MESA_PARTES".equals(r.getCodigo()));
    }

    private void validarPertenenciaOAdmin(Usuario usuario, Long oficinaId) {
        if (esAdmin(usuario)) return;
        Long ofId = resolverOficinaUsuario(usuario);
        if (ofId == null || !ofId.equals(oficinaId)) {
            throw new AccessDeniedException("No tiene autorización para asignar expedientes en la oficina ID " + oficinaId);
        }
    }

    private void validarTrabajadorActivoEnOficina(Usuario trabajador, Long oficinaId) {
        if (!Boolean.TRUE.equals(trabajador.getEnabled()) || Boolean.TRUE.equals(trabajador.getAccountLocked())) {
            throw new BusinessException("El trabajador seleccionado se encuentra inhabilitado o bloqueado.");
        }
        if (trabajador.getPersona() == null) {
            throw new BusinessException("El usuario seleccionado no cuenta con una ficha de persona asignada.");
        }
        boolean pertenece = personaOficinaRepository.findByPersonaIdAndEstado(trabajador.getPersona().getId(), "ACTIVO")
                .stream()
                .anyMatch(po -> po.getOficina() != null && po.getOficina().getId().equals(oficinaId));

        if (!pertenece) {
            throw new BusinessException("El trabajador seleccionado no cuenta con asignación activa en esta oficina.");
        }
    }
}
