package pe.gob.municipalidad.sigm.modules.mercados.enums;

public enum EstadoPagoMercado {
    CONFIRMADO("Confirmado"),
    CONCILIADO("Conciliado"),
    REVERSADO("Reversado"),
    ANULADO("Anulado");

    private final String descripcion;

    EstadoPagoMercado(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public boolean isValido() {
        return this == CONFIRMADO || this == CONCILIADO;
    }
}
