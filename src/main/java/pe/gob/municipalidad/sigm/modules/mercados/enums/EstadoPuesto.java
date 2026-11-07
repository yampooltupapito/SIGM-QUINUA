package pe.gob.municipalidad.sigm.modules.mercados.enums;

/**
 * Estados operativos y de asignación de un puesto comercial en el mercado municipal.
 */
public enum EstadoPuesto {
    DISPONIBLE("Disponible"),
    OCUPADO("Ocupado"),
    RESERVADO("Reservado"),
    EN_MANTENIMIENTO("En Mantenimiento"),
    INHABILITADO("Inhabilitado"),
    NO_ASIGNABLE("No Asignable");

    private final String descripcion;

    EstadoPuesto() {
        this.descripcion = name();
    }

    EstadoPuesto(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion != null ? descripcion : name();
    }

    public boolean esAsignable() {
        return this == DISPONIBLE;
    }
}
