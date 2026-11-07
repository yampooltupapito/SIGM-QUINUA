package pe.gob.municipalidad.sigm.modules.serenazgo.enums;

public enum EstadoMantenimientoRecurso {
    PROGRAMADO("Mantenimiento Programado"),
    EN_PROCESO("En Proceso de Taller"),
    FINALIZADO("Finalizado y Conformidad"),
    CANCELADO("Cancelado");

    private final String descripcion;

    EstadoMantenimientoRecurso(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
