package pe.gob.municipalidad.sigm.modules.serenazgo.enums;

public enum ResultadoSupervisionSerenazgo {
    CONFORME("Conforme con los Estándares"),
    CON_OBSERVACIONES("Con Observaciones Menores"),
    NO_CONFORME("No Conforme / Incumplimiento Grave");

    private final String descripcion;

    ResultadoSupervisionSerenazgo(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
