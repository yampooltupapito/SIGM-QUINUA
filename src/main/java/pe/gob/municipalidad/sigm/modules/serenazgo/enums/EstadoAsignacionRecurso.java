package pe.gob.municipalidad.sigm.modules.serenazgo.enums;

public enum EstadoAsignacionRecurso {
    ACTIVA("Asignación Activa"),
    DEVUELTA("Devuelta y Conforme"),
    DEVUELTA_TOTAL("Devuelta Total"),
    DEVUELTA_PARCIAL("Devuelta Parcial"),
    CANCELADA("Cancelada");

    private final String descripcion;

    EstadoAsignacionRecurso(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
