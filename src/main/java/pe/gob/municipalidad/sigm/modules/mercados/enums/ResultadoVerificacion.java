package pe.gob.municipalidad.sigm.modules.mercados.enums;

public enum ResultadoVerificacion {
    CONFORME("Conforme"),
    NO_CONFORME("No Conforme"),
    NO_APLICA("No Aplica"),
    NO_EVALUADO("No Evaluado");

    private final String descripcion;

    ResultadoVerificacion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
