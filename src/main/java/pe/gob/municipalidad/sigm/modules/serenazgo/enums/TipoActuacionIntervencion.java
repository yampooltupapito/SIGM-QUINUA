package pe.gob.municipalidad.sigm.modules.serenazgo.enums;

public enum TipoActuacionIntervencion {
    OBSERVACION_PREVENTIVA("Observación y Vigilancia", "Vigilancia visual y monitoreo preventivo en la zona"),
    DIALOGO_VECINAL("Diálogo con Ciudadanos", "Entrevista, diálogo y recopilación pacífica de información"),
    DISUASION_VERBAL("Disuasión Verbal", "Exhortación pacífica y disuasión de conductas inapropiadas"),
    ORIENTACION_GUIA("Orientación y Guía", "Orientación a vecinos o visitantes sobre rutas o servicios"),
    RETIRO_VOLUNTARIO("Exhortación al Retiro", "Exhortación al retiro voluntario de personas o elementos del espacio público"),
    VERIFICACION_LUGAR("Inspección de Lugar", "Verificación visual de estado de inmueble, vía o área pública"),
    COORDINACION_PNP("Coordinación en Sitio con PNP", "Apoyo en perímetro o comunicación directa a efectivos de la PNP"),
    COORDINACION_OTRA("Coordinación con Otra Dependencia", "Coordinación presencial con fiscalizadores, defensa civil o salud"),
    OTRO("Otra Actuación Preventiva", "Actuación operativa preventiva de Serenazgo no listada anteriormente");

    private final String etiqueta;
    private final String descripcion;

    TipoActuacionIntervencion(String etiqueta, String descripcion) {
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
