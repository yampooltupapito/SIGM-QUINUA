package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;

/**
 * Categorías fundamentales de comunicaciones y notificaciones tributarias municipales.
 */
@Getter
public enum CategoriaTipoNotificacion {
    INFORMATIVA("Informativa", "Comunicación general de cortesía sin apercibimiento ni efectos procesales"),
    RECORDATORIO("Recordatorio", "Recordatorio de vencimiento de obligaciones tributarias en etapa ordinaria"),
    AVISO_ADMINISTRATIVO("Aviso Administrativo", "Comunicación administrativa previa que previene el inicio de actos formales"),
    NOTIFICACION_FORMAL("Notificación Formal", "Acto administrativo formal sujeto a requisitos estrictos de validez legal según el Código Tributario");

    private final String nombre;
    private final String descripcion;

    CategoriaTipoNotificacion(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }
}

