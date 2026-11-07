package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Tipos de datos soportados para parámetros tributarios.
 */
@Getter
@RequiredArgsConstructor
public enum TipoDatoParametro {
    DECIMAL("Decimal / Monetario / Porcentaje"),
    ENTERO("Número Entero"),
    TEXTO("Texto Alfanumérico"),
    BOOLEANO("Booleano (Sí/No)"),
    FECHA("Fecha (AAAA-MM-DD)");

    private final String descripcion;

    public String getNombre() {
        return descripcion;
    }
}
