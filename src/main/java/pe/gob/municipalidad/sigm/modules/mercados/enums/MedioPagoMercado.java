package pe.gob.municipalidad.sigm.modules.mercados.enums;

public enum MedioPagoMercado {
    EFECTIVO("Efectivo"),
    TRANSFERENCIA_BANCARIA("Transferencia Bancaria"),
    TRANSFERENCIA("Transferencia Bancaria"),
    DEPOSITO_BANCARIO("Depósito en Cuenta"),
    DEPOSITO("Depósito en Cuenta"),
    TARJETA_DEBITO_CREDITO("Tarjeta Débito/Crédito"),
    TARJETA("Tarjeta Débito/Crédito"),
    BILLETERA_DIGITAL("Billetera Digital"),
    YAPE("Yape"),
    PLIN("Plin"),
    OTRO("Otro Medio Autorizado");

    private final String descripcion;

    MedioPagoMercado(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
