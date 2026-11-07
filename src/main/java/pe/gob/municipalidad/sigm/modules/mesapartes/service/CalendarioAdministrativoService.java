package pe.gob.municipalidad.sigm.modules.mesapartes.service;

import pe.gob.municipalidad.sigm.modules.mesapartes.dto.ConfiguracionMpvDtos.*;

import java.time.LocalDate;
import java.util.List;

public interface CalendarioAdministrativoService {

    List<CalendarioAdminResponse> listarPorAnio(Integer anio);

    List<CalendarioAdminResponse> listarTodosActivos();

    CalendarioAdminResponse guardarFeriado(GuardarCalendarioRequest request, String username, String ipOrigen);

    void cambiarEstado(Long id, boolean activo, String username, String ipOrigen);

    boolean esDiaNoLaborable(LocalDate fecha);
}
