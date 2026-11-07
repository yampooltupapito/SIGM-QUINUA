package pe.gob.municipalidad.sigm.modules.serenazgo.enums;

public enum ResultadoIntervencionSerenazgo {
    ACTUACION_REGISTRADA("Actuación Registrada", "Actuación Preventiva Municipal Registrada y Documentada"),
    ORIENTACION_REALIZADA("Orientación Ciudadana", "Orientación, Guía y Auxilio al Vecino Realizado"),
    COMUNICACION_INSTITUCIONAL("Comunicación Institucional", "Comunicación y Derivación a Autoridad Competente"),
    SITUACION_DERIVADA("Situación Derivada", "Situación Derivada para Actuación Policial / Salud / Entidad Externa"),
    SEGUIMIENTO_RECOMENDADO("Seguimiento Recomendado", "Seguimiento y Monitoreo Preventivo Recomendado"),
    SIN_NOVEDAD("Sin Novedad", "Sin Novedad / No se Encontró Ocurrencia en Sitio"),
    RESUELTO_EN_LUGAR("Resuelto en Lugar", "Resuelto y Controlado Pacíficamente en el Lugar"),
    DERIVADO_PNP("Coordinado PNP", "Derivado / Coordinado con la Policía Nacional del Perú"),
    DERIVADO_SALUD("Derivado Salud", "Derivado a Centro de Salud / Puesto Médico"),
    DERIVADO_OTRO("Derivado Otra Autoridad", "Derivado a Otra Autoridad Competente"),
    EN_CURSO("En Desarrollo", "Intervención en Desarrollo");

    private final String etiqueta;
    private final String descripcion;

    ResultadoIntervencionSerenazgo(String etiqueta, String descripcion) {
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
