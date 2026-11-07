package pe.gob.municipalidad.sigm.modules.mesapartes.service;

import pe.gob.municipalidad.sigm.modules.mesapartes.dto.ConfiguracionMpvDtos.*;

import java.util.List;

public interface ConfiguracionNumeracionService {

    List<NumeracionResponse> listarCorrelativos();

    NumeracionResponse obtenerCorrelativoActual(int anio);
}
