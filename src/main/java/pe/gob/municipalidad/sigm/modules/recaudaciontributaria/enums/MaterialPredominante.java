package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Material estructural predominante en edificaciones de Quinua.
 */
@Getter
@RequiredArgsConstructor
public enum MaterialPredominante {
    CONCRETO("Concreto Armado"),
    LADRILLO("Ladrillo / Bloque de Cemento"),
    LADRILLO_CONCRETO("Ladrillo / Concreto Armado"),
    ADOBE_TAPIA("Adobe o Tapial Tradicional"),
    MADERA("Madera / Troncos"),
    QUINCHA("Quincha / Caña con Barro"),
    DRYWALL("Drywall / Fibrocemento"),
    ESTRUCTURA_METALICA("Estructura Metálica / Tinglado"),
    OTRO("Otro Material");

    private final String descripcion;

    public String getNombre() {
        return descripcion;
    }
}
