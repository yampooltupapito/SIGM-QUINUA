package pe.gob.municipalidad.sigm.modules.serenazgo.enums;

public enum CanalAlertaCiudadana {
    PORTAL_CIUDADANO("Portal Web Ciudadano"),
    BOTON_PANICO("Botón de Pánico Digital"),
    ATENCION_PRESENCIAL("Mesa de Entrada / Guardia"),
    LLAMADA_REGISTRADA("Llamada Registrada por Operador"),
    OTRO("Otro Canal Oficial");

    private final String descripcion;

    CanalAlertaCiudadana(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
