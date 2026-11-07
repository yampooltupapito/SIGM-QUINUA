package pe.gob.municipalidad.sigm.modules.mercados.enums;

import lombok.Getter;

@Getter
public enum EstadoPlano {
    ACTIVO("Activo"),
    INACTIVO("Inactivo");

    private final String label;

    EstadoPlano(String label) {
        this.label = label;
    }
}
