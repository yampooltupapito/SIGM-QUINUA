package pe.gob.municipalidad.sigm.modules.mercados.enums;

public enum EstadoSesionCaja {
    ABIERTA("Abierta"),
    CERRADA("Cerrada"),
    CONCILIADA("Conciliada"),
    ANULADA("Anulada");

    private final String descripcion;

    EstadoSesionCaja(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
