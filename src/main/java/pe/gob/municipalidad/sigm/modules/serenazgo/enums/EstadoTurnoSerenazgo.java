package pe.gob.municipalidad.sigm.modules.serenazgo.enums;

public enum EstadoTurnoSerenazgo {
    ACTIVO("Activo"),
    INACTIVO("Inactivo"),
    ARCHIVADO("Archivado");

    private final String descripcion;

    EstadoTurnoSerenazgo(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
