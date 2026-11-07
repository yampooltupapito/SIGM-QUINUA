package pe.gob.municipalidad.sigm.modules.serenazgo.enums;

public enum EstadoUnidadMovil {
    DISPONIBLE("Disponible para Asignación"),
    OPERATIVA("Operativa para Servicio"),
    ASIGNADA("Asignada en Servicio"),
    EN_MANTENIMIENTO("En Mantenimiento Técnico"),
    FUERA_DE_SERVICIO("Fuera de Servicio Temporal"),
    INACTIVA("Inactiva o En Reserva"),
    RETIRADA("Retirada o Dada de Baja");

    private final String descripcion;

    EstadoUnidadMovil(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public boolean isDisponiblePatrullaje() {
        return this == DISPONIBLE || this == OPERATIVA;
    }
}
