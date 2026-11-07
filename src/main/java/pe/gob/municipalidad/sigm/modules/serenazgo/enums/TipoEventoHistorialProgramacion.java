package pe.gob.municipalidad.sigm.modules.serenazgo.enums;

public enum TipoEventoHistorialProgramacion {
    CREACION("Creación de Programación"),
    CONFIRMACION("Confirmación y Publicación del Servicio"),
    REPROGRAMACION("Reprogramación Operativa"),
    CANCELACION("Cancelación del Servicio"),
    INICIO("Inicio de Servicio"),
    FINALIZACION("Finalización del Servicio"),
    ASIGNACION_PERSONAL("Asignación de Personal"),
    DESASIGNACION_PERSONAL("Desasignación / Sustitución de Personal"),
    CAMBIO_RESPONSABLE("Cambio de Responsable Operativo"),
    RESERVA_UNIDAD("Reserva de Unidad Móvil"),
    RESERVA_EQUIPAMIENTO("Reserva de Equipamiento"),
    CORRECCION_ADMINISTRATIVA("Corrección Administrativa");

    private final String descripcion;

    TipoEventoHistorialProgramacion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
