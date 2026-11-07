package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service;

import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.deuda.ParametroInteresRequest;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.deuda.ParametroInteresResponse;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.deuda.SimulacionInteresRequest;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.deuda.SimulacionInteresResponse;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ObligacionTributaria;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface InteresMoratorioService {

    SimulacionInteresResponse simularInteresObligacion(UUID obligacionId, LocalDate fechaCorte);

    SimulacionInteresResponse simularInteresGenerico(SimulacionInteresRequest request);

    SimulacionInteresResponse calcularInteresParaObligacion(ObligacionTributaria obligacion, LocalDate fechaCorte);

    List<ParametroInteresResponse> listarParametros();

    ParametroInteresResponse obtenerParametro(UUID id);

    ParametroInteresResponse crearParametro(ParametroInteresRequest request, String username);

    ParametroInteresResponse actualizarParametro(UUID id, ParametroInteresRequest request, String username);
}
