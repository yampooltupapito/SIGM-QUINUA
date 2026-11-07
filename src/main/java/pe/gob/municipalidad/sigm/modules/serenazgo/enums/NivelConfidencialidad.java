package pe.gob.municipalidad.sigm.modules.serenazgo.enums;
public enum NivelConfidencialidad {
    ORDINARIO("Ordinario", "Información ordinaria de acceso administrativo general"),
    RESTRINGIDO("Restringido", "Información de circulación interna restringida"),
    PUBLICO("Público", "Información administrativa general no sensible"),
    RESERVADO("Reservado", "Uso exclusivo interno de la Subgerencia de Serenazgo"),
    CONFIDENCIAL("Confidencial", "Contiene datos personales o circunstancias de reserva legal");

    private final String etiqueta;
    private final String descripcion;

    NivelConfidencialidad(String etiqueta, String descripcion) {
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
