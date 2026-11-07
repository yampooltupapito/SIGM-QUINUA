package pe.gob.municipalidad.sigm.modules.mesapartes.service;

import pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvAdminDtos.ActualizarPrioridadRequest;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvAdminDtos.CambioPrioridadResponse;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvDtos.ExpedienteResponse;

import java.util.List;

/**
 * Servicio para gestión y auditoría de prioridades de expedientes administrativos.
 */
public interface PrioridadExpedienteService {

    ExpedienteResponse actualizarPrioridad(Long expedienteId, ActualizarPrioridadRequest request, String username);

    List<CambioPrioridadResponse> listarHistorialPrioridades(Long expedienteId, String username);
}
