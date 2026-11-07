package pe.gob.municipalidad.sigm.modules.serenazgo.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.TurnoCreateRequest;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.TurnoResponse;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.TurnoUpdateRequest;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoTurnoSerenazgo;

import java.util.List;
import java.util.UUID;

public interface TurnoSerenazgoService {

    TurnoResponse crearTurno(TurnoCreateRequest request, String username);

    TurnoResponse actualizarTurno(UUID id, TurnoUpdateRequest request, String username);

    TurnoResponse cambiarEstadoTurno(UUID id, EstadoTurnoSerenazgo nuevoEstado, String username);

    TurnoResponse obtenerTurnoPorId(UUID id);

    List<TurnoResponse> listarTurnosActivos();

    Page<TurnoResponse> buscarTurnos(String termino, EstadoTurnoSerenazgo estado, Pageable pageable);
}
