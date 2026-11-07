package pe.gob.municipalidad.sigm.modules.mercados.enums;

/**
 * Tipos de representación legal de personas jurídicas en el ámbito municipal.
 */
public enum TipoRepresentacion {
    GERENTE_GENERAL("Gerente General"),
    APODERADO("Apoderado"),
    ADMINISTRADOR("Administrador"),
    REPRESENTANTE_LEGAL("Representante Legal"),
    OTRO("Otro");

    private final String descripcion;

    TipoRepresentacion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
