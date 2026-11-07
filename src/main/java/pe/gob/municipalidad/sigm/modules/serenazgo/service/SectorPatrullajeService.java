package pe.gob.municipalidad.sigm.modules.serenazgo.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.*;

import java.util.List;
import java.util.UUID;

public interface SectorPatrullajeService {

    Page<SectorResponse> listarSectores(SectorFilter filter, Pageable pageable);

    SectorDetailResponse obtenerDetallePorId(UUID id);

    SectorResponse crearSector(SectorCreateRequest request, String username);

    SectorResponse actualizarSector(UUID id, SectorUpdateRequest request, String username);

    SectorResponse cambiarEstado(UUID id, CambioEstadoTerritorioRequest request, String username);

    SectorResponse guardarDelimitacion(UUID id, DelimitacionGeoJsonRequest request, String username);

    void archivarSector(UUID id, String motivo, String username);

    // Métodos previos para compatibilidad hacia atrás
    List<SectorPatrullajeResponse> listarSectoresActivos();

    SectorPatrullajeResponse obtenerSectorPorId(UUID sectorId);

    List<ZonaPatrullajeResponse> listarZonasPorSector(UUID sectorId);
}
