package pe.gob.municipalidad.sigm.modules.serenazgo.enums;

public enum NivelGravedadIncidencia {
    LEVE("Gravedad Leve"),
    MEDIA("Gravedad Media"),
    GRAVE("Gravedad Grave"),
    CRITICA("Gravedad Crítica - Inoperatividad Total");

    private final String descripcion;

    NivelGravedadIncidencia(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
