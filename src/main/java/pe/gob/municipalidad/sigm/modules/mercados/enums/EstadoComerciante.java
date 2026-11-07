package pe.gob.municipalidad.sigm.modules.mercados.enums;

/**
 * Estados del comerciante en el padrón municipal de Quinua.
 */
public enum EstadoComerciante {
    PENDIENTE_VERIFICACION("Pendiente de Verificación"),
    ACTIVO("Activo"),
    SUSPENDIDO("Suspendido"),
    INACTIVO("Inactivo");

    private final String descripcion;

    EstadoComerciante(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public boolean puedeOcuparPuesto() {
        return this == ACTIVO;
    }
}
