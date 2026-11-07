package pe.gob.municipalidad.sigm.modules.mercados.enums;

/**
 * Estados físicos y condiciones estructurales de un puesto comercial.
 */
public enum EstadoFisicoPuesto {
    OPERATIVO("Operativo", "El puesto se encuentra en óptimas condiciones físicas para operar."),
    EN_MANTENIMIENTO("En Mantenimiento", "El puesto está bajo reparaciones, acondicionamiento o refacción."),
    INHABILITADO("Inhabilitado", "El puesto no cumple con condiciones estructurales o de Defensa Civil.");

    private final String descripcion;
    private final String detalle;

    EstadoFisicoPuesto(String descripcion, String detalle) {
        this.descripcion = descripcion;
        this.detalle = detalle;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getDetalle() {
        return detalle;
    }

    public boolean esAsignable() {
        return this == OPERATIVO;
    }
}
