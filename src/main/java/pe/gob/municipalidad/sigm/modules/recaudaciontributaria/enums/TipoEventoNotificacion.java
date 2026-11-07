package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;

/**
 * Eventos auditables en la cronología de una notificación tributaria.
 */
@Getter
public enum TipoEventoNotificacion {
    CREACION("Creación Inicial"),
    VALIDACION("Validación de Datos"),
    AUTORIZACION("Autorización de Expedición"),
    GENERACION_PDF("Generación de Documento PDF"),
    ASIGNACION_NOTIFICADOR("Asignación a Notificador"),
    REASIGNACION("Reasignación de Notificador"),
    PROGRAMACION("Programación de Ruta"),
    REGISTRO_INTENTO("Registro de Intento de Entrega"),
    CARGO_REGISTRADO("Registro de Cargo de Recepción"),
    EVIDENCIA_ADJUNTADA("Incorporación de Evidencia"),
    REVISION_PROCEDIMENTAL("Revisión Procedimental"),
    VALIDACION_LEGAL("Declaración de Validez Legal"),
    OBSERVACION_LEGAL("Observación Procedimental"),
    INVALIDACION_LEGAL("Declaración de Invalidez / Nulidad"),
    PLAZO_COMPUTADO("Cómputo de Plazos Jurídicos"),
    ANULACION("Anulación de Notificación"),
    NOTIFICACION_EXTERNA_SIMULADA("Simulación Técnica de Envío");

    private final String etiqueta;

    TipoEventoNotificacion(String etiqueta) {
        this.etiqueta = etiqueta;
    }
}

