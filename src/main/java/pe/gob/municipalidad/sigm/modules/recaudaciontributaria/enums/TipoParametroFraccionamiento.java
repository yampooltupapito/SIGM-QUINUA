package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Tipos de parámetros normativos aplicables a programas de fraccionamiento tributario.
 */
@Getter
@RequiredArgsConstructor
public enum TipoParametroFraccionamiento {
    MONTO_MINIMO("Monto Mínimo Elegible", "PEN"),
    CUOTAS_MAX("Número Máximo de Cuotas", "CUOTAS"),
    CUOTAS_MIN("Número Mínimo de Cuotas", "CUOTAS"),
    CUOTA_INICIAL_MIN_PCT("Porcentaje Mínimo de Cuota Inicial", "%"),
    TASA_INTERES_MENSUAL("Tasa de Interés Mensual de Fraccionamiento", "%"),
    PERIODICIDAD_DIAS("Periodicidad entre Cuotas", "DIAS"),
    TOLERANCIA_DIAS("Días Hábiles de Tolerancia para Vencimiento", "DIAS"),
    CUOTAS_INCUMPLIMIENTO_PERDIDA("Cuotas Impagas como Causal de Pérdida", "CUOTAS");

    private final String descripcion;
    private final String unidadPorDefecto;
}
