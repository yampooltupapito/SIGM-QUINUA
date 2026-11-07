package pe.gob.municipalidad.sigm.modules.serenazgo.enums;

public enum EstadoAsignacionPersonal {
    ASIGNADO("Asignado"),
    CONFIRMADO("Confirmado"),
    SUSTITUIDO("Sustituido"),
    CANCELADO("Cancelado");

    private final String descripcion;

    EstadoAsignacionPersonal(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
