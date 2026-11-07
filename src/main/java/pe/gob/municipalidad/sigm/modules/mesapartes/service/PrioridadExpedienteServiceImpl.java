package pe.gob.municipalidad.sigm.modules.mesapartes.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvAdminDtos.ActualizarPrioridadRequest;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvAdminDtos.CambioPrioridadResponse;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvDtos.ExpedienteResponse;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.CambioPrioridadExpediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.Expediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.ExpedienteHistorial;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.PrioridadExpediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.mapper.MpvMapper;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.CambioPrioridadExpedienteRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.ExpedienteHistorialRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.ExpedienteRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PrioridadExpedienteServiceImpl implements PrioridadExpedienteService {

    private final ExpedienteRepository expedienteRepository;
    private final CambioPrioridadExpedienteRepository cambioPrioridadRepository;
    private final ExpedienteHistorialRepository historialRepository;
    private final UsuarioRepository usuarioRepository;
    private final MpvMapper mpvMapper;

    public PrioridadExpedienteServiceImpl(ExpedienteRepository expedienteRepository,
                                          CambioPrioridadExpedienteRepository cambioPrioridadRepository,
                                          ExpedienteHistorialRepository historialRepository,
                                          UsuarioRepository usuarioRepository,
                                          MpvMapper mpvMapper) {
        this.expedienteRepository = expedienteRepository;
        this.cambioPrioridadRepository = cambioPrioridadRepository;
        this.historialRepository = historialRepository;
        this.usuarioRepository = usuarioRepository;
        this.mpvMapper = mpvMapper;
    }

    @Override
    @Transactional
    public ExpedienteResponse actualizarPrioridad(Long expedienteId, ActualizarPrioridadRequest request, String username) {
        Usuario usuario = obtenerUsuario(username);

        if (esCiudadano(usuario)) {
            throw new AccessDeniedException("Operación no autorizada: la gestión de prioridades es facultad exclusiva de los servidores públicos.");
        }

        Expediente expediente = expedienteRepository.findByIdWithDetalles(expedienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Expediente no encontrado con ID: " + expedienteId));

        PrioridadExpediente anterior = expediente.getPrioridad();
        if (anterior == request.getPrioridad()) {
            return mpvMapper.toExpedienteResponse(expediente);
        }

        CambioPrioridadExpediente cambio = CambioPrioridadExpediente.builder()
                .expediente(expediente)
                .prioridadAnterior(anterior)
                .prioridadNueva(request.getPrioridad())
                .motivo(request.getMotivo().trim())
                .usuario(usuario)
                .fechaCambio(OffsetDateTime.now())
                .build();
        cambioPrioridadRepository.save(cambio);

        expediente.setPrioridad(request.getPrioridad());
        Expediente guardado = expedienteRepository.save(expediente);

        ExpedienteHistorial hist = ExpedienteHistorial.builder()
                .expediente(guardado)
                .estadoAnterior(guardado.getEstadoActual())
                .estadoNuevo(guardado.getEstadoActual())
                .accion("CAMBIO_PRIORIDAD")
                .observacion("Prioridad modificada de " + anterior + " a " + request.getPrioridad() + ". Motivo: " + request.getMotivo().trim())
                .usuarioResponsable(usuario)
                .oficina(guardado.getOficinaResponsable())
                .fechaMovimiento(OffsetDateTime.now())
                .build();
        historialRepository.save(hist);

        return mpvMapper.toExpedienteResponse(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CambioPrioridadResponse> listarHistorialPrioridades(Long expedienteId, String username) {
        return cambioPrioridadRepository.findByExpedienteIdOrderByFechaCambioDesc(expedienteId)
                .stream()
                .map(mpvMapper::toCambioPrioridadResponse)
                .collect(Collectors.toList());
    }

    private Usuario obtenerUsuario(String username) {
        return usuarioRepository.findByUsernameWithRolesAndPersona(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));
    }

    private boolean esCiudadano(Usuario usuario) {
        if (usuario.getRoles() == null || usuario.getRoles().isEmpty()) return true;
        return usuario.getRoles().stream()
                .allMatch(r -> r.getNombre().equalsIgnoreCase("ROLE_CIUDADANO"));
    }
}
