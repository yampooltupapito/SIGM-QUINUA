package pe.gob.municipalidad.sigm.modules.mesapartes.service;

import pe.gob.municipalidad.sigm.modules.mesapartes.dto.SeguimientoDtos.ConsultaPublicaRequest;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.SeguimientoDtos.ConsultaPublicaResponse;

public interface ConsultaPublicaExpedienteService {

    ConsultaPublicaResponse consultarPublicamente(ConsultaPublicaRequest request, String ipOrigen, String userAgent);
}
