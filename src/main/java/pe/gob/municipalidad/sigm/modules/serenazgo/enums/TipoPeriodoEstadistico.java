package pe.gob.municipalidad.sigm.modules.serenazgo.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Rangos de fechas predefinidos para filtros analíticos.
 */
@Getter
@RequiredArgsConstructor
public enum TipoPeriodoEstadistico {

    HOY("Hoy", "Día en curso"),
    ULTIMOS_7_DIAS("Últimos 7 días", "Semana móvil"),
    MES_ACTUAL("Mes actual", "Desde el 1° del mes a la fecha"),
    TRIMESTRE_ACTUAL("Trimestre actual", "Trimestre calendario en curso"),
    ANIO_ACTUAL("Año actual", "Año fiscal en curso"),
    PERSONALIZADO("Intervalo personalizado", "Rango de fechas especificado por el usuario");

    private final String etiqueta;
    private final String descripcion;
}
