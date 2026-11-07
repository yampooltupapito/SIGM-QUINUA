package pe.gob.municipalidad.sigm.modules.mercados.enums;

public enum TipoMovimientoCuentaCorriente {
    CARGO("Cargo / Obligación"),
    ABONO("Abono / Pago"),
    REVERSO("Reverso de Pago");

    private final String descripcion;

    TipoMovimientoCuentaCorriente(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
