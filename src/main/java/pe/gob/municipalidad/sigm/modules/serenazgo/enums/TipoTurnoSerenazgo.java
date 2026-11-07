package pe.gob.municipalidad.sigm.modules.serenazgo.enums;

public enum TipoTurnoSerenazgo {
    DIURNO("Turno Diurno"),
    NOCTURNO("Turno Nocturno"),
    MIXTO("Turno Mixto / Rotativo"),
    ESPECIAL("Turno Especial"),
    EXTRAORDINARIO("Turno Extraordinario");

    private final String descripcion;

    TipoTurnoSerenazgo(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
