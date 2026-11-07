package pe.gob.municipalidad.sigm.modules.mesapartes.service;

import pe.gob.municipalidad.sigm.modules.mesapartes.dto.AtencionDtos.*;

public interface RevisionDecisionService {

    PropuestaResponse revisarPropuesta(Long propuestaId, RevisarPropuestaRequest request, String username);
}
