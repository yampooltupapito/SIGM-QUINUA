package pe.gob.municipalidad.sigm.modules.mesapartes.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.common.model.PersonaOficina;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.notification.dto.NotificationDtos.EnviarNotificacionOficinaRequest;
import pe.gob.municipalidad.sigm.common.notification.service.CentralNotificationService;
import pe.gob.municipalidad.sigm.common.repository.OficinaRepository;
import pe.gob.municipalidad.sigm.common.repository.PersonaOficinaRepository;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.DerivacionDtos.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoDerivacion;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoExpediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.TipoEventoDerivacion;
import pe.gob.municipalidad.sigm.modules.mesapartes.mapper.DerivacionMapper;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.DerivacionEventoRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.DerivacionExpedienteRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.ExpedienteRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.DerivacionExpedienteService;
import pe.gob.municipalidad.sigm.modules.mesapartes.workflow.ExpedienteWorkflowService;

import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Service
public class DerivacionExpedienteServiceImpl implements DerivacionExpedienteService {

    private final DerivacionExpedienteRepository derivacionRepository;
    private final DerivacionEventoRepository derivacionEventoRepository;
    private final ExpedienteRepository expedienteRepository;
    private final OficinaRepository oficinaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PersonaOficinaRepository personaOficinaRepository;
    private final ExpedienteWorkflowService workflowService;
    private final CentralNotificationService notificationService;
    private final DerivacionMapper derivacionMapper;

