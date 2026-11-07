package pe.gob.municipalidad.sigm.modules.mesapartes.service;

import pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvAdminDtos.RegistrarObservacionAdminRequest;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvDtos.ObservacionResponse;

import java.util.List;

/**
 * Servicio de formulación, gestión y notificación de observaciones a expedientes.
 */
public interface ObservacionExpedienteService {

    ObservacionResponse registrarObservacion(Long expedienteId, RegistrarObservacionAdminRequest request, String username);

    List<ObservacionResponse> listarObservaciones(Long expedienteId, String username);
}
