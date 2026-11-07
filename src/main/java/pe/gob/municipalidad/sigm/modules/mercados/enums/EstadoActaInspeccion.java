package pe.gob.municipalidad.sigm.modules.mercados.enums;

public enum EstadoActaInspeccion {
    BORRADOR("Borrador"),
    EN_REVISION("En Revisión Legal / Técnica"),
    APROBADA("Aprobada"),
    EMITIDA("Emitida y Notificada"),
    ANULADA("Anulada");

    private final String descripcion;

    EstadoActaInspeccion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
