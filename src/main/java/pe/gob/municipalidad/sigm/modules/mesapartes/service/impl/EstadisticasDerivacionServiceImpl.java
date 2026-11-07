package pe.gob.municipalidad.sigm.modules.mesapartes.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.common.model.PersonaOficina;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.OficinaRepository;
import pe.gob.municipalidad.sigm.common.repository.PersonaOficinaRepository;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.DerivacionDtos.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvDtos.ExpedienteResponse;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.Expediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoDerivacion;
import pe.gob.municipalidad.sigm.modules.mesapartes.mapper.DerivacionMapper;
import pe.gob.municipalidad.sigm.modules.mesapartes.mapper.MpvMapper;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.AsignacionExpedienteRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.DerivacionExpedienteRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.ExpedienteRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.EstadisticasDerivacionService;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EstadisticasDerivacionServiceImpl implements EstadisticasDerivacionService {

    private final DerivacionExpedienteRepository derivacionRepository;
    private final ExpedienteRepository expedienteRepository;
    private final AsignacionExpedienteRepository asignacionRepository;
    private final OficinaRepository oficinaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PersonaOficinaRepository personaOficinaRepository;
    private final DerivacionMapper derivacionMapper;
    private final MpvMapper mpvMapper;

    public EstadisticasDerivacionServiceImpl(
            DerivacionExpedienteRepository derivacionRepository,
            ExpedienteRepository expedienteRepository,
            AsignacionExpedienteRepository asignacionRepository,
            OficinaRepository oficinaRepository,
            UsuarioRepository usuarioRepository,
            PersonaOficinaRepository personaOficinaRepository,
            DerivacionMapper derivacionMapper,
            MpvMapper mpvMapper) {
        this.derivacionRepository = derivacionRepository;
        this.expedienteRepository = expedienteRepository;
        this.asignacionRepository = asignacionRepository;
        this.oficinaRepository = oficinaRepository;
        this.usuarioRepository = usuarioRepository;
        this.personaOficinaRepository = personaOficinaRepository;
        this.derivacionMapper = derivacionMapper;
        this.mpvMapper = mpvMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public EstadisticasDerivacionResponse obtenerEstadisticas(Long oficinaId, String username) {
        Usuario usuario = obtenerUsuario(username);
        Long ofId = (oficinaId != null) ? oficinaId : resolverOficinaUsuario(usuario);

        if (ofId != null) {
            validarPertenenciaOAdmin(usuario, ofId);
            return calcularEstadisticasPorOficina(ofId);
        } else if (esAdmin(usuario)) {
            return calcularEstadisticasGlobales();
        } else {
            throw new AccessDeniedException("Usuario sin asignación de oficina activa.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PanelOficinaResponse obtenerPanelOficina(Long oficinaId, String username) {
        Usuario usuario = obtenerUsuario(username);
        Long ofId = (oficinaId != null) ? oficinaId : resolverOficinaUsuario(usuario);

        if (ofId == null) {
            throw new AccessDeniedException("Debe especificar o contar con una oficina asignada para acceder al panel de supervisión.");
        }

        validarPertenenciaOAdmin(usuario, ofId);

        Oficina oficina = oficinaRepository.findById(ofId)
                .orElseThrow(() -> new ResourceNotFoundException("Oficina no encontrada con ID: " + ofId));

        EstadisticasDerivacionResponse stats = calcularEstadisticasPorOficina(ofId);

        // Expedientes pendientes de asignación (en esta oficina, sin responsable)
        List<Expediente> todosOficina = expedienteRepository.findByOficinaResponsableId(ofId, PageRequest.of(0, 50, Sort.by("createdAt").descending())).getContent();

        List<ExpedienteResponse> pendientesAsignacion = todosOficina.stream()
                .filter(e -> e.getResponsableActual() == null)
                .map(mpvMapper::toExpedienteResponse)
                .collect(Collectors.toList());

        List<ExpedienteResponse> enAtencion = todosOficina.stream()
                .filter(e -> e.getResponsableActual() != null)
                .map(mpvMapper::toExpedienteResponse)
                .collect(Collectors.toList());

        // Carga de trabajo por trabajador
        List<Usuario> trabajadores = usuarioRepository.findActivosByOficinaId(ofId);
        List<CargaTrabajadorDto> cargaTrabajadores = new ArrayList<>();
        for (Usuario t : trabajadores) {
            long asignados = expedienteRepository.countByResponsableActualId(t.getId());
            cargaTrabajadores.add(CargaTrabajadorDto.builder()
                    .usuarioId(t.getId())
                    .username(t.getUsername())
                    .nombreCompleto(t.getPersona() != null ? t.getPersona().getNombreCompleto() : t.getUsername())
                    .cargo(null)
                    .totalAsignados(asignados)
                    .build());
        }

        return PanelOficinaResponse.builder()
                .oficina(derivacionMapper.toOficinaSimpleDto(oficina))
                .estadisticas(stats)
                .pendientesAsignacion(pendientesAsignacion)
                .enAtencion(enAtencion)
                .cargaTrabajadores(cargaTrabajadores)
                .build();
    }

    private EstadisticasDerivacionResponse calcularEstadisticasPorOficina(Long ofId) {
        long enviadas = derivacionRepository.countByOficinaOrigenId(ofId);
        long pendientes = derivacionRepository.countByOficinaDestinoIdAndEstado(ofId, EstadoDerivacion.PENDIENTE);
        long recibidas = derivacionRepository.countByOficinaDestinoIdAndEstado(ofId, EstadoDerivacion.RECIBIDA);
        long rechazadas = derivacionRepository.countByOficinaDestinoIdAndEstado(ofId, EstadoDerivacion.RECHAZADA);
        long devueltas = derivacionRepository.countByOficinaDestinoIdAndEstado(ofId, EstadoDerivacion.DEVUELTA);

        long asignados = expedienteRepository.countByOficinaResponsableIdAndResponsableActualIsNotNull(ofId);
        long sinResponsable = expedienteRepository.countByOficinaResponsableIdAndResponsableActualIsNull(ofId);
        long totalOficina = expedienteRepository.countByOficinaResponsableId(ofId);

        return EstadisticasDerivacionResponse.builder()
                .derivacionesEnviadas(enviadas)
                .derivacionesPendientes(pendientes)
                .derivacionesRecibidas(recibidas)
                .derivacionesRechazadas(rechazadas)
                .derivacionesDevueltas(devueltas)
                .expedientesAsignados(asignados)
                .expedientesSinResponsable(sinResponsable)
                .totalExpedientesOficina(totalOficina)
                .proximosAVencer(0L)
                .vencidos(0L)
                .build();
    }

    private EstadisticasDerivacionResponse calcularEstadisticasGlobales() {
        long enviadas = derivacionRepository.count();
        long pendientes = derivacionRepository.countByEstado(EstadoDerivacion.PENDIENTE);
        long recibidas = derivacionRepository.countByEstado(EstadoDerivacion.RECIBIDA);
        long rechazadas = derivacionRepository.countByEstado(EstadoDerivacion.RECHAZADA);
        long devueltas = derivacionRepository.countByEstado(EstadoDerivacion.DEVUELTA);

        long totalExp = expedienteRepository.count();

        return EstadisticasDerivacionResponse.builder()
                .derivacionesEnviadas(enviadas)
                .derivacionesPendientes(pendientes)
                .derivacionesRecibidas(recibidas)
                .derivacionesRechazadas(rechazadas)
                .derivacionesDevueltas(devueltas)
                .expedientesAsignados(0L)
                .expedientesSinResponsable(0L)
                .totalExpedientesOficina(totalExp)
                .proximosAVencer(0L)
                .vencidos(0L)
                .build();
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
            throw new AccessDeniedException("No tiene autorización para ver estadísticas de la oficina ID " + oficinaId);
        }
    }
}
