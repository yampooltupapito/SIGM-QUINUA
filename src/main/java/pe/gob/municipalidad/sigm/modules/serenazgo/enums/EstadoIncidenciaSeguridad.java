package pe.gob.municipalidad.sigm.modules.serenazgo.enums;

public enum EstadoIncidenciaSeguridad {
    REGISTRADA("Registrada en Central"),
    EN_EVALUACION("En Evaluación y Verificación"),
    ASIGNADA("Asignada a Unidad/Efectivo"),
    EN_ATENCION("En Atención en Lugar"),
    RESUELTA("Resuelta y Controlada"),
    CERRADA("Cerrada con Informe/Parte"),
    DESCARTADA("Descartada / Falsa Alarma");

    private final String descripcion;

    EstadoIncidenciaSeguridad(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public boolean isAbierta() {
        return this == REGISTRADA || this == EN_EVALUACION || this == ASIGNADA || this == EN_ATENCION;
    }
}
