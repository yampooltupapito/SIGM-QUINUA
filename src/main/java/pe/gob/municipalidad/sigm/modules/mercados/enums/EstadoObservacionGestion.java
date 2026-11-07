package pe.gob.municipalidad.sigm.modules.mercados.enums;

import lombok.Getter;

@Getter
public enum EstadoObservacionGestion {
    PENDIENTE("Pendiente"),
    SUBSANADA("Subsanada"),
    REITERADA("Reiterada"),
    DESESTIMADA("Desestimada");

    private final String label;

    EstadoObservacionGestion(String label) {
        this.label = label;
    }
}
