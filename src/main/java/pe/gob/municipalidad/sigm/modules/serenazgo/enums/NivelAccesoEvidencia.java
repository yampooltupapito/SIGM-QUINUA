package pe.gob.municipalidad.sigm.modules.serenazgo.enums;

public enum NivelAccesoEvidencia {
    PUBLICO("Público - Sin datos personales"),
    INTERNO("Uso Interno Operativo"),
    CONFIDENCIAL("Confidencial - Acceso Restringido a Jefatura"),
    RESERVADO("Reservado - Custodia Policial o Fiscal");

    private final String descripcion;

    NivelAccesoEvidencia(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
