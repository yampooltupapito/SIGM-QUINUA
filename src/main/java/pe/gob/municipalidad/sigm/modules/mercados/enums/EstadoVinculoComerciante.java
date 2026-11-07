package pe.gob.municipalidad.sigm.modules.mercados.enums;

import lombok.Getter;

/**
 * Estados del ciclo de vida del vínculo cuenta ciudadana - registro comercial.
 */
@Getter
public enum EstadoVinculoComerciante {
    PENDIENTE("Pendiente de Verificación"),
    VERIFICADO("Verificado y Activo"),
    RECHAZADO("Rechazado por Sustento Insuficiente"),
    REVOCADO("Revocado por Autoridad o Vencimiento"),
    VENCIDO("Vencido por Plazo de Poder");

    private final String label;

    EstadoVinculoComerciante(String label) {
        this.label = label;
    }
}
