package pe.gob.municipalidad.sigm.modules.serenazgo.enums;

public enum EstadoPatrullajeSerenazgo {
    BORRADOR("Borrador"),
    PROGRAMADO("Programado"),
    ASIGNADO("Asignado"),
    EN_CURSO("En Curso / Patrullando"),
    EN_PAUSA("En Pausa"),
    FINALIZADO("Finalizado"),
    CANCELADO("Cancelado"),
    INTERRUMPIDO("Interrumpido por Emergencia");

    private final String descripcion;

    EstadoPatrullajeSerenazgo(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
