package pe.gob.municipalidad.sigm.modules.mesapartes.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.DerivacionDtos.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvDtos.ExpedienteResponse;

import java.util.List;

public interface AsignacionExpedienteService {

    AsignacionResponse asignarResponsable(Long expedienteId, AsignarExpedienteRequest request, String username);

    AsignacionResponse reasignarResponsable(Long expedienteId, ReasignarExpedienteRequest request, String username);

    Page<ExpedienteResponse> listarMisExpedientes(Pageable pageable, String username);

    List<UsuarioSimpleDto> listarTrabajadoresHabilitados(Long oficinaId, String username);
}
