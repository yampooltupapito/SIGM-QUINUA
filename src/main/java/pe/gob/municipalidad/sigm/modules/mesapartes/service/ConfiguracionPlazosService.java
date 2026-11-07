package pe.gob.municipalidad.sigm.modules.mesapartes.service;

import pe.gob.municipalidad.sigm.modules.mesapartes.dto.ConfiguracionMpvDtos.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.UnidadPlazo;

import java.util.List;

public interface ConfiguracionPlazosService {

    List<TramiteAdminResponse> listarPlazosPorTramite();

    TramiteAdminResponse actualizarPlazo(Long tipoTramiteId, Integer plazoDias, UnidadPlazo unidadPlazo, String baseNormativa, String motivo, String username, String ipOrigen);
}
