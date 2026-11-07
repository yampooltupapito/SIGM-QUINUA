package pe.gob.municipalidad.sigm.modules.serenazgo.enums;

public enum EstadoEquipamientoSerenazgo {
    DISPONIBLE("Disponible en Base/Almacén"),
    ASIGNADO("Asignado a Efectivo o Unidad"),
    EN_MANTENIMIENTO("En Mantenimiento Técnico"),
    EN_REPARACION("En Reparación"),
    DE_BAJA("Dado de Baja Definitiva"),
    PERDIDO("Extraviado o No Hallado");

    private final String descripcion;

    EstadoEquipamientoSerenazgo(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public boolean isDisponibleAsignacion() {
        return this == DISPONIBLE;
    }
}
