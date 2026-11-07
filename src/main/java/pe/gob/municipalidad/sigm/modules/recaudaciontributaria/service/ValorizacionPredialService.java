package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service;

import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.*;

import java.math.BigDecimal;
import java.util.List;

public interface ValorizacionPredialService {

    /**
     * Valoriza físicamente un predio (terreno, construcciones, depreciación y autovalúo)
     * para el ejercicio fiscal y según los parámetros normativos aplicables.
     */
    CalculoPredialDetalle valorizarPredio(
            PredioTributario predio,
            BigDecimal porcentajeParticipacion,
            Integer anioFiscal,
            BigDecimal uit,
            ConfiguracionConceptoTributario configuracion,
            List<String> advertencias,
            List<String> explicaciones
    );
}
