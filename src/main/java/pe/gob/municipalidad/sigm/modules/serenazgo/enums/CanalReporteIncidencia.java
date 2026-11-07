package pe.gob.municipalidad.sigm.modules.serenazgo.enums;

public enum CanalReporteIncidencia {
    LLAMADA_OPERADOR("Llamada Telefónica a Central"),
    ALERTA_CIUDADANA("Alerta Digital de Portal Ciudadano"),
    PATRULLAJE("Avistamiento en Patrullaje"),
    PRESENCIAL_BASE("Reporte Presencial en Base"),
    RADIO_COMUNICACION("Enlace Radial / Interinstitucional");

    private final String descripcion;

    CanalReporteIncidencia(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
