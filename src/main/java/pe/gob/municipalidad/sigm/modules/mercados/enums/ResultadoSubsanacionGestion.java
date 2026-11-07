package pe.gob.municipalidad.sigm.modules.mercados.enums;

import lombok.Getter;

@Getter
public enum ResultadoSubsanacionGestion {
    PENDIENTE("Pendiente de Revisión"),
    CONFORME("Conforme"),
    RECHAZADA("Rechazada"),
    INSUFICIENTE("Insuficiente");

    private final String label;

    ResultadoSubsanacionGestion(String label) {
        this.label = label;
    }
}
