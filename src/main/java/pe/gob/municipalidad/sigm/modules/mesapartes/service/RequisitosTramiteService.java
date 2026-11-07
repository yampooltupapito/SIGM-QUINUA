package pe.gob.municipalidad.sigm.modules.mesapartes.service;

import pe.gob.municipalidad.sigm.modules.mesapartes.dto.ConfiguracionMpvDtos.*;

import java.util.List;

public interface RequisitosTramiteService {

    List<RequisitoAdminResponse> listarPorTramite(Long tipoTramiteId);

    RequisitoAdminResponse obtenerPorId(Long id);

    RequisitoAdminResponse guardarRequisito(GuardarRequisitoRequest request, String username, String ipOrigen);

    RequisitoAdminResponse actualizarRequisito(Long id, GuardarRequisitoRequest request, String username, String ipOrigen);

    void cambiarEstadoRequisito(Long id, String nuevoEstado, String motivo, String username, String ipOrigen);
}
