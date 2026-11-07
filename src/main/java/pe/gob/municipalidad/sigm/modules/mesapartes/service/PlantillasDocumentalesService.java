package pe.gob.municipalidad.sigm.modules.mesapartes.service;

import pe.gob.municipalidad.sigm.modules.mesapartes.dto.ConfiguracionMpvDtos.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.TipoDocumentalPlantilla;

import java.util.List;
import java.util.Map;

public interface PlantillasDocumentalesService {

    List<PlantillaAdminResponse> listarPlantillas();

    PlantillaAdminResponse obtenerPorId(Long id);

    PlantillaAdminResponse obtenerPorTipo(TipoDocumentalPlantilla tipoDocumental);

    PlantillaAdminResponse guardarPlantilla(GuardarPlantillaRequest request, String username, String ipOrigen);

    PlantillaAdminResponse actualizarPlantilla(Long id, GuardarPlantillaRequest request, String username, String ipOrigen);

    String previsualizarContenido(Long id, Map<String, String> variables);
}
