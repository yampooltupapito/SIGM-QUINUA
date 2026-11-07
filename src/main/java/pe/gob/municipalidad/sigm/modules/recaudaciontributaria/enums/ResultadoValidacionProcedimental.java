package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;

/**
 * Decisión del funcionario competente sobre la validez del procedimiento de notificación.
 */
@Getter
public enum ResultadoValidacionProcedimental {
    CONFORME_VALIDA("Conforme / Válida", "Cumple todos los requisitos legales y surte plenos efectos jurídicos"),
    OBSERVADA_SUBSANABLE("Observada Subsanable", "Presenta omisiones formales que requieren subsanación por el notificador"),
    INVALIDA_NULA("Inválida / Nula", "Vicios esenciales que invalidan la diligencia y requieren nueva notificación");

    private final String etiqueta;
    private final String descripcion;

    ResultadoValidacionProcedimental(String etiqueta, String descripcion) {
        this.etiqueta = etiqueta;
        this.descripcion = descripcion;
    }
}

