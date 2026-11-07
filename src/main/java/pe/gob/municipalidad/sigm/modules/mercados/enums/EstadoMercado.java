package pe.gob.municipalidad.sigm.modules.mercados.enums;

/**
 * Estados operativos de un mercado municipal.
 */
public enum EstadoMercado {
    ACTIVO("Activo"),
    INACTIVO("Inactivo"),
    EN_MANTENIMIENTO("En Mantenimiento"),
    SUSPENDIDO("Suspendido");

    private final String descripcion;

    EstadoMercado(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public boolean permiteAsignaciones() {
        return this == ACTIVO;
    }
}
