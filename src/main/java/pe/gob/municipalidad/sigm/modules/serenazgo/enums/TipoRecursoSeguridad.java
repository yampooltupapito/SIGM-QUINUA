package pe.gob.municipalidad.sigm.modules.serenazgo.enums;

public enum TipoRecursoSeguridad {
    UNIDAD_MOVIL("Unidad Móvil Vehicular"),
    EQUIPAMIENTO("Bien o Equipamiento Operativo");

    private final String descripcion;

    TipoRecursoSeguridad(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