    public DerivacionExpedienteServiceImpl(
            DerivacionExpedienteRepository derivacionRepository,
            DerivacionEventoRepository derivacionEventoRepository,
            ExpedienteRepository expedienteRepository,
            OficinaRepository oficinaRepository,
            UsuarioRepository usuarioRepository,
            PersonaOficinaRepository personaOficinaRepository,
            ExpedienteWorkflowService workflowService,
            CentralNotificationService notificationService,
            DerivacionMapper derivacionMapper) {
        this.derivacionRepository = derivacionRepository;
        this.derivacionEventoRepository = derivacionEventoRepository;
        this.expedienteRepository = expedienteRepository;
        this.oficinaRepository = oficinaRepository;
        this.usuarioRepository = usuarioRepository;
        this.personaOficinaRepository = personaOficinaRepository;
        this.workflowService = workflowService;
        this.notificationService = notificationService;
        this.derivacionMapper = derivacionMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DerivacionResponse> listarDerivaciones(Long oficinaId, EstadoDerivacion estado, Pageable pageable, String username) {
        Usuario usuario = obtenerUsuario(username);
        Long oficinaUsuarioId = resolverOficinaUsuario(usuario);
        boolean isAdmin = esAdmin(usuario);

        Specification<DerivacionExpediente> spec = (root, query, cb) -> {
            var predicates = cb.conjunction();
            if (!isAdmin && oficinaUsuarioId != null) {
                predicates = cb.and(predicates, cb.or(
                        cb.equal(root.get("oficinaOrigen").get("id"), oficinaUsuarioId),
                        cb.equal(root.get("oficinaDestino").get("id"), oficinaUsuarioId)
                ));
            } else if (oficinaId != null) {
                predicates = cb.and(predicates, cb.or(
                        cb.equal(root.get("oficinaOrigen").get("id"), oficinaId),
                        cb.equal(root.get("oficinaDestino").get("id"), oficinaId)
                ));
            }
            if (estado != null) {
                predicates = cb.and(predicates, cb.equal(root.get("estado"), estado));
            }
            return predicates;
        };

        return derivacionRepository.findAll(spec, pageable).map(derivacionMapper::toDerivacionResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DerivacionResponse> listarDerivacionesRecibidas(EstadoDerivacion estado, Pageable pageable, String username) {
        Usuario usuario = obtenerUsuario(username);
        Long oficinaUsuarioId = resolverOficinaUsuario(usuario);
        boolean isAdmin = esAdmin(usuario);

        Specification<DerivacionExpediente> spec = (root, query, cb) -> {
            var predicates = cb.conjunction();
            if (!isAdmin) {
                if (oficinaUsuarioId == null) {
                    return cb.disjunction();
                }
                predicates = cb.and(predicates, cb.equal(root.get("oficinaDestino").get("id"), oficinaUsuarioId));
            }
            if (estado != null) {
                predicates = cb.and(predicates, cb.equal(root.get("estado"), estado));
            }
            return predicates;
        };

        return derivacionRepository.findAll(spec, pageable).map(derivacionMapper::toDerivacionResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public DerivacionResponse obtenerPorId(Long derivacionId, String username) {
        DerivacionExpediente derivacion = derivacionRepository.findById(derivacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Derivación no encontrada con ID: " + derivacionId));

        Usuario usuario = obtenerUsuario(username);
        validarAccesoADerivacion(usuario, derivacion);

        return derivacionMapper.toDerivacionResponse(derivacion);
    }

    @Override
    @Transactional
    public DerivacionResponse derivarExpediente(Long expedienteId, DerivarExpedienteRequest request, String username) {
        Usuario usuario = obtenerUsuario(username);
        Expediente expediente = expedienteRepository.findByIdWithDetalles(expedienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Expediente no encontrado con ID: " + expedienteId));

        Oficina oficinaOrigen = expediente.getOficinaResponsable();
        if (oficinaOrigen == null) {
            throw new BusinessException("El expediente no tiene una oficina responsable asignada.");
        }

        validarPertenenciaOAdmin(usuario, oficinaOrigen.getId());

        // Verificar estados permitidos
        EstadoExpediente estadoActual = expediente.getEstadoActual();
        if (estadoActual != EstadoExpediente.RECIBIDO &&
            estadoActual != EstadoExpediente.EN_REVISION &&
            estadoActual != EstadoExpediente.EN_ATENCION &&
            estadoActual != EstadoExpediente.DERIVADO) {
            throw new BusinessException("No es posible derivar un expediente en estado actual '" + estadoActual + "'.");
        }

        // Verificar que la oficina de destino exista y esté activa
        Oficina oficinaDestino = oficinaRepository.findById(request.getOficinaDestinoId())
                .orElseThrow(() -> new ResourceNotFoundException("Oficina de destino no encontrada con ID: " + request.getOficinaDestinoId()));

        if (!"ACTIVO".equalsIgnoreCase(oficinaDestino.getEstado())) {
            throw new BusinessException("La oficina de destino seleccionada se encuentra inactiva.");
        }

        if (oficinaDestino.getId().equals(oficinaOrigen.getId())) {
            throw new BusinessException("La oficina de destino no puede ser la misma oficina de origen.");
        }

        // Verificar que no exista otra derivación pendiente
        derivacionRepository.findFirstByExpedienteIdAndEstado(expedienteId, EstadoDerivacion.PENDIENTE)
                .ifPresent(d -> {
                    throw new BusinessException("Ya existe una derivación pendiente de recepción hacia la oficina '" +
                            d.getOficinaDestino().getNombre() + "'.");
                });

        if (request.getPrioridad() != null) {
            expediente.setPrioridad(request.getPrioridad());
        }

        DerivacionExpediente derivacion = DerivacionExpediente.builder()
                .expediente(expediente)
                .oficinaOrigen(oficinaOrigen)
                .oficinaDestino(oficinaDestino)
                .usuarioDerivador(usuario)
                .motivo(request.getMotivo().trim())
                .indicaciones(request.getIndicaciones() != null ? request.getIndicaciones().trim() : null)
                .fechaDerivacion(OffsetDateTime.now())
                .estado(EstadoDerivacion.PENDIENTE)
                .createdBy(usuario.getUsername())
                .build();

        DerivacionExpediente guardada = derivacionRepository.save(derivacion);

        // Transición de workflow
        workflowService.transicionar(
                expediente,
                EstadoExpediente.DERIVADO,
                "DERIVACION_EMITIDA",
                "Derivado de " + oficinaOrigen.getNombre() + " a " + oficinaDestino.getNombre() + ". Motivo: " + request.getMotivo(),
                usuario,
                oficinaOrigen
        );

        // Registro de evento
        derivacionEventoRepository.save(DerivacionEvento.builder()
                .derivacion(guardada)
                .tipoEvento(TipoEventoDerivacion.ENVIADA)
                .usuario(usuario)
                .oficina(oficinaOrigen)
                .motivo(request.getMotivo())
                .observacion("Expediente derivado a " + oficinaDestino.getNombre())
                .fechaEvento(OffsetDateTime.now())
                .build());

        // Notificación interna a la oficina destino
        try {
            notificationService.enviarNotificacionOficina(
                    EnviarNotificacionOficinaRequest.builder()
                            .oficinaId(oficinaDestino.getId())
                            .titulo("Expediente Derivado: " + expediente.getNumeroExpediente())
                            .mensaje("Se ha recibido en despacho el expediente '" + expediente.getAsunto() +
                                    "'. Remitido por " + oficinaOrigen.getNombre() + ". Motivo: " + request.getMotivo())
                            .tipo("TRAMITE")
                            .urlDestino("/dashboard/mesa-partes/derivaciones/recibidas")
                            .canal("INTERNO")
                            .build(),
                    usuario.getUsername()
            );
        } catch (Exception ex) {
            log.warn("No se pudo enviar notificación de derivación: {}", ex.getMessage());
        }

        return derivacionMapper.toDerivacionResponse(guardada);
    }

    @Override
    @Transactional
    public DerivacionResponse recibirDerivacion(Long derivacionId, RecepcionDerivacionRequest request, String username) {
        Usuario usuario = obtenerUsuario(username);
        DerivacionExpediente derivacion = derivacionRepository.findById(derivacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Derivación no encontrada con ID: " + derivacionId));

        if (derivacion.getEstado() != EstadoDerivacion.PENDIENTE) {
            throw new BusinessException("Solo se pueden recibir derivaciones en estado PENDIENTE. Estado actual: " + derivacion.getEstado());
        }

        validarPertenenciaOAdmin(usuario, derivacion.getOficinaDestino().getId());

        OffsetDateTime ahora = OffsetDateTime.now();
        derivacion.setEstado(EstadoDerivacion.RECIBIDA);
        derivacion.setFechaRecepcion(ahora);
        derivacion.setUsuarioReceptor(usuario);
        if (request != null && request.getObservaciones() != null) {
            derivacion.setObservaciones(request.getObservaciones().trim());
        }
        derivacion.setUpdatedBy(usuario.getUsername());

        DerivacionExpediente guardada = derivacionRepository.save(derivacion);

        Expediente expediente = derivacion.getExpediente();
        expediente.setOficinaResponsable(derivacion.getOficinaDestino());
        // Al recibir la oficina, el expediente queda pendiente de asignación a un trabajador específico
        expediente.setResponsableActual(null);

        workflowService.transicionar(
                expediente,
                EstadoExpediente.EN_ATENCION,
                "RECEPCION_DERIVACION",
                "Recepción formal aceptada en " + derivacion.getOficinaDestino().getNombre() +
                        (derivacion.getObservaciones() != null ? ". Nota: " + derivacion.getObservaciones() : ""),
                usuario,
                derivacion.getOficinaDestino()
        );

        derivacionEventoRepository.save(DerivacionEvento.builder()
                .derivacion(guardada)
                .tipoEvento(TipoEventoDerivacion.RECIBIDA)
                .usuario(usuario)
                .oficina(derivacion.getOficinaDestino())
                .observacion("Derivación aceptada en despacho")
                .fechaEvento(ahora)
                .build());

        // Notificar a la oficina remitente
        try {
            notificationService.enviarNotificacionOficina(
                    EnviarNotificacionOficinaRequest.builder()
                            .oficinaId(derivacion.getOficinaOrigen().getId())
                            .titulo("Derivación Aceptada: " + expediente.getNumeroExpediente())
                            .mensaje("La oficina " + derivacion.getOficinaDestino().getNombre() +
                                    " ha aceptado formalmente la recepción del expediente.")
                            .tipo("TRAMITE")
                            .urlDestino("/dashboard/mesa-partes/expedientes/" + expediente.getId())
                            .canal("INTERNO")
                            .build(),
                    usuario.getUsername()
            );
        } catch (Exception ex) {
            log.warn("No se pudo notificar recepción de derivación: {}", ex.getMessage());
        }

        return derivacionMapper.toDerivacionResponse(guardada);
    }

    @Override
    @Transactional
    public DerivacionResponse rechazarDerivacion(Long derivacionId, RechazoDevolucionRequest request, String username) {
        return procesarRechazoODevolucion(derivacionId, request, username, EstadoDerivacion.RECHAZADA, TipoEventoDerivacion.RECHAZADA, "RECHAZO_DERIVACION");
    }

    @Override
    @Transactional
    public DerivacionResponse devolverDerivacion(Long derivacionId, RechazoDevolucionRequest request, String username) {
        return procesarRechazoODevolucion(derivacionId, request, username, EstadoDerivacion.DEVUELTA, TipoEventoDerivacion.DEVUELTA, "DEVOLUCION_DERIVACION");
    }

    private DerivacionResponse procesarRechazoODevolucion(Long derivacionId,
                                                          RechazoDevolucionRequest request,
                                                          String username,
                                                          EstadoDerivacion nuevoEstado,
                                                          TipoEventoDerivacion tipoEvento,
                                                          String accionWorkflow) {
        if (request == null || request.getMotivo() == null || request.getMotivo().trim().isBlank()) {
            throw new BusinessException("El motivo de " + nuevoEstado.name().toLowerCase() + " es estrictamente obligatorio.");
        }

        Usuario usuario = obtenerUsuario(username);
        DerivacionExpediente derivacion = derivacionRepository.findById(derivacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Derivación no encontrada con ID: " + derivacionId));

        if (derivacion.getEstado() != EstadoDerivacion.PENDIENTE) {
            throw new BusinessException("Solo se pueden rechazar o devolver derivaciones en estado PENDIENTE.");
        }

        validarPertenenciaOAdmin(usuario, derivacion.getOficinaDestino().getId());

        OffsetDateTime ahora = OffsetDateTime.now();
        derivacion.setEstado(nuevoEstado);
        derivacion.setFechaRecepcion(ahora);
        derivacion.setUsuarioReceptor(usuario);
        derivacion.setMotivoDevolucion(request.getMotivo().trim());
        if (request.getObservaciones() != null) {
            derivacion.setObservaciones(request.getObservaciones().trim());
        }
        derivacion.setUpdatedBy(usuario.getUsername());

        DerivacionExpediente guardada = derivacionRepository.save(derivacion);

        Expediente expediente = derivacion.getExpediente();
        // El expediente retorna formalmente a la oficina de origen
        expediente.setOficinaResponsable(derivacion.getOficinaOrigen());

        workflowService.transicionar(
                expediente,
                EstadoExpediente.RECIBIDO,
                accionWorkflow,
                nuevoEstado + " por " + derivacion.getOficinaDestino().getNombre() + ". Motivo: " + request.getMotivo(),
                usuario,
                derivacion.getOficinaDestino()
        );

        derivacionEventoRepository.save(DerivacionEvento.builder()
                .derivacion(guardada)
                .tipoEvento(tipoEvento)
                .usuario(usuario)
                .oficina(derivacion.getOficinaDestino())
                .motivo(request.getMotivo())
                .observacion("Expediente devuelto a oficina de origen")
                .fechaEvento(ahora)
                .build());

        // Notificar a la oficina remitente
        try {
            notificationService.enviarNotificacionOficina(
                    EnviarNotificacionOficinaRequest.builder()
                            .oficinaId(derivacion.getOficinaOrigen().getId())
                            .titulo("Derivación " + nuevoEstado + ": " + expediente.getNumeroExpediente())
                            .mensaje("La oficina " + derivacion.getOficinaDestino().getNombre() +
                                    " ha " + nuevoEstado.name().toLowerCase() + " la derivación. Motivo: " + request.getMotivo())
                            .tipo("TRAMITE")
                            .urlDestino("/dashboard/mesa-partes/expedientes/" + expediente.getId())
                            .canal("INTERNO")
                            .build(),
                    usuario.getUsername()
            );
        } catch (Exception ex) {
            log.warn("No se pudo notificar rechazo/devolución de derivación: {}", ex.getMessage());
        }

        return derivacionMapper.toDerivacionResponse(guardada);
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
            throw new AccessDeniedException("No tiene autorización para operar sobre recursos de la oficina ID " + oficinaId);
        }
    }

    private void validarAccesoADerivacion(Usuario usuario, DerivacionExpediente derivacion) {
        if (esAdmin(usuario)) return;
        Long ofId = resolverOficinaUsuario(usuario);
        if (ofId == null) {
            throw new AccessDeniedException("Usuario sin asignación de oficina activa.");
        }
        boolean esOrigen = derivacion.getOficinaOrigen() != null && ofId.equals(derivacion.getOficinaOrigen().getId());
        boolean esDestino = derivacion.getOficinaDestino() != null && ofId.equals(derivacion.getOficinaDestino().getId());
        if (!esOrigen && !esDestino) {
            throw new AccessDeniedException("No tiene permisos para consultar derivaciones ajenas a su oficina.");
        }
    }
}
