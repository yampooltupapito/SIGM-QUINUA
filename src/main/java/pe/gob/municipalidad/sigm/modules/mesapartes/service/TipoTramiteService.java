package pe.gob.municipalidad.sigm.modules.mesapartes.service;

import pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvDtos.TipoTramiteRequest;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvDtos.TipoTramiteResponse;

import java.util.List;

public interface TipoTramiteService {

    List<TipoTramiteResponse> listarActivos();

    List<TipoTramiteResponse> listarTodos();

    TipoTramiteResponse obtenerPorId(Long id);

    TipoTramiteResponse crearTipoTramite(TipoTramiteRequest request, String username);

    TipoTramiteResponse actualizarTipoTramite(Long id, TipoTramiteRequest request, String username);

    void cambiarEstado(Long id, String estado, String username);
}
