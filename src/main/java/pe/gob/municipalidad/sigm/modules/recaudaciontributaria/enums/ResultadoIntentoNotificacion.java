package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;

/**
 * Resultados fácticos de un intento o diligencia de notificación presencial.
 */
@Getter
public enum ResultadoIntentoNotificacion {
    ENTREGA_EFECTIVA("Entrega Efectiva", "Documento recibido por contribuyente, representante o tercero autorizado"),
    DESTINATARIO_NO_UBICADO("Destinatario No Ubicado", "Predio cerrado o nadie respondió a los llamados en el domicilio"),
    DIRECCION_OBSERVADA("Dirección Observada / No Existe", "Dirección imprecisa, inexistente o numeración no concordante en sector"),
    RECEPCION_RECHAZADA("Recepción Rechazada", "Se negó a recibir el documento o a identificarse/firmar"),
    ACTUACION_INCONCLUSA("Actuación Inconclusa", "Fuerza mayor, inclemencia climática o peligro inminente en zona"),
    OTRA_INCIDENCIA("Otra Incidencia", "Incidencia documentada de manera individual en acta de visita");

    private final String etiqueta;
    private final String descripcion;

    ResultadoIntentoNotificacion(String etiqueta, String descripcion) {
        this.etiqueta = etiqueta;
        this.descripcion = descripcion;
    }
}

