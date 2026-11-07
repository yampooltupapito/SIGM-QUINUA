package pe.gob.municipalidad.sigm.modules.mercados.enums;

public enum EstadoObligacion {
    PENDIENTE("Pendiente"),
    PARCIALMENTE_PAGADA("Parcialmente Pagada"),
    PAGADA("Pagada"),
    VENCIDA("Vencida"),
    SUSPENDIDA("Suspendida"),
    ANULADA("Anulada");

    private final String descripcion;

    EstadoObligacion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public boolean isExigible() {
        return this == PENDIENTE || this == PARCIALMENTE_PAGADA || this == VENCIDA;
    }
}
