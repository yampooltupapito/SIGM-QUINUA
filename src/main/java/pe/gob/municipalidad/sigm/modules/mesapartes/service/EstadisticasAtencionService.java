package pe.gob.municipalidad.sigm.modules.mesapartes.service;

import pe.gob.municipalidad.sigm.modules.mesapartes.dto.AtencionDtos.EstadisticasAtencionResponse;

public interface EstadisticasAtencionService {

    EstadisticasAtencionResponse obtenerEstadisticasAtencion(Long oficinaId, String username);
}
