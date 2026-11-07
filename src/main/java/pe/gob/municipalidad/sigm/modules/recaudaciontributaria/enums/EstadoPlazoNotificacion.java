package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;

/**
 * Estados del cómputo legal del plazo derivado de la notificación válida.
 */
@Getter
public enum EstadoPlazoNotificacion {
    VIGENTE("Vigente", "Plazo en curso legal regular"),
    PROXIMO_A_VENCER("Próximo a Vencer", "Quedan 3 días hábiles o menos para el vencimiento"),
    VENCIDO("Vencido", "Término legal fenecido sin cumplimiento o descargo"),
    CUMPLIDO("Cumplido", "Trámite, pago o recurso interpuesto oportunamente"),
    SUSPENDIDO("Suspendido", "Cómputo en suspenso por mandato legal o fuerza mayor");

    private final String etiqueta;
    private final String descripcion;

    EstadoPlazoNotificacion(String etiqueta, String descripcion) {
        this.etiqueta = etiqueta;
        this.descripcion = descripcion;
    }
}

