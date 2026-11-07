package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Unidad temporal de expresión de la tasa de interés.
 */
@Getter
@RequiredArgsConstructor
public enum UnidadTemporalInteres {
    DIARIA("Diaria"),
    MENSUAL("Mensual"),
    ANUAL("Anual");

    private final String descripcion;
}
