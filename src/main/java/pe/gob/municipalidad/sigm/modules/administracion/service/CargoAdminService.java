package pe.gob.municipalidad.sigm.modules.administracion.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.ConflictException;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.common.model.Cargo;
import pe.gob.municipalidad.sigm.common.repository.CargoRepository;
import pe.gob.municipalidad.sigm.modules.administracion.dto.CargoDtos.CargoCreateRequest;
import pe.gob.municipalidad.sigm.modules.administracion.dto.CargoDtos.CargoResponse;
import pe.gob.municipalidad.sigm.modules.administracion.dto.CargoDtos.CargoUpdateRequest;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de administración de cargos y puestos institucionales municipales.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CargoAdminService {

    private final CargoRepository cargoRepository;
    private final AuditoriaAdministrativaService auditoriaService;

    @Transactional(readOnly = true)
    public Page<CargoResponse> buscarCargos(String term, String estado, Pageable pageable) {
        return cargoRepository.searchCargos(
                (term != null && !term.isBlank()) ? term : null,
                (estado != null && !estado.isBlank()) ? estado : null,
                pageable
        ).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<CargoResponse> listarTodosActivos() {
        return cargoRepository.findByEstadoOrderByNombreAsc("ACTIVO").stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CargoResponse obtenerPorId(Long id) {
        Cargo cargo = cargoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cargo no encontrado con ID: " + id));
        return toResponse(cargo);
    }

    @Transactional
    public CargoResponse crearCargo(CargoCreateRequest request) {
        String codigo = request.getCodigo().trim().toUpperCase();
        if (cargoRepository.existsByCodigo(codigo)) {
            throw new ConflictException("Ya existe un cargo registrado con el código: " + codigo);
        }

        Cargo nuevo = Cargo.builder()
                .codigo(codigo)
                .nombre(request.getNombre().trim())
                .descripcion(request.getDescripcion())
                .nivelJerarquico(request.getNivelJerarquico() != null ? request.getNivelJerarquico() : 1)
                .estado("ACTIVO")
                .build();

        Cargo guardado = cargoRepository.save(nuevo);

        auditoriaService.registrar("CREAR_CARGO", "CARGO", guardado.getId(), "EXITO",
                "Cargo creado: " + guardado.getCodigo() + " - " + guardado.getNombre());

        return toResponse(guardado);
    }

    @Transactional
    public CargoResponse actualizarCargo(Long id, CargoUpdateRequest request) {
        Cargo cargo = cargoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cargo no encontrado con ID: " + id));

        cargo.setNombre(request.getNombre().trim());
        cargo.setDescripcion(request.getDescripcion());
        if (request.getNivelJerarquico() != null) {
            cargo.setNivelJerarquico(request.getNivelJerarquico());
        }

        Cargo guardado = cargoRepository.save(cargo);

        auditoriaService.registrar("ACTUALIZAR_CARGO", "CARGO", id, "EXITO",
                "Cargo actualizado: " + guardado.getCodigo() + " - " + guardado.getNombre());

        return toResponse(guardado);
    }

    @Transactional
    public CargoResponse cambiarEstado(Long id, String nuevoEstado) {
        Cargo cargo = cargoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cargo no encontrado con ID: " + id));

        cargo.setEstado(nuevoEstado.toUpperCase());
        Cargo guardado = cargoRepository.save(cargo);

        auditoriaService.registrar("CAMBIO_ESTADO_CARGO", "CARGO", id, "EXITO",
                "Estado de cargo " + cargo.getCodigo() + " cambiado a " + nuevoEstado);

        return toResponse(guardado);
    }

    private CargoResponse toResponse(Cargo c) {
        return CargoResponse.builder()
                .id(c.getId())
                .codigo(c.getCodigo())
                .nombre(c.getNombre())
                .descripcion(c.getDescripcion())
                .nivelJerarquico(c.getNivelJerarquico())
                .estado(c.getEstado())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}
