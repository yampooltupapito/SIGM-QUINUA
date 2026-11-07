package pe.gob.municipalidad.sigm.modules.serenazgo.enums;

public enum TipoIncidenciaTecnica {
    AVERIA("Avería Mecánica o Eléctrica"),
    DANO("Daño Físico o Estructural"),
    PERDIDA_REPORTADA("Pérdida o Extravío Reportado"),
    FALLA_FUNCIONAMIENTO("Falla de Funcionamiento / Batería"),
    OBSERVACION_SEGURIDAD("Observación Crítica de Seguridad"),
    OTRO("Otra Incidencia Técnica");

    private final String descripcion;

    TipoIncidenciaTecnica(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
