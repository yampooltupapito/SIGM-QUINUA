package pe.gob.municipalidad.sigm.modules.mesapartes.service;

import pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvAdminDtos.RevisionRequisitoResponse;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvAdminDtos.VerificarRequisitoRequest;

import java.util.List;

/**
 * Servicio para la verificación y auditoría de requisitos documentales de expedientes.
 */
public interface RevisionRequisitosService {

    List<RevisionRequisitoResponse> obtenerOInicializarRequisitos(Long expedienteId, String username);

    RevisionRequisitoResponse verificarRequisito(Long expedienteId, Long requisitoId, VerificarRequisitoRequest request, String username);
}
