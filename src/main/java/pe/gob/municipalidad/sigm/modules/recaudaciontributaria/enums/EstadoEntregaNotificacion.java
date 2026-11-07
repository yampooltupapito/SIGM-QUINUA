package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;

/**
 * Estados del diligenciamiento material y entrega de la comunicación tributaria.
 */
@Getter
public enum EstadoEntregaNotificacion {
    NO_PROGRAMADA("No Programada", "Aún no cuenta con fecha de salida a campo"),
    PROGRAMADA("Programada", "Agendada en ruta con notificador asignado"),
    EN_PROCESO("En Proceso", "Notificador en campo ejecutando diligencias"),
    INTENTO_REGISTRADO("Intento Registrado", "Se ejecutó una visita que no culminó en entrega efectiva"),
    ENTREGADA_CON_EVIDENCIA("Entregada con Evidencia", "Entrega material realizada con cargo o evidencia física/digital"),
    NO_ENTREGADA("No Entregada", "No fue posible la entrega tras agotar diligencias reglamentarias"),
    DEVUELTA("Devuelta", "Devuelta a la oficina de origen por causas justificadas"),
    RESULTADO_PENDIENTE_REVISION("Pendiente Revisión", "Actuación registrada en espera de cotejo con expediente");

    private final String etiqueta;
    private final String descripcion;

    EstadoEntregaNotificacion(String etiqueta, String descripcion) {
        this.etiqueta = etiqueta;
        this.descripcion = descripcion;
    }
}

