package pe.gob.municipalidad.sigm.modules.mesapartes.service;

import pe.gob.municipalidad.sigm.modules.mesapartes.dto.AtencionDtos.*;

public interface ArchivoExpedienteService {

    ArchivoResponse archivarExpediente(Long expedienteId, ArchivarExpedienteRequest request, String username);

    ReaperturaResponse reabrirExpediente(Long expedienteId, ReabrirExpedienteRequest request, String username);

    ArchivoResponse obtenerArchivo(Long expedienteId, String username);
}
