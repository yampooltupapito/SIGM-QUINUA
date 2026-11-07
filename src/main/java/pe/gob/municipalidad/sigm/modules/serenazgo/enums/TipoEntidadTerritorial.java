package pe.gob.municipalidad.sigm.modules.serenazgo.enums;

public enum TipoEntidadTerritorial {
    SECTOR("Sector de Patrullaje"),
    ZONA("Zona de Patrullaje");

    private final String descripcion;

    TipoEntidadTerritorial(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
