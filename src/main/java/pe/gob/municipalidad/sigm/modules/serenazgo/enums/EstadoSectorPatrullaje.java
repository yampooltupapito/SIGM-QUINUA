package pe.gob.municipalidad.sigm.modules.serenazgo.enums;

public enum EstadoSectorPatrullaje {
    BORRADOR("Borrador"),
    ACTIVO("Activo"),
    INACTIVO("Inactivo"),
    ARCHIVADO("Archivado");

    private final String descripcion;

    EstadoSectorPatrullaje(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
