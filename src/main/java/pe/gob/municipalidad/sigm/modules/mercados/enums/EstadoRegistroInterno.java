package pe.gob.municipalidad.sigm.modules.mercados.enums;

/**
 * Estado del registro y verificación interna de entidades o personas jurídicas.
 */
public enum EstadoRegistroInterno {
    PENDIENTE_VERIFICACION("Pendiente de Verificación"),
    VERIFICADO("Verificado"),
    OBSERVADO("Observado"),
    INACTIVO("Inactivo");

    private final String descripcion;

    EstadoRegistroInterno(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
