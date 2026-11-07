package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;

/**
 * Tipología de evidencias documentales o digitales adjuntadas a la notificación.
 */
@Getter
public enum TipoEvidenciaNotificacion {
    DOCUMENTO_CARGO("Cargo Físico Digitalizado", "Cédula o cargo con firma manuscrita, fecha, hora y datos del receptor"),
    FOTOGRAFIA_FACHADA("Fotografía de Fachada", "Fotografía con placa de numeración o frontis del predio visitado"),
    FOTOGRAFIA_ACTUACION("Fotografía de Actuación", "Constancia fotográfica de aviso de visita fijado en puerta cerrada"),
    ACUSE_ELECTRONICO("Acuse Electrónico / Log", "Comprobante técnico de depósito en casilla o log certificado de transmisión"),
    CONSTANCIA_VISITA("Constancia o Acta de Visita", "Acta formal levantada por el notificador con especificación de hechos"),
    DOCUMENTO_PROVEEDOR_POSTAL("Guía / Acuse de Operador Postal", "Comprobante de entrega devuelto por la empresa postal autorizada");

    private final String etiqueta;
    private final String descripcion;

    TipoEvidenciaNotificacion(String etiqueta, String descripcion) {
        this.etiqueta = etiqueta;
        this.descripcion = descripcion;
    }
}

