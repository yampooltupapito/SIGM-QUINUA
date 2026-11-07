package pe.gob.municipalidad.sigm.modules.serenazgo.enums;

public enum EstadoIncidenciaTecnica {
    REGISTRADA("Reportada / Registrada"),
    EN_EVALUACION("En Evaluación Técnica"),
    EN_REPARACION("En Reparación"),
    RESUELTA("Resuelta y Operativa"),
    DESESTIMADA("Desestimada / Sin Falla");

    private final String descripcion;

    EstadoIncidenciaTecnica(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
