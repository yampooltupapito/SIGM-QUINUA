package pe.gob.municipalidad.sigm.modules.serenazgo.enums;

public enum TipoIntervencionSerenazgo {
    PATRULLAJE_PREVENTIVO("Patrullaje Preventivo", "Actuación disuasiva durante ronda o patrullaje ordinario"),
    ORIENTACION_CIUDADANA("Orientación y Auxilio", "Orientación, auxilio y soporte al ciudadano o turista"),
    APOYO_MUNICIPAL("Apoyo Municipal", "Acompañamiento a fiscalización, transporte o control municipal"),
    VERIFICACION_REPORTADA("Verificación de Reporte", "Inspección ocular y verificación de situación reportada"),
    COMUNICACION_AUTORIDAD("Coordinación Autoridad", "Actuación conjunta o coordinación con PNP u otra entidad"),
    DISUASION_ESPACIO_PUBLICO("Disuasión y Recuperación", "Exhortación y recuperación de espacios públicos"),
    OTRA_ACTUACION("Otra Actuación", "Otra actuación preventiva en el marco de competencias municipales");

    private final String etiqueta;
    private final String descripcion;

    TipoIntervencionSerenazgo(String etiqueta, String descripcion) {
        this.etiqueta = etiqueta;
        this.descripcion = descripcion;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
