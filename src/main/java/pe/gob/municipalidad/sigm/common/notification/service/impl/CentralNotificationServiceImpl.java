package pe.gob.municipalidad.sigm.common.notification.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.common.model.PersonaOficina;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.notification.dto.NotificationDtos.EmailRequestDto;
import pe.gob.municipalidad.sigm.common.notification.dto.NotificationDtos.EnviarNotificacionOficinaRequest;
import pe.gob.municipalidad.sigm.common.notification.dto.NotificationDtos.EnviarNotificacionRequest;
import pe.gob.municipalidad.sigm.common.notification.dto.NotificationDtos.NotificacionItemDto;
import pe.gob.municipalidad.sigm.common.notification.dto.NotificationDtos.NotificacionPreferenciasDto;
import pe.gob.municipalidad.sigm.common.notification.dto.NotificationDtos.NotificacionProgramadaDto;
import pe.gob.municipalidad.sigm.common.notification.dto.NotificationDtos.ProgramarNotificacionRequest;
import pe.gob.municipalidad.sigm.common.notification.entity.NotificacionPreferencias;
import pe.gob.municipalidad.sigm.common.notification.entity.NotificacionProgramacion;
import pe.gob.municipalidad.sigm.common.notification.repository.NotificacionPreferenciasRepository;
import pe.gob.municipalidad.sigm.common.notification.repository.NotificacionProgramacionRepository;
import pe.gob.municipalidad.sigm.common.notification.service.CentralNotificationService;
import pe.gob.municipalidad.sigm.common.notification.service.EmailService;
import pe.gob.municipalidad.sigm.common.repository.OficinaRepository;
import pe.gob.municipalidad.sigm.common.repository.PersonaOficinaRepository;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.administracion.service.AuditoriaAdministrativaService;
import pe.gob.municipalidad.sigm.modules.notificaciones.model.Notificacion;
import pe.gob.municipalidad.sigm.modules.notificaciones.repository.NotificacionRepository;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CentralNotificationServiceImpl implements CentralNotificationService {

    private final NotificacionRepository notificacionRepository;
    private final NotificacionPreferenciasRepository preferenciasRepository;
    private final NotificacionProgramacionRepository programacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final OficinaRepository oficinaRepository;
    private final PersonaOficinaRepository personaOficinaRepository;
    private final EmailService emailService;
    private final AuditoriaAdministrativaService auditoriaService;

    @Override
    @Transactional(readOnly = true)
    public Page<NotificacionItemDto> buscarNotificacionesFiltradas(String username, Boolean leida, String tipo, String termino, Pageable pageable) {
        Usuario usuario = buscarUsuarioPorUsername(username);
        return notificacionRepository.buscarNotificacionesFiltradas(usuario.getId(), leida, tipo, termino, pageable)
                .map(NotificacionItemDto::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificacionItemDto obtenerNotificacionPorId(Long id, String username) {
        Usuario usuario = buscarUsuarioPorUsername(username);
        Notificacion notificacion = notificacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notificación no encontrada con ID: " + id));

        if (!notificacion.getUsuario().getId().equals(usuario.getId())) {
            log.warn("Acceso denegado: El usuario {} intentó acceder a la notificación {} de otro usuario", username, id);
            throw new AccessDeniedException("Acceso denegado: La notificación no pertenece al usuario autenticado.");
        }

        return NotificacionItemDto.fromEntity(notificacion);
    }

    @Override
    @Transactional(readOnly = true)
    public long contarNoLeidas(String username) {
        Usuario usuario = usuarioRepository.findByUsername(username).orElse(null);
        if (usuario == null) {
            return 0L;
        }
        return notificacionRepository.countByUsuarioIdAndLeidaFalse(usuario.getId());
    }

    @Override
    @Transactional
    public void marcarComoLeida(Long notificacionId, String username) {
        Usuario usuario = buscarUsuarioPorUsername(username);
        Notificacion notificacion = notificacionRepository.findById(notificacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Notificación no encontrada con ID: " + notificacionId));

        if (!notificacion.getUsuario().getId().equals(usuario.getId())) {
            log.warn("Acceso denegado: El usuario {} intentó marcar la notificación {} ajena", username, notificacionId);
            throw new AccessDeniedException("Acceso denegado: La notificación no pertenece al usuario autenticado.");
        }

        if (!notificacion.isLeida()) {
            notificacion.setLeida(true);
            notificacion.setFechaLectura(OffsetDateTime.now());
            notificacionRepository.save(notificacion);
        }
    }

    @Override
    @Transactional
    public void marcarTodasComoLeidas(String username) {
        Usuario usuario = buscarUsuarioPorUsername(username);
        notificacionRepository.marcarTodasComoLeidas(usuario.getId(), OffsetDateTime.now());
    }

    @Override
    @Transactional
    public NotificacionItemDto enviarNotificacion(EnviarNotificacionRequest request, String creadoPor) {
        Usuario usuario = usuarioRepository.findById(request.getUsuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + request.getUsuarioId()));

        NotificacionPreferencias pref = obtenerOInicializarEntidadPreferencias(usuario.getId());
        String tipo = request.getTipo() != null ? request.getTipo() : "INFORMACION";

        boolean esSeguridad = "SEGURIDAD".equalsIgnoreCase(tipo);
        boolean canalInternoHabilitado = esSeguridad || Boolean.TRUE.equals(pref.getCanalInterno());
        boolean categoriaHabilitada = esSeguridad || esCategoriaHabilitada(tipo, pref);

        Notificacion notificacion = null;
        if (canalInternoHabilitado && categoriaHabilitada) {
            notificacion = Notificacion.builder()
                    .usuario(usuario)
                    .titulo(request.getTitulo())
                    .mensaje(request.getMensaje())
                    .tipo(tipo)
                    .leida(false)
                    .fechaCreacion(OffsetDateTime.now())
                    .urlDestino(request.getUrlDestino())
                    .referenciaId(request.getReferenciaId())
                    .origen(creadoPor != null ? creadoPor : "SISTEMA")
                    .build();

            notificacion = notificacionRepository.save(notificacion);
        }

        // Si se solicita envío por correo electrónico o preferencia de email está activa
        boolean canalEmailHabilitado = esSeguridad || Boolean.TRUE.equals(pref.getCanalEmail());
        if ((request.isEnviarCorreo() || canalEmailHabilitado) && categoriaHabilitada) {
            if (usuario.getPersona() != null && usuario.getPersona().getCorreo() != null && !usuario.getPersona().getCorreo().isBlank()) {
                encolarEmailNotificacion(usuario, request.getTitulo(), request.getMensaje(), tipo, request.getUrlDestino());
            }
        }

        if (notificacion != null) {
            auditoriaService.registrar("CREAR_NOTIFICACION", "notificaciones", notificacion.getId(), "EXITO",
                    "Notificación creada para usuario: " + usuario.getUsername() + ", Tipo: " + tipo);
            return NotificacionItemDto.fromEntity(notificacion);
        } else {
            // Caso donde las preferencias del usuario omitieron el canal interno pero se procesó
            return NotificacionItemDto.builder()
                    .usuarioId(usuario.getId())
                    .titulo(request.getTitulo())
                    .mensaje(request.getMensaje())
                    .tipo(tipo)
                    .leida(false)
                    .fechaCreacion(OffsetDateTime.now())
                    .origen(creadoPor != null ? creadoPor : "SISTEMA")
                    .build();
        }
    }

    @Override
    @Transactional
    public int enviarNotificacionOficina(EnviarNotificacionOficinaRequest request, String creadoPor) {
        Oficina oficina = oficinaRepository.findById(request.getOficinaId())
                .orElseThrow(() -> new ResourceNotFoundException("Oficina no encontrada con ID: " + request.getOficinaId()));

        List<PersonaOficina> asignaciones = personaOficinaRepository.findByOficinaIdAndEstado(oficina.getId(), "ACTIVO");
        LocalDate hoy = LocalDate.now();

        Set<Usuario> destinatarios = new HashSet<>();
        for (PersonaOficina po : asignaciones) {
            if (po.getFechaFin() != null && po.getFechaFin().isBefore(hoy)) {
                continue;
            }
            usuarioRepository.findByPersonaId(po.getPersona().getId()).ifPresent(usuario -> {
                if (Boolean.TRUE.equals(usuario.getEnabled()) && !Boolean.TRUE.equals(usuario.getAccountLocked())) {
                    destinatarios.add(usuario);
                }
            });
        }

        int enviados = 0;
        String canal = request.getCanal() != null ? request.getCanal() : "INTERNO";
        boolean enviarInterno = "INTERNO".equalsIgnoreCase(canal) || "AMBOS".equalsIgnoreCase(canal);
        boolean enviarCorreo = "CORREO".equalsIgnoreCase(canal) || "AMBOS".equalsIgnoreCase(canal);

        for (Usuario usuario : destinatarios) {
            if (enviarInterno) {
                Notificacion n = Notificacion.builder()
                        .usuario(usuario)
                        .titulo(request.getTitulo())
                        .mensaje(request.getMensaje())
                        .tipo(request.getTipo() != null ? request.getTipo() : "INFORMACION")
                        .leida(false)
                        .fechaCreacion(OffsetDateTime.now())
                        .urlDestino(request.getUrlDestino())
                        .origen(creadoPor != null ? creadoPor : "OFICINA:" + oficina.getSigla())
                        .build();
                notificacionRepository.save(n);
            }

            if (enviarCorreo && usuario.getPersona() != null && usuario.getPersona().getCorreo() != null) {
                encolarEmailNotificacion(usuario, request.getTitulo(), request.getMensaje(), request.getTipo(), request.getUrlDestino());
            }
            enviados++;
        }

        auditoriaService.registrar("NOTIFICACION_OFICINA", "oficinas", oficina.getId(), "EXITO",
                "Difusión institucional enviada a " + enviados + " integrantes de la oficina " + oficina.getNombre());

        return enviados;
    }

    @Override
    @Transactional
    public NotificacionProgramadaDto programarNotificacion(ProgramarNotificacionRequest request, String creadoPor) {
        if (request.getUsuarioId() == null && request.getOficinaId() == null) {
            throw new BusinessException("Debe especificar al menos un usuario destinatario o una oficina para la programación.");
        }
        if (request.getFechaProgramada() == null || !request.getFechaProgramada().isAfter(OffsetDateTime.now())) {
            throw new BusinessException("La fecha programada debe ser estrictamente futura.");
        }

        Usuario usuarioDestino = request.getUsuarioId() != null
                ? usuarioRepository.findById(request.getUsuarioId()).orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + request.getUsuarioId()))
                : null;
        Oficina oficinaDestino = request.getOficinaId() != null
                ? oficinaRepository.findById(request.getOficinaId()).orElseThrow(() -> new ResourceNotFoundException("Oficina no encontrada con ID: " + request.getOficinaId()))
                : null;

        NotificacionProgramacion programacion = NotificacionProgramacion.builder()
                .usuario(usuarioDestino)
                .oficina(oficinaDestino)
                .titulo(request.getTitulo())
                .mensaje(request.getMensaje())
                .tipo(request.getTipo() != null ? request.getTipo() : "INFORMACION")
                .canal(request.getCanal() != null ? request.getCanal() : "INTERNO")
                .fechaProgramada(request.getFechaProgramada())
                .ejecutada(false)
                .cancelada(false)
                .creadoPor(creadoPor != null ? creadoPor : "SISTEMA")
                .build();

        programacion = programacionRepository.save(programacion);

        auditoriaService.registrar("PROGRAMAR_NOTIFICACION", "notificacion_programaciones", programacion.getId(), "EXITO",
                "Notificación programada para ejecución en: " + programacion.getFechaProgramada());

        return NotificacionProgramadaDto.fromEntity(programacion);
    }

    @Override
    @Transactional
    public void cancelarProgramacion(Long programacionId, String usuario) {
        NotificacionProgramacion programacion = programacionRepository.findById(programacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Programación no encontrada con ID: " + programacionId));

        if (Boolean.TRUE.equals(programacion.getEjecutada())) {
            throw new BusinessException("No se puede cancelar una notificación que ya ha sido ejecutada.");
        }
        if (Boolean.TRUE.equals(programacion.getCancelada())) {
            throw new BusinessException("La programación ya fue cancelada previamente.");
        }

        programacion.setCancelada(true);
        programacionRepository.save(programacion);

        auditoriaService.registrar("CANCELAR_PROGRAMACION", "notificacion_programaciones", programacion.getId(), "EXITO",
                "Programación de notificación cancelada por " + usuario);
    }

    @Override
    @Transactional
    public int procesarNotificacionesProgramadas() {
        OffsetDateTime ahora = OffsetDateTime.now();
        List<NotificacionProgramacion> vencidas = programacionRepository.findProgramadasPendientesDeEjecucion(ahora, PageRequest.of(0, 50));
        int procesadas = 0;

        for (NotificacionProgramacion prog : vencidas) {
            try {
                if (prog.getUsuario() != null) {
                    EnviarNotificacionRequest req = EnviarNotificacionRequest.builder()
                            .usuarioId(prog.getUsuario().getId())
                            .titulo(prog.getTitulo())
                            .mensaje(prog.getMensaje())
                            .tipo(prog.getTipo())
                            .enviarCorreo("CORREO".equalsIgnoreCase(prog.getCanal()) || "AMBOS".equalsIgnoreCase(prog.getCanal()))
                            .build();
                    enviarNotificacion(req, "PROGRAMADOR:" + prog.getCreadoPor());
                } else if (prog.getOficina() != null) {
                    EnviarNotificacionOficinaRequest req = EnviarNotificacionOficinaRequest.builder()
                            .oficinaId(prog.getOficina().getId())
                            .titulo(prog.getTitulo())
                            .mensaje(prog.getMensaje())
                            .tipo(prog.getTipo())
                            .canal(prog.getCanal())
                            .build();
                    enviarNotificacionOficina(req, "PROGRAMADOR:" + prog.getCreadoPor());
                }

                prog.setEjecutada(true);
                prog.setFechaEjecucion(OffsetDateTime.now());
                programacionRepository.save(prog);
                procesadas++;
            } catch (Exception e) {
                log.error("Fallo al ejecutar notificación programada {}: {}", prog.getId(), e.getMessage());
            }
        }

        if (procesadas > 0) {
            log.info("Se procesaron exitosamente {} notificaciones programadas vencidas", procesadas);
        }
        return procesadas;
    }

    @Override
    @Transactional
    public NotificacionPreferenciasDto obtenerOInicializarPreferencias(String username) {
        Usuario usuario = buscarUsuarioPorUsername(username);
        NotificacionPreferencias entidad = obtenerOInicializarEntidadPreferencias(usuario.getId());
        return NotificacionPreferenciasDto.fromEntity(entidad);
    }

    @Override
    @Transactional
    public NotificacionPreferenciasDto actualizarPreferencias(String username, NotificacionPreferenciasDto dto) {
        Usuario usuario = buscarUsuarioPorUsername(username);
        NotificacionPreferencias entidad = obtenerOInicializarEntidadPreferencias(usuario.getId());

        entidad.setCanalInterno(dto.getCanalInterno() != null ? dto.getCanalInterno() : true);
        entidad.setCanalEmail(dto.getCanalEmail() != null ? dto.getCanalEmail() : true);
        // Regla obligatoria: Las alertas de seguridad SIEMPRE permanecen activas
        entidad.setAlertasSeguridad(true);
        entidad.setAvisosTramites(dto.getAvisosTramites() != null ? dto.getAvisosTramites() : true);
        entidad.setAvisosDocumentos(dto.getAvisosDocumentos() != null ? dto.getAvisosDocumentos() : true);
        entidad.setAvisosPagos(dto.getAvisosPagos() != null ? dto.getAvisosPagos() : true);
        entidad.setComunicacionesGenerales(dto.getComunicacionesGenerales() != null ? dto.getComunicacionesGenerales() : true);

        entidad = preferenciasRepository.save(entidad);

        auditoriaService.registrar("ACTUALIZAR_PREFERENCIAS", "notificacion_preferencias", entidad.getId(), "EXITO",
                "Preferencias de comunicación actualizadas por el usuario: " + username);

        return NotificacionPreferenciasDto.fromEntity(entidad);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificacionProgramadaDto> listarProgramadas(Pageable pageable) {
        return programacionRepository.findAll(pageable).map(NotificacionProgramadaDto::fromEntity);
    }

    private Usuario buscarUsuarioPorUsername(String username) {
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));
    }

    private NotificacionPreferencias obtenerOInicializarEntidadPreferencias(Long usuarioId) {
        return preferenciasRepository.findByUsuarioId(usuarioId).orElseGet(() -> {
            Usuario usuario = usuarioRepository.findById(usuarioId)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + usuarioId));
            NotificacionPreferencias def = NotificacionPreferencias.builder()
                    .usuario(usuario)
                    .canalInterno(true)
                    .canalEmail(true)
                    .alertasSeguridad(true)
                    .avisosTramites(true)
                    .avisosDocumentos(true)
                    .avisosPagos(true)
                    .comunicacionesGenerales(true)
                    .build();
            return preferenciasRepository.save(def);
        });
    }

    private boolean esCategoriaHabilitada(String tipo, NotificacionPreferencias pref) {
        if (tipo == null) return true;
        return switch (tipo.toUpperCase()) {
            case "TRAMITE", "APROBACION" -> Boolean.TRUE.equals(pref.getAvisosTramites());
            case "DOCUMENTO", "OBSERVACION" -> Boolean.TRUE.equals(pref.getAvisosDocumentos());
            case "PAGO" -> Boolean.TRUE.equals(pref.getAvisosPagos());
            case "INFORMACION", "INFO", "EXITO", "ADVERTENCIA", "ALERTA", "ERROR", "SISTEMA" -> Boolean.TRUE.equals(pref.getComunicacionesGenerales());
            default -> true;
        };
    }

    private void encolarEmailNotificacion(Usuario usuario, String titulo, String mensaje, String tipo, String urlDestino) {
        try {
            Map<String, Object> vars = new HashMap<>();
            String nombre = (usuario.getPersona() != null && usuario.getPersona().getNombres() != null)
                    ? usuario.getPersona().getNombres() + " " + usuario.getPersona().getApellidoPaterno()
                    : usuario.getUsername();

            vars.put("nombreDestinatario", nombre);
            vars.put("titulo", titulo);
            vars.put("mensaje", mensaje);
            vars.put("tipo", tipo != null ? tipo : "INFORMACION");
            vars.put("enlaceAccion", urlDestino != null && !urlDestino.isBlank() ? urlDestino : "/notificaciones");

            String plantillaCodigo = "SEGURIDAD".equalsIgnoreCase(tipo) ? "ALERTA_SEGURIDAD" : "NUEVA_NOTIFICACION";

            EmailRequestDto emailReq = EmailRequestDto.builder()
                    .destinatarioCorreo(usuario.getPersona().getCorreo())
                    .destinatarioNombre(nombre)
                    .usuarioId(usuario.getId())
                    .plantillaCodigo(plantillaCodigo)
                    .asuntoPersonalizado("SIGM-QUINUA — " + titulo)
                    .variables(vars)
                    .tipo(tipo)
                    .idempotencyKey(UUID.randomUUID().toString())
                    .build();

            emailService.encolarEnvio(emailReq);
        } catch (Exception e) {
            log.warn("No se pudo encolar correo para la notificación {}: {}", titulo, e.getMessage());
        }
    }
}
