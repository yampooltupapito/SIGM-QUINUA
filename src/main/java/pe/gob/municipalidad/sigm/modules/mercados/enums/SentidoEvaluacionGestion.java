package pe.gob.municipalidad.sigm.modules.mercados.enums;

import lombok.Getter;

@Getter
public enum SentidoEvaluacionGestion {
    FAVORABLE("Favorable"),
    DESFAVORABLE("Desfavorable"),
    OBSERVADA("Observada"),
    REQUIERE_INFORMACION("Requiere Información Adicional");

    private final String label;

    SentidoEvaluacionGestion(String label) {
        this.label = label;
    }
}
