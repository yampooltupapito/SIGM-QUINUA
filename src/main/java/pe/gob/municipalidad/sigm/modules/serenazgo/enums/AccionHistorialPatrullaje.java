package pe.gob.municipalidad.sigm.modules.serenazgo.enums;

public enum AccionHistorialPatrullaje {
    CREACION("Creación"),
    PROGRAMACION("Programación"),
    ASIGNACION("Asignación de Recursos"),
    INICIO("Inicio de Patrullaje"),
    VISITA_PUNTO("Paso por Punto de Control"),
    REGISTRO_NOVEDAD("Registro de Novedad"),
    PAUSA("Pausa Operativa"),
    REANUDACION("Reanudación de Patrullaje"),
    CAMBIO_RUTA("Cambio de Ruta"),
    FINALIZACION("Finalización"),
    CANCELACION("Cancelación");

    private final String descripcion;

    AccionHistorialPatrullaje(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
