package pe.gob.municipalidad.sigm.modules.mercados.enums;

import lombok.Getter;

/**
 * Tipo de vínculo legal o contractual entre la cuenta ciudadana y el comerciante.
 */
@Getter
public enum TipoVinculoComerciante {
    TITULAR("Titular Directo"),
    REPRESENTANTE_LEGAL("Representante Legal"),
    APODERADO("Apoderado con Poder Verificado");

    private final String label;

    TipoVinculoComerciante(String label) {
        this.label = label;
    }
}
