package pe.gob.municipalidad.sigm.modules.serenazgo.enums;

public enum EstadoAtencionIncidencia {
    PENDIENTE_ASIGNACION("Pendiente de Asignación"),
    ASIGNADA("Recursos Asignados"),
    DESPACHADA("Despachada a Unidad"),
    ACEPTADA("Aceptada por Unidad"),
    EN_ATENCION("En Atención en Sitio"),
    EN_ESPERA("En Espera / Pausa Operativa"),
    FINALIZADA("Atención Finalizada"),
    CANCELADA("Atención Cancelada");

    private final String descripcion;

    EstadoAtencionIncidencia(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getEtiqueta() {
        return descripcion;
    }
}
