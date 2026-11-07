package pe.gob.municipalidad.sigm.modules.serenazgo.enums;

public enum EstadoDocumentoPersonalSerenazgo {
    VIGENTE("Documento Vigente"),
    VENCIDO("Documento Vencido"),
    ANULADO("Documento Anulado");

    private final String descripcion;

    EstadoDocumentoPersonalSerenazgo(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
