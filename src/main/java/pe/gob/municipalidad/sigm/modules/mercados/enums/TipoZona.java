package pe.gob.municipalidad.sigm.modules.mercados.enums;

public enum TipoZona {
    COMERCIAL("Comercial", true),
    CIRCULACION("Circulación", false),
    SERVICIOS_COMUNES("Servicios Comunes", false),
    ALMACENAMIENTO("Almacenamiento", true),
    MANTENIMIENTO("Mantenimiento", false),
    OTRAS("Otras", false);

    private final String descripcion;
    private final boolean asignableDefecto;

    TipoZona(String descripcion, boolean asignableDefecto) {
        this.descripcion = descripcion;
        this.asignableDefecto = asignableDefecto;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public boolean isAsignableDefecto() {
        return asignableDefecto;
    }
}
