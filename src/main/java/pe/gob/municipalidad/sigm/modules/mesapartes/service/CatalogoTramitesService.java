package pe.gob.municipalidad.sigm.modules.mesapartes.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.ConfiguracionMpvDtos.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoCatalogoTramite;

import java.util.List;

public interface CatalogoTramitesService {

    Page<TramiteAdminResponse> listarTramites(EstadoCatalogoTramite estadoCatalogo, String query, Pageable pageable);

    TramiteAdminResponse obtenerPorId(Long id);

    TramiteAdminResponse crearBorrador(GuardarTramiteRequest request, String username, String ipOrigen);

    TramiteAdminResponse editarBorrador(Long id, GuardarTramiteRequest request, String username, String ipOrigen);

    TramiteAdminResponse enviarARevision(Long id, AccionTransicionRequest request, String username, String ipOrigen);

    TramiteAdminResponse aprobar(Long id, AccionTransicionRequest request, String username, String ipOrigen);

    TramiteAdminResponse publicar(Long id, AccionTransicionRequest request, String username, String ipOrigen);

    TramiteAdminResponse suspender(Long id, AccionTransicionRequest request, String username, String ipOrigen);

    TramiteAdminResponse desactivar(Long id, AccionTransicionRequest request, String username, String ipOrigen);

    List<TramiteVersionResponse> listarVersiones(Long tipoTramiteId);

    TramiteVersionResponse obtenerVersion(Long tipoTramiteId, Integer numeroVersion);
}
