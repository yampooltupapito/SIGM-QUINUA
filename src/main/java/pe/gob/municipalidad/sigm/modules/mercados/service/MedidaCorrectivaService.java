package pe.gob.municipalidad.sigm.modules.mercados.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.administracion.service.AuditoriaAdministrativaService;
import pe.gob.municipalidad.sigm.modules.mercados.dto.MedidaCorrectivaRequest;
import pe.gob.municipalidad.sigm.modules.mercados.dto.MedidaCorrectivaResponse;
import pe.gob.municipalidad.sigm.modules.mercados.dto.SubsanacionRequest;
import pe.gob.municipalidad.sigm.modules.mercados.dto.VerificacionSubsanacionRequest;
import pe.gob.municipalidad.sigm.modules.mercados.entity.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoMedidaCorrectiva;
import pe.gob.municipalidad.sigm.modules.mercados.repository.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MedidaCorrectivaService {

    private final MedidaCorrectivaMercadoRepository medidaRepository;
    private final HallazgoInspeccionRepository hallazgoRepository;
    private final SubsanacionMedidaMercadoRepository subsanacionRepository;
    private final ComercianteRepository comercianteRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuditoriaAdministrativaService auditoriaService;

    public MedidaCorrectivaService(MedidaCorrectivaMercadoRepository medidaRepository,
                                   HallazgoInspeccionRepository hallazgoRepository,
                                   SubsanacionMedidaMercadoRepository subsanacionRepository,
                                   ComercianteRepository comercianteRepository,
                                   UsuarioRepository usuarioRepository,
                                   AuditoriaAdministrativaService auditoriaService) {
        this.medidaRepository = medidaRepository;
        this.hallazgoRepository = hallazgoRepository;
        this.subsanacionRepository = subsanacionRepository;
        this.comercianteRepository = comercianteRepository;
        this.usuarioRepository = usuarioRepository;
        this.auditoriaService = auditoriaService;
    }

    @Transactional
    public MedidaCorrectivaResponse crearMedida(UUID hallazgoId, MedidaCorrectivaRequest request, String username) {
        HallazgoInspeccionMercado hallazgo = hallazgoRepository.findById(hallazgoId)
                .orElseThrow(() -> new ResourceNotFoundException("Hallazgo no encontrado: " + hallazgoId));

        if (request.getFechaLimite() != null && request.getFechaLimite().isBefore(request.getFechaAsignacion())) {
            throw new BusinessException("La fecha límite no puede ser anterior a la fecha de asignación.");
        }

        Usuario responsable = null;
        if (request.getResponsableId() != null) {
            responsable = usuarioRepository.findById(request.getResponsableId()).orElse(null);
        }

        Comerciante comerciante = null;
        if (request.getComercianteResponsableId() != null) {
            comerciante = comercianteRepository.findById(request.getComercianteResponsableId()).orElse(null);
        } else if (hallazgo.getInspeccion().getComerciante() != null) {
            comerciante = hallazgo.getInspeccion().getComerciante();
        }

        MedidaCorrectivaMercado entity = MedidaCorrectivaMercado.builder()
                .hallazgo(hallazgo)
                .descripcion(request.getDescripcion().trim())
                .tipoResponsable(request.getTipoResponsable() != null ? request.getTipoResponsable() : "COMERCIANTE_TITULAR")
                .responsable(responsable)
                .comercianteResponsable(comerciante)
                .fechaAsignacion(request.getFechaAsignacion())
                .fechaLimite(request.getFechaLimite())
                .estado(EstadoMedidaCorrectiva.PENDIENTE)
                .observaciones(request.getObservaciones())
                .subsanaciones(new ArrayList<>())
                .build();

        MedidaCorrectivaMercado guardada = medidaRepository.save(entity);

        // Actualizar estado del hallazgo a CON_MEDIDA_CORRECTIVA
        if ("ABIERTO".equalsIgnoreCase(hallazgo.getEstado())) {
            hallazgo.setEstado("CON_MEDIDA_CORRECTIVA");
            hallazgoRepository.save(hallazgo);
        }

        auditoriaService.registrar("CREAR_MEDIDA_CORRECTIVA_MERCADO", "mercados_medidas_correctivas", guardada.getId(),
                "EXITO", "Medida correctiva asignada al hallazgo: " + hallazgo.getCodigo());

        return toResponse(guardada);
    }

    @Transactional
    public MedidaCorrectivaResponse presentarSubsanacion(UUID medidaId, SubsanacionRequest request, String username, Long usuarioId) {
        MedidaCorrectivaMercado medida = medidaRepository.findById(medidaId)
                .orElseThrow(() -> new ResourceNotFoundException("Medida correctiva no encontrada: " + medidaId));

        if (medida.getEstado() == EstadoMedidaCorrectiva.SUBSANADA) {
            throw new BusinessException("La medida correctiva ya se encuentra debidamente subsanada.");
        }

        Usuario presentadoPor = resolverUsuario(usuarioId, username);

        SubsanacionMedidaMercado subsanacion = SubsanacionMedidaMercado.builder()
                .medidaCorrectiva(medida)
                .descripcionSubsanacion(request.getDescripcionSubsanacion().trim())
                .fechaPresentacion(OffsetDateTime.now())
                .presentadoPor(presentadoPor)
                .documentoEvidenciaId(request.getDocumentoEvidenciaId())
                .estadoVerificacion("PENDIENTE_REVISION")
                .build();

        subsanacionRepository.save(subsanacion);

        medida.setEstado(EstadoMedidaCorrectiva.PRESENTADA_PARA_VERIFICACION);
        MedidaCorrectivaMercado guardada = medidaRepository.save(medida);

        auditoriaService.registrar("PRESENTAR_SUBSANACION_MEDIDA", "mercados_subsanaciones_medida", subsanacion.getId(),
                "EXITO", "Subsanación presentada para medida de hallazgo: " + medida.getHallazgo().getCodigo());

        return toResponse(guardada);
    }

    @Transactional
    public MedidaCorrectivaResponse verificarSubsanacion(UUID subsanacionId, VerificacionSubsanacionRequest request, String username, Long inspectorId) {
        SubsanacionMedidaMercado subsanacion = subsanacionRepository.findById(subsanacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Registro de subsanación no encontrado: " + subsanacionId));

        MedidaCorrectivaMercado medida = subsanacion.getMedidaCorrectiva();

        Usuario inspector = resolverUsuario(inspectorId, username);

        subsanacion.setFechaVerificacion(OffsetDateTime.now());
        subsanacion.setInspectorVerificador(inspector);
        subsanacion.setObservacionesVerificacion(request.getObservaciones().trim());

        if (Boolean.TRUE.equals(request.getAprobada())) {
            subsanacion.setEstadoVerificacion("APROBADA_CONFORME");
            medida.setEstado(EstadoMedidaCorrectiva.SUBSANADA);

            // Verificar si todas las medidas del hallazgo están subsanadas
            HallazgoInspeccionMercado hallazgo = medida.getHallazgo();
            boolean todasSubsanadas = hallazgo.getMedidasCorrectivas().stream()
                    .allMatch(m -> m.getId().equals(medida.getId()) || m.getEstado() == EstadoMedidaCorrectiva.SUBSANADA);

            if (todasSubsanadas) {
                hallazgo.setEstado("SUBSANADO");
                hallazgoRepository.save(hallazgo);
            }
        } else {
            subsanacion.setEstadoVerificacion("RECHAZADA_INSUFICIENTE");
            // Si la fecha límite expiró, pasa a NO_SUBSANADA, caso contrario a EN_PROCESO
            if (medida.getFechaLimite() != null && medida.getFechaLimite().isBefore(LocalDate.now())) {
                medida.setEstado(EstadoMedidaCorrectiva.NO_SUBSANADA);
            } else {
                medida.setEstado(EstadoMedidaCorrectiva.EN_PROCESO);
            }
        }

        subsanacionRepository.save(subsanacion);
        MedidaCorrectivaMercado guardada = medidaRepository.save(medida);

        auditoriaService.registrar("VERIFICAR_SUBSANACION_MEDIDA", "mercados_subsanaciones_medida", subsanacion.getId(),
                "EXITO", "Subsanación evaluada: " + subsanacion.getEstadoVerificacion() + " por " + inspector.getUsername());

        return toResponse(guardada);
    }

    @Transactional(readOnly = true)
    public Page<MedidaCorrectivaResponse> buscarSeguimientoPaginado(
            EstadoMedidaCorrectiva estado, UUID mercadoId, UUID comercianteId, boolean soloVencidas, Pageable pageable) {
        return medidaRepository.buscarSeguimientoPaginado(estado, mercadoId, comercianteId, soloVencidas, LocalDate.now(), pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public MedidaCorrectivaResponse obtenerPorId(UUID id) {
        return medidaRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Medida correctiva no encontrada: " + id));
    }

    public MedidaCorrectivaResponse toResponse(MedidaCorrectivaMercado m) {
        boolean vencida = m.getFechaLimite() != null && m.getFechaLimite().isBefore(LocalDate.now()) &&
                (m.getEstado() == EstadoMedidaCorrectiva.PENDIENTE || m.getEstado() == EstadoMedidaCorrectiva.EN_PROCESO);

        List<MedidaCorrectivaResponse.SubsanacionDto> subsanacionesDtos = new ArrayList<>();
        if (m.getSubsanaciones() != null) {
            subsanacionesDtos = m.getSubsanaciones().stream()
                    .map(s -> MedidaCorrectivaResponse.SubsanacionDto.builder()
                            .id(s.getId())
                            .descripcionSubsanacion(s.getDescripcionSubsanacion())
                            .fechaPresentacion(s.getFechaPresentacion())
                            .presentadoPorNombre(s.getPresentadoPor() != null ? s.getPresentadoPor().getUsername() : null)
                            .estadoVerificacion(s.getEstadoVerificacion())
                            .fechaVerificacion(s.getFechaVerificacion())
                            .inspectorVerificadorNombre(s.getInspectorVerificador() != null ? s.getInspectorVerificador().getUsername() : null)
                            .observacionesVerificacion(s.getObservacionesVerificacion())
                            .build())
                    .collect(Collectors.toList());
        }

        return MedidaCorrectivaResponse.builder()
                .id(m.getId())
                .hallazgoId(m.getHallazgo().getId())
                .hallazgoCodigo(m.getHallazgo().getCodigo())
                .hallazgoDescripcion(m.getHallazgo().getDescripcion())
                .inspeccionId(m.getHallazgo().getInspeccion().getId())
                .inspeccionCodigo(m.getHallazgo().getInspeccion().getCodigoInspeccion())
                .mercadoNombre(m.getHallazgo().getInspeccion().getMercado() != null ? m.getHallazgo().getInspeccion().getMercado().getNombre() : null)
                .puestoCodigo(m.getHallazgo().getInspeccion().getPuesto() != null ? m.getHallazgo().getInspeccion().getPuesto().getCodigo() : null)
                .descripcion(m.getDescripcion())
                .tipoResponsable(m.getTipoResponsable())
                .responsableId(m.getResponsable() != null ? m.getResponsable().getId() : null)
                .responsableNombre(m.getResponsable() != null ? m.getResponsable().getUsername() : null)
                .comercianteResponsableId(m.getComercianteResponsable() != null ? m.getComercianteResponsable().getId() : null)
                .comercianteResponsableNombre(m.getComercianteResponsable() != null && m.getComercianteResponsable().getPersona() != null ?
                        m.getComercianteResponsable().getPersona().getNombreCompleto() : null)
                .fechaAsignacion(m.getFechaAsignacion())
                .fechaLimite(m.getFechaLimite())
                .estado(m.getEstado())
                .estadoDescripcion(m.getEstado() != null ? m.getEstado().getDescripcion() : null)
                .vencida(vencida)
                .observaciones(m.getObservaciones())
                .subsanaciones(subsanacionesDtos)
                .createdAt(m.getCreatedAt())
                .build();
    }

    private Usuario resolverUsuario(Long usuarioId, String username) {
        if (usuarioId != null) {
            Optional<Usuario> u = usuarioRepository.findById(usuarioId);
            if (u.isPresent()) {
                return u.get();
            }
        }
        if (username != null && !username.isBlank() && !"sistema".equalsIgnoreCase(username.trim())) {
            Optional<Usuario> u = usuarioRepository.findByUsername(username.trim());
            if (u.isPresent()) {
                return u.get();
            }
        }
        if (username != null && !username.isBlank()) {
            Optional<Usuario> u = usuarioRepository.findByUsername(username.trim());
            if (u.isPresent()) {
                return u.get();
            }
        }
        return usuarioRepository.findAll().stream()
                .filter(u -> Boolean.TRUE.equals(u.getEnabled()))
                .findFirst()
                .orElse(null);
    }
}
