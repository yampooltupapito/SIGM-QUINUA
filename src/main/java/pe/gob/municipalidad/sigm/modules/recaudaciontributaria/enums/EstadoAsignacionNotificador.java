package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;

/**
 * Estados de la asignación de una notificación a un notificador municipal.
 */
@Getter
public enum EstadoAsignacionNotificador {
    ASIGNADA("Asignada", "Asignada a notificador municipal"),
    EN_RUTA("En Ruta", "En poder del notificador en recorrido de campo"),
    DEVUELTA("Devuelta", "Devuelta con informe de impedimento o motivo"),
    REASIGNADA("Reasignada", "Reasignada a otro notificador por cambio de sector o licencia"),
    COMPLETADA("Completada", "Diligencia concluida y cargo/evidencia entregado en mesa"),
    CANCELADA("Cancelada", "Asignación cancelada por anulación de la notificación");

    private final String etiqueta;
    private final String descripcion;

    EstadoAsignacionNotificador(String etiqueta, String descripcion) {
        this.etiqueta = etiqueta;
        this.descripcion = descripcion;
    }
}

