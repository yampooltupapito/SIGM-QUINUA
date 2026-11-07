package pe.gob.municipalidad.sigm.modules.mercados.enums;

public enum EstadoInspeccion {
    BORRADOR("Borrador"),
    PROGRAMADA("Programada"),
    ASIGNADA("Asignada a Inspectores"),
    EN_PROCESO("En Ejecución In Situ"),
    EN_EJECUCION("En Ejecución In Situ"),
    PENDIENTE_REVISION("Pendiente de Revisión y Acta"),
    OBSERVADA("Observada por Jefatura"),
    FINALIZADA("Finalizada con Acta Emitida"),
    CANCELADA("Cancelada");

    private final String descripcion;

    EstadoInspeccion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
