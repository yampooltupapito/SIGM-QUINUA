package pe.gob.municipalidad.sigm.modules.serenazgo.enums;

import lombok.Getter;

@Getter
public enum TipoRegistroSupervision {
    INCIDENCIA("Incidencia de Seguridad", "Registro de incidencia reportada o constatada", "alert-triangle"),
    ALERTA("Alerta Ciudadana", "Alerta enviada por vecinos o canales comunitarios", "bell"),
    ATENCION("Atención / Despacho", "Despacho operativo y asignación de personal a incidencia", "radio"),
    INTERVENCION("Intervención de Serenazgo", "Actuación o intervención formal en campo", "shield-alert"),
    PATRULLAJE("Patrullaje / Recorrido", "Ronda o patrullaje programado o ejecutado", "car"),
    DOCUMENTO("Evidencia / Documento", "Fotografía, video, acta o documento de sustento", "file-text"),
    OTRO("Otro Registro Operativo", "Actividad o hecho complementario sujeto a supervisión", "info");

    private final String descripcion;
    private final String detalle;
    private final String icono;

    TipoRegistroSupervision(String descripcion, String detalle, String icono) {
        this.descripcion = descripcion;
        this.detalle = detalle;
        this.icono = icono;
    }
}
