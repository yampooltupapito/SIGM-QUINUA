package pe.gob.municipalidad.sigm.modules.mesapartes.service;

import pe.gob.municipalidad.sigm.modules.mesapartes.dto.ConfiguracionMpvDtos.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvSolicitudDtos.EsquemaTramiteResponse;

import java.util.List;

public interface AdministracionFormulariosService {

    List<FormularioAdminResponse> listarFormularios();

    FormularioAdminResponse obtenerPorId(Long id);

    FormularioAdminResponse obtenerPorTramiteId(Long tipoTramiteId);

    FormularioAdminResponse guardarFormulario(GuardarFormularioRequest request, String username, String ipOrigen);

    FormularioAdminResponse publicarVersion(Long formularioId, String username, String ipOrigen);

    EsquemaTramiteResponse generarVistaPrevia(Long formularioId);
}
