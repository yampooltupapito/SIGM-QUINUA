package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;

@Getter
public enum TipoDatoCriterio {
    NUMERICO("Numérico Escalar"),
    PORCENTAJE("Porcentaje"),
    CATEGORICO("Categórico / Clasificación"),
    COEFICIENTE("Coeficiente Factor");

    private final String descripcion;

    TipoDatoCriterio(String descripcion) {
        this.descripcion = descripcion;
    }
}
