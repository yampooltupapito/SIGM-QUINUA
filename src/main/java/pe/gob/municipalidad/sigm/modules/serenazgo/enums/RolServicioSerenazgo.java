package pe.gob.municipalidad.sigm.modules.serenazgo.enums;

public enum RolServicioSerenazgo {
    PATRULLAJE("Patrullaje General"),
    PATRULLAJE_A_PIE("Patrullaje a Pie / Proximidad"),
    PATRULLAJE_MOVIL("Patrullaje Móvil / Vehicular"),
    CENTRAL_OPERACIONES("Operador de Central y Comunicaciones"),
    PUESTO_FIJO("Servicio en Punto Fijo / Estratégico"),
    SUPERVISION("Supervisión de Turno y Control");

    private final String descripcion;

    RolServicioSerenazgo(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
