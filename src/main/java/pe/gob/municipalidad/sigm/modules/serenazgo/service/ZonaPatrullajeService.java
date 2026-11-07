package pe.gob.municipalidad.sigm.modules.serenazgo.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.*;

import java.util.UUID;

public interface ZonaPatrullajeService {

    Page<ZonaPatrullajeResponse> listarZonas(ZonaFilter filter, Pageable pageable);

    ZonaDetailResponse obtenerDetallePorId(UUID id);

    ZonaPatrullajeResponse crearZona(ZonaCreateRequest request, String username);

    ZonaPatrullajeResponse actualizarZona(UUID id, ZonaUpdateRequest request, String username);

    ZonaPatrullajeResponse cambiarEstado(UUID id, CambioEstadoTerritorioRequest request, String username);

    ZonaPatrullajeResponse reasignarSector(UUID id, ReasignarZonaRequest request, String username);

    ZonaPatrullajeResponse guardarDelimitacion(UUID id, DelimitacionGeoJsonRequest request, String username);

    void archivarZona(UUID id, String motivo, String username);
}
