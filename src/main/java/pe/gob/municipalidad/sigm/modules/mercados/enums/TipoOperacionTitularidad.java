package pe.gob.municipalidad.sigm.modules.mercados.enums;

import lombok.Getter;

@Getter
public enum TipoOperacionTitularidad {
    CAMBIO_TITULAR("Cambio de Titular"),
    TRANSFERENCIA_AUTORIZADA("Transferencia Autorizada"),
    RENOVACION_ASIGNACION("Renovación de Asignación");

    private final String label;

    TipoOperacionTitularidad(String label) {
        this.label = label;
    }
}
