package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service;

import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ConfiguracionArbitrioMunicipal;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.DetalleCalculoArbitrio;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.PredioTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoBeneficioArbitrio;

import java.math.BigDecimal;
import java.util.List;

public interface DistribucionArbitrioService {

    DetalleCalculoArbitrio calcularServicioArbitrio(
            PredioTributario predio,
            ConfiguracionArbitrioMunicipal configuracion,
            TipoBeneficioArbitrio beneficio,
            BigDecimal porcentajeParticipacion,
            List<String> advertencias,
            List<String> explicaciones
    );

    BigDecimal estimarFrenteLinealMetros(PredioTributario predio);
}
