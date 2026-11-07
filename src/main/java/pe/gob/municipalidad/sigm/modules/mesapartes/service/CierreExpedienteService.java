package pe.gob.municipalidad.sigm.modules.mesapartes.service;

import pe.gob.municipalidad.sigm.modules.mesapartes.dto.AtencionDtos.*;

public interface CierreExpedienteService {

    CierreResponse cerrarExpediente(Long expedienteId, CerrarExpedienteRequest request, String username);

    CierreResponse obtenerCierre(Long expedienteId, String username);
}
