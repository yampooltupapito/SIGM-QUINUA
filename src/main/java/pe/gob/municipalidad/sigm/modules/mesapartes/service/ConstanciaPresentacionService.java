package pe.gob.municipalidad.sigm.modules.mesapartes.service;

import pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvSolicitudDtos.ConstanciaPresentacionResponse;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.Expediente;

public interface ConstanciaPresentacionService {

    byte[] generarConstanciaPdf(Expediente expediente, String baseUrl);

    ConstanciaPresentacionResponse obtenerDatosConstancia(Expediente expediente, String baseUrl);
}
