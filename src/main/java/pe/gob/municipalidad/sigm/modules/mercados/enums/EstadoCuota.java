package pe.gob.municipalidad.sigm.modules.mercados.enums;

public enum EstadoCuota {
    PENDIENTE("Pendiente"),
    PARCIALMENTE_PAGADA("Parcialmente Pagada"),
    PAGADA("Pagada"),
    VENCIDA("Vencida");

    private final String descripcion;

    EstadoCuota(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
