package pe.gob.municipalidad.sigm.modules.serenazgo.enums;

public enum TipoSupervisionSerenazgo {
    RUTINA("Supervisión Ordinaria de Guardia"),
    INOPINADA("Supervisión Inopinada / Control Sorpresivo"),
    CONTROL_CALIDAD("Control de Calidad de Servicio"),
    SEGUIMIENTO("Seguimiento a Medidas Correctivas");

    private final String descripcion;

    TipoSupervisionSerenazgo(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
