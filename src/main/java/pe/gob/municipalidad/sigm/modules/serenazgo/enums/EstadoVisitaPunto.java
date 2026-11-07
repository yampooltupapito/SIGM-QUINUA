package pe.gob.municipalidad.sigm.modules.serenazgo.enums;

public enum EstadoVisitaPunto {
    VISITADO("Punto Visitado Conforme"),
    OMITIDO("Punto Omitido con Justificación");

    private final String descripcion;

    EstadoVisitaPunto(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
