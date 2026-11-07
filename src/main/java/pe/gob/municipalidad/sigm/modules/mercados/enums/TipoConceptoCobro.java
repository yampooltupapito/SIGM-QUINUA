package pe.gob.municipalidad.sigm.modules.mercados.enums;

public enum TipoConceptoCobro {
    USO_PUESTO_COMERCIAL("Uso de Puesto Comercial"),
    SERVICIO_MERCADO("Servicio de Mercado"),
    DERECHO_ADMINISTRATIVO("Derecho Administrativo"),
    OBLIGACION_CONTRACTUAL("Obligación Contractual"),
    OTRO("Otro Concepto Habilitado");

    private final String descripcion;

    TipoConceptoCobro(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
