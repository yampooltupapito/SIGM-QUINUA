package pe.gob.municipalidad.sigm.modules.mesapartes.service;

import pe.gob.municipalidad.sigm.modules.mesapartes.dto.DerivacionDtos.EstadisticasDerivacionResponse;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.DerivacionDtos.PanelOficinaResponse;

public interface EstadisticasDerivacionService {

    EstadisticasDerivacionResponse obtenerEstadisticas(Long oficinaId, String username);

    PanelOficinaResponse obtenerPanelOficina(Long oficinaId, String username);
}
