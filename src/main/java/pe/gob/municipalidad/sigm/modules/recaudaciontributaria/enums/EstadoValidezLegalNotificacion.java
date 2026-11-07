package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;

/**
 * Calificación jurídica formal de la validez procedimental de la notificación.
 * Una entrega física o acuse técnico NO implica por sí mismo validez jurídica.
 */
@Getter
public enum EstadoValidezLegalNotificacion {
    NO_APLICA("No Aplica", "Comunicación puramente informativa o recordatorio que no surte efectos procesales"),
    PENDIENTE_VERIFICACION("Pendiente Verificación", "Esperando calificación legal por abogado o funcionario competente"),
    EN_REVISION("En Revisión", "Bajo evaluación procedimental frente a los requisitos del Código Tributario"),
    VALIDADA_POR_RESPONSABLE("Validada por Responsable", "Calificada positivamente con checklist procedimental y firma de responsable"),
    OBSERVADA("Observada", "Presenta vicios u omisiones subsanables en el cargo o diligencia"),
    INVALIDADA("Invalidada / Nula", "Vicios insubsanables que acarrean ineficacia o nulidad del acto de notificación");

    private final String etiqueta;
    private final String descripcion;

    EstadoValidezLegalNotificacion(String etiqueta, String descripcion) {
        this.etiqueta = etiqueta;
        this.descripcion = descripcion;
    }
}

