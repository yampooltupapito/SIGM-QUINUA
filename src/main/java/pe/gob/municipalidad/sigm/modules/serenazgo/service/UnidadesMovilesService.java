package pe.gob.municipalidad.sigm.modules.serenazgo.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.*;

import java.util.UUID;

public interface UnidadesMovilesService {

    UnidadMovilResponse registrar(UnidadMovilCreateRequest request, String username);

    UnidadMovilResponse actualizar(UUID id, UnidadMovilUpdateRequest request, String username);

    UnidadMovilDetailResponse obtenerDetalle(UUID id);

    Page<UnidadMovilResponse> listar(UnidadMovilFilter filter, Pageable pageable);

    UnidadMovilResponse cambiarEstado(UUID id, CambioEstadoUnidadRequest request, String username);

    AsignacionUnidadResponse asignar(UUID id, AsignarUnidadRequest request, String username);

    AsignacionUnidadResponse devolver(UUID id, DevolverUnidadRequest request, String username);

    MantenimientoResponse registrarMantenimiento(UUID id, RegistroMantenimientoRequest request, String username);

    IncidenciaTecnicaResponse registrarIncidencia(UUID id, RegistroIncidenciaTecnicaRequest request, String username);

    byte[] generarFichaPdf(UUID id);
}
