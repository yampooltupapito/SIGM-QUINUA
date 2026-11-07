package pe.gob.municipalidad.sigm.modules.mesapartes.service;

import pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvSolicitudDtos.EsquemaTramiteResponse;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.Expediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.FormularioVersion;

import java.util.Map;

public interface FormularioDinamicoService {

    EsquemaTramiteResponse obtenerEsquemaTramite(Long tipoTramiteId);

    FormularioVersion obtenerVersionActivaPorTramite(Long tipoTramiteId);

    void validarRespuestas(FormularioVersion version, Map<String, String> valores, boolean esPresentacionFinal);

    void guardarRespuestas(Expediente expediente, Map<String, String> valores, boolean esPresentacionFinal);

    Map<String, String> obtenerMapaRespuestas(Long expedienteId);
}
