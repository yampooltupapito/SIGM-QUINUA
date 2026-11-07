package pe.gob.municipalidad.sigm.modules.serenazgo.enums;

public enum CategoriaIncidenciaSeguridad {
    ORDEN_PUBLICO("Orden Público y Convivencia"),
    SEGURIDAD_VIAL("Seguridad y Tránsito Vial"),
    APOYO_COMUNITARIO("Apoyo Comunitario y Social"),
    RIESGO_URBANO("Riesgo e Infraestructura Urbana"),
    EMERGENCIAS("Emergencias y Urgencias Médicas"),
    OTRO("Otras Ocurrencias");

    private final String descripcion;

    CategoriaIncidenciaSeguridad(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
