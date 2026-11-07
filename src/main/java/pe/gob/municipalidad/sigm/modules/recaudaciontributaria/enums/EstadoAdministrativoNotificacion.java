package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;

/**
 * Estados del ciclo de vida administrativo de una notificación tributaria.
 */
@Getter
public enum EstadoAdministrativoNotificacion {
    BORRADOR("Borrador", "En preparación preliminar y carga de datos"),
    PENDIENTE_VALIDACION("Pendiente de Validación", "Enviado a revisión de datos y consistencia"),
    OBSERVADA("Observada", "Con reparos que impiden su autorización formal"),
    VALIDADA("Validada", "Datos y sustento conformes para autorización"),
    AUTORIZADA("Autorizada", "Aprobada por funcionario competente para su expedición"),
    EN_GESTION("En Gestión", "Con cédula emitida y asignada para diligenciamiento"),
    CERRADA("Cerrada", "Procedimiento de notificación concluido formalmente"),
    ANULADA("Anulada", "Cancelada formalmente por resolución o informe motivado");

    private final String etiqueta;
    private final String descripcion;

    EstadoAdministrativoNotificacion(String etiqueta, String descripcion) {
        this.etiqueta = etiqueta;
        this.descripcion = descripcion;
    }
}

