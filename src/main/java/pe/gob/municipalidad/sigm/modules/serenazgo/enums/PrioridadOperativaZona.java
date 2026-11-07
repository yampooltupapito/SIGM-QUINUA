package pe.gob.municipalidad.sigm.modules.serenazgo.enums;

public enum PrioridadOperativaZona {
    BAJA("Baja"),
    MEDIA("Media"),
    ALTA("Alta"),
    CRITICA("Crítica");

    private final String descripcion;

    PrioridadOperativaZona(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
