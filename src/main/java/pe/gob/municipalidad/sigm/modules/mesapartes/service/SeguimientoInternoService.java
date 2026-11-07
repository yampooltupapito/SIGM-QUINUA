package pe.gob.municipalidad.sigm.modules.mesapartes.service;

import pe.gob.municipalidad.sigm.modules.mesapartes.dto.DerivacionDtos.SeguimientoInternoResponse;

public interface SeguimientoInternoService {

    SeguimientoInternoResponse obtenerSeguimiento(Long expedienteId, String username);
}
