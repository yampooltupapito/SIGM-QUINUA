package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Unidades de medida explícitas para parámetros tributarios.
 */
@Getter
@RequiredArgsConstructor
public enum UnidadMedidaParametro {
    PEN("Soles (PEN)", "Moneda de curso legal"),
    PORCENTAJE("Porcentaje (%)", "Valor expresado en tanto por ciento"),
    UIT("UIT", "Unidad Impositiva Tributaria"),
    M2("Metros Cuadrados (m²)", "Superficie de predio o edificación"),
    METROS_LINEALES("Metros Lineales (m)", "Longitud o frente del predio"),
    DIAS("Días", "Plazo en días calendario o hábiles"),
    UNIDAD("Unidad", "Cantidad unitaria o conteo"),
    FACTOR("Factor Numérico", "Multiplicador adimensional");

    private final String descripcion;
    private final String detalle;

    public String getNombre() {
        return descripcion;
    }
}
