package pe.gob.municipalidad.sigm.modules.administracion.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.ConflictException;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.common.model.TipoActor;
import pe.gob.municipalidad.sigm.common.repository.TipoActorRepository;
import pe.gob.municipalidad.sigm.modules.administracion.dto.TipoActorDtos.TipoActorCreateRequest;
import pe.gob.municipalidad.sigm.modules.administracion.dto.TipoActorDtos.TipoActorResponse;
import pe.gob.municipalidad.sigm.modules.administracion.dto.TipoActorDtos.TipoActorUpdateRequest;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de administración del catálogo dinámico de tipos de actores institucionales.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TipoActorAdminService {

    private final TipoActorRepository tipoActorRepository;
    private final AuditoriaAdministrativaService auditoriaService;

    @Transactional(readOnly = true)
    public Page<TipoActorResponse> buscarTiposActores(String term, String estado, Pageable pageable) {
        return tipoActorRepository.searchTiposActores(
                (term != null && !term.isBlank()) ? term : null,
                (estado != null && !estado.isBlank()) ? estado : null,
                pageable
        ).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<TipoActorResponse> listarTodosActivos() {
        return tipoActorRepository.findByEstadoOrderByNombreAsc("ACTIVO").stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TipoActorResponse obtenerPorId(Long id) {
        TipoActor t = tipoActorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de actor no encontrado con ID: " + id));
        return toResponse(t);
    }

    @Transactional
    public TipoActorResponse crearTipoActor(TipoActorCreateRequest request) {
        String codigo = request.getCodigo().trim().toUpperCase();
        if (tipoActorRepository.existsByCodigo(codigo)) {
            throw new ConflictException("Ya existe un tipo de actor registrado con el código: " + codigo);
        }

        TipoActor nuevo = TipoActor.builder()
                .codigo(codigo)
                .nombre(request.getNombre().trim())
                .descripcion(request.getDescripcion())
                .estado("ACTIVO")
                .build();

        TipoActor guardado = tipoActorRepository.save(nuevo);

        auditoriaService.registrar("CREAR_TIPO_ACTOR", "TIPO_ACTOR", guardado.getId(), "EXITO",
                "Tipo de actor creado: " + guardado.getCodigo() + " - " + guardado.getNombre());

        return toResponse(guardado);
    }

    @Transactional
    public TipoActorResponse actualizarTipoActor(Long id, TipoActorUpdateRequest request) {
        TipoActor actor = tipoActorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de actor no encontrado con ID: " + id));

        actor.setNombre(request.getNombre().trim());
        actor.setDescripcion(request.getDescripcion());

        TipoActor guardado = tipoActorRepository.save(actor);

        auditoriaService.registrar("ACTUALIZAR_TIPO_ACTOR", "TIPO_ACTOR", id, "EXITO",
                "Tipo de actor actualizado: " + guardado.getCodigo() + " - " + guardado.getNombre());

        return toResponse(guardado);
    }

    @Transactional
    public TipoActorResponse cambiarEstado(Long id, String nuevoEstado) {
        TipoActor actor = tipoActorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de actor no encontrado con ID: " + id));

        actor.setEstado(nuevoEstado.toUpperCase());
        TipoActor guardado = tipoActorRepository.save(actor);

        auditoriaService.registrar("CAMBIO_ESTADO_TIPO_ACTOR", "TIPO_ACTOR", id, "EXITO",
                "Estado de tipo de actor " + actor.getCodigo() + " cambiado a " + nuevoEstado);

        return toResponse(guardado);
    }

    private TipoActorResponse toResponse(TipoActor t) {
        return TipoActorResponse.builder()
                .id(t.getId())
                .codigo(t.getCodigo())
                .nombre(t.getNombre())
                .descripcion(t.getDescripcion())
                .estado(t.getEstado())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }
}
