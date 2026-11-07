package pe.gob.municipalidad.sigm.modules.mercados.enums;

public enum PeriodicidadConcepto {
    PAGO_UNICO("Pago Único"),
    MENSUAL("Mensual"),
    TRIMESTRAL("Trimestral"),
    SEMESTRAL("Semestral"),
    ANUAL("Anual"),
    PERSONALIZADA("Personalizada");

    private final String descripcion;

    PeriodicidadConcepto(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
