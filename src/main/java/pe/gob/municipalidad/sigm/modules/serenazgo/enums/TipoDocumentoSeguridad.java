package pe.gob.municipalidad.sigm.modules.serenazgo.enums;

import lombok.Getter;

@Getter
public enum TipoDocumentoSeguridad {
    FOTOGRAFIA("Fotografía", "Imagen digital capturada en campo o enviada por ciudadano", "image"),
    VIDEO("Video", "Grabación de video, cámara de vigilancia o dispositivo móvil", "video"),
    INFORME("Informe", "Informe administrativo o técnico de serenazgo", "file-text"),
    ACTA_ADMINISTRATIVA("Acta Administrativa", "Acta formal de constatación, intervención o entrega", "clipboard-check"),
    COMUNICACION_INSTITUCIONAL("Comunicación Institucional", "Oficio, memorando o comunicación interinstitucional", "mail"),
    DOCUMENTO_RESPALDO("Documento de Respaldo", "Comprobante, registro u otro respaldo documental", "paperclip"),
    OTRO("Otro Archivo Autorizado", "Archivo digital complementario autorizado", "file");

    private final String descripcion;
    private final String detalle;
    private final String icono;

    TipoDocumentoSeguridad(String descripcion, String detalle, String icono) {
        this.descripcion = descripcion;
        this.detalle = detalle;
        this.icono = icono;
    }
}
