package pe.gob.municipalidad.sigm.modules.serenazgo.enums;

public enum EstadoAlertaCiudadana {
    RECIBIDA("Recibida por la Central"),
    PENDIENTE("Pendiente de Evaluación"),
    EN_REVISION("En Revisión por Operador"),
    EN_PROCESO("En Proceso de Despacho"),
    REQUIERE_INFORMACION("Requiere Información Complementaria"),
    CLASIFICADA("Clasificada y Priorizada"),
    VINCULADA("Vinculada a Incidencia"),
    CONVERTIDA_EN_INCIDENCIA("Convertida en Incidencia Formal"),
    CERRADA("Cerrada Administrativamente"),
    DESCARTADA("Descartada");

    private final String descripcion;

    EstadoAlertaCiudadana(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
