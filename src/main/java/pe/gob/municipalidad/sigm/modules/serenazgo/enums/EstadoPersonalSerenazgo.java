package pe.gob.municipalidad.sigm.modules.serenazgo.enums;

public enum EstadoPersonalSerenazgo {
    ACTIVO("Activo"),
    INACTIVO("Inactivo"),
    SUSPENDIDO("Suspendido"),
    RETIRADO("Retirado");

    private final String descripcion;

    EstadoPersonalSerenazgo(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
