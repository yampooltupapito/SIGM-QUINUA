package pe.gob.municipalidad.sigm.modules.serenazgo.enums;

public enum EstadoProgramacionServicio {
    BORRADOR("Borrador"),
    PROGRAMADA("Programada"),
    PROGRAMADO("Programada"), // Compatibilidad con migraciones y código previo
    CONFIRMADA("Confirmada"),
    EN_CURSO("En Curso"),
    PRESENTE("Presente en Guardia"),
    AUSENTE("Ausente sin Justificación"),
    JUSTIFICADO("Ausencia Justificada / Permiso"),
    FINALIZADA("Finalizada"),
    CANCELADA("Cancelada"),
    CANCELADO("Cancelada"); // Compatibilidad con migraciones y código previo

    private final String descripcion;

    EstadoProgramacionServicio(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public boolean esActivo() {
        return this == BORRADOR || this == PROGRAMADA || this == PROGRAMADO || this == CONFIRMADA || this == EN_CURSO || this == PRESENTE;
    }

    public boolean esConfirmadoOEnCurso() {
        return this == CONFIRMADA || this == EN_CURSO || this == PRESENTE;
    }
}
