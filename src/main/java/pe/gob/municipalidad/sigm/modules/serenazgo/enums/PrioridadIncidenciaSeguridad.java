package pe.gob.municipalidad.sigm.modules.serenazgo.enums;

public enum PrioridadIncidenciaSeguridad {
    BAJA("Baja - Atención preventiva ordinaria"),
    MEDIA("Media - Atención en ruta de patrullaje"),
    ALTA("Alta - Despacho inmediato de unidad móvil"),
    CRITICA("Crítica - Riesgo vital / Concurrencia de auxilio");

    private final String descripcion;

    PrioridadIncidenciaSeguridad(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getEtiqueta() {
        return descripcion;
    }
}
