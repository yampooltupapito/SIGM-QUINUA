package pe.gob.municipalidad.sigm.modules.serenazgo.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.*;

import java.util.UUID;

public interface EquipamientoSerenazgoService {

    EquipamientoResponse registrar(EquipamientoCreateRequest request, String username);

    EquipamientoResponse actualizar(UUID id, EquipamientoUpdateRequest request, String username);

    EquipamientoDetailResponse obtenerDetalle(UUID id);

    Page<EquipamientoResponse> listar(EquipamientoFilter filter, Pageable pageable);

    AsignacionEquipamientoResponse asignar(UUID id, AsignarEquipamientoRequest request, String username);

    AsignacionEquipamientoResponse devolver(UUID id, DevolverEquipamientoRequest request, String username);

    MantenimientoResponse registrarMantenimiento(UUID id, RegistroMantenimientoRequest request, String username);

    IncidenciaTecnicaResponse registrarIncidencia(UUID id, RegistroIncidenciaTecnicaRequest request, String username);
}
