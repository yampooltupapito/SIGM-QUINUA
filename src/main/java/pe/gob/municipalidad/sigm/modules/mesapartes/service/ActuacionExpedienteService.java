package pe.gob.municipalidad.sigm.modules.mesapartes.service;

import pe.gob.municipalidad.sigm.modules.mesapartes.dto.DerivacionDtos.ActuacionResponse;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.DerivacionDtos.RegistrarActuacionRequest;

import java.util.List;

public interface ActuacionExpedienteService {

    ActuacionResponse registrarActuacion(Long expedienteId, RegistrarActuacionRequest request, String username);

    List<ActuacionResponse> listarActuaciones(Long expedienteId, String username);
}
