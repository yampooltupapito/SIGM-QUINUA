package pe.gob.municipalidad.sigm.modules.serenazgo.enums;

public enum EstadoIntervencionSerenazgo {
    BORRADOR("Borrador", "Borrador de Intervención"),
    REGISTRADA("Registrada", "Intervención Registrada"),
    EN_REVISION("En Revisión", "En Revisión por Supervisor"),
    VALIDADA("Validada", "Validada Administrativamente"),
    OBSERVADA("Observada", "Observada por Inconsistencias"),
    ARCHIVADA("Archivada", "Archivada Administrativamente"),
    // Compatibilidad retrospectiva con código previo
    INICIADA("Iniciada", "Iniciada"),
    EN_PROCESO("En Proceso", "En Proceso"),
    FINALIZADA("Finalizada", "Finalizada con Acta/Parte"),
    CANCELADA("Cancelada", "Cancelada");

    private final String etiqueta;
    private final String descripcion;

    EstadoIntervencionSerenazgo(String etiqueta, String descripcion) {
        this.etiqueta = etiqueta;
        this.descripcion = descripcion;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public boolean isEditable() {
        return this == BORRADOR || this == OBSERVADA || this == INICIADA || this == EN_PROCESO;
    }

    public boolean isValidada() {
        return this == VALIDADA;
    }

    public boolean isObservada() {
        return this == OBSERVADA;
    }

    public boolean isArchivada() {
        return this == ARCHIVADA;
    }
}
