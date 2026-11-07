package pe.gob.municipalidad.sigm.modules.serenazgo.enums;

public enum TipoDocumentoIntervencion {
    ACTA_ADMINISTRATIVA("Acta Administrativa", "Acta administrativa de ocurrencia de Serenazgo"),
    INFORME_PREVENTIVO("Informe Preventivo", "Informe de actuación preventiva y disuasiva"),
    PARTE_DIARIO("Parte Diario", "Parte diario o reporte del líder de cuadrilla"),
    OFICIO_COORDINACION("Oficio de Coordinación", "Documento u oficio cursado a autoridad competente"),
    FOTOGRAFIA("Registro Fotográfico", "Fotografía administrativa autorizada con protección de privacidad"),
    OTRO_DOCUMENTO("Otro Documento", "Otro documento o anexo administrativo de sustento");

    private final String etiqueta;
    private final String descripcion;

    TipoDocumentoIntervencion(String etiqueta, String descripcion) {
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
