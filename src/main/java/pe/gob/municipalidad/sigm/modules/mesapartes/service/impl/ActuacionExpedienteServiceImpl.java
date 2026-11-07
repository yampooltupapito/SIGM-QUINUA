package pe.gob.municipalidad.sigm.modules.mesapartes.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.document.entity.Documento;
import pe.gob.municipalidad.sigm.common.document.repository.DocumentoRepository;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.common.model.PersonaOficina;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.PersonaOficinaRepository;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.DerivacionDtos.ActuacionResponse;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.DerivacionDtos.RegistrarActuacionRequest;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.ActuacionDocumento;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.ActuacionExpediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.Expediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.ExpedienteHistorial;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.VisibilidadObservacion;
import pe.gob.municipalidad.sigm.modules.mesapartes.mapper.DerivacionMapper;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.ActuacionDocumentoRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.ActuacionExpedienteRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.ExpedienteHistorialRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.ExpedienteRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.ActuacionExpedienteService;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ActuacionExpedienteServiceImpl implements ActuacionExpedienteService {

    private final ActuacionExpedienteRepository actuacionRepository;
    private final ActuacionDocumentoRepository actuacionDocumentoRepository;
    private final ExpedienteRepository expedienteRepository;
    private final ExpedienteHistorialRepository historialRepository;
    private final DocumentoRepository documentoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PersonaOficinaRepository personaOficinaRepository;
    private final DerivacionMapper derivacionMapper;

    public ActuacionExpedienteServiceImpl(
            ActuacionExpedienteRepository actuacionRepository,
            ActuacionDocumentoRepository actuacionDocumentoRepository,
            ExpedienteRepository expedienteRepository,
            ExpedienteHistorialRepository historialRepository,
            DocumentoRepository documentoRepository,
            UsuarioRepository usuarioRepository,
            PersonaOficinaRepository personaOficinaRepository,
            DerivacionMapper derivacionMapper) {
        this.actuacionRepository = actuacionRepository;
        this.actuacionDocumentoRepository = actuacionDocumentoRepository;
        this.expedienteRepository = expedienteRepository;
        this.historialRepository = historialRepository;
        this.documentoRepository = documentoRepository;
        this.usuarioRepository = usuarioRepository;
        this.personaOficinaRepository = personaOficinaRepository;
        this.derivacionMapper = derivacionMapper;
    }

    @Override
    @Transactional
    public ActuacionResponse registrarActuacion(Long expedienteId, RegistrarActuacionRequest request, String username) {
        if (request == null || request.getDescripcion() == null || request.getDescripcion().trim().isBlank()) {
            throw new BusinessException("La descripción de la actuación es obligatoria.");
        }
        if (request.getTipo() == null) {
            throw new BusinessException("El tipo de actuación es obligatorio.");
        }

        Usuario usuario = obtenerUsuario(username);
        Expediente expediente = expedienteRepository.findByIdWithDetalles(expedienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Expediente no encontrado con ID: " + expedienteId));

        Oficina oficina = expediente.getOficinaResponsable();
        if (oficina == null) {
            throw new BusinessException("El expediente no tiene oficina responsable asignada.");
        }

        validarPertenenciaOAdmin(usuario, oficina.getId());

        OffsetDateTime ahora = OffsetDateTime.now();
        VisibilidadObservacion visibilidad = request.getVisibilidad() != null ? request.getVisibilidad() : VisibilidadObservacion.INTERNA;

        ActuacionExpediente actuacion = ActuacionExpediente.builder()
                .expediente(expediente)
                .tipo(request.getTipo())
                .descripcion(request.getDescripcion().trim())
                .usuarioResponsable(usuario)
                .oficina(oficina)
                .fechaActuacion(ahora)
                .visibilidad(visibilidad)
                .createdBy(usuario.getUsername())
                .build();

        ActuacionExpediente guardada = actuacionRepository.save(actuacion);

        // Documentos adjuntos
        if (request.getDocumentoIds() != null && !request.getDocumentoIds().isEmpty()) {
            List<ActuacionDocumento> actDocs = new ArrayList<>();
            for (Long docId : request.getDocumentoIds()) {
                Documento doc = documentoRepository.findById(docId)
                        .orElseThrow(() -> new ResourceNotFoundException("Documento no encontrado con ID: " + docId));

                ActuacionDocumento ad = ActuacionDocumento.builder()
                        .actuacion(guardada)
                        .documento(doc)
                        .tipoDocumento(request.getTipo().name())
                        .createdAt(ahora)
                        .build();

                actDocs.add(actuacionDocumentoRepository.save(ad));
            }
            guardada.setDocumentos(actDocs);
        }

        // Registrar en historial
        historialRepository.save(ExpedienteHistorial.builder()
                .expediente(expediente)
                .estadoAnterior(expediente.getEstadoActual())
                .estadoNuevo(expediente.getEstadoActual())
                .accion("ACTUACION_" + request.getTipo().name())
                .observacion("Actuación interna (" + request.getTipo() + "): " + request.getDescripcion().trim())
                .usuarioResponsable(usuario)
                .oficina(oficina)
                .fechaMovimiento(ahora)
                .identificadorCorrelacion(UUID.randomUUID().toString())
                .build());

        return derivacionMapper.toActuacionResponse(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActuacionResponse> listarActuaciones(Long expedienteId, String username) {
        Usuario usuario = obtenerUsuario(username);
        boolean esPersonal = esPersonalMunicipal(usuario);

        List<ActuacionExpediente> list = esPersonal
                ? actuacionRepository.findByExpedienteIdOrderByFechaActuacionDesc(expedienteId)
                : actuacionRepository.findByExpedienteIdAndVisibilidadOrderByFechaActuacionDesc(expedienteId, VisibilidadObservacion.CIUDADANO);

        return list.stream()
                .map(derivacionMapper::toActuacionResponse)
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

    private boolean esPersonalMunicipal(Usuario usuario) {
        return usuario.getRoles().stream()
                .anyMatch(r -> !r.getCodigo().equals("ROLE_CIUDADANO") && !r.getCodigo().equals("ROLE_ADMINISTRADO"));
    }

    private void validarPertenenciaOAdmin(Usuario usuario, Long oficinaId) {
        if (esAdmin(usuario)) return;
        Long ofId = resolverOficinaUsuario(usuario);
        if (ofId == null || !ofId.equals(oficinaId)) {
            throw new AccessDeniedException("No tiene autorización para registrar actuaciones en la oficina ID " + oficinaId);
        }
    }
}
