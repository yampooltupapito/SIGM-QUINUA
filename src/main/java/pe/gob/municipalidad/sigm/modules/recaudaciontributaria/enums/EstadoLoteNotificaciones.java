package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;

/**
 * Estados del flujo de procesamiento de lotes masivos de notificaciones tributarias.
 */
@Getter
public enum EstadoLoteNotificaciones {
    BORRADOR("Borrador", "Lote creado con registros preliminares"),
    EN_VALIDACION("En Validación", "Validando contribuyentes, direcciones y saldos"),
    AUTORIZADO("Autorizado", "Lote validado y autorizado para emisión de documentos"),
    EN_GESTION("En Gestión", "Cédulas generadas y distribuidas operativamente"),
    CERRADO("Cerrado", "Lote concluido con todas sus notificaciones procesadas"),
    ANULADO("Anulado", "Lote descartado antes de emisión");

    private final String etiqueta;
    private final String descripcion;

    EstadoLoteNotificaciones(String etiqueta, String descripcion) {
        this.etiqueta = etiqueta;
        this.descripcion = descripcion;
    }
}

