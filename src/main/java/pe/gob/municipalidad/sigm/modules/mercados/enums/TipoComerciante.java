package pe.gob.municipalidad.sigm.modules.mercados.enums;

/**
 * Modalidad de comerciante en el padrón municipal: Persona Natural o Jurídica.
 */
public enum TipoComerciante {
    PERSONA_NATURAL("Persona Natural"),
    PERSONA_JURIDICA("Persona Jurídica");

    private final String descripcion;

    TipoComerciante(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
