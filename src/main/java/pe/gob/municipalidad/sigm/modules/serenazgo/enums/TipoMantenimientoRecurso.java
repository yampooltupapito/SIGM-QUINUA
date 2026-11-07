package pe.gob.municipalidad.sigm.modules.serenazgo.enums;

public enum TipoMantenimientoRecurso {
    PREVENTIVO("Mantenimiento Preventivo Periódico"),
    CORRECTIVO("Mantenimiento Correctivo por Falla"),
    INSPECCION_TECNICA("Inspección Técnica / Revisión Oficial"),
    OTRO("Otro Servicio Técnico");

    private final String descripcion;

    TipoMantenimientoRecurso(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
