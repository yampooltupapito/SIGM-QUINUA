package pe.gob.municipalidad.sigm.modules.serenazgo.enums;

public enum EstadoReservaRecurso {
    RESERVADA("Reservada"),
    ASIGNADA("Asignada"),
    LIBERADA("Liberada"),
    CANCELADA("Cancelada");

    private final String descripcion;

    EstadoReservaRecurso(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
