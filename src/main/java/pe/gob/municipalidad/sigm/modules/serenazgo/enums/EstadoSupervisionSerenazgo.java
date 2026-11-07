package pe.gob.municipalidad.sigm.modules.serenazgo.enums;

public enum EstadoSupervisionSerenazgo {
    REGISTRADA("Registrada"),
    ATENDIDA("Atendida con Descargos"),
    ARCHIVADA("Archivada");

    private final String descripcion;

    EstadoSupervisionSerenazgo(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
