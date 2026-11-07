package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;

@Getter
public enum TipoCalculoArbitrio {
    SIMULACION("Simulación Referencial"),
    DETERMINACION_INDIVIDUAL("Determinación Individual por Predio"),
    EMISION_MASIVA_PRELIMINAR("Emisión Masiva Preliminar");

    private final String descripcion;

    TipoCalculoArbitrio(String descripcion) {
        this.descripcion = descripcion;
    }
}
