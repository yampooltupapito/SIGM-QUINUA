package pe.gob.municipalidad.sigm.modules.serenazgo.enums;

public enum TipoServicioSerenazgo {
    PATRULLAJE_PREVENTIVO("Patrullaje Preventivo Integrado"),
    VIGILANCIA_MUNICIPAL("Vigilancia Municipal / Punto Fijo"),
    APOYO_OPERATIVO("Apoyo Operativo Autorizado"),
    SERVICIO_EXTRAORDINARIO("Servicio Extraordinario Municipal"),
    ACTIVIDAD_INSTITUCIONAL("Actividad Institucional y Protocolar"),
    OTRO("Otro Servicio Autorizado");

    private final String descripcion;

    TipoServicioSerenazgo(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
